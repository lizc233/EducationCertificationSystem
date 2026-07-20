package com.educationcertificationsystem.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.model.entity.Manager;
import com.educationcertificationsystem.user.mapper.ManagerMapper;
import com.educationcertificationsystem.user.service.ManagerService;
import org.springframework.stereotype.Service;

/**
* @author Codex
 * @description 针对表【edu_manager】的数据库操作Service实现
*/
@Service
public class ManagerServiceImpl extends ServiceImpl<ManagerMapper, Manager>
    implements ManagerService {

}
