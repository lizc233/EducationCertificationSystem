package com.educationcertificationsystem.common;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private Long total;

    private Long pageNum;

    private Long pageSize;

    private List<T> records;
}
