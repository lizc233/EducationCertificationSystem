package com.educationcertificationsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.educationcertificationsystem.common.BusinessException;
import com.educationcertificationsystem.course.service.EduStudentService;
import com.educationcertificationsystem.dto.selection.CourseSelectionTaskSaveRequest;
import com.educationcertificationsystem.entity.CourseSelectionRecord;
import com.educationcertificationsystem.entity.CourseSelectionTask;
import com.educationcertificationsystem.model.entity.EduStudent;
import com.educationcertificationsystem.model.entity.OrgClass;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.mapper.CourseSelectionRecordMapper;
import com.educationcertificationsystem.mapper.CourseSelectionTaskMapper;
import com.educationcertificationsystem.org.service.OrgClassService;
import com.educationcertificationsystem.user.service.SysUserService;
import com.educationcertificationsystem.vo.selection.CourseSelectionAdminTaskVO;
import com.educationcertificationsystem.vo.selection.CourseSelectionRecordVO;
import com.educationcertificationsystem.vo.selection.CourseSelectionStudentTaskVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CourseSelectionService {

    private static final String TASK_STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String TASK_STATUS_OPEN = "OPEN";
    private static final String TASK_STATUS_CLOSED = "CLOSED";
    private static final String RECORD_STATUS_SELECTED = "SELECTED";
    private static final String RECORD_STATUS_DROPPED = "DROPPED";
    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final JdbcTemplate jdbcTemplate;
    private final CourseSelectionTaskMapper taskMapper;
    private final CourseSelectionRecordMapper recordMapper;
    private final EduStudentService studentService;
    private final OrgClassService classService;
    private final SysUserService userService;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public CourseSelectionService(
        JdbcTemplate jdbcTemplate,
        CourseSelectionTaskMapper taskMapper,
        CourseSelectionRecordMapper recordMapper,
        EduStudentService studentService,
        OrgClassService classService,
        SysUserService userService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.taskMapper = taskMapper;
        this.recordMapper = recordMapper;
        this.studentService = studentService;
        this.classService = classService;
        this.userService = userService;
    }

    public List<CourseSelectionAdminTaskVO> listAdminTasks(String term, String status, String keyword) {
        ensureReady();
        List<CourseSelectionTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<CourseSelectionTask>()
            .eq(CourseSelectionTask::getIsDeleted, 0));
        Map<Long, Integer> selectedCountMap = buildSelectedCountMap();
        List<CourseSelectionAdminTaskVO> rows = tasks.stream()
            .map(task -> toAdminTaskVO(task, selectedCountMap.getOrDefault(task.getId(), 0)))
            .filter(item -> matchAdminTask(item, term, status, keyword))
            .sorted(Comparator.comparing(CourseSelectionAdminTaskVO::getSelectionStartTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
        syncDynamicStatuses(tasks);
        return rows;
    }

    public CourseSelectionAdminTaskVO createTask(CourseSelectionTaskSaveRequest request) {
        ensureReady();
        validateTaskRequest(request);
        CourseSelectionTask task = new CourseSelectionTask();
        LocalDateTime now = LocalDateTime.now();
        task.setTaskCode(generateTaskCode(now));
        task.setTerm(normalize(request.getTerm()));
        task.setCourseName(normalize(request.getCourseName()));
        task.setTeacherName(normalize(request.getTeacherName()));
        task.setCredit(request.getCredit());
        task.setCapacity(request.getCapacity());
        task.setSelectionStartTime(request.getSelectionStartTime());
        task.setSelectionEndTime(request.getSelectionEndTime());
        task.setTaskStatus(resolveTaskStatus(request.getSelectionStartTime(), request.getSelectionEndTime(), null));
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        task.setIsDeleted(0);
        task.setRemark(request.getRemark());
        taskMapper.insert(task);
        return toAdminTaskVO(task, 0);
    }

    public CourseSelectionAdminTaskVO updateTask(Long id, CourseSelectionTaskSaveRequest request) {
        ensureReady();
        validateTaskRequest(request);
        CourseSelectionTask task = getTask(id);
        int selectedCount = buildSelectedCountMap().getOrDefault(id, 0);
        if (request.getCapacity() < selectedCount) {
            throw new BusinessException("容量不能小于当前已选人数");
        }
        task.setTerm(normalize(request.getTerm()));
        task.setCourseName(normalize(request.getCourseName()));
        task.setTeacherName(normalize(request.getTeacherName()));
        task.setCredit(request.getCredit());
        task.setCapacity(request.getCapacity());
        task.setSelectionStartTime(request.getSelectionStartTime());
        task.setSelectionEndTime(request.getSelectionEndTime());
        task.setTaskStatus(resolveTaskStatus(request.getSelectionStartTime(), request.getSelectionEndTime(), task.getTaskStatus()));
        task.setUpdatedAt(LocalDateTime.now());
        task.setRemark(request.getRemark());
        taskMapper.updateById(task);
        return toAdminTaskVO(task, selectedCount);
    }

    public void closeTask(Long id) {
        ensureReady();
        CourseSelectionTask task = getTask(id);
        LocalDateTime now = LocalDateTime.now();
        task.setTaskStatus(TASK_STATUS_CLOSED);
        task.setSelectionEndTime(now);
        task.setUpdatedAt(now);
        taskMapper.updateById(task);
    }

    public void deleteTask(Long id) {
        ensureReady();
        CourseSelectionTask task = getTask(id);
        long activeSelections = recordMapper.selectCount(new LambdaQueryWrapper<CourseSelectionRecord>()
            .eq(CourseSelectionRecord::getTaskId, id)
            .eq(CourseSelectionRecord::getIsDeleted, 0)
            .eq(CourseSelectionRecord::getSelectStatus, RECORD_STATUS_SELECTED));
        if (activeSelections > 0) {
            throw new BusinessException("当前任务仍有学生已选课，不能删除");
        }
        task.setIsDeleted(1);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    public List<CourseSelectionRecordVO> listTaskStudents(Long taskId) {
        ensureReady();
        getTask(taskId);
        return recordMapper.selectList(new LambdaQueryWrapper<CourseSelectionRecord>()
                .eq(CourseSelectionRecord::getTaskId, taskId)
                .eq(CourseSelectionRecord::getIsDeleted, 0)
                .eq(CourseSelectionRecord::getSelectStatus, RECORD_STATUS_SELECTED)
                .orderByAsc(CourseSelectionRecord::getSelectedAt))
            .stream()
            .map(this::toRecordVO)
            .toList();
    }

    public List<CourseSelectionStudentTaskVO> listStudentTasks(Long userId, String term, String keyword) {
        ensureReady();
        ensureStudentProfile(userId);
        Map<Long, Integer> selectedCountMap = buildSelectedCountMap();
        Map<Long, CourseSelectionRecord> selectedRecordMap = recordMapper.selectList(new LambdaQueryWrapper<CourseSelectionRecord>()
                .eq(CourseSelectionRecord::getStudentUserId, userId)
                .eq(CourseSelectionRecord::getIsDeleted, 0))
            .stream()
            .collect(Collectors.toMap(CourseSelectionRecord::getTaskId, Function.identity(), (left, right) -> right));

        List<CourseSelectionTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<CourseSelectionTask>()
            .eq(CourseSelectionTask::getIsDeleted, 0));
        syncDynamicStatuses(tasks);
        return tasks.stream()
            .map(task -> toStudentTaskVO(task, selectedCountMap.getOrDefault(task.getId(), 0), selectedRecordMap.get(task.getId())))
            .filter(item -> matchStudentTask(item, term, keyword))
            .sorted(Comparator.comparing(CourseSelectionStudentTaskVO::getSelectionEndTime, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    public void selectTask(Long taskId, Long userId) {
        ensureReady();
        CourseSelectionTask task = getTask(taskId);
        if (!TASK_STATUS_OPEN.equals(resolveTaskStatus(task.getSelectionStartTime(), task.getSelectionEndTime(), task.getTaskStatus()))) {
            throw new BusinessException("当前任务不在选课开放时间内");
        }

        int selectedCount = buildSelectedCountMap().getOrDefault(taskId, 0);
        if (selectedCount >= task.getCapacity()) {
            throw new BusinessException("当前课程已满，无法选课");
        }

        StudentContext studentContext = ensureStudentProfile(userId);
        CourseSelectionRecord existing = recordMapper.selectOne(new LambdaQueryWrapper<CourseSelectionRecord>()
            .eq(CourseSelectionRecord::getTaskId, taskId)
            .eq(CourseSelectionRecord::getStudentUserId, userId)
            .eq(CourseSelectionRecord::getIsDeleted, 0)
            .last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (existing != null && RECORD_STATUS_SELECTED.equals(existing.getSelectStatus())) {
            throw new BusinessException("您已选择该课程");
        }
        if (existing == null) {
            existing = new CourseSelectionRecord();
            existing.setTaskId(taskId);
            existing.setStudentUserId(userId);
            existing.setStudentNo(studentContext.student().getStudentNo());
            existing.setStudentName(studentContext.user().getRealName());
            existing.setClassName(studentContext.orgClass().getClassName());
            existing.setCreatedAt(now);
            existing.setIsDeleted(0);
        }
        existing.setSelectStatus(RECORD_STATUS_SELECTED);
        existing.setSelectedAt(now);
        existing.setDroppedAt(null);
        existing.setUpdatedAt(now);
        if (existing.getId() == null) {
            recordMapper.insert(existing);
        } else {
            recordMapper.updateById(existing);
        }
    }

    public void dropTask(Long taskId, Long userId) {
        ensureReady();
        CourseSelectionTask task = getTask(taskId);
        if (LocalDateTime.now().isAfter(task.getSelectionEndTime())) {
            throw new BusinessException("选课已截止，无法退选");
        }
        CourseSelectionRecord existing = recordMapper.selectOne(new LambdaQueryWrapper<CourseSelectionRecord>()
            .eq(CourseSelectionRecord::getTaskId, taskId)
            .eq(CourseSelectionRecord::getStudentUserId, userId)
            .eq(CourseSelectionRecord::getIsDeleted, 0)
            .last("limit 1"));
        if (existing == null || !RECORD_STATUS_SELECTED.equals(existing.getSelectStatus())) {
            throw new BusinessException("当前课程未处于已选状态");
        }
        existing.setSelectStatus(RECORD_STATUS_DROPPED);
        existing.setDroppedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        recordMapper.updateById(existing);
    }

    private void ensureReady() {
        if (initialized.get()) {
            return;
        }
        synchronized (initialized) {
            if (initialized.get()) {
                return;
            }
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS course_selection_task (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    task_code VARCHAR(50) NOT NULL UNIQUE,
                    term VARCHAR(50) NOT NULL,
                    course_name VARCHAR(100) NOT NULL,
                    teacher_name VARCHAR(100) NOT NULL,
                    credit DECIMAL(5,2) NOT NULL,
                    capacity INT NOT NULL,
                    selection_start_time TIMESTAMP NOT NULL,
                    selection_end_time TIMESTAMP NOT NULL,
                    task_status VARCHAR(20) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    remark VARCHAR(500)
                )
                """);
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS course_selection_record (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    task_id BIGINT NOT NULL,
                    student_user_id BIGINT NOT NULL,
                    student_no VARCHAR(50) NOT NULL,
                    student_name VARCHAR(100) NOT NULL,
                    class_name VARCHAR(100) NOT NULL,
                    select_status VARCHAR(20) NOT NULL,
                    selected_at TIMESTAMP,
                    dropped_at TIMESTAMP,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    remark VARCHAR(500),
                    CONSTRAINT uk_course_selection_record UNIQUE (task_id, student_user_id)
                )
                """);
            seedInitialData();
            initialized.set(true);
        }
    }

    private void seedInitialData() {
        long taskCount = taskMapper.selectCount(new LambdaQueryWrapper<CourseSelectionTask>()
            .eq(CourseSelectionTask::getIsDeleted, 0));
        if (taskCount > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        CourseSelectionTask openTask = buildSeedTask(
            "2025-2026-2",
            "软件工程",
            "Teacher Li",
            BigDecimal.valueOf(3.0),
            80,
            now.minusDays(1),
            now.plusDays(5)
        );
        taskMapper.insert(openTask);
        CourseSelectionTask upcomingTask = buildSeedTask(
            "2025-2026-2",
            "人工智能导论",
            "Teacher Zhao",
            BigDecimal.valueOf(2.0),
            40,
            now.plusDays(1),
            now.plusDays(6)
        );
        taskMapper.insert(upcomingTask);
        CourseSelectionTask closedTask = buildSeedTask(
            "2025-2026-1",
            "离散数学",
            "Teacher Zhang",
            BigDecimal.valueOf(3.0),
            70,
            now.minusDays(30),
            now.minusDays(20)
        );
        taskMapper.insert(closedTask);

        List<StudentContext> students = studentService.list(new LambdaQueryWrapper<EduStudent>()
                .eq(EduStudent::getIsDeleted, 0)
                .eq(EduStudent::getStatus, 1))
            .stream()
            .map(student -> {
                SysUser user = userService.getById(student.getUserId());
                OrgClass orgClass = classService.getById(student.getClassId());
                if (user == null || orgClass == null) {
                    return null;
                }
                return new StudentContext(student, user, orgClass);
            })
            .filter(Objects::nonNull)
            .limit(3)
            .toList();
        for (int i = 0; i < students.size(); i++) {
            StudentContext context = students.get(i);
            CourseSelectionRecord record = new CourseSelectionRecord();
            record.setTaskId(i == 2 ? closedTask.getId() : openTask.getId());
            record.setStudentUserId(context.user().getId());
            record.setStudentNo(context.student().getStudentNo());
            record.setStudentName(context.user().getRealName());
            record.setClassName(context.orgClass().getClassName());
            record.setSelectStatus(RECORD_STATUS_SELECTED);
            record.setSelectedAt(now.minusHours(6L - i));
            record.setCreatedAt(now.minusHours(6L - i));
            record.setUpdatedAt(now.minusHours(6L - i));
            record.setIsDeleted(0);
            recordMapper.insert(record);
        }
    }

    private CourseSelectionTask buildSeedTask(
        String term,
        String courseName,
        String teacherName,
        BigDecimal credit,
        Integer capacity,
        LocalDateTime start,
        LocalDateTime end
    ) {
        LocalDateTime now = LocalDateTime.now();
        CourseSelectionTask task = new CourseSelectionTask();
        task.setTaskCode(generateTaskCode(now));
        task.setTerm(term);
        task.setCourseName(courseName);
        task.setTeacherName(teacherName);
        task.setCredit(credit);
        task.setCapacity(capacity);
        task.setSelectionStartTime(start);
        task.setSelectionEndTime(end);
        task.setTaskStatus(resolveTaskStatus(start, end, null));
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        task.setIsDeleted(0);
        task.setRemark("Seed task");
        return task;
    }

    private String generateTaskCode(LocalDateTime time) {
        LocalDateTime codeTime = time == null ? LocalDateTime.now() : time;
        String baseCode = "SEL" + codeTime.format(CODE_FORMATTER);
        String candidate = baseCode;
        int suffix = 1;
        while (taskCodeExists(candidate)) {
            candidate = baseCode + String.format(Locale.ROOT, "%02d", suffix);
            suffix += 1;
        }
        return candidate;
    }

    private boolean taskCodeExists(String taskCode) {
        return taskMapper.selectCount(new LambdaQueryWrapper<CourseSelectionTask>()
            .eq(CourseSelectionTask::getTaskCode, taskCode)) > 0;
    }

    private void syncDynamicStatuses(List<CourseSelectionTask> tasks) {
        LocalDateTime now = LocalDateTime.now();
        for (CourseSelectionTask task : tasks) {
            String current = resolveTaskStatus(task.getSelectionStartTime(), task.getSelectionEndTime(), task.getTaskStatus(), now);
            if (!current.equals(task.getTaskStatus())) {
                task.setTaskStatus(current);
                task.setUpdatedAt(now);
                taskMapper.updateById(task);
            }
        }
    }

    private Map<Long, Integer> buildSelectedCountMap() {
        return recordMapper.selectList(new LambdaQueryWrapper<CourseSelectionRecord>()
                .eq(CourseSelectionRecord::getIsDeleted, 0)
                .eq(CourseSelectionRecord::getSelectStatus, RECORD_STATUS_SELECTED))
            .stream()
            .collect(Collectors.groupingBy(CourseSelectionRecord::getTaskId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    private CourseSelectionAdminTaskVO toAdminTaskVO(CourseSelectionTask task, int selectedCount) {
        CourseSelectionAdminTaskVO vo = new CourseSelectionAdminTaskVO();
        vo.setId(task.getId());
        vo.setTaskCode(task.getTaskCode());
        vo.setTerm(task.getTerm());
        vo.setCourseName(task.getCourseName());
        vo.setTeacherName(task.getTeacherName());
        vo.setCredit(task.getCredit());
        vo.setSelectedCount(selectedCount);
        vo.setCapacity(task.getCapacity());
        vo.setSelectionStartTime(task.getSelectionStartTime());
        vo.setSelectionEndTime(task.getSelectionEndTime());
        String status = resolveTaskStatus(task.getSelectionStartTime(), task.getSelectionEndTime(), task.getTaskStatus());
        vo.setStatus(status);
        vo.setStatusLabel(resolveTaskStatusLabel(status));
        vo.setRemark(task.getRemark());
        return vo;
    }

    private CourseSelectionStudentTaskVO toStudentTaskVO(CourseSelectionTask task, int selectedCount, CourseSelectionRecord record) {
        CourseSelectionStudentTaskVO vo = new CourseSelectionStudentTaskVO();
        vo.setId(task.getId());
        vo.setTerm(task.getTerm());
        vo.setCourseName(task.getCourseName());
        vo.setTeacherName(task.getTeacherName());
        vo.setCredit(task.getCredit());
        vo.setSelectedCount(selectedCount);
        vo.setCapacity(task.getCapacity());
        vo.setSelectionEndTime(task.getSelectionEndTime());
        String taskStatus = resolveTaskStatus(task.getSelectionStartTime(), task.getSelectionEndTime(), task.getTaskStatus());
        String selectionStatus;
        if (record != null && RECORD_STATUS_SELECTED.equals(record.getSelectStatus())) {
            selectionStatus = "SELECTED";
            vo.setSelected(true);
        } else if (!TASK_STATUS_OPEN.equals(taskStatus)) {
            selectionStatus = "CLOSED";
            vo.setSelected(false);
        } else if (selectedCount >= task.getCapacity()) {
            selectionStatus = "FULL";
            vo.setSelected(false);
        } else {
            selectionStatus = "AVAILABLE";
            vo.setSelected(false);
        }
        vo.setSelectionStatus(selectionStatus);
        vo.setSelectionStatusLabel(resolveStudentStatusLabel(selectionStatus));
        return vo;
    }

    private CourseSelectionRecordVO toRecordVO(CourseSelectionRecord record) {
        CourseSelectionRecordVO vo = new CourseSelectionRecordVO();
        vo.setId(record.getId());
        vo.setStudentNo(record.getStudentNo());
        vo.setStudentName(record.getStudentName());
        vo.setClassName(record.getClassName());
        vo.setSelectedAt(record.getSelectedAt());
        vo.setStatus(record.getSelectStatus());
        vo.setStatusLabel(RECORD_STATUS_SELECTED.equals(record.getSelectStatus()) ? "已选" : "已退选");
        return vo;
    }

    private boolean matchAdminTask(CourseSelectionAdminTaskVO item, String term, String status, String keyword) {
        if (StringUtils.hasText(term) && !term.equals(item.getTerm())) {
            return false;
        }
        if (StringUtils.hasText(status) && !status.equals(item.getStatus())) {
            return false;
        }
        if (StringUtils.hasText(keyword)) {
            String normalized = keyword.trim().toLowerCase(Locale.ROOT);
            return contains(item.getCourseName(), normalized) || contains(item.getTeacherName(), normalized);
        }
        return true;
    }

    private boolean matchStudentTask(CourseSelectionStudentTaskVO item, String term, String keyword) {
        if (StringUtils.hasText(term) && !term.equals(item.getTerm())) {
            return false;
        }
        if (StringUtils.hasText(keyword)) {
            String normalized = keyword.trim().toLowerCase(Locale.ROOT);
            return contains(item.getCourseName(), normalized) || contains(item.getTeacherName(), normalized);
        }
        return true;
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private CourseSelectionTask getTask(Long id) {
        CourseSelectionTask task = taskMapper.selectById(id);
        if (task == null || task.getIsDeleted() == 1) {
            throw new BusinessException("选课任务不存在");
        }
        return task;
    }

    private StudentContext ensureStudentProfile(Long userId) {
        EduStudent student = studentService.getOne(new LambdaQueryWrapper<EduStudent>()
            .eq(EduStudent::getUserId, userId)
            .eq(EduStudent::getIsDeleted, 0)
            .last("limit 1"));
        if (student == null) {
            throw new BusinessException("当前用户未绑定学生档案");
        }
        SysUser user = userService.getById(userId);
        OrgClass orgClass = classService.getById(student.getClassId());
        if (user == null || user.getIsDeleted() == 1 || orgClass == null || orgClass.getIsDeleted() == 1) {
            throw new BusinessException("学生档案信息不完整");
        }
        return new StudentContext(student, user, orgClass);
    }

    private void validateTaskRequest(CourseSelectionTaskSaveRequest request) {
        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new BusinessException("容量必须大于 0");
        }
        if (request.getSelectionEndTime() == null || request.getSelectionStartTime() == null
            || !request.getSelectionEndTime().isAfter(request.getSelectionStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
    }

    private String resolveTaskStatus(LocalDateTime start, LocalDateTime end, String currentStatus) {
        return resolveTaskStatus(start, end, currentStatus, LocalDateTime.now());
    }

    private String resolveTaskStatus(LocalDateTime start, LocalDateTime end, String currentStatus, LocalDateTime now) {
        if (TASK_STATUS_CLOSED.equals(currentStatus) && end != null && !end.isAfter(now)) {
            return TASK_STATUS_CLOSED;
        }
        if (end != null && !end.isAfter(now)) {
            return TASK_STATUS_CLOSED;
        }
        if (start != null && start.isAfter(now)) {
            return TASK_STATUS_NOT_STARTED;
        }
        return TASK_STATUS_OPEN;
    }

    private String resolveTaskStatusLabel(String status) {
        return switch (status) {
            case TASK_STATUS_NOT_STARTED -> "未开始";
            case TASK_STATUS_OPEN -> "进行中";
            default -> "已结束";
        };
    }

    private String resolveStudentStatusLabel(String status) {
        return switch (status) {
            case "AVAILABLE" -> "可选";
            case "SELECTED" -> "已选";
            case "FULL" -> "已满";
            default -> "已关闭";
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private record StudentContext(EduStudent student, SysUser user, OrgClass orgClass) {
    }
}
