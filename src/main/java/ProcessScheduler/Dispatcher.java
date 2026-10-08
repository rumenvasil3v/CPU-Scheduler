package ProcessScheduler;

import java.util.Queue;

/**
 * Dispatcher is a thread which checks the readyQueue for a process and
 * dispatches one at a time.
 */
public class Dispatcher implements Runnable {
	/**
	 * Instance fields for accessing ready queue and scheduler object
	 */
	private ProcessControlBlock PCB = null;
	private Queue<ProcessControlBlock> readyQueue = null;
	private Scheduler scheduler = null;
	private ProcessCreator creator = null;
	private boolean dispatched = false; // ensures only one process is dispatched at a time.
	private JobQueue jobQueue = null;

	/**
	 * Constructor to assign input parameters to instance fields.
	 * 
	 * @param readyQueue
	 * @param scheduler
	 */
	// Dispatcher.java (constructor needs creator + jobQueue)
	public Dispatcher(Queue<ProcessControlBlock> readyQueue, Scheduler scheduler, ProcessCreator creator,
			JobQueue jobQueue) {
		this.readyQueue = readyQueue;
		this.scheduler = scheduler;
		this.creator = creator;
		this.jobQueue = jobQueue;
	}

	/**
	 * Continuously check the readyQueue and dispatch one process at a time until
	 * all processes have finished. Run in a loop while there is still process in
	 * the readyQueue If readyQueue is empty: Check whether ProcessCreator has
	 * finished and jobQueue is empty. If both are true → stop the Dispatcher.
	 * Otherwise → wait for new processes to arrive (wait() on readyQueue). If
	 * readyQueue is not empty: Call scheduler.runAlgorithm() to execute the
	 * scheduling algorithm Use synchronisation when accessing readyQueue.
	 */
	@Override
	public void run() {

		while (true) {

//			if (!readyQueue.isEmpty()) {				
//				this.scheduler.runAlgorithm();
//			}
			
			synchronized (readyQueue) {
				
				if (this.readyQueue.isEmpty()) {

					if (creator.isFinished() && jobQueue.getQueue().isEmpty()) {
						return;
					} else {

						while (readyQueue.isEmpty()) {
							try {
								readyQueue.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					this.scheduler.runAlgorithm();
				}
			}		
		}
	}

	public ProcessControlBlock getPCB() {
		return this.PCB;
	}

	public void setDispatched(boolean cpuBusy) {
		this.dispatched = cpuBusy;
	}

	public boolean getDispatched() {
		return this.dispatched;
	}

}
