package com.virtacore.app.service;

import com.virtacore.app.dto.request.vm.TemplateRequest;
import com.virtacore.app.dto.response.TemplateResponse;
import com.virtacore.app.dto.response.TemplateSummary;

import java.util.List;
import java.util.UUID;

public interface TemplateService {

    TemplateResponse create(TemplateRequest request);

    List<TemplateResponse> findAll();
    List<TemplateSummary> findAllSummary();

    TemplateResponse findById(UUID id);
}
