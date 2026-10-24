package com.platformcloud.infrastructure.infrastructure.persistence.entity;


import com.platformcloud.infrastructure.domain.enums.NodeStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "nodes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_node_name_per_cluster",
                        columnNames = {"cluster_id", "name"}
                )
        },
        indexes = {
                @Index(name = "idx_nodes_cluster_id", columnList = "cluster_id")
        }
)
public class NodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cluster_id", nullable = false)
    private ClusterEntity cluster;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NodeStatus status = NodeStatus.UNKNOWN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected NodeEntity() {
    }

    public NodeEntity(ClusterEntity cluster, String name) {
        this.cluster = cluster;
        this.name = name;
        this.status = NodeStatus.UNKNOWN;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public ClusterEntity getCluster() {
        return cluster;
    }

    public String getName() {
        return name;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void updateStatus(NodeStatus status) {
        this.status = status;
    }
}
