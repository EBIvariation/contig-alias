package uk.ac.ebi.eva.contigalias.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table(name = "AssemblySequence")
@Entity
public class AssemblySequenceEntity {

    @Id
    @Column(nullable = false)
    private String accession;

    @Column(nullable = false)
    private String name;
}
