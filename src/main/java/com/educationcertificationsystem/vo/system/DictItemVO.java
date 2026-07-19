package com.educationcertificationsystem.vo.system;

import lombok.Data;

@Data
public class DictItemVO {

    private Long id;

    private Long dictTypeId;

    private String itemLabel;

    private String itemValue;

    private Integer itemSort;

    private Integer isDefault;

    private Integer status;

    private String remark;
}
