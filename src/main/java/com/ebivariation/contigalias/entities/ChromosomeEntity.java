package com.ebivariation.contigalias.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GenerationType;


@Entity
@Table
public class ChromosomeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;

    private String name;

    private String genbank;

    private String refseq;

    @ManyToOne(cascade = CascadeType.ALL)
    private AssemblyEntity assembly;

    public ChromosomeEntity() {
    }

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

    public ChromosomeEntity setId(long id) {
        this.id = id;
        return this;
    }

    public ChromosomeEntity setName(String name) {
        this.name = name;
        return this;
    }

    public ChromosomeEntity setGenbank(String genbank) {
        this.genbank = genbank;
        return this;
    }

    public ChromosomeEntity setRefseq(String refseq) {
        this.refseq = refseq;
        return this;
    }

    public ChromosomeEntity setAssembly(AssemblyEntity assembly) {
        this.assembly = assembly;
        return this;
    }

    public ChromosomeEntity() {
    }
}
