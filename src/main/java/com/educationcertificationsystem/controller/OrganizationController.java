package com.educationcertificationsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.educationcertificationsystem.auth.RequireRoles;
import com.educationcertificationsystem.auth.RoleConstants;
import com.educationcertificationsystem.common.ApiResponse;
import com.educationcertificationsystem.common.BusinessException;
import com.educationcertificationsystem.dto.org.ClassSaveRequest;
import com.educationcertificationsystem.dto.org.CollegeSaveRequest;
import com.educationcertificationsystem.dto.org.GradeSaveRequest;
import com.educationcertificationsystem.dto.org.MajorSaveRequest;
import com.educationcertificationsystem.dto.org.StudentBindingSaveRequest;
import com.educationcertificationsystem.dto.org.TeacherBindingSaveRequest;
import com.educationcertificationsystem.course.service.EduStudentService;
import com.educationcertificationsystem.course.service.EduTeacherService;
import com.educationcertificationsystem.model.entity.EduStudent;
import com.educationcertificationsystem.model.entity.EduTeacher;
import com.educationcertificationsystem.model.entity.OrgClass;
import com.educationcertificationsystem.model.entity.OrgCollege;
import com.educationcertificationsystem.model.entity.OrgGrade;
import com.educationcertificationsystem.model.entity.OrgMajor;
import com.educationcertificationsystem.model.entity.SysRole;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.model.entity.SysUserRole;
import com.educationcertificationsystem.org.service.OrgClassService;
import com.educationcertificationsystem.org.service.OrgCollegeService;
import com.educationcertificationsystem.org.service.OrgGradeService;
import com.educationcertificationsystem.org.service.OrgMajorService;
import com.educationcertificationsystem.role.service.SysRoleService;
import com.educationcertificationsystem.role.service.SysUserRoleService;
import com.educationcertificationsystem.user.service.SysUserService;
import com.educationcertificationsystem.vo.org.OptionVO;
import com.educationcertificationsystem.vo.org.OrgClassVO;
import com.educationcertificationsystem.vo.org.OrgCollegeVO;
import com.educationcertificationsystem.vo.org.OrgGradeVO;
import com.educationcertificationsystem.vo.org.OrgMajorVO;
import com.educationcertificationsystem.vo.org.OrgOptionsVO;
import com.educationcertificationsystem.vo.org.OrgOverviewItemVO;
import com.educationcertificationsystem.vo.org.OrgStudentBindingVO;
import com.educationcertificationsystem.vo.org.OrgTeacherBindingVO;
import com.educationcertificationsystem.vo.org.OrgTreeNodeVO;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/org")
@RequireRoles(RoleConstants.SUPER_ADMIN)
public class OrganizationController {

    private final OrgCollegeService collegeService;

    private final OrgMajorService majorService;

    private final OrgGradeService gradeService;

    private final OrgClassService classService;

    private final EduTeacherService teacherService;

    private final EduStudentService studentService;

    private final SysUserService userService;

    private final SysUserRoleService userRoleService;

    private final SysRoleService roleService;

