package com.educationcertificationsystem.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LookupOption {
    private Long value;
    private String label;
    private Long parentValue;

    public LookupOption(Long value, String label) {
        this(value, label, null);
    }

    public LookupOption(Long value, String label, Long parentValue) {
        this.value = value;
        this.label = label;
        this.parentValue = parentValue;
    }
}
