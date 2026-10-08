package ProcessScheduler;

import java.util.Queue;
import java.util.Random;

/**
 * Scheduler selects which process runs next based on the chosen algorithm. -
 * FCFS - RR (Round Robin) - Non-preemptive priority
 */
public class Scheduler {

	protected final Queue<ProcessControlBlock> readyQueue;
	private final EventLog log;

	private String schedulerAlgorithm;
	private long timeQuantum;

	public Scheduler(Queue<ProcessControlBlock> readyQueue, String schedulerAlgorithm, long timeQuantum, EventLog log) {
		this.readyQueue = readyQueue;
		this.schedulerAlgorithm = schedulerAlgorithm;
		this.timeQuantum = timeQuantum;
		this.log = log;
	}

	public String getAlgorithm() {
		return schedulerAlgorithm;
	}

	public long getQuantum() {
		return timeQuantum;
	}

	/**
	 * Executes exactly ONE scheduling decision per call.
	 * 
	 * @return stage number for convenience (1=FCFS, 2=RR, 3=PRIORITY), or -1 if
	 *         unknown.
	 */
	public int runAlgorithm() {
		if (schedulerAlgorithm == null)
			return -1;

		if (schedulerAlgorithm.equalsIgnoreCase("FCFS")) {
			FCFS();
			return 1;
		}

		if (schedulerAlgorithm.equalsIgnoreCase("RR")) {
			RR();
			return 2;
		}

		if (schedulerAlgorithm.equalsIgnoreCase("PRIORITY") || schedulerAlgorithm.equalsIgnoreCase("NPPRIORITY")
				|| schedulerAlgorithm.equalsIgnoreCase("NPP") || schedulerAlgorithm.equalsIgnoreCase("Priority")) {
			nonPreemptivePriority();
			return 3;
		}

		return -1;
	}

	/**
	 * Stage 1 — FCFS Runs the first process in the ready queue to completion.
	 * Remove the first PCB Run it using CPU Wait for completion Set state to
	 * "terminated" Update its execution time Set the remaining burst time to be
	 * zero Add PCB to EventLog print the details of each completed PCB. For
	 * example,e.g., P01: terminated, Context Switches: 0, Output: Sum is 9,
	 * Execution Time: 30ms
	 */
	public void FCFS() {
	
		if (readyQueue.isEmpty()) {
			return;
		} else {
			ProcessControlBlock pcb = this.readyQueue.remove();
			pcb.setState("new");

			CPU cpu = new CPU(pcb);
			long startTime = System.nanoTime();
			cpu.run();

			try {
				cpu.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long endTime = System.nanoTime();

			pcb.setState("terminated");
			pcb.setRemainingBurstTime(0);
			pcb.addExecutedTime((endTime - startTime) / 1000000);

			this.log.addPCB(pcb);
			System.out.println(pcb.getPID() + ": " + pcb.getState() + ", Context Switches: " + pcb.getContextSwitches()
					+ ", Output: " + pcb.getPCBResult() + ", Execution Time: " + pcb.getExecutionTime());
		}	
	}

	/**
	 * Stage 2 — Round Robin (simulated CPU burst; script executes once at
	 * completion) - Remove head of readyQueue - Run for min(remainingBurstTime,
	 * timeQuantum) (simulation) - If unfinished: contextSwitches++, state="ready",
	 * re-add to END - If finished: run Python once, terminate and log
	 */
	public void RR() {
		final ProcessControlBlock pcb;

		if (readyQueue.isEmpty()) {
			return;
		} else {
			pcb = readyQueue.remove();

			long execTime = Math.min(pcb.getRemainingBurstTime(), timeQuantum);
			pcb.addExecutedTime(execTime);
			pcb.setRemainingBurstTime(pcb.getRemainingBurstTime() - timeQuantum);

			if (pcb.getRemainingBurstTime() > 0) {
				
				pcb.addContextSwitch();
				pcb.setState("ready");
				readyQueue.add(pcb);
				
			} else if (pcb.getRemainingBurstTime() <= 0) {
				CPU cpu = new CPU(pcb);
				cpu.run();
				try {
					cpu.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				pcb.setState("terminated");
				log.addPCB(pcb);
				
				for (ProcessControlBlock p : log.getPCBLog()) {
					System.out.println("Going through the event log: ");
					
					System.out.println("\t\t" + "PID: " + p.getPID());
				}
				
				System.out.println(pcb.toString());
				System.out.println(pcb.printProcessControlBlock());
				System.out.println(pcb.getPCBResult());
				System.out.println("---------------------------------------------------");
			}	
		}
	}

	/**
	 * Stage 3 — Non-Preemptive Priority Scheduling
	 *
	 * Picks the highest-priority process from readyQueue (higher number = higher
	 * priority). Tie-break: earlier arrivalTime first. Non-preemptive: once
	 * selected, runs to completion.
	 */
	public void nonPreemptivePriority() {
		int highestPriority = 0;
		
		for (int i = 0; i < readyQueue.size(); i++) {	
			
			if (readyQueue.peek().getPriority() > highestPriority) {
				continue;
			} else {
				ProcessControlBlock pcb = readyQueue.remove();
				readyQueue.add(pcb);
			}
		}
		
		this.FCFS(); // Making use of the FCFS algorithm to execute the process with the highest priority
	}
}
