package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning(
            pointcut = "execution(* com.feuca.facturacion.service.impl.UsuarioServiceImpl.create(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.UsuarioServiceImpl.update(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.UsuarioServiceImpl.delete(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.EmpresaServiceImpl.create(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.EmpresaServiceImpl.update*(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.EmpresaServiceImpl.delete*(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.FacturaServiceImpl.create(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.FacturaServiceImpl.update(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.FacturaServiceImpl.delete(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.FacturaServiceImpl.prepararParaEnvio(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.FacturaServiceImpl.enviarAHacienda(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.IvaTasaServiceImpl.create(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.IvaTasaServiceImpl.update(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.IvaTasaServiceImpl.delete*(..))",
            returning = "result"
    )
    public void auditSuccess(JoinPoint joinPoint, Object result) {
        AuditTarget target = target(joinPoint, result);
        auditService.recordSuccess(target.accion(), target.recurso(), target.recursoId(), target.empresaId(), target.metadata());
    }

    @AfterThrowing(
            pointcut = "execution(* com.feuca.facturacion.service.impl.UsuarioServiceImpl.create(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.UsuarioServiceImpl.update(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.UsuarioServiceImpl.delete(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.EmpresaServiceImpl.create(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.EmpresaServiceImpl.update*(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.EmpresaServiceImpl.delete*(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.FacturaServiceImpl.create(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.FacturaServiceImpl.update(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.FacturaServiceImpl.delete(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.FacturaServiceImpl.prepararParaEnvio(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.FacturaServiceImpl.enviarAHacienda(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.IvaTasaServiceImpl.create(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.IvaTasaServiceImpl.update(..))"
                    + " || execution(* com.feuca.facturacion.service.impl.IvaTasaServiceImpl.delete*(..))",
            throwing = "exception"
    )
    public void auditFailure(JoinPoint joinPoint, Throwable exception) {
        AuditTarget target = target(joinPoint, null);
        auditService.recordFailure(target.accion(), target.recurso(), target.recursoId(), target.empresaId(),
                "error=" + exception.getClass().getSimpleName());
    }

    private AuditTarget target(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String recurso = resourceName(className);
        String accion = actionName(className, methodName);
        UUID empresaId = firstUuidByName(joinPoint, "empresaId")
                .or(() -> extractUuid(result, "getEmpresaId"))
                .or(() -> firstRequestEmpresaId(joinPoint))
                .orElse(null);
        String recursoId = firstUuidByName(joinPoint, "id")
                .or(() -> firstUuidByName(joinPoint, "usuarioId"))
                .or(() -> firstUuidByName(joinPoint, "facturaId"))
                .or(() -> extractUuid(result, "getId"))
                .map(UUID::toString)
                .orElse(null);

        return new AuditTarget(accion, recurso, recursoId, empresaId, "method=" + methodName);
    }

    private String resourceName(String className) {
        return className.replace("ServiceImpl", "");
    }

    private String actionName(String className, String methodName) {
        String recurso = resourceName(className).toUpperCase();
        if ("enviarAHacienda".equals(methodName)) {
            return "EMITIR_" + recurso;
        }
        if ("prepararParaEnvio".equals(methodName)) {
            return "PREPARAR_ENVIO_" + recurso;
        }
        if (methodName.startsWith("delete")) {
            return "ELIMINAR_" + recurso;
        }
        if (methodName.startsWith("update")) {
            return "ACTUALIZAR_" + recurso;
        }
        if (methodName.startsWith("create")) {
            return "CREAR_" + recurso;
        }
        return methodName.toUpperCase() + "_" + recurso;
    }

    private Optional<UUID> firstUuidByName(JoinPoint joinPoint, String name) {
        if (!(joinPoint.getSignature() instanceof MethodSignature signature)) {
            return Optional.empty();
        }
        String[] names = signature.getParameterNames();
        Object[] values = joinPoint.getArgs();
        for (int i = 0; i < names.length && i < values.length; i++) {
            if (name.equals(names[i]) && values[i] instanceof UUID uuid) {
                return Optional.of(uuid);
            }
        }
        return Optional.empty();
    }

    private Optional<UUID> firstRequestEmpresaId(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            Optional<UUID> empresaId = extractUuid(arg, "getEmpresaId");
            if (empresaId.isPresent()) {
                return empresaId;
            }
        }
        return Optional.empty();
    }

    private Optional<UUID> extractUuid(Object target, String methodName) {
        if (target == null) {
            return Optional.empty();
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof UUID uuid ? Optional.of(uuid) : Optional.empty();
        } catch (ReflectiveOperationException ignored) {
            return Optional.empty();
        }
    }

    private record AuditTarget(String accion, String recurso, String recursoId, UUID empresaId, String metadata) {
    }
}
