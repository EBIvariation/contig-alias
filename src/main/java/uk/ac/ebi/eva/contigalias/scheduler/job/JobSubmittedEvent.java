package uk.ac.ebi.eva.contigalias.scheduler.job;


import org.springframework.context.ApplicationEvent;

public class JobSubmittedEvent extends ApplicationEvent {
    public JobSubmittedEvent(Object source) {
        super(source);
    }
}