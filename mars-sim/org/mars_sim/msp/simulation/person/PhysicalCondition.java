/**
 * Mars Simulation Project
 * @author Barry Evans
 * @version 2.74
 */

package org.mars_sim.msp.simulation.person;

import org.mars_sim.msp.simulation.*;
import java.io.Serializable;

/**
 * This class represents the Physical Condition of a Person. It is models the
 * Persons health and physical charactertics.
 */
public class PhysicalCondition implements Serializable {


    private boolean isAlive;            // Is the person alive
    private MedicalComplaint illness;   // Injury/Illness effecting person
    private boolean isRecovering;       // Persion is recovering
    private double illnessDuration;     // Time Physicla condition has been ill
    private double fatigue;             // Person's fatigue level
    private double hunger;              // Person's hunger level
    private MedicalManager medic;       // Simulation Medical manager

    /**
     * Construct a Physical Condition instance.
     *
     * @param mars main simulation control.
     */
    public PhysicalCondition(VirtualMars mars) {
        isAlive = true;
        illness = null;
        isRecovering = false;
        illnessDuration = 0;

        medic = mars.getMedicalManager();
        fatigue = RandomUtil.getRandomDouble(1000D);
        hunger = RandomUtil.getRandomDouble(1000D);
    }

    /**
     * The Physical condition should be updated to reflect a passing of time.
     * This method has to check the recover or degradation of any current
     * illness. The progression of this time period may result in the illness
     * turning fatal.
     * It also updated the hunder and fatigue status
     *
     * @param time amount of time passing (in millisols)
     * @param support Life support system.
     * @param props Simulation properties.
     * @return True still alive.
     */
    boolean timePassing(double time, LifeSupport support,
                        SimulationProperties props) {

        // If already has an illness then update the time period.
        if (illness != null) {
            illnessDuration += time;

            // Recoving so has the recovery period expired
            if (isRecovering) {
                if (illnessDuration > illness.getRecoveryPeriod()) {
                    illness = null;
                }
            }
            else if (illnessDuration > illness.getDegradePeriod()) {
                // Illness has moved to next phase, if null then dead
                MedicalComplaint nextPhase = illness.getNextPhase();
                if (nextPhase == null) {
                    setDead();
                }
                else {
                    setProblem(nextPhase);
                }
            }
        }

        // Consume necessary oxygen and water.
        consumeOxygen(support, props.getPersonOxygenConsumption() * (time / 1000D));
        consumeWater(support, props.getPersonWaterConsumption() * (time / 1000D));

        // Build up fatigue & hunger for given time passing.
        fatigue += time;
        hunger += time;


        return isAlive;
    }

    /** Person consumes given amount of food
     *  @param amount amount of food to consume (in kg).
     *  @param support Life System system suporting Person.
     *  @param props Simulation proerties.
     */
    public void consumeFood(double amount, LifeSupport support,
                            SimulationProperties props) {
        double amountRecieved = support.removeFood(amount);

        if (amountRecieved != amount) {
            setProblem(medic.getStarvation());
        }
        else {
            // If Person is straving, then start recovery has there is food
            if ((illness != null) && illness.equals(medic.getStarvation())) {
                startRecovery();
            }
        }
    }

    /** Person consumes given amount of oxygen
     *  @param support Life support system providing water.
     *  @param amount amount of oxygen to consume (in kg)
     */
    private void consumeOxygen(LifeSupport support, double amount) {
        double amountRecieved = support.removeOxygen(amount);

        if (amountRecieved != amount) {
            setProblem(medic.getLackOfOxygen());
        }
        else {
            // If Person is straving, then start recovery has there is oxygen
            if ((illness != null) && illness.equals(medic.getLackOfOxygen())) {
                startRecovery();
            }
        }
    }

    /** Person consumes given amount of water
     *
     *  @param support Life support system providing water.
     *  @param amount amount of water to consume (in kg)
     */
    private void consumeWater(LifeSupport support, double amount) {
        double amountRecieved = support.removeWater(amount);

        if (amountRecieved != amount) {
            setProblem(medic.getDehydration());
        }
        else {
            // If Person is straving, then start recovery has there is food
            if ((illness != null) && illness.equals(medic.getDehydration())) {
                startRecovery();
            }
        }
    }

    /**
     * Predicate to check if the Person is alive.
     * @return Boolean of alive state.
     */
    public boolean getAlive() {
        return isAlive;
    }

    /** Gets the person's fatigue level
     *  @return person's fatigue
     */
    public double getFatigue() {
        return fatigue;
    }

    /**
     * Define the fatigue setting for this person
     * @param fatigue New fatigue.
     */
    void setFatigue(double fatigue) {
        this.fatigue = fatigue;
    }

    /** Gets the person's hunger level
     *  @return person's hunger
     */
    public double getHunger() {
        return hunger;
    }

    /**
     * Define the hunger setting for this person
     * @param hunger New hunger.
     */
    void setHunger(double hunger) {
        this.hunger = hunger;
    }

    /**
     * Get the associated medical complaint
     */
    public MedicalComplaint getIllness() {
        return illness;
    }

    /**
     * This Person is now dead.
     */
    private void setDead() {
        fatigue = 0;
        hunger = 0;
        isAlive = false;
    }

    /**
     * Get a string description of the health situation.
     * @return A string containing the current illness if any.
     */
    public String getHealthSituation() {
        String situation = "Well";
        if (illness != null) {
            if (isRecovering) {
                situation = "Recovering - " + illness.getName();
            }
            else if (!isAlive) {
                situation = "Dead - " + illness.getName();
            }
            else {
                situation = illness.getName();
            }
        }
        return situation;
    }

    /**
     * Get a rating of the current health situation. This is a percentage value
     * and may either represent the recovering or degradation of the current
     * illness.
     * @return Percentage value.
     */
    public int getHealthRating() {
        int rating = 0;
        if (illness != null) {
            double max = (isRecovering ? illness.getRecoveryPeriod() :
                                         illness.getDegradePeriod());
            rating = (int)((illnessDuration * 100D) / max);
        }
        return rating;
    }

    /**
     * This physical condition is being effected by a Medical Complaint. If
     * there is an assigned illness already, the specified complaint must
     * have a higher seriousness rating to take control.
     *
     * @param complaint Complaint effecting the condition.
     */
    private void setProblem(MedicalComplaint complaint) {
        // If the new complaint is less serious than the current, reject.
        if ((illness != null) && (illness.getSeriousness() >=
                                  complaint.getSeriousness())) {
            return;
        }

        illness = complaint;
        illnessDuration = 0;

        // If no degrade period, then can do self heel
        isRecovering = (complaint.getDegradePeriod() == 0D);
    }

    /**
     * This is now moving to a recovery state.
     */
    public void startRecovery() {
        illnessDuration = 0;
        isRecovering = true;
    }
}