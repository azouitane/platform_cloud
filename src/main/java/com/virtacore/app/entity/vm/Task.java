package com.virtacore.app.entity.vm;

import com.virtacore.app.Enums.TaskStatus;
import com.virtacore.app.Enums.TaskType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String upid; // Proxmox task id

    @Enumerated(EnumType.STRING)
    private TaskType type; // CLONE, START, STOP, RESIZE

    @Enumerated(EnumType.STRING)
    private TaskStatus status; // PENDING, SUCCESS, FAILED

    @ManyToOne
    @JoinColumn(name = "vm_id")
    private VirtualMachine vm;
}