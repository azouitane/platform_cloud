package com.virtacore.app.entity.vm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "nodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Node {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;



    @Column(nullable = false)
    private String name;


    @Column(nullable = false, unique = true)
    private String host;



    private boolean active = true;

    @OneToMany(
            mappedBy = "node",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<VirtualMachine> virtualMachines = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cluster_id",
            nullable = false
    )
    private Cluster cluster;
}
