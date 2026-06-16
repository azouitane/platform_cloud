package com.virtacore.app.repository;


import com.virtacore.app.Enums.VmStatus;
import com.virtacore.app.entity.vm.VirtualMachine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VirtualMachineRepository extends JpaRepository<VirtualMachine, UUID> {
    List<VirtualMachine> findByOwnerId(UUID ownerId);

    Optional<VirtualMachine> findByVmId(Long vmId);

    List<VirtualMachine> findByStatus(VmStatus status);
}
