package uk.ac.ebi.eva.contigalias.entities;


import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Setter
@Getter
@Table(name = "AssemblySequences")
@Entity
public class AssemblySequencesEntity {

    @Id
    @Column(nullable = false)
    @ApiModelProperty(value = "Assembly's INSDC accession. It can be either a GenBank, ENA or a DDBJ accession.")
    private String insdcAccession;


    @ApiModelProperty(value = "List of all sequences of the assembly.")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(targetEntity = Sequence.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "insdcAccession", referencedColumnName = "insdcAccession")
    private List<Sequence> sequences;
}
