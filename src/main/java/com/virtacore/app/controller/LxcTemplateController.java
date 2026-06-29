package com.virtacore.app.controller;

import com.virtacore.app.dto.request.lxc.LxcTemplateRequest;
import com.virtacore.app.dto.response.LxcTemplateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.virtacore.app.service.LxcTemplateService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lxc-templates")
@RequiredArgsConstructor
public class LxcTemplateController {
    private final LxcTemplateService lxcTemplateService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LxcTemplateResponse create(
            @Valid @RequestBody LxcTemplateRequest request
    ) {
        return lxcTemplateService.create(request);
    }

    @GetMapping
    public List<LxcTemplateResponse> findAll() {
        return lxcTemplateService.findAll();
    }

    @GetMapping("/{id}")
    public LxcTemplateResponse findById(
            @PathVariable UUID id
    ) {
        return lxcTemplateService.findById(id);
    }
}
