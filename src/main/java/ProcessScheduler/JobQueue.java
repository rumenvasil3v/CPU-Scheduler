package ProcessScheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * This class reads a text file, and create PCB objects to be added to the
 * JobQueue
 */
public class JobQueue {

	private Queue<ProcessControlBlock> queue = null;

	public JobQueue() throws FileNotFoundException {
		this.queue = new LinkedList<ProcessControlBlock>();
	}
	/**
	 * Create one ProcessControlBlock (PCB) for each valid line in the input file and define 
	 * when each process arrives in the system.
	 * The order in the file determines their arrival time. 
	 * You need to set the arrival time of process in here. 
	 * The first process in the file should have arrival time 0. 
	 * The next process should have arrival time 1
	 * Then 2, 3, and so on
	 * @param filePath
	 * @throws FileNotFoundException
	 */

	public void readFile(String filePath) throws FileNotFoundException {
		File file = new File(filePath);
		Scanner scanner = new Scanner(file);

		long arrival = 0; // simulated arrival time in file order
		
		while (scanner.hasNextLine()) {
			String data = scanner.nextLine();
			System.out.println(data);
			
			if (data.isBlank() || data.startsWith("#") || data.startsWith(" ") || (!data.startsWith("P")) || data.contains(" ") || data.equals("")) {
				continue;
			} else {
				String[] lineArguments = data.split(",");
				
				String pid = lineArguments[0].trim();
				int priority = 1;
				String scriptName = lineArguments[1].trim();
				
				if (lineArguments.length == 3) {
					priority = Integer.parseInt(lineArguments[1].trim());
					scriptName = lineArguments[2].trim();
				}
				
				ProcessControlBlock pcb = this.addToQueue(pid, priority, scriptName);
				pcb.setState("new");
				pcb.setArrivalTime(arrival);
				
				arrival++;
			}
		}
		
		scanner.close();
	}

	// change signature to return pcb so we can set arrivalTime
	public ProcessControlBlock addToQueue(String PID, int priority, String processPathFile) {
		long burstTime = generateBurst(processPathFile);
		ProcessControlBlock pcb = new ProcessControlBlock(PID, priority, burstTime, processPathFile);
		this.queue.add(pcb);
		return pcb;
	}

	/**
	 * Do not change this method for marking purpose
	 * generate Deterministic burst times (ms)
	 * @param PythonPathFile
	 * @return
	 */
	private long generateBurst(String PythonPathFile) {
		String name = new File(PythonPathFile).getName();

		switch (name) {
			case "process0.py":
				return 10;
			case "process1.py":
				return 30;
			case "process2.py":
				return 80;
			case "process3.py":
				return 130;
			case "process4.py":
				return 200;
			case "process5.py":
				return 300;
			default:
				// scale by file size, but clamp to avoid extreme delays
				long scriptSize = new File(PythonPathFile).length();
				long burst = (scriptSize / 50L) * 10L; // coarse scaling
				if (burst < 10)
					burst = 10;
				if (burst > 700)
					burst = 700;
				return burst;
		}
	}

	public Queue<ProcessControlBlock> getQueue() {
		return this.queue;
	}

}
