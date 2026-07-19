package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("course_selection_task")
public class CourseSelectionTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskCode;

    private String term;

    private String courseName;

    private String teacherName;

    private BigDecimal credit;

    private Integer capacity;

    private LocalDateTime selectionStartTime;

    private LocalDateTime selectionEndTime;

    private String taskStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer isDeleted;

    private String remark;
}
