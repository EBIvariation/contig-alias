package uk.ac.ebi.eva.contigalias.datasource;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import uk.ac.ebi.eva.contigalias.entities.AssemblySequencesEntity;

public interface AssemblySequencesDataSource {

    Optional<AssemblySequencesEntity> getAssemblySequencesByAccession(String accession) throws IOException, NoSuchAlgorithmException;

}
