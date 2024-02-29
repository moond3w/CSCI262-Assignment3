## Setting up the Intrusion Detection System

Two text files are needed:
- `Events.txt`: This text file sets up the format of the events.
- `Stats.txt`: This text file sets up the distribution of the events.

## Running the IDS

To run the IDS Simulation, we have to set up the Java path in the command prompt:

- `path = C:\Program Files\Java\jdk-17\bin`: This path will differ according to your file location and version used.
- `set classpath=.`
- `javac *.java`: This will compile the java files in the folder if another version of java is used.

We then run the program through the following command:

- `java IDS Events.txt Stats.txt 10`

'10' represents the number of days the simulation will run to produce the expected baseline statistics of each event.
> The higher the number, the closer the value will be to the given distribution found in `Stats.txt`

## Alert Engine

The alert engine will prompt the user for a number of days and a file with the same format as Stats.txt from earlier but with different parameters for the events. 
The activity engine will produce data for the number of days specified and the alert engine will detect any anomalies by comparing the generated anomaly counter value with the threshold.

The threshold is as follows:
`2 × (𝑆𝑢𝑚𝑠 𝑜𝑓 𝑤𝑒𝑖𝑔ℎ𝑡𝑠)` 

The program will prompt for another file again until the user enters 'q' to end the program.
