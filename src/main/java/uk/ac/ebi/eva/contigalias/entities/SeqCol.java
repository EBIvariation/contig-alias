package uk.ac.ebi.eva.contigalias.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;

@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(value = SeqColId.class)
public class SeqCol {

    @Id
    @Column(nullable = false)
    private String digest;

    @Id
    private Integer level;

    @Type(type = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "jsonb")
    private JSONObject object;

    public enum NamingConvention {
        ENA, NCBI, USCS
    }
}
