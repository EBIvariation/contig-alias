package uk.ac.ebi.eva.contigalias.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
    private String refseq;

    @Column
    @ApiModelProperty(value = "Sequence's MD5 checksum value.")
    private String sequenceMD5;



}
