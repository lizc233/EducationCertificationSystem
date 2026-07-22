package com.educationcertificationsystem.vo.org;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionVO {

    private Long id;

    private String label;

    private Long parentId;

    public OptionVO(Long id, String label) {
        this(id, label, null);
    }
}
