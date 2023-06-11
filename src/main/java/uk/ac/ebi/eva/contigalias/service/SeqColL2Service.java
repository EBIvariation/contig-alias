package uk.ac.ebi.eva.contigalias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.contigalias.entities.SeqColL2;
import uk.ac.ebi.eva.contigalias.repo.SeqColL2Repository;

import java.util.Optional;

@Service
public class SeqColL2Service {

    @Autowired
    private SeqColL2Repository repository;

    public Optional<SeqColL2> addSequenceCollectionL2(SeqColL2 seqColL2){
        SeqColL2 seqCol = repository.save(seqColL2);
        return Optional.of(seqCol);
    }
}
