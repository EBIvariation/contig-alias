package uk.ac.ebi.eva.contigalias.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Data
@Table(name = "sequence_collections_L1")
public class SeqColL1 extends SeqCol{

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JSONObjectL1 object;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NamingConvention namingConvention;

   public SeqColL1(String digest, JSONObjectL1 jsonObjectL1, NamingConvention namingConvention){
       super(digest);
       this.object = jsonObjectL1;
       this.namingConvention = namingConvention;
   }

}
