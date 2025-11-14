# TeamMate – Team Formation System

TeamMate is a Java OOP project that collects participant survey data, classifies personality types, and forms balanced teams using a structured matching algorithm. The system supports CSV file handling, manual survey input, exception handling, and multithreading for efficient processing of large datasets.

## Features

### 1. Input & Survey
- Collects participant data through:
  - Manual console-based survey (Q1–Q5 personality questions)
  - Interest selection (Valorant, Dota, FIFA, Basketball, Badminton)
  - Role selection (Strategist, Attacker, Defender, Supporter, Coordinator)
- Option to load participants from a sample CSV file.

### 2. Personality Classification
Based on the 5-question survey (scaled to 100):
- Leader: 90–100  
- Balanced: 70–89  
- Thinker: 50–69  

### 3. Team Formation Logic
Teams are built using the following rules:
- Game interest diversity  
- At least three different roles per team  
- Personality mix (1 Leader, 1–2 Thinkers, rest Balanced)  
- Skill balance across teams  
- Randomization for fair distribution  

Users can select a team size between 3 and 6.

### 4. File Handling
- Load participants from `participants_sample.csv`
- Save generated teams into `formed_teams.csv`

### 5. Exception Handling
- Validation for invalid role, interest, and score inputs
- Handling missing or corrupted CSV files
- Error handling for file read/write issues
- Input validation for survey entries and team size

### 6. Concurrency
- Uses threads for:
  - Processing survey data
  - Forming teams in parallel

### 7. Object-Oriented Design
Includes clear separation of responsibilities using:
- `Participant` class
- `PersonalityClassifier`
- `Team` class
- `TeamBuilder`
- `CSVUtil`
- `Main` controller class

## How to Run
1. Place `participants_sample.csv` in the `data/` folder.
2. Compile the project:
   ```bash
   javac *.java
   java Main
   ```
### Follow on-screen instructions to:

- Load CSV participants
- Add participants manually
- Choose team size
- Generate teams and export results

### Output

- Console-based preview of teams
- CSV output stored as formed_teams.csv

### Project Purpose

This project was developed as part of an OOP assignment to demonstrate:

- Class design
- Encapsulation
- File handling
- Multithreading
- Exception handling
- Applying real-world matching logic in an OOP system