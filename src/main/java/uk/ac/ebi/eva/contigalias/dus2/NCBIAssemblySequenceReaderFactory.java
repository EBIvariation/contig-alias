package uk.ac.ebi.eva.contigalias.dus2;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.stereotype.Component;

@Component
public class NCBIAssemblySequenceReaderFactory {

    public NCBIAssemblySequenceReader build(InputStream inputStream){
        return new NCBIAssemblySequenceReader(new InputStreamReader(inputStream));
    }

    public NCBIAssemblySequenceReader build(InputStreamReader inputStreamReader){
        return new NCBIAssemblySequenceReader(inputStreamReader);
    }
}
