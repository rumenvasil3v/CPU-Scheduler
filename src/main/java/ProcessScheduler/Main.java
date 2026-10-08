package ProcessScheduler;

import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.io.FileNotFoundException;

public class Main {

    /*
     * ARRIVAL_COUNTER:
     * Used to give each process a unique arrival order.
     * This helps when two processes arrive at the same time.
     * (You do NOT need to edit this.)
     */
    private static final AtomicLong ARRIVAL_COUNTER = new AtomicLong(0);

    /*
     * =========================================================
     * Choose which stage to run
     * =========================================================
     *
     * 0 -> Run ONE script only (no scheduling)
     * 1 -> FCFS using input file
     * 2 -> Round Robin using input file + time quantum
     * 3 -> Non-preemptive Priority using input file
     *
     * For Stage 0 : Only focus on runStage0SingleScript()
     * You do NOT need to understand all threads yet.
     */

    private static final AtomicLong CURRENT_TIME = new AtomicLong(0);

    // These threads are used in Stage 1–3 (ignore for Stage 0)
    private static Thread processCreatorThread = null;
    private static Thread dispatcherThread = null;

    // These queues are used in Stage 1–3
    private static JobQueue jobQueue = null;
    private static Queue<ProcessControlBlock> readyQueue = null;

    private static String fileSource;
    private static String algorithm = null;
    private static long quantum = 50;

    // Stores completed processes (used in Stage 1–3)
    private static EventLog log = new EventLog();

    // Path to Python interpreter (Linux path shown here)
    // type 'where python' in Windows in the command prompt to get the path. 
	// The path should have double backslashes (\\) in it,
	// (e.g. C:\\Users\\YourUsername\\AppData\\Local\\Programs\\Python\\Python39\\python.exe)
    // Type 'which python3' in Linux (lab machines) to find the path 
    // ** Change it to the origin path for the marking **
    public static final String pythonPath = "/usr/bin/python3";
    public static final String pythonPathWindows = "python.exe";
    
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {

        /*
         * Change this number to test different stages.
         */
        int stage = 2;

        String script = "process4.py"; // Used in Stage 0
        String inputScripts = "InputScripts1.txt"; // Used in Stage 1–3

        switch (stage) {

            case 0 -> {
                // Stage 0: Run one single script only
                runStage0SingleScript(script);
            }

            case 1 -> {
                // Stage 1: FCFS
                initialise(new String[] { inputScripts, "FCFS", "0" });
                finaliseThreads();
            }

            case 2 -> {
                // Stage 2: Round Robin
                initialise(new String[] { inputScripts, "RR", Long.toString(quantum) });
                finaliseThreads();
            }

            case 3 -> {
                // Stage 3: Non-preemptive Priority
                initialise(new String[] { inputScripts, "PRIORITY", "0" });
                finaliseThreads();
            }

            default -> System.err.println("Invalid stage. Must be 0,1,2,3.");
        }
    }

    /*
     * =========================================================
     * TO DO: STAGE 0 — RUN A SINGLE SCRIPT
     * =========================================================
     * 1. Create a PCB (process)
     * 2. Pass it to the CPU
     * 3. Start the CPU thread
     * 4. Wait for it to finish
     * 5. Print the result
     *
     * There is NO scheduling here.
     * No ready queue.
     * No dispatcher.
     *
     */
    public static ProcessControlBlock runStage0SingleScript(String scriptName)  {

        //  Create a PCB (Process Control Block), Format: (PID, priority, arrivalTime, scriptName)
    	ProcessControlBlock PCB = new ProcessControlBlock("P01", 1, 0, scriptName);
    	PCB.setCPUBurstTime(0);
    	PCB.setState("new");
    	
    	CPU cpu = new CPU(PCB);
    	cpu.run();
    	
    	try {
			cpu.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	if (!PCB.getState().equals("terminated")) {
    		PCB.setState("terminated");
    	}
    	
        return PCB;
    }

    /*
     * Used in Stage 1–3 only.
     * Waits for all threads to finish and prints completion order.
     */
    public static void finaliseThreads() {
        try {
            processCreatorThread.join();
            dispatcherThread.join();
            Thread.sleep(100);

            System.out.println("Completion order:");
            ArrayList<ProcessControlBlock> processes = log.getPCBLog();
            for (ProcessControlBlock process : processes) {
                System.out.println(process.toString());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /*
     * Stage 1–3 setup.
     * Reads input file and starts scheduling threads.
     * You do NOT need to modify this for Stage 0.
     */
    public static void initialise(String[] optionalArgs) throws FileNotFoundException {

        if (optionalArgs != null) {
            algorithm = optionalArgs[1];
            fileSource = optionalArgs[0];
            quantum = Long.parseLong(optionalArgs[2]);
        }

        CURRENT_TIME.set(0);

        // Create JobQueue and read file
        jobQueue = new JobQueue();
        try {
            jobQueue.readFile(fileSource);
        } catch (Exception e) {
            e.printStackTrace();
        }

        readyQueue = new LinkedList<>();

        ProcessCreator creator = new ProcessCreator(jobQueue, readyQueue);
        processCreatorThread = new Thread(creator);

        Scheduler scheduler = new Scheduler(readyQueue, algorithm, quantum, log);
        dispatcherThread = new Thread(new Dispatcher(readyQueue, scheduler, creator, jobQueue));

        processCreatorThread.start();
        dispatcherThread.start();
    }

    // Helper methods (no need to modify)

    public static EventLog getLog() {
        return log;
    }

    // Do not edit this function
    public static void resetForTests() {
        log = new EventLog();
        processCreatorThread = null;
        dispatcherThread = null;
        jobQueue = null;
        readyQueue = null;
        fileSource = null;
        algorithm = null;
        quantum = 50;
        ARRIVAL_COUNTER.set(0);
    }

    // Do not edit this function
    public static long getCurrentTime() {
        return CURRENT_TIME.get();
    }

    // Do not edit this function
    /*
    * advanceTime(deltaMs)
    *
    * Simulates the system clock moving forward.
    * Call this whenever a process uses CPU time.
    *
    * Example:
    * If a process runs for 50ms,
    * call Main.advanceTime(50).
    */
    public static void advanceTime(long deltaMs) {
        if (deltaMs < 0)
            return;
        CURRENT_TIME.addAndGet(deltaMs);
    }
}