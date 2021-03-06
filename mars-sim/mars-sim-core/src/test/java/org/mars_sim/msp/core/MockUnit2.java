/**
 * Mars Simulation Project
 * MockUnit2.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.msp.core;

public class MockUnit2 extends Unit {

	public MockUnit2() {
		// Use Unit constructor.
		super("Mock Unit 2", new Coordinates(0D, 0D));
		
		setBaseMass(20D);
	}
}