/**
 * Mars Simulation Project
 * RoverMission.java
 * @version 3.08 2015-07-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.DriveGroundVehicle;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.OperateVehicle;
import org.mars_sim.msp.core.person.ai.task.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A mission that involves driving a rover vehicle along a series of navpoints.
 * TODO externalize life support strings
 */
public abstract class RoverMission
extends VehicleMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(RoverMission.class.getName());

	// Static members
	public static final int MIN_PEOPLE = 1; // Changed from 2 to 1
	public static final double MIN_STARTING_SETTLEMENT_METHANE = 1000D;

	// Data members
	private Settlement startingSettlement;
	private Map<AmountResource, Double> dessertResources;
	
	/**
	 * Constructor.
	 * @param name the name of the mission.
	 * @param startingMember the mission member starting the mission.
	*/
	protected RoverMission(String name, MissionMember startingMember) {
		// Use VehicleMission constructor.
		super(name, startingMember, MIN_PEOPLE);
	}
	
	/**
	 * Constructor with min people.
	 * @param name the name of the mission.
	 * @param startingMember the mission member starting the mission.
	 * @param minPeople the minimum number of members required for mission.
	*/
	protected RoverMission(String name,  MissionMember startingMember, int minPeople) {
		// Use VehicleMission constructor.
		super(name, startingMember, minPeople);
	}	
	/**
	 * Constructor with min people and rover.
	 * @param name the name of the mission.
	 * @param startingMember the mission member starting the mission.
	 * @param minPeople the minimum number of people required for mission.
	 * @param rover the rover to use on the mission.
	*/
	protected RoverMission(String name, MissionMember startingMember, int minPeople,
			Rover rover) {
		// Use VehicleMission constructor.
		super(name, startingMember, minPeople, rover);
	}
	
	/**
	 * Gets the mission's rover if there is one.
	 * @return vehicle or null if none.
	 */
	public final Rover getRover() {
		return (Rover) getVehicle();
	}

	/**
	 * Sets the starting settlement.
	 * @param startingSettlement the new starting settlement
	 */
	protected final void setStartingSettlement(Settlement startingSettlement) {
		this.startingSettlement = startingSettlement;
		fireMissionUpdate(MissionEventType.STARTING_SETTLEMENT_EVENT);
	}

	/**
	 * Gets the starting settlement.
	 * @return starting settlement
	 */
	public final Settlement getStartingSettlement() {
		return startingSettlement;
	}

