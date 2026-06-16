package com.virtacore.app.repository;

import com.virtacore.app.entity.vm.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByVmId(UUID vmId);

    List<Task> findByStatus(String status);

    List<Task> findByType(String type);

    Task findByUpid(String upid);
}
