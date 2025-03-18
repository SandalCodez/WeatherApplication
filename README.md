# WeatherApplication
A Java application for analyzing and generating reports from weather data stored in CSV format.
# Overview
This application provides tools to parse, analyze, and generate reports from weather data. It leverages modern Java features including:

Records for immutable data representation
Functional interfaces and lambda expressions
Stream API for efficient data processing
Text blocks for formatting output
Pattern matching with enhanced switch expressions
Virtual threads (with fallback for older Java versions)

# Features

Parse weather data from CSV files
Calculate statistics like average temperature and count of rainy days
Categorize weather data based on temperature ranges
Generate monthly weather reports
Filter data based on custom predicates
Group and analyze weather data by various attributes

# Data Structure
The application uses an immutable WeatherData record with the following fields:

date: The date of the weather record (LocalDate)
temperature: Temperature in Celsius (double)
humidity: Humidity percentage (double)
precipitation: Precipitation amount in mm (double)
windSpeed: Wind speed in km/h (double)

# CSV Format
The application expects CSV files with the following format:
Copydate,temperature,humidity,precipitation,windSpeed
2023-01-01,5.5,80.0,2.1,12.3
2023-01-02,4.8,85.0,0.0,8.7

# Output Example
The application produces reports in the following format:
CopyWeather Data Analysis Summary
----------------------------
Total records: 17
Date range: 2023-01-01 to 2023-12-01

Detailed Analysis:
Average Temperature: 16.65°C
Rainy Days: 9

Temperature Categories:
 - Hot: 5 day(s)
 - Warm: 4 day(s)
 - Mild: 2 day(s)
 - Cool: 6 day(s)

Monthly Average Temperatures:
 - JANUARY: 4.5°C
 - FEBRUARY: 7.4°C
 - MARCH: 14.0°C
 - APRIL: 18.2°C
 - MAY: 22.7°C
 - JUNE: 26.5°C
 - JULY: 32.0°C
 - AUGUST: 30.5°C
 - SEPTEMBER: 25.3°C
 - OCTOBER: 19.8°C
 - NOVEMBER: 12.5°C
 - DECEMBER: 7.2°C

Monthly Weather Report
---------------------

JANUARY:
  Average Temperature: 4.5°C
  Rainy Days: 2
  Dominant Weather: Cold

FEBRUARY:
  Average Temperature: 7.4°C
  Rainy Days: 0
  Dominant Weather: Cool
