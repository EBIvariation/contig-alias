package uk.ac.ebi.eva.contigalias.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Data

public class NamingConvention {

    @Id
    private String convention;


}
