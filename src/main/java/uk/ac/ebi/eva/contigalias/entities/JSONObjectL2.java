package uk.ac.ebi.eva.contigalias.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class JSONObjectL2 implements Serializable {
    private List<String> object; // Level 2 lengths array

    public JSONObjectL2(List<String> object){
        this.object = object;
    }
}
