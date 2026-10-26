package com.platformcloud.infrastructure.presentation.controller;

import com.platformcloud.infrastructure.application.service.NodeService;
import com.platformcloud.infrastructure.domain.exception.ValidationException;
import com.platformcloud.infrastructure.domain.model.Node;
import com.platformcloud.infrastructure.presentation.mapper.NodeDtoMapper;
import com.platformcloud.infrastructure.presentation.request.CreateNodeRequest;
import com.platformcloud.infrastructure.presentation.request.UpdateNodeStatusRequest;
import com.platformcloud.infrastructure.presentation.response.NodeResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clusters/{clusterId}/nodes")
@RequiredArgsConstructor
public class NodeController {

    private final NodeService service;
    private final NodeDtoMapper mapper;

    @PostMapping
    public ResponseEntity<NodeResponse> create(
            @PathVariable UUID clusterId,
            @Valid @RequestBody CreateNodeRequest request
    ) {
        var node = service.create(
                mapper.toDomain(request, clusterId)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(node));
    }

    @GetMapping
    public List<NodeResponse> findAll(
            @PathVariable UUID clusterId
    ) {
        return mapper.toResponseList(
                service.findByClusterId(clusterId)
        );
    }

    @GetMapping("/{nodeId}")
    public NodeResponse findById(
            @PathVariable UUID clusterId,
            @PathVariable UUID nodeId
    ) {
        var node = service.findById(nodeId);

        if (!node.clusterId().equals(clusterId)) {
            throw new ValidationException(
                    "Node does not belong to this cluster"
            );
        }

        return mapper.toResponse(node);
    }

    @PatchMapping("/{nodeId}/status")
    public NodeResponse updateStatus(
            @PathVariable UUID clusterId,
            @PathVariable UUID nodeId,
            @Valid @RequestBody UpdateNodeStatusRequest request
    ) {
        Node updated = service.updateStatus(
                clusterId,
                nodeId,
                request.status()
        );

        return mapper.toResponse(updated);
    }

    @DeleteMapping("/{nodeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID clusterId,
            @PathVariable UUID nodeId
    ) {
        var node = service.findById(nodeId);

        if (!node.clusterId().equals(clusterId)) {
            throw new ValidationException(
                    "Node does not belong to this cluster"
            );
        }

        service.delete(nodeId);
    }
}