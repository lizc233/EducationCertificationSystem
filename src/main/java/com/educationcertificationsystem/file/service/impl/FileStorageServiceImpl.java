package com.educationcertificationsystem.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.educationcertificationsystem.model.entity.CourseEvidenceMaterial;
import com.educationcertificationsystem.model.entity.CourseResource;
import com.educationcertificationsystem.model.entity.ImprovePlanRecord;
import com.educationcertificationsystem.model.entity.ReportExportLog;
import com.educationcertificationsystem.model.entity.SysFile;
import com.educationcertificationsystem.course.service.CourseEvidenceMaterialService;
import com.educationcertificationsystem.course.service.CourseResourceService;
import com.educationcertificationsystem.file.service.FileStorageService;
import com.educationcertificationsystem.improve.service.ImprovePlanRecordService;
import com.educationcertificationsystem.report.service.ReportExportLogService;
import com.educationcertificationsystem.file.service.SysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final String DEFAULT_BIZ_TYPE = "GENERAL";
    private static final String DEFAULT_VISIBILITY_SCOPE = "PRIVATE";
    private static final String STORAGE_TYPE_LOCAL = "LOCAL";

    private final SysFileService sysFileService;
    private final CourseResourceService courseResourceService;
    private final CourseEvidenceMaterialService courseEvidenceMaterialService;
    private final ReportExportLogService reportExportLogService;
    private final ImprovePlanRecordService improvePlanRecordService;

    @Value("${app.file-storage.root-path:D:/javacode/Project/downloadtest}")
    private String rootPath;

    @Value("${app.file-storage.max-size-bytes:104857600}")
    private long maxSizeBytes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFile store(MultipartFile file,
                         String bizType,
                         Long bizId,
                         Long uploadUserId,
                         String visibilityScope,
                         String remark) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (uploadUserId == null) {
            throw new IllegalArgumentException("uploadUserId 不能为空");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("文件大小超过限制");
        }

        Path root = storageRoot();
        String originalName = normalizeOriginalName(file.getOriginalFilename());
        String fileExt = StringUtils.getFilenameExtension(originalName);
        String storedName = buildStoredName(fileExt);
        Path target = root.resolve(storedName).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalStateException("非法文件路径");
        }

        try {
            Files.createDirectories(root);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            try (InputStream inputStream = new BufferedInputStream(file.getInputStream());
                 DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);
                 OutputStream outputStream = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                digestInputStream.transferTo(outputStream);
            }

            SysFile sysFile = new SysFile();
            sysFile.setBizType(hasText(bizType) ? bizType : DEFAULT_BIZ_TYPE);
            sysFile.setBizId(bizId);
            sysFile.setOriginalName(originalName);
            sysFile.setStoredName(storedName);
            sysFile.setFileExt(fileExt);
            sysFile.setFileSize(Files.size(target));
            sysFile.setMimeType(hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream");
            sysFile.setStorageType(STORAGE_TYPE_LOCAL);
            sysFile.setStoragePath(target.toAbsolutePath().toString());
            sysFile.setMd5(HexFormat.of().formatHex(digest.digest()));
            sysFile.setUploadUserId(uploadUserId);
            sysFile.setVisibilityScope(hasText(visibilityScope) ? visibilityScope : DEFAULT_VISIBILITY_SCOPE);
            sysFile.setFileStatus(1);
            sysFile.setCreatedAt(LocalDateTime.now());
            sysFile.setUpdatedAt(LocalDateTime.now());
            sysFile.setIsDeleted(0);
            sysFile.setRemark(remark);
            sysFileService.save(sysFile);
            return sysFile;
        } catch (IOException | NoSuchAlgorithmException ex) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
                // Ignore cleanup failures and rethrow the original error.
            }
            throw new IllegalStateException("文件上传失败", ex);
        }
    }

    @Override
    public SysFile requireFile(Long fileId) {
        SysFile file = sysFileService.getById(fileId);
        if (file == null || Integer.valueOf(1).equals(file.getIsDeleted()) || Integer.valueOf(0).equals(file.getFileStatus())) {
            throw new IllegalArgumentException("文件不存在或已删除");
        }
        return file;
    }

    @Override
    public Resource loadAsResource(SysFile file) {
        try {
            Path path = Paths.get(file.getStoragePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("文件已丢失或不可读");
            }
            return resource;
        } catch (IOException ex) {
            throw new IllegalStateException("读取文件失败", ex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(Long fileId) {
        SysFile file = sysFileService.getById(fileId);
        if (file == null || Integer.valueOf(1).equals(file.getIsDeleted())) {
            return false;
        }
        if (hasActiveReference(fileId)) {
            return false;
        }
        try {
            Files.deleteIfExists(Paths.get(file.getStoragePath()));
        } catch (IOException ex) {
            throw new IllegalStateException("删除文件失败", ex);
        }
        file.setFileStatus(0);
        file.setIsDeleted(1);
        file.setUpdatedAt(LocalDateTime.now());
        sysFileService.updateById(file);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindFile(Long fileId, String bizType, Long bizId) {
        SysFile file = requireFile(fileId);
        file.setBizType(hasText(bizType) ? bizType : DEFAULT_BIZ_TYPE);
        file.setBizId(bizId);
        file.setUpdatedAt(LocalDateTime.now());
        sysFileService.updateById(file);
    }

    private Path storageRoot() {
        return Paths.get(rootPath).toAbsolutePath().normalize();
    }

    private String buildStoredName(String fileExt) {
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String suffix = UUID.randomUUID().toString().replace("-", "");
        return hasText(fileExt) ? prefix + "_" + suffix + "." + fileExt : prefix + "_" + suffix;
    }

    private String normalizeOriginalName(String originalName) {
        if (!hasText(originalName)) {
            return "upload.bin";
        }
        String cleaned = StringUtils.getFilename(originalName);
        return hasText(cleaned) ? cleaned : originalName;
    }

    private boolean hasActiveReference(Long fileId) {
        return hasCourseResourceReference(fileId)
                || hasCourseEvidenceMaterialReference(fileId)
                || hasReportExportReference(fileId)
                || hasImprovePlanRecordReference(fileId);
    }

    private boolean hasCourseResourceReference(Long fileId) {
        return courseResourceService.count(new QueryWrapper<CourseResource>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("file_id", fileId)) > 0;
    }

    private boolean hasCourseEvidenceMaterialReference(Long fileId) {
        return courseEvidenceMaterialService.count(new QueryWrapper<CourseEvidenceMaterial>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("file_id", fileId)) > 0;
    }

    private boolean hasReportExportReference(Long fileId) {
        return reportExportLogService.count(new QueryWrapper<ReportExportLog>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("file_id", fileId)) > 0;
    }

    private boolean hasImprovePlanRecordReference(Long fileId) {
        return improvePlanRecordService.count(new QueryWrapper<ImprovePlanRecord>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("attachment_file_id", fileId)) > 0;
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }
}
