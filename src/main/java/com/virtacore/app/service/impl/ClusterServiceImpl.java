package com.virtacore.app.service.impl;

import com.virtacore.app.dto.request.vm.ClusterRequest;
import com.virtacore.app.dto.response.ClusterResponse;
import com.virtacore.app.dto.response.ClusterSummary;
import com.virtacore.app.entity.vm.Cluster;
import com.virtacore.app.exception.ValidationException;
import com.virtacore.app.mapper.ClusterMapper;
import com.virtacore.app.repository.ClusterRepository;
import com.virtacore.app.service.ClusterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClusterServiceImpl implements ClusterService {


    private final ClusterRepository clusterRepository;

    private final ClusterMapper clusterMapper;



    @Override
    public ClusterResponse create(
            ClusterRequest request
    ) {


        if (clusterRepository.existsByName(request.name())) {

            throw new ValidationException(
                    "Cluster name already exists"
            );
        }


        if (clusterRepository.existsByHost(request.host())) {

            throw new ValidationException(
                    "Cluster host already exists"
            );
        }


        Cluster cluster =
                clusterMapper.toEntity(request);


        cluster.setActive(true);


        return clusterMapper.toResponse(
                clusterRepository.save(cluster)
        );
    }




    @Override
    public ClusterResponse findById(
            UUID id
    ) {


        Cluster cluster =
                clusterRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Cluster not found"
                                )
                        );


        return clusterMapper.toResponse(cluster);
    }




    @Override
    public List<ClusterResponse> findAll() {


        return clusterRepository
                .findAll()
                .stream()
                .map(clusterMapper::toResponse)
                .toList();

    }

    @Override
    public List<ClusterSummary> findAllSummary() {

        return clusterRepository
                .findAll()
                .stream()
                .map(clusterMapper::toSummaryResponse)
                .toList();

    }

    @Override
    public ClusterResponse update(
            UUID id,
            ClusterRequest request
    ) {



        if (clusterRepository.existsByName(request.name())) {

            throw new ValidationException(
                    "Cluster name already exists"
            );
        }


        if (clusterRepository.existsByHost(request.host())) {

            throw new ValidationException(
                    "Cluster host already exists"
            );
        }

        Cluster cluster =
                clusterRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Cluster not found"
                                )
                        );


        clusterMapper.update(
                request,
                cluster
        );


        return clusterMapper.toResponse(
                clusterRepository.save(cluster)
        );
    }




    @Override
    public void delete(UUID id) {

        clusterRepository.deleteById(id);

    }

}