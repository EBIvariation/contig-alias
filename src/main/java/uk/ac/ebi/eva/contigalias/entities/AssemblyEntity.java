/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.contigalias.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "assembly")
public class AssemblyEntity {

    @Id
    @Column(nullable = false)
    @ApiModelProperty(value = "Assembly's INSDC accession. It can be either a GenBank, ENA or a DDBJ accession.")
    private String insdcAccession;

    @Column(nullable = false)
    @ApiModelProperty(value = "The name of the assembly.")
    private String name;

    @ApiModelProperty(value = "The organism of the assembly.")
    private String organism;

    @Column(nullable = false)
    @ApiModelProperty(value = "Assembly's taxonomic ID.")
    private Long taxid;

    @ApiModelProperty(value = "Assembly's Refseq accession.")
    private String refseq;

    @ApiModelProperty(value = "Are assembly's INSDC and Refseq accessions identical")
    private boolean isGenbankRefseqIdentical;

    @ApiModelProperty(value = "Assembly's MD5 checksum value.")
    private String md5checksum;

    @ApiModelProperty(value = "Assembly's TRUNC512 checksum value.")
    private String trunc512checksum;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty(value = "List of all chromosomes of the assembly present in the database.")
    @LazyCollection(LazyCollectionOption.TRUE)
    @OneToMany(mappedBy = "assembly", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<ChromosomeEntity> chromosomes;

    public AssemblyEntity() {
    }

    public String getName() {
        return name;
    }

    public AssemblyEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getOrganism() {
        return organism;
    }

    public AssemblyEntity setOrganism(String organism) {
        this.organism = organism;
        return this;
    }

    public Long getTaxid() {
        return taxid;
    }

    public AssemblyEntity setTaxid(Long taxid) {
        this.taxid = taxid;
        return this;
    }

    public String getInsdcAccession() {
        return insdcAccession;
    }

    public AssemblyEntity setInsdcAccession(String insdcAccession) {
        this.insdcAccession = insdcAccession;
        return this;
    }

    public String getRefseq() {
        return refseq;
    }

    public AssemblyEntity setRefseq(String refseq) {
        this.refseq = refseq;
        return this;
    }

    public boolean isGenbankRefseqIdentical() {
        return isGenbankRefseqIdentical;
    }

    public AssemblyEntity setGenbankRefseqIdentical(boolean genbankRefseqIdentical) {
        isGenbankRefseqIdentical = genbankRefseqIdentical;
        return this;
    }

    public String getMd5checksum() {
        return md5checksum;
    }

    public AssemblyEntity setMd5checksum(String md5checksum) {
        this.md5checksum = md5checksum;
        return this;
    }

    public String getTrunc512checksum() {
        return trunc512checksum;
    }

    public AssemblyEntity setTrunc512checksum(String trunc512checksum) {
        this.trunc512checksum = trunc512checksum;
        return this;
    }

    public List<ChromosomeEntity> getChromosomes() {
        return chromosomes;
    }

    public AssemblyEntity setChromosomes(List<ChromosomeEntity> chromosomes) {
        this.chromosomes = chromosomes;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name :\t")
               .append(this.name)
               .append("\n")
               .append("Organism :\t")
               .append(this.organism)
               .append("\n")
               .append("Tax ID :\t")
               .append(this.taxid)
               .append("\n")
               .append("INSDC :\t")
               .append(this.insdcAccession)
               .append("\n")
               .append("Refseq :\t")
               .append(this.refseq)
               .append("\n")
               .append("INSDC & Refseq identical :\t")
               .append(isGenbankRefseqIdentical)
               .append("\n")
               .append("md5checksum :\t")
               .append(this.md5checksum)
               .append("\n")
               .append("trunc512checksum :\t")
               .append(this.trunc512checksum)
               .append("\n");
        return builder.toString();
    }
}
