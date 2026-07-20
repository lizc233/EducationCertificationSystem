package com.educationcertificationsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.educationcertificationsystem.auth.RequireRoles;
import com.educationcertificationsystem.auth.RoleConstants;
import com.educationcertificationsystem.common.ApiResponse;
import com.educationcertificationsystem.common.BusinessException;
import com.educationcertificationsystem.common.PageResult;
import com.educationcertificationsystem.dto.system.DictItemSaveRequest;
import com.educationcertificationsystem.dto.system.DictTypeSaveRequest;
import com.educationcertificationsystem.dto.system.ParamSaveRequest;
import com.educationcertificationsystem.model.entity.SysDictItem;
import com.educationcertificationsystem.model.entity.SysDictType;
import com.educationcertificationsystem.model.entity.SysOperationLog;
import com.educationcertificationsystem.model.entity.SysParam;
import com.educationcertificationsystem.system.service.SysDictItemService;
import com.educationcertificationsystem.system.service.SysDictTypeService;
import com.educationcertificationsystem.system.service.SysOperationLogService;
import com.educationcertificationsystem.system.service.SysParamService;
import com.educationcertificationsystem.vo.system.DictItemVO;
import com.educationcertificationsystem.vo.system.DictTypeVO;
import com.educationcertificationsystem.vo.system.OperationLogVO;
import com.educationcertificationsystem.vo.system.ParamVO;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@RequireRoles(RoleConstants.SUPER_ADMIN)
public class SystemController {

    private final SysParamService paramService;

    private final SysDictTypeService dictTypeService;

    private final SysDictItemService dictItemService;

    private final SysOperationLogService operationLogService;

    public SystemController(
        SysParamService paramService,
        SysDictTypeService dictTypeService,
        SysDictItemService dictItemService,
        SysOperationLogService operationLogService
    ) {
        this.paramService = paramService;
        this.dictTypeService = dictTypeService;
        this.dictItemService = dictItemService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/params")
    public ApiResponse<List<ParamVO>> params(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "") String group,
        @RequestParam(defaultValue = "") String status
    ) {
        List<ParamVO> rows = paramService.list(new LambdaQueryWrapper<SysParam>()
                .eq(SysParam::getIsDeleted, 0)
                .orderByDesc(SysParam::getUpdatedAt))
            .stream()
            .map(this::toParamVO)
            .filter(item -> matchParam(item, keyword, group, status))
            .toList();
        return ApiResponse.success(rows);
    }

    @PostMapping("/params")
    public ApiResponse<Void> createParam(@Valid @RequestBody ParamSaveRequest request) {
        saveParam(null, request);
        return ApiResponse.success();
    }

    @PutMapping("/params/{id}")
    public ApiResponse<Void> updateParam(@PathVariable Long id, @Valid @RequestBody ParamSaveRequest request) {
        saveParam(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/params/{id}")
    public ApiResponse<Void> deleteParam(@PathVariable Long id) {
        SysParam param = getParam(id);
        param.setIsDeleted(1);
        param.setUpdatedAt(LocalDateTime.now());
        paramService.updateById(param);
        return ApiResponse.success();
    }

    @GetMapping("/dicts")
    public ApiResponse<List<DictTypeVO>> dicts() {
        List<DictItemVO> items = dictItemService.list(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getIsDeleted, 0)
                .orderByAsc(SysDictItem::getItemSort))
            .stream()
            .map(this::toDictItemVO)
            .toList();
        Map<Long, List<DictItemVO>> itemGroup = items.stream().collect(Collectors.groupingBy(DictItemVO::getDictTypeId));
        List<DictTypeVO> types = dictTypeService.list(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getIsDeleted, 0)
                .orderByAsc(SysDictType::getId))
            .stream()
            .map(type -> {
                DictTypeVO vo = new DictTypeVO();
                vo.setId(type.getId());
                vo.setDictType(type.getDictType());
                vo.setDictName(type.getDictName());
                vo.setStatus(type.getStatus());
                vo.setRemark(type.getRemark());
                vo.setItems(itemGroup.getOrDefault(type.getId(), List.of()));
                return vo;
            })
            .toList();
        return ApiResponse.success(types);
    }

    @PostMapping("/dicts/types")
    public ApiResponse<Void> createDictType(@Valid @RequestBody DictTypeSaveRequest request) {
        saveDictType(null, request);
        return ApiResponse.success();
    }

