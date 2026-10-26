package com.platformcloud.infrastructure.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clusters")
public class ClusterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String host;

    @Column(nullable = false)
    private Integer port = 8006;

    @Column(name = "secret_ref", nullable = false, length = 255)
    private String secretRef;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected ClusterEntity() {
    }

    public ClusterEntity(
            String name,
            String host,
            Integer port,
            String secretRef
    ) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.secretRef = secretRef;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void update(
            String name,
            String host,
            String secretRef,
            boolean enabled
    ) {
        this.name = name;
        this.host = host;
        this.secretRef = secretRef;
        this.enabled = enabled;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getSecretRef() {
        return secretRef;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}