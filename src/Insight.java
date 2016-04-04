package insight;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.*;
import java.util.*;

public class Insight {
	static int count = 0;
	private static double maxTime = 0.0;
	private static double minTime = 0.0;
	private static Queue<Tweet> curWindow ;
	private static Graph graph;
	//static int temp = 0;
	public static void main(String[] args) throws JSONException {
		//
			String output_path = "tweet_output/output.txt";
			String input_path = "tweet_input/tweets.txt";
			try {
				FileWriter fos = new FileWriter(output_path);
				PrintWriter dos = new PrintWriter(fos);
	
				//String filename = "tweets.txt";
				curWindow = new LinkedList<Tweet>();
				graph = new Graph();
				try {
					FileReader fileReader = new FileReader(input_path);
					BufferedReader br = new BufferedReader(fileReader);
					String line = br.readLine(); 
					
					while(line != null){
						
						
						String str = line.replace("\"", "\\\"");
						JSONObject obj = new JSONObject(line);
						
						if(count <4) {
							System.out.println(str);
							System.out.println(obj);
						}
						count++;
						if(obj.has("created_at")) { //ignore rate-limit messages
							//count ++;
							String createdAt =  obj.getString("created_at");
							
							System.out.println("created at : " + createdAt + " count: " + count);
							//if(obj.has("entities")) {
								JSONArray hashtags = obj.getJSONObject("entities").getJSONArray("hashtags");
								JSONObject checktag = obj.getJSONObject("entities");
							//	dos.println(checktag);
								//System.out.println(checktag);
								//System.out.println("HT : " + hashtags +"," + count);
								
							//}
								handlethisTweet(createdAt, hashtags, dos);
								String ave = average_edge();
								//average_edge();
								//
								dos.println(ave);
//								for(int i = 1; i< finalPrediction.length; i++) {
//									int prediction = finalPrediction[i];
//									dos.println(i + "\t" + prediction);
//									dos.println();
//								}
								//	
						}  //else {temp ++;} 
						line = br.readLine();
					}
					br.close();
				} catch(FileNotFoundException ex) {
					System.out.println("Unable to read file '" + filename + "'");
				} catch(IOException ex) {
					System.out.println("Error reading file '" + filename + "'");
				}
				System.out.println(count);
				dos.close();
				fos.close();
			}catch(IOException e) {
				System.out.println(e.getMessage());
			}
			

//		while(!curWindow.isEmpty()) {
//			System.out.println("tweet " + i +"-th : " + curWindow.poll().timeStamp);i++;
//			
//			
//		}
		
	}
	
	private static void handlethisTweet(String createdAt, JSONArray hashtags, PrintWriter dos) throws JSONException {
		//dos.print(hashtags);
		Tweet tweet = new Tweet(createdAt, hashtags);
		
		//curWindow.add(tweet);
		
		if(tweet.timeStamp > maxTime) {
			maxTime = tweet.timeStamp;
			minTime = maxTime - 60.0 ;
		//	System.out.println("max : " + maxTime + ", min : " + minTime);
		}
		if(tweet.timeStamp >= minTime && tweet.timeStamp <= maxTime ) {
			curWindow.add(tweet);
			if(tweet.nodes != null) {
				//dos.println("hashtag nodes: " + tweet.nodes + ", count : " + count);
				
				for(int i = 0 ; i <tweet.nodes.size(); i++) {
					String fromNode = tweet.nodes.get(i);
					graph.addNode(fromNode);
					for(int j = i + 1 ; j < tweet.nodes.size(); j ++) {			
						graph.addEdge(fromNode, tweet.nodes.get(j));
						graph.addEdge(tweet.nodes.get(j), fromNode);
					}
					
				}
			}

		} 
		while(curWindow.peek().timeStamp < minTime) {
			tweet = curWindow.peek();
			if(tweet.nodes != null) {
//				for(int i = 0 ; i < tweet.nodes.size(); i++) {
//					String fromNode = tweet.nodes.get(i);
//					graph.deleteNode(fromNode);
//					for(int j = i + 1; j <tweet.nodes.size(); j++) {
//						graph.deleteEdge(fromNode, tweet.nodes.get(j));
//						graph.deleteEdge(tweet.nodes.get(j), fromNode);
//					}
//				}
				graph.deleteNode(tweet.nodes);
				graph.deleteEdge(tweet.nodes);
			}
			curWindow.poll();
		}
		//current node list
		//current edge list
	}
	
