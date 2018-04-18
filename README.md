# COMS4112 Database System Implement Project 2

## TeamMember: Yanan Zhang (yz3054) and Li-Chieh Liu (ll3123)


## Summary 
In this project, we implemented a query optimizer for the condition selection based on the paper of "Selection Conditions in Main Memory". 


## Files
### 1. query.txt
This file is used to specify the selectivity for each basic terms whcih is discussed in section 4.1 of the paper and the format of the query.txt is showed below:
0.8 0.5 0.3 0.2
0.2 0.1 0.9
0.6 0.75 0.8 1 0.9
0.8 0.8 0.9 0.7 0.7 0.7

This means there are four cases and each line reprented one case with the selectivities. 

### 2. config.txt
This file is used to store the configures values which described the r, t, l, m, a, f values in the Section 4.2 of the paper. 

### 3. helper.java, subsetRecord.java and project2.java
The three java files are used for the algorithm implementations. project2.java is the main file and subsetRecord defined a subsetRecord class used for the algorithm, and helper.java defined the utiltiy functions that are used for the algorithm implementation. 

### 4.Makefile
It is used to compile the java program. 

### 5.Stage2.sh
Shell script to run the program.

## Run
1. First complile java files using cmd: $make
2. Then run the shell script file: $./stage2.sh query.txt config.txt
3. It will print the results to the consule as well as to a output.txt file. 

