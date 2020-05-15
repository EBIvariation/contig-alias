package com.ebivariation.contigalias.entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
public class AssemblyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;

    private String name;

    private String organism;

    private long taxid;

    private String genbank;

    private String refseq;

    @OneToMany(mappedBy = "assembly", cascade = CascadeType.ALL)
    private List<ChromosomeEntity> chromosomes;

    public AssemblyEntity() {
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOrganism() {
        return organism;
    }

    public long getTaxid() {
        return taxid;
    }

    public String getGenbank() {
        return genbank;
    }

    public String getRefseq() {
        return refseq;
    }

    public List<ChromosomeEntity> getChromosomes() {
        return chromosomes;
    }

    public AssemblyEntity setId(long id) {
        this.id = id;
        return this;
    }

    public AssemblyEntity setName(String name) {
        this.name = name;
        return this;
    }

    public AssemblyEntity setOrganism(String organism) {
        this.organism = organism;
        return this;
    }

    public AssemblyEntity setTaxid(long taxid) {
        this.taxid = taxid;
        return this;
    }

    public AssemblyEntity setGenbank(String genbank) {
        this.genbank = genbank;
        return this;
    }

    public AssemblyEntity setRefseq(String refseq) {
        this.refseq = refseq;
        return this;
    }

    public AssemblyEntity setChromosomes(List<ChromosomeEntity> scaffolds) {
        this.chromosomes = scaffolds;
        return this;
    }

}