	private static String average_edge() {
		double ave = (double) graph.edges.size() / graph.nodes.size(); 
		//System.out.println(Math.round(ave * 100.0)/100.0 + ", " + graph.edges.size() + " , " + graph.nodes.size()); 
		String str = Math.round(ave * 100.0)/100.0 + ", " + graph.edges.size() + " , " + graph.nodes.size();
		return str;
		//return(Math.round(ave * 100.0)/100.0);
	}
}

class Tweet {
	Date date ;
	double timeStamp = 0.0;
	ArrayList<String> nodes = null;
	
	public Tweet() {
	}
	
	public Tweet(String createdAt, JSONArray hashtags) throws JSONException {
		createdAt = createdAt.substring(4);
		//System.out.println(createdAt);
		DateFormat informat = new SimpleDateFormat( "MMM dd HH:mm:ssZ yyyy");
		Date date = null;
		try {
			date = informat.parse(createdAt);
			//System.out.println(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//tweet.timeStamp.getTime()
		timeStamp = date.getTime()/1000;
		//System.out.println(timeStamp);
		if(hashtags.length() != 0 ) {
			nodes = new ArrayList<String>();
			for(int i = 0 ; i <hashtags.length(); i++) {
				nodes.add(((JSONObject) hashtags.get(i)).getString("text"));
			}
			//
			for(int i = 0 ; i < nodes.size(); i++) {
//				if(nodes.get(i).contains(" ")) {
//					System.out.println("contains blank .. ");
//				}
				//System.out.print(nodes.get(i) + ", ");
			} //System.out.println();
		}
	}
}

class Graph {
	HashMap<String, Integer> nodes;
	HashMap<String, Integer> edges;
	public Graph() {
		nodes = new HashMap<String, Integer>();
		edges = new HashMap<String, Integer>();
	}
	public void addNode(String fromNode) {
		if(!nodes.containsKey(fromNode)) {
			nodes.put(fromNode, 1);
		} else {
			nodes.put(fromNode, nodes.get(fromNode) + 1);
		}
	}
	public void addEdge(String fromNode, String toNode) {
		if(!fromNode.equals(toNode)) {
			String nodePair = fromNode +  " " + toNode;
			if(!edges.containsKey(nodePair)) {
				edges.put(nodePair, 1) ;
			} else {
				edges.put(nodePair, edges.get(nodePair) + 1);
			}
		}
	}
//	public void deleteNode(String node) {
//		int numNode = nodes.get(node);
//		if(numNode == 1) {
//			nodes.remove(node);
//		} else {
//			nodes.put(node, numNode - 1);
//		}
//	}
	public void deleteNode(ArrayList<String> nodeList) {
		Set<String> set = new HashSet<String>(nodeList);
		for(String node : set) {
			int numNode = nodes.get(node);
			if(numNode == 1) {
				nodes.remove(node);
			} else {
				nodes.put(node, numNode - 1);
			}
		}
	}
//	public void deleteEdge(String fromNode, String toNode) {
//		String nodePair = fromNode + " " + toNode;
//		int numEdge = edges.get(nodePair);
//		System.out.println(numEdge);
//		if(numEdge == 1) {
//			edges.remove(nodePair);
//		} else {
//			edges.put(nodePair, numEdge - 1);
//		}
//	}
	public void deleteEdge(ArrayList<String> nodeList) {
		Set<String> set = new HashSet<String>(nodeList);
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


