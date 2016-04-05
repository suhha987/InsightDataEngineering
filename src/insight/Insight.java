package insight;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.*;
import java.util.*;

public class Insight {
	private static double maxTime = 0.0;
	private static double minTime = 0.0;
	private static Queue<Tweet> curWindow ; //later, containing tweets within 60 seconds from the maximum time appeared.
	private static Graph graph; //later, for tweets in curWindow, storing number of apprearance of each valid nodes and edges.
	
	public static void main(String[] args) throws JSONException {
		
		String output_path = "tweet_output/output.txt";
		String input_path = "tweet_input/tweets.txt";
		try { //write to output file
			FileWriter fos = new FileWriter(output_path);
			PrintWriter dos = new PrintWriter(fos);
			curWindow = new LinkedList<Tweet>();
			graph = new Graph();
			
			try { //read input file
				FileReader fileReader = new FileReader(input_path);
				BufferedReader br = new BufferedReader(fileReader);
				String line = br.readLine(); 
				
				while(line != null){ //read each line(one json message at a time)
					JSONObject obj = new JSONObject(line);
					if(obj.has("created_at")) { //ignore rate-limit messages
						String createdAt =  obj.getString("created_at"); //read created_At
						JSONArray hashtags = obj.getJSONObject("entities").getJSONArray("hashtags"); //read hashtags
						handlethisTweet(createdAt, hashtags); //handle current tweet with variables createdAt and hastags
						double ave = average_edge(); //compute average number of edge for the current graph
						dos.println(new DecimalFormat("0.00").format(ave)); //format to xx.00 (include exact and up to two decimal points)
					}  
					line = br.readLine();
				}
				br.close();
			} catch(FileNotFoundException ex) {
				System.out.println("Unable to read file '" + input_path + "'");
			} catch(IOException ex) {
				System.out.println("Error reading file '" + input_path + "'");
			}
			dos.close();
			fos.close();
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * *
	 * Method handlethisTweet(String createdAt, JSONArray hashtags) :
	 * This method get input of createdAt and hashtags for a tweet -> take appropriate action
	 * - If the time createdAt is the latest one, then update maxTime and add the information for this tweet into the curWindow and graph
	 * - If the time createdAt is between minTime and maxTime, add this tweet into the curWindow and graph
	 * - If the time createdAt is less then minTime, do nothing.
	 * - After updating the current tweet, look at old tweets in curWindow which is less then the updated minTime, remove them and take
	 * 	 appropriate action to graph.
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * */
	private static void handlethisTweet(String createdAt, JSONArray hashtags) throws JSONException {
		Tweet tweet = new Tweet(createdAt, hashtags);
		
		if(tweet.timeStamp > maxTime) {
			maxTime = tweet.timeStamp;
			minTime = maxTime - 60.0 ;
		}
		
		if(tweet.timeStamp >= minTime && tweet.timeStamp <= maxTime ) {
			curWindow.add(tweet);
			if(tweet.nodes != null ) {
				if(tweet.nodes.size() > 1) { //if number of node in this tweet is 1, then it does not contain any edge -> do nothing
					graph.addNode(tweet.nodes);
					graph.addEdge(tweet.nodes);
				}
			}
		} 
		
		//evict old tweets
		while(curWindow.peek().timeStamp < minTime) {
			tweet = curWindow.peek();
			if(tweet.nodes != null) {
				if(tweet.nodes.size() > 1) {
					graph.deleteNode(tweet.nodes);
					graph.deleteEdge(tweet.nodes);
				}
			}
			curWindow.poll();
		}
	}
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * *
	 * Method average_edge :
	 * For the current graph, compute the average edges
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * */
	private static double average_edge() {
		double ave = (double) graph.edges.size() / graph.nodes.size(); 
		return(Math.round(ave * 100.0)/100.0);
	}
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * *
 * Class Tweet :
 * For each tweet, define createdAt as data and convert it into seconds. 
 * Each tweet also has arrayList<String> which represents every node(hashtag) it contains. If it has no hashtag, then the arrayList is empty
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * */

class Tweet {
	Date date ;
	double timeStamp = 0.0;
	ArrayList<String> nodes = null;
	
	public Tweet() {
		
	}
	
	public Tweet(String createdAt, JSONArray hashtags) throws JSONException {
		createdAt = createdAt.substring(4);
		DateFormat informat = new SimpleDateFormat( "MMM dd HH:mm:ssZ yyyy");
		Date date = null;
		try {
			date = informat.parse(createdAt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		timeStamp = date.getTime()/1000;
		if(hashtags.length() != 0 ) { //if it has at least one hashtag
			nodes = new ArrayList<String>(); //store each hashtag as an element of ArrayList<String> nodes.
			for(int i = 0 ; i <hashtags.length(); i++) {
				nodes.add(((JSONObject) hashtags.get(i)).getString("text"));
			}
		}
	}
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * *
 * Class Graph :
 * Graph contains HashMap of nodes and edges
 * * HashMap<String, Integer> nodes : contains <key = nodes that has at least one edge to others, value = number of occurrences in curWindow>
 * * HashMap<String, Integer> edges : contains <key = edges(ex, "node1 node2"), value = number of occurrences of that edge in curWindow>
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * ** * * * * * * * * * * * * * * * * * * * */

class Graph {
	HashMap<String, Integer> nodes;
	HashMap<String, Integer> edges;
	
	public Graph() {
		nodes = new HashMap<String, Integer>();
		edges = new HashMap<String, Integer>();
	}
	//addNode :  if there already exists the given name node, then increment the count by 1, otherwise put it into hashMap with value 1.
	public void addNode(ArrayList<String> nodeList) {
		Set<String> set = new HashSet<String>(nodeList);
		if(set.size() > 1) {
			for(String node: set) {
				if(!nodes.containsKey(node)) {
					nodes.put(node, 1);
				} else {
					nodes.put(node, nodes.get(node) + 1);
				}
			}
		}
	}
	
	//addEdge : if there already exists the pair of two given nodes, then increment the count by 1, otherwise put the pair into the hashMap with value 1.
	public void addEdge(ArrayList<String> nodeList) {
		Set<String> set = new HashSet<String>(nodeList);
		if(set.size() > 1) {
			Iterator<String> set1= set.iterator();
			while(set1.hasNext()) {
				String fromNode = set1.next();
				Iterator<String> set2 = set.iterator();
				while(set2.hasNext()) {
					String toNode = set2.next();
					if(!fromNode.equals(toNode)) {
						String nodePair = fromNode + " " + toNode;
						if(!edges.containsKey(nodePair)) {
							edges.put(nodePair, 1) ;
						} else {
							edges.put(nodePair, edges.get(nodePair) + 1);
						}
					}
				}
			}
		}
	}
	
	//deleteNode : if there exists the given node in the hashMap, then subtract 1 from the value. If the value becomes 0, then remove the node from the hashMap
	public void deleteNode(ArrayList<String> nodeList) {
		Set<String> set = new HashSet<String>(nodeList);
		for(String node : set) {
			if(nodes.containsKey(node)) {
				int numNode = nodes.get(node);
				if(numNode == 1) {
					nodes.remove(node);
				} else {
					nodes.put(node, numNode - 1);
				}
			}
		}
	}
	
	//deleteEdge : if there exists the pair of two given nodes, then subtract 1 from the value. If the value becomes 0, then remove the pair of edge from the hashMap
	public void deleteEdge(ArrayList<String> nodeList) {
		Set<String> set = new HashSet<String>(nodeList); //converted arrayList of nodes into set in order to consider only distinct nodes of a tweet
		if(set.size() > 1) {
			Iterator<String> set1 = set.iterator();
			while(set1.hasNext()) {
				String fromNode = set1.next();
				Iterator<String> set2 = set.iterator();
				while(set2.hasNext()) {
					String toNode = set2.next();
					if(!fromNode.equals(toNode)) {
						String nodePair = fromNode + " " + toNode;
						int numEdge = edges.get(nodePair);
						if(numEdge == 1) {
							edges.remove(nodePair);
						} else {
							edges.put(nodePair, numEdge - 1);
						}
					}
				}
			}
		}
	}
}


