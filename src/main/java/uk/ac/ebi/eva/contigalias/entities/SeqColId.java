package uk.ac.ebi.eva.contigalias.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@Data
@NoArgsConstructor
public class SeqColId implements Serializable {
    private String digest;
    private Integer level;
}
