package uk.ac.ebi.eva.contigalias.entities;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Data
@NoArgsConstructor
public class SeqColEntity {

    @ApiModelProperty(value = "The level 0 digest of the object")
    private String digest;

    @ApiModelProperty(value = "The representation level of the the object")
    @Enumerated(EnumType.ORDINAL)
    private Level level;

    @ApiModelProperty(value = "The naming convention used to construct this seqCol object")
    @Enumerated(EnumType.STRING)
    private NamingConvention namingConvention;

    @ApiModelProperty(value = "The array of the sequences' lengths")
    private List<Long> lengths;

    @ApiModelProperty(value = "The array of the sequences' names")
    private List<String> names;

    @ApiModelProperty(value = "The array of the sequences")
    private List<String> sequences;

    public enum Level {
        ZERO, ONE, TWO
    }

    public enum NamingConvention {
        ENA, GENBANK, UCSC
    }
}
