package de.innot.avreclipse.devexp.pinconfig;

import java.util.List;
import java.io.BufferedReader;
import java.io.IOException; 
import java.nio.charset.StandardCharsets;
import java.nio.file.Files; 
import java.nio.file.Path;
import java.nio.file.Paths; 


public class PinIoStorage {
	List<PinIOItem> pins;
	String filePath;

	
	public PinIoStorage(String filePath) {
		this.filePath = filePath;
	}
	
	public List<PinIOItem> readConfig(String filePath) {
		Path pathToFile = Paths.get(filePath);
		
		// create an instance of BufferedReader
        // using try with resource, Java 7 feature to close resources
        try (BufferedReader br = Files.newBufferedReader(pathToFile,StandardCharsets.US_ASCII)) {

            // read the first line from the text file
            String line = br.readLine();

            // loop until all lines are read
            while (line != null) {
//
//                // use string.split to load a string array with the values from
//                // each line of
//                // the file, using a comma as the delimiter
//                String[] attributes = line.split(";");
//                
//                pins.add(new PinIOItem())
//                
//                Book book = createBook(attributes);
//
//                // adding book into ArrayList
//                books.add(book);
//
//                // read next line before looping
//                // if end of file reached, line would be null
//                line = br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
		
		
		return pins;
	}
	
	public void writeConfig(String filePath, List<PinIOItem> pins) {
		
		
	}
	
}
