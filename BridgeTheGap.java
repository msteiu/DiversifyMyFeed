import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.Scanner;
import java.util.Random;

import org.jsoup.*;

/**
 * This program creates a graph of a list of news organizations. Distance = # of links between them
 * It depends on Jsoup, which is a java api, and HTMLScanner (which is a program based on Jsoup).
 *
 * Structurally, it uses an EdgeList to create the graphs, and a TreeMap to store Vertices (String, Vertex)
 *
 * There are two nested classes - Vertex and Edge
 *
 *
 * @author Eliana, Mara January 28, 2018
 *
 */
public class BridgeTheGap {

	public String[][] rankings;
	public TreeMap<String, Vertex> vertexMap;
	public String center; //url that represents the center

	//----------------------------------------------//
	//Vertex class
	class Vertex implements Comparable<Vertex>{

		//INSTANCE VARIABLES
		String name; //article title or organization name
		ArrayList<Edge> links = new ArrayList<Edge>();
		Vertex prev;
		int rank;
		String url;

		//default values
		boolean isGreen = false;
		boolean isYellow = false;
		boolean isGray = true;
		int distance = 0;

		//VERTEX CONSTRUCTORS

		//constructor for vertices containing the location of non-news sources
		public Vertex(String urlName){
			url = urlName;
		}

		//news sources urls
		public Vertex(String theName, int theRank, String urlName) {
			name = theName;
			rank = theRank;
			url = urlName;
		}

		//return distance between current vertex and another vertex (used in Dijkstrav's algorithm)
		@Override
		public int compareTo(Vertex v1) {
			return (v1.distance - this.distance);
		}

		//GETTERS
		public String getURL(){
			return this.url;
		}

		public String getName(){
			return this.name;
		}

		public String toString(){
			return "The number " + rank + " vertex's name is " + name + ".";
		}
	}

	//remember when adding edge, make it cost + 1
	class Edge {
		Vertex otherend;
		int cost;

		public Edge(Vertex v, int c) {
			otherend = v;
			cost = c;
		}

		public int getCost() {
			return cost;
		}

	}

	//taking scanner text and translating it into a vertex, plus adding it to the vertices collection
	public ArrayList<Vertex> createAndAddVertex(){
		ArrayList<Vertex> newsOrg = new ArrayList<Vertex>(10);
		File fileName = new File("news.txt");
		try{
			Scanner scan = new Scanner(fileName);//read from file!!!
			while (scan.hasNextLine()){
				String line = scan.nextLine();
				if (line.substring(0,3).equals("ï»¿")){
					line = line.substring(3);
					String[] splitStr = line.split(" ");
					String newName = "";
					for (int i = 1; i<splitStr.length-1; i++)
						newName+=splitStr[i] + " ";
					int newRank = Integer.parseInt(splitStr[0]);
					String newUrl = splitStr[splitStr.length-1];
					Vertex newVertex = new Vertex(newName, newRank, newUrl);
					newsOrg.add(newVertex);
					vertexMap.put(newUrl, newVertex); 
				} 
				else {
					String[] splitStr = line.split(" ");
					String newName = "";
					for (int i = 1; i<splitStr.length-1; i++)
						newName+=splitStr[i] + " ";
					int newRank = Integer.parseInt(splitStr[0]);
					String newUrl = splitStr[splitStr.length-1];
					Vertex newVertex = new Vertex(newName, newRank, newUrl);
					newsOrg.add(newVertex);
					vertexMap.put(newUrl, newVertex);
				}
			}
		}
		catch (FileNotFoundException ex){
			System.out.println("Couldn't find file");
		}
		return newsOrg;
	}

	//create list of urls of news sources
	public ArrayList<String> newsURLs(){
		ArrayList<Vertex> orgList = createAndAddVertex();
		ArrayList<String> urls = new ArrayList<String>(10);

		for (Vertex v: orgList){
			//System.out.println("url in newsURLs method is " + v.url);
			urls.add(v.url);
		}

		return urls;
	}

	/**
	 * This is the constructor of BridgeTheGap
	 * @param url
	 */

	public BridgeTheGap() {
		vertexMap = new TreeMap<String, Vertex>(); //instantiate vertexMap here
		createAndAddVertex(); //makes organization vertex objects and stores them in a TreeMap (vertexMap)
		ArrayList<String> newsUrls = newsURLs(); //makes list of URLs that are news orgs
		String prevURL = "";

		try {
			for (String url: newsUrls){
				String theUrl = url;
				HTMLScanner scan = new HTMLScanner(url);

				//System.out.println("url is" + url);

				while (scan.hasNextLink()) {
					String nextVertexURL = scan.nextLink();
					if (newsUrls.contains(nextVertexURL)){//is news
						vertexMap.get(url).prev = vertexMap.get(prevURL);
						//System.out.println("nextURL is" + nextVertexURL);
						Edge e = new Edge(vertexMap.get(nextVertexURL), 1);
						//what to about the fact that some news sources are more connected to each other than others? 
						vertexMap.get(url).links.add(e); //adds to edgeList
					} 
				}
				prevURL = url;
				//System.out.println("prevURL is" + prevURL);
			}

		} catch (IOException e) {
			System.out.println("Didn't work.");
		}
	}

