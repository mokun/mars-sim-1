/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.io.Serializable;

/** The LoadVehicle class is a task for loading a vehicle with fuel and supplies. 
 */
class LoadVehicle extends Task implements Serializable {

    // The amount of resources (kg) one person can load per millisol.
    private static double LOAD_RATE = 10D;

    // Data members
    private Vehicle vehicle;  // The vehicle that needs to be loaded.
    private StoreroomFacility stores;  // The settlement's stores.
    private Settlement settlement; // The person's settlement.

    /** Constructs a LoadVehicle object. 
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param vehicle the vehicle to be loaded
     */
    public LoadVehicle(Person person, VirtualMars mars, Vehicle vehicle) {
        super("Loading " + vehicle.getName(), person, mars);

        this.vehicle = vehicle;

        settlement = person.getSettlement();
        FacilityManager facilities = settlement.getFacilityManager();
        stores = (StoreroomFacility) facilities.getFacility("Storerooms");
    }

    /** Performs this task for a given period of time 
     *  @param time amount of time to perform task (in millisols) 
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        double amountLoading = LOAD_RATE * time;

        if (hasEnoughSupplies(settlement, vehicle)) {
         
            // Load fuel
            double fuelAmount = vehicle.getFuelCapacity() - vehicle.getFuel();
            if (fuelAmount > amountLoading) fuelAmount = amountLoading;
            stores.removeFuel(fuelAmount);
            vehicle.addFuel(fuelAmount);
            amountLoading -= fuelAmount;

            // Load oxygen 
            double oxygenAmount = vehicle.getOxygenCapacity() - vehicle.getOxygen();
            if (oxygenAmount > amountLoading) oxygenAmount = amountLoading;
            stores.removeOxygen(oxygenAmount);
            vehicle.addOxygen(oxygenAmount);
            amountLoading -= oxygenAmount;

            // Load water 
            double waterAmount = vehicle.getWaterCapacity() - vehicle.getWater();
            if (waterAmount > amountLoading) waterAmount = amountLoading;
            stores.removeWater(waterAmount);
            vehicle.addWater(waterAmount);
            amountLoading -= waterAmount;

            // Load Food 
            double foodAmount = vehicle.getFoodCapacity() - vehicle.getFood();
            if (foodAmount > amountLoading) foodAmount = amountLoading;
            stores.removeFood(foodAmount);
            vehicle.addFood(foodAmount);
            amountLoading -= foodAmount;
        }
        else done = true;

        if (isFullyLoaded(vehicle)) done = true;

        return 0;
    }

    /** Returns true if there are enough supplies in the settlements stores to supply vehicle.
     *  @param settlement the settlement the vehicle is at
     *  @param vehicle the vehicle to be checked
     *  @return enough supplies?
     */
    public static boolean hasEnoughSupplies(Settlement settlement, Vehicle vehicle) {
        boolean enoughSupplies = true;

        FacilityManager facilities = settlement.getFacilityManager();
        StoreroomFacility stores = (StoreroomFacility) facilities.getFacility("Storerooms");

        double neededFuel = vehicle.getFuelCapacity() - vehicle.getFuel();
        if (neededFuel > stores.getFuelStores() - 50D) enoughSupplies = false;
        
        double neededOxygen = vehicle.getOxygenCapacity() - vehicle.getOxygen();
        if (neededOxygen > stores.getOxygenStores() - 50D) enoughSupplies = false;

        double neededWater = vehicle.getWaterCapacity() - vehicle.getWater();
        if (neededWater > stores.getWaterStores() - 50D) enoughSupplies = false;
 
        double neededFood = vehicle.getFoodCapacity() - vehicle.getFood();
        if (neededFood > stores.getFoodStores() - 50D) enoughSupplies = false;

        return enoughSupplies;
    }

    /** Returns true if the vehicle is fully loaded with supplies.
     *  @param vehicle to be checked
     *  @return is vehicle fully loaded?
     */
    public static boolean isFullyLoaded(Vehicle vehicle) {
        boolean result = true;
     
        if (vehicle.getFuel() != vehicle.getFuelCapacity()) result = false;
        if (vehicle.getOxygen() != vehicle.getOxygenCapacity()) result = false;
        if (vehicle.getWater() != vehicle.getWaterCapacity()) result = false;
        if (vehicle.getFood() != vehicle.getFoodCapacity()) result = false;

        return result;
    }
}