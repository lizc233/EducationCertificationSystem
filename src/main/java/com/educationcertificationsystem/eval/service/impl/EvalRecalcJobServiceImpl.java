package com.educationcertificationsystem.eval.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.model.entity.EvalRecalcJob;
import com.educationcertificationsystem.eval.service.EvalRecalcJobService;
import com.educationcertificationsystem.eval.mapper.EvalRecalcJobMapper;
import com.educationcertificationsystem.support.EntityAuditSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

/**
* @author Lizc233
* @description 针对表【eval_recalc_job(重算任务表)】的数据库操作Service实现
* @createDate 2026-07-16 14:29:33
*/
@Service
public class EvalRecalcJobServiceImpl extends ServiceImpl<EvalRecalcJobMapper, EvalRecalcJob>
    implements EvalRecalcJobService{

    private static final DateTimeFormatter JOB_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Override
    public EvalRecalcJob createJob(String jobType, String relationType, Long relationId, String remark) {
        EvalRecalcJob job = new EvalRecalcJob();
        job.setJobNo("EVAL-" + LocalDateTime.now().format(JOB_FORMATTER));
        job.setJobType(jobType);
        job.setRelationType(relationType);
        job.setRelationId(relationId);
        job.setStatus("QUEUED");
        job.setRetryCount(0);
        job.setQueuedAt(LocalDateTime.now());
        job.setRemark(remark);
        EntityAuditSupport.touchCreate(job);
        save(job);
        return job;
    }

    @Override
    public EvalRecalcJob markRunning(Long jobId) {
        EvalRecalcJob job = baseMapper.selectActiveById(jobId);
        if (job == null) {
            return null;
        }
        job.setStatus("RUNNING");
        job.setStartedAt(LocalDateTime.now());
        EntityAuditSupport.touchUpdate(job);
        updateById(job);
        return job;
    }

    @Override
    public EvalRecalcJob markSuccess(Long jobId) {
        EvalRecalcJob job = baseMapper.selectActiveById(jobId);
        if (job == null) {
            return null;
        }
        job.setStatus("SUCCESS");
        job.setFinishedAt(LocalDateTime.now());
        job.setErrorMessage(null);
        EntityAuditSupport.touchUpdate(job);
        updateById(job);
        return job;
    }

    @Override
    public EvalRecalcJob markFailed(Long jobId, String errorMessage) {
        EvalRecalcJob job = baseMapper.selectActiveById(jobId);
        if (job == null) {
            return null;
        }
        job.setStatus("FAILED");
        job.setErrorMessage(errorMessage);
        job.setFinishedAt(LocalDateTime.now());
        job.setRetryCount(job.getRetryCount() == null ? 1 : job.getRetryCount() + 1);
        EntityAuditSupport.touchUpdate(job);
        updateById(job);
        return job;
    }
}




