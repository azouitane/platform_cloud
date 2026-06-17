package com.virtacore.app.controller;

import com.virtacore.app.dto.request.vm.ClusterRequest;
import com.virtacore.app.dto.response.ClusterResponse;
import com.virtacore.app.service.ClusterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clusters")
@RequiredArgsConstructor
public class ClusterController {


    private final ClusterService clusterService;



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
    public ResponseEntity<List<ClusterResponse>> findAll(){

        return ResponseEntity.ok(
                clusterService.findAll()
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





    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ){

        clusterService.delete(id);

        return ResponseEntity.noContent()
                .build();
    }

}