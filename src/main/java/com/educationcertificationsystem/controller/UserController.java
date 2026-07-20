package com.educationcertificationsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.educationcertificationsystem.auth.CurrentUserContext;
import com.educationcertificationsystem.auth.CurrentUserInfo;
import com.educationcertificationsystem.auth.RequireRoles;
import com.educationcertificationsystem.auth.RoleConstants;
import com.educationcertificationsystem.common.ApiResponse;
import com.educationcertificationsystem.common.BusinessException;
import com.educationcertificationsystem.common.PageResult;
import com.educationcertificationsystem.course.service.EduStudentService;
import com.educationcertificationsystem.course.service.EduTeacherService;
import com.educationcertificationsystem.dto.user.PasswordChangeRequest;
import com.educationcertificationsystem.dto.user.ProfileUpdateRequest;
import com.educationcertificationsystem.dto.user.RoleSaveRequest;
import com.educationcertificationsystem.dto.user.UserBatchCreateRequest;
import com.educationcertificationsystem.dto.user.UserPasswordResetRequest;
import com.educationcertificationsystem.dto.user.UserSaveRequest;
import com.educationcertificationsystem.dto.user.UserStatusRequest;
import com.educationcertificationsystem.model.entity.EduStudent;
import com.educationcertificationsystem.model.entity.EduTeacher;
import com.educationcertificationsystem.model.entity.Manager;
import com.educationcertificationsystem.model.entity.OrgClass;
import com.educationcertificationsystem.model.entity.OrgCollege;
import com.educationcertificationsystem.model.entity.OrgGrade;
import com.educationcertificationsystem.model.entity.OrgMajor;
import com.educationcertificationsystem.model.entity.SysLoginSession;
import com.educationcertificationsystem.model.entity.SysRole;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.model.entity.SysUserRole;
import com.educationcertificationsystem.org.service.OrgClassService;
import com.educationcertificationsystem.org.service.OrgCollegeService;
import com.educationcertificationsystem.org.service.OrgGradeService;
import com.educationcertificationsystem.org.service.OrgMajorService;
import com.educationcertificationsystem.user.service.ManagerService;
import com.educationcertificationsystem.role.service.SysRoleService;
import com.educationcertificationsystem.role.service.SysUserRoleService;
import com.educationcertificationsystem.service.MenuAccessService;
import com.educationcertificationsystem.user.service.SysLoginSessionService;
import com.educationcertificationsystem.user.service.SysUserService;
import com.educationcertificationsystem.util.PasswordUtils;
import com.educationcertificationsystem.vo.auth.AuthLoginResponse;
import com.educationcertificationsystem.vo.auth.MenuNodeVO;
import com.educationcertificationsystem.vo.auth.UserInfoVO;
import com.educationcertificationsystem.vo.user.RoleManageItemVO;
import com.educationcertificationsystem.vo.user.RoleOptionVO;
import com.educationcertificationsystem.vo.user.UserListItemVO;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final String DEFAULT_PASSWORD = "123456";

    private final SysUserService userService;

    private final SysRoleService roleService;

    private final SysUserRoleService userRoleService;

    private final SysLoginSessionService loginSessionService;

    private final ManagerService managerService;

    private final EduTeacherService teacherService;

    private final EduStudentService studentService;

    private final OrgCollegeService collegeService;

    private final OrgMajorService majorService;

    private final OrgGradeService gradeService;

    private final OrgClassService classService;

    private final MenuAccessService menuAccessService;

    public UserController(
        SysUserService userService,
        SysRoleService roleService,
        SysUserRoleService userRoleService,
        SysLoginSessionService loginSessionService,
        ManagerService managerService,
        EduTeacherService teacherService,
        EduStudentService studentService,
        OrgCollegeService collegeService,
        OrgMajorService majorService,
        OrgGradeService gradeService,
        OrgClassService classService,
        MenuAccessService menuAccessService
    ) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.loginSessionService = loginSessionService;
        this.managerService = managerService;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.collegeService = collegeService;
        this.majorService = majorService;
        this.gradeService = gradeService;
        this.classService = classService;
        this.menuAccessService = menuAccessService;
    }

    @GetMapping("/me")
    public ApiResponse<AuthLoginResponse> me() {
        CurrentUserInfo currentUser = CurrentUserContext.require();
        SysUser user = getActiveUser(currentUser.getUserId());
        Set<String> roleCodes = loadRoleCodes(user.getId());
        AuthLoginResponse response = new AuthLoginResponse();
        response.setUserInfo(buildUserInfo(user, roleCodes));
        response.setPermissions(menuAccessService.getPermissionCodes(roleCodes));
        response.setMenuPaths(menuAccessService.getAccessiblePaths(roleCodes));
        response.setMenus(menuAccessService.getAccessibleMenuTree(roleCodes));
        return ApiResponse.success(response);
    }

    @GetMapping("/roles")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<List<RoleOptionVO>> roles() {
        List<RoleOptionVO> roles = roleService.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getIsDeleted, 0)
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSortNo))
            .stream()
            .map(role -> new RoleOptionVO(role.getId(), role.getRoleCode(), role.getRoleName()))
            .toList();
        return ApiResponse.success(roles);
    }

    @GetMapping("/menu-tree")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<List<MenuNodeVO>> menuTree() {
        return ApiResponse.success(menuAccessService.getFullMenuTree());
    }

    @GetMapping("/role-management")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<List<RoleManageItemVO>> roleManagement() {
        List<SysRole> roles = roleService.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getIsDeleted, 0)
                .orderByAsc(SysRole::getSortNo)
                .orderByAsc(SysRole::getId));
        Map<Long, Integer> userCountMap = userRoleService.list(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getIsDeleted, 0))
            .stream()
            .collect(Collectors.groupingBy(SysUserRole::getRoleId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        List<RoleManageItemVO> rows = roles.stream()
            .map(role -> {
                RoleManageItemVO item = new RoleManageItemVO();
                item.setId(role.getId());
                item.setRoleCode(role.getRoleCode());
                item.setRoleName(role.getRoleName());
                item.setRoleType(role.getRoleType());
                item.setDataScope(role.getDataScope());
                item.setSortNo(role.getSortNo());
                item.setStatus(role.getStatus());
                item.setRemark(role.getRemark());
                item.setUserCount(userCountMap.getOrDefault(role.getId(), 0));
                item.setMenuIds(menuAccessService.getAssignedMenuIds(role.getId()));
                return item;
            })
            .toList();
        return ApiResponse.success(rows);
    }

    @PostMapping("/roles/manage")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<Void> createRole(@Valid @RequestBody RoleSaveRequest request) {
        saveRole(null, request);
        return ApiResponse.success();
    }

    @PutMapping("/roles/manage/{id}")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleSaveRequest request) {
        saveRole(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/roles/manage/{id}")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        SysRole role = getRole(id);
        if (List.of(RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER, RoleConstants.STUDENT).contains(role.getRoleCode())) {
            throw new BusinessException("系统内置角色不允许删除");
        }
        if (userRoleService.count(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getRoleId, id)
            .eq(SysUserRole::getIsDeleted, 0)) > 0) {
            throw new BusinessException("当前角色已分配给用户，不能删除");
        }
        role.setIsDeleted(1);
        role.setUpdatedAt(LocalDateTime.now());
        roleService.updateById(role);
        menuAccessService.replaceRoleMenus(id, List.of());
        return ApiResponse.success();
    }

    @GetMapping("/list")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<PageResult<UserListItemVO>> list(
        @RequestParam(defaultValue = "1") Long pageNum,
        @RequestParam(defaultValue = "10") Long pageSize,
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "") String role,
        @RequestParam(defaultValue = "") String department,
        @RequestParam(required = false) Integer status
    ) {
        List<UserListItemVO> users = userService.list(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getIsDeleted, 0)
                .orderByDesc(SysUser::getCreatedAt))
            .stream()
            .map(user -> buildUserListItem(user, loadRoleCodes(user.getId())))
            .filter(item -> matchUser(item, keyword, role, department, status))
            .sorted(Comparator.comparing(UserListItemVO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
        return ApiResponse.success(buildPage(users, pageNum, pageSize));
    }

    @PostMapping
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> create(@Valid @RequestBody UserSaveRequest request) {
        saveUser(null, request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UserSaveRequest request) {
        saveUser(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/batch")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> batchCreate(@Valid @RequestBody UserBatchCreateRequest request) {
        for (UserSaveRequest item : request.getUsers()) {
            saveUser(null, item);
        }
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        CurrentUserInfo currentUser = CurrentUserContext.require();
        if (currentUser.getUserId().equals(id) && request.getStatus() != 1) {
            throw new BusinessException("当前登录账号不能被停用");
        }
        SysUser user = getActiveUser(id);
        LocalDateTime now = LocalDateTime.now();
        user.setUserStatus(request.getStatus());
        user.setUpdatedAt(now);
        userService.updateById(user);
        syncProfileStatuses(user.getId(), request.getStatus(), now);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/reset-password")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) UserPasswordResetRequest request) {
        SysUser user = getActiveUser(id);
        user.setPasswordHash(PasswordUtils.encode(request != null && StringUtils.hasText(request.getPassword()) ? request.getPassword() : DEFAULT_PASSWORD));
        user.setUpdatedAt(LocalDateTime.now());
        userService.updateById(user);
        revokeSessions(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        CurrentUserInfo currentUser = CurrentUserContext.require();
        if (currentUser.getUserId().equals(id)) {
            throw new BusinessException("当前登录账号不能删除");
        }
        SysUser user = getActiveUser(id);
        LocalDateTime now = LocalDateTime.now();
        user.setIsDeleted(1);
        user.setUpdatedAt(now);
        userService.updateById(user);
        List<SysUserRole> relations = userRoleService.list(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getUserId, id)
            .eq(SysUserRole::getIsDeleted, 0));
        relations.forEach(relation -> {
            relation.setIsDeleted(1);
            relation.setUpdatedAt(now);
        });
        if (!relations.isEmpty()) {
            userRoleService.updateBatchById(relations);
        }
        softDeleteRoleProfiles(id, now);
        revokeSessions(id);
        return ApiResponse.success();
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@RequestBody ProfileUpdateRequest request) {
        CurrentUserInfo currentUser = CurrentUserContext.require();
        SysUser user = getActiveUser(currentUser.getUserId());
        validateUniqueContact(user.getId(), user.getUsername(), request.getPhone(), request.getEmail());
        user.setPhone(normalize(request.getPhone()));
        user.setEmail(normalizeEmail(request.getEmail()));
        user.setUpdatedAt(LocalDateTime.now());
        userService.updateById(user);
        return ApiResponse.success();
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        CurrentUserInfo currentUser = CurrentUserContext.require();
        SysUser user = getActiveUser(currentUser.getUserId());
        if (!PasswordUtils.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("原密码不正确");
        }
        user.setPasswordHash(PasswordUtils.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userService.updateById(user);
        revokeSessions(user.getId());
        return ApiResponse.success();
    }

    private void saveUser(Long userId, UserSaveRequest request) {
        String accountId = normalize(request.getAccountId());
        String phone = normalize(request.getPhone());
        String email = normalizeEmail(request.getEmail());
        String roleCode = normalize(request.getRole());
        String department = resolveDepartment(request, roleCode);
        if (!List.of(RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER, RoleConstants.STUDENT).contains(roleCode)) {
            throw new BusinessException("角色不合法");
        }

        validateUniqueContact(userId, accountId, phone, email);
        SysRole role = roleService.getOne(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleCode, roleCode)
            .eq(SysRole::getIsDeleted, 0)
            .last("limit 1"));
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        SysUser user = userId == null ? new SysUser() : getActiveUser(userId);
        LocalDateTime now = LocalDateTime.now();
        if (userId == null) {
            user.setPasswordHash(PasswordUtils.encode(DEFAULT_PASSWORD));
            user.setCreatedAt(now);
            user.setIsDeleted(0);
            user.setUserStatus(1);
        }
        user.setUsername(accountId);
        user.setRealName(normalize(request.getRealName()));
        user.setPhone(phone);
        user.setEmail(email);
        user.setRemark(department);
        user.setUpdatedAt(now);
        if (userId == null) {
            userService.save(user);
        } else {
            userService.updateById(user);
        }

        List<SysUserRole> existing = userRoleService.list(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getUserId, user.getId())
            .eq(SysUserRole::getIsDeleted, 0));
        existing.forEach(relation -> {
            relation.setIsDeleted(1);
            relation.setUpdatedAt(now);
        });
        if (!existing.isEmpty()) {
            userRoleService.updateBatchById(existing);
        }
        SysUserRole relation = new SysUserRole();
        relation.setUserId(user.getId());
        relation.setRoleId(role.getId());
        relation.setCreatedAt(now);
        relation.setUpdatedAt(now);
        relation.setIsDeleted(0);
        userRoleService.save(relation);

        syncRoleProfiles(user, request, roleCode, department, now);
    }

    private void syncRoleProfiles(SysUser user, UserSaveRequest request, String roleCode, String department, LocalDateTime now) {
        syncManagerProfile(user, request, roleCode, department, now);
        syncTeacherProfile(user, request, roleCode, department, now);
        syncStudentProfile(user, request, roleCode, department, now);
    }

    private void syncManagerProfile(SysUser user, UserSaveRequest request, String roleCode, String department, LocalDateTime now) {
        Manager manager = findManagerByUserId(user.getId());
        if (!RoleConstants.SUPER_ADMIN.equals(roleCode)) {
            if (manager != null && manager.getIsDeleted() == 0) {
                manager.setIsDeleted(1);
                manager.setUpdatedAt(now);
                managerService.updateById(manager);
            }
            return;
        }

        if (manager == null) {
            manager = new Manager();
            manager.setUserId(user.getId());
            manager.setCreatedAt(now);
            manager.setIsDeleted(0);
        }
        manager.setAdminNo(user.getUsername());
        manager.setDepartmentName(department);
        manager.setPositionName(normalize(request.getPositionName()));
        manager.setStatus(user.getUserStatus());
        manager.setRemark(department);
        manager.setUpdatedAt(now);
        if (manager.getId() == null) {
            managerService.save(manager);
        } else {
            if (manager.getIsDeleted() != null && manager.getIsDeleted() == 1) {
                manager.setIsDeleted(0);
            }
            managerService.updateById(manager);
        }
    }

    private void syncTeacherProfile(SysUser user, UserSaveRequest request, String roleCode, String department, LocalDateTime now) {
        EduTeacher teacher = findTeacherByUserId(user.getId());
        if (!RoleConstants.TEACHER.equals(roleCode)) {
            if (teacher != null && teacher.getIsDeleted() == 0) {
                teacher.setIsDeleted(1);
                teacher.setUpdatedAt(now);
                teacherService.updateById(teacher);
            }
            return;
        }

        Long collegeId = requireCollegeId(request.getCollegeId());
        Long majorId = request.getMajorId();
        validateMajor(collegeId, majorId);

        if (teacher == null) {
            teacher = new EduTeacher();
            teacher.setUserId(user.getId());
            teacher.setCreatedAt(now);
            teacher.setIsDeleted(0);
        }
        teacher.setTeacherNo(user.getUsername());
        teacher.setCollegeId(collegeId);
        teacher.setMajorId(majorId);
        teacher.setTitle(normalize(request.getTitle()));
        teacher.setJobTitle(normalize(request.getJobTitle()));
        teacher.setPhone(user.getPhone());
        teacher.setEmail(user.getEmail());
        teacher.setStatus(user.getUserStatus());
        teacher.setRemark(department);
        teacher.setUpdatedAt(now);
        if (teacher.getId() == null) {
            teacherService.save(teacher);
        } else {
            if (teacher.getIsDeleted() != null && teacher.getIsDeleted() == 1) {
                teacher.setIsDeleted(0);
            }
            teacherService.updateById(teacher);
        }
    }

    private String resolveDepartment(UserSaveRequest request, String roleCode) {
        if (RoleConstants.SUPER_ADMIN.equals(roleCode)) {
            return resolveManagerDepartment(request);
        }
        if (RoleConstants.TEACHER.equals(roleCode)) {
            return resolveTeacherDepartment(request);
        }
        if (RoleConstants.STUDENT.equals(roleCode)) {
            return resolveStudentDepartment(request);
        }
        return normalize(request.getDepartment());
    }

    private String resolveManagerDepartment(UserSaveRequest request) {
        String department = normalize(request.getDepartment());
        if (StringUtils.hasText(department)) {
            return department;
        }
        String positionName = normalize(request.getPositionName());
        return StringUtils.hasText(positionName) ? positionName : "管理员";
    }

    private String resolveTeacherDepartment(UserSaveRequest request) {
        OrgCollege college = getCollege(requireCollegeId(request.getCollegeId()));
        Long majorId = request.getMajorId();
        if (majorId == null) {
            return college.getCollegeName();
        }
        OrgMajor major = getMajor(majorId);
        if (!college.getId().equals(major.getCollegeId())) {
            throw new BusinessException("所属学院与专业不匹配");
        }
        return college.getCollegeName() + " / " + major.getMajorName();
    }

    private String resolveStudentDepartment(UserSaveRequest request) {
        OrgClass clazz = getClassEntity(requireClassId(request.getClassId()));
        OrgMajor major = getMajor(clazz.getMajorId());
        OrgCollege college = getCollege(major.getCollegeId());
        OrgGrade grade = getGrade(clazz.getGradeId());
        return college.getCollegeName() + " / " + major.getMajorName() + " / " + grade.getGradeYear() + "级 / " + clazz.getClassName();
    }

    private void syncStudentProfile(SysUser user, UserSaveRequest request, String roleCode, String department, LocalDateTime now) {
        EduStudent student = findStudentByUserId(user.getId());
        if (!RoleConstants.STUDENT.equals(roleCode)) {
            if (student != null && student.getIsDeleted() == 0) {
                student.setIsDeleted(1);
                student.setUpdatedAt(now);
                studentService.updateById(student);
            }
            return;
        }

        Long classId = requireClassId(request.getClassId());
        Integer admissionYear = requireAdmissionYear(request.getAdmissionYear());

        if (student == null) {
            student = new EduStudent();
            student.setUserId(user.getId());
            student.setCreatedAt(now);
            student.setIsDeleted(0);
        }
        student.setStudentNo(user.getUsername());
        student.setClassId(classId);
        student.setAdmissionYear(admissionYear);
        student.setGender(normalize(request.getGender()));
        student.setStatus(user.getUserStatus());
        student.setGraduationStatus(request.getGraduationStatus() == null ? 0 : request.getGraduationStatus());
        student.setRemark(department);
        student.setUpdatedAt(now);
        if (student.getId() == null) {
            studentService.save(student);
        } else {
            if (student.getIsDeleted() != null && student.getIsDeleted() == 1) {
                student.setIsDeleted(0);
            }
            studentService.updateById(student);
        }
    }

    private void syncProfileStatuses(Long userId, Integer status, LocalDateTime now) {
        Manager manager = findManagerByUserId(userId);
        if (manager == null || manager.getIsDeleted() == 1) {
        } else {
            manager.setStatus(status);
            manager.setUpdatedAt(now);
            managerService.updateById(manager);
        }

        EduTeacher teacher = findTeacherByUserId(userId);
        if (teacher == null || teacher.getIsDeleted() == 1) {
        } else {
            teacher.setStatus(status);
            teacher.setUpdatedAt(now);
            teacherService.updateById(teacher);
        }

        EduStudent student = findStudentByUserId(userId);
        if (student == null || student.getIsDeleted() == 1) {
            return;
        }
        student.setStatus(status);
        student.setUpdatedAt(now);
        studentService.updateById(student);
    }

    private void softDeleteRoleProfiles(Long userId, LocalDateTime now) {
        Manager manager = findManagerByUserId(userId);
        if (manager != null && manager.getIsDeleted() == 0) {
            manager.setIsDeleted(1);
            manager.setUpdatedAt(now);
            managerService.updateById(manager);
        }

        EduTeacher teacher = findTeacherByUserId(userId);
        if (teacher != null && teacher.getIsDeleted() == 0) {
            teacher.setIsDeleted(1);
            teacher.setUpdatedAt(now);
            teacherService.updateById(teacher);
        }

        EduStudent student = findStudentByUserId(userId);
        if (student == null || student.getIsDeleted() == 1) {
            return;
        }
        student.setIsDeleted(1);
        student.setUpdatedAt(now);
        studentService.updateById(student);
    }

    private Manager findManagerByUserId(Long userId) {
        return managerService.getOne(new LambdaQueryWrapper<Manager>()
            .eq(Manager::getUserId, userId)
            .last("limit 1"));
    }

    private EduTeacher findTeacherByUserId(Long userId) {
        return teacherService.getOne(new LambdaQueryWrapper<EduTeacher>()
            .eq(EduTeacher::getUserId, userId)
            .last("limit 1"));
    }

    private EduStudent findStudentByUserId(Long userId) {
        return studentService.getOne(new LambdaQueryWrapper<EduStudent>()
            .eq(EduStudent::getUserId, userId)
            .last("limit 1"));
    }

    private Long requireCollegeId(Long collegeId) {
        if (collegeId == null) {
            throw new BusinessException("教师所属学院不能为空");
        }
        return getCollege(collegeId).getId();
    }

    private void validateMajor(Long collegeId, Long majorId) {
        if (majorId == null) {
            return;
        }
        OrgMajor major = getMajor(majorId);
        if (!collegeId.equals(major.getCollegeId())) {
            throw new BusinessException("所属学院与专业不匹配");
        }
    }

    private Long requireClassId(Long classId) {
        if (classId == null) {
            throw new BusinessException("学生所属班级不能为空");
        }
        return getClassEntity(classId).getId();
    }

    private Integer requireAdmissionYear(Integer admissionYear) {
        if (admissionYear == null) {
            throw new BusinessException("学生入学年份不能为空");
        }
        return admissionYear;
    }

    private OrgCollege getCollege(Long collegeId) {
        OrgCollege college = collegeService.getById(collegeId);
        if (college == null || college.getIsDeleted() == 1) {
            throw new BusinessException("所属学院不存在");
        }
        return college;
    }

    private OrgMajor getMajor(Long majorId) {
        OrgMajor major = majorService.getById(majorId);
        if (major == null || major.getIsDeleted() == 1) {
            throw new BusinessException("所属专业不存在");
        }
        return major;
    }

    private OrgGrade getGrade(Long gradeId) {
        OrgGrade grade = gradeService.getById(gradeId);
        if (grade == null || grade.getIsDeleted() == 1) {
            throw new BusinessException("所属年级不存在");
        }
        return grade;
    }

    private OrgClass getClassEntity(Long classId) {
        OrgClass clazz = classService.getById(classId);
        if (clazz == null || clazz.getIsDeleted() == 1) {
            throw new BusinessException("所属班级不存在");
        }
        return clazz;
    }

    private void saveRole(Long roleId, RoleSaveRequest request) {
        String roleCode = normalize(request.getRoleCode()).toUpperCase(Locale.ROOT);
        String roleName = normalize(request.getRoleName());
        validateUniqueRole(roleId, roleCode, roleName);

        SysRole role = roleId == null ? new SysRole() : getRole(roleId);
        LocalDateTime now = LocalDateTime.now();
        if (roleId == null) {
            role.setCreatedAt(now);
            role.setIsDeleted(0);
        } else if (List.of(RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER, RoleConstants.STUDENT).contains(role.getRoleCode())
            && !role.getRoleCode().equals(roleCode)) {
            throw new BusinessException("系统内置角色编码不允许修改");
        }

        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setRoleType(normalize(request.getRoleType()));
        role.setDataScope(normalize(request.getDataScope()));
        role.setSortNo(request.getSortNo());
        role.setStatus(request.getStatus());
        role.setRemark(request.getRemark());
        role.setUpdatedAt(now);
        if (roleId == null) {
            roleService.save(role);
        } else {
            roleService.updateById(role);
        }

        menuAccessService.replaceRoleMenus(role.getId(), request.getMenuIds());
    }

    private PageResult<UserListItemVO> buildPage(List<UserListItemVO> source, Long pageNum, Long pageSize) {
        int fromIndex = Math.max(0, Math.toIntExact((pageNum - 1) * pageSize));
        int toIndex = Math.min(source.size(), fromIndex + Math.toIntExact(pageSize));
        List<UserListItemVO> records = fromIndex >= source.size() ? List.of() : source.subList(fromIndex, toIndex);
        return new PageResult<>((long) source.size(), pageNum, pageSize, records);
    }

    private boolean matchUser(UserListItemVO item, String keyword, String role, String department, Integer status) {
        String normalizedKeyword = normalize(keyword).toLowerCase(Locale.ROOT);
        if (StringUtils.hasText(normalizedKeyword)) {
            boolean matched = contains(item.getAccountId(), normalizedKeyword)
                || contains(item.getRealName(), normalizedKeyword)
                || contains(item.getPhone(), normalizedKeyword)
                || contains(item.getEmail(), normalizedKeyword);
            if (!matched) {
                return false;
            }
        }
        if (StringUtils.hasText(role) && !role.equals(item.getRole())) {
            return false;
        }
        if (StringUtils.hasText(department) && !department.equals(item.getDepartment())) {
            return false;
        }
        return status == null || status.equals(item.getStatus());
    }

    private boolean contains(String source, String target) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(target);
    }

    private SysUser getActiveUser(Long id) {
        SysUser user = userService.getById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private SysRole getRole(Long id) {
        SysRole role = roleService.getById(id);
        if (role == null || role.getIsDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    private Set<String> loadRoleCodes(Long userId) {
        List<Long> roleIds = userRoleService.list(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getIsDeleted, 0))
            .stream()
            .map(SysUserRole::getRoleId)
            .toList();
        return roleService.listByIds(roleIds).stream()
            .filter(role -> role.getIsDeleted() == 0 && role.getStatus() == 1)
            .map(SysRole::getRoleCode)
            .collect(Collectors.toSet());
    }

    private UserInfoVO buildUserInfo(SysUser user, Set<String> roleCodes) {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setAccountId(user.getUsername());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setRole(roleCodes.stream().findFirst().orElse(""));
        userInfoVO.setDepartment(user.getRemark());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setEmail(user.getEmail());
        userInfoVO.setStatus(user.getUserStatus());
        return userInfoVO;
    }

    private UserListItemVO buildUserListItem(SysUser user, Set<String> roleCodes) {
        UserListItemVO item = new UserListItemVO();
        String roleCode = roleCodes.stream().findFirst().orElse("");
        item.setId(user.getId());
        item.setAccountId(user.getUsername());
        item.setRealName(user.getRealName());
        item.setRole(roleCode);
        item.setDepartment(user.getRemark());
        item.setPhone(user.getPhone());
        item.setEmail(user.getEmail());
        item.setStatus(user.getUserStatus());
        item.setCreatedAt(user.getCreatedAt());
        List<String> loginAccounts = new ArrayList<>();
        if (StringUtils.hasText(user.getUsername())) {
            loginAccounts.add(user.getUsername());
        }
        if (StringUtils.hasText(user.getPhone())) {
            loginAccounts.add(user.getPhone());
        }
        if (StringUtils.hasText(user.getEmail())) {
            loginAccounts.add(user.getEmail());
        }
        item.setLoginAccounts(loginAccounts);
        fillRoleProfile(item, user.getId(), roleCode);
        return item;
    }

    private void fillRoleProfile(UserListItemVO item, Long userId, String roleCode) {
        if (RoleConstants.SUPER_ADMIN.equals(roleCode)) {
            Manager manager = findManagerByUserId(userId);
            if (manager != null && manager.getIsDeleted() == 0) {
                item.setPositionName(manager.getPositionName());
            }
            return;
        }
        if (RoleConstants.TEACHER.equals(roleCode)) {
            EduTeacher teacher = findTeacherByUserId(userId);
            if (teacher != null && teacher.getIsDeleted() == 0) {
                item.setCollegeId(teacher.getCollegeId());
                item.setMajorId(teacher.getMajorId());
                item.setTitle(teacher.getTitle());
                item.setJobTitle(teacher.getJobTitle());
            }
            return;
        }
        if (!RoleConstants.STUDENT.equals(roleCode)) {
            return;
        }
        EduStudent student = findStudentByUserId(userId);
        if (student == null || student.getIsDeleted() == 1) {
            return;
        }
        item.setClassId(student.getClassId());
        item.setAdmissionYear(student.getAdmissionYear());
        item.setGender(student.getGender());
        item.setGraduationStatus(student.getGraduationStatus());
    }

    private void validateUniqueContact(Long excludeId, String accountId, String phone, String email) {
        List<SysUser> users = userService.list(new LambdaQueryWrapper<SysUser>().eq(SysUser::getIsDeleted, 0));
        for (SysUser user : users) {
            if (excludeId != null && excludeId.equals(user.getId())) {
                continue;
            }
            if (accountId.equalsIgnoreCase(user.getUsername())) {
                throw new BusinessException("账号已存在");
            }
            if (StringUtils.hasText(phone) && phone.equals(normalize(user.getPhone()))) {
                throw new BusinessException("手机号已存在");
            }
            if (StringUtils.hasText(email) && email.equalsIgnoreCase(normalizeEmail(user.getEmail()))) {
                throw new BusinessException("邮箱已存在");
            }
        }
    }

    private void validateUniqueRole(Long excludeId, String roleCode, String roleName) {
        List<SysRole> roles = roleService.list(new LambdaQueryWrapper<SysRole>().eq(SysRole::getIsDeleted, 0));
        for (SysRole role : roles) {
            if (excludeId != null && excludeId.equals(role.getId())) {
                continue;
            }
            if (roleCode.equalsIgnoreCase(role.getRoleCode())) {
                throw new BusinessException("角色编码已存在");
            }
            if (roleName.equalsIgnoreCase(normalize(role.getRoleName()))) {
                throw new BusinessException("角色名称已存在");
            }
        }
    }

    private void revokeSessions(Long userId) {
        List<SysLoginSession> sessions = loginSessionService.list(new LambdaQueryWrapper<SysLoginSession>()
            .eq(SysLoginSession::getUserId, userId)
            .eq(SysLoginSession::getRevokedFlag, 0)
            .eq(SysLoginSession::getIsDeleted, 0));
        LocalDateTime now = LocalDateTime.now();
        sessions.forEach(session -> {
            session.setRevokedFlag(1);
            session.setLogoutTime(now);
            session.setUpdatedAt(now);
        });
        if (!sessions.isEmpty()) {
            loginSessionService.updateBatchById(sessions);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeEmail(String value) {
        return normalize(value).toLowerCase(Locale.ROOT);
    }
}
