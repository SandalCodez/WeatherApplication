

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WeatherDataApplcation {


    /**
     * # Weather Data Record
     * Immutable record representing a single day's weather data.
     *
     * @param date The date of the weather record
     * @param temperature The temperature in Celsius
     * @param humidity The humidity percentage
     * @param precipitation The precipitation amount in mm
     * @param windSpeed The wind speed in km/h
     */
    public record WeatherData(
            LocalDate date,
            double temperature,
            double humidity,
            double precipitation,
            double windSpeed) {

        /**
         * Factory method to create a WeatherData from CSV line.
         *
         * # Example:
         * ```java
         * WeatherData data = WeatherData.fromCsv("2023-01-01,5.5,80.0,2.1,12.3");
         * ```
         */
        public static WeatherData fromCsv(String line) {
            String[] parts = line.split(",");
            return new WeatherData(
                    LocalDate.parse(parts[0], DateTimeFormatter.ISO_DATE),
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]),
                    parts.length > 4 ? Double.parseDouble(parts[4]) : 0.0
            );
        }

        /**
         * Determines the weather category based on temperature.
         * Uses enhanced switch with pattern matching.
         *
         * # Example:
         * ```java
         * String category = weatherData.getWeatherCategory();
         * ```
         */
        public String getWeatherCategory() {
            // Using enhanced switch with arrow syntax (Java 14+)
            int temp = (int) Math.floor(temperature);
            return switch (temp/10) {
                case 3,4,5 -> "Hot";
                case 2 -> "Warm";
                case 1 -> "Mild";
                case 0 -> "Cool";
                default -> "Cold";
            };
        }

        /**
         * Checks if it was a rainy day.
         *
         * @return true if precipitation is greater than 0
         */
        public boolean isRainyDay() {
            return precipitation > 0;
        }
    }

    /**
     * # Weather Analyzer Interface
     * Functional interface for analyzing weather data.
     */
    @FunctionalInterface
    interface WeatherAnalyzer<T> {
        T analyze(List<WeatherData> data);

        /**
         * Default method to filter data before analysis.
         *
         * # Example:
         * ```java
         * WeatherAnalyzer<Double> analyzer = data -> data.stream()
         *     .mapToDouble(WeatherData::temperature)
         *     .average()
         *     .orElse(0);
         * Double result = analyzer.withFilter(data -> data.temperature() > 10)
         *     .analyze(weatherDataList);
         * ```
         */
        default WeatherAnalyzer<T> withFilter(Predicate<WeatherData> filter) {
            return data -> analyze(data.stream()
                    .filter(filter)
                    .toList());
        }
    }

    /**
     * # Weather Report Interface
     * Interface for generating weather reports.
     */
    interface WeatherReport {
        /**
         * Creates a formatted report as a string.
         * Uses text blocks from Java 15+.
         *
         * @param data The weather data
         * @return A formatted report
         */
        String generate(List<WeatherData> data);
    }

    /**
     * Main method to run the Weather Data Analyzer.
     *
     * @param args Command line arguments (expected: path to CSV file)
     */
    public static void main(String[] args) {
        String filePath = "src/weatherdata.csv";
        try {

            try {
                Thread.startVirtualThread(() -> {
                    try {
                        analyzeWeatherData(filePath);
                    } catch (IOException e) {
                        System.err.println("Error analyzing weather data: " + e.getMessage());
                    }
                }).join();
            } catch (UnsupportedOperationException e) {
                // Fallback for older Java versions without virtual threads
                Thread thread = new Thread(() -> {
                    try {
                        analyzeWeatherData(args[0]);
                    } catch (IOException e2) {
                        System.err.println("Error analyzing weather data: " + e2.getMessage());
                    }
                });
                thread.start();
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Analysis interrupted: " + e.getMessage());
        }
    }

    /**
     * Analyzes weather data from a CSV file.
     *
     * # Example:
     * ```java
     * analyzeWeatherData("weatherdata.csv");
     * ```
     *
     * @param filePath Path to the CSV file
     * @throws IOException If an I/O error occurs
     */
    private static void analyzeWeatherData(String filePath) throws IOException {
        List<WeatherData> weatherData = parseWeatherDataCsv(filePath);

        // Print summary using text blocks
        String summary = """
            Weather Data Analysis Summary
            ----------------------------
            Total records: %d
            Date range: %s to %s
            
            Detailed Analysis:
            """.formatted(
                weatherData.size(),
                weatherData.stream().map(WeatherData::date).min(LocalDate::compareTo).orElse(null),
                weatherData.stream().map(WeatherData::date).max(LocalDate::compareTo).orElse(null)
        );

        System.out.println(summary);

        // Define analyzers using lambdas and functional interfaces
        WeatherAnalyzer<Double> averageTemperatureAnalyzer = data -> data.stream()
                .mapToDouble(WeatherData::temperature)
                .average()
                .orElse(0);

        WeatherAnalyzer<Long> rainyDaysCounter = data -> data.stream()
                .filter(WeatherData::isRainyDay)
                .count();

        // Using standard lambda expressions
        WeatherAnalyzer<Map<String, Long>> categoryCounts = data -> data.stream()
                .collect(Collectors.groupingBy(
                        WeatherData::getWeatherCategory,
                        Collectors.counting()
                ));

        // Analyze by month using Java Stream API
        Map<Month, List<WeatherData>> weatherByMonth = weatherData.stream()
                .collect(Collectors.groupingBy(data -> data.date().getMonth()));

        // Print analysis results
        System.out.println("Average Temperature: " +
                averageTemperatureAnalyzer.analyze(weatherData) + "°C");

        System.out.println("Rainy Days: " +
                rainyDaysCounter.analyze(weatherData));

        System.out.println("\nTemperature Categories:");
        categoryCounts.analyze(weatherData).forEach((category, count) ->
                System.out.println(" - " + category + ": " + count + " day(s)"));

        System.out.println("\nMonthly Average Temperatures:");
        weatherByMonth.forEach((month, monthData) -> {
            double avg = averageTemperatureAnalyzer.analyze(monthData);
            System.out.printf(" - %s: %.1f°C\n", month, avg);
        });

        // Show days with temperature above a threshold using filter
        double threshold = 25.0;
        List<WeatherData> hotDays = weatherData.stream()
                .filter(data -> data.temperature() > threshold)
                .toList();

        System.out.println("\nDays with temperature above " + threshold + "°C: " + hotDays.size());
        hotDays.forEach(data ->
                System.out.println(" - " + data.date() + ": " + data.temperature() + "°C"));

        // Custom weather report using interface and text blocks
        WeatherReport monthlyReport = data -> {
            StringBuilder reportBuilder = new StringBuilder();

            // Using the strip indent feature (Java 15)
            reportBuilder.append("""
                    Monthly Weather Report
                    ---------------------
                    """.stripIndent());

            data.stream()
                    .collect(Collectors.groupingBy(d -> d.date().getMonth()))
                    .forEach((month, monthData) -> {
                        double avgTemp = monthData.stream()
                                .mapToDouble(WeatherData::temperature)
                                .average()
                                .orElse(0);

                        long rainyDays = monthData.stream()
                                .filter(WeatherData::isRainyDay)
                                .count();

                        Map<String, Long> categories = monthData.stream()
                                .map(WeatherData::getWeatherCategory)
                                .collect(Collectors.groupingBy(
                                        category -> category,
                                        Collectors.counting()
                                ));

                        String dominantCategory = categories.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse("Unknown");

                        reportBuilder.append(String.format("""
                            
                            %s:
                              Average Temperature: %.1f°C
                              Rainy Days: %d
                              Dominant Weather: %s
                            """,
                                month, avgTemp, rainyDays, dominantCategory));
                    });

            return reportBuilder.toString();
        };

        System.out.println("\n" + monthlyReport.generate(weatherData));
    }

    /**
     * Parses weather data from a CSV file.
     *
     * # Example CSV format:
     * ```
     * date,temperature,humidity,precipitation,windSpeed
     * 2023-01-01,5.5,80.0,2.1,12.3
     * 2023-01-02,4.8,85.0,0.0,8.7
     * ```
     *
     * @param filePath Path to the CSV file
     * @return List of WeatherData records
     * @throws IOException If an I/O error occurs
     */
    private static List<WeatherData> parseWeatherDataCsv(String filePath) throws IOException {
        try (Stream<String> lines = Files.lines(Path.of(filePath))) {
            return lines
                    .skip(1) // Skip header
                    .map(WeatherData::fromCsv)
                    .toList();
        }
    }

    /**
     * Creates a sample CSV file with weather data for testing.
     *
     * @param filePath Path where to create the sample file
     * @throws IOException If an I/O error occurs
     */
    public static void createSampleData(String filePath) throws IOException {
        List<String> lines = Arrays.asList(
                "date,temperature,humidity,precipitation,windSpeed",
                "2023-01-01,5.5,80.0,2.1,12.3",
                "2023-01-02,4.8,85.0,0.0,8.7",
                "2023-01-03,3.2,90.0,5.6,15.2",
                "2023-02-01,6.7,75.0,0.0,10.5",
                "2023-02-02,8.1,65.0,0.0,9.3",
                "2023-03-01,12.3,60.0,1.2,8.7",
                "2023-03-15,15.6,55.0,0.0,12.8",
                "2023-04-01,18.2,50.0,0.5,14.3",
                "2023-05-01,22.7,45.0,0.0,11.2",
                "2023-06-01,26.5,40.0,0.0,9.8",
                "2023-07-01,32.3,35.0,0.0,7.5",
                "2023-07-15,31.8,38.0,0.0,8.2",
                "2023-08-01,30.5,42.0,1.8,10.3",
                "2023-09-01,25.3,55.0,2.5,12.8",
                "2023-10-01,19.8,65.0,3.2,14.5",
                "2023-11-01,12.5,75.0,4.7,16.3",
                "2023-12-01,7.2,85.0,3.1,18.7"
        );

        Files.write(Path.of(filePath), lines);
    }
}

