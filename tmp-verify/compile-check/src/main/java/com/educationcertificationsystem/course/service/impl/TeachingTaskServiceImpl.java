package com.educationcertificationsystem.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.model.entity.TeachingTask;
import com.educationcertificationsystem.course.service.TeachingTaskService;
import com.educationcertificationsystem.course.mapper.TeachingTaskMapper;
import org.springframework.stereotype.Service;

/**
* @author Lizc233
* @description 针对表【teaching_task(授课任务表)】的数据库操作Service实现
* @createDate 2026-07-16 14:29:34
*/
@Service
public class TeachingTaskServiceImpl extends ServiceImpl<TeachingTaskMapper, TeachingTask>
    implements TeachingTaskService{

}




