/**
 * Mars Simulation Project
 * TimeWindow.java
 * @version 2.72 2001-07-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import org.mars_sim.msp.simulation.*;  
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/** The TimeWindow is a tool window that displays the current 
 *  Martian and Earth time 
 */
public class TimeWindow extends ToolWindow implements Runnable {

    // Data members
    private MainDesktopPane desktop; // Desktop pane
    private MarsClock marsTime;      // Martian Clock
    private EarthClock earthTime;    // Earth Clock
    private UpTimer uptimer;         // Uptime Timer
    private MarsCalendarDisplay calendarDisplay;  // Martian calendar panel
    private JLabel martianTimeLabel; // JLabel for Martian time
    private JLabel martianMonthLabel; // JLabel for Martian month
    private JLabel northernSeasonLabel; // JLabel for Northern hemisphere season
    private JLabel southernSeasonLabel; // JLabel for Southern hemisphere season
    private JLabel earthTimeLabel;   // JLabel for Earth time
    private JLabel uptimeLabel;      // JLabel for uptimer
    private Thread updateThread;     // Window update thread

    /** Constructs a TimeWindow object 
     *  @param desktop the desktop pane
     */
    public TimeWindow(MainDesktopPane desktop) {

        // Use TimeWindow constructor
        super("Time Tool");

        // Set internal frame listener
        addInternalFrameListener(new ViewFrameListener());

        // Initialize data members
        this.desktop = desktop;
        MasterClock master = desktop.getMainWindow().getVirtualMars().getMasterClock();
        marsTime = master.getMarsClock();
        earthTime = master.getEarthClock();
        uptimer = master.getUpTimer(); 

        // Get content pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);

        // Create Martian time panel
        JPanel martianTimePane = new JPanel(new BorderLayout());
        martianTimePane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        mainPane.add(martianTimePane, "North");

        // Create Martian time header label
        JLabel martianTimeHeaderLabel = new JLabel("Martian Time", JLabel.CENTER);
        martianTimeHeaderLabel.setForeground(Color.black);
        martianTimePane.add(martianTimeHeaderLabel, "North");

        // Create Martian time label
        martianTimeLabel = new JLabel(marsTime.getTimeStamp(), JLabel.CENTER);
        martianTimeLabel.setForeground(Color.black);
        martianTimePane.add(martianTimeLabel, "South");

        // Create Martian calendar panel
        JPanel martianCalendarPane = new JPanel(new FlowLayout());
        martianCalendarPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        mainPane.add(martianCalendarPane, "Center");

        // Create Martian calendar month panel
        JPanel calendarMonthPane = new JPanel(new BorderLayout());
        martianCalendarPane.add(calendarMonthPane);

        // Create martian month label
        martianMonthLabel = new JLabel(marsTime.getMonthName(), JLabel.CENTER);
        martianMonthLabel.setForeground(Color.black);
        calendarMonthPane.add(martianMonthLabel, "North");

        // Create Martian calendar display
        calendarDisplay = new MarsCalendarDisplay(marsTime);
        JPanel innerCalendarPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        innerCalendarPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
        innerCalendarPane.add(calendarDisplay);
        calendarMonthPane.add(innerCalendarPane, "Center");        

        JPanel southPane = new JPanel(new BorderLayout());
        mainPane.add(southPane, "South");

        // Create Martian season panel
        JPanel marsSeasonPane = new JPanel(new BorderLayout());
        marsSeasonPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        southPane.add(marsSeasonPane, "North");

        // Create Martian season label
        JLabel marsSeasonLabel = new JLabel("Martian Seasons", JLabel.CENTER);
        marsSeasonLabel.setForeground(Color.black);
        marsSeasonPane.add(marsSeasonLabel, "North");

        // Create Northern season label
        northernSeasonLabel = new JLabel("Northern Hemisphere: " + marsTime.getSeason(MarsClock.NORTHERN_HEMISPHERE), JLabel.CENTER);
        northernSeasonLabel.setForeground(Color.black);
        marsSeasonPane.add(northernSeasonLabel, "Center");
 
        // Create Southern season label
        southernSeasonLabel = new JLabel("Southern Hemisphere: " + marsTime.getSeason(MarsClock.SOUTHERN_HEMISPHERE), JLabel.CENTER);
        southernSeasonLabel.setForeground(Color.black);
        marsSeasonPane.add(southernSeasonLabel, "South");

        // Create Earth time panel
        JPanel earthTimePane = new JPanel(new BorderLayout());
        earthTimePane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        southPane.add(earthTimePane, "Center");

        // Create Earth time header label
        JLabel earthTimeHeaderLabel = new JLabel("Earth Time", JLabel.CENTER);
        earthTimeHeaderLabel.setForeground(Color.black);
        earthTimePane.add(earthTimeHeaderLabel, "North");

        // Create Earth time label
        earthTimeLabel = new JLabel(earthTime.getTimeStamp(), JLabel.CENTER);
        earthTimeLabel.setForeground(Color.black);
        earthTimePane.add(earthTimeLabel, "South");

        // Create uptime panel
        JPanel uptimePane = new JPanel(new BorderLayout());
        uptimePane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        southPane.add(uptimePane, "South");

        // Create uptime header label
        JLabel uptimeHeaderLabel = new JLabel("Simulation Uptime", JLabel.CENTER);
        uptimeHeaderLabel.setForeground(Color.black);
        uptimePane.add(uptimeHeaderLabel, "North");

        // Create uptime label
        uptimeLabel = new JLabel(uptimer.getUptime(), JLabel.CENTER);
        uptimeLabel.setForeground(Color.black);
        uptimePane.add(uptimeLabel, "South");

        // Pack window
        pack();

        // Add 10 pixels to packed window width
        Dimension windowSize = getSize();
        setSize(new Dimension((int)windowSize.getWidth() + 10, (int) windowSize.getHeight()));

        // Start update thread
        start();
    }

    /** Starts the update thread, or creates and starts a new one if necessary */
    public void start() {
        if ((updateThread == null) || (!updateThread.isAlive())) {
            updateThread = new Thread(this, "Time Tool Window");
            updateThread.start();
        }
    }

    /** Update thread runner */
    public void run() {
    
        // Endless refresh loop
        while (true) {

            // Pause for 1 second between display refreshes
            try {
                updateThread.sleep(1000);
            } catch (InterruptedException e) {}

            // Update window
            update();
        }
    }

    /** Update window */
    private void update() {
       martianTimeLabel.setText(marsTime.getTimeStamp());
       earthTimeLabel.setText(earthTime.getTimeStamp());
       uptimeLabel.setText(uptimer.getUptime());
       martianMonthLabel.setText(marsTime.getMonthName());
       northernSeasonLabel.setText("Northern Hemisphere: " + marsTime.getSeason(MarsClock.NORTHERN_HEMISPHERE));
       southernSeasonLabel.setText("Southern Hemisphere: " + marsTime.getSeason(MarsClock.SOUTHERN_HEMISPHERE)); 
       calendarDisplay.update();
    }
}