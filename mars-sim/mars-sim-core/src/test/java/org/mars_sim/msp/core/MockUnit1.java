/**
 * Mars Simulation Project
 * MockUnit1.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

/**
 * A mock unit used for unit testing.
 */
public class MockUnit1 extends Unit {
	
	public MockUnit1() {
		// Use Unit constructor.
		super("Mock Unit 1", new Coordinates(0D, 0D));
		
		setBaseMass(10D);
	}
}