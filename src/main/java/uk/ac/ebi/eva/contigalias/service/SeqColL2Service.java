package uk.ac.ebi.eva.contigalias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.SeqColL2;
import uk.ac.ebi.eva.contigalias.repo.SeqColL2Repository;

import java.util.List;
import java.util.Optional;

@Service
public class SeqColL2Service {

    @Autowired
    private SeqColL2Repository repository;


    public Optional<SeqColL2> addSeqColL2(SeqColL2 seqColL2){
        try {
            System.out.println("CHECK POINT: " + seqColL2.getDigest() + " | " + seqColL2.getObject());
            SeqColL2 seqColL21 = repository.save(seqColL2);
            return Optional.of(seqColL21);
        } catch (Exception e){
            System.out.println("Problem when inserting seqCol object: " + seqColL2.getDigest() + " already exists !");
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<SeqColL2> getAllSequenceCollections(){
        return repository.findAll();
    }
}
