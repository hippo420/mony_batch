package app.monybatch.mony.business.batch.job;

import org.springframework.batch.core.Job;

public class DescriptiveJob {
    private final Job job;
    private final String description;

    public DescriptiveJob(Job job, String description) {
        this.job = job;
        this.description = description;
    }

    public Job getJob() { return job; }
    public String getDescription() { return description; }
}
