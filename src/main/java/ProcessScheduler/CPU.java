package ProcessScheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * CPU Thread. Runs a python script from a given PCB.
 *
 */
public class CPU extends Thread {

	/**
	 * Instance fields used in constructor for parsing in other objects
	 */
	private boolean running = true; // if CPU is actively executing a process
	private ProcessControlBlock PCB = null;

	public CPU(ProcessControlBlock PCB) {
		this.PCB = PCB;
	}

	/**
	 * TO DO for Stage 0
	 */
	@Override
	public void run() {

		// Print that the process is running
		this.PCB.setState("running");
		System.out.println(this.PCB.getPID() + ": " + this.PCB.getState().toUpperCase().charAt(0) + this.PCB.getState().substring(1));

		ProcessBuilder processBuilder = new ProcessBuilder(Main.pythonPathWindows, PCB.getProcessPath());

    	// processBuilder.directory(new File("C:\\\\Users\\\\Rumen\\\\Documents\\\\com1032rv00349"));
    	// System.out.println("Process: " + processBuilder.command());
    	// System.out.println("PB working directory: " + processBuilder.directory());
    	// System.out.println("Current working directory: " + System.getProperty("user.dir"));

		try {
			Process pythonProcess = processBuilder.start();

			// Wait for the script to finish execution
			pythonProcess.waitFor();

			// Read the script output
			InputStream in = pythonProcess.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			String output = "";
			String line;
			while ((line = reader.readLine()) != null) {
				output += line + "\n";
			}

			output.trim();

			// Update the PCB with the script's output
			this.PCB.setState("terminated");
			this.PCB.setPCBResult(output);

			// Print the output of the process
//			System.out.println(PCB.getPID() + ": Output: " + PCB.getPCBResult());
		} catch (IOException | InterruptedException e) {
			System.out.println("IOException occurred");
			e.printStackTrace();
		}
	}

	public ProcessControlBlock getPCB() {
		return this.PCB;
	}

	public boolean getRunning() {
		return this.running;
	}

}
