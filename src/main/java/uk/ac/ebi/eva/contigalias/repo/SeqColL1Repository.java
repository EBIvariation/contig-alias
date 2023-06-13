package uk.ac.ebi.eva.contigalias.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.contigalias.entities.SeqColL1;

@Repository
public interface SeqColL1Repository extends JpaRepository<SeqColL1, String> {

}
