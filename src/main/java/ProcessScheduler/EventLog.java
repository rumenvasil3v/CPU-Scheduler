package ProcessScheduler;

import java.util.ArrayList;

/**
 * Defines a Log object that stores an array of PCB objects 
 * as they were created and an array containing the processor output.
 * You are not required to modify this class for the coursework
 */
public class EventLog {
	/**
	 * Completion Log. Stores the output strings in an array of the processor as it completes a process.
	 */
//	private ArrayList<String> completionLog = null; 
	
	/**
	 * PCB Log. Stores an array of PCB as the are created. Used in testing for calculating number of context switches.
	 */
	private ArrayList<ProcessControlBlock> PCBLog = null;
	
	public EventLog() {
		this.PCBLog = new ArrayList<ProcessControlBlock>();
	}
	
	public ArrayList<ProcessControlBlock> getPCBLog(){
		return this.PCBLog;
	}
	
	public void addPCB(ProcessControlBlock PCB) {
		this.PCBLog.add(PCB);
	}
}
