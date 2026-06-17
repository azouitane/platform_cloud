package com.virtacore.app.service;

import com.virtacore.app.dto.request.vm.NodeRequest;
import com.virtacore.app.dto.response.NodeResponse;
import com.virtacore.app.entity.vm.Node;

import java.util.List;
import java.util.UUID;

public interface NodeService {


    NodeResponse create(
            NodeRequest request
    );


    List<NodeResponse> findByCluster(
            UUID clusterId
    );


    NodeResponse findAvailableNode(
            UUID clusterId
    );


    void delete(
            UUID id
    );

    Node findAvailableNodeEntity(
            UUID clusterId
    );

}