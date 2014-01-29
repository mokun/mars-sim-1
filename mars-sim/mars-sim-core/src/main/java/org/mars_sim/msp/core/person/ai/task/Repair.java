/**
 * Mars Simulation Project
 * Repair.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.malfunction.Malfunctionable;

/**
 * The Repair interface is a task for repairing malfunction. 
 */
public interface Repair {

    /**
     * Gets the malfunctionable entity the person is currently 
     * repairing or null if none.
     * @return entity
     */
    public Malfunctionable getEntity(); 
}