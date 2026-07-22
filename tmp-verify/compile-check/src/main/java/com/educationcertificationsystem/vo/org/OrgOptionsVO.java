package com.educationcertificationsystem.vo.org;

import java.util.List;
import lombok.Data;

@Data
public class OrgOptionsVO {

    private List<OptionVO> colleges;

    private List<OptionVO> majors;

    private List<OptionVO> grades;

    private List<OptionVO> classes;

    private List<OptionVO> teachers;

    private List<OptionVO> teacherUsers;

    private List<OptionVO> studentUsers;
}
