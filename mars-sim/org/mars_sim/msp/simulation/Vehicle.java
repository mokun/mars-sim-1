/**
 * Mars Simulation Project
 * Vehicle.java
 * @version 2.73 2001-11-22
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.*;

/** The Vehicle class represents a generic vehicle. It keeps track of
 *  generic information about the vehicle. This class needs to be
 *  subclassed to represent a specific type of vehicle.
 */
public abstract class Vehicle extends Unit {

    // Data members
    private Direction direction; // Direction vehicle is traveling in
    private double speed = 0; // Current speed of vehicle in kph
    private double baseSpeed = 0; // Base speed of vehicle in kph (can be set in child class)
    private String status; // Current status of vehicle ("Moving", "Parked") (other child-specific status allowed)
    private Settlement settlement; // The settlement which the vehicle is parked at
    private Vector passengers; // List of people who are passengers in vehicle
    private Person driver; // Driver of the vehicle
    private double distanceTraveled = 0; // Total distance traveled by vehicle
    private double distanceMaint = 0; // Distance traveled by vehicle since last maintenance
    private int maxPassengers = 0; // Maximum number of passengers the vehicle can carry.
    private double fuel = 0; // Current amount of fuel in the vehicle.
    private double fuelCapacity = 0; // Maximum amount of fuel the vehicle can carry.
    private double oxygen = 0; // Curent amount of oxygen in the vehicle.
    private double oxygenCapacity = 0; // Maximum amount of oxygen the vehicle can carry.
    private double water = 0; // Curent amount of water in the vehicle.
    private double waterCapacity = 0; // Maximum amount of water the vehicle can carry.
    private double food = 0; // Curent amount of food in the vehicle.
    private double foodCapacity = 0; // Maximum amount of food the vehicle can carry.
    protected double range; // Maximum range of vehicle.
    private Coordinates destinationCoords; // Coordinates of the destination
    private Settlement destinationSettlement; // Destination settlement (it applicable)
    private String destinationType; // Type of destination ("None", "Settlement" or "Coordinates")
    private double distanceToDestination = 0; // Distance in meters to the destination
    private boolean isReserved = false; // True if vehicle is currently reserved for a driver and cannot be taken by another
    private int vehicleSize = 1; // Size of vehicle in arbitrary units.(Value of size units will be established later.)
    private double maintenanceWork = 0; // Work done for vehicle maintenance.
    private double totalMaintenanceWork; // Total amount of work necessary for vehicle maintenance.
    private HashMap potentialFailures; // A table of potential failures in the vehicle. (populated by child classes)
    private MechanicalFailure mechanicalFailure; // A list of current failures in the vehicle.
    private boolean distanceMark = false; // True if vehicle is due for maintenance.
    private MarsClock estimatedTimeOfArrival; // Estimated time of arrival to destination.

    /** Constructs a Vehicle object with a given settlement
     *  @param name the vehicle's name
     *  @param settlement the settlement the vehicle is parked at
     *  @param mars the virtual Mars
     */
    Vehicle(String name, Settlement settlement, VirtualMars mars) {
        // use Unit constructor
        super(name, settlement.getCoordinates(), mars);

        setSettlement(settlement);
        initVehicleData();
    }
    
    /** Constructs a Vehicle object
     *  @param name the vehicle's name
     *  @param mars the virtual Mars
     *  @param manager the unit manager
     *  @throws Exception when there are no available settlements
     */
    Vehicle(String name, VirtualMars mars, UnitManager manager) throws Exception {
        // use Unit constructor
        super(name, new Coordinates(0D, 0D), mars);

        if (manager.getSettlementNum() == 0) throw new Exception("No available settlements");
        
        Settlement leastVehicles = null;
        int least = Integer.MAX_VALUE;
        SettlementIterator i = manager.getSettlements().iterator();
        while (i.hasNext()) {
            Settlement settlement = i.next();
            if (settlement.getVehicleNum() < least) {
                least = settlement.getVehicleNum();
                leastVehicles = settlement;
            }
        }
        setSettlement(leastVehicles);
        
        initVehicleData();
    }
    
