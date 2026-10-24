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

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "api_url", nullable = false, length = 500)
    private String apiUrl;

    @Column(name = "secret_ref", nullable = false)
    private String secretRef;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ClusterEntity() {
    }

    public ClusterEntity(String name, String apiUrl,String secretRef) {
        this.name = name;
        this.apiUrl = apiUrl;
        this.secretRef = secretRef;
        this.enabled = true;
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

    public String getName() {
        return name;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void update(String name, String apiUrl,String secretRef ,boolean enabled) {
        this.name = name;
        this.apiUrl = apiUrl;
        this.secretRef = secretRef;
        this.enabled = enabled;
    }
}
