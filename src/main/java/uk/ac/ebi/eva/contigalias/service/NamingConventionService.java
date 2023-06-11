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
        NamingConvention convention = repository.save(namingConvention);
        return Optional.of(convention);
    }
}