    /** Initializes vehicle data */
    private void initVehicleData() {
        setStatus("Parked");
        setDestinationType("None");
        passengers = new Vector();
        potentialFailures = new HashMap();
        totalMaintenanceWork = 1000D; // (1 sol) 
        direction = new Direction(0);
    }

    /** Returns vehicle's current status 
     *  @return the vehicle's current status
     */
    public String getStatus() {
        return status;
    }

    /** Sets vehicle's current status 
     *  @param status the vehicle's current status
     */
    public void setStatus(String status) {
        this.status = status;
        if (status.equals("Parked") || status.equals("Broken Down"))
            setSpeed(0D); 
    }

    /** Returns true if vehicle is reserved by someone 
     *  @return true if vehicle is currently reserved by someone
     */
    public boolean isReserved() {
        return isReserved;
    }

    /** Reserves a vehicle or cancels a reservation 
     *  @param status the vehicle's reserved status
     */
    public void setReserved(boolean status) {
        // System.out.println(getName() + " reserved: " + status);
        isReserved = status;
    }

    /** Returns speed of vehicle 
     *  @return the vehicle's speed (in km/hr)
     */
    public double getSpeed() {
        return speed;
    }

    /** Sets the vehicle's current speed 
     *  @param speed the vehicle's speed (in km/hr)
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /** Returns base speed of vehicle 
     *  @return the vehicle's base speed (in km/hr)
     */
    public double getBaseSpeed() {
        return baseSpeed;
    }

    /** Sets the base speed of vehicle 
     * @param speed the vehicle's base speed (in km/hr)
     */
    public void setBaseSpeed(double speed) {
        baseSpeed = speed;
    }

    /** Gets the range of the vehicle
     *  @return the range of the vehicle (in km)
     */
    public double getRange() {
        return range;
    }

    /** Returns the current amount of fuel in the vehicle. 
     *  @return the vehicle's fuel stores (in units)
     */
    public double getFuel() {
        return fuel;
    }

    /** Adds fuel to the vehicle. 
     *  @param addedFuel the amount of fuel to be added (in units)
     */
    public void addFuel(double addedFuel) {
        fuel += addedFuel;
        if (fuel > fuelCapacity) {
            fuel = fuelCapacity;
        }
    }

    /** Consumes a portion of the vehicle's fuel. 
     *  @param consumedFuel the amount of fuel consumed (in units)
     */
    public void consumeFuel(double consumedFuel) {
        boolean noFuel = (fuel == 0D);
        fuel -= consumedFuel;
        if (fuel < 0D) fuel = 0D;
    }

    /** Returns the fuel capacity of the vehicle. 
     *  @return the vehicle's fuel capacity
     */
    public double getFuelCapacity() {
        return fuelCapacity;
    }

    /** Sets the fuel capacity of the vehicle. 
     *  @param capacity the vehicle's fuel capacity
     */
    void setFuelCapacity(double capacity) {
        fuelCapacity = capacity;
    }

    /** Returns the current amount of oxygen in the vehicle. 
     *  @return the vehicle's oxygen stores (in units)
     */
    public double getOxygen() {
        return oxygen;
    }

    /** Adds oxygen to the vehicle. 
     *  @param addedOxygen the amount of oxygen to be added (in units)
     */
    public void addOxygen(double addedOxygen) {
        oxygen += addedOxygen;
        if (oxygen > oxygenCapacity) {
            oxygen = oxygenCapacity;
        }
    }

    /** Removes a portion of the vehicle's oxygen. 
     *  @param amount the amount of oxygen removed (in units)
     *  @return Amount of oxygen actually removed (in units)
     */
    public double removeOxygen(double amount) {
       double result = amount;
        if (amount > oxygen) {
            result = oxygen;
            oxygen = 0;
        }
        else oxygen -= amount;

        return result;
    }

