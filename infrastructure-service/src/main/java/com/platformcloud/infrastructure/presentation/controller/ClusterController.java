package com.platformcloud.infrastructure.presentation.controller;

import com.platformcloud.infrastructure.application.service.ClusterService;
import com.platformcloud.infrastructure.presentation.mapper.ClusterDtoMapper;
import com.platformcloud.infrastructure.presentation.request.CreateClusterRequest;
import com.platformcloud.infrastructure.presentation.request.UpdateClusterRequest;
import com.platformcloud.infrastructure.presentation.response.ClusterResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clusters")
@RequiredArgsConstructor
public class ClusterController {

    private final ClusterService service;
    private final ClusterDtoMapper mapper;

    @PostMapping
    public ResponseEntity<ClusterResponse> create(
            @Valid @RequestBody CreateClusterRequest request
    ) {
        var cluster = service.create(mapper.toDomain(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(cluster));
    }

    @GetMapping("/{id}")
    public ClusterResponse findById(@PathVariable UUID id) {
        return mapper.toResponse(service.findById(id));
    }

    @GetMapping
    public List<ClusterResponse> findAll() {
        return mapper.toResponseList(service.findAll());
    }

    @PutMapping("/{id}")
    public ClusterResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClusterRequest request
    ) {
        return mapper.toResponse(
                service.update(mapper.toDomain(request, id))
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    record Status(
            boolean status
    ) {}

    @PostMapping("/{id}/connection")
    public Status verifyConnection(@PathVariable UUID id) {
        boolean status = service.verifyConnection(id);

        return new Status(status);
    }
}

