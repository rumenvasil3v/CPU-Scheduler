README.md — Structured Reflection Template
--------------------------------------------

The badge below indicates if your submission compiled correctly. It may take some time to update. You can click the badge to see the jobs page and failure traces.

You must ensure your project compiles correctly.

[![Pipeline status](https://gitlab.surrey.ac.uk/csee/com1032/2025-26/com1032rv00349/badges/main/pipeline.svg)](https://gitlab.surrey.ac.uk/csee/com1032/2025-26/com1032rv00349/-/jobs/)

For each stage (0–3), complete the table below.
* 50 to 100 words per stage.
* Bullet points only.

Stage 0 (5%)
--------------
- **Key Implementation** - implemented two methods responsible for creating process context (PCB) with provided data and then running the CPU thread run() method that executes that process utilizing ProcessBuilder to create the process and then start() it. After the process finishes I am setting the output via the PCB and then I am returning it. After that, I implemented two tests to verify the methods behave as intented.
- **One Bug Encountered** - the program couldn't find the location of the processes resulting in throwing IOException       
- **How You Diagnosed It** - tried approaches to run the scripts using the command line by providing the absolute paths of the python.exe and the scipt I wanted to run. Printed the current directory I am working in and tried to run the same command using the relative paths
- **What You Changed** - create a new variable that is *String pythonPathWindows = "python.exe"*, providing that in the ProcessBuilder constructor as the command and then the relative path of the process -> *new ProcessBuilder(Main.pythonPathWindows, PCB.getProcessPath)*


Stage 1 — FCFS (5%)
----------------------
- **Key Implementation** - Implemented functionality to read Proceses from a text file and then for each process creating PCB instance and adding it to the jobQueue as part of the JobQueue class. After that, I implemented the functionality to move PCB's from the jobQueue in the readyQueue as part of the ProcessCreator class and the Dispatcher that dispatches one process at a time from the readyQueue. Finally, in the Scheduler class I implemented the FCFS scheduling algorithm.
- **One Bug Encountered** - I had issue connected with two of the test cases from step4 which are -> step4_empty_input_completes_cleanly() and step4_fcfs_completion_order_matches_file_order().
- **How You Diagnosed It** - Analyzed the test cases to understand what they are verifying and then tweaked them to observe the underlying problem which was coming from the JobQueue class.
- **What You Changed** - The first problem was connected with that when I read file, I read line that starts with a space, so I just added a condition in the run() method in the JobQueue class that checks if the line starts with empty space (" ") or if it is different that the starting character 'P' which is for process and then I fixed that issue. The second one was concerned with that the test case did not provide priority and when I read the file I was not assigned the line arguments when reading new line from the file correctly. I adapted the code in the run() method in the JobQueue class so when there are only the PID and the scriptName to assign them correctly when creating new PCB and passing default priority -> 1 in my case.


Stage 2 — Round Robin (5%)
-----------------------------
- **Key Implementation** - Implemented the RoundRobin scheduling algorithm in the Scheduler class, which is an algorithm that specifies a specific time for each process to execute and in that way providing fair share of CPU utilization of each process, preventing starvation.
- **One Bug Encountered** - Had problems with synchronization and did not enforce time quantum, because I did not simulate it correctly. The algorithm ran in a loop while executing each process while it had to execute only one that is directed by the Dispatcher.
- **How You Diagnosed It** - Returned back to the previous stage to make sure I have implemented the ProcessCreator and the Dispatcher correctly and tried a lot of different scenarios and approaches to synchronize the access to the readyQueue. Did some research on the internet to understand how can I synchronize specific block of code only and in that case block of code involving the readyQueue.
- **What You Changed** - Made synchronization block in the ProcessCreator class when adding PCB in it and to notify and wake up the Dispatcher thread I made notifyAll() to the *eadyQueue -> *readyQueue.notifyAll()*, so in that way the Dipatcher can take control and start dispatching processes to the scheduling algorithm. Added synchronization block in the Dispatcher class as well when accessing the readyQueue to check if it isEmpty(). Moreover, specifically in the RR() method I removed the while loop, so one process can execute in a time and used the remainingBurstTime and the timeQuantum to simulate process execution for that time quantum.

Stage 3 — Priority (5%)
-------------------------------
- **Key Implementation** - Implemented the Non-Preemptive Priority Scheduling algoritm, which is an algorithm that executes the process with highest priority and if even at that time it arrives another process with higher priority, it won't be executed imediately and that running process will finish its job (the meaning of non-preemptive)
- **One Bug Encountered** - I did not have any issues with that Stage. However, when I ran the tests I had problem with Stage 2 again and I did not check if the remainingCPUBurstTime is <= 0 in order to complete the process and add it to the log which led to not executing process correctly and adding them in the wrong order in the log.
- **How You Diagnosed It** - Analyzed the RR() method, instead of using the remainingBurstTime I tried to use the CPUBurstTime and ran the program to check for that approach.	
- **What You Changed** - Added condition if the remainingCPUBurstTime is <= 0 which fixed the issue and completed and added processes in the log correctly.