    /** Returns the oxygen capacity of the vehicle. 
     *  @return the vehicle's oxygen capacity
     */
    public double getOxygenCapacity() {
        return oxygenCapacity;
    }    
    
    /** Sets the oxygen capacity of the vehicle. 
     *  @param capacity the vehicle's oxygen capacity
     */
    void setOxygenCapacity(double capacity) {
        oxygenCapacity = capacity;
    }
    
    /** Returns the current amount of water in the vehicle. 
     *  @return the vehicle's water stores (in units)
     */
    public double getWater() {
        return water;
    }

    /** Adds water to the vehicle. 
     *  @param addedWater the amount of water to be added (in units)
     */
    public void addWater(double addedWater) {
        water += addedWater;
        if (water > waterCapacity) {
            water = waterCapacity;
        }
    }

    /** Removes water from storage. 
     *  @param amount the amount of water requested (in units)
     *  @return the amount of water actually received (in units)
     */
    public double removeWater(double amount) {
        double result = amount;
        if (amount > water) {
            result = water;
            water = 0;
        }
        else water -= amount;

        return result;
    }

    /** Returns the water capacity of the vehicle. 
     *  @return the vehicle's water capacity
     */
    public double getWaterCapacity() {
        return waterCapacity;
    }
    
    /** Sets the water capacity of the vehicle. 
     *  @param capacity the vehicle's water capacity
     */
    void setWaterCapacity(double capacity) {
        waterCapacity = capacity;
    }
    
    /** Returns the current amount of food in the vehicle. 
     *  @return the vehicle's food stores (in units)
     */
    public double getFood() {
        return food;
    }

    /** Adds food to the vehicle. 
     *  @param addedFood the amount of food to be added (in units)
     */
    public void addFood(double addedFood) {
        food += addedFood;
        if (food > foodCapacity) {
            food = foodCapacity;
        }
    }

    /** Removes food from storage. 
     *  @param amount the amount of food requested from storage (in units)
     *  @return the amount of food actually received from storage (in units)
     */
    public double removeFood(double amount) {
        double result = amount;
        if (amount > food) {
            result = food;
            food = 0;
        }
        else food -= amount;

        return result;
    }
    
    /** Returns the food capacity of the vehicle. 
     *  @return the vehicle's food capacity
     */
    public double getFoodCapacity() {
        return foodCapacity;
    }
    
    /** Sets the food capacity of the vehicle. 
     *  @param capacity the vehicle's food capacity
     */
    void setFoodCapacity(double capacity) {
        foodCapacity = capacity;
    }
    
    /** Returns total distance traveled by vehicle (in km.) 
     *  @return the total distanced traveled by the vehicle (in km)
     */
    public double getTotalDistanceTraveled() {
        return distanceTraveled;
    }

    /** Adds a distance (in km.) to the vehicle's total distance traveled 
     *  @param distance distance to add to total distance traveled (in km)
     */
    public void addTotalDistanceTraveled(double distance) {
        distanceTraveled += distance;
    }

    /** Returns distance traveled by vehicle since last maintenance (in km.) 
     *  @return distance traveled by vehicle since last maintenance (in km)
     */
    public double getDistanceLastMaintenance() {
        return distanceMaint;
    }

    /** Adds a distance (in km.) to the vehicle's distance since last maintenance.
     *  Set distanceMark to true if vehicle is due for maintenance.
     *  @param distance distance to add (in km)
     */
    public void addDistanceLastMaintenance(double distance) {
        distanceMaint += distance;
        if ((distanceMaint > 5000D) && !distanceMark) {
            distanceMark = true;
        }
    }

    /** Sets vehicle's distance since last maintenance to zero */
    public void clearDistanceLastMaintenance() {
        distanceMaint = 0;
    }

    /** Returns direction of vehicle (0 = north, clockwise in radians) 
     *  @return the direction the vehicle is traveling (in radians)
     */
    public Direction getDirection() {
        return (Direction) direction.clone();
    }

