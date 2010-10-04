/**
 * Mars Simulation Project
 * UpTimer.java
 * @version 2.84 2008-04-17
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;
import java.util.Formatter;
import java.util.Locale;

/** The UpTimer class keeps track of how long an instance of the simulation 
 *  has been running in real time.
 */
public class UpTimer implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -4992839027918134952L;
	/**
	 * 
	 */
	private long thiscall=System.currentTimeMillis();
    private long lastcall = thiscall;
	
    private final int secspmin = 60, secsphour = 3600, secspday = 86400, secsperyear = 31536000;
	private long years,days,hours,minutes,seconds;
	
	// Data members
    private long uptime = 1; //in case it gets divided by 0 right away
    private long utsec = 0;

	private boolean paused = false;

    public UpTimer() 
    {
    	
    }
    
    /**
     * This method adds a period of time to the running time of the 
     * simulation.
     * @param period Extra time the simulation is running. (milliseconds)
     */
    public void addTime(long period) {
        //uptime += period;
    	/*
    	 * I left a placeholder function so callers would not break. 
    	 * It's perhaps better to base elapsed time on the system millisecond clock. 
    	 * */
    }

    /** Reportsthe amount of time the simulation has been running, as a String.  
     *  @return simulation running time formatted in a string. ex "6 days 5:32:58"
     */
    public String getUptime() {
    	utsec = getUptimeMillis()/1000;
   		days = (int)((utsec%secsperyear)/secspday);		
   		hours=(int)((utsec%secspday)/secsphour);
   		minutes=(int)((utsec%secsphour)/secspmin);
   		seconds=(int)((utsec%secspmin));

        String minstr = "" + minutes;
        if (minutes < 10) minstr = "0" + minutes;

        String secstr = "" + seconds;
        if (seconds < 10) secstr = "0" + seconds;

        String daystr = "";
        if (days == 1) daystr = "" + days + " day ";
        else {daystr = "" + days + " days ";}

        String hourstr = "" + hours;
        return daystr + hourstr + ":" + minstr + ":" + secstr;
    }
    
    public long getUptimeMillis() {

    	if (paused ) 
    	{	return uptime;	} 
    	else {
        	//uptime = System.currentTimeMillis()-firstcall;
    			thiscall = System.currentTimeMillis();
    	    	uptime = uptime + ( thiscall-lastcall);
    	    	lastcall = thiscall;
        	return uptime ;
    	}
    }
    
    public boolean isPaused() {return paused;}
	
    public void setPaused(boolean isPaused) {
		paused = isPaused;
		if (isPaused) {
			
		} else {
			lastcall = System.currentTimeMillis();
		}
	}
}