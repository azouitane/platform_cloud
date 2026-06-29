package com.virtacore.app.entity;

import jakarta.persistence.Column.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lxc_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LxcTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // Display name
    @Column(nullable = false)
    private String name;


    // Proxmox volid
    // local:vztmpl/ubuntu-22.04-standard_22.04-1_amd64.tar.zst
    @Column(nullable = false, unique = true)
    private String ostemplate;


    // ubuntu, debian, alpine...
    private String distribution;


    // version
    private String version;


    // tar.zst / tar.xz / tar.gz
    private String format;


    // Size in bytes
    private Long size;


    // Storage name: local
    private String storage;


    // Is downloaded on Proxmox
    private Boolean available = true;
}