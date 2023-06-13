package uk.ac.ebi.eva.contigalias.entities;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SeqCol {
    private String digest;
}
