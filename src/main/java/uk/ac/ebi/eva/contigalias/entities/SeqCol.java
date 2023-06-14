package uk.ac.ebi.eva.contigalias.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import net.minidev.json.JSONObject;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Entity
@Data
public class SeqCol {

    @Id
    @Column(nullable = false)
    private String digest;

    @Type(type = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private JSONObject object;
}
