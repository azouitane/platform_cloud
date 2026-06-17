package com.virtacore.app.controller;


import com.virtacore.app.dto.request.vm.NodeRequest;
import com.virtacore.app.dto.response.NodeResponse;
import com.virtacore.app.service.NodeService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;



@RestController
@RequestMapping("/api/v1/nodes")
@RequiredArgsConstructor
public class NodeController {


    private final NodeService nodeService;




    @PostMapping
    public ResponseEntity<NodeResponse> create(
            @RequestBody NodeRequest request
    ){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        nodeService.create(request)
                );
    }






    @GetMapping("/cluster/{clusterId}")
    public ResponseEntity<List<NodeResponse>> findByCluster(
            @PathVariable UUID clusterId
    ){

        return ResponseEntity.ok(
                nodeService.findByCluster(clusterId)
        );
    }







    @GetMapping("/available/{clusterId}")
    public ResponseEntity<NodeResponse> availableNode(
            @PathVariable UUID clusterId
    ){

        return ResponseEntity.ok(
                nodeService.findAvailableNode(clusterId)
        );
    }







    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ){

        nodeService.delete(id);

        return ResponseEntity.noContent()
                .build();
    }

}