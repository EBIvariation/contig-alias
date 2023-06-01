package uk.ac.ebi.eva.contigalias.dus2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.ac.ebi.eva.contigalias.entities.AssemblySequenceEntity;

public abstract class AssemblySequenceReader {

    protected final BufferedReader reader;

    protected AssemblySequenceEntity assemblySequenceEntity;

    protected boolean fileParsed = false;


    public AssemblySequenceReader(InputStreamReader inputStreamReader){
        this.reader = new BufferedReader(inputStreamReader);
    }

    public AssemblySequenceEntity getAssemblySequenceEntity() throws IOException {
        if(!fileParsed || assemblySequenceEntity == null){
            parseFile();
        }
        return assemblySequenceEntity;
    }

    protected abstract void parseFile() throws IOException, NullPointerException;


    protected abstract void parseAssemblySequenceEntity(String line);



    public boolean ready() throws IOException {
        return reader.ready();
    }
}
