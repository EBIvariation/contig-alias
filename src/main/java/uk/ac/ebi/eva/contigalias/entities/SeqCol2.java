package uk.ac.ebi.eva.contigalias.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.ToString;
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
public class SeqCol2 {
    @Id
    private String digest; // Can be of any level

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JSONObject object; // Can be any level object

    /**
     * These are the possible keys in a seqCol JSON entity
     **/
    public enum Attribute {
        names, lengths, sequences, convention
    }

    // TODO: NOTE: CAN BE PLACED IN A SEPARATED CLASS
    public enum NamingConvention{
        ENA, NCBI, USCS
    }
}
