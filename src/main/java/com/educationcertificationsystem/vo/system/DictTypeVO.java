package com.educationcertificationsystem.vo.system;

import java.util.List;
import lombok.Data;

@Data
public class DictTypeVO {

    private Long id;

    private String dictType;

    private String dictName;

    private Integer status;

    private String remark;

    private List<DictItemVO> items;
}