    /** Sets the vehicle's facing direction (0 = north, clockwise in radians) 
     *  @param direction the direction the vehicle is travleling (in radians)
     */
    public void setDirection(Direction direction) {
        this.direction.setDirection(direction.getDirection());
    }

    /** Returns the maximum passenger capacity of the vehicle (including the driver). 
     *  @return the maximum passenger capacity of the vehicle
     */
    public int getMaxPassengers() {
        return maxPassengers;
    }

    /** Returns the maximum passenger capacity of the vehicle (including the driver). 
     *  @param num the maximum passenger capacity of the vehicle
     */
    void setMaxPassengers(int num) {
        maxPassengers = num;
    }

    /** Returns number of passengers in vehicle 
     *  @return the current number of passengers
     */
    public int getPassengerNum() {
        return passengers.size();
    }

    /** Returns a particular passenger by vector index number 
     *  @param index the passenger's index number
     *  @return the passenger
     */
    public Person getPassenger(int index) {
        Person result = null;
        if (index < passengers.size()) {
            result = (Person) passengers.elementAt(index);
        }
        return result;
    }

    /** Returns true if a given person is currently in the vehicle 
     *  @param person the person in question
     *  @return true if person is a passenger in the vehicle
     */
    public boolean isPassenger(Person person) {

        for (int x = 0; x < passengers.size(); x++) {
            if (person == (Person) passengers.elementAt(x)) {
                return true;
            }
        }
        return false;
    }

    /** Add a new passenger to the vehicle if enough capacity and person is not alreay aboard. 
     *  @param passenger a new passenger
     */
    public void addPassenger(Person passenger) {
        if ((passengers.size() < maxPassengers) && !isPassenger(passenger)) {
            passengers.addElement(passenger);
        }
    }

    /** Removes a passenger from a vehicle 
     *  @param passenger passenger leaving vehicle
     */
    public void removePassenger(Person passenger) {
        if (isPassenger(passenger)) {
            passengers.removeElement(passenger);
            if (passenger == driver) {
                driver = null;
            }
        }
    }

    /** Returns driver of the vehicle 
     *  @return the driver
     */
    public Person getDriver() {
        return driver;
    }

    /** Sets the driver of the vehicle 
     *  @param driver the driver
     */
    public void setDriver(Person driver) {
        this.driver = driver;
    }

    /** Returns the current settlement vehicle is parked at.
     *  Returns null if vehicle is not currently parked at a settlement. 
     *  @return the settlement the vehicle is parked at
     */
    public Settlement getSettlement() {
        if ((status.equals("Parked") || status.equals("Periodic Maintenance")) && (settlement != null)) {
            return settlement;
        } else {
            return null;
        }
    }

