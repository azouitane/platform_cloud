package com.virtacore.app.mapper;

import com.virtacore.app.dto.response.VirtualMachineResponse;
import com.virtacore.app.dto.response.VirtualMachineSummaryResponse;
import com.virtacore.app.entity.vm.VirtualMachine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VirtualMachineMapper {

    @Mapping(
            source = "cluster.name",
            target = "clusterName"
    )

    @Mapping(
            source = "template.os",
            target = "os"
    )

    @Mapping(
            source = "template.version",
            target = "version"
    )
    VirtualMachineResponse
    toResponse(VirtualMachine vm);


    @Mapping(
            source = "template.name",
            target = "nameOS"
    )
    VirtualMachineSummaryResponse
    toSummaryResponse(VirtualMachine vm);


}
