package uk.ac.ebi.eva.contigalias.entities;


import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Data
@Table(name = "assemblySequences")
@Entity
public class AssemblySequencesEntity {

    @Id
    @Column(nullable = false)
    @ApiModelProperty(value = "Assembly's INSDC accession. It can be either a GenBank, ENA or a DDBJ accession.")
    private String assemblyInsdcAccession;


    @ApiModelProperty(value = "List of all sequences of the assembly.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(targetEntity = Sequence.class, cascade = CascadeType.ALL)
    //@OneToMany(mappedBy = "assemblySequences", cascade = CascadeType.ALL)
    //@JoinColumn(name = "assembly_insdc_accession", referencedColumnName = "assemblyInsdcAccession")
    @JoinColumn(name = "assembly_insdc_accession")
    private List<Sequence> sequences;
}
