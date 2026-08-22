package com.dayflow.hrmtool.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String oldValue = args.length > 0 ? args[0].toString() : "null"; 

        Object result = joinPoint.proceed();

        String newValue = result != null ? result.toString() : "null";

        AuditLog log = new AuditLog();
        log.setAction(auditable.action().isEmpty() ? joinPoint.getSignature().getName() : auditable.action());
        log.setEntityName(joinPoint.getTarget().getClass().getSimpleName());
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setChangedBy("system"); // Retrieve from SecurityContextHolder in reality
        log.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(log);

        return result;
    }
}
