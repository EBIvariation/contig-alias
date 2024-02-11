package uk.ac.ebi.eva.contigalias.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.contigalias.conf.ApplicationContextHolder;
import uk.ac.ebi.eva.contigalias.scheduler.Job.Job;
import uk.ac.ebi.eva.contigalias.scheduler.Job.JobSubmittedEvent;
import uk.ac.ebi.eva.contigalias.scheduler.Job.JobType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class ChromosomeUpdater {
    private final Logger logger = LoggerFactory.getLogger(ChromosomeUpdater.class);
    private final BlockingQueue<Job> jobQueue = new LinkedBlockingQueue<>();
    private final ENASequenceNameUpdater enaSequenceNameUpdater;
    private final MD5ChecksumUpdater md5ChecksumUpdater;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Job currentJob;

    @Autowired
    public ChromosomeUpdater(ENASequenceNameUpdater enaSequenceNameUpdater, MD5ChecksumUpdater md5ChecksumUpdater) {
        this.md5ChecksumUpdater = md5ChecksumUpdater;
        this.enaSequenceNameUpdater = enaSequenceNameUpdater;
    }

    public void submitJob(Job job) {
        jobQueue.add(job);
        logger.info("Submitted Job : " + job);
        JobSubmittedEvent event = new JobSubmittedEvent(this);
        ApplicationContextHolder.getApplicationContext().publishEvent(event);
    }

    public void submitJob(List<Job> jobList) {
        jobQueue.addAll(jobList);
        jobList.stream().forEach(job -> logger.info("Submitted Job : " + job));
        JobSubmittedEvent event = new JobSubmittedEvent(this);
        ApplicationContextHolder.getApplicationContext().publishEvent(event);
    }

    @Async
    public void processJobs() {
        running.set(true);
        currentJob = null;
        while (!jobQueue.isEmpty()) {
            try {
                currentJob = jobQueue.take();
                if (currentJob.getType() == JobType.ENA_SEQUENCE_NAME_UPDATE) {
                    enaSequenceNameUpdater.updateENASequenceNameForAssembly(currentJob.getParameter());
                } else if (currentJob.getType() == JobType.MD5_CHECKSUM_UPDATE) {
                    md5ChecksumUpdater.updateMD5ChecksumForAssembly(currentJob.getParameter());
                }
            } catch (Exception e) {
                logger.error("Exception while running job : " + currentJob);
            }
        }
        currentJob = null;
        running.set(false);
    }

    public List<String> getScheduledJobStatus() {
        List<String> jobList = new ArrayList<>();
        if (currentJob != null) {
            jobList.add(currentJob.toString());
        }
        jobList.addAll(jobQueue.stream()
                .map(j -> j.getType().toString() + " : " + j.getParameter())
                .collect(Collectors.toList()));

        return jobList;
    }

    public AtomicBoolean isRunning() {
        return running;
    }
}
