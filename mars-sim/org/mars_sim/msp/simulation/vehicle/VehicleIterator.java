/**
 * Mars Simulation Project
 * VehicleIterator.java
 * @version 2.74 2002-01-13
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation.vehicle;

/** 
 *  The VehicleIterator class is an iterator over a VehicleCollection. 
 */
public interface VehicleIterator {
 
    /**
     *  Returns the next element in the iteration.
     *  @return the next element in the iteration.
     */
    public Vehicle next();

    /**
     *  Returns true if the iteration has more elements.
     *  @return true if the iterator has more elements.
     */
    public boolean hasNext();

    /**
     *  Removes from the underlying collection the 
     *  last element returned by the iterator.
     */
    public void remove();
}