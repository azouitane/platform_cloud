package com.virtacore.app.controller;

import com.virtacore.app.dto.request.vm.ClusterRequest;
import com.virtacore.app.dto.response.ClusterResponse;
import com.virtacore.app.dto.response.ClusterSummary;
import com.virtacore.app.service.ClusterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clusters")
@RequiredArgsConstructor
public class ClusterController {


    private final ClusterService clusterService;



    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ClusterResponse> create(@Valid
            @RequestBody ClusterRequest request
    ){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        clusterService.create(request)
                );
    }




    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<ClusterResponse>> findAll(){

        return ResponseEntity.ok(
                clusterService.findAll()
        );
    }

    @GetMapping("summary")
    public ResponseEntity<List<ClusterSummary>> findAllSummary(){

        return ResponseEntity.ok(
                clusterService.findAllSummary()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<ClusterResponse> findById(
            @PathVariable UUID id
    ){

        return ResponseEntity.ok(
                clusterService.findById(id)
        );
    }




    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ClusterResponse> update(
            @PathVariable UUID id,
            @RequestBody ClusterRequest request
    ){

        return ResponseEntity.ok(
                clusterService.update(
                        id,
                        request
                )
        );
    }




    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ){

        clusterService.delete(id);

        return ResponseEntity.noContent()
                .build();
    }

}