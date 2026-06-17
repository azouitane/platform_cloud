package com.virtacore.app.entity.vm;

import com.virtacore.app.Enums.VmStatus;
import com.virtacore.app.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "virtual_machines")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VirtualMachine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // Proxmox VMID (important)
    @Column(nullable = false, unique = true)
    private Long vmId;

    @Column(nullable = false)
    private String name;

    private String node;

    private Integer cpu;

    private Integer memory;

    private Integer disk;

    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private VmStatus status;

    @CreatedDate
    private LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cluster_id")
    private Cluster cluster;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id")
    private Node proxmoxNode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id")
    private Template template;

    @ManyToOne(
            fetch = FetchType.LAZY, optional = false
    )
    @JoinColumn(name = "user_id")
    private User owner;
}
