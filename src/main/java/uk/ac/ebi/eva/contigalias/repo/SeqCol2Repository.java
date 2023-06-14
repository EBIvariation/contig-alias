package uk.ac.ebi.eva.contigalias.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.contigalias.entities.SeqCol;

@Repository
public interface SeqCol2Repository extends JpaRepository<SeqCol, String> {

}
