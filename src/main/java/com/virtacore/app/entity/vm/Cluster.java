package com.virtacore.app.entity.vm;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clusters")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cluster {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false, unique = true)
    private String name;


    @Column(nullable = false, unique = true)
    private String host;


    @Column(nullable = false )
    private Integer apiPort = 8006;


    @Column(nullable = false, unique = true)
    private String tokenId;


    @Column(nullable = false , unique = true)
    private String tokenSecret;


    private boolean active = true;



    @OneToMany(
            mappedBy = "cluster",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Node> nodes = new ArrayList<>();

}