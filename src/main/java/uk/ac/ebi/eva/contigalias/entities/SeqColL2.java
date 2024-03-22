package uk.ac.ebi.eva.contigalias.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(SeqColL2Id.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SeqColL2 {
    @Id
    @Column(nullable = false)
    private String digest;

    @Id
    @Column(nullable = false)
    private String object; // The value of on of the seqCol's attribute

}
