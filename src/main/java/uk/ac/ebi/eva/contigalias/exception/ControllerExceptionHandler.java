package uk.ac.ebi.eva.contigalias.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AssemblyNotFoundException.class)
    public ResponseEntity<String> handleExceptions(AssemblyNotFoundException exception, WebRequest webRequest) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateAssemblyException.class)
    public ResponseEntity<String> handleExceptions(DuplicateAssemblyException exception, WebRequest webRequest){
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IncorrectAccessionException.class)
    public ResponseEntity<String> handleExceptions(IncorrectAccessionException exception, WebRequest webRequest){
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DownloadFailedException.class)
    public ResponseEntity<String> handleExceptions(DownloadFailedException exception, WebRequest webRequest){
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AssemblyIngestionException.class)
    public ResponseEntity<String> handleExceptions(AssemblyIngestionException exception, WebRequest webRequest){
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
