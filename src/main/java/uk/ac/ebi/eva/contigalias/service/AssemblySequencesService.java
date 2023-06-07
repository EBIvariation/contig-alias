package uk.ac.ebi.eva.contigalias.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.contigalias.datasource.NCBIAssemblySequencesDataSource;
import uk.ac.ebi.eva.contigalias.entities.AssemblySequencesEntity;
import uk.ac.ebi.eva.contigalias.entities.Sequence;
import uk.ac.ebi.eva.contigalias.exception.AssemblySequenceNotFoundException;
import uk.ac.ebi.eva.contigalias.exception.DuplicateAssemblySequenceException;
import uk.ac.ebi.eva.contigalias.repo.AssemblySequencesRepository;

@Service
public class AssemblySequencesService {

    @Autowired
    private ChromosomeService chromosomeService;

    private final AssemblySequencesRepository repository;

    private final NCBIAssemblySequencesDataSource ncbiSequencesDataSource;

    private final Logger logger = LoggerFactory.getLogger(AssemblySequencesService.class);


    public AssemblySequencesService(
            AssemblySequencesRepository repository, NCBIAssemblySequencesDataSource ncbiSequencesDataSource){
        this.repository = repository;
        this.ncbiSequencesDataSource = ncbiSequencesDataSource;
    }

    public void fetchAndInsertAssemblySequence(String accession) throws IOException, NoSuchAlgorithmException {
        Optional<AssemblySequencesEntity> entity = repository.findAssemblySequenceEntityByAssemblyInsdcAccession(accession);
        if(entity.isPresent())
            throw duplicateAssemblySequenceInsertionException(accession, entity.get());
        Optional<AssemblySequencesEntity> fetchAssemblySequences = ncbiSequencesDataSource.getAssemblySequencesByAccession(accession);
        if(!fetchAssemblySequences.isPresent()){
            throw new AssemblySequenceNotFoundException(accession);
        }
        if (fetchAssemblySequences.get().getAssemblyInsdcAccession() != null){
            insertAssemblySequences(fetchAssemblySequences.get());
            logger.info("Successfully inserted assembly sequences for accession: " + accession);
        }else {
            logger.error("Skipping inserting assembly sequences : No name in assembly: " + accession);
        }
    }

    @Transactional
    public void insertAssemblySequences(AssemblySequencesEntity entity) {
        if (isEntityPresent(entity)) {
            throw duplicateAssemblySequenceInsertionException(null, entity);
        } else {
            // Inserting the sequences' md5Checksum in the correct place in the chromosome table
            for (Sequence s: entity.getSequences()){
                chromosomeService.updateChromosomeEntityByRefseqSetMD5Checksum(s.getSequenceRefseq(), s.getSequenceMD5());
            }
            System.out.println("Assembly_insdc_accession: " + entity.getAssemblyInsdcAccession());
            repository.save(entity);
        }
    }

    private boolean isEntityPresent(AssemblySequencesEntity entity) {
        // TODO: THE CONDITIONS IN THIS METHOD WILL BE CHANGED WHEN WE ADD MORE ATTRIBUTES TO THE ENTITY
        Optional<AssemblySequencesEntity> existingAssembly = repository.findAssemblySequenceEntityByAssemblyInsdcAccession(entity.getAssemblyInsdcAccession());
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
