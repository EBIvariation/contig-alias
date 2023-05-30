package uk.ac.ebi.eva.contigalias.datasource;

import java.io.IOException;
import java.util.Optional;

import uk.ac.ebi.eva.contigalias.entities.AssemblySequenceEntity;

public interface AssemblySequenceDataSource {

    Optional<AssemblySequenceEntity> getAssemblySequenceByAccession(String accession) throws IOException;

}
