package com.virtacore.app.service.impl;

import com.virtacore.app.dto.request.lxc.LxcTemplateRequest;
import com.virtacore.app.dto.response.LxcTemplateResponse;
import com.virtacore.app.entity.lxc.LxcTemplate;
import com.virtacore.app.exception.ResourceNotFoundException;
import com.virtacore.app.exception.ValidationException;
import com.virtacore.app.mapper.LxcTemplateMapper;
import com.virtacore.app.repository.LxcTemplateRepository;
import com.virtacore.app.service.LxcTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LxcTemplateServiceImpl implements LxcTemplateService {
    private final LxcTemplateRepository lxcTemplateRepository;
    private final LxcTemplateMapper lxcTemplateMapper;
    @Override
    public LxcTemplateResponse create(LxcTemplateRequest request) {
        if (lxcTemplateRepository.existsByOstemplate(request.ostemplate())) {
            throw new ValidationException(
                    "Template already exists: "
            );
        }

        LxcTemplate template = lxcTemplateMapper.toEntity(request);

        return lxcTemplateMapper.toResponse(lxcTemplateRepository.save(template));
    }

    @Override
    public List<LxcTemplateResponse> findAll() {
        return lxcTemplateRepository.findAll()
                .stream()
                .map(lxcTemplateMapper::toResponse)
                .toList();
    }

    @Override
    public LxcTemplateResponse findById(UUID id) {
        LxcTemplate template = lxcTemplateRepository.findById(id)
                .orElseThrow(
                        () -> new  ResourceNotFoundException("LXC Template not found")
                );
        return lxcTemplateMapper.toResponse(template);
    }
}
