package uk.ac.ebi.eva.contigalias.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.ac.ebi.eva.contigalias.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.contigalias.exception.DuplicateAssemblyException;

@ControllerAdvice
public class AdminControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AssemblyNotFoundException.class)
    public ResponseEntity<String> handleExceptions(AssemblyNotFoundException exception, WebRequest webRequest) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateAssemblyException.class)
    public ResponseEntity<String> handleExceptions(DuplicateAssemblyException exception, WebRequest webRequest){
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

}
