package uk.ac.ebi.eva.contigalias.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.contigalias.entities.SeqCol2;

@Repository
public interface SeqCol2Repository extends JpaRepository<SeqCol2, String> {

}
