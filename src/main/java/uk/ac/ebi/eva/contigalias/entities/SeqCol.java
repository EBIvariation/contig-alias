package uk.ac.ebi.eva.contigalias.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.util.List;

@MappedSuperclass
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public abstract class SeqCol {

    @Id
    @Column (name = "digest")
    protected String digest; // The level 0 digest

    /*@Transient
    protected NamingConvention.Convention namingConvention;*/
}
