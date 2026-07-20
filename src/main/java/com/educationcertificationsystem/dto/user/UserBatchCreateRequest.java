package com.educationcertificationsystem.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class UserBatchCreateRequest {

    @Valid
    @NotEmpty(message = "批量用户数据不能为空")
    private List<UserSaveRequest> users;
}
