package ProcessScheduler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * COM1032 Coursework Unit Tests
 * The following sample test cases are provided for you to verify your implementations.
 * Passing all test cases does not mean getting all marks. 
 * The provided test cases are just some examples for you to test your code, they are not complete test cases.
 * You can create your own test cases, but we won't use your test cases for marking.
 */
public class schedulerUnitTests {

    @TempDir
    Path tempDir;

    @BeforeEach
    void resetSystem() {
        Main.resetForTests();
    }

    /*
     * =========================================================
     * Helpers
     * =========================================================
     */

    private Path writeFile(String name, List<String> lines) {
        try {
            Path p = tempDir.resolve(name);
            Files.write(p, lines, StandardCharsets.UTF_8);
            return p;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Path writePython(String name, String content) {
        try {
            Path p = tempDir.resolve(name);
            Files.writeString(p, content, StandardCharsets.UTF_8);
            return p;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private EventLog runMain(Path input, String alg, long quantum) {
        assertTimeoutPreemptively(
                Duration.ofSeconds(5),
                () -> {
                    Main.initialise(new String[] {
                            input.toString(),
                            alg,
                            String.valueOf(quantum)
                    });
                    Main.finaliseThreads();
                });
        return Main.getLog();
    }

    private void assertCompletedUniqueAndTerminated(EventLog log, int expectedCount) {
        List<ProcessControlBlock> completed = log.getPCBLog();
        assertEquals(expectedCount, completed.size(), "Unexpected number of completed processes.");

        Set<String> unique = completed.stream()
                .map(ProcessControlBlock::getPID)
                .collect(Collectors.toSet());
        assertEquals(expectedCount, unique.size(), "Each PID should appear exactly once in EventLog.");

        assertTrue(completed.stream().allMatch(p -> "terminated".equals(p.getState())),
                "All completed processes must be in 'terminated' state.");
    }

    /*
     * =========================================================
     * Stage 0 (25%) — Run a single process 
     * =========================================================
     */

    @Test
    void stage0_test1_pcb_initialisation() {
        Path script = writePython("stage0_a.py", "print('Hello')\n");

        ProcessControlBlock pcb = Main.runStage0SingleScript(script.toString());

        assertEquals("P01", pcb.getPID());
        assertEquals(1, pcb.getPriority());
        assertEquals(0, pcb.getCPUBurstTime());
        assertEquals(script.toString(), pcb.getProcessPath());
        assertNotNull(pcb.getState());
    }

    @Test
    void stage0_test2_script_execution_and_output_capture() {
        Path script = writePython("stage0_b.py", "print('Sum is 9')\n");

        ProcessControlBlock pcb = Main.runStage0SingleScript(script.toString());

        assertNotNull(pcb.getPCBResult(), "PCB result should be stored.");
        assertEquals("Sum is 9", pcb.getPCBResult().trim(), "Captured stdout does not match expected output.");
    }

    @Test
    void stage0_test3_thread_handling_and_completion() throws InterruptedException {
        Path script = writePython("stage0_c.py", "print('OK')\n");

        ProcessControlBlock pcb = Main.runStage0SingleScript(script.toString());

        assertEquals("terminated", pcb.getState(), "PCB must be returned in terminated state.");
    }

    @Test
    void stage0_test4_generalisation_no_hardcoding() throws InterruptedException {
        Path script1 = writePython("stage0_d1.py", "print('Output A')\n");
        Path script2 = writePython("stage0_d2.py", "print('Output B')\n");

        ProcessControlBlock pcb1 = Main.runStage0SingleScript(script1.toString());
        ProcessControlBlock pcb2 = Main.runStage0SingleScript(script2.toString());

        assertEquals("Output A", pcb1.getPCBResult().trim());
        assertEquals("Output B", pcb2.getPCBResult().trim());
    }

    @Test
    @Timeout(2)
    void stage0_test5_robustness_no_hang_on_missing_script() {
        ProcessControlBlock pcb = Main.runStage0SingleScript(tempDir.resolve("missing.py").toString());
        assertNotNull(pcb);
        assertEquals("terminated", pcb.getState(), "Method must return and terminate even if script is missing.");
    }
    
    @Test
    void stage0_test6_script_execution_and_process_state_terminated() {
    	String script = "process4.py";
    	ProcessControlBlock pcb = Main.runStage0SingleScript(script);
    	
    	assertEquals("terminated", pcb.getState());
    	assertEquals('5', pcb.getPCBResult().charAt(17));
    }
    
    @Test
    void stage0_test7_script_not_existend() {
    	String script = "process14.py";
    	ProcessControlBlock pcb = Main.runStage0SingleScript(script);
    	
    	assertNotNull(pcb);
    	assertEquals("terminated", pcb.getState());
    }

    /*
     * =========================================================
     * Stage 1 (25%) — FCFS 
     * =========================================================
     */
    // parses valid lines + ignores comments/empty lines
    @Test
    void step1_jobqueue_parses_lines_and_ignores_comments(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("Input.txt");
        Files.write(input, List.of(
                "# comment",
                "",
                "P01,1,process1.py",
                "   ",
                "P02,7,process2.py"));

        JobQueue jq = new JobQueue();
        jq.readFile(input.toString());

        assertEquals(2, jq.getQueue().size());
        List<ProcessControlBlock> list = new ArrayList<>(jq.getQueue());

        assertEquals("P01", list.get(0).getPID());
        assertEquals(1, list.get(0).getPriority());
        assertEquals("process1.py", Paths.get(list.get(0).getProcessPath()).getFileName().toString());

        assertEquals("P02", list.get(1).getPID());
        assertEquals(7, list.get(1).getPriority());
    }

    // Stage 1 step 1 -- arrivalTime set to 0,1,2… in file order
    @Test
    void step1_jobqueue_arrival_times_follow_file_order(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("Input.txt");
        Files.write(input, List.of(
                "P10,3,process0.py",
                "P11,3,process1.py",
                "P12,3,process2.py"));

        JobQueue jq = new JobQueue();
        jq.readFile(input.toString());

        List<ProcessControlBlock> list = new ArrayList<>(jq.getQueue());

        assertEquals(0, list.get(0).getArrivalTime());
        assertEquals(1, list.get(1).getArrivalTime());
        assertEquals(2, list.get(2).getArrivalTime());
        assertEquals("new", list.get(0).getState());
    }

    // Step 2 Test 2A: moves all jobs to readyQueue and sets state="ready"
    @Test
    void step2_processcreator_moves_all_jobs_to_readyQueue() throws Exception {
        JobQueue jq = new JobQueue();
        Queue<ProcessControlBlock> ready = new LinkedList<>();

        ProcessControlBlock p1 = new ProcessControlBlock("P01", 1, 0, "process1.py");
        p1.setState("new");
        ProcessControlBlock p2 = new ProcessControlBlock("P02", 1, 1, "process2.py");
        p2.setState("new");

        jq.getQueue().add(p1);
        jq.getQueue().add(p2);

        ProcessCreator pc = new ProcessCreator(jq, ready);
        Thread creatorThread = new Thread(pc);
        creatorThread.start();
        creatorThread.join(300); // should finish quickly

        assertEquals(0, jq.getQueue().size());
        assertEquals(2, ready.size());

        for (ProcessControlBlock pcb : ready) {
            assertEquals("ready", pcb.getState());
        }
    }

    // Test 2B: order preserved when moving into readyQueue
    @Test
    void step2_processcreator_preserves_order() throws Exception {
        JobQueue jq = new JobQueue();
        Queue<ProcessControlBlock> ready = new LinkedList<>();

        jq.getQueue().add(new ProcessControlBlock("P03", 1, 0, "a.py"));
        jq.getQueue().add(new ProcessControlBlock("P01", 1, 1, "b.py"));

        ProcessCreator pc = new ProcessCreator(jq, ready);
        Thread creatorThread = new Thread(pc);
        creatorThread.start();
        creatorThread.join(300); // should finish quickly
        assertEquals("P03", ready.poll().getPID());
        assertEquals("P01", ready.poll().getPID());
    }

    // Step 3 Test 3A: Dispatcher stops if no work and ProcessCreator finished
    @Test
    void step3_dispatcher_stops_when_no_jobs_remain() throws Exception {
        Queue<ProcessControlBlock> ready = new LinkedList<>();
        JobQueue jq = new JobQueue(); // empty

        EventLog log = new EventLog();
        Scheduler scheduler = new Scheduler(ready, "FCFS", 0, log);

        ProcessCreator creator = new ProcessCreator(jq, ready);
        Thread creatorThread = new Thread(creator);
        creatorThread.start();
        creatorThread.join(300); // should finish quickly
        assertFalse(creatorThread.isAlive(), "ProcessCreator thread should finish when jobQueue is empty.");

        Dispatcher dispatcher = new Dispatcher(ready, scheduler, creator, jq);
        Thread dispatcherThread = new Thread(dispatcher);
        dispatcherThread.start();
        dispatcherThread.join(500);

        assertFalse(dispatcherThread.isAlive(), "Dispatcher should stop when no jobs remain.");
    }

    // Step 3 Test B — Dispatcher does not busy-spin (waits when queue is empty)
    @Test
    void step3_dispatcher_does_not_spin_when_readyQueue_empty() throws Exception {
        Queue<ProcessControlBlock> ready = new LinkedList<>();
        JobQueue jq = new JobQueue();
        EventLog log = new EventLog();

        CountingScheduler scheduler = new CountingScheduler(ready, "FCFS", 0, log);

        ProcessCreator creator = new ProcessCreator(jq, ready);
        Thread creatorThread = new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        creatorThread.start();

        Dispatcher dispatcher = new Dispatcher(ready, scheduler, creator, jq);
        Thread dispatcherThread = new Thread(dispatcher);
        dispatcherThread.start();

        Thread.sleep(150); // allow dispatcher to run/wait

        assertEquals(0, scheduler.calls, "Dispatcher should not call scheduler when readyQueue is empty.");

        // cleanup
        dispatcherThread.interrupt();
        creatorThread.interrupt();
    }

    // Test stage 1 step 4A: The completion order matches the input file order
    @Test
    void step4_fcfs_completion_order_matches_file_order() {
        Path p1 = writePython("process1.py", "print('Sum is 9')\n");
        Path p2 = writePython("process2.py", "print([10, 30, 50, 70])\n");
        Path p3 = writePython("process3.py", "print('HCF: 4, LCM: 576')\n");
        Path p0 = writePython("process0.py", "print('Sum is 5')\n");
        
        Path input = writeFile("fcfs_baseline.txt", List.of(
                "P01," + p1,
                "P02," + p2,
                "P03," + p3,
                "P04," + p0));
        
//        Path input2Test = writeFile("fcfs_baseline.txt", List.of(
//                "P01," + 0 + "," + p1,
//                "P02," + 0 + "," + p2,
//                "P03," + 0 + "," + p3,
//                "P04," + 0 + "," + p0));

        EventLog log = runMain(input, "FCFS", 0);

        assertCompletedUniqueAndTerminated(log, 4);

        List<String> order = log.getPCBLog().stream().map(ProcessControlBlock::getPID).toList();
        int size = log.getPCBLog().size();
        assertEquals(4, size);
        assertEquals(List.of("P01", "P02", "P03", "P04"), order,
                "FCFS must complete processes in arrival order.");
    }
    // Test stage 1 step 4 with empty input
    @Test
    void step4_empty_input_completes_cleanly() {
        Path input = writeFile("fcfs_empty.txt", List.of(
                "# no processes",
                "",
                "   # still no processes"));

        EventLog log = runMain(input, "FCFS", 0);
        assertEquals(0, log.getPCBLog().size(), "Empty input must produce an empty completion log.");
    }

    // Test stage 1 step 4: FCFS removes head and logs terminated PCB once
    @Test
    void step4_fcfs_runs_head_and_logs_once(@TempDir Path tempDir) throws Exception {
        // Create tiny python script
        Path script = tempDir.resolve("a.py");
        Files.writeString(script, "print('A')\n");

        Queue<ProcessControlBlock> ready = new LinkedList<>();
        ProcessControlBlock pcb = new ProcessControlBlock("P01", 1, 0, script.toString());
        pcb.setState("ready");
        ready.add(pcb);

        EventLog log = new EventLog();
        Scheduler scheduler = new Scheduler(ready, "FCFS", 0, log);

        scheduler.FCFS();

        assertEquals(0, ready.size());
        assertEquals(1, log.getPCBLog().size());
        assertEquals("P01", log.getPCBLog().get(0).getPID());
        assertEquals("terminated", log.getPCBLog().get(0).getState());
    }


    /*
     * =========================================================
     * Stage 2 (20%) — Round Robin (time quantum)
     * Tests required by brief:
     * 1) Time quantum enforcement
     * 2) Correct re-queuing
     * 3) Context switch increment
     * 4) Proper termination
     * 5) Correct final completion order
     * =========================================================
     */

    @Test
    void stage2_test1_time_quantum_enforcement() {
        // process2 -> burst 80 (see JobQueue burst mapping)
        Path longProc = writePython("process2.py", "print('LONG')\n");

        Path input = writeFile("rr_quantum.txt", List.of(
                "P01," + longProc));

        EventLog log = runMain(input, "RR", 40);

        ProcessControlBlock pcb = log.getPCBLog().get(0);
        assertEquals(80, pcb.getExecutionTime(),
                "Execution time should equal total CPU burst time after RR completion.");
        assertEquals(1, pcb.getContextSwitches(),
                "Burst 80 with quantum 40 must require 2 slices => 1 context switch.");
    }

    @Test
    void stage2_test2_correct_requeuing_unfinished_goes_to_end() {
        // Long P01 runs first slice and must be requeued to END,
        // allowing newly-arrived short P02 to run next and finish before P01.
        Path longProc = writePython("process2.py", "print('LONG')\n"); // burst 80
        Path shortProc = writePython("process0.py", "print('SHORT')\n"); // burst 10

        Path input = writeFile("rr_requeue.txt", List.of(
                "P01," + longProc, // arrival 0
                "P02," + shortProc // arrival 1 (becomes eligible while P01 runs first slice)
        ));

        EventLog log = runMain(input, "RR", 40);

        List<String> order = log.getPCBLog().stream().map(ProcessControlBlock::getPID).toList();
        assertEquals(List.of("P02", "P01"), order,
                "If unfinished processes are incorrectly requeued to the front, P01 would finish before P02.");
    }

    @Test
    void stage2_test3_context_switch_increment_only_on_preemption() {
        Path longProc = writePython("process2.py", "print('L')\n"); // burst 80
        Path shortProc = writePython("process1.py", "print('S')\n"); // burst 30

        Path input = writeFile("rr_ctx.txt", List.of(
                "P01," + longProc,
                "P02," + shortProc));

        EventLog log = runMain(input, "RR", 40);

        ProcessControlBlock p01 = log.getPCBLog().stream().filter(p -> p.getPID().equals("P01")).findFirst()
                .orElseThrow();
        ProcessControlBlock p02 = log.getPCBLog().stream().filter(p -> p.getPID().equals("P02")).findFirst()
                .orElseThrow();

        assertEquals(1, p01.getContextSwitches(), "P01 must be preempted once (80 with quantum 40).");
        assertEquals(0, p02.getContextSwitches(), "P02 must not be preempted (30 <= quantum 40).");
    }

    @Test
    void stage2_test4_correct_final_completion_order_example_case() {
        // With quantum 40 and arrivals 0,1,2:
        // P01 (burst 30) completes before others are eligible
        // Then P02 (burst 80) takes 1 slice, requeues; P03 (burst 10) completes; then
        // P02 completes.
        Path p1 = writePython("process1.py", "print('P1')\n"); // burst 30
        Path p2 = writePython("process2.py", "print('P2')\n"); // burst 80
        Path p0 = writePython("process0.py", "print('P0')\n"); // burst 10

        Path input = writeFile("rr_order.txt", List.of(
                "P01," + p1,
                "P02," + p2,
                "P03," + p0));

        EventLog log = runMain(input, "RR", 40);

        List<String> order = log.getPCBLog().stream().map(ProcessControlBlock::getPID).toList();
        assertEquals(List.of("P01", "P03", "P02"), order);
    }
    
    /*
     * =========================================================
     * Stage 3 (10%) — Non-preemptive Priority 
     * =========================================================
     */

    @Test
    void stage3_test1_select_highest_priority_next() {
        Path low = writePython("low.py", "print('LOW')\n");
        Path high = writePython("high.py", "print('HIGH')\n");

        // Both arrive early; scheduler should pick highest priority first.
        Path input = writeFile("prio_select.txt", List.of(
                "P01,1," + low,
                "P02,9," + high));

        EventLog log = runMain(input, "PRIORITY", 0);

        assertCompletedUniqueAndTerminated(log, 2);
        List<String> order = log.getPCBLog().stream().map(ProcessControlBlock::getPID).toList();
        assertEquals(List.of("P01", "P02"), order, "P01 arrives before P02, and gets execution first.");
    }

    @Test
    void stage3_test2_tie_break_by_arrival_time() {
        Path a = writePython("a.py", "print('A')\n");
        Path b = writePython("b.py", "print('B')\n");

        // Same priority: must pick earlier arrival (file order) first.
        Path input = writeFile("prio_tie.txt", List.of(
                "P10,5," + a, // arrival 0
                "P11,5," + b // arrival 1
        ));

        EventLog log = runMain(input, "PRIORITY", 0);

        List<String> order = log.getPCBLog().stream().map(ProcessControlBlock::getPID).toList();
        assertEquals(List.of("P10", "P11"), order, "Tie-break must respect arrivalTime (file order).");
    }
    /*
     * =========================================================
     * Use a stub Scheduler that counts calls
     * =========================================================
     */

    static class CountingScheduler extends Scheduler {
        int calls = 0;

        CountingScheduler(Queue<ProcessControlBlock> rq, String alg, long tq, EventLog log) {
            super(rq, alg, tq, log);
        }

        @Override
        public int runAlgorithm() {
            calls++;
            // simulate: remove one item so Dispatcher can terminate
            synchronized (readyQueue) {
                if (!readyQueue.isEmpty())
                    readyQueue.poll();
                readyQueue.notifyAll();
            }
            return 1;
        }
    }

}
