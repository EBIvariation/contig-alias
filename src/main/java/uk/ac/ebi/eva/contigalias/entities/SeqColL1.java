package uk.ac.ebi.eva.contigalias.entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Data
public class SeqColL1 extends SeqCol{

    @Column(nullable = false)
    private String sequences;

    @Column(nullable = false)
    private String lengths;

    @Column(nullable = false)
    private String names;

    @Column(nullable = false)
    private String naming_convention;
}
