package uk.ac.ebi.eva.contigalias.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.eva.contigalias.entities.AssemblySequenceEntity;

@Repository
public interface AssemblySequenceRepository extends JpaRepository<AssemblySequenceEntity, Long> {
    Optional<AssemblySequenceEntity> findAssemblySequenceEntityByAccession(String accession);


}
