package uk.ac.ebi.eva.contigalias.scheduler.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.contigalias.scheduler.ChromosomeUpdater;

@Component
public class JobSubmittedEventHandler implements ApplicationListener<JobSubmittedEvent> {
    private ChromosomeUpdater chromosomeUpdater;

    @Autowired
    public JobSubmittedEventHandler(ChromosomeUpdater chromosomeUpdater) {
        this.chromosomeUpdater = chromosomeUpdater;
    }

    @Override
    public void onApplicationEvent(JobSubmittedEvent event) {
        if (!chromosomeUpdater.isRunning().get()) {
            chromosomeUpdater.processJobs();
        }
    }
}
