package uk.ac.ebi.eva.contigalias.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.contigalias.datasource.NCBIAssemblySequencesDataSource;
import uk.ac.ebi.eva.contigalias.entities.AssemblySequencesEntity;
import uk.ac.ebi.eva.contigalias.exception.AssemblySequenceNotFoundException;
import uk.ac.ebi.eva.contigalias.exception.DuplicateAssemblySequenceException;
import uk.ac.ebi.eva.contigalias.repo.AssemblySequencesRepository;

@Service
public class AssemblySequencesService {

    private final AssemblySequencesRepository repository;

    private final NCBIAssemblySequencesDataSource ncbiSequenceDataSource;

    private final Logger logger = LoggerFactory.getLogger(AssemblyService.class);


    public AssemblySequencesService(
            AssemblySequencesRepository repository, NCBIAssemblySequencesDataSource ncbiSequenceDataSource){
        this.repository = repository;
        this.ncbiSequenceDataSource = ncbiSequenceDataSource;
    }

    public void fetchAndInsertAssemblySequence(String accession) throws IOException, NoSuchAlgorithmException {
        Optional<AssemblySequencesEntity> entity = repository.findAssemblySequenceEntityByInsdcAccession(accession);
        if(entity.isPresent())
            throw duplicateAssemblySequenceInsertionException(accession, entity.get());
        Optional<AssemblySequencesEntity> fetchAssembly = ncbiSequenceDataSource.getAssemblySequencesByAccession(accession);
        if(!fetchAssembly.isPresent()){
            throw new AssemblySequenceNotFoundException(accession);
        }
        if (fetchAssembly.get().getInsdcAccession() != null){ // This condition is only for testing, it'll change as soon as we add more attributes to the entity
            insertAssemblySequence(fetchAssembly.get());
            logger.info("Successfully inserted assembly for accession " + accession);
        }else {
            logger.error("Skipping inserting assembly sequence : No name in assembly : " + accession);
        }
    }

    @Transactional
    public void insertAssemblySequence(AssemblySequencesEntity entity) {
        if (isEntityPresent(entity)) {
            throw duplicateAssemblySequenceInsertionException(null, entity);
        } else {
            repository.save(entity);
        }
    }

    private boolean isEntityPresent(AssemblySequencesEntity entity) {
        // TODO: THE CONDITIONS IN THIS METHOD WILL BE CHANGED WHEN WE ADD MORE ATTRIBUTES TO THE ENTITY
        Optional<AssemblySequencesEntity> existingAssembly = repository.findAssemblySequenceEntityByInsdcAccession(entity.getInsdcAccession());
        return existingAssembly.isPresent();
    }

    private DuplicateAssemblySequenceException duplicateAssemblySequenceInsertionException(String accession, AssemblySequencesEntity present) {
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
