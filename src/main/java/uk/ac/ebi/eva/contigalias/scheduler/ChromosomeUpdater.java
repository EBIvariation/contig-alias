package uk.ac.ebi.eva.contigalias.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Service
public class ChromosomeUpdater implements ApplicationListener<JobSubmittedEvent> {
    private final Logger logger = LoggerFactory.getLogger(ChromosomeUpdater.class);
    private final BlockingQueue<Job> jobQueue = new LinkedBlockingQueue<>();
    private final ENASequenceNameUpdater enaSequenceNameUpdater;
    private final MD5ChecksumUpdater md5ChecksumUpdater;


    @Autowired
    public ChromosomeUpdater(ENASequenceNameUpdater enaSequenceNameUpdater, MD5ChecksumUpdater md5ChecksumUpdater) {
        this.md5ChecksumUpdater = md5ChecksumUpdater;
        this.enaSequenceNameUpdater = enaSequenceNameUpdater;
    }

    public void submitJob(Job job) {
        jobQueue.add(job);
        logger.info("Submitted Job : " + job.getType() + " for assembly " + job.getParameter());
        JobSubmittedEvent event = new JobSubmittedEvent(this);
        ApplicationContextHolder.getApplicationContext().publishEvent(event);
    }

    @Override
    public void onApplicationEvent(JobSubmittedEvent event) {
        processJobs();
    }

    @Async
    public void processJobs() {
        while (!jobQueue.isEmpty()) {
            try {
                Job job = jobQueue.take();
                if (job.getType() == JobType.ENA_SEQUENCE_NAME_UPDATE) {
                    enaSequenceNameUpdater.updateENASequenceNameForAssembly(job.getParameter());
                } else if (job.getType() == JobType.MD5_CHECKSUM_UPDATE) {
                    md5ChecksumUpdater.updateMD5ChecksumForAssembly(job.getParameter());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public List<String> getScheduledJobStatus() {
        return jobQueue.stream().map(j -> j.getType().toString()).collect(Collectors.toList());
    }
}