    public OrganizationController(
        OrgCollegeService collegeService,
        OrgMajorService majorService,
        OrgGradeService gradeService,
        OrgClassService classService,
        EduTeacherService teacherService,
        EduStudentService studentService,
        SysUserService userService,
        SysUserRoleService userRoleService,
        SysRoleService roleService
    ) {
        this.collegeService = collegeService;
        this.majorService = majorService;
        this.gradeService = gradeService;
        this.classService = classService;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.roleService = roleService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<OrgTreeNodeVO>> tree() {
        List<OrgCollege> colleges = listColleges();
        List<OrgMajor> majors = listMajors();
        List<OrgClass> classes = listClasses();

        OrgTreeNodeVO root = new OrgTreeNodeVO();
        root.setId("root");
        root.setLabel("瀛︽牎");
        root.setType("瀛︽牎");

        Map<Long, List<OrgMajor>> majorGroup = majors.stream().collect(Collectors.groupingBy(OrgMajor::getCollegeId));
        Map<Long, List<OrgClass>> classGroup = classes.stream().collect(Collectors.groupingBy(OrgClass::getMajorId));

        for (OrgCollege college : colleges) {
            OrgTreeNodeVO collegeNode = new OrgTreeNodeVO();
            collegeNode.setId("college-" + college.getId());
            collegeNode.setLabel(college.getCollegeName());
            collegeNode.setType("闄㈢郴");

            for (OrgMajor major : majorGroup.getOrDefault(college.getId(), List.of())) {
                OrgTreeNodeVO majorNode = new OrgTreeNodeVO();
                majorNode.setId("major-" + major.getId());
                majorNode.setLabel(major.getMajorName());
                majorNode.setType("涓撲笟");

                for (OrgClass clazz : classGroup.getOrDefault(major.getId(), List.of())) {
                    OrgTreeNodeVO classNode = new OrgTreeNodeVO();
                    classNode.setId("class-" + clazz.getId());
                    classNode.setLabel(clazz.getClassName());
                    classNode.setType("鐝骇");
                    majorNode.getChildren().add(classNode);
                }
                collegeNode.getChildren().add(majorNode);
            }
            root.getChildren().add(collegeNode);
        }
        return ApiResponse.success(List.of(root));
    }

    @GetMapping("/overview")
    public ApiResponse<List<OrgOverviewItemVO>> overview() {
        Map<Long, OrgCollege> collegeMap = listColleges().stream().collect(Collectors.toMap(OrgCollege::getId, Function.identity()));
        Map<Long, List<OrgClass>> classGroup = listClasses().stream().collect(Collectors.groupingBy(OrgClass::getMajorId));
        Map<Long, String> teacherNameMap = buildTeacherNameMap();

        List<OrgOverviewItemVO> rows = new ArrayList<>();
        for (OrgMajor major : listMajors()) {
            OrgOverviewItemVO item = new OrgOverviewItemVO();
            item.setId("major-" + major.getId());
            item.setName(major.getMajorName());
            item.setType("涓撲笟");
            item.setDirector(collegeMap.getOrDefault(major.getCollegeId(), new OrgCollege()).getCollegeName());
            item.setStudents(classGroup.getOrDefault(major.getId(), List.of()).stream().mapToInt(OrgClass::getStudentCount).sum());
            item.setUpdatedAt(major.getUpdatedAt());
            rows.add(item);
        }
        for (OrgClass clazz : listClasses()) {
            OrgOverviewItemVO item = new OrgOverviewItemVO();
            item.setId("class-" + clazz.getId());
            item.setName(clazz.getClassName());
            item.setType("鐝骇");
            item.setDirector(teacherNameMap.getOrDefault(clazz.getHeadTeacherId(), "-"));
            item.setStudents(clazz.getStudentCount());
            item.setUpdatedAt(clazz.getUpdatedAt());
            rows.add(item);
        }
        rows.sort(Comparator.comparing(OrgOverviewItemVO::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return ApiResponse.success(rows);
    }

    @GetMapping("/options")
    public ApiResponse<OrgOptionsVO> options() {
        OrgOptionsVO options = new OrgOptionsVO();
        Map<Long, String> majorMap = listMajors().stream().collect(Collectors.toMap(OrgMajor::getId, OrgMajor::getMajorName));
        options.setColleges(listColleges().stream().map(item -> new OptionVO(item.getId(), item.getCollegeName())).toList());
        options.setMajors(listMajors().stream().map(item -> new OptionVO(item.getId(), item.getMajorName(), item.getCollegeId())).toList());
        options.setGrades(listGrades().stream().map(item -> new OptionVO(
            item.getId(),
            item.getGradeYear() + "级" + (StringUtils.hasText(majorMap.get(item.getMajorId())) ? " / " + majorMap.get(item.getMajorId()) : ""),
            item.getMajorId()
        )).toList());
        options.setClasses(listClasses().stream().map(item -> new OptionVO(item.getId(), item.getClassName(), item.getMajorId())).toList());
        options.setTeachers(buildTeacherOptions());
        options.setTeacherUsers(buildUserOptions(RoleConstants.TEACHER));
        options.setStudentUsers(buildUserOptions(RoleConstants.STUDENT));
        return ApiResponse.success(options);
    }

    @GetMapping("/colleges")
    public ApiResponse<List<OrgCollegeVO>> colleges() {
        return ApiResponse.success(listColleges().stream().map(this::toCollegeVO).toList());
    }

    @PostMapping("/colleges")
    public ApiResponse<Void> createCollege(@Valid @RequestBody CollegeSaveRequest request) {
        saveCollege(null, request);
        return ApiResponse.success();
    }

    @PutMapping("/colleges/{id}")
    public ApiResponse<Void> updateCollege(@PathVariable Long id, @Valid @RequestBody CollegeSaveRequest request) {
        saveCollege(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/colleges/{id}")
    public ApiResponse<Void> deleteCollege(@PathVariable Long id) {
        OrgCollege college = getCollege(id);
        LocalDateTime now = LocalDateTime.now();
        List<OrgMajor> majors = listMajors().stream().filter(item -> id.equals(item.getCollegeId())).toList();
        List<Long> majorIds = majors.stream().map(OrgMajor::getId).toList();
        List<OrgGrade> grades = listGrades().stream().filter(item -> majorIds.contains(item.getMajorId())).toList();
        List<Long> gradeIds = grades.stream().map(OrgGrade::getId).toList();
        List<OrgClass> classes = listClasses().stream()
            .filter(item -> majorIds.contains(item.getMajorId()) || gradeIds.contains(item.getGradeId()))
            .toList();
        softDeleteClasses(classes, now);
        softDeleteGrades(grades, now);
        softDeleteMajors(majors, now);
        softDeleteCollege(college, now);
        return ApiResponse.success();
    }

    @GetMapping("/majors")
    public ApiResponse<List<OrgMajorVO>> majors() {
        Map<Long, String> collegeMap = listColleges().stream().collect(Collectors.toMap(OrgCollege::getId, OrgCollege::getCollegeName));
        return ApiResponse.success(listMajors().stream().map(item -> toMajorVO(item, collegeMap)).toList());
    }

    @PostMapping("/majors")
    public ApiResponse<Void> createMajor(@Valid @RequestBody MajorSaveRequest request) {
        saveMajor(null, request);
        return ApiResponse.success();
    }

    @PutMapping("/majors/{id}")
    public ApiResponse<Void> updateMajor(@PathVariable Long id, @Valid @RequestBody MajorSaveRequest request) {
        saveMajor(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/majors/{id}")
    public ApiResponse<Void> deleteMajor(@PathVariable Long id) {
        OrgMajor major = getMajor(id);
        LocalDateTime now = LocalDateTime.now();
        List<OrgGrade> grades = listGrades().stream().filter(item -> id.equals(item.getMajorId())).toList();
        List<Long> gradeIds = grades.stream().map(OrgGrade::getId).toList();
        List<OrgClass> classes = listClasses().stream()
            .filter(item -> id.equals(item.getMajorId()) || gradeIds.contains(item.getGradeId()))
            .toList();
        softDeleteClasses(classes, now);
        softDeleteGrades(grades, now);
        softDeleteMajor(major, now);
        return ApiResponse.success();
    }


    @GetMapping("/grades")
    public ApiResponse<List<OrgGradeVO>> grades() {
        Map<Long, OrgMajor> majorMap = listMajors().stream().collect(Collectors.toMap(OrgMajor::getId, Function.identity()));
        Map<Long, String> collegeMap = listColleges().stream().collect(Collectors.toMap(OrgCollege::getId, OrgCollege::getCollegeName));
        return ApiResponse.success(listGrades().stream().map(item -> toGradeVO(item, majorMap, collegeMap)).toList());
    }

    @PostMapping("/grades")
    public ApiResponse<Void> createGrade(@Valid @RequestBody GradeSaveRequest request) {
        saveGrade(null, request);
        return ApiResponse.success();
    }

    @PutMapping("/grades/{id}")
    public ApiResponse<Void> updateGrade(@PathVariable Long id, @Valid @RequestBody GradeSaveRequest request) {
        saveGrade(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/grades/{id}")
    public ApiResponse<Void> deleteGrade(@PathVariable Long id) {
        OrgGrade grade = getGrade(id);
        LocalDateTime now = LocalDateTime.now();
        List<OrgClass> classes = listClasses().stream().filter(item -> id.equals(item.getGradeId())).toList();
        softDeleteClasses(classes, now);
        softDeleteGrade(grade, now);
        return ApiResponse.success();
    }


    @GetMapping("/classes")
    public ApiResponse<List<OrgClassVO>> classes() {
        Map<Long, String> teacherNameMap = buildTeacherNameMap();
        Map<Long, OrgMajor> majorMap = listMajors().stream().collect(Collectors.toMap(OrgMajor::getId, Function.identity()));
        Map<Long, OrgGrade> gradeMap = listGrades().stream().collect(Collectors.toMap(OrgGrade::getId, Function.identity()));
        Map<Long, String> collegeMap = listColleges().stream().collect(Collectors.toMap(OrgCollege::getId, OrgCollege::getCollegeName));
        return ApiResponse.success(listClasses().stream().map(item -> toClassVO(item, teacherNameMap, majorMap, gradeMap, collegeMap)).toList());
    }

    @PostMapping("/classes")
    public ApiResponse<Void> createClass(@Valid @RequestBody ClassSaveRequest request) {
        saveClass(null, request);
        return ApiResponse.success();
    }

    @PutMapping("/classes/{id}")
    public ApiResponse<Void> updateClass(@PathVariable Long id, @Valid @RequestBody ClassSaveRequest request) {
        saveClass(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/classes/{id}")
    public ApiResponse<Void> deleteClass(@PathVariable Long id) {
        if (studentService.count(new LambdaQueryWrapper<EduStudent>().eq(EduStudent::getClassId, id).eq(EduStudent::getIsDeleted, 0)) > 0) {
            throw new BusinessException("褰撳墠鐝骇涓嬩粛鏈夊鐢熺粦瀹氾紝涓嶈兘鍒犻櫎");
        }
        OrgClass clazz = getClassEntity(id);
        clazz.setIsDeleted(1);
        clazz.setUpdatedAt(LocalDateTime.now());
        classService.updateById(clazz);
        return ApiResponse.success();
    }

    @GetMapping("/teacher-bindings")
    public ApiResponse<List<OrgTeacherBindingVO>> teacherBindings() {
        Map<Long, SysUser> userMap = listUsersByRole(RoleConstants.TEACHER).stream().collect(Collectors.toMap(SysUser::getId, item -> item));
        Map<Long, String> collegeMap = listColleges().stream().collect(Collectors.toMap(OrgCollege::getId, OrgCollege::getCollegeName));
        Map<Long, String> majorMap = listMajors().stream().collect(Collectors.toMap(OrgMajor::getId, OrgMajor::getMajorName));
        List<OrgTeacherBindingVO> rows = teacherService.list(new LambdaQueryWrapper<EduTeacher>()
                .eq(EduTeacher::getIsDeleted, 0)
                .orderByAsc(EduTeacher::getTeacherNo))
            .stream()
            .map(item -> toTeacherBindingVO(item, userMap, collegeMap, majorMap))
            .toList();
        return ApiResponse.success(rows);
    }

    @PostMapping("/teacher-bindings")
    public ApiResponse<Void> createTeacherBinding(@Valid @RequestBody TeacherBindingSaveRequest request) {
        saveTeacherBinding(null, request);
        return ApiResponse.success();
    }

    @PutMapping("/teacher-bindings/{id}")
    public ApiResponse<Void> updateTeacherBinding(@PathVariable Long id, @Valid @RequestBody TeacherBindingSaveRequest request) {
        saveTeacherBinding(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/student-bindings")
    public ApiResponse<List<OrgStudentBindingVO>> studentBindings() {
        Map<Long, SysUser> userMap = listUsersByRole(RoleConstants.STUDENT).stream().collect(Collectors.toMap(SysUser::getId, item -> item));
        Map<Long, String> classMap = listClasses().stream().collect(Collectors.toMap(OrgClass::getId, OrgClass::getClassName));
        List<OrgStudentBindingVO> rows = studentService.list(new LambdaQueryWrapper<EduStudent>()
                .eq(EduStudent::getIsDeleted, 0)
                .orderByAsc(EduStudent::getStudentNo))
            .stream()
            .map(item -> toStudentBindingVO(item, userMap, classMap))
            .toList();
        return ApiResponse.success(rows);
    }

    @PostMapping("/student-bindings")
    public ApiResponse<Void> createStudentBinding(@Valid @RequestBody StudentBindingSaveRequest request) {
        saveStudentBinding(null, request);
        return ApiResponse.success();
    }

    @PutMapping("/student-bindings/{id}")
    public ApiResponse<Void> updateStudentBinding(@PathVariable Long id, @Valid @RequestBody StudentBindingSaveRequest request) {
        saveStudentBinding(id, request);
        return ApiResponse.success();
    }

    private void saveCollege(Long id, CollegeSaveRequest request) {
        OrgCollege college = id == null ? new OrgCollege() : getCollege(id);
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            college.setCreatedAt(now);
            college.setIsDeleted(0);
        }
        college.setCollegeCode(StringUtils.hasText(request.getCollegeCode()) ? request.getCollegeCode().trim() : "COL" + System.currentTimeMillis());
        college.setCollegeName(request.getCollegeName().trim());
        college.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        college.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        college.setRemark(request.getRemark());
        college.setUpdatedAt(now);
        if (id == null) {
            collegeService.save(college);
        } else {
            collegeService.updateById(college);
        }
    }

    private void saveMajor(Long id, MajorSaveRequest request) {
        getCollege(request.getCollegeId());
        OrgMajor major = id == null ? new OrgMajor() : getMajor(id);
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            major.setCreatedAt(now);
            major.setIsDeleted(0);
        }
        major.setCollegeId(request.getCollegeId());
        major.setMajorCode(StringUtils.hasText(request.getMajorCode()) ? request.getMajorCode().trim() : "MAJ" + System.currentTimeMillis());
        major.setMajorName(request.getMajorName().trim());
        major.setDegreeType(request.getDegreeType().trim());
        major.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        major.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        major.setRemark(request.getRemark());
        major.setUpdatedAt(now);
        if (id == null) {
            majorService.save(major);
        } else {
            majorService.updateById(major);
        }
    }

    private void saveGrade(Long id, GradeSaveRequest request) {
        OrgMajor major = getMajor(request.getMajorId());
        validateUniqueGrade(id, request.getMajorId(), request.getGradeYear());
        OrgGrade grade = id == null ? new OrgGrade() : getGrade(id);
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            grade.setCreatedAt(now);
            grade.setIsDeleted(0);
        }
        grade.setMajorId(major.getId());
        grade.setGradeYear(request.getGradeYear());
        grade.setAdmissionYear(request.getAdmissionYear());
        grade.setExpectedGraduationYear(request.getExpectedGraduationYear());
        grade.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        grade.setRemark(request.getRemark());
        grade.setUpdatedAt(now);
        if (id == null) {
            gradeService.save(grade);
        } else {
            gradeService.updateById(grade);
        }
    }

    private void saveClass(Long id, ClassSaveRequest request) {
        OrgMajor major = getMajor(request.getMajorId());
        OrgGrade grade = getGrade(request.getGradeId());
        if (!major.getId().equals(grade.getMajorId())) {
            throw new BusinessException("所选年级与所属专业不匹配");
        }
        if (request.getHeadTeacherId() != null) {
            getTeacherUser(request.getHeadTeacherId());
        }
        OrgClass clazz = id == null ? new OrgClass() : getClassEntity(id);
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            clazz.setCreatedAt(now);
            clazz.setIsDeleted(0);
        }
        clazz.setMajorId(request.getMajorId());
        clazz.setGradeId(request.getGradeId());
        clazz.setClassCode(StringUtils.hasText(request.getClassCode()) ? request.getClassCode().trim() : "CLS" + System.currentTimeMillis());
        clazz.setClassName(request.getClassName().trim());
        clazz.setHeadTeacherId(request.getHeadTeacherId());
        clazz.setStudentCount(request.getStudentCount() == null ? 0 : request.getStudentCount());
        clazz.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        clazz.setRemark(request.getRemark());
        clazz.setUpdatedAt(now);
        if (id == null) {
            classService.save(clazz);
        } else {
            classService.updateById(clazz);
        }
    }

    private void saveTeacherBinding(Long id, TeacherBindingSaveRequest request) {
        SysUser user = getTeacherUser(request.getUserId());
        getCollege(request.getCollegeId());
        if (request.getMajorId() != null) {
            getMajor(request.getMajorId());
        }
        validateUniqueTeacherBinding(id, request.getUserId(), request.getTeacherNo());

        EduTeacher teacher = id == null ? new EduTeacher() : getTeacherBinding(id);
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            teacher.setCreatedAt(now);
            teacher.setIsDeleted(0);
        }
        teacher.setUserId(user.getId());
        teacher.setTeacherNo(request.getTeacherNo().trim());
        teacher.setCollegeId(request.getCollegeId());
        teacher.setMajorId(request.getMajorId());
        teacher.setTitle(request.getTitle());
        teacher.setJobTitle(request.getJobTitle());
        teacher.setPhone(request.getPhone());
        teacher.setEmail(request.getEmail());
        teacher.setStatus(request.getStatus());
        teacher.setRemark(request.getRemark());
        teacher.setUpdatedAt(now);
        if (id == null) {
            teacherService.save(teacher);
        } else {
            teacherService.updateById(teacher);
        }
    }

    private void saveStudentBinding(Long id, StudentBindingSaveRequest request) {
        SysUser user = getStudentUser(request.getUserId());
        getClassEntity(request.getClassId());
        validateUniqueStudentBinding(id, request.getUserId(), request.getStudentNo());

        EduStudent student = id == null ? new EduStudent() : getStudentBinding(id);
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            student.setCreatedAt(now);
            student.setIsDeleted(0);
        }
        student.setUserId(user.getId());
        student.setStudentNo(request.getStudentNo().trim());
        student.setClassId(request.getClassId());
        student.setAdmissionYear(request.getAdmissionYear());
        student.setGender(request.getGender());
        student.setStatus(request.getStatus());
        student.setGraduationStatus(request.getGraduationStatus() == null ? 0 : request.getGraduationStatus());
        student.setRemark(request.getRemark());
        student.setUpdatedAt(now);
        if (id == null) {
            studentService.save(student);
        } else {
            studentService.updateById(student);
        }
    }

    private List<OrgCollege> listColleges() {
        return collegeService.list(new LambdaQueryWrapper<OrgCollege>().eq(OrgCollege::getIsDeleted, 0).orderByAsc(OrgCollege::getSortNo));
    }

    private List<OrgMajor> listMajors() {
        return majorService.list(new LambdaQueryWrapper<OrgMajor>().eq(OrgMajor::getIsDeleted, 0).orderByAsc(OrgMajor::getSortNo));
    }

    private List<OrgGrade> listGrades() {
        return gradeService.list(new LambdaQueryWrapper<OrgGrade>()
            .eq(OrgGrade::getIsDeleted, 0)
            .orderByDesc(OrgGrade::getGradeYear)
            .orderByAsc(OrgGrade::getMajorId)
            .orderByAsc(OrgGrade::getId));
    }

    private List<OrgClass> listClasses() {
        return classService.list(new LambdaQueryWrapper<OrgClass>().eq(OrgClass::getIsDeleted, 0));
    }

    private List<SysUser> listUsersByRole(String roleCode) {
        List<Long> roleIds = roleService.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getIsDeleted, 0))
            .stream()
            .map(SysRole::getId)
            .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = userRoleService.list(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getRoleId, roleIds)
                .eq(SysUserRole::getIsDeleted, 0))
            .stream()
            .map(SysUserRole::getUserId)
            .toList();
        return userService.listByIds(userIds).stream()
            .filter(user -> user.getIsDeleted() == 0)
            .sorted(Comparator.comparing(SysUser::getRealName))
            .toList();
    }

    private Map<Long, String> buildTeacherNameMap() {
        return buildTeacherOptions().stream().collect(Collectors.toMap(OptionVO::getId, OptionVO::getLabel));
    }

    private List<OptionVO> buildTeacherOptions() {
        return listUsersByRole(RoleConstants.TEACHER).stream()
            .map(user -> new OptionVO(user.getId(), buildUserOptionLabel(user)))
            .sorted(Comparator.comparing(OptionVO::getLabel))
            .toList();
    }

    private List<OptionVO> buildUserOptions(String roleCode) {
        return listUsersByRole(roleCode).stream()
            .map(user -> new OptionVO(user.getId(), buildUserOptionLabel(user)))
            .toList();
    }

    private String buildUserOptionLabel(SysUser user) {
        if (!StringUtils.hasText(user.getUsername())) {
            return user.getRealName();
        }
        return user.getRealName() + " / " + user.getUsername();
    }

    private SysUser getTeacherUser(Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("教师不存在");
        }
        return user;
    }

    private OrgCollege getCollege(Long id) {
        OrgCollege college = collegeService.getById(id);
        if (college == null || college.getIsDeleted() == 1) {
            throw new BusinessException("学院不存在");
        }
        return college;
    }

    private OrgMajor getMajor(Long id) {
        OrgMajor major = majorService.getById(id);
        if (major == null || major.getIsDeleted() == 1) {
            throw new BusinessException("专业不存在");
        }
        return major;
    }

    private OrgGrade getGrade(Long id) {
        OrgGrade grade = gradeService.getById(id);
        if (grade == null || grade.getIsDeleted() == 1) {
            throw new BusinessException("年级不存在");
        }
        return grade;
    }

    private OrgClass getClassEntity(Long id) {
        OrgClass clazz = classService.getById(id);
        if (clazz == null || clazz.getIsDeleted() == 1) {
            throw new BusinessException("班级不存在");
        }
        return clazz;
    }

    private SysUser getStudentUser(Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("学生不存在");
        }
        return user;
    }

    private EduTeacher getTeacherBinding(Long id) {
        EduTeacher teacher = teacherService.getById(id);
        if (teacher == null || teacher.getIsDeleted() == 1) {
            throw new BusinessException("教师绑定不存在");
        }
        return teacher;
    }

    private EduStudent getStudentBinding(Long id) {
        EduStudent student = studentService.getById(id);
        if (student == null || student.getIsDeleted() == 1) {
            throw new BusinessException("学生绑定不存在");
        }
        return student;
    }

    private void validateUniqueTeacherBinding(Long excludeId, Long userId, String teacherNo) {
        List<EduTeacher> teachers = teacherService.list(new LambdaQueryWrapper<EduTeacher>().eq(EduTeacher::getIsDeleted, 0));
        for (EduTeacher teacher : teachers) {
            if (excludeId != null && excludeId.equals(teacher.getId())) {
                continue;
            }
            if (userId.equals(teacher.getUserId())) {
                throw new BusinessException("璇ユ暀甯堢敤鎴峰凡缁戝畾妗ｆ");
            }
            if (teacherNo.trim().equalsIgnoreCase(teacher.getTeacherNo())) {
                throw new BusinessException("工号已存在");
            }
        }
    }

    private void validateUniqueStudentBinding(Long excludeId, Long userId, String studentNo) {
        List<EduStudent> students = studentService.list(new LambdaQueryWrapper<EduStudent>().eq(EduStudent::getIsDeleted, 0));
        for (EduStudent student : students) {
            if (excludeId != null && excludeId.equals(student.getId())) {
                continue;
            }
            if (userId.equals(student.getUserId())) {
                throw new BusinessException("该学生用户已绑定档案");
            }
            if (studentNo.trim().equalsIgnoreCase(student.getStudentNo())) {
                throw new BusinessException("学号已存在");
            }
        }
    }

    private void validateUniqueGrade(Long excludeId, Long majorId, Integer gradeYear) {
        List<OrgGrade> grades = gradeService.list(new LambdaQueryWrapper<OrgGrade>()
            .eq(OrgGrade::getMajorId, majorId)
            .eq(OrgGrade::getIsDeleted, 0));
        for (OrgGrade grade : grades) {
            if (excludeId != null && excludeId.equals(grade.getId())) {
                continue;
            }
            if (gradeYear.equals(grade.getGradeYear())) {
                throw new BusinessException("同一专业下年级已存在");
            }
        }
    }

    private void softDeleteCollege(OrgCollege college, LocalDateTime now) {
        college.setIsDeleted(1);
        college.setUpdatedAt(now);
        collegeService.updateById(college);
    }

    private void softDeleteMajor(OrgMajor major, LocalDateTime now) {
        major.setIsDeleted(1);
        major.setUpdatedAt(now);
        majorService.updateById(major);
    }

    private void softDeleteMajors(List<OrgMajor> majors, LocalDateTime now) {
        for (OrgMajor major : majors) {
            softDeleteMajor(major, now);
        }
    }

    private void softDeleteGrade(OrgGrade grade, LocalDateTime now) {
        grade.setIsDeleted(1);
        grade.setUpdatedAt(now);
        gradeService.updateById(grade);
    }

    private void softDeleteGrades(List<OrgGrade> grades, LocalDateTime now) {
        for (OrgGrade grade : grades) {
            softDeleteGrade(grade, now);
        }
    }

    private void softDeleteClasses(List<OrgClass> classes, LocalDateTime now) {
        for (OrgClass clazz : classes) {
            clazz.setIsDeleted(1);
            clazz.setUpdatedAt(now);
            classService.updateById(clazz);
        }
    }

    private OrgCollegeVO toCollegeVO(OrgCollege college) {
        OrgCollegeVO vo = new OrgCollegeVO();
        vo.setId(college.getId());
        vo.setCollegeCode(college.getCollegeCode());
        vo.setCollegeName(college.getCollegeName());
        vo.setSortNo(college.getSortNo());
        vo.setStatus(college.getStatus());
        vo.setCreatedAt(college.getCreatedAt());
        vo.setRemark(college.getRemark());
        return vo;
    }

    private OrgMajorVO toMajorVO(OrgMajor major, Map<Long, String> collegeMap) {
        OrgMajorVO vo = new OrgMajorVO();
        vo.setId(major.getId());
        vo.setCollegeId(major.getCollegeId());
        vo.setCollegeName(collegeMap.getOrDefault(major.getCollegeId(), ""));
        vo.setMajorCode(major.getMajorCode());
        vo.setMajorName(major.getMajorName());
        vo.setDegreeType(major.getDegreeType());
        vo.setSortNo(major.getSortNo());
        vo.setStatus(major.getStatus());
        vo.setCreatedAt(major.getCreatedAt());
        vo.setRemark(major.getRemark());
        return vo;
    }

    private OrgGradeVO toGradeVO(OrgGrade grade, Map<Long, OrgMajor> majorMap, Map<Long, String> collegeMap) {
        OrgGradeVO vo = new OrgGradeVO();
        OrgMajor major = majorMap.get(grade.getMajorId());
        vo.setId(grade.getId());
        vo.setMajorId(grade.getMajorId());
        vo.setMajorName(major != null ? major.getMajorName() : "");
        vo.setCollegeName(major != null ? collegeMap.getOrDefault(major.getCollegeId(), "") : "");
        vo.setGradeYear(grade.getGradeYear());
        vo.setAdmissionYear(grade.getAdmissionYear());
        vo.setExpectedGraduationYear(grade.getExpectedGraduationYear());
        vo.setStatus(grade.getStatus());
        vo.setCreatedAt(grade.getCreatedAt());
        vo.setRemark(grade.getRemark());
        return vo;
    }

    private OrgClassVO toClassVO(
        OrgClass clazz,
        Map<Long, String> teacherNameMap,
        Map<Long, OrgMajor> majorMap,
        Map<Long, OrgGrade> gradeMap,
        Map<Long, String> collegeMap
    ) {
        OrgClassVO vo = new OrgClassVO();
        OrgMajor major = majorMap.get(clazz.getMajorId());
        OrgGrade grade = gradeMap.get(clazz.getGradeId());
        vo.setId(clazz.getId());
        vo.setMajorId(clazz.getMajorId());
        vo.setMajorName(major != null ? major.getMajorName() : "");
        vo.setGradeId(clazz.getGradeId());
        vo.setGradeName(grade != null && grade.getGradeYear() != null ? grade.getGradeYear() + "级" : "");
        vo.setCollegeName(major != null ? collegeMap.getOrDefault(major.getCollegeId(), "") : "");
        vo.setClassCode(clazz.getClassCode());
        vo.setClassName(clazz.getClassName());
        vo.setHeadTeacherId(clazz.getHeadTeacherId());
        vo.setHeadTeacherName(teacherNameMap.getOrDefault(clazz.getHeadTeacherId(), ""));
        vo.setStudentCount(clazz.getStudentCount());
        vo.setStatus(clazz.getStatus());
        vo.setCreatedAt(clazz.getCreatedAt());
        vo.setUpdatedAt(clazz.getUpdatedAt());
        vo.setRemark(clazz.getRemark());
        return vo;
    }

    private OrgTeacherBindingVO toTeacherBindingVO(
        EduTeacher teacher,
        Map<Long, SysUser> userMap,
        Map<Long, String> collegeMap,
        Map<Long, String> majorMap
    ) {
        OrgTeacherBindingVO vo = new OrgTeacherBindingVO();
        SysUser user = userMap.get(teacher.getUserId());
        vo.setId(teacher.getId());
        vo.setUserId(teacher.getUserId());
        vo.setRealName(user != null ? user.getRealName() : "");
        vo.setTeacherNo(teacher.getTeacherNo());
        vo.setCollegeId(teacher.getCollegeId());
        vo.setCollegeName(collegeMap.getOrDefault(teacher.getCollegeId(), ""));
        vo.setMajorId(teacher.getMajorId());
        vo.setMajorName(majorMap.getOrDefault(teacher.getMajorId(), ""));
        vo.setTitle(teacher.getTitle());
        vo.setJobTitle(teacher.getJobTitle());
        vo.setPhone(teacher.getPhone());
        vo.setEmail(teacher.getEmail());
        vo.setStatus(teacher.getStatus());
        vo.setRemark(teacher.getRemark());
        return vo;
    }

    private OrgStudentBindingVO toStudentBindingVO(EduStudent student, Map<Long, SysUser> userMap, Map<Long, String> classMap) {
        OrgStudentBindingVO vo = new OrgStudentBindingVO();
        SysUser user = userMap.get(student.getUserId());
        vo.setId(student.getId());
        vo.setUserId(student.getUserId());
        vo.setRealName(user != null ? user.getRealName() : "");
        vo.setStudentNo(student.getStudentNo());
        vo.setClassId(student.getClassId());
        vo.setClassName(classMap.getOrDefault(student.getClassId(), ""));
        vo.setAdmissionYear(student.getAdmissionYear());
        vo.setGender(student.getGender());
        vo.setStatus(student.getStatus());
        vo.setGraduationStatus(student.getGraduationStatus());
        vo.setRemark(student.getRemark());
        return vo;
    }
}
