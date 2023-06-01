package uk.ac.ebi.eva.contigalias.datasource;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.ebi.eva.contigalias.entities.AssemblySequencesEntity;
import uk.ac.ebi.eva.contigalias.entities.Sequence;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class NCBIAssemblySequencesDataSourceTest {


    @Autowired
    NCBIAssemblySequencesDataSource dataSource;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAssemblySequenceByAccession() throws IOException, NoSuchAlgorithmException, InterruptedException {


        String accession = "GCF_000001765.3";
        //String accession2 = "GCF_000001405.31";
        Optional<AssemblySequencesEntity> entity = dataSource.getAssemblySequencesByAccession(accession);
        //displayAssemblySequencesEntityContent(entity.get());
        assertEquals(accession, entity.get().getInsdcAccession());
    }

    void displayAssemblySequencesEntityContent(AssemblySequencesEntity entity) throws InterruptedException {
        System.out.println("ACCESSION: " + entity.getInsdcAccession());
        System.out.println("TOTAL NUMBER OF SEQUENCES: " + entity.getSequences().size());
        for (Sequence s: entity.getSequences()){
            System.out.print("REFSEQ: " + s.getRefseq() + " | ");
            System.out.println("SEQUENCE_MD5: " + s.getSequenceMD5());
            Thread.sleep(1000); // Just for lazy and fun display :)
        }
    }

    @Test
    void downloadAssemblySequence() {
    }
}