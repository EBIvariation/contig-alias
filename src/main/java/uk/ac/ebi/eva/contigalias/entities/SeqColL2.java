package uk.ac.ebi.eva.contigalias.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@Data
@IdClass(SeqColL2Id.class)
public class SeqColL2 extends SeqCol{

    @Id
    private String object; // The value of on of the seqCol's attribute

}
