package com.virtacore.app.service.impl;

import com.virtacore.app.dto.request.vm.NodeRequest;
import com.virtacore.app.dto.response.NodeResponse;
import com.virtacore.app.entity.vm.Cluster;
import com.virtacore.app.entity.vm.Node;
import com.virtacore.app.exception.ValidationException;
import com.virtacore.app.mapper.NodeMapper;
import com.virtacore.app.repository.ClusterRepository;
import com.virtacore.app.repository.NodeRepository;
import com.virtacore.app.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NodeServiceImpl
        implements NodeService {


    private final NodeRepository nodeRepository;

    private final ClusterRepository clusterRepository;

    private final NodeMapper nodeMapper;



    @Override
    public NodeResponse create(
            NodeRequest request
    ) {


        if (nodeRepository.existsByName(request.name())) {

            throw new ValidationException(
                    "Node name already exists"
            );
        }


        if (nodeRepository.existsByHost(request.host())) {

            throw new ValidationException(
                    "Node host already exists"
            );
        }



        Cluster cluster =
                clusterRepository.findById(
                                request.clusterId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Cluster not found"
                                )
                        );



        Node node =
                nodeMapper.toEntity(request);



        node.setCluster(cluster);

        node.setActive(true);



        return nodeMapper.toResponse(
                nodeRepository.save(node)
        );
    }





    @Override
    public List<NodeResponse> findByCluster(
            UUID clusterId
    ) {


        return nodeRepository
                .findByClusterId(clusterId)
                .stream()
                .map(nodeMapper::toResponse)
                .toList();

    }





    @Override
    public NodeResponse findAvailableNode(
            UUID clusterId
    ) {


        Node node =
                nodeRepository
                        .findByClusterIdAndActiveTrue(clusterId)
                        .stream()
                        .findFirst()
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "No node available"
                                )
                        );


        return nodeMapper.toResponse(node);
    }


    @Override
    public Node findAvailableNodeEntity(
            UUID clusterId
    ) {


        return nodeRepository
                .findByClusterIdAndActiveTrue(clusterId)
                .stream()
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException(
                                "No node available"
                        )
                );

    }

    @Override
    public void delete(UUID id) {

        nodeRepository.deleteById(id);

    }

}