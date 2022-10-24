package uk.ac.ebi.eva.contigalias.exception;

public class DownloadFailedException extends RuntimeException {

    public DownloadFailedException(String msg) {
        super(msg);
    }
}
