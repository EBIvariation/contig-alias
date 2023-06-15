package uk.ac.ebi.eva.contigalias.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.contigalias.entities.SeqColL2;

@Repository
public interface SeqColL2Repository extends JpaRepository<SeqColL2, String> {

}