    @PutMapping("/dicts/types/{id}")
    public ApiResponse<Void> updateDictType(@PathVariable Long id, @Valid @RequestBody DictTypeSaveRequest request) {
        saveDictType(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/dicts/types/{id}")
    public ApiResponse<Void> deleteDictType(@PathVariable Long id) {
        if (dictItemService.count(new LambdaQueryWrapper<SysDictItem>()
            .eq(SysDictItem::getDictTypeId, id)
            .eq(SysDictItem::getIsDeleted, 0)) > 0) {
            throw new BusinessException("当前字典类型下仍有字典项，不能删除");
        }
        SysDictType type = getDictType(id);
        type.setIsDeleted(1);
        type.setUpdatedAt(LocalDateTime.now());
        dictTypeService.updateById(type);
        return ApiResponse.success();
    }

    @PostMapping("/dicts/items")
    public ApiResponse<Void> createDictItem(@Valid @RequestBody DictItemSaveRequest request) {
        saveDictItem(null, request);
        return ApiResponse.success();
    }

    @PutMapping("/dicts/items/{id}")
    public ApiResponse<Void> updateDictItem(@PathVariable Long id, @Valid @RequestBody DictItemSaveRequest request) {
        saveDictItem(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/dicts/items/{id}")
    public ApiResponse<Void> deleteDictItem(@PathVariable Long id) {
        SysDictItem item = getDictItem(id);
        item.setIsDeleted(1);
        item.setUpdatedAt(LocalDateTime.now());
        dictItemService.updateById(item);
        return ApiResponse.success();
    }

    @GetMapping("/logs")
    public ApiResponse<PageResult<OperationLogVO>> logs(
        @RequestParam(defaultValue = "1") Long pageNum,
        @RequestParam(defaultValue = "10") Long pageSize,
        @RequestParam(defaultValue = "") String operator,
        @RequestParam(defaultValue = "") String module,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        List<OperationLogVO> rows = operationLogService.list(new LambdaQueryWrapper<SysOperationLog>()
                .eq(SysOperationLog::getIsDeleted, 0)
                .orderByDesc(SysOperationLog::getCreatedAt))
            .stream()
            .map(this::toOperationLogVO)
            .filter(item -> matchLog(item, operator, module, startDate, endDate))
            .sorted(Comparator.comparing(OperationLogVO::getTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
        int fromIndex = Math.max(0, Math.toIntExact((pageNum - 1) * pageSize));
        int toIndex = Math.min(rows.size(), fromIndex + Math.toIntExact(pageSize));
        List<OperationLogVO> records = fromIndex >= rows.size() ? List.of() : rows.subList(fromIndex, toIndex);
        return ApiResponse.success(new PageResult<>((long) rows.size(), pageNum, pageSize, records));
    }

    @GetMapping("/logs/export")
    public ApiResponse<List<OperationLogVO>> exportLogs(
        @RequestParam(defaultValue = "") String operator,
        @RequestParam(defaultValue = "") String module,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        List<OperationLogVO> rows = operationLogService.list(new LambdaQueryWrapper<SysOperationLog>()
                .eq(SysOperationLog::getIsDeleted, 0)
                .orderByDesc(SysOperationLog::getCreatedAt))
            .stream()
            .map(this::toOperationLogVO)
            .filter(item -> matchLog(item, operator, module, startDate, endDate))
            .sorted(Comparator.comparing(OperationLogVO::getTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
        return ApiResponse.success(rows);
    }

    private void saveParam(Long id, ParamSaveRequest request) {
        SysParam param = id == null ? new SysParam() : getParam(id);
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            param.setCreatedAt(now);
            param.setIsDeleted(0);
        }
        param.setParamKey(request.getParamKey().trim());
        param.setParamValue(request.getParamValue().trim());
        param.setParamType(request.getParamType().trim());
        param.setIsSystem(request.getIsSystem() == null ? 0 : request.getIsSystem());
        param.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        param.setRemark(request.getRemark());
        param.setUpdatedAt(now);
        if (id == null) {
            paramService.save(param);
        } else {
            paramService.updateById(param);
        }
    }

    private void saveDictType(Long id, DictTypeSaveRequest request) {
        SysDictType type = id == null ? new SysDictType() : getDictType(id);
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            type.setCreatedAt(now);
            type.setIsDeleted(0);
        }
        type.setDictType(request.getDictType().trim());
        type.setDictName(request.getDictName().trim());
        type.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        type.setRemark(request.getRemark());
        type.setUpdatedAt(now);
        if (id == null) {
            dictTypeService.save(type);
        } else {
            dictTypeService.updateById(type);
        }
    }

    private void saveDictItem(Long id, DictItemSaveRequest request) {
        getDictType(request.getDictTypeId());
        SysDictItem item = id == null ? new SysDictItem() : getDictItem(id);
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            item.setCreatedAt(now);
            item.setIsDeleted(0);
        }
        item.setDictTypeId(request.getDictTypeId());
        item.setItemLabel(request.getItemLabel().trim());
        item.setItemValue(request.getItemValue().trim());
        item.setItemSort(request.getItemSort() == null ? 0 : request.getItemSort());
        item.setIsDefault(request.getIsDefault() == null ? 0 : request.getIsDefault());
        item.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        item.setRemark(request.getRemark());
        item.setUpdatedAt(now);
        if (id == null) {
            dictItemService.save(item);
        } else {
            dictItemService.updateById(item);
        }
    }

    private boolean matchParam(ParamVO item, String keyword, String group, String status) {
        String normalizedKeyword = normalize(keyword);
        if (StringUtils.hasText(normalizedKeyword)) {
            if (!contains(item.getKey(), normalizedKeyword) && !contains(item.getValue(), normalizedKeyword) && !contains(item.getDesc(), normalizedKeyword)) {
                return false;
            }
        }
        if (StringUtils.hasText(group) && !group.equals(item.getGroup())) {
            return false;
        }
        return !StringUtils.hasText(status) || status.equals(item.getStatus());
    }

    private boolean matchLog(OperationLogVO item, String operator, String module, String startDate, String endDate) {
        String normalizedOperator = normalize(operator);
        if (StringUtils.hasText(normalizedOperator) && !contains(item.getOperator(), normalizedOperator)) {
            return false;
        }
        if (StringUtils.hasText(module) && !module.equals(item.getModule())) {
            return false;
        }
        if (StringUtils.hasText(startDate)) {
            LocalDate start = LocalDate.parse(startDate);
            if (item.getTime() == null || item.getTime().toLocalDate().isBefore(start)) {
                return false;
            }
        }
        if (StringUtils.hasText(endDate)) {
            LocalDate end = LocalDate.parse(endDate);
            if (item.getTime() == null || item.getTime().toLocalDate().isAfter(end)) {
                return false;
            }
        }
        return true;
    }

    private boolean contains(String source, String target) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(target);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private ParamVO toParamVO(SysParam param) {
        ParamVO vo = new ParamVO();
        vo.setId(param.getId());
        vo.setGroup(resolveParamGroup(param.getParamKey()));
        vo.setKey(param.getParamKey());
        vo.setValue(param.getParamValue());
        vo.setType(param.getParamType());
        vo.setDesc(param.getRemark());
        vo.setStatus(param.getStatus() == 1 ? "启用" : "停用");
        vo.setUpdatedAt(param.getUpdatedAt());
        return vo;
    }

    private String resolveParamGroup(String key) {
        if (key.startsWith("mail.")) {
            return "邮件参数";
        }
        if (key.startsWith("achievement.") || key.startsWith("questionnaire.")) {
            return "认证参数";
        }
        return "基本参数";
    }

    private DictItemVO toDictItemVO(SysDictItem item) {
        DictItemVO vo = new DictItemVO();
        vo.setId(item.getId());
        vo.setDictTypeId(item.getDictTypeId());
        vo.setItemLabel(item.getItemLabel());
        vo.setItemValue(item.getItemValue());
        vo.setItemSort(item.getItemSort());
        vo.setIsDefault(item.getIsDefault());
        vo.setStatus(item.getStatus());
        vo.setRemark(item.getRemark());
        return vo;
    }

    private OperationLogVO toOperationLogVO(SysOperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(log.getId());
        vo.setTime(log.getCreatedAt());
        vo.setOperator(log.getOperatorName());
        vo.setModule(log.getModuleName());
        vo.setType(log.getLogType());
        vo.setIp(log.getIpAddress());
        vo.setResult(log.getSuccessFlag() == 1 ? "成功" : "失败");
        vo.setDetail(String.format("%s %s %s", defaultString(log.getRequestMethod()), defaultString(log.getRequestUri()), defaultString(log.getErrorMessage())).trim());
        return vo;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private SysParam getParam(Long id) {
        SysParam param = paramService.getById(id);
        if (param == null || param.getIsDeleted() == 1) {
            throw new BusinessException("参数不存在");
        }
        return param;
    }

    private SysDictType getDictType(Long id) {
        SysDictType type = dictTypeService.getById(id);
        if (type == null || type.getIsDeleted() == 1) {
            throw new BusinessException("字典类型不存在");
        }
        return type;
    }

    private SysDictItem getDictItem(Long id) {
        SysDictItem item = dictItemService.getById(id);
        if (item == null || item.getIsDeleted() == 1) {
            throw new BusinessException("字典项不存在");
        }
        return item;
    }
}
