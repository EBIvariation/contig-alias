package uk.ac.ebi.eva.contigalias.entities;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "Sequence")
public class Sequence {


    @Id
    @Column(nullable = false)
    @ApiModelProperty(value = "Assembly's Refseq accession.")
    private String sequenceRefseq;

    @Column(nullable = false)
    @ApiModelProperty(value = "Sequence's MD5 checksum value.")
    private String sequenceMD5;

    /*@JsonInclude(JsonInclude.Include.NON_NULL)
    @ManyToOne
    @JoinColumn(name = "assembly_insdc_accession", nullable = false)
    private AssemblySequencesEntity assemblySequences;*/

}
