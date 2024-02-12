package uk.ac.ebi.eva.contigalias.exception;

public class AssemblyIngestionException extends RuntimeException {

    public AssemblyIngestionException(String accession) {
        super("Error Ingesting assembly with accession " + accession);
    }
}
