Insight Data Engineering - Coding Challenge
===========================================================
## Challenge Summary

Calculate the average degree of a vertex in a Twitter hashtag graph for the last 60 seconds and update this each time a new tweet apprears. 
Update the Twitter hashtag graph each time a new tweet appears so that the graph consist of tweets that arrived in the last 60 seconds as comparted to the maximum timestamp that has beeen processed.
Average degree is calculated as : 
    $$\frac{total number of directed edges in the graph}{total number of valid nodes in the graph}$$

## Steps to run the code 

- input file : tweets.input/tweets.txt 
- run : run.sh file (sh run.sh)
- output file : tweets.output/output.txt
- run2.sh file is for insight_testsuite/run_tests.sh file. 
  (I corrected run.sh -> run2.sh in run_tests.sh file for conveniece)

## Solution Explanation

My solution (Insight.java file) does the following :

    1) Read each JSON message from the input file.
    2) Extract the information from each JSON message, "created_at" and "hashtags". -> Save as a tweet consists of timestamp and hashtags(ArrayList<String>).
    3) - If the current tweet was created the most recently, then update maxTime appropriately and add this tweet in curWindow(tweets withn in last 60 seconds) and add it to the current graph if it contains hashtags.
       - Else If the tweet was created between minTime and maxTime, then same as above case.
       - Else (the tweet was created before minTime), then ignore it.

    
       