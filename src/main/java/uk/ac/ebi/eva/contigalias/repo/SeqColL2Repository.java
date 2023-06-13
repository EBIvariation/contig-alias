package uk.ac.ebi.eva.contigalias.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.contigalias.entities.SeqColL2;
import uk.ac.ebi.eva.contigalias.entities.SeqColL2Id;

@Repository
public interface SeqColL2Repository extends JpaRepository<SeqColL2, SeqColL2Id> {

}
