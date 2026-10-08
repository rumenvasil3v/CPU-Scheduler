package ProcessScheduler;

import java.util.Queue;

public class ProcessCreator implements Runnable {
    private final JobQueue jobQueue;
    private final Queue<ProcessControlBlock> readyQueue;
    private volatile boolean finished = false;

    public ProcessCreator(JobQueue jobQueue, Queue<ProcessControlBlock> readyQueue) {
        this.jobQueue = jobQueue;
        this.readyQueue = readyQueue;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
    /**
     * Move every PCB from JobQueue to readyQueue.
     * Repeatedly take a PCB from jobQueue
     * For each PCB:
     *   Set state to "ready"
     *   Add PCB to readyQueue    
     * Synchronise access to the readyQueue so no other thread can modify it while processing.
     * Stop when jobQueue is empty
     */
    @Override
    public void run() {
        
    	Queue<ProcessControlBlock> jobQueue = this.jobQueue.getQueue();
    	
    	while (jobQueue.size() > 0) {
    		
    		ProcessControlBlock pcb = jobQueue.remove();
    		pcb.setState("ready");

    		synchronized (readyQueue) {
    			readyQueue.add(pcb);	
    			readyQueue.notifyAll();
    		}
    	}
    	
    	this.setFinished(true);
    }

    public JobQueue getJobQueue() {
        return jobQueue;
    }

    public Queue<ProcessControlBlock> getReadyQueue() {
        return readyQueue;
    }
}
