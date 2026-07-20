package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("course_selection_record")
public class CourseSelectionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long studentUserId;

    private String studentNo;

    private String studentName;

    private String className;

    private String selectStatus;

    private LocalDateTime selectedAt;

    private LocalDateTime droppedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer isDeleted;

    private String remark;
}
