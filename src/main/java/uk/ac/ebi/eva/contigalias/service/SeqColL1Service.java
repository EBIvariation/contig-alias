package uk.ac.ebi.eva.contigalias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.SeqColL1;
import uk.ac.ebi.eva.contigalias.repo.SeqColL1Repository;

import java.util.List;
import java.util.Optional;

@Service
public class SeqColL1Service {

    @Autowired
    private SeqColL1Repository repository;


    public Optional<SeqColL1> addSeqColL1(SeqColL1 seqColL1){
        try {
            repository.save(seqColL1);
            return Optional.of(seqColL1);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public List<SeqColL1> getAll(){
        return repository.findAll();
    }
}
