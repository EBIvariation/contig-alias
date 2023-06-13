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
            SeqColL1 seqColL11 = repository.save(seqColL1);
            return Optional.of(seqColL11);
        } catch (Exception e){
            System.out.println("SeqColL1 with digest: " + seqColL1.getDigest() + " already exists !");
        }
        return Optional.empty();
    }

    public List<SeqColL1> getAllSequenceCollections(){
        return repository.findAll();
    }
}
