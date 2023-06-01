package uk.ac.ebi.eva.contigalias.dus2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ebi.eva.contigalias.entities.AssemblySequencesEntity;
import uk.ac.ebi.eva.contigalias.entities.Sequence;
import uk.ac.ebi.eva.contigalias.utils.MD5Digest;

public class NCBIAssemblySequencesReader extends AssemblySequencesReader {

    public NCBIAssemblySequencesReader(InputStreamReader inputStreamReader, String accession){
        super(inputStreamReader, accession);
    }

    @Override
    protected void parseFile() throws IOException, NullPointerException, NoSuchAlgorithmException {
        if (reader == null){
            throw new NullPointerException("Cannot use AssemblySequenceReader without having a valid InputStreamReader.");
        }
        MD5Digest md5Digest = new MD5Digest();
        if (assemblySequencesEntity == null){
            assemblySequencesEntity = new AssemblySequencesEntity();
        }
        // Setting the accession of the whole assembly file
        assemblySequencesEntity.setInsdcAccession(accession);
        List<Sequence> sequences = new LinkedList<>();
        String line = reader.readLine();
        while (line != null){
            if (line.startsWith(">")){
                Sequence sequence = new Sequence();
                String refSeq = line.substring(1, line.indexOf(' '));
                sequence.setRefseq(refSeq);
                line = reader.readLine();
                StringBuilder sequenceValue = new StringBuilder();
                while (line != null && !line.startsWith(">")){
                    // Looking for the sequence lines for this refseq
                    sequenceValue.append(line);
                    line = reader.readLine();
                }
                String md5checksum = md5Digest.hash(sequenceValue.toString());
                sequence.setSequenceMD5(md5checksum);
                sequences.add(sequence);
            }
        }
        assemblySequencesEntity.setSequences(sequences);
        fileParsed = true;
        reader.close();
    }

    @Override
    // Parsing a line of the file
    protected void parseAssemblySequenceEntity(String line) {
        // TODO: HERE WE'LL PARSE A LINE OF THE FILE (AN ENTRY)
        // TODO: NOTE: THIS METHOD MIGHT NOT BE COMPLETELY USEFUL SINCE THE FILE CONTAINS ONLY
        // TODO: TEXT AND A '>' SEPARATORS TO SEPARATE SEQUENCES FROM ONE ANOTHER
    }
}
