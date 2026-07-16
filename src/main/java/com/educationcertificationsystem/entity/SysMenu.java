package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 菜单表
 * @TableName sys_menu
 */
@TableName(value ="sys_menu")
@Data
public class SysMenu {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long parentId;

    /**
     * 
     */
    private String menuType;

    /**
     * 
     */
    private String menuName;

    /**
     * 
     */
    private String routePath;

    /**
     * 
     */
    private String componentPath;

    /**
     * 
     */
    private String permissionCode;

    /**
     * 
     */
    private String icon;

    /**
     * 
     */
    private Integer sortNo;

    /**
     * 
     */
    private Integer visible;

    /**
     * 
     */
    private Integer status;

    /**
     * 
     */
    private LocalDateTime createdAt;

    /**
     * 
     */
    private LocalDateTime updatedAt;

    /**
     * 
     */
    private Integer isDeleted;

    /**
     * 
     */
    private String remark;
}