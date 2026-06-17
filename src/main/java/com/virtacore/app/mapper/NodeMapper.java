package com.virtacore.app.mapper;


import com.virtacore.app.dto.request.vm.NodeRequest;
import com.virtacore.app.dto.response.NodeResponse;
import com.virtacore.app.entity.vm.Node;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NodeMapper {


    @Mapping(
            source = "cluster.id",
            target = "clusterId"
    )
    NodeResponse toResponse(Node node);

    Node toEntity(NodeRequest request);

}