	/**
	 * implementation of Dijkstra's algorithm, sorting by distance from center
	 */

	public void Dijkstra(String center) {

		Vertex centerV = vertexMap.get(center); //gets the vertex from TreeMap

		// Does priority queue automatically re-sort?

		PriorityQueue<Vertex> priorityQ = new PriorityQueue<Vertex>(); //instantiate priority queue - will need to set its comparator based on length of edge list? Idk
		priorityQ.add(centerV);

		while (priorityQ.size() > 0) { //while all nodes have not been visited
			Vertex headOfQ = priorityQ.poll();
			headOfQ.isGreen = true;

			//for every vertex in the vertex's edgeList
			for (Edge e : headOfQ.links) {
				//do I need to remove this from the original queue?
				Vertex dest = e.otherend;
				if (dest.isGray) {
					dest.isYellow = true;
					dest.isGray = false;
					dest.distance += 1; //might not be 1, might be something else
					dest.prev = headOfQ;
					priorityQ.add(dest);
				} else {
					dest.distance += 1;
					dest.prev = headOfQ;
				}
			}

			Collection c = vertexMap.values();
			java.util.Iterator itr = c.iterator();
			while (itr.hasNext()){
				Vertex v = (Vertex) itr.next();
				//System.out.println("distance from center of "+ v.name + " " + v.distance);
			}
		}
	}

	//sorting
	public ArrayList<Vertex> Sorting(){ 

		PriorityQueue<Vertex> priorityQ = new PriorityQueue<Vertex>();
		Collection c = vertexMap.values();
		java.util.Iterator itr = c.iterator();
		while (itr.hasNext()){
			Vertex v = (Vertex) itr.next();
			priorityQ.add(v);
		}

		ArrayList<Vertex> orderedList = new ArrayList<Vertex>();

		while (priorityQ.size() != 0){
			orderedList.add(priorityQ.poll());
		}

		return orderedList;

	}

	//outputs 5 suggestions, one from each category
	public String[] finalOutput(){
		String[] suggestionArray = new String[5];
		ArrayList<Vertex> newList = Sorting();

		int avgDist = (newList.get(0).distance + newList.get(-1).distance)/2; //smallest distance + largest distance) / 2
		int number= avgDist%6;

		suggestionArray[0] = newList.get(number).name;
		suggestionArray[1] = newList.get(number + 7).name;
		suggestionArray[2] = newList.get(number + 14).name;
		suggestionArray[3] = newList.get(number + 21).name;
		suggestionArray[4] = newList.get(number + 28).name;

		return suggestionArray;

	}

	//outputs the type the person leans
	public String leaningType(){
		int rank = vertexMap.get(center).rank;
		if (rank <=7) {
			return "Strongly Liberal";
		}
		if (rank >7 && rank <=14) {
			return "Liberal";
		}
		if (rank >14 && rank <=21) {
			return "Moderate";
		}
		if (rank >21 && rank <=28) {
			return "Conservative";
		}
		if (rank >28 && rank <=35) {
			return "Strongly Conservative";
		}
		return "";
	}

	//Replacement for input - takes a random url from the urlList and uses that as the center
	public String inputReplacer() {
		ArrayList<String> urlList = newsURLs();
		int randomNum = new Random().nextInt(urlList.size());
		this.center = urlList.get(randomNum);

		return vertexMap.get(center).name;
	}

	public static void main (String[] args){
		BridgeTheGap newObject = new BridgeTheGap();
		newObject.inputReplacer(); //input
		newObject.Dijkstra(newObject.center);
		newObject.finalOutput(); //5 element string array of suggestions
		newObject.leaningType(); //tells user what type they are liberal conservative etc



	}

	//takes in .txt file generated from Twitter

	public String realInput() {
		ArrayList<String> inputURLs = new ArrayList<String>();
		ArrayList<String> compareURLs = newsURLs();

		File inputF = new File("twitter.txt");
		Scanner scan;
		String returnVal = "";
		try {
			scan = new Scanner(inputF);
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if (line.equals("/n /n /n")) {
					line = line.replace("'", "");
					line = line.replace(",", "");
					if (compareURLs.contains(line)) {
						inputURLs.add(line);
					}
				}
				//sums url ranks and then averages them to find the average political score
				int sum = 0;
				for (String url: inputURLs) {
					sum += vertexMap.get(url).rank;
				}
				sum = sum / inputURLs.size();

				//converts that rank into a url, by making a list of urls
				returnVal = "";
				while (returnVal.equals("") && sum <= 34) {
					for (String url1: compareURLs) {
						if (vertexMap.get(url1).rank == sum) {
							returnVal = url1;
						}
					} 
					sum += 1;
				}
				return returnVal;
			}

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return returnVal;
	}

}



