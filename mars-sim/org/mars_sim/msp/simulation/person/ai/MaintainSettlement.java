/**
 * Mars Simulation Project
 * MaintainSettlement.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import java.io.Serializable;

/** The MaintainSettlement class is a task for cleaning, organizing and performing
 *  preventive maintenance on a settlement.
 *  The duration of the task is by default chosen randomly, up to 200 millisols.
 *
 *  Note: Preventive maintenance might affect settlement mechanical failures when
 *  they are implemented.
 */
class MaintainSettlement extends Task implements Serializable {

    // Data members
    private double duration; // The predetermined duration of task in millisols

    /** Constructs a MaintainSettlement object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public MaintainSettlement(Person person, VirtualMars mars) {
        super("Performing Settlement Maintenance", person, mars);

        duration = RandomUtil.getRandomInt(200);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, VirtualMars mars) {
        if (person.getSettlement() != null) return 50D;
        else return 0D;
    }

    /** This task simply waits until the set duration of the task is complete, then ends the task. 
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        timeCompleted += time;
        if (timeCompleted > duration) {
            done = true;
            return timeCompleted - duration;
        }
        else return 0;
    }
}
