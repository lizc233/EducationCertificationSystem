package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 管理员表
 * @TableName edu_manager
 */
@TableName(value = "edu_manager")
@Data
public class Manager {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String adminNo;

    private String departmentName;

    private String positionName;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer isDeleted;

    private String remark;
}
