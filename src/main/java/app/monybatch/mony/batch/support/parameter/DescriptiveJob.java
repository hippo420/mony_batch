package app.monybatch.mony.batch.support.parameter;

import org.springframework.batch.core.Job;

import java.util.List;

public class DescriptiveJob {
    private final Job job;
    private final String description;
    private final List<JobParamSpec> paramSpecs;

    public DescriptiveJob(Job job, String description) {
        this(job, description, List.of());
    }

    public DescriptiveJob(Job job, String description, List<JobParamSpec> paramSpecs) {
        this.job = job;
        this.description = description;
        this.paramSpecs = paramSpecs;
    }

    public Job getJob() { return job; }
    public String getDescription() { return description; }
    public List<JobParamSpec> getParamSpecs() { return paramSpecs; }
}
