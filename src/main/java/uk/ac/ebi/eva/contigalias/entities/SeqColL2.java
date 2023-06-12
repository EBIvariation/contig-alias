package uk.ac.ebi.eva.contigalias.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "sequence_collections_L2")
public class SeqColL2 extends SeqCol{

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JSONObjectL2 object;

    public SeqColL2(String digest, JSONObjectL2 jsonObjectL2){
        super(digest);
        this.object = jsonObjectL2;
    }
}
