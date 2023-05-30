package uk.ac.ebi.eva.contigalias.service;

import java.io.IOException;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.contigalias.datasource.NCBIAssemblySequenceDataSource;
import uk.ac.ebi.eva.contigalias.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.contigalias.exception.AssemblySequenceNotFoundException;
import uk.ac.ebi.eva.contigalias.exception.DuplicateAssemblySequenceException;
import uk.ac.ebi.eva.contigalias.repo.AssemblySequenceRepository;

@Service
public class AssemblySequenceService {

    private final AssemblySequenceRepository repository;

    private final NCBIAssemblySequenceDataSource ncbiSequenceDataSource;

    private final Logger logger = LoggerFactory.getLogger(AssemblyService.class);


    public AssemblySequenceService(
            AssemblySequenceRepository repository, NCBIAssemblySequenceDataSource ncbiSequenceDataSource){
        this.repository = repository;
        this.ncbiSequenceDataSource = ncbiSequenceDataSource;
    }

    public void fetchAndInsertAssemblySequence(String accession) throws IOException {
        Optional<AssemblySequenceEntity> entity = repository.findAssemblySequenceEntityByAccession(accession);
        if(entity.isPresent())
            throw duplicateAssemblySequenceInsertionException(accession, entity.get());
        Optional<AssemblySequenceEntity> fetchAssembly = ncbiSequenceDataSource.getAssemblySequenceByAccession(accession);
        if(!fetchAssembly.isPresent()){
            throw new AssemblySequenceNotFoundException(accession);
        }
        if (fetchAssembly.get().getName() != null){ // This condition is only for testing, it'll change as soon as we add more attributes to the entity
            insertAssemblySequence(fetchAssembly.get());
            logger.info("Successfully inserted assembly for accession " + accession);
        }else {
            logger.error("Skipping inserting assembly sequence : No name in assembly : " + accession);
        }
    }

    @Transactional
    public void insertAssemblySequence(AssemblySequenceEntity entity) {
        if (isEntityPresent(entity)) {
            throw duplicateAssemblySequenceInsertionException(null, entity);
        } else {
            repository.save(entity);
        }
    }

    private boolean isEntityPresent(AssemblySequenceEntity entity) {
        // TODO: THE CONDITIONS IN THIS METHOD WILL BE CHANGED WHEN WE ADD MORE ATTRIBUTES TO THE ENTITY
        Optional<AssemblySequenceEntity> existingAssembly = repository.findAssemblySequenceEntityByAccession(entity.getAccession());
        return existingAssembly.isPresent();
    }

    private DuplicateAssemblySequenceException duplicateAssemblySequenceInsertionException(String accession, AssemblySequenceEntity present) {
        StringBuilder exception = new StringBuilder("A similar assembly Sequence already exists");
        if (accession != null){
            exception.append("\n");
            exception.append("Assembly Sequence trying to insert:");
            exception.append("\t");
            exception.append(accession);
        }
        if (present != null){
            exception.append("\n");
            exception.append("Assembly Sequence already present");
            exception.append("\t");
            exception.append(present);
        }
        return new DuplicateAssemblySequenceException(exception.toString());
    }
}
