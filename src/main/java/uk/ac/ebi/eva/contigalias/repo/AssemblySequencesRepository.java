package uk.ac.ebi.eva.contigalias.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.eva.contigalias.entities.AssemblySequencesEntity;

@Repository
public interface AssemblySequencesRepository extends JpaRepository<AssemblySequencesEntity, String> {
    Optional<AssemblySequencesEntity> findAssemblySequenceEntityByInsdcAccession(String accession);


}
