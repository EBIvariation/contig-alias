package uk.ac.ebi.eva.contigalias.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JSONObjectL1 implements Serializable {
    private String sequences;
    private String names;
    private String lengths;
}
















