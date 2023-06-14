package uk.ac.ebi.eva.contigalias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.SeqCol;
import uk.ac.ebi.eva.contigalias.repo.SeqCol2Repository;

import java.util.List;
import java.util.Optional;

@Service
public class SeqCol2Service {

    @Autowired
    private SeqCol2Repository seqCol2Repository;


    public Optional<SeqCol> addSeqCol(SeqCol seqCol2){
        try {
            SeqCol seqCol = seqCol2Repository.save(seqCol2);
            return Optional.of(seqCol);
        } catch (Exception e){
            System.out.println("Sequence collection object with digest: " + seqCol2.getDigest()
            + " already exists in the DB.");
        }
        return Optional.empty();
    }

    public List<SeqCol> getAll(){
        return seqCol2Repository.findAll();
    }
}
