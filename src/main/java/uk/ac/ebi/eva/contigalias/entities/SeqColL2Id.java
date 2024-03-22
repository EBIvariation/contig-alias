package uk.ac.ebi.eva.contigalias.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@NoArgsConstructor
@Data
public class SeqColL2Id implements Serializable {
    private String digest;
    private String object;
}
