// To run the file: java .\src\IDS.java Events.txt Stats.txt 4

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.IntStream;
import java.util.Random;
import java.util.ArrayList;

public class IDS {
    // Declaration for file IO
    private static Scanner input;
    private static Formatter output;

    // Declaration for values recorded
    private static int noOfEvents, noOfStats, days, threshold;
    private static String[] eventName, eventType;
    private static int[] eventMin, eventMax, eventWeight;
    private static double[] eventMean, eventStd, actualMean, actualStd, newEventMean, newEventStd;

    // Declaration to store log event data
    private static ArrayList<ArrayList<Double>> baseline = new ArrayList<ArrayList<Double>>(noOfEvents);
    private static ArrayList<ArrayList<Double>> detectData;
    private static Scanner userInput = new Scanner(System.in);

    private static void openFile(String fileName)
	{
		try
		{
			input = new Scanner(Paths.get (fileName));
		}
		catch (IOException e)
		{
			System.out.println("Error in opening the file");
			System.exit(1);
		}
		
		System.out.println(fileName + " successfully opened for processing");

	}
	
	private static void processEventsFile(String fileName) throws InconsistentInputException
	{
        String line;
        int i = 0;

		try
		{
			while (input.hasNext())
			{
				// Retrieve line
                line = input.nextLine();

                // Process line
                if (i == 0)   // get number of events monitored
                {
                    noOfEvents = Integer.parseInt(line);
                    // Display number of events being read
                    System.out.println(noOfEvents + " events detected.");
                    eventName = new String[noOfEvents];
                    eventType = new String[noOfEvents];
                    eventMin = new int[noOfEvents];
                    eventMax = new int[noOfEvents];
                    eventWeight = new int[noOfEvents];
                    eventMean = new double[noOfEvents];
                    eventStd = new double[noOfEvents];
                }
                else           // events monitored
				{
                    String[] eventList = line.split(":");
                    eventName[i-1] = eventList[0];
                    eventType[i-1] = eventList[1];
                    eventMin[i-1] = Integer.parseInt(eventList[2]);
                    // If no maximum value
                    if (eventList[3].equals(""))
                    {
                        eventMax[i-1] = Integer.MAX_VALUE;
                    }
                    else // maximum value exists
                    {
                        eventMax[i-1] = Integer.parseInt(eventList[3]);
                    }
                    eventWeight[i-1] = Integer.parseInt(eventList[4]);

                    System.out.println(eventName[i-1] + " saved.");
                }

                // Next iterator
                i++; 
			}

            // Check if all events are accounted for
            if (i <= noOfEvents)
            {
                throw new InconsistentInputException("Number of events read do not match! Expected: " + noOfEvents + " Actual: " + (i-1));
            }

            System.out.println(fileName + " has been processed successfully.");
		}
		catch (NoSuchElementException e)
		{
			System.out.println ("File was not properly formed");
			System.exit (1);
		}
		catch (IllegalStateException e)
		{
			System.out.println ("Error in reading of file");
			System.exit (1);
		}
	}
	
    private static void processStatsFile(String fileName, double[] mean, double[] std) throws InconsistentInputException
	{
        String line;
        int i = 0;
		try
		{
			while (input.hasNext())
			{
				// Retrieve line
                line = input.nextLine();

                // Process line
                if (i == 0)   // get number of events monitored
                {
                    noOfStats = Integer.parseInt(line);
                    if (noOfStats != noOfEvents)
                    {
                        // Throw exception if number of stats and events do not match
                        throw new InconsistentInputException("Number of events do not match! Expected: " + noOfEvents + " Actual: " + noOfStats);
                    }
                }
                else           // events monitored
				{
                    String[] eventList = line.split(":");
                    // Stats name match event name
                    if (eventName[i-1].equals(eventList[0]))
                    {
                        mean[i-1] = Double.parseDouble(eventList[1]);
                        std[i-1] = Double.parseDouble(eventList[2]);
                    }
                    else
                    {
                        // Throw exception if event name does not match in both files
                        throw new InconsistentInputException("Event names do not match! Expected: " + eventName[i-1] + " Actual: " + eventList[0]);
                    }
                }

                // Next iterator
                i++;
			}

            // Check if all events are accounted for
            if (i <= noOfStats)
            {
                throw new InconsistentInputException("Number of events read do not match! Expected: " + noOfStats + " Actual: " + (i-1));
            }

            System.out.println(fileName + " has been processed successfully.");
		}
		catch (NoSuchElementException e)
		{
			System.out.println ("File was not properly formed");
			System.exit (1);
		}
		catch (IllegalStateException e)
		{
			System.out.println ("Error in reading of file");
			System.exit (1);
		}
	}

	private static void closeFile(String fileName)
	{
		if (input != null)
		{
			input.close();
			System.out.println (fileName + " successfully closed for processing");
		}
	}

    // Generate value based on mean and standard deviation
    private static double generateValue(double mean, double stdev, int min, int max)
    {
        Random r = new Random();
        double value = r.nextGaussian() * stdev + mean;

        // Handle scenario where value exceeds range
        if (value < min) {
            value = min;
        }
        else if (value > max) {
            value = max;
        }

        return value;
    }

