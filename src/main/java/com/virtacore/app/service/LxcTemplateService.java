package com.virtacore.app.service;

import com.virtacore.app.dto.request.lxc.LxcTemplateRequest;
import com.virtacore.app.dto.response.LxcTemplateResponse;

import java.util.List;
import java.util.UUID;

public interface LxcTemplateService {
    LxcTemplateResponse create(LxcTemplateRequest request);

    List<LxcTemplateResponse> findAll();

    LxcTemplateResponse findById(UUID id);

}
