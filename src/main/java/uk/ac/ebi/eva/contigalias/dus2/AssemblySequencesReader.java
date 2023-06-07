package uk.ac.ebi.eva.contigalias.dus2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import uk.ac.ebi.eva.contigalias.entities.AssemblySequencesEntity;

public abstract class AssemblySequencesReader {

    protected final BufferedReader reader;

    protected final String accession;

    protected AssemblySequencesEntity assemblySequencesEntity;


    protected boolean fileParsed = false;


    public AssemblySequencesReader(InputStreamReader inputStreamReader, String accession){
        this.reader = new BufferedReader(inputStreamReader);
        this.accession = accession;
    }

    public AssemblySequencesEntity getAssemblySequenceEntity() throws IOException, NoSuchAlgorithmException {
        if(!fileParsed || assemblySequencesEntity == null){
            parseFile();
        }
        return assemblySequencesEntity;
    }

    protected abstract void parseFile() throws IOException, NullPointerException, NoSuchAlgorithmException;


    protected abstract void parseAssemblySequenceEntity(String line);



    public boolean ready() throws IOException {
        return reader.ready();
    }
}