    private static void openWriteFile(String fileName)
	{
		try
		{
			output = new Formatter (fileName);
		}
		catch (SecurityException e)
		{
			System.out.println ("Write permission denied");
			System.exit (1);
		}
		catch (FileNotFoundException e)
		{
			System.out.println ("Error in opening the file");
		}
		
		System.out.println (fileName + " successsfully opened for creation.");
	}
	
    // Create log event file for baseline statistics
	private static void writeLogEventFile(String fileName, int days, double[] mean, double[] std)
	{
		try
		{
			for (int i = 0; i < days; i++)
            {
                int dVal = -1; // Impossible value
                
                // Indicate new day
                output.format("Day:%d%n", i+1);

                for (int j = 0; j < noOfEvents; j++)
                {
                    // Write name to file
                    output.format(eventName[j] + ":");

                    // Generate values
                    double value = generateValue(mean[j], std[j], eventMin[j], eventMax[j]);

                    // Determine continuous or discreet and save value
                    if (eventType[j].equals("D"))
                    {
                        dVal = (int)Math.round(value);
                        output.format("%d%n", dVal);
                    }
                    else if (eventType[j].equals("C"))
                    {
                        output.format("%.2f%n", value);
                    }
                }
                output.format("%n");
            }
            System.out.println (fileName + " successfully generated.");
		}
		catch (FormatterClosedException e)
		{
			System.out.println ("Error in writing to file");
			System.exit (1);
		}
	}
	
	private static void closeWriteFile(String fileName)
	{
		if (output != null)
		{
			output.close ();
			System.out.println (fileName + " successfully created.");
		}
	}

    // Read Log Event File to process for Analysis Engine
    private static void readLogEventFile(String fileName, ArrayList<ArrayList<Double>> arr)
    {
        String line;
        int i = 0;

        // Initialize baseline arraylist
        for (int j = 0; j < noOfEvents; j++)
        {
            arr.add(new ArrayList<Double>());
        }

		try
		{
			while (input.hasNext())
			{
				// Retrieve line
                line = input.nextLine();
                
                // Ignore readability buffers
                if (!line.equals(""))
                {
                    String[] eventList = line.split(":"); // should split into element:output

                    // Ignore days - irrelevant except for readability
                    if (!eventList[0].equals("Day"))
                    {
                        // Store event statistic in appropriate arraylist index
                        Double value = Double.parseDouble(eventList[1]);
                        arr.get(i).add(value);

                        // move to next event or reset
                        i++;
                        i %= noOfEvents; // i = index associated with the object in arrays
                    }
                }
			}

            System.out.println(fileName + " data recorded successfully.");
		}
		catch (NoSuchElementException e)
		{
			System.out.println ("File was not properly formed");
			System.exit (1);
		}
		catch (IllegalStateException e)
		{
			System.out.println ("Error in reading of file");
			System.exit (1);
		}
    }
    
    // Calculate mean of values in an arraylist
    private static double calcMean(ArrayList<Double> arr)
    {
        double total = 0.0;
        for (Double val : arr)
        {
            total += val;
        }
        return total/arr.size();
    }
    
    // Calculate standard deviation of values in an arraylist
    private static double calcStd(ArrayList<Double> arr, double mean)
    {
        // Calculate variance
        double variance = 0;
        for (Double val : arr) {
            variance += Math.pow(val - mean, 2);
        }
        variance /= arr.size();

        // Get standard deviation
        double std = Math.sqrt(variance);

        return std;
    }

    // Write statistics to logfile
    private static void writeAnalysisFile(String fileName, ArrayList<ArrayList<Double>> arr)
    {
        // Create mean and std
        actualMean = new double[noOfEvents];
        actualStd = new double[noOfEvents];
        
        try
		{
			output.format("======== BASELINE ANALYSIS ========%n");
            int i = 0;

            // Generate total statistics
            for (ArrayList<Double> event : arr)
            {
                double mean = calcMean(event);
                double std = calcStd(event, mean);

                // Store in array
                actualMean[i] = mean;
                actualStd[i] = std;

                // Write to file
                output.format("%s Mean : %.2f%n", eventName[i], mean);
                output.format("%s Std  : %.2f%n%n", eventName[i], std);

                i++;
            }

            // Produce daily totals
            output.format("%n======== DAILY TOTALS ========%n");
            for (int j = 0; j < days; j++)
            {
                double total = 0.0;
                for (ArrayList<Double> event : arr)
                {
                    total += event.get(j);
                }

                output.format("Day %d Total : %.2f%n", (j+1), total);
            }

            System.out.println(fileName + " successfully generated.");
		}
		catch (FormatterClosedException e)
		{
			System.out.println ("Error in writing to file");
			System.exit (1);
		}
        
    }

    // Check if fileName is a valid filename
    private static boolean validFile(String fileName)
    {
        return (fileName.endsWith(".txt"));
    }