//	/**
//	 * The person performs the current phase of the mission.
//	 * @param person the person performing the phase.
//	 * @throws MissionException if problem performing the phase.
//	 */
//	protected void performPhase(Person person) {
//		// if (hasEmergency()) setEmergencyDestination(true);
//		super.performPhase(person);
//	}
//	protected void performPhase(Robot robot) {
//		// if (hasEmergency()) setEmergencyDestination(true);
//		super.performPhase(robot);
//	}
	
	/**
	 * Gets the available vehicle at the settlement with the greatest range.
	 * @param settlement the settlement to check.
	 * @param allowMaintReserved allow vehicles that are reserved for maintenance.
	 * @return vehicle or null if none available.
	 * @throws Exception if error finding vehicles.
	 */
	public static Vehicle getVehicleWithGreatestRange(Settlement settlement,
			boolean allowMaintReserved) {
		Vehicle result = null;

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();

			boolean usable = true;
			if (vehicle.isReservedForMission())
				usable = false;
			if (!allowMaintReserved && vehicle.isReserved())
				usable = false;
			if (!vehicle.getStatus().equals(Vehicle.PARKED))
				usable = false;
			if (vehicle.getInventory().getTotalInventoryMass(false) > 0D)
				usable = false;
			if (!(vehicle instanceof Rover))
				usable = false;

			if (usable) {
				if (result == null)
					result = vehicle;
				else if (vehicle.getRange() > result.getRange())
					result = vehicle;
			}
		}

		return result;
	}

	/**
	 * Checks to see if any vehicles are available at a settlement.
	 * @param settlement the settlement to check.
	 * @param allowMaintReserved allow vehicles that are reserved for maintenance.
	 * @return true if vehicles are available.
	 */
	public static boolean areVehiclesAvailable(Settlement settlement,
			boolean allowMaintReserved) {

		boolean result = false;

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();

			boolean usable = true;
			if (vehicle.isReservedForMission())
				usable = false;
			if (!allowMaintReserved && vehicle.isReserved())
				usable = false;
			if (!vehicle.getStatus().equals(Vehicle.PARKED))
				usable = false;
			if (!(vehicle instanceof Rover))
				usable = false;

			if (vehicle.getInventory().getTotalInventoryMass(false) > 0D)
				usable = false;

			if (usable)
				result = true;
		}

		return result;
	}

	/**
	 * Checks if vehicle is usable for this mission. (This method should be overridden by children)
	 * @param newVehicle the vehicle to check
	 * @return true if vehicle is usable.
	 * @throws MissionException if problem determining if vehicle is usable.
	 */
	protected boolean isUsableVehicle(Vehicle newVehicle) {
		boolean usable = super.isUsableVehicle(newVehicle);
		if (!(newVehicle instanceof Rover))
			usable = false;
		return usable;
	}

	/**
	 * Checks that everyone in the mission is aboard the rover.
	 * @return true if everyone is aboard
	 */
	protected final boolean isEveryoneInRover() {
		boolean result = true;
		Iterator<MissionMember> i = getMembers().iterator();
		while (i.hasNext()) {
			if (i.next().getLocationSituation() != LocationSituation.IN_VEHICLE) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * Checks that no one in the mission is aboard the rover.
	 * @return true if no one is aboard
	 */
	protected final boolean isNoOneInRover() {
		boolean result = true;
		Iterator<MissionMember> i = getMembers().iterator();
		while (i.hasNext()) {
			if (i.next().getLocationSituation() == LocationSituation.IN_VEHICLE) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * Checks if the rover is currently in a garage or not.
	 * @return true if rover is in a garage.
	 */
	protected boolean isRoverInAGarage() {
		return (BuildingManager.getBuilding(getVehicle()) != null);
	}

	/**
	 * Performs the embark from settlement phase of the mission.
	 * @param member the mission member currently performing the mission
	 */
	protected void performEmbarkFromSettlementPhase(MissionMember member) {

		Settlement settlement = getVehicle().getSettlement();
		if (settlement == null)
			throw new IllegalStateException(Msg.getString("RoverMission.log.notAtSettlement",getPhase().getName())); //$NON-NLS-1$

		// Add the rover to a garage if possible.
		if (BuildingManager.getBuilding(getVehicle()) == null) {
			BuildingManager.addToRandomBuilding((Rover) getVehicle(),
					getVehicle().getSettlement());
		}

		// Load vehicle if not fully loaded.
		if (!loadedFlag) {
			if (isVehicleLoaded()) {
				loadedFlag = true;
			}
			else {
				// Check if vehicle can hold enough supplies for mission.
				if (isVehicleLoadable()) {
					if (member.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
						// Load rover
						// Random chance of having person load (this allows person to do other things sometimes)
						if (RandomUtil.lessThanRandPercent(75)) {
							if (BuildingManager.getBuilding(getVehicle()) != null) {
							    // TODO Refactor.
							    if (member instanceof Person) {
							        Person person = (Person) member;
							        assignTask(person, new LoadVehicleGarage(person, getVehicle(), 
							                getRequiredResourcesToLoad(), getOptionalResourcesToLoad(), 
							                getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
							    }
							}
							else {
								// Check if it is day time.
							    SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
							    if ((surface.getSolarIrradiance(member.getCoordinates()) > 0D) || 
							            surface.inDarkPolarRegion(member.getCoordinates())) {
							        // TODO Refactor.
							        if (member instanceof Person) {
							            Person person = (Person) member;
							            assignTask(person, new LoadVehicleEVA(person, getVehicle(), 
							                    getRequiredResourcesToLoad(), getOptionalResourcesToLoad(), 
							                    getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
							        }
							    }
							}
						}
					}
				} else {
					endMission(Msg.getString("RoverMission.log.notLoadable")); //$NON-NLS-1$
					return;
				}
			}
		} else {
			// If person is not aboard the rover, board rover.
			if (member.getLocationSituation() != LocationSituation.IN_VEHICLE
					&& member.getLocationSituation() != LocationSituation.BURIED) {

				// Move person to random location within rover.
				Point2D.Double vehicleLoc = LocalAreaUtil.getRandomInteriorLocation(getVehicle());
				Point2D.Double adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(), 
						vehicleLoc.getY(), getVehicle());
				// TODO Refactor.
				if (member instanceof Person) {
				    Person person = (Person) member;
				    if (Walk.canWalkAllSteps(person, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle())) {
				        assignTask(person, new Walk(person, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle()));
				    }
				    else {
				        logger.severe(Msg.getString("RoverMission.log.unableToEnter",person.getName(),getVehicle().getName())); //$NON-NLS-1$
				        endMission(Msg.getString("RoverMission.log.unableToEnter",person.getName(),getVehicle().getName())); //$NON-NLS-1$
				    }
				}
				else if (member instanceof Robot) {
				    Robot robot = (Robot) member;
                    if (Walk.canWalkAllSteps(robot, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle())) {
                        assignTask(robot, new Walk(robot, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle()));
                    }
                    else {
                        logger.severe(Msg.getString("RoverMission.log.unableToEnter",robot.getName(),getVehicle().getName())); //$NON-NLS-1$
                        endMission(Msg.getString("RoverMission.log.unableToEnter",robot.getName(),getVehicle().getName())); //$NON-NLS-1$
                    }
				}

				if (!isDone() && isRoverInAGarage()) {

					// Store one EVA suit for person (if possible).
					if (settlement.getInventory().findNumUnitsOfClass(EVASuit.class) > 0) {
						EVASuit suit = (EVASuit) settlement.getInventory().findUnitOfClass(EVASuit.class);
						if (getVehicle().getInventory().canStoreUnit(suit, false)) {
							settlement.getInventory().retrieveUnit(suit);
							getVehicle().getInventory().storeUnit(suit);
						}
						else {
							endMission(Msg.getString("RoverMission.log.cannotBeLoaded",suit.getName(),getVehicle().getName())); //$NON-NLS-1$
							return;
						}
					}
				}
			}

			// If rover is loaded and everyone is aboard, embark from settlement.
			if (!isDone() && loadedFlag && isEveryoneInRover()) {

				// Remove from garage if in garage.
				Building garageBuilding = BuildingManager
						.getBuilding(getVehicle());
				if (garageBuilding != null) {
					VehicleMaintenance garage = (VehicleMaintenance) garageBuilding.getFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
					garage.removeVehicle(getVehicle());
				}

				// Embark from settlement
				settlement.getInventory().retrieveUnit(getVehicle());
				setPhaseEnded(true);
			}
		}
	}

//	protected void performEmbarkFromSettlementPhase(Robot robot) {
//
//		Settlement settlement = getVehicle().getSettlement();
//		if (settlement == null)
//			throw new IllegalStateException(Msg.getString("RoverMission.log.notAtSettlement",getPhase().getName())); //$NON-NLS-1$
//
//		// Add the rover to a garage if possible.
//		if (BuildingManager.getBuilding(getVehicle()) == null) {
//			BuildingManager.addToRandomBuilding((Rover) getVehicle(),
//					getVehicle().getSettlement());
//		}
//
//		// Load vehicle if not fully loaded.
//		if (!loadedFlag) {
//			if (isVehicleLoaded()) {
//				loadedFlag = true;
//			}
//			else {
//				// Check if vehicle can hold enough supplies for mission.
//				if (isVehicleLoadable()) {
//					if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
//						// Load rover
//						// Random chance of having robot load (this allows robot to do other things sometimes)
//						if (RandomUtil.lessThanRandPercent(75)) {
//							if (BuildingManager.getBuilding(getVehicle()) != null) {
//								assignTask(robot, new LoadVehicleGarage(robot, getVehicle(), 
//										getRequiredResourcesToLoad(), getOptionalResourcesToLoad(), 
//										getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
//							}
//							else {
//								// Check if it is day time.
//								SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
//								if ((surface.getSolarIrradiance(robot.getCoordinates()) > 0D) || 
//										surface.inDarkPolarRegion(robot.getCoordinates())) {
//									assignTask(robot, new LoadVehicleEVA(robot, getVehicle(), 
//											getRequiredResourcesToLoad(), getOptionalResourcesToLoad(), 
//											getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
//								}
//							}
//						}
//					}
//				} else {
//					endMission(Msg.getString("RoverMission.log.notLoadable")); //$NON-NLS-1$
//					return;
//				}
//			}
//		} else {
//			// If robot is not aboard the rover, board rover.
//			if (robot.getLocationSituation() != LocationSituation.IN_VEHICLE
//					&& robot.getLocationSituation() != LocationSituation.BURIED) {
//
//				// Move robot to random location within rover.
//				Point2D.Double vehicleLoc = LocalAreaUtil.getRandomInteriorLocation(getVehicle());
//				Point2D.Double adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(), 
//						vehicleLoc.getY(), getVehicle());
//				if (Walk.canWalkAllSteps(robot, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle())) {
//					assignTask(robot, new Walk(robot, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle()));
//				}
//				else {
//					logger.severe(Msg.getString("RoverMission.log.unableToEnter",robot.getName(),getVehicle().getName())); //$NON-NLS-1$
//					endMission(Msg.getString("RoverMission.log.unableToEnter",robot.getName(),getVehicle().getName())); //$NON-NLS-1$
//				}
//
//				if (!isDone() && isRoverInAGarage()) {
//
//					// Store one EVA suit for robot (if possible).
//					/*
//					if (settlement.getInventory().findNumUnitsOfClass(EVASuit.class) > 0) {
//						EVASuit suit = (EVASuit) settlement.getInventory().findUnitOfClass(EVASuit.class);
//						if (getVehicle().getInventory().canStoreUnit(suit, false)) {
//							settlement.getInventory().retrieveUnit(suit);
//							getVehicle().getInventory().storeUnit(suit);
//						}
//						else {
//							endMission(Msg.getString("RoverMission.log.cannotBeLoaded",suit.getName(),getVehicle().getName())); //$NON-NLS-1$
//							return;
//						}
//					}
//					*/
//				}
//				
//			}
//
//			// If rover is loaded and everyone is aboard, embark from settlement.
//			if (!isDone() && loadedFlag && isEveryoneInRover()) {
//
//				// Remove from garage if in garage.
//				Building garageBuilding = BuildingManager
//						.getBuilding(getVehicle());
//				if (garageBuilding != null) {
//					VehicleMaintenance garage = (VehicleMaintenance) garageBuilding.getFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
//					garage.removeVehicle(getVehicle());
//				}
//
//				// Embark from settlement
//				settlement.getInventory().retrieveUnit(getVehicle());
//				setPhaseEnded(true);
//			}
//		}
//	}
	
	/**
	 * Performs the disembark to settlement phase of the mission.
	 * @param member the mission member currently performing the mission.
	 * @param disembarkSettlement the settlement to be disembarked to.
	 */
	protected void performDisembarkToSettlementPhase(MissionMember member,
			Settlement disembarkSettlement) {

		Building garageBuilding = null;
		VehicleMaintenance garage = null;

		// If rover is not parked at settlement, park it.
		if ((getVehicle() != null) && (getVehicle().getSettlement() == null)) {
			disembarkSettlement.getInventory().storeUnit(getVehicle());
			getVehicle().determinedSettlementParkedLocationAndFacing();

			// Add vehicle to a garage if available.
			BuildingManager.addToRandomBuilding((GroundVehicle) getVehicle(),
					disembarkSettlement);
			garageBuilding = BuildingManager.getBuilding(getVehicle());
			if (garageBuilding != null)
				garage = (VehicleMaintenance) garageBuilding
				.getFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
		}

		// Have member exit rover if necessary.
		if (member.getLocationSituation() != LocationSituation.IN_SETTLEMENT) {

			// Get closest airlock building at settlement.
		    Building destinationBuilding = null;
		    // TODO Refactor.
		    if (member instanceof Person) {
		        destinationBuilding = (Building) disembarkSettlement.getClosestAvailableAirlock((Person) member).getEntity();
		    }
		    else if (member instanceof Robot) {
		        destinationBuilding = (Building) disembarkSettlement.getClosestAvailableAirlock((Robot) member).getEntity();
		    }
		    
			if (destinationBuilding != null) {
				Point2D destinationLoc = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
				Point2D adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(destinationLoc.getX(), 
						destinationLoc.getY(), destinationBuilding);

				// TODO Refactor.
				if (member instanceof Person) {
				    Person person = (Person) member;
				    if (Walk.canWalkAllSteps(person, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding)) {
				        assignTask(person, new Walk(person, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding));
				    }
				    else {
				        logger.severe(Msg.getString("RoverMission.log.unableWalkBuilding",person.getName(),destinationBuilding.getName())); //$NON-NLS-1$
				        logger.severe(Msg.getString("RoverMission.log.emergencyEnterBuilding",person.getName(),destinationBuilding.getName())); //$NON-NLS-1$
				        if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
				            person.getVehicle().getInventory().retrieveUnit(person);
				        }
				        disembarkSettlement.getInventory().storeUnit(person);
				        BuildingManager.addPersonOrRobotToBuilding(person, destinationBuilding, adjustedLoc.getX(), adjustedLoc.getY());
				    }
				}
				else if (member instanceof Robot) {
                    Robot robot = (Robot) member;
                    if (Walk.canWalkAllSteps(robot, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding)) {
                        assignTask(robot, new Walk(robot, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding));
                    }
                    else {
                        logger.severe(Msg.getString("RoverMission.log.unableWalkBuilding",robot.getName(),destinationBuilding.getName())); //$NON-NLS-1$
                        logger.severe(Msg.getString("RoverMission.log.emergencyEnterBuilding",robot.getName(),destinationBuilding.getName())); //$NON-NLS-1$
                        if (robot.getLocationSituation() == LocationSituation.IN_VEHICLE) {
                            robot.getVehicle().getInventory().retrieveUnit(robot);
                        }
                        disembarkSettlement.getInventory().storeUnit(robot);
                        BuildingManager.addPersonOrRobotToBuilding(robot, destinationBuilding, adjustedLoc.getX(), adjustedLoc.getY());
                    }
                }
			}
			else {
				logger.severe(Msg.getString("RoverMission.log.noHabitat", destinationBuilding)); //$NON-NLS-1$
				endMission(Msg.getString("RoverMission.log.noHabitat", destinationBuilding)); //$NON-NLS-1$
			}
		}

		Rover rover = (Rover) getVehicle();
		if (rover != null) {

			// If any people are aboard the rover who aren't mission members, carry them into the settlement.
			if (isNoOneInRover() && (rover.getCrewNum() > 0)) {
				Iterator<Person> i = rover.getCrew().iterator();
				while (i.hasNext()) {
					Person crewmember = i.next();
					logger.severe(Msg.getString("RoverMission.log.emergencyEnterSettlement",crewmember.getName(),disembarkSettlement.getName())); //$NON-NLS-1$
					rover.getInventory().retrieveUnit(crewmember);
					disembarkSettlement.getInventory().storeUnit(crewmember);
					Building destinationBuilding = null;
					// TODO Refactor
					if (member instanceof Person) {
					    destinationBuilding = (Building) disembarkSettlement.getClosestAvailableAirlock((Person) member).getEntity();
					}
					else if (member instanceof Robot) {
					    destinationBuilding = (Building) disembarkSettlement.getClosestAvailableAirlock((Robot) member).getEntity();
					}
					
					if (destinationBuilding != null) {
					    BuildingManager.addPersonOrRobotToBuildingRandomLocation(crewmember, destinationBuilding);
					}
				}
			}

			// If no one is in the rover, unload it and end phase.
			if (isNoOneInRover()) {

				// Unload rover if necessary.
				boolean roverUnloaded = rover.getInventory().getTotalInventoryMass(false) == 0D;
				if (!roverUnloaded) {
					if (member.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
						// Random chance of having person unload (this allows person to do other things sometimes)
						if (RandomUtil.lessThanRandPercent(50)) {
							if (BuildingManager.getBuilding(rover) != null) {
							    // TODO Refactor.
							    if (member instanceof Person) {
							        Person person = (Person) member;
							        assignTask(person, new UnloadVehicleGarage(person, rover));
							    }
							}
							else {
								// Check if it is day time.
								SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
								if ((surface.getSolarIrradiance(member.getCoordinates()) > 0D) || 
										surface.inDarkPolarRegion(member.getCoordinates())) {
								    // TODO Refactor.
								    if (member instanceof Person) {
								        Person person = (Person) member;
								        assignTask(person, new UnloadVehicleEVA(person, rover));
								    }
								}
							}

							return;
						}
					}
				}
				else {
					// End the phase.

					// If the rover is in a garage, put the rover outside.
					if (isRoverInAGarage()) {
						garageBuilding = BuildingManager.getBuilding(getVehicle());
						garage = (VehicleMaintenance) garageBuilding
								.getFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
						garage.removeVehicle(getVehicle());
					}

					// Leave the vehicle.
					leaveVehicle();
					setPhaseEnded(true);
				}
			}
		} 
		else {
			setPhaseEnded(true);
		}
	}

//	protected void performDisembarkToSettlementPhase(Robot robot,
//			Settlement disembarkSettlement) {
//
//		Building garageBuilding = null;
//		VehicleMaintenance garage = null;
//
//		// If rover is not parked at settlement, park it.
//		if ((getVehicle() != null) && (getVehicle().getSettlement() == null)) {
//			disembarkSettlement.getInventory().storeUnit(getVehicle());
//			getVehicle().determinedSettlementParkedLocationAndFacing();
//
//			// Add vehicle to a garage if available.
//			BuildingManager.addToRandomBuilding((GroundVehicle) getVehicle(),
//					disembarkSettlement);
//			garageBuilding = BuildingManager.getBuilding(getVehicle());
//			if (garageBuilding != null)
//				garage = (VehicleMaintenance) garageBuilding
//				.getFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
//		}
//
//		// Have robot exit rover if necessary.
//		if (robot.getLocationSituation() != LocationSituation.IN_SETTLEMENT) {
//
//			// Get closest airlock building at settlement.
//		    Building destinationBuilding = (Building) disembarkSettlement.getClosestAvailableAirlock(robot).getEntity();
//			if (destinationBuilding != null) {
//				Point2D destinationLoc = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
//				Point2D adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(destinationLoc.getX(), 
//						destinationLoc.getY(), destinationBuilding);
//
//				if (Walk.canWalkAllSteps(robot, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding)) {
//					assignTask(robot, new Walk(robot, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding));
//				}
//				else {
//					logger.severe(Msg.getString("RoverMission.log.unableWalkBuilding",robot.getName(),destinationBuilding.getName())); //$NON-NLS-1$
//					logger.severe(Msg.getString("RoverMission.log.emergencyEnterBuilding",robot.getName(),destinationBuilding.getName())); //$NON-NLS-1$
//					if (robot.getLocationSituation() == LocationSituation.IN_VEHICLE) {
//						robot.getVehicle().getInventory().retrieveUnit(robot);
//					}
//					disembarkSettlement.getInventory().storeUnit(robot);
//					BuildingManager.addPersonOrRobotToBuilding(robot, destinationBuilding, adjustedLoc.getX(), adjustedLoc.getY());
//				}
//			}
//			else {
//				logger.severe(Msg.getString("RoverMission.log.noHabitat", destinationBuilding)); //$NON-NLS-1$
//				endMission(Msg.getString("RoverMission.log.noHabitat", destinationBuilding)); //$NON-NLS-1$
//			}
//		}
//
//		Rover rover = (Rover) getVehicle();
//		if (rover != null) {
//
//			// If any people are aboard the rover who aren't mission members, carry them into the settlement.
//			if (isNoOneInRover() && (rover.getRobotCrewNum() > 0)) {
//				Iterator<Robot> i = rover.getRobotCrew().iterator();
//				while (i.hasNext()) {
//					Robot crewmember = i.next();
//					logger.severe(Msg.getString("RoverMission.log.emergencyEnterSettlement",crewmember.getName(),disembarkSettlement.getName())); //$NON-NLS-1$
//					rover.getInventory().retrieveUnit(crewmember);
//					disembarkSettlement.getInventory().storeUnit(crewmember);
//					Building destinationBuilding = (Building) disembarkSettlement.getClosestAvailableAirlock(robot).getEntity();
//					BuildingManager.addPersonOrRobotToBuildingRandomLocation(crewmember, destinationBuilding);
//				}
//			}
//
//			// If no one is in the rover, unload it and end phase.
//			if (isNoOneInRover()) {
//
//				// Unload rover if necessary.
//				boolean roverUnloaded = rover.getInventory().getTotalInventoryMass(false) == 0D;
//				if (!roverUnloaded) {
//					if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
//						// Random chance of having robot unload (this allows robot to do other things sometimes)
//						if (RandomUtil.lessThanRandPercent(50)) {
//							if (BuildingManager.getBuilding(rover) != null) {
//								assignTask(robot, new UnloadVehicleGarage(robot, rover));
//							}
//							else {
//								// Check if it is day time.
//								SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
//								if ((surface.getSolarIrradiance(robot.getCoordinates()) > 0D) || 
//										surface.inDarkPolarRegion(robot.getCoordinates())) {
//									assignTask(robot, new UnloadVehicleEVA(robot, rover));
//								}
//							}
//
//							return;
//						}
//					}
//				}
//				else {
//					// End the phase.
//
//					// If the rover is in a garage, put the rover outside.
//					if (isRoverInAGarage()) {
//						garageBuilding = BuildingManager.getBuilding(getVehicle());
//						garage = (VehicleMaintenance) garageBuilding
//								.getFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
//						garage.removeVehicle(getVehicle());
//					}
//
//					// Leave the vehicle.
//					leaveVehicle();
//					setPhaseEnded(true);
//				}
//			}
//		} 
//		else {
//			setPhaseEnded(true);
//		}
//	}
	
	/**
	 * Gets a new instance of an OperateVehicle task for the mission member.
	 * @param member the mission member operating the vehicle.
	 * @return an OperateVehicle task for the person.
	 */
	protected OperateVehicle getOperateVehicleTask(MissionMember member,
			TaskPhase lastOperateVehicleTaskPhase) {
		OperateVehicle result = null;
		if (member instanceof Person) {
		    Person person = (Person) member;

		    if (lastOperateVehicleTaskPhase != null) {
		        result = new DriveGroundVehicle(person, getRover(),
		                getNextNavpoint().getLocation(),
		                getCurrentLegStartingTime(), getCurrentLegDistance(),
		                lastOperateVehicleTaskPhase);
		    } 
		    else {
		        result = new DriveGroundVehicle(person, getRover(),
		                getNextNavpoint().getLocation(),
		                getCurrentLegStartingTime(), getCurrentLegDistance());
		    }
		}

		return result;
	}
//	protected OperateVehicle getOperateVehicleTask(Robot robot,
//			TaskPhase lastOperateVehicleTaskPhase) {
//		OperateVehicle result = null;
//		if (lastOperateVehicleTaskPhase != null) {
//			result = new DriveGroundVehicle(robot, getRover(),
//					getNextNavpoint().getLocation(),
//					getCurrentLegStartingTime(), getCurrentLegDistance(),
//					lastOperateVehicleTaskPhase);
//		} else {
//			result = new DriveGroundVehicle(robot, getRover(),
//					getNextNavpoint().getLocation(),
//					getCurrentLegStartingTime(), getCurrentLegDistance());
//		}
//
//		return result;
//	}
	/**
	 * Checks to see if at least one inhabitant a settlement is remaining there.
	 * @param settlement the settlement to check.
	 * @param member the mission member checking
	 * @return true if at least one person left at settlement.
	 */
	protected static boolean atLeastOnePersonRemainingAtSettlement(
			Settlement settlement, MissionMember member) {
		boolean result = false;

		if (settlement != null) {
			Iterator<Person> i = settlement.getInhabitants().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if ((inhabitant != member)
						&& !inhabitant.getMind().hasActiveMission()) {
					result = true;
				}
			}
		}

		return result;
	}

	/**
	 * Checks to see if at least a minimum number of people are available for a mission at a settlement.
	 * @param settlement the settlement to check.
	 * @param minNum minimum number of people required.
	 * @return true if minimum people available.
	 */
	public static boolean minAvailablePeopleAtSettlement(
			Settlement settlement, int minNum) {
		boolean result = false;

		if (settlement != null) {
			int numAvailable = 0;
			Iterator<Person> i = settlement.getInhabitants().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if (!inhabitant.getMind().hasActiveMission())
					numAvailable++;
			}
			if (numAvailable >= minNum)
				result = true;
		}

		return result;
	}

	/**
	 * Checks if there is only one person at the associated settlement and he/she has a serious medical problem.
	 * @return true if serious medical problem
	 */
	protected final boolean hasDangerousMedicalProblemAtAssociatedSettlement() {
		boolean result = false;
		if (getAssociatedSettlement() != null) {
			if (getAssociatedSettlement().getCurrentPopulationNum() == 1) {
				Person person = (Person) getAssociatedSettlement()
						.getInhabitants().toArray()[0];
				if (person.getPhysicalCondition().hasSeriousMedicalProblems())
					result = true;
			}
		}
		return result;
	}

	/**
	 * Checks if the mission has an emergency situation.
	 * @return true if emergency.
	 */
	protected final boolean hasEmergency() {
		boolean result = super.hasEmergency();
		if (hasDangerousMedicalProblemAtAssociatedSettlement())
			result = true;
		return result;
	}

	@Override
	public Map<Resource, Number> getResourcesNeededForTrip(boolean useBuffer,
			double distance) {
		Map<Resource, Number> result = super.getResourcesNeededForTrip(
				useBuffer, distance);

		// Determine estimate time for trip.
		double time = getEstimatedTripTime(useBuffer, distance);
		double timeSols = time / 1000D;

		int crewNum = getPeopleNumber();

		// Determine life support supplies needed for trip.
		double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate()
				* timeSols * crewNum;
		if (useBuffer)
			oxygenAmount *= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
		AmountResource oxygen = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
		result.put(oxygen, oxygenAmount);

		double waterAmount = PhysicalCondition.getWaterConsumptionRate()
				* timeSols * crewNum;
		if (useBuffer)
			waterAmount *= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
		AmountResource water = AmountResource.findAmountResource(LifeSupportType.WATER);
		result.put(water, waterAmount);

		double foodAmount = PhysicalCondition.getFoodConsumptionRate() 
				* timeSols * crewNum; //  * PhysicalCondition.FOOD_RESERVE_FACTOR
		if (useBuffer)
			foodAmount *= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
		AmountResource food = AmountResource.findAmountResource(LifeSupportType.FOOD);
		result.put(food, foodAmount);
		
		return result;
	}
	
	@Override
	public Map<Resource, Number> getOptionalResourcesToLoad() {
	    
	    Map<Resource, Number> result = super.getOptionalResourcesToLoad();
       
	    // Initialize dessert resources if necessary.
	    if (dessertResources == null) {
	        determineDessertResources();
	    }
	    
	    // Add any dessert resources to optional resources to load.
	    Iterator<AmountResource> i = dessertResources.keySet().iterator();
	    while (i.hasNext()) {
	        AmountResource dessert = i.next();
	        double amount = dessertResources.get(dessert);
	        
	        if (result.containsKey(dessert)) {
	            double initialAmount = (double) result.get(dessert);
	            amount += initialAmount;
	        }
	        
	        result.put(dessert, amount);
	    }
	    
	    return result;
	}
	
	/**
	 * Determine an unprepared dessert resource to load on the mission.
	 */
	private void determineDessertResources() {
	    
	    dessertResources = new HashMap<AmountResource, Double>(1);
	    
	    // Determine estimate time for trip.
        double distance = getTotalRemainingDistance();
        double time = getEstimatedTripTime(true, distance);
        double timeSols = time / 1000D;

        int crewNum = getPeopleNumber();
        
        // Determine dessert amount for trip.
        double dessertAmount =  PhysicalCondition.getDessertConsumptionRate() * crewNum * timeSols;
        
        
        // Put together a list of available unprepared dessert resources. 
        List<String> dessertList = new ArrayList<String>();
        String [] availableDesserts = PreparingDessert.getArrayOfDesserts();
        for (String n : availableDesserts) {     
            
            // See if an unprepared dessert resource is available
            boolean isAvailable = Storage.retrieveAnResource(dessertAmount, n, startingSettlement.getInventory(), false);
            if (isAvailable) {
                dessertList.add(n);                    
            }
        }
        
        // Randomly choose an unprepared dessert resource from the available resources.
        AmountResource dessert = null;
        if (dessertList.size() > 0) {
            String dessertName = dessertList.get(RandomUtil.getRandomInt(dessertList.size() - 1));
            dessert = AmountResource.findAmountResource(dessertName);
        }
        
        if (dessert != null) {
            dessertResources.put(dessert, dessertAmount);
        }
	}

	@Override
	public void endMission(String reason) {
		// If at a settlement, associate all members with the settlement.
		Iterator<MissionMember> i = getMembers().iterator();
		while (i.hasNext()) {
			MissionMember member = i.next();
			if (member.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			    member.setAssociatedSettlement(member.getSettlement());
			}
		}

		super.endMission(reason);
	}

	/**
	 * Checks if there is an available backup rover at the settlement for the mission.
	 * @param settlement the settlement to check.
	 * @return true if available backup rover.
	 */
	public static boolean hasBackupRover(Settlement settlement) {
		int availableVehicleNum = 0;
		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if ((vehicle instanceof Rover) && !vehicle.isReservedForMission())
				availableVehicleNum++;
		}
		return (availableVehicleNum >= 2);
	}

	/**
	 * Checks if there are enough basic mission resources at the settlement to start mission.
	 * @param settlement the starting settlement.
	 * @return true if enough resources.
	 */
	public static boolean hasEnoughBasicResources(Settlement settlement) {
		boolean hasBasicResources = true;

		Inventory inv = settlement.getInventory();
		try {
			AmountResource methane = AmountResource.findAmountResource("methane");
			if (inv.getAmountResourceStored(methane, false) < 100D) {
				hasBasicResources = false;
			}
			AmountResource oxygen = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
			if (inv.getAmountResourceStored(oxygen, false) < 50D) {
				hasBasicResources = false;
			}
			AmountResource water = AmountResource.findAmountResource(LifeSupportType.WATER);
			if (inv.getAmountResourceStored(water, false) < 50D) {
				hasBasicResources = false;
			}
			AmountResource food = AmountResource.findAmountResource(LifeSupportType.FOOD);
			if (inv.getAmountResourceStored(food, false) < 50D) {
				hasBasicResources = false;
			}
		} 
		catch (Exception e) {
			e.printStackTrace(System.err);
		}

		return hasBasicResources;
	}
	
    @Override
    protected void recruitMembersForMission(MissionMember startingMember) {
        super.recruitMembersForMission(startingMember);

        // Make sure there is at least one person left at the starting
        // settlement.
        if (!atLeastOnePersonRemainingAtSettlement(getStartingSettlement(),
                startingMember)) {
            // Remove last person added to the mission.
            Person lastPerson = null;
            Iterator<MissionMember> i = getMembers().iterator();
            while (i.hasNext()) {
                MissionMember member = i.next();
                if (member instanceof Person) {
                    lastPerson = (Person) member;
                }
            }

            if (lastPerson != null) {
                lastPerson.getMind().setMission(null);
                if (getMembersNumber() < getMinMembers()) {
                    endMission("Not enough members.");
                }
                else if (getPeopleNumber() == 0) {
                    endMission("No people on mission.");
                }
            }
        }
    }

	@Override
	public void destroy() {
		super.destroy();

		startingSettlement = null;
	}
}