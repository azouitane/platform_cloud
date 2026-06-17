package com.virtacore.app.mapper;

import com.virtacore.app.dto.request.vm.ClusterRequest;
import com.virtacore.app.dto.response.ClusterResponse;
import com.virtacore.app.entity.vm.Cluster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClusterMapper {

    Cluster toEntity(ClusterRequest request);


    ClusterResponse toResponse(Cluster cluster);


    void update(
            ClusterRequest request,
            @MappingTarget Cluster cluster
    );

}