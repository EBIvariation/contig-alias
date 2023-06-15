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


    public Optional<SeqColL2> addSeqColL2(SeqColL2 seqColL2){
        try {
            repository.save(seqColL2);
            return Optional.of(seqColL2);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