    // Determine if a given string only contains numbers
    public static boolean isNumeric(String str) 
    {
        try 
        {
            if (str.contains(".")) 
            {
                Double.parseDouble(str);
            }
            else
            {
                Integer.parseInt(str);
            }
            return true;
        } 
        catch(NumberFormatException e)
        {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        // ============== Setting Up System =================
        String events = args[0];    // Get events.txt
        openFile(events);
        try
        {
            processEventsFile(events);
        }
        catch (InconsistentInputException e) {
            System.out.printf ("==> ERROR: %s%n", e);
            System.exit(1);
        }
        closeFile(events);

        System.out.println("");

        // Calculate threshold based on event weight
        threshold = (IntStream.of(eventWeight).sum()) * 2;

        String stats = args[1]; // Get stats.txt
        openFile(stats);
        try {
            processStatsFile(stats, eventMean, eventStd);
        }
        catch (InconsistentInputException e) {
            System.out.printf ("==> ERROR: %s%n", e);
            System.exit(1);
        }
        closeFile(stats);

        System.out.println("\nSetup Completed!\n");

        days = Integer.parseInt(args[2]);   // Get number of days

        // ============== Activity Simulation =================
        // Create log file
        openWriteFile("BaselineLog.txt");
        writeLogEventFile("BaselineLog.txt", days, eventMean, eventStd);
        closeWriteFile("BaselineLog.txt");

        System.out.println("\nEvent Generation Completed!\n");

        // ============== Analysis Engine =================
        // Read log baseline file
        openFile("BaselineLog.txt");
        readLogEventFile("BaselineLog.txt", baseline);
        closeFile("BaselineLog.txt");

        // Create analysis file based on baseline
        openWriteFile("BaselineAnalysis.txt");
        writeAnalysisFile("BaselineAnalysis.txt", baseline);
        closeWriteFile("BaselineAnalysis.txt");
        //appendLogEventFile("BaselineLog.txt");

        System.out.println("\nAnalysis Completed!\n");

        // ============== Alert Engine =================
        boolean programRun = true;

        while (programRun)
        {
            String newStatsFile = "";
            boolean validInput = false;
            int newDays = -1; //impossible val
            detectData = new ArrayList<ArrayList<Double>>(noOfEvents);

            // Retrieve new stats name
            while (!validInput)
            {
                System.out.print("Enter new statistics file or enter 'q' to quit: ");
                newStatsFile = userInput.nextLine();

                if (validFile(newStatsFile))
                {
                    validInput = true;
                }
                else if (newStatsFile.equals("q"))
                {
                    programRun = false;
                    validInput = true;
                    System.out.println("Ending program...");
                }
                else 
                {
                    System.out.println("Invalid filename entered!");
                }
            }

            // End program if user quits
            if (!programRun)
            {
                break;
            }

            // Reset valid input and retrieve new number of days
            validInput = false;
            while (!validInput)
            {
                System.out.print("Enter number of days to simulate: ");
                String newDaysInput = userInput.nextLine();

                if (isNumeric(newDaysInput))
                {
                    validInput = true;
                    newDays = Integer.parseInt(newDaysInput);
                }
                else {
                    System.out.println("Invalid number of days entered!");
                }
            }

            System.out.println("");

            // Generate new statistics
            newEventMean = new double[noOfEvents];
            newEventStd = new double[noOfEvents];

            openFile(newStatsFile);
            try {
                processStatsFile(newStatsFile, newEventMean, newEventStd);
            }
            catch (InconsistentInputException e) {
                System.out.printf ("==> ERROR: %s%n", e);
                System.exit(1);
            }
            closeFile(newStatsFile);
            
            // Create new log file
            openWriteFile("EventLog.txt");
            writeLogEventFile("EventLog.txt", newDays, newEventMean, newEventStd);
            closeWriteFile("EventLog.txt");

            System.out.println("\nNew data generated!\n");

            // Read log actual file
            openFile("EventLog.txt");
            readLogEventFile("EventLog.txt", detectData);
            closeFile("EventLog.txt");

            System.out.println("\nNew logs generated!\n");

            // Determine daily output
            for (int i = 0; i < newDays; i++)
            {
                double val = -1.0;
                double anomalyCounter = 0.0;

                for (int j = 0; j < noOfEvents; j++)
                {
                    // Get every event on the i-th day and compute anomaly
                    val = detectData.get(j).get(i);
                    double distFromStd = (Math.abs(val - actualMean[j])) / actualStd[j];
                    anomalyCounter += distFromStd * eventWeight[j];
                }

                System.out.println("Day " + (i+1));
                System.out.println("Threshold: " + threshold);
                System.out.printf("Anomaly Counter: %.2f%n", anomalyCounter);
                boolean alert = anomalyCounter>threshold;
                if (alert)
                {
                    System.out.println("ALERT! VALUE OVER THRESHOLD DETECTED!");
                }
                else
                {
                    System.out.println("No anomaly detected.");
                }
                System.out.println("");
            }
        }
    }
}

// Custom Exception to handle input between Events.txt and Stats.txt
class InconsistentInputException extends Exception
{
	private String message;
	
	public InconsistentInputException()
	{
		message = "Inconsistent input detected!";
	}
	
	public InconsistentInputException(String message)
	{
		this.message = message;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	@Override
	public String toString()
	{
		return message;
	}
}