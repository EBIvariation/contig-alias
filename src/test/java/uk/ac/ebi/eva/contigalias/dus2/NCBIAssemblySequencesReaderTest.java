package uk.ac.ebi.eva.contigalias.dus2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.ebi.eva.contigalias.entities.AssemblySequencesEntity;
import uk.ac.ebi.eva.contigalias.entities.Sequence;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NCBIAssemblySequencesReaderTest {

    private static final String ACCESSION = "GCF_000001765.3";

    private static final String FASTA_FILE_PATH = "/tmp/genome_sequence.fna";
    private InputStreamReader streamReader;

    private InputStream stream;

    @Autowired
    private NCBIAssemblySequencesReaderFactory readerFactory;

    private NCBIAssemblySequencesReader reader;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        stream = new FileInputStream(FASTA_FILE_PATH);
        streamReader = new InputStreamReader(stream);
        reader = readerFactory.build(streamReader, ACCESSION);
    }

    @AfterEach
    void tearDown() throws IOException {
        stream.close();
        streamReader.close();
    }

    @Test
    void getAssemblySequencesReader() throws IOException {
        assertTrue(reader.ready());
    }

    @Test
    void assertParsedFastaFileValid() throws IOException, NoSuchAlgorithmException {
        reader.parseFile();
        displayAssemblySequencesEntityContent(reader.assemblySequencesEntity);
        assertEquals(ACCESSION, reader.assemblySequencesEntity.getInsdcAccession());
    }

    void displayAssemblySequencesEntityContent(AssemblySequencesEntity entity){
        System.out.println("ACCESSION: " + entity.getInsdcAccession());
        for (Sequence s: entity.getSequences()){
            System.out.print("REFSEQ: " + s.getRefseq() + " | ");
            System.out.println("SEQUENCE_MD5: " + s.getSequenceMD5());
        }
    }
}