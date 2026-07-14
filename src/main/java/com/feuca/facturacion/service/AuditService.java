package com.feuca.facturacion.service;

import java.util.UUID;

public interface AuditService {

    void recordSuccess(String accion, String recurso, String recursoId, UUID empresaId, String metadata);

    void recordFailure(String accion, String recurso, String recursoId, UUID empresaId, String metadata);

    void recordLogin(String email, boolean success, String metadata);
}
