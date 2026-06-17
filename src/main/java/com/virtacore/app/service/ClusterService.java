package com.virtacore.app.service;

import com.virtacore.app.dto.request.vm.ClusterRequest;
import com.virtacore.app.dto.response.ClusterResponse;

import java.util.List;
import java.util.UUID;

public interface ClusterService {


    ClusterResponse create(
            ClusterRequest request
    );


    ClusterResponse findById(
            UUID id
    );


    List<ClusterResponse> findAll();


    ClusterResponse update(
            UUID id,
            ClusterRequest request
    );


    void delete(
            UUID id
    );

}