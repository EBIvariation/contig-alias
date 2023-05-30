package uk.ac.ebi.eva.contigalias.exception;

public class AssemblySequenceNotFoundException extends RuntimeException{
    public AssemblySequenceNotFoundException(String accession) {
        super("No assembly sequence corresponding to accession " + accession + " could be found");
    }
}
