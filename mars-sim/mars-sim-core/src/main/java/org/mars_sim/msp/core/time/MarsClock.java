/**
 * Mars Simulation Project
 * MarsClock.java
 * @version 3.07 2015-02-24

 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.OrbitInfo;

/** The MarsClock class keeps track of Martian time.
 *  This uses Shaun Moss's Mars Calendar, which is
 *  described at http://www.virtualmars.net/Time.asp.
 */
public class MarsClock implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MarsClock.class.getName());

	// The Mars tropical year is 686.9726 day or 668.5921 sol.
	//  A Mars solar day has a mean period of 24 hours 39 minutes 35.244 seconds, and is customarily referred to as a "sol" in order to distinguish this from the roughly 3% shorter solar day on Earth.
	// The Mars sidereal day, as measured with respect to the fixed stars, is 24h 37m 22.663s, as compared with 23h 56m 04.0905s for Earth.

	// Martian calendar static members
	public static final int SOLS_IN_ORBIT_NON_LEAPYEAR = 668;
    public static final int SOLS_IN_ORBIT_LEAPYEAR = 669;
    private static final int MONTHS_IN_ORBIT = 24;
    private static final int SOLS_IN_MONTH_SHORT = 27;
    public static final int SOLS_IN_MONTH_LONG = 28;
    // private static final int WEEKS_IN_ORBIT = 96;
    // private static final int WEEKS_IN_MONTH = 4;
    private static final int SOLS_IN_WEEK_SHORT = 6;
    private static final int SOLS_IN_WEEK_LONG = 7;
    // private static final int MILLISOLS_IN_SOL = 1000;
    public static final int NORTHERN_HEMISPHERE = 1;
    public static final int SOUTHERN_HEMISPHERE = 2;

    // Note: the summer solstice marks the longest day of the calendar year and the beginning of summer in the "Northern Hemisphere".
    // The solar longitude Ls is the Mars-Sun angle, measured from the Northern Hemisphere spring equinox where L_s=0.
    // Ls=90 corresponds to summer solstice, just as L_s=180 marks the autumn equinox and L_s=270 the winter solstice (all relative to the northern hemisphere).
    // see http://www-mars.lmd.jussieu.fr/mars/time/solar_longitude.html
    private static final int SUMMER_SOLSTICE = 168;
	private static final int AUTUMN_EQUINOX = 346;
	private static final int WINTER_SOLSTICE = 489;
	private static final int SPRING_EQUINOX = 643; // or on the -25th sols

    // Martian/Gregorian calendar conversion
    public static final double SECONDS_IN_MILLISOL = 88.775244;

    public static final int THE_FIRST_SOL = 9353;

    // Martian calendar static strings
    private static final String[] MONTH_NAMES = { "Adir", "Bora", "Coan", "Detri",
        "Edal", "Flo", "Geor", "Heliba", "Idanon", "Jowani", "Kireal", "Larno",
        "Medior", "Neturima", "Ozulikan", "Pasurabi", "Rudiakel", "Safundo", "Tiunor",
        "Ulasja", "Vadeun", "Wakumi", "Xetual", "Zungo" };

    private static final String[] WEEK_SOL_NAMES = { "Solisol", "Phobosol", "Deimosol",
        "Terrasol", "Hermesol", "Venusol", "Jovisol" };

    // Data members
    private int orbit;
    private int month;
    private int sol;
    private double millisol;

    private OrbitInfo orbitInfo;

    /**
     * Constructor with date string parameter.
     * @param dateString format: "orbit-month-sol:millisol"
     * @throws Exception if dateString is invalid.
     */
    public MarsClock(String dateString) {
		//logger.info("MarsClock's constructor is on " + Thread.currentThread().getName() + " Thread");

        // Set initial date to dateString. ex: "15-Adir-01:000.000"
        String orbitStr = dateString.substring(0, dateString.indexOf("-"));
        orbit = Integer.parseInt(orbitStr);
        if (orbit < 0) throw new IllegalStateException("Invalid orbit number: " + orbit);

        String monthStr = dateString.substring(dateString.indexOf("-") + 1, dateString.lastIndexOf("-"));
        month = 0;
        for (int x=0; x < MONTH_NAMES.length; x++) {
        	if (monthStr.equals(MONTH_NAMES[x])) month = x + 1;
        }
        if ((month < 1) || (month > MONTH_NAMES.length)) throw new IllegalStateException("Invalid month: " + monthStr);

        String solStr = dateString.substring(dateString.lastIndexOf("-") + 1, dateString.indexOf(":"));
        sol = Integer.parseInt(solStr);
        if (sol < 1) throw new IllegalStateException("Invalid sol number: " + sol);

        String millisolStr = dateString.substring(dateString.indexOf(":") + 1);
        millisol = Double.parseDouble(millisolStr);
        if (millisol < 0D) throw new IllegalStateException("Invalid millisol number: " + millisol);

        if (orbitInfo == null)
        	orbitInfo = Simulation.instance().getMars().getOrbitInfo();

    }

    /** Constructs a MarsClock object with a given time
     *  param orbit current orbit
     *  param month current month
     *  param sol current sol
     *  param millisol current millisol
     */
    public MarsClock(int orbit, int month, int sol, double millisol) {
		//logger.info("MarsClock's constructor is on " + Thread.currentThread().getName() + " Thread");

    	// Set date/time to given parameters.
        this.orbit = orbit;
        this.month = month;
        this.sol = sol;
        this.millisol = millisol;

        if (orbitInfo == null)
        	orbitInfo = Simulation.instance().getMars().getOrbitInfo();

    }

    /** Converts seconds to millisols
     *  @param seconds decimal number of seconds
     *  @return equivalent number of millisols
     */
    public static double convertSecondsToMillisols(double seconds) {
        return seconds / SECONDS_IN_MILLISOL;
    }

    /** Converts millisols to seconds
     *  @param millisols decimal number of millisols
     *  @return equivalent number of seconds
     */
    public static double convertMillisolsToSeconds(double millisols) {
        return millisols * SECONDS_IN_MILLISOL;
    }

    /** Returns the time difference between two Mars clock instances.
     *  @param firstTime first Mars clock instance
     *  @param secondTime second Mars clock instance
     *  @return time difference in millisols
     */
    public static double getTimeDiff(MarsClock firstTime, MarsClock secondTime) {
        return getTotalMillisols(firstTime) - getTotalMillisols(secondTime);
    }

    /**
     * Gets the names of the Martian months.
     * @return array of month names.
     */
    public static String[] getMonthNames() {
        return Arrays.copyOf(MONTH_NAMES, MONTH_NAMES.length);
    }

    /**
     * Gets the names of the Martian sols of the week.
     * @return array of week sol names.
     */
    public static String[] getWeekSolNames() {
        return Arrays.copyOf(WEEK_SOL_NAMES, WEEK_SOL_NAMES.length);
    }


    /** Returns the number of sols of that given year
     *  @return the number of sol as an integer
     */
    // 2015-01-28 Added getSolOfYear()
    public static int getSolOfYear(MarsClock time) {
    	int result = 0;

        // Add sols up to current month
        for (int x = 1; x < time.month; x++)
            result += MarsClock.getSolsInMonth(x, time.orbit) ;

        // Add sols up to current sol
        result += time.sol  ;

        return result;
    }

    /** Returns the total number of sols since the start of the simulation
     *  @return the total number of sol as an integer
     */
    // 2015-02-09 Added getTotalSol
    public static int getTotalSol(MarsClock time) {

    	int result = 0;

        // Add sols up to current orbit
        for (int x=1; x < time.orbit; x++) {
            if (MarsClock.isLeapOrbit(x)) result += SOLS_IN_ORBIT_LEAPYEAR;
            else result += SOLS_IN_ORBIT_NON_LEAPYEAR;
        }

        // Add sols up to current month
        for (int x=1; x < time.month; x++)
            result += MarsClock.getSolsInMonth(x, time.orbit) ;

        // Add sols up to current sol
        result += time.sol  ;

        result = result - THE_FIRST_SOL;

        return result;
    }

    /** Returns the number of sols for a given month and orbit.
     *  @param month the month number
     *  @param orbit the orbit number
     */
    public static int getSolsInMonth(int month, int orbit) {

        // Standard month has 28 sols.
        int result = SOLS_IN_MONTH_LONG;

        // If month number is divisible by 6, month has 27 sols
        if ((month % 6) == 0) result = SOLS_IN_MONTH_SHORT;

        // If leap orbit and month number is 24, month has 28 sols
        if ((month == 24) && isLeapOrbit(orbit)) result = SOLS_IN_MONTH_LONG;

        return result;
    }

    /** Returns true if orbit is a leap orbit, false if not.
     *  @param orbit the orbit number
     */
    public static boolean isLeapOrbit(int orbit) {
        boolean result = false;

        // If an orbit is divisable by 10 it is a leap orbit
        if ((orbit % 10) == 0) result = true;

        // If an orbit is divisable by 100, it is not a leap orbit
        if ((orbit % 100) == 0) result = false;

        // If an orbit is divisable by 500, it is a leap orbit
        if ((orbit % 500) == 0) result = true;

        return result;
    }

    /**
     * Adds time to the calendar
     * Note: negative time should be used to subtract time.
     * @param addedMillisols millisols to be added to the calendar
     */
    public void addTime(double addedMillisols) {

        millisol += addedMillisols;

        if (addedMillisols > 0D) {
            while (millisol >= 1000D) {
                millisol -= 1000D;
                sol += 1;
                if (sol > getSolsInMonth(month, orbit)) {
                    sol = 1;
                    month += 1;
                    if (month > MONTHS_IN_ORBIT) {
                        month = 1;
                        orbit += 1;
                    }
                }
            }
        }
        else if (addedMillisols < 0D) {
            while (millisol < 0D) {
                millisol += 1000D;
                sol -= 1;
                if (sol < 1) {
                    month -= 1;
                    if (month < 1) {
                        month = MONTHS_IN_ORBIT;
                        orbit -= 1;
                    }
                    sol = getSolsInMonth(month, orbit);
                }
            }
        }
    }

    /** Returns formatted time stamp string.
     *  e.g.. "13-Adir-05  056.349"
     *  @return formatted time stamp string
     */
    public String getDateTimeStamp() {
        return new StringBuilder(getDateString()).append("  ").append(getTimeString()).toString();
    }

    /** Returns formatted time stamp string
     *  e.g. "13-Adir-05 0056"
     *  @return formatted String
     */
    public static String getDateTimeStamp(MarsClock time) {
        return new StringBuilder(getDateString(time)).append(" ").append(getMillisolString(time)).toString();
    }


    /**
     * Gets the current date string.
     * ex. "13-Adir-05"
     * @return current date string
     */
    public String getDateString() {
        StringBuilder result = new StringBuilder();

        // Append orbit
        result.append(orbit).append("-").append(getMonthName()).append("-");

        if(sol < 10){
            result.append("0");
        }
        result.append(sol);
//        // Append sol of month
//        String solString = "" + sol;
//        if (solString.length() == 1) solString = "0" + solString;
//        result.append(solString);

        return result.toString();
    }

    /**
     * Gets the date string of a given time.
     * ex. "13-Adir-05"
     * @return date string
     */
    public static String getDateString(MarsClock time) {
        StringBuilder result = new StringBuilder();
        int orbit = time.getOrbit();
        int sol = time.getSolOfMonth();
        String month = time.getMonthName();

        // Append orbit
        result.append(orbit).append("-").append(month).append("-");

        if(sol < 10){
            result.append("0");
        }
        result.append(sol);

        return result.toString();
    }

    /** Returns the current time string.
     *  ex. "05:056.349"
     */
    public String getTimeString() {
        StringBuilder b = new StringBuilder();
        double tb = Math.floor(millisol * 1000D) / 1000D;
//        String result = "" + tb;
        b.append(tb);
        if (millisol < 100D) {
            b.insert(0,"0");
//            result = "0" + result;
        }
        if (millisol < 10D) {
            b.insert(0,"0");
//            result = "0" + result;
        }
        while (b.length() < 7){
            b.append("0");
//            result += "0";
        }

        return b.toString();
    }

    /** Returns the time string of a given time.
     *  ex. "056"
     */
    public static String getMillisolString(MarsClock time) {
    	   StringBuilder b = new StringBuilder();
    	   int millisol = (int) time.getMillisol();

//           String result = "" + tb;
           b.append(millisol);
           if (millisol < 100D) {
               b.insert(0,"0");
//               result = "0" + result;
           }
           if (millisol < 10D) {
               b.insert(0,"0");
//               result = "0" + result;
           }
           while (b.length() < 3){
               b.append("0");
//               result += "0";
           }

           return b.toString();
    }


    /** Returns the total number of millisols of a given time.
     *  @param time
     */
    public static double getTotalMillisols(MarsClock time) {
        double result = 0D;

        // Add millisols up to current orbit
        for (int x=1; x < time.orbit; x++) {
            if (MarsClock.isLeapOrbit(x)) result += SOLS_IN_ORBIT_LEAPYEAR * 1000D;
            else result += SOLS_IN_ORBIT_NON_LEAPYEAR * 1000D;
        }

        // Add millisols up to current month
        for (int x=1; x < time.month; x++)
            result += MarsClock.getSolsInMonth(x, time.orbit) * 1000D;

        // Add millisols up to current sol
        result += (time.sol - 1) * 1000D;

        // Add millisols in current sol
        result += time.millisol;

		//System.out.println("MarsClock : result : " + result);

        return result;
    }

    /** Returns the name of the current month.
     *  @return name of the current month
     */
    public String getMonthName() {
        return MONTH_NAMES[month - 1];
    }

    /** Returns the orbit
     *  @return the orbit as an integer
     */
    public int getOrbit() { return orbit; }

    /** Returns the month (1 - 24)
     *  @return the month as an integer
     */
    public int getMonth() { return month; }

    /** Returns the sol of month (1 - 28)
     *  @return the sol of month as an integer
     */
    public int getSolOfMonth() { return sol; }

    /** Returns the millisol
     *  @return the millisol as a double
     */
    public double getMillisol() { return millisol; }

    /** Returns the week of the month (1-4)
     *  @return the week of the month as an integer
     */
    public int getWeekOfMonth() {
        return ((sol -1) / 7) + 1;
    }

    /** Returns the sol number of the week (1-7)
     *  @return the sol number of the week as an integer
     */
    public int getSolOfWeek() {
        return sol - ((getWeekOfMonth() - 1) * 7);
    }

    /** Return the sol name of the week
     *  @return the sol name of the week as a String
     */
    public String getSolOfWeekName() {
        return WEEK_SOL_NAMES[getSolOfWeek() - 1];
    }

    /** Returns the number of sols in the current week
     *  @return the number of osls in the current week as an integer
     */
    public int getSolsInWeek() {
        int result = SOLS_IN_WEEK_LONG;

        if (getSolsInMonth(month, orbit) == SOLS_IN_MONTH_SHORT) {
            if (getWeekOfMonth() == 4)
                result = SOLS_IN_WEEK_SHORT;
        }
        return result;
    }

    /** Returns the current season for the given hemisphere
     *  @param hemisphere the hemisphere
     *  NORTHERN_HEMISPHERE or SOUTHERN_HEMISPHERE valid parameters
     *  @return season as String ("Spring", "Summer", "Autumn" or "Winter")
     */
	// 2015-02-24 Reconstructed getSeason() based on value of L_s
    public String getSeason(int hemisphere) {
    	String season = "";
    	if (orbitInfo == null)
			orbitInfo = Simulation.instance().getMars().getOrbitInfo();
		double L_s = orbitInfo.getL_s();
    	//System.out.println("    L_s  :" +  L_s );
    	String modifier = "";

		if (L_s == 360 || L_s < 90 ){
			if (L_s < 30 )
				modifier = "Early ";
			else if (L_s < 60)
				modifier = "Mid ";
			else
				modifier = "Late ";
            if (hemisphere == NORTHERN_HEMISPHERE) season = modifier + "Spring";
            else if (hemisphere == SOUTHERN_HEMISPHERE) season = modifier + "Autumn";
        }
		else if (L_s < 180) {
			if (L_s < 120)
				modifier = "Early ";
			else if (L_s < 150)
				modifier = "Mid ";
			else
				modifier = "Late ";
            if (hemisphere == NORTHERN_HEMISPHERE) season = modifier + "Summer";
            else if (hemisphere == SOUTHERN_HEMISPHERE) season = modifier + "Winter";
        }
		else if (L_s < 270){
			if (L_s < 210)
				modifier = "Early ";
			else if (L_s < 240)
				modifier = "Mid ";
			else
				modifier = "Late ";
            if (hemisphere == NORTHERN_HEMISPHERE) season = modifier + "Autumn";
            else if (hemisphere == SOUTHERN_HEMISPHERE) season = modifier + "Spring";
        }
		else if (L_s < 360) {
			if (L_s < 300)
				modifier = "Early ";
			else if (L_s < 330)
				modifier = "Mid ";
			else
				modifier = "Late ";
            if (hemisphere == NORTHERN_HEMISPHERE) season = modifier + "Winter";
            else if (hemisphere == SOUTHERN_HEMISPHERE) season = modifier + "Summer";
        }

		return season;
    }


    /**
     * Creates a clone of this MarsClock object, with the time set the same.
     * @return clone of this MarsClock object
     */
    public Object clone() {
        return new MarsClock(orbit, month, sol, millisol);
    }

    /**
     * Gets a (random) time on the next day between t1 and t2 millisols.
     * if t2 = 0, millisols = t1.
     * @return MarsClock
     */
    public MarsClock getMarsClockNextSol(MarsClock clock, int t1, int t2) {
    	int millis = 0;
    	if (t2 == 0)
    		millis = t1;
    	else
    		millis = RandomUtil.getRandomInt(t1, t2);
    	int s = clock.getSolOfMonth() + 1;
    	int m = clock.getMonth();
    	int o = clock.getOrbit();
    	if (s == 29) {
    		s = 1;
    		m++;
    	}
    	if (m == 24) {
    		m = 1;
    		o++;
    	}
        return new MarsClock(o, m, s, millis);
    }


    /**
     * Displays the string version of the clock.
     * @return time stamp string.
     */
    public String toString() {
    	return getDateTimeStamp();
    }

    /**
     * Checks if another object is equal to this one.
     */
    public boolean equals(Object object) {
    	boolean result = true;
    	if (object instanceof MarsClock) {
    		MarsClock otherClock = (MarsClock) object;
    		if (orbit != otherClock.orbit) result = false;
    		else if (month != otherClock.month) result = false;
    		else if (sol != otherClock.sol) result = false;
    		else if (millisol != otherClock.millisol) result = false;
    	}
    	else result = false;
    	return result;
    }

    /**
     * Gets the hash code for this object.
     * @return hash code.
     */
    public int hashCode() {
    	return orbit * month * sol + ((int) (millisol * 1000D));
    }
}