//************************** Greenhouse Facility **************************
// Last Modified: 5/24/00

// The GreenhouseFacility class represents the greenhouses in a settlement.
// It defines the amount of fresh and dried foods generated by the greenhouses.

// Every settlement should have greenhouses.

public class GreenhouseFacility extends Facility {

	// Data members
	
	private float fullHarvestAmount;      // Number of number of food units the greenhouse can produce at full harvest.
	private float workLoad;                // Number of work-hours tending greenhouse required during growth period for full harvest.
	private float growingWork;             // Number of work-hours completed for growing phase.
	private float workCompleted;           // Number of work-hours completed for current phase.
	private float growthPeriod;            // Number of days for growth period.
	private float growthPeriodCompleted;   // Number of days completed in current growth period..
	private String phase;                  // "Inactive", "Planting", "Growing" or "Harvesting"

	// Constructor for random creation.

	public GreenhouseFacility(FacilityManager manager) {
	
		// Use Facility's constructor.
		
		super(manager, "Greenhouse");
	
		// Initialize data members
		
		workLoad = 125F;
		workCompleted = 0F;
		growthPeriod = 20F;
		growthPeriodCompleted = 0F;
		phase = "Inactive";
	
		// Randomly determine full harvest amount.
		
		fullHarvestAmount = 10 + RandomUtil.getRandomInteger(20);
	}
	
	// Constructor for set values (used later when facilities can be built or upgraded.)
	
	public GreenhouseFacility(FacilityManager manager, float workLoad, float growthPeriod, float fullHarvestAmount) {
	
		// Use Facility's constructor.
		
		super(manager, "Greenhouse");
		
		// Initialize data members.
		
		this.workLoad = workLoad;
		this.growthPeriod = growthPeriod;
		this.fullHarvestAmount = fullHarvestAmount;
		workCompleted = 0F;
		growthPeriodCompleted = 0F;
		phase = "Inactive";
	}
	
	// Returns the harvest amount of the greenhouse.
	
	public float getFullHarvestAmount() { return fullHarvestAmount; }
	
	// Returns the work load of the greenhouse. (in work-hours)
	
	public float getWorkLoad() { return workLoad; }
	
	// Returns the work completed in this cycle in the growing phase.
	
	public float getGrowingWork() { return growingWork; }
	
	// Returns the growth period of the greenhouse. (in days)
	
	public float getGrowthPeriod() { return growthPeriod; }
	
	// Returns the current work completed on the current phase. (in work-hours)
	
	public float getWorkCompleted() { return workCompleted; }
	
	// Returns the time completed of the current growth cycle. (in days)
	
	public float getTimeCompleted() { return growthPeriodCompleted; }
	
	// Returns true if a harvest cycle has been started.
	
	public String getPhase() { return phase; }
	
	// Adds work to the work completed on a growth cycle.
	
	public void addWorkToGrowthCycle(int seconds) { 
		
		float plantingWork = 4F * 60F * 60F;
		float harvestingWork = (1F * fullHarvestAmount) * 60F * 60F;
		float workInPhase = (workCompleted * 60F * 60F) + seconds;
		
		if (phase.equals("Inactive")) phase = "Planting";
		
		if (phase.equals("Planting")) {
			if (workInPhase >= plantingWork) {
				workInPhase -= plantingWork;
				phase = "Growing";
			}
		}
		
		if (phase.equals("Growing")) growingWork = workInPhase / (60F * 60F);
		
		if (phase.equals("Harvesting")) {
			if (workInPhase >= harvestingWork) {
				workInPhase -= harvestingWork;
				double foodProduced = fullHarvestAmount * (growingWork / workLoad);
				((StoreroomFacility) manager.getFacility("Storerooms")).addFood(foodProduced);
				phase = "Planting";
				growingWork = 0F;
				growthPeriodCompleted = 0F;
				System.out.println(manager.getSettlement().getName() + " has harvested " + foodProduced + " food units.");
			}
		}
		
		workCompleted = workInPhase / (60F * 60F);
	}
	
	// Returns the UI panel for this facility.
	
	public FacilityPanel getUIPanel(MainDesktopPane desktop) { return new GreenhouseFacilityPanel(this, desktop); }
	
	// Override Facility's timePasses method to allow for harvest cycle.
	
	public void timePasses(int seconds) { 	
		
		if (phase.equals("Growing")) {
			growthPeriodCompleted += (seconds / (60F * 60F * 25F));
			if (growthPeriodCompleted >= growthPeriod) phase = "Harvesting";
		}
	}
}	

// Mars Simulation Project
// Copyright (C) 2000 Scott Davis
//
// For questions or comments on this project, email:
// mars-sim-users@lists.sourceforge.net
//
// or visit the project's Web site at:
// http://mars-sim@sourceforge.net
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA