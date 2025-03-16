import java.util.*;

// Process class to store process attributes
class Process {
    String pid; // Process ID (e.g., P1, P2)
    int arrivalTime; // Time when process arrives in ready queue
    int burstTime; // Total CPU time required by the process
    int remainingTime; // Remaining CPU time (used for SRT and RR)
    int waitingTime; // Time spent waiting in ready queue
    int turnaroundTime; // Total time from arrival to completion

    // Constructor to initialize process attributes
    public Process(String pid, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime; // Initially, remaining time equals burst time
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }
}

class CPUScheduler {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // Main loop to keep the program running until user chooses to exit
        while (true) {
            displayMenu(); // Show the menu options
            int choice = getValidInput(sc, 1, 5); // Get user's choice with input validation
            if (choice == 5) break; // Exit the program if user selects 5

            // Input the number of processes
            System.out.print("Enter number of processes: ");
            int n = getValidInput(sc, 1, Integer.MAX_VALUE);
            List<Process> processes = inputProcesses(sc, n); // Collect process details

            // Execute the selected scheduling algorithm
            switch (choice) {
                case 1: fcfs(processes); break; // First-Come, First-Served
                case 2: sjf(processes); break; // Shortest-Job-First (non-preemptive)
                case 3: srt(processes); break; // Shortest-Remaining-Time (preemptive)
                case 4:
                    System.out.print("Enter time quantum: ");
                    int quantum = getValidInput(sc, 1, Integer.MAX_VALUE);
                    roundRobin(processes, quantum); // Round Robin with time quantum
                    break;
            }
            displayResults(processes); // Show waiting and turnaround times
        }
        sc.close(); // Close the scanner to prevent resource leaks
    }

    // Displays the menu options to the user
    static void displayMenu() {
        System.out.println("\nCPU Scheduling Simulator");
        System.out.println("1. First-Come, First-Served (FCFS)");
        System.out.println("2. Shortest-Job-First (SJF)");
        System.out.println("3. Shortest-Remaining-Time (SRT)");
        System.out.println("4. Round Robin (RR)");
        System.out.println("5. Exit");
        System.out.print("Select an option: ");
    }

    // Ensures valid integer input within a specified range
    static int getValidInput(Scanner sc, int min, int max) {
        while (true) {
            try {
                int input = sc.nextInt();
                if (input >= min && input <= max) return input;
                System.out.print("Invalid input. Enter a number between " + min + " and " + max + ": ");
            } catch (Exception e) {
                System.out.print("Invalid input. Enter an integer: ");
                sc.next(); // Clear invalid input
            }
        }
    }

    // Collects process details (PID, arrival time, burst time) from user
    static List<Process> inputProcesses(Scanner sc, int n) {
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            System.out.println("\nProcess " + (i + 1) + ":");
            System.out.print("Enter Process ID (e.g., P1): ");
            String pid = sc.next();
            System.out.print("Enter Arrival Time: ");
            int arrival = getValidInput(sc, 0, Integer.MAX_VALUE); // Arrival time must be non-negative
            System.out.print("Enter Burst Time: ");
            int burst = getValidInput(sc, 1, Integer.MAX_VALUE); // Burst time must be positive
            processes.add(new Process(pid, arrival, burst));
        }
        return processes;
    }

    // FCFS: Executes processes in the order of their arrival (non-preemptive)
    static void fcfs(List<Process> processes) {
        // Sort processes by arrival time to ensure FCFS order
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0; // Tracks the current time in the CPU schedule
        System.out.println("\nGantt Chart:");
        for (Process p : processes) {
            // If CPU is idle (current time < arrival), fast-forward to arrival
            if (currentTime < p.arrivalTime) currentTime = p.arrivalTime;
            System.out.print("[" + currentTime + "] " + p.pid + " "); // Print Gantt chart entry
            p.waitingTime = currentTime - p.arrivalTime; // Waiting = start time - arrival
            currentTime += p.burstTime; // Add burst time to current time
            p.turnaroundTime = currentTime - p.arrivalTime; // Turnaround = completion - arrival
        }
        System.out.println("[" + currentTime + "]"); // End of Gantt chart
    }

    // SJF: Executes the process with the shortest burst time first (non-preemptive)
    static void sjf(List<Process> processes) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime)); // Sort by arrival initially
        int currentTime = 0, completed = 0; // Track time and number of completed processes
        List<Process> ready = new ArrayList<>(); // Ready queue for processes that have arrived
        System.out.println("\nGantt Chart:");
        while (completed < processes.size()) {
            // Add processes to ready queue if they have arrived
            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && p.burstTime > 0 && !ready.contains(p)) {
                    ready.add(p);
                }
            }
            // If no processes are ready, increment time and continue
            if (ready.isEmpty()) {
                currentTime++;
                continue;
            }
            // Sort ready queue by burst time to pick the shortest job
            ready.sort(Comparator.comparingInt(p -> p.burstTime));
            Process p = ready.remove(0); // Pick the process with shortest burst
            System.out.print("[" + currentTime + "] " + p.pid + " "); // Print Gantt chart entry
            p.waitingTime = currentTime - p.arrivalTime; // Calculate waiting time
            currentTime += p.burstTime; // Add burst time to current time
            p.turnaroundTime = currentTime - p.arrivalTime; // Calculate turnaround time
            p.burstTime = 0; // Mark process as completed (burst time = 0)
            completed++;
        }
        System.out.println("[" + currentTime + "]"); // End of Gantt chart
    }

    // SRT: Preemptive version of SJF, selects process with shortest remaining time
    static void srt(List<Process> processes) {
        int currentTime = 0, completed = 0; // Track time and completed processes
        System.out.println("\nGantt Chart:");
        while (completed < processes.size()) {
            Process shortest = null; // Process with shortest remaining time
            // Find the process with the shortest remaining time among arrived processes
            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                    if (shortest == null || p.remainingTime < shortest.remainingTime) {
                        shortest = p;
                    }
                }
            }
            // If no process is ready, increment time and continue
            if (shortest == null) {
                currentTime++;
                continue;
            }
            System.out.print("[" + currentTime + "] " + shortest.pid + " "); // Print Gantt chart entry
            shortest.remainingTime--; // Decrease remaining time by 1 (preemptive)
            currentTime++; // Increment current time
            // If process is completed, calculate its metrics
            if (shortest.remainingTime == 0) {
                shortest.turnaroundTime = currentTime - shortest.arrivalTime;
                shortest.waitingTime = shortest.turnaroundTime - shortest.burstTime;
                completed++;
            }
        }
        System.out.println("[" + currentTime + "]"); // End of Gantt chart
    }

    // RR: Executes processes in a circular queue with a fixed time quantum
    static void roundRobin(List<Process> processes, int quantum) {
        Queue<Process> queue = new LinkedList<>(); // Queue for RR scheduling
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime)); // Sort by arrival
        int currentTime = 0; // Track current time
        System.out.println("\nGantt Chart:");
        int i = 0; // Index to track processes yet to arrive
        // Continue until all processes are completed
        while (!queue.isEmpty() || i < processes.size()) {
            // Add newly arrived processes to the queue
            while (i < processes.size() && processes.get(i).arrivalTime <= currentTime) {
                queue.add(processes.get(i++));
            }
            // If queue is empty, increment time and continue
            if (queue.isEmpty()) {
                currentTime++;
                continue;
            }
            Process p = queue.poll(); // Get the next process from the queue
            System.out.print("[" + currentTime + "] " + p.pid + " "); // Print Gantt chart entry
            // Determine the time slice (quantum or remaining time, whichever is smaller)
            int timeSlice = Math.min(quantum, p.remainingTime);
            p.remainingTime -= timeSlice; // Reduce remaining time
            currentTime += timeSlice; // Increment current time
            // Add any new processes that arrived during this time slice
            while (i < processes.size() && processes.get(i).arrivalTime <= currentTime) {
                queue.add(processes.get(i++));
            }
            // If process is not finished, add it back to the queue
            if (p.remainingTime > 0) {
                queue.add(p);
            } else {
                // If process is finished, calculate its metrics
                p.turnaroundTime = currentTime - p.arrivalTime;
                p.waitingTime = p.turnaroundTime - p.burstTime;
            }
        }
        System.out.println("[" + currentTime + "]"); // End of Gantt chart
    }

    // Displays the waiting and turnaround times for each process, along with averages
    static void displayResults(List<Process> processes) {
        System.out.println("\nProcess\tWaiting Time\tTurnaround Time");
        double avgWaiting = 0, avgTurnaround = 0;
        for (Process p : processes) {
            System.out.printf("%s\t%d\t\t%d\n", p.pid, p.waitingTime, p.turnaroundTime);
            avgWaiting += p.waitingTime;
            avgTurnaround += p.turnaroundTime;
        }
        // Calculate averages
        avgWaiting /= processes.size();
        avgTurnaround /= processes.size();
        System.out.printf("\nAverage Waiting Time: %.2f\n", avgWaiting);
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
    }
}