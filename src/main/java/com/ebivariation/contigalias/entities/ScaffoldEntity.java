package com.ebivariation.contigalias.entities;

import javax.persistence.*;

@Entity
@Table
public class ScaffoldEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;
    private String name;
    private String genbank;
    private String refseq;

    @ManyToOne(cascade = CascadeType.ALL)
    private AssemblyEntity assembly;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGenbank() {
        return genbank;
    }

    public String getRefseq() {
        return refseq;
    }

    public AssemblyEntity getAssembly() {
        return assembly;
    }

    public ScaffoldEntity setId(long id) {
        this.id = id;
        return this;
    }

    public ScaffoldEntity setName(String name) {
        this.name = name;
        return this;
    }

    public ScaffoldEntity setGenbank(String genbank) {
        this.genbank = genbank;
        return this;
    }

    public ScaffoldEntity setRefseq(String refseq) {
        this.refseq = refseq;
        return this;
    }

    public ScaffoldEntity setAssembly(AssemblyEntity assembly) {
        this.assembly = assembly;
        return this;
    }
}
