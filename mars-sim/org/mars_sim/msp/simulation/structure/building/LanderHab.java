/**
 * Mars Simulation Project
 * LanderHab.java
 * @version 2.75 2003-02-15
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import java.util.*;

/**
 * The LanderHab class represents a lander habitat building.
 */
public class LanderHab extends InhabitableBuilding 
        implements LivingAccommodations, Research, Communication, EVA, 
        Recreation, Dining, ResourceProcessing, Storage {
    
    // Number of people the hab can accommodate at once.
    private final static int ACCOMMODATION_CAPACITY = 6;
    
    // Power down level for processes.
    private final static double POWER_DOWN_LEVEL = .5D;
    
    private ResourceProcessManager processManager;
    private Map resourceStorageCapacity;
    
    /**
     * Constructor
     * @param manager - building manager.
     */
    public LanderHab(BuildingManager manager) {
        // Use InhabitableBulding constructor
        super("Lander Hab", manager, ACCOMMODATION_CAPACITY);
        
        // Set up resource processes.
        Inventory inv = manager.getSettlement().getInventory();
        processManager = new ResourceProcessManager(this, inv);
        
        // Create water recycling process
        ResourceProcess waterRecycling = new ResourceProcess("water recycling", inv);
        waterRecycling.addMaxInputResourceRate(Resource.WASTE_WATER, .0002D, false);
        waterRecycling.addMaxOutputResourceRate(Resource.WATER, .00017D, false);
        processManager.addResourceProcess(waterRecycling);
        
        // Create carbon scrubbing process
        ResourceProcess carbonScrubbing = new ResourceProcess("carbon scrubbing", inv);
        carbonScrubbing.addMaxInputResourceRate(Resource.CARBON_DIOXIDE, .000067D, false);
        carbonScrubbing.addMaxOutputResourceRate(Resource.OXYGEN, .00005D, false);
        processManager.addResourceProcess(carbonScrubbing);
        
        // Set up resource storage capacity map.
        resourceStorageCapacity = new HashMap();
        resourceStorageCapacity.put(Resource.OXYGEN, new Double(1000D));
        resourceStorageCapacity.put(Resource.WATER, new Double(5000D));
        resourceStorageCapacity.put(Resource.WASTE_WATER, new Double(500D));
        resourceStorageCapacity.put(Resource.CARBON_DIOXIDE, new Double(500D));
        resourceStorageCapacity.put(Resource.FOOD, new Double(1000D));
        
        // Add resource storage capacity to settlement inventory.
        Iterator i = resourceStorageCapacity.keySet().iterator();
        while (i.hasNext()) {
            String resourceName = (String) i.next();
            double capacity = ((Double) resourceStorageCapacity.get(resourceName)).doubleValue();
            inv.setResourceCapacity(resourceName, inv.getResourceCapacity(resourceName) + capacity);
        }
        
        // Initial resources in lander hab
        inv.addResource(Resource.WATER, 500D);
        inv.addResource(Resource.OXYGEN, 500D);
        inv.addResource(Resource.FOOD, 500D);
    }
    
    /**
     * Gets the accommodation capacity of this building.
     *
     * @return number of accomodations.
     */
    public int getAccommodationCapacity() {
        return ACCOMMODATION_CAPACITY;
    }
    
    /**
     * Gets the power this building currently requires for full-power mode.
     * @return power in kW.
     */
    public double getFullPowerRequired() {
        return getLifeSupportPowerRequired() + 10D;
    }
    
    /**
     * Gets the building's resource process manager.
     * @return resource process manager
     */
    public ResourceProcessManager getResourceProcessManager() {
        return processManager;
    }
    
    /**
     * Gets the power down mode resource processing level.
     * @return proportion of max processing rate (0D - 1D)
     */
    public double getPowerDownResourceProcessingLevel() {
        return POWER_DOWN_LEVEL;
    }
    
    /** 
     * Gets a map of the resources this building is capable of
     * storing and their amounts in kg.
     * @return Map of resource keys and amount Double values.
     */
    public Map getResourceStorageCapacity() {
        return resourceStorageCapacity;
    }
    
    /**
     * Time passing for building.
     * Child building should override this method for things
     * that happen over time for the building.
     *
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) {
        super.timePassing(time);
        
        // Utilize water.
        waterUsage(time);
        
        // Determine resource processing production level.
        double productionLevel = 0D;
        if (powerMode.equals(FULL_POWER)) productionLevel = 1D;
        else if (powerMode.equals(POWER_DOWN)) productionLevel = POWER_DOWN_LEVEL;
        
        // Process resources
        processManager.processResources(time, productionLevel);
    } 
    
    /** 
     * Utilizes water for bathing, washing, etc. based on population.
     * @param time amount of time passing (millisols)
     */
    public void waterUsage(double time) {
        
        Settlement settlement = manager.getSettlement();
        double waterUsagePerPerson = (LivingAccommodations.WASH_WATER_USAGE_PERSON_SOL / 1000D) * time;
        double waterUsageSettlement = waterUsagePerPerson * settlement.getCurrentPopulationNum();
        double buildingProportionCap = (double) ACCOMMODATION_CAPACITY / 
            (double) settlement.getPopulationCapacity();
        double waterUsageBuilding = waterUsageSettlement * buildingProportionCap;
        
        Inventory inv = settlement.getInventory();
        double waterUsed = inv.removeResource(Resource.WATER, waterUsageBuilding);
        inv.addResource(Resource.WASTE_WATER, waterUsed);
    }   
}
