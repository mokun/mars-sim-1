/**
 * Mars Simulation Project
 * SurfMarsMap.java
 * @version 2.70 2000-09-04
 * @author Scott Davis
 * @author Greg Whelan
 */

import java.io.*;
import javax.swing.JComponent;

public class SurfMarsMap extends CannedMarsMap {

    private RandomAccessFile map;

    public SurfMarsMap(JComponent displayArea) {
	super(displayArea);

	try {
	    map = new RandomAccessFile("SurfaceMarsMap.dat", "r");
	} catch (FileNotFoundException ex) {
	    System.out.println("Could not find SurfaceMarsMap.dat");
	    System.exit(0);
	}
    }

    public RandomAccessFile getMapFile() {
	return map;
    }
}