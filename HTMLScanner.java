import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * Creates a Scanner-like interface to a web page, file URL, or
 * local file.  Uses <a href="http://jsoup.org">jsoup: Java-based HTML parser</a>
 * to handle the processing of HTML.
 * <p>
 * Students should refer to the <a href="http://jsoup.org/apidocs/">JSoup API reference</a>
 * for details or ideas of how they might want to extend this class to
 * report on other information (like title, links, etc.). 
 * <p>
 * Unless you are extending the functionality, you should not need
 * to modify this file.
 * 
 * @author Benjamin Kuperman (Spring 2011)
 *
 */
public class HTMLScanner implements Iterator<String> {

    /** base URL it is being constructed from */
    protected String url;
    
    /** The JSoup version of the document */
    protected Document doc;
    
    /** The scanner we use to actually perform the iteration */
    protected Scanner scanner;
    
    /** A way to iterate through links */
    private Iterator<Element> linkIterator;

    /**
     * Creates a Scanner-like interface to a webpage.  Uses the 
     * <a href="http://jsoup.org/">jsoup platform</a> to do 
     * parse and handle the HTML.
     *  
     * @param url Web page to be read (should be HTTP or HTTPS).
     * 		  Files can be specified using "file:" or just the name.
     * @throws IOException on error fetching page
     */
    public HTMLScanner(String url) throws IOException {
	
	// save this for students use later
	this.url = url;
	
	// Open up a connection to the web page or file
	if (url.toLowerCase().startsWith("http://") 
		|| url.toLowerCase().startsWith("https://")) {
	    this.doc = Jsoup.connect(url).get();
	} else {
	    String tempUrl = url;
	    if (url.toLowerCase().startsWith("file:")) {
		tempUrl = url.substring(5);
	    }
	    this.doc = Jsoup.parse(new File(tempUrl), null, url);
	    this.doc.setBaseUri("file://"+tempUrl);
	}
	
	// Read the document
	String body = doc.body().text();
	
	// Create a scanner to parse the document
	this.scanner = new Scanner(body);
	
	// But we only care about alphanumeric things
	this.scanner.useDelimiter(Pattern.compile("[^a-zA-Z0-9]+"));
	
	// If we want to use the links
	Elements links = doc.select("a[href]");
	linkIterator = links.iterator();
	
    }
    
    
    public boolean hasNext() {
	return this.scanner.hasNext();
    }

    public String next() {
	return this.scanner.next();
    }

    public void remove() {
	throw new UnsupportedOperationException("Cannot remove from HTMLScanner");
    }
    
    public boolean hasNextLink() {
	return linkIterator.hasNext();
    }
    
    public String nextLink() {
	return linkIterator.next().attr("abs:href");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
	for (String s: args) {
	    try {
		HTMLScanner url = new HTMLScanner(s);
		System.out.println(url.url);
		System.out.println(url.url.replaceAll(".", "-"));
		while (url.hasNext()) {
		    System.out.println(url.next());
		}
		System.out.println("\nLinks\n-----");
		while (url.hasNextLink()) {
		    System.out.println(url.nextLink());
		}
		System.out.println(url.doc.baseUri());
	    } catch (IOException e) {
		System.out.println("Error reading URL: "+s+" ("+e.getMessage()+")");
//		e.printStackTrace();
	    }
	    System.out.println();
	}
    }

}

