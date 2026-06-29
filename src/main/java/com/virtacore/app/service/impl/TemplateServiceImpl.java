package com.virtacore.app.service.impl;

import com.virtacore.app.dto.request.vm.TemplateRequest;
import com.virtacore.app.dto.response.TemplateResponse;
import com.virtacore.app.dto.response.TemplateSummary;
import com.virtacore.app.entity.vm.Template;
import com.virtacore.app.exception.ResourceNotFoundException;
import com.virtacore.app.mapper.TemplateMapper;
import com.virtacore.app.repository.TemplateRepository;
import com.virtacore.app.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;
    @Override
    public TemplateResponse create(TemplateRequest request) {
        if (templateRepository.existsByProxmoxTemplateId(
                request.proxmoxTemplateId()
        )) {
            throw new ResourceNotFoundException(
                    "Template already exists with Proxmox Template ID: "
                            + request.proxmoxTemplateId()
            );
        }

        Template template = templateMapper.toEntity(request);

        Template savedTemplate = templateRepository.save(template);

        return templateMapper.toResponse(savedTemplate);
    }

    @Override
    public List<TemplateResponse> findAll() {
        return templateRepository.findAll()
                .stream()
                .map(templateMapper::toResponse)
                .toList();
    }

    @Override
    public List<TemplateSummary> findAllSummary() {
        return templateRepository.findAll()
                .stream().map(
                        templateMapper::toSummaryResponse
                ).toList();
    }

    @Override
    public TemplateResponse findById(UUID id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Template not found: " + id));

        return templateMapper.toResponse(template);
    }
}
