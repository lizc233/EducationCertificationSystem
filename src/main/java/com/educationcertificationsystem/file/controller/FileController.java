package com.educationcertificationsystem.file.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.model.entity.SysFile;
import com.educationcertificationsystem.file.service.FileStorageService;
import com.educationcertificationsystem.file.service.SysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final SysFileService sysFileService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public Result<Page<SysFile>> list(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String bizType,
                                      @RequestParam(required = false) Long bizId,
                                      @RequestParam(required = false) Long uploadUserId,
                                      @RequestParam(required = false) String visibilityScope,
                                      @RequestParam(required = false) Integer fileStatus,
                                      @RequestParam(required = false) String keyword) {
        QueryWrapper<SysFile> wrapper = activeWrapper();
        if (StringUtils.hasText(bizType)) {
            wrapper.eq("biz_type", bizType);
        }
        if (bizId != null) {
            wrapper.eq("biz_id", bizId);
        }
        if (uploadUserId != null) {
            wrapper.eq("upload_user_id", uploadUserId);
        }
        if (StringUtils.hasText(visibilityScope)) {
            wrapper.eq("visibility_scope", visibilityScope);
        }
        if (fileStatus != null) {
            wrapper.eq("file_status", fileStatus);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("original_name", keyword)
                    .or().like("stored_name", keyword)
                    .or().like("remark", keyword));
        }
        wrapper.orderByDesc("id");
        Page<SysFile> result = sysFileService.page(new Page<>(page, size), wrapper);
        decorateDownloadUrl(result.getRecords());
        return Result.success(result);
    }

    @PostMapping("/upload")
    public Result<SysFile> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam Long uploadUserId,
                                  @RequestParam(required = false) String bizType,
                                  @RequestParam(required = false) Long bizId,
                                  @RequestParam(required = false) String visibilityScope,
                                  @RequestParam(required = false) String remark) {
        try {
            SysFile saved = fileStorageService.store(file, bizType, bizId, uploadUserId, visibilityScope, remark);
            decorateDownloadUrl(saved);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<SysFile> detail(@PathVariable Long id) {
        try {
            SysFile file = fileStorageService.requireFile(id);
            decorateDownloadUrl(file);
            return Result.success(file);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        SysFile file;
        try {
            file = fileStorageService.requireFile(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        Resource resource = fileStorageService.loadAsResource(file);
        Path path = Paths.get(file.getStoragePath()).toAbsolutePath().normalize();
        String fileName = StringUtils.hasText(file.getOriginalName()) ? file.getOriginalName() : path.getFileName().toString();
        MediaType mediaType = MediaTypeFactory.getMediaType(fileName).orElse(MediaType.APPLICATION_OCTET_STREAM);
        long contentLength;
        try {
            contentLength = Files.size(path);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件已丢失");
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(contentLength)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileName, StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            boolean deleted = fileStorageService.deleteFile(id);
            if (!deleted) {
                return Result.error("文件仍被业务记录引用，无法删除");
            }
            return Result.success();
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    private QueryWrapper<SysFile> activeWrapper() {
        return new QueryWrapper<SysFile>().and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"));
    }

    private void decorateDownloadUrl(SysFile file) {
        if (file != null) {
            file.setDownloadUrl("/api/files/" + file.getId() + "/download");
        }
    }

    private void decorateDownloadUrl(List<SysFile> files) {
        if (files == null) {
            return;
        }
        for (SysFile file : files) {
            decorateDownloadUrl(file);
        }
    }
}
