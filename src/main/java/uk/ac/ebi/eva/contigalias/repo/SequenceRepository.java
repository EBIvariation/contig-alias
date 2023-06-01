package uk.ac.ebi.eva.contigalias.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.eva.contigalias.entities.Sequence;

public interface SequenceRepository extends JpaRepository<Sequence, String> {
}
