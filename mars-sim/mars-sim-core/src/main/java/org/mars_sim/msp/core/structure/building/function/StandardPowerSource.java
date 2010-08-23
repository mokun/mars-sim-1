/**
 * Mars Simulation Project
 * StandardPowerSource.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.building.Building;

/**
 * A power source that gives a constant supply of power.
 */
public class StandardPowerSource extends PowerSource implements Serializable {

	private final static String TYPE = "Standard Power Source";

	public StandardPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(TYPE, maxPower);
	}

	/**
	 * Gets the current power produced by the power source.
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	public double getCurrentPower(Building building) {
		return getMaxPower();
	}
}