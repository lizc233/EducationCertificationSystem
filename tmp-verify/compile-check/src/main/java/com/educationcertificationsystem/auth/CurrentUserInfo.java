package com.educationcertificationsystem.auth;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserInfo {

    private Long userId;

    private String accountId;

    private String realName;

    private Set<String> roleCodes;

    public boolean hasAnyRole(String[] roles) {
        if (roles == null || roles.length == 0) {
            return true;
        }
        for (String role : roles) {
            if (roleCodes.contains(role)) {
                return true;
            }
        }
        return false;
    }
}
