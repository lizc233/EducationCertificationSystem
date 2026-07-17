package com.educationcertificationsystem.eval.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.course.service.EduCourseService;
import com.educationcertificationsystem.eval.mapper.EvalModelMapper;
import com.educationcertificationsystem.eval.service.EvalModelItemService;
import com.educationcertificationsystem.eval.service.EvalModelScopeService;
import com.educationcertificationsystem.eval.service.EvalModelService;
import com.educationcertificationsystem.model.dto.eval.EvalModelItemRequest;
import com.educationcertificationsystem.model.dto.eval.EvalModelSaveRequest;
import com.educationcertificationsystem.model.dto.eval.EvalModelScopeRequest;
import com.educationcertificationsystem.model.entity.EduCourse;
import com.educationcertificationsystem.model.entity.EvalModel;
import com.educationcertificationsystem.model.entity.EvalModelItem;
import com.educationcertificationsystem.model.entity.EvalModelScope;
import com.educationcertificationsystem.model.entity.TrProgramVersion;
import com.educationcertificationsystem.model.vo.eval.EvalModelDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalModelItemVO;
import com.educationcertificationsystem.model.vo.eval.EvalModelPageItemVO;
import com.educationcertificationsystem.model.vo.eval.EvalModelScopeVO;
import com.educationcertificationsystem.program.service.TrProgramVersionService;
import com.educationcertificationsystem.support.EntityAuditSupport;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EvalModelServiceImpl extends ServiceImpl<EvalModelMapper, EvalModel>
        implements EvalModelService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final EvalModelItemService evalModelItemService;
    private final EvalModelScopeService evalModelScopeService;
    private final EduCourseService eduCourseService;
    private final TrProgramVersionService trProgramVersionService;

    @Override
    public EvalModel getActiveById(Long id) {
        return baseMapper.selectActiveById(id);
    }

    @Override
    public Page<EvalModelPageItemVO> pageByCondition(long pageNum, long pageSize, String modelType, String scopeType,
                                                     String status, Integer enabled, String keyword) {
        long current = Math.max(pageNum, 1);
        long size = Math.max(pageSize, 1);
        long offset = (current - 1) * size;
        long total = baseMapper.countByCondition(modelType, scopeType, status, enabled, keyword);
        List<EvalModelPageItemVO> records = total == 0
                ? List.of()
                : baseMapper.selectPageByCondition(offset, size, modelType, scopeType, status, enabled, keyword);
        Page<EvalModelPageItemVO> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public EvalModelDetailVO getDetail(Long id) {
        EvalModel model = getActiveById(id);
        if (model == null) {
            return null;
        }

        List<EvalModelItem> items = evalModelItemService.listActiveByModelId(id);
        List<EvalModelScope> scopes = evalModelScopeService.listActiveByModelId(id);

        EvalModelDetailVO detail = new EvalModelDetailVO();
        detail.setId(model.getId());
        detail.setModelCode(model.getModelCode());
        detail.setModelName(model.getModelName());
        detail.setModelType(model.getModelType());
        detail.setScopeType(model.getScopeType());
        detail.setFormulaExpression(model.getFormulaExpression());
        detail.setThresholdValue(model.getThresholdValue());
        detail.setIncludeQuestionnaireFlag(model.getIncludeQuestionnaireFlag());
        detail.setEnabled(model.getEnabled());
        detail.setStatus(model.getStatus());
        detail.setRemark(model.getRemark());
        detail.setCreatedAt(model.getCreatedAt());
        detail.setUpdatedAt(model.getUpdatedAt());
        detail.setItems(toItemVOList(items));
        detail.setScopes(toScopeVOList(scopes));
        detail.setItemCount(detail.getItems().size());
        detail.setScopeCount(detail.getScopes().size());
        return detail;
    }

    @Override
    @Transactional
    public EvalModelDetailVO createModel(EvalModelSaveRequest request) {
        validateRequest(request, null);

        EvalModel model = new EvalModel();
        fillModel(model, request);
        EntityAuditSupport.touchCreate(model);
        save(model);

        evalModelItemService.replaceModelItems(model.getId(), buildItems(model.getId(), request.getItems()));
        evalModelScopeService.replaceModelScopes(model.getId(), buildScopes(model.getId(), request));
        return getDetail(model.getId());
    }

    @Override
    @Transactional
    public EvalModelDetailVO updateModel(Long id, EvalModelSaveRequest request) {
        EvalModel model = getActiveById(id);
        if (model == null) {
            throw new IllegalArgumentException("Model not found");
        }
        validateRequest(request, id);

        fillModel(model, request);
        EntityAuditSupport.touchUpdate(model);
        updateById(model);

        evalModelItemService.replaceModelItems(id, buildItems(id, request.getItems()));
        evalModelScopeService.replaceModelScopes(id, buildScopes(id, request));
        return getDetail(id);
    }

    @Override
    @Transactional
    public void deleteModel(Long id) {
        EvalModel model = getActiveById(id);
        if (model == null) {
            throw new IllegalArgumentException("Model not found");
        }
        if (baseMapper.countResultReferences(id) > 0) {
            throw new IllegalStateException("Model is referenced by result data and cannot be deleted");
        }
        EntityAuditSupport.touchDelete(model);
        updateById(model);
    }

    @Override
    @Transactional
    public EvalModel updateEnabled(Long id, Integer enabled) {
        EvalModel model = getActiveById(id);
        if (model == null) {
            throw new IllegalArgumentException("Model not found");
        }
        model.setEnabled(enabled == null || enabled != 0 ? 1 : 0);
        EntityAuditSupport.touchUpdate(model);
        updateById(model);
        return model;
    }

    @Override
    @Transactional
    public EvalModel updateStatus(Long id, String status) {
        EvalModel model = getActiveById(id);
        if (model == null) {
            throw new IllegalArgumentException("Model not found");
        }
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Status cannot be blank");
        }
        model.setStatus(status.trim());
        EntityAuditSupport.touchUpdate(model);
        updateById(model);
        return model;
    }

    private void validateRequest(EvalModelSaveRequest request, Long excludeId) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (!StringUtils.hasText(request.getModelCode())) {
            throw new IllegalArgumentException("Model code cannot be blank");
        }
        if (!StringUtils.hasText(request.getModelName())) {
            throw new IllegalArgumentException("Model name cannot be blank");
        }
        if (!StringUtils.hasText(request.getModelType())) {
            throw new IllegalArgumentException("Model type cannot be blank");
        }
        if (!StringUtils.hasText(request.getScopeType())) {
            throw new IllegalArgumentException("Scope type cannot be blank");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Model items cannot be empty");
        }
        if (request.getScopes() == null || request.getScopes().isEmpty()) {
            throw new IllegalArgumentException("Model scopes cannot be empty");
        }
        if (baseMapper.countByModelCode(request.getModelCode().trim(), excludeId) > 0) {
            throw new IllegalArgumentException("Model code already exists");
        }
        validateItems(request.getItems());
        validateScopes(request.getScopeType().trim(), request.getScopes());
    }

    private void validateItems(List<EvalModelItemRequest> items) {
        LinkedHashSet<String> itemCodes = new LinkedHashSet<>();
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (EvalModelItemRequest item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Model item cannot be null");
            }
            if (!StringUtils.hasText(item.getItemCode())) {
                throw new IllegalArgumentException("Item code cannot be blank");
            }
            if (!StringUtils.hasText(item.getItemName())) {
                throw new IllegalArgumentException("Item name cannot be blank");
            }
            if (!StringUtils.hasText(item.getItemType())) {
                throw new IllegalArgumentException("Item type cannot be blank");
            }
            if (item.getWeightPercent() == null) {
                throw new IllegalArgumentException("Item weight cannot be null");
            }
            if (item.getWeightPercent().compareTo(BigDecimal.ZERO) < 0
                    || item.getWeightPercent().compareTo(HUNDRED) > 0) {
                throw new IllegalArgumentException("Item weight must be between 0 and 100");
            }
            String itemCode = item.getItemCode().trim();
            if (!itemCodes.add(itemCode)) {
                throw new IllegalArgumentException("Duplicate item code: " + itemCode);
            }
            if (item.getEnabled() == null || item.getEnabled() != 0) {
                totalWeight = totalWeight.add(item.getWeightPercent());
            }
        }
        if (totalWeight.compareTo(HUNDRED) != 0) {
            throw new IllegalArgumentException("Enabled item weights must sum to 100");
        }
    }

    private void validateScopes(String modelScopeType, List<EvalModelScopeRequest> scopes) {
        LinkedHashSet<String> uniqueScopes = new LinkedHashSet<>();
        for (EvalModelScopeRequest scope : scopes) {
            if (scope == null) {
                throw new IllegalArgumentException("Model scope cannot be null");
            }
            String scopeType = StringUtils.hasText(scope.getScopeType())
                    ? scope.getScopeType().trim()
                    : modelScopeType;
            if (!modelScopeType.equals(scopeType)) {
                throw new IllegalArgumentException("Scope type must match model scope type");
            }
            if (scope.getScopeId() == null) {
                throw new IllegalArgumentException("Scope id cannot be null");
            }
            String uniqueKey = scopeType + ":" + scope.getScopeId();
            if (!uniqueScopes.add(uniqueKey)) {
                throw new IllegalArgumentException("Duplicate scope: " + uniqueKey);
            }
            validateScopeExists(scopeType, scope.getScopeId());
        }
    }

    private void validateScopeExists(String scopeType, Long scopeId) {
        if ("COURSE".equalsIgnoreCase(scopeType)) {
            EduCourse course = eduCourseService.getById(scopeId);
            if (course == null || (course.getIsDeleted() != null && course.getIsDeleted() != 0)) {
                throw new IllegalArgumentException("Course scope not found: " + scopeId);
            }
            return;
        }
        if ("PROGRAM_VERSION".equalsIgnoreCase(scopeType)) {
            TrProgramVersion version = trProgramVersionService.getById(scopeId);
            if (version == null || (version.getIsDeleted() != null && version.getIsDeleted() != 0)) {
                throw new IllegalArgumentException("Program version scope not found: " + scopeId);
            }
        }
    }

    private void fillModel(EvalModel model, EvalModelSaveRequest request) {
        model.setModelCode(request.getModelCode().trim());
        model.setModelName(request.getModelName().trim());
        model.setModelType(request.getModelType().trim());
        model.setScopeType(request.getScopeType().trim());
        model.setFormulaExpression(request.getFormulaExpression());
        model.setThresholdValue(request.getThresholdValue());
        model.setIncludeQuestionnaireFlag(request.getIncludeQuestionnaireFlag() == null ? 0 : request.getIncludeQuestionnaireFlag());
        model.setEnabled(request.getEnabled() == null ? 1 : (request.getEnabled() == 0 ? 0 : 1));
        model.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "DRAFT");
        model.setRemark(request.getRemark());
    }

    private List<EvalModelItem> buildItems(Long modelId, List<EvalModelItemRequest> itemRequests) {
        List<EvalModelItem> items = new ArrayList<>(itemRequests.size());
        for (EvalModelItemRequest request : itemRequests) {
            EvalModelItem item = new EvalModelItem();
            item.setModelId(modelId);
            item.setItemCode(request.getItemCode().trim());
            item.setItemName(request.getItemName().trim());
            item.setItemType(request.getItemType().trim());
            item.setWeightPercent(request.getWeightPercent());
            item.setThresholdValue(request.getThresholdValue());
            item.setCalcRule(request.getCalcRule());
            item.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
            item.setEnabled(request.getEnabled() == null ? 1 : (request.getEnabled() == 0 ? 0 : 1));
            item.setRemark(request.getRemark());
            items.add(item);
        }
        return items;
    }

    private List<EvalModelScope> buildScopes(Long modelId, EvalModelSaveRequest request) {
        List<EvalModelScope> scopes = new ArrayList<>(request.getScopes().size());
        for (EvalModelScopeRequest scopeRequest : request.getScopes()) {
            EvalModelScope scope = new EvalModelScope();
            scope.setModelId(modelId);
            scope.setScopeType(StringUtils.hasText(scopeRequest.getScopeType())
                    ? scopeRequest.getScopeType().trim()
                    : request.getScopeType().trim());
            scope.setScopeId(scopeRequest.getScopeId());
            scope.setRemark(scopeRequest.getRemark());
            scopes.add(scope);
        }
        return scopes;
    }

    private List<EvalModelItemVO> toItemVOList(List<EvalModelItem> items) {
        List<EvalModelItemVO> result = new ArrayList<>(items.size());
        for (EvalModelItem item : items) {
            EvalModelItemVO vo = new EvalModelItemVO();
            vo.setId(item.getId());
            vo.setModelId(item.getModelId());
            vo.setItemCode(item.getItemCode());
            vo.setItemName(item.getItemName());
            vo.setItemType(item.getItemType());
            vo.setWeightPercent(item.getWeightPercent());
            vo.setThresholdValue(item.getThresholdValue());
            vo.setCalcRule(item.getCalcRule());
            vo.setSortNo(item.getSortNo());
            vo.setEnabled(item.getEnabled());
            vo.setRemark(item.getRemark());
            vo.setCreatedAt(item.getCreatedAt());
            vo.setUpdatedAt(item.getUpdatedAt());
            result.add(vo);
        }
        return result;
    }

    private List<EvalModelScopeVO> toScopeVOList(List<EvalModelScope> scopes) {
        Map<Long, String> courseNameMap = new HashMap<>();
        Map<Long, String> programVersionNameMap = new HashMap<>();
        List<Long> courseIds = new ArrayList<>();
        List<Long> versionIds = new ArrayList<>();

        for (EvalModelScope scope : scopes) {
            if ("COURSE".equalsIgnoreCase(scope.getScopeType())) {
                courseIds.add(scope.getScopeId());
            } else if ("PROGRAM_VERSION".equalsIgnoreCase(scope.getScopeType())) {
                versionIds.add(scope.getScopeId());
            }
        }

        if (!courseIds.isEmpty()) {
            for (EduCourse course : eduCourseService.listByIds(courseIds)) {
                if (course != null && (course.getIsDeleted() == null || course.getIsDeleted() == 0)) {
                    courseNameMap.put(course.getId(),
                            StringUtils.hasText(course.getCourseName()) ? course.getCourseName() : course.getCourseCode());
                }
            }
        }
        if (!versionIds.isEmpty()) {
            for (TrProgramVersion version : trProgramVersionService.listByIds(versionIds)) {
                if (version != null && (version.getIsDeleted() == null || version.getIsDeleted() == 0)) {
                    programVersionNameMap.put(version.getId(),
                            StringUtils.hasText(version.getVersionName()) ? version.getVersionName() : version.getVersionNo());
                }
            }
        }

        List<EvalModelScopeVO> result = new ArrayList<>(scopes.size());
        for (EvalModelScope scope : scopes) {
            EvalModelScopeVO vo = new EvalModelScopeVO();
            vo.setId(scope.getId());
            vo.setModelId(scope.getModelId());
            vo.setScopeType(scope.getScopeType());
            vo.setScopeId(scope.getScopeId());
            vo.setRemark(scope.getRemark());
            vo.setCreatedAt(scope.getCreatedAt());
            vo.setUpdatedAt(scope.getUpdatedAt());
            if ("COURSE".equalsIgnoreCase(scope.getScopeType())) {
                vo.setScopeName(courseNameMap.get(scope.getScopeId()));
            } else if ("PROGRAM_VERSION".equalsIgnoreCase(scope.getScopeType())) {
                vo.setScopeName(programVersionNameMap.get(scope.getScopeId()));
            }
            result.add(vo);
        }
        return result;
    }
}
