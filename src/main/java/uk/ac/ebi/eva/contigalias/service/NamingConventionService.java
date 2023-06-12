package uk.ac.ebi.eva.contigalias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.contigalias.entities.NamingConvention;
import uk.ac.ebi.eva.contigalias.repo.NamingConventionRepository;

import java.util.Optional;

@Service
public class NamingConventionService {

    @Autowired
    private NamingConventionRepository repository;


    public Optional<NamingConvention> addNamingConvention(NamingConvention namingConvention){
        NamingConvention convention = null;
        try {
            convention = repository.save(namingConvention);
            return Optional.of(convention);
        } catch (Exception e){
            // TODO: THROW AN EXCEPTIONS CLASS
            System.out.println("Seqcol with digest "+ namingConvention.getSeqColL1().getDigest() +" already exist");
        }
        return Optional.empty();
    }
}
