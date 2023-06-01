package uk.ac.ebi.eva.contigalias.dus2;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.stereotype.Component;

@Component
public class NCBIAssemblySequencesReaderFactory {

    public NCBIAssemblySequencesReader build(InputStream inputStream, String accession){
        return new NCBIAssemblySequencesReader(new InputStreamReader(inputStream), accession);
    }

    public NCBIAssemblySequencesReader build(InputStreamReader inputStreamReader, String accession){
        return new NCBIAssemblySequencesReader(inputStreamReader, accession);
    }
}
