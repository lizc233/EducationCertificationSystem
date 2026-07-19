package com.educationcertificationsystem.dto.selection;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class CourseSelectionTaskSaveRequest {

    @NotBlank(message = "学期不能为空")
    private String term;

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    @NotBlank(message = "授课教师不能为空")
    private String teacherName;

    @NotNull(message = "学分不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "学分必须大于 0")
    private BigDecimal credit;

    @NotNull(message = "容量不能为空")
    private Integer capacity;

    @NotNull(message = "开始时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime selectionStartTime;

    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime selectionEndTime;

    private String remark;
}
