package com.educationcertificationsystem.file.service;

import com.educationcertificationsystem.model.entity.SysFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    SysFile store(MultipartFile file,
                  String bizType,
                  Long bizId,
                  Long uploadUserId,
                  String visibilityScope,
                  String remark);

    SysFile requireFile(Long fileId);

    Resource loadAsResource(SysFile file);

    boolean deleteFile(Long fileId);

    void bindFile(Long fileId, String bizType, Long bizId);
}
