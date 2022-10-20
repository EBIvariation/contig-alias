package uk.ac.ebi.eva.contigalias.exception;

public class DownloadFailedException extends RuntimeException {

    public DownloadFailedException(String accession) {
        super("Failed to download Assembly Report for " + accession);
    }
}
