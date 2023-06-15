package uk.ac.ebi.eva.contigalias.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class SeqColL1 {

    @Id
    private String digest;

    private String assemblyInsdcAccession;
    private String sequences;
    private String names;
    private String lengths;
}
