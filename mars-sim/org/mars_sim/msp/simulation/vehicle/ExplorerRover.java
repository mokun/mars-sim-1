/**
 * Mars Simulation Project
 * ExplorerRover.java
 * @version 2.74 2002-03-03
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import java.io.Serializable;

/**
 * The ExplorerRover class is a rover designed for exploration and collecting
 * rock samples.
 */
public class ExplorerRover extends Rover implements Serializable {
	
    // Static data members
    private static final int CREW_CAPACITY = 6; // Max number of crewmembers.
	
    /** 
     * Constructs an ExplorerRover object at a given settlement.
     * @param name the name of the rover
     * @param settlement the settlementt he rover is parked at
     * @param mars the mars instance
     */
    ExplorerRover(String name, Settlement settlement, VirtualMars mars) {
        // Use the Rover constructor
	super(name, settlement, mars);

	initExplorerRoverData();

	// Add EVA Suits
	addEVASuits();
    }

    /**
     * Constructs an ExplorerRover object
     * @param name the name of the rover
     * @param mars the mars instance
     * @param manager the unit manager
     * @throws Exception when there are no available settlements
     */
    ExplorerRover(String name, VirtualMars mars, UnitManager manager) throws Exception {
        // Use the Rover constructor
	super(name, mars, manager);

	initExplorerRoverData();

	// Add EVA Suits
	addEVASuits();
    }

    /**
     * Initialize rover data
     */
    private void initExplorerRoverData() {
        
        // Set crew capacity
	crewCapacity = CREW_CAPACITY;
    }

    /** 
     * Returns a string describing the vehicle.
     * @return string describing vehicle
     */
    public String getDescription() {
        return "Long Range Exploration Rover";
    }
}