    /** Sets the settlement which the vehicle is parked at 
     *  @param settlement the settlement the vehicle is parked at
     */
    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
        if (settlement != null) {
            location.setCoords(settlement.getCoordinates());
            settlement.addVehicle(this);
        }
    }

    /** Returns distance to destination in kilometers
     *  Returns 0 if vehicle is not currently moving toward a destination 
     *  @return the distance to the vehicle's destination
     */
    public double getDistanceToDestination() {
        return distanceToDestination;
    }

    /** Sets the vehicle's distance to its destination 
     *  @param distanceToDestination the distance to the vehicle's destination
     */
    public void setDistanceToDestination(double distanceToDestination) {
        this.distanceToDestination = distanceToDestination;
    }

    /** Gets the type of destination for the vehicle 
     *  @return the vehicle's destination type
     */
    public String getDestinationType() {
        return destinationType;
    }

    /** Sets the type of destination for the vehicle ("Coordinates", "Settlement" or "None") 
     *  @param the vehicle's destination type
     */
    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    /** Sets the destination coordinates 
     *  @param destinationCoords the vehicle's destination location
     */
    public void setDestination(Coordinates destinationCoords) {
        this.destinationCoords = destinationCoords;
        if (destinationType == null) destinationType = "Coordinates";
    }

    /** Returns the destination coordinates.
     *  (null if no destination). 
     *  @return the vehicle's destination location
     */
    public Coordinates getDestination() {
        return destinationCoords;
    }

    /** Sets the destination settlement 
     *  @param destinationSettlement the vehicle's destination settlement
     */
    public void setDestinationSettlement(Settlement destinationSettlement) {
        this.destinationSettlement = destinationSettlement;
        if (destinationSettlement != null) {
            setDestination(destinationSettlement.getCoordinates());
            destinationType = "Settlement";
        }
    }

    /** Returns the destination settlement.
     *  (null if no destination settlement). 
     *  @return the vehicle's destination settlement
     */
    public Settlement getDestinationSettlement() {
        return destinationSettlement;
    }
    
    /** Returns the ETA (Estimated Time of Arrival)
     *  @return ETA as string ("13-Adir-05  056.349")
     */
    public String getETA() {
        if (estimatedTimeOfArrival != null) 
            return estimatedTimeOfArrival.getTimeStamp();
        else return "";
    }
    
    /** Sets the ETA (Estimated Time of Arrival) of the vehicle.
     *  @param newETA new ETA of the vehicle
     */
    public void setETA(MarsClock newETA) {
        this.estimatedTimeOfArrival = newETA;
    }
            
    /** Returns the vehicle's size. 
     *  @return the vehicle's size
     */
    public int getSize() {
        return vehicleSize;
    }

    /** Sets the vehicle's size. 
     *  @param size the vehicle's size
     */
    void setSize(int size) {
        vehicleSize = size;
    }

    /** Adds a potential mechanical failure for the vehicle. 
     *  @param failureName the name of the mechanical failure
     */
    public void addPotentialFailure(String failureName) {
        potentialFailures.put(failureName, new Integer(1));
    }

    /** Returns the vehicle's current mechanical failure. 
     *  @return the vehicle's current mechanical failure
     */
    public MechanicalFailure getMechanicalFailure() {
        return mechanicalFailure;
    }

    /** Creates a new mechanical failure for the vehicle from its list
     *  of potential failures. 
     */
    public void newMechanicalFailure() {
        Object keys[] = potentialFailures.keySet().toArray();

        // Sum weights
        int totalWeight = 0;

        for (int x = 0; x < keys.length; x++) {
            totalWeight += ((Integer) potentialFailures.get((String) keys[x])).intValue();
        }

        // Get a random number from 0 to the total weight
        int r = (int) Math.round(Math.random() * (double) totalWeight);

        // Determine which failure is selected
        int tempWeight = ((Integer) potentialFailures.get((String) keys[0])).intValue();
        int failureNum = 0;
        while (tempWeight < r) {
            failureNum++;
            tempWeight += ((Integer) potentialFailures.get((String) keys[failureNum])).intValue();
        }
        String failureName = (String) keys[failureNum];

        mechanicalFailure = new MechanicalFailure(failureName);
        // System.out.println(name + " has mechanical failure: " + mechanicalFailure.getName());
    }

    /** Add work to periodic vehicle maintenance. 
     *  @param time amount of work time added to vehicle maintenance (in millisols)
     */
    public void addWorkToMaintenance(double time) {
        // If vehicle has already been maintained, return.
        if (distanceMaint == 0D) {
            return;
        }

        // Add work to maintenance work done.
        maintenanceWork += time;

        // If maintenance work is complete, vehicle good for 5,000 km.
        if (maintenanceWork >= totalMaintenanceWork) {
            maintenanceWork = 0;
            distanceMaint = 0D;
        }
    }

    /** Returns the current amount of work towards maintenance. 
     *  @return the current amount of work towards maintenance
     */
    public double getCurrentMaintenanceWork() {
        return maintenanceWork;
    }

    /** Returns the total amount of work needed for maintenance. 
     *  @return the total amount of work needed for maintenance
     */
    public double getTotalMaintenanceWork() {
        return totalMaintenanceWork;
    }
}
