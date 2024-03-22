package uk.ac.ebi.eva.contigalias.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeqColL1 {
    @Id
    private String digest;

    @Column(nullable = false)
    private String sequences;

    @Column(nullable = false)
    private String lengths;

    @Column(nullable = false)
    private String names;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NamingConvention naming_convention;

    public enum NamingConvention {
        ENA, GENBANK, USCS
    }
}
