package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.JobExecution;

import java.util.UUID;

public class JobCancelledException extends Exception {
    private String jobName;
    private UUID eventId;
    private UUID fileId;

    public JobCancelledException(JobExecution execution, String message){
        this(execution, message, null);
    }

    public JobCancelledException(JobExecution execution, String message, Throwable throwable){
        super(message, throwable);
        this.jobName = execution.getJob().getJobName();
        this.eventId = execution.getTrigger().getId();
        this.fileId = execution.getTrigger().getFileUuid();
    }

    public String getJobName() {
        return jobName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getFileId() {
        return fileId;
    }
}
