package uk.ac.ebi.eva.contigalias.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.contigalias.datasource.NCBIAssemblyDataSource;
import uk.ac.ebi.eva.contigalias.datasource.NCBIAssemblySequencesDataSource;
import uk.ac.ebi.eva.contigalias.entities.*;
import uk.ac.ebi.eva.contigalias.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.contigalias.exception.AssemblySequenceNotFoundException;
import uk.ac.ebi.eva.contigalias.repo.AssemblyRepository;
import uk.ac.ebi.eva.contigalias.repo.AssemblySequencesRepository;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class SequenceCollectionService {

    @Autowired
    private AssemblyService assemblyService;

    @Autowired
    private AssemblySequencesService assemblySequencesService;

    private final AssemblyRepository assemblyRepository;

    private final AssemblySequencesRepository assemblySequencesRepository;

    private final NCBIAssemblyDataSource assemblyDataSource;

    private final NCBIAssemblySequencesDataSource assemblySequencesDataSource;

    private final Logger logger = LoggerFactory.getLogger(SequenceCollectionService.class);

    public SequenceCollectionService(AssemblyRepository assemblyRepository, AssemblySequencesRepository assemblySequencesRepository,
                                     NCBIAssemblyDataSource assemblyDataSource, NCBIAssemblySequencesDataSource assemblySequencesDataSource) {
        this.assemblyRepository = assemblyRepository;
        this.assemblySequencesRepository = assemblySequencesRepository;
        this.assemblyDataSource = assemblyDataSource;
        this.assemblySequencesDataSource = assemblySequencesDataSource;
    }

    /**
     * Search for the assembly report as well as the assembly real sequences and insert them
     * in the database.
     * Use the given naming convention while constructing the SeqCol Object*/
    public void fetchAndInsertSequenceCollection(String accession, SeqColEntity.NamingConvention namingConvention)
            throws IOException, NoSuchAlgorithmException {
        // TODO: Check if the needed seqCol data does not exist in the database
        // TODO: If not, call the appropriate service(s) to fetch it

        Optional<AssemblyEntity> fetchAssembly = assemblyDataSource.getAssemblyByAccession(accession);
        if (!fetchAssembly.isPresent()){
            throw new AssemblyNotFoundException(accession);
        }
        assemblyService.insertAssembly(fetchAssembly.get());
        Optional<AssemblySequencesEntity> fetchAssemblySequences = assemblySequencesDataSource
                .getAssemblySequencesByAccession(accession);
        if (!fetchAssemblySequences.isPresent()){
            throw new AssemblySequenceNotFoundException(accession);
        }
        assemblySequencesService.insertAssemblySequences(fetchAssemblySequences.get());

        SeqColEntity seqColLevel2 = constructSequenceCollectionObjectL2(fetchAssembly.get(), fetchAssemblySequences.get(),
                namingConvention);

    }

    /**
     * Return a level 1 entity of the sequence collection following the given naming convention.
     * */
    public SeqColEntity constructSequenceCollectionObjectL2(AssemblyEntity assemblyEntity,
                                                             AssemblySequencesEntity assemblySequencesEntity,
                                                             SeqColEntity.NamingConvention namingConvention) {

        List<ChromosomeEntity> chromosomeList = assemblyEntity.getChromosomes();
        List<Sequence> sequenceList = assemblySequencesEntity.getSequences();
        assert chromosomeList.size() == sequenceList.size();

        Comparator<ChromosomeEntity> chromosomeComparator = (chromosomeEntity, t1) ->
                chromosomeEntity.getRefseq().compareTo(t1.getRefseq());
        Comparator<Sequence> sequenceComparator = (sequence, t1) -> sequence.getSequenceRefseq().compareTo(t1.getSequenceRefseq());

        Collections.sort(chromosomeList, chromosomeComparator);
        Collections.sort(sequenceList, sequenceComparator);

        SeqColEntity seqColL2 = new SeqColEntity();


        List<String> sequences = new LinkedList<>();
        List<String> names = new LinkedList<>();
        List<Long> lengths = new LinkedList<>();

        switch (namingConvention) {
            case ENA:
                for (int i=0; i<sequenceList.size(); i++){
                    sequences.add(sequenceList.get(i).getSequenceMD5());
                    names.add(chromosomeList.get(i).getEnaSequenceName());
                    lengths.add(chromosomeList.get(i).getSeqLength());
                }
            break;
            case GENBANK:
                for (int i=0; i<sequenceList.size(); i++){
                    sequences.add(sequenceList.get(i).getSequenceMD5());
                    names.add(chromosomeList.get(i).getGenbankSequenceName());
                    lengths.add(chromosomeList.get(i).getSeqLength());
                }
            break;
            case UCSC:
                for (int i=0; i<sequenceList.size(); i++){
                    sequences.add(sequenceList.get(i).getSequenceMD5());
                    names.add(chromosomeList.get(i).getUcscName());
                    lengths.add(chromosomeList.get(i).getSeqLength());
                }
        }
        seqColL2.setSequences(sequences);
        seqColL2.setLengths(lengths);
        seqColL2.setNames(names);
        seqColL2.setLevel(SeqColEntity.Level.TWO);
        seqColL2.setNamingConvention(namingConvention);

        return seqColL2;
    }
}
