package com.virtacore.app.entity.lxc;

import com.virtacore.app.Enums.VmStatus;
import com.virtacore.app.entity.user.User;
import com.virtacore.app.entity.vm.Cluster;
import com.virtacore.app.entity.vm.Node;
import com.virtacore.app.entity.vm.Template;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lxc_containers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LxcContainer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // Proxmox CT ID
    @Column(nullable = false, unique = true)
    private Long lxcId;


    @Column(nullable = false)
    private String hostname;



    // CPU cores
    private Integer cores;


    // RAM MB
    private Integer memory;


    // SWAP MB
    private Integer swap;

    // Disk GB
    private Integer disk;


    // Container IP
    private String ipAddress;


    @Enumerated(EnumType.STRING)
    private VmStatus status;


    private Boolean privileged = false;


    private Boolean nesting = false;


    private LocalDateTime createdAt;

    // pve node
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "node_id")
    private Node proxmoxNode;


    // Ubuntu/Debian template
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id")
    private LxcTemplate template;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cluster_id")
    private Cluster cluster;



    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User owner;


}