/**
 * Mars Simulation Project
 * VehicleUIProxy.java
 * @version 2.71 2000-10-23
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import org.mars_sim.msp.simulation.*; 
 
/**
 * Abstract user interface proxy for a vehicle. 
 */
public abstract class VehicleUIProxy extends UnitUIProxy {

    /** Constructs a VehicleUIProxy object 
     *  @param vehicle the vehicle
     *  @param proxyManager the vehicle's proxy manager
     */
    public VehicleUIProxy(Vehicle vehicle, UIProxyManager proxyManager) {
        super(vehicle, proxyManager);
    }
}