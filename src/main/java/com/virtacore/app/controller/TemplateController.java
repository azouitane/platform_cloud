package com.virtacore.app.controller;

import com.virtacore.app.dto.request.vm.TemplateRequest;
import com.virtacore.app.dto.response.TemplateResponse;
import com.virtacore.app.dto.response.TemplateSummary;
import com.virtacore.app.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {
    private final TemplateService templateService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateResponse create( @Valid
            @RequestBody TemplateRequest request
    ) {
        return templateService.create(request);
    }

    @GetMapping
    public List<TemplateResponse> findAll() {
        return templateService.findAll();
    }


    @GetMapping("summary")
    public List<TemplateSummary> findAllSummary() {
        return templateService.findAllSummary();
    }
    @GetMapping("/{id}")
    public TemplateResponse findById(
            @PathVariable UUID id
    ) {
        return templateService.findById(id);
    }

    
}
