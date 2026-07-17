package com.educationcertificationsystem.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.model.entity.EduStudent;
import com.educationcertificationsystem.course.service.EduStudentService;
import com.educationcertificationsystem.course.mapper.EduStudentMapper;
import org.springframework.stereotype.Service;

/**
* @author Lizc233
* @description 针对表【edu_student(学生表)】的数据库操作Service实现
* @createDate 2026-07-16 14:29:32
*/
@Service
public class EduStudentServiceImpl extends ServiceImpl<EduStudentMapper, EduStudent>
    implements EduStudentService{

}




