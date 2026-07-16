package com.educationcertificationsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.entity.NoticeMessage;
import com.educationcertificationsystem.service.NoticeMessageService;
import com.educationcertificationsystem.mapper.NoticeMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author Lizc233
* @description 针对表【notice_message(通知消息表)】的数据库操作Service实现
* @createDate 2026-07-16 14:29:34
*/
@Service
public class NoticeMessageServiceImpl extends ServiceImpl<NoticeMessageMapper, NoticeMessage>
    implements NoticeMessageService{

    @Override
    public long count() {
        return 0;
    }
}




