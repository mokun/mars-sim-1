/**
 * Mars Simulation Project
 * MasterClock.java
 * @version 2.73 2001-12-04
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;

/** The MasterClock represents the simulated time clock on virtual
 *  Mars. Virtual Mars has only one master clock. The master clock
 *  delivers a clock pulse the virtual Mars every second or so, which
 *  represents a pulse of simulated time.  All actions taken with
 *  virtual Mars and its units are synchronized with this clock pulse.
 */
public class MasterClock implements Runnable, Serializable {

    // Data members
    private VirtualMars mars;     // Virtual Mars
    private MarsClock marsTime;   // Martian Clock
    private EarthClock earthTime; // Earth Clock
    private UpTimer uptimer;      // Uptime Timer
    private boolean keepRunning;  // Runnable flag

    // Sleep duration in milliseconds 
    private final static int SLEEP_DURATION = 1000;
    
    static final long serialVersionUID = -1688463735489226494L;

    /** Constructs a MasterClock object
     *  @param mars the virtual mars that uses the clock
     */
    public MasterClock(VirtualMars mars) {
        // Initialize data members
        this.mars = mars;

        // Create a Martian clock
        marsTime = new MarsClock();
	
        // Create an Earth clock
        earthTime = new EarthClock();

        // Create an Uptime Timer
        uptimer = new UpTimer();
    }

    /** Returns the Martian clock
     *  @return Martian clock instance
     */
    public MarsClock getMarsClock() {
        return marsTime;
    }

    /** Returns the Earth clock
     *  @return Earth clock instance
     */
    public EarthClock getEarthClock() {
        return earthTime;
    }

    /** Returns uptime timer
     *  @return uptimer instance
     */
    public UpTimer getUpTimer() {
        return uptimer;
    }

    /** Gets the time pulse length
     *  @return time pulse length in millisols
     */
    public double getTimePulse() {

        // Get time ratio from simulation properties.
        double timeRatio = mars.getSimulationProperties().getTimeRatio();

        double timePulse;
        if (timeRatio > 0D) {
            double timePulseSeconds = timeRatio * (SLEEP_DURATION / 1000D);
            timePulse = MarsClock.convertSecondsToMillisols(timePulseSeconds);
        }
        else timePulse = 1D;
    
        return timePulse;
    }

    /** Run clock */
    public void run() {
        keepRunning = true;

        // Keep running until told not to
        while (keepRunning) {
            try {
                Thread.currentThread().sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {}

            //Increment the uptimer
            uptimer.addTime(SLEEP_DURATION);

            // Get the time pulse length in millisols.
            double timePulse = getTimePulse();

            // Send virtual Mars a clock pulse representing the time pulse length (in millisols).
            mars.clockPulse(timePulse);

            // Add time pulse length to Earth and Mars clocks. 
            earthTime.addTime(MarsClock.convertMillisolsToSeconds(timePulse));
            marsTime.addTime(timePulse);
        }
    }

    /**
     * Stop the clock 
     */
    public void stop() {
        keepRunning = false;
    }
}
