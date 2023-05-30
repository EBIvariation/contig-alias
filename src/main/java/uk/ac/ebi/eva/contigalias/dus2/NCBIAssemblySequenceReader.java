package uk.ac.ebi.eva.contigalias.dus2;

import java.io.IOException;
import java.io.InputStreamReader;

public class NCBIAssemblySequenceReader extends AssemblySequenceReader{

    public NCBIAssemblySequenceReader(InputStreamReader inputStreamReader){
        super(inputStreamReader);
    }

    @Override
    protected void parseFile() throws IOException, NullPointerException {
        if (reader == null){
            throw new NullPointerException("Cannot use AssemblySequenceReader without having a valid InputStreamReader.");
        }
        // TODO: HERE WE'LL EXTARACT THE .gz FILE AND PARSE THE fna FILE
    }

    @Override
    // Parsing a line of the file
    protected void parseAssemblySequenceEntity(String line) {
        // TODO: HERE WE'LL PARSE A LINE OF THE FILE (AN ENTRY)
        // TODO: NOTE: THIS METHOD MIGHT NOT BE COMPLETELY USEFUL SINCE THE FILE CONTAINS ONLY
        // TODO: TEXT AND A '>' SEPARATORS TO SEPARATE SEQUENCES FROM ONE ANOTHER
    }
}
