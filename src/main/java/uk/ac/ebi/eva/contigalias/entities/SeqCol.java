package uk.ac.ebi.eva.contigalias.entities;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Data
public abstract class SeqCol {
    @Id
    private String digest;
}
