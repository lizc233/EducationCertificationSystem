package com.educationcertificationsystem.vo.org;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OrgTreeNodeVO {

    private String id;

    private String label;

    private String type;

    private List<OrgTreeNodeVO> children = new ArrayList<>();
}
