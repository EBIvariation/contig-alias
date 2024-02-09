package uk.ac.ebi.eva.contigalias.scheduler.Job;


import org.springframework.context.ApplicationEvent;

public class JobSubmittedEvent extends ApplicationEvent {
    public JobSubmittedEvent(Object source) {
        super(source);
    }
}