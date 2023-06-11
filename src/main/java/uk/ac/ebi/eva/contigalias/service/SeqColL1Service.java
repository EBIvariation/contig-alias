package uk.ac.ebi.eva.contigalias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.contigalias.entities.SeqColL1;
import uk.ac.ebi.eva.contigalias.repo.SeqColL1Repository;

import java.util.Optional;

@Service
public class SeqColL1Service {

    @Autowired
    private SeqColL1Repository repository;

    /**
     * Add a new Level 1 sequence collection object and save it to the
     * database*/
    public Optional<SeqColL1> addSequenceCollectionL1(SeqColL1 seqColL1){
        SeqColL1 seqCol = repository.save(seqColL1);
        return Optional.of(seqCol);
    }
}
