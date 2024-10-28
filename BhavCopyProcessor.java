package com.tech;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class BhavCopyProcessor {
	private static List<String[]> bhavData = new ArrayList<>();
	private static Map<String, String[]> bhavMap = new HashMap<>();

	public static void main(String[] args) throws IOException {
		// Use fixed file path
//	        String filePath = "C:/Users/Merceadm/Documents/sec_bhavdata_full_22102024.csv";
		String filePath = "D:\\New folder\\bhavcopy.csv";

		// Load the BhavCopy file into memory
		Files.lines(Paths.get(filePath)).skip(1).forEach(line -> {
			// Skip empty or malformed lines
			if (line.trim().isEmpty())
				return;

			String[] columns = line.split(",");
			if (columns.length > 0) { // Ensure there is data in the line
				bhavData.add(columns);
				bhavMap.put(columns[0], columns); // SYMBOL as key
			}
		});

		// Command Loop
		Scanner scanner = new Scanner(System.in);
		while (true) {
			String input = scanner.nextLine().trim();
			if (input.equalsIgnoreCase("quit"))
				break;
			processCommand(input);
		}
		scanner.close();
	}

	// Process user commands
	private static void processCommand(String input) {
		String[] tokens = input.split(" ");
		String command = tokens[0];
		String arg = tokens.length > 1 ? tokens[1] : "";

		switch (command.toUpperCase()) {
		case "SYMBOL":
			// New line that prints actual array contents:
			System.out.println(Arrays.toString(bhavMap.getOrDefault(arg, new String[] { "Symbol not found" })));
			break;
		case "COUNT":
			System.out.println(bhavData.stream().filter(data -> data[1].trim().equalsIgnoreCase(arg.trim())).count());
			break;
		case "GAIN":
			double gainThreshold = Double.parseDouble(arg);
			bhavData.stream().filter(data -> calcPercent(data, 2, 5) > gainThreshold) // Make sure to pass the whole row
					.map(data -> data[0]) // Assuming SYMBOL is at index 0
					.forEach(System.out::println);
			break;

		case "TOPBOT":
			double topBot = Double.parseDouble(arg);
			bhavData.stream().filter(data -> calcPercent(data[3], data[4]) > topBot)
					.forEach(data -> System.out.println(data[0]));
			break;
		case "STDDEV":
			System.out.println(calcStdDev(arg));
			break;

		case "TOPGAINER":
			int n = Integer.parseInt(arg);
			displayTopGainers(n);
			break;
		case "TOPGAINERS":
			int m = Integer.parseInt(arg);
			displayTopGainers(m);
			break;
			
		case "TOPLAGGARDS":
            int laggardsN = Integer.parseInt(arg);
            displayTopLaggards(laggardsN);
            break;
        case "TOPTRADED":
            int tradedN = Integer.parseInt(arg);
            displayTopTraded(tradedN);
            break;
        case "BOTTRADED":
            int botTradedN = Integer.parseInt(arg);
            bottomNTraded(botTradedN);
            break;
        case "HIGHLOW":
            displayHighLow(arg);
            break;


		default:
			System.out.println("Unknown command");
		}
	}

	private static void displayTopGainers(int n) {
		bhavData.stream()
				.sorted((data1, data2) -> Double.compare(calcPercent(data2[8], data2[4]),
						calcPercent(data1[8], data1[4])))
				.limit(n).map(data -> String.format("Symbol: %s, Gain: %.2f%%", data[0], calcPercent(data[8], data[4])))
				.forEach(System.out::println);
	}
	
	
	private static void displayTopLaggards(int n) {
        bhavData.stream()
                .sorted(Comparator.comparingDouble(data -> calcPercent(data[8], data[4])))
                .limit(n)
                .map(data -> String.format("Symbol: %s, Loss: %.2f%%", data[0], calcPercent(data[8], data[4])))
                .forEach(System.out::println);
    }
	
	private static void displayTopTraded(int n) {
        bhavData.stream()
                .sorted((data1, data2) -> Double.compare(Double.parseDouble(data2[10]), Double.parseDouble(data1[10]))) // Assuming traded volume is at index 6
                .limit(n)
                .map(data -> String.format("Symbol: %s, Volume: %s", data[10], data[10]))
                .forEach(System.out::println);
    }
	
//	private static void displayBottomTraded(int n) {
//		bhavData.stream()
//        .sorted((data1, data2) -> Double.compare(Double.parseDouble(data1[10]), Double.parseDouble(data2[10]))) // Assuming traded volume is at index 6
//        .limit(n)
//        .map(data -> String.format("Symbol: %s, Volume: %s", data[10], data[10]))
//        .forEach(System.out::println);
//    }
//	Bottom N least traded (by volume) symbols
    public static List<String> bottomNTraded(int n) {
        return bhavData.stream()
                   .sorted(Comparator.comparingInt(row -> Integer.parseInt(row[10]))) // TTL_TRD_QNTY
                   .limit(n)
                   .map(row -> row[10])
                   .collect(Collectors.toList());
    }
	
	
	private static void displayHighLow(String series) {
        Optional<String[]> highest = bhavData.stream()
                .filter(data -> data[1].strip().equalsIgnoreCase(series))
                .max(Comparator.comparingDouble(data -> Double.parseDouble(data[6]))); // Assuming TOTRDVAL is at index 6

        Optional<String[]> lowest = bhavData.stream()
                .filter(data -> data[1].strip().equalsIgnoreCase(series))
                .min(Comparator.comparingDouble(data -> Double.parseDouble(data[6]))); // Assuming TOTRDVAL is at index 6

        if (highest.isPresent() && lowest.isPresent()) {
            System.out.println("Highest: " + highest.get()[0] + ", Lowest: " + lowest.get()[0]);
        } else {
            System.out.println("No data found for the series: " + series);
        }
    }
	
	

	public int countSymbolsBySeries(String series) {
		int count = 0;
		for (String[] row : bhavData) {
			if (row.length > 1 && row[1].trim().equalsIgnoreCase(series.trim())) {
				count++;
			}
		}
		
		return count;
	}

	private static double calcPercent(String[] columns, int openIdx, int closeIdx) {
		return closeIdx;
	}

	// Calculate percentage change
	private static double calcPercent(String end, String start) {
		return (Double.parseDouble(end) - Double.parseDouble(start)) / Double.parseDouble(start) * 100;
	}

	// Calculate standard deviation for a given series
	private static double calcStdDev(String series) {
		List<Double> closePrices = bhavData.stream()
				.filter(data -> data[1].strip().equalsIgnoreCase(series))
				.map(data -> Double.parseDouble(data[8])).toList();
		double mean = closePrices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		return Math.sqrt(closePrices.stream().mapToDouble(price -> Math.pow(price - mean, 2)).average().orElse(0.0));
	}

}
