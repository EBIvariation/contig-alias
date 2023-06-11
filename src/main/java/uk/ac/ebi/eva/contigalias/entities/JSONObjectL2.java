package uk.ac.ebi.eva.contigalias.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JSONObjectL2 implements Serializable {
    private List<Integer> lengths; // Level 2 lengths array
    private List<String> names; // Level 2 names array
    private List<String> sequences; // Level 2 sequences array

}
