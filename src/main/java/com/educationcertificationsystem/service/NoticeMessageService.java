package com.educationcertificationsystem.service;

import com.educationcertificationsystem.entity.NoticeMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Lizc233
* @description 针对表【notice_message(通知消息表)】的数据库操作Service
* @createDate 2026-07-16 14:29:34
*/
public interface NoticeMessageService extends IService<NoticeMessage> {

    public long count();
}
