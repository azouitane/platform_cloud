package com.virtacore.app.service;

import com.virtacore.app.dto.request.lxc.CreateLxcRequest;
import com.virtacore.app.dto.response.LxcResponse;
import com.virtacore.app.dto.response.LxcSummaryResponse;
import com.virtacore.app.dto.response.VmStatusResponse;

import java.util.List;
import java.util.UUID;

public interface LxcContainerService {

    LxcResponse create(CreateLxcRequest request, UUID userId);

    List<LxcSummaryResponse> findAll();

    LxcResponse findById(UUID id);

    LxcResponse findByVmId(Long lxcId);

    VmStatusResponse getLxcMonitor(UUID id);

    void start(UUID id);

    void stop(UUID id);

    void restart(UUID id);

    void delete(UUID id);

    void shutdown(UUID id);
}
