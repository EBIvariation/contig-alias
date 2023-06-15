package uk.ac.ebi.eva.contigalias.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SeqColL2 {

    @Id
    private String digest;
    private String sequences;
    private String names;
    private String lengths;
}
