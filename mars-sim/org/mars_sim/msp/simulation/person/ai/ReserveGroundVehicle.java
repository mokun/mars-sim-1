/**
 * Mars Simulation Project
 * ReserveGroundVehicle.java
 * @version 2.74 2002-01-30
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.io.Serializable;

/** The ReserveGroundVehicle class is a task for reserving a ground
 *  vehicle at a settlement for a trip.
 *  The duration of the task is 50 millisols.
 */
class ReserveGroundVehicle extends Task implements Serializable {

    // Data members
    private double duration = 50D; // The predetermined duration of task in millisols
    private GroundVehicle reservedVehicle; // The reserved vehicle
    private Coordinates destination; // The destination coordinates for the trip

    /** Constructs a ReserveGroundVehicle object with a destination.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param destination the destination of the trip
     */
    public ReserveGroundVehicle(Person person, VirtualMars mars, Coordinates destination) {
        super("Reserving a vehicle", person, false, mars);

        this.destination = destination;
        reservedVehicle = null;
    }

    /** Constructs a ReserveGroundVehicle object without a destinatiion.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public ReserveGroundVehicle(Person person, VirtualMars mars) {
        super("Reserving a vehicle", person, false, mars);

        destination = null;
        reservedVehicle = null;
    }

    /** Perform this task for the given amount of time.
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        timeCompleted += time;
        if (timeCompleted > duration) {

            Settlement settlement = person.getSettlement();
            FacilityManager facilities = settlement.getFacilityManager();
            MaintenanceGarageFacility garage = (MaintenanceGarageFacility) facilities.getFacility("Maintenance Garage");

	    VehicleIterator i = settlement.getParkedVehicles().iterator();
	    while (i.hasNext()) {
	        Vehicle tempVehicle = i.next();
                if (reservedVehicle == null) {
                    boolean reservable = true;

                    if (!(tempVehicle instanceof GroundVehicle)) reservable = false;
                    if (tempVehicle.isReserved()) reservable = false;
                    if (garage.vehicleInGarage(tempVehicle)) reservable = false;
                    if (destination != null) {
                        if (tempVehicle.getRange() < person.getCoordinates().getDistance(destination)) reservable = false;
                    }

                    if (reservable) {
                        reservedVehicle = (GroundVehicle) tempVehicle;
                        reservedVehicle.setReserved(true);
                    }
                }
            }

            done = true;
            return timeCompleted - duration;
        }
        else return 0;
    }

    /** Returns true if settlement has an available ground vehicle.
     *  @param settlement
     *  @return are there any available ground vehicles
     */
    public static boolean availableVehicles(Settlement settlement) {

        boolean result = false;

        FacilityManager facilities = settlement.getFacilityManager();
        MaintenanceGarageFacility garage = (MaintenanceGarageFacility) facilities.getFacility("Maintenance Garage");

	VehicleIterator i = settlement.getParkedVehicles().iterator();
	while (i.hasNext()) {
	    Vehicle vehicle = i.next();
            if (vehicle instanceof GroundVehicle) {
                if (!vehicle.isReserved() && !garage.vehicleInGarage(vehicle)) result = true;
            }
        }

        return result;
    }

    /** Gets the reserved ground vehicle if task is done and successful.
     *  Returns null otherwise.
     *  @return reserved ground vehicle
     */
    public GroundVehicle getReservedVehicle() {
        return reservedVehicle;
    }
}

