package uk.ac.ebi.eva.contigalias.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@ToString
@Table(name = "naming_conventions")
public class NamingConvention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Convention convention;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "digest", referencedColumnName = "digest")
    private SeqColL1 seqColL1;

    /*@OneToOne
    @JoinColumn(name = "digest")
    private SeqColL1 seqCols;*/

    public enum Convention {
        ENA, GENBANK
    }

    public NamingConvention(Convention convention, SeqColL1 seqColL1){
        this.convention = convention;
        this.seqColL1 = seqColL1;
    }
}
