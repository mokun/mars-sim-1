/**
 * Mars Simulation Project
 * GlobeDisplay.java
 * @version 2.71 2000-10-19
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;
 
import org.mars_sim.msp.simulation.*;  
import java.awt.*;
import java.util.*;
import javax.swing.*;

/** The Globe Display class displays a graphical globe of Mars in the
 *  "Mars Navigator" tool.
 */
class GlobeDisplay extends JComponent implements Runnable {

    // Data members
    private UIProxyManager proxyManager; // Unit UI proxy manager
    private MarsGlobe marsSphere; // Real surface sphere object
    private MarsGlobe topoSphere; // Topographical sphere object
    private Coordinates centerCoords; // Spherical coordinates for globe center
    private Thread showThread; // Refresh thread
    private boolean topo; // True if in topographical mode, false if in real surface mode
    private boolean recreate; // True if globe needs to be regenerated
    private int width; // width of the globe display component
    private int height; // height of the globe display component

    private static final double HALF_PI = (Math.PI / 2);

    /** Constructs a GlobeDisplay object 
     *  @param proxyManager the UI proxy manager
     *  @width the width of the globe display
     *  @height the height of the globe display
     */
    public GlobeDisplay(UIProxyManager proxyManager, int width, int height) {

        this.proxyManager = proxyManager;
        this.width = width;
        this.height = height;

        // Set component size
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());

        // Construct sphere objects for both real and topographical modes
        marsSphere = new MarsGlobe("surface", this);
        topoSphere = new MarsGlobe("topo", this);

        // Initialize global variables
        centerCoords = new Coordinates(HALF_PI, 0D);
        topo = false;
        recreate = true;

        // Initially show real surface globe
        showSurf();
    }

    /** Displays real surface globe, regenerating if necessary */
    public void showSurf() {
        if (topo) recreate = true; 
        topo = false;
        showGlobe(centerCoords);
    }

    /** Displays topographical globe, regenerating if necessary */
    public void showTopo() {
        if (!topo) recreate = true;
        topo = true;
        showGlobe(centerCoords);
    }

    /** Displays globe at given center regardless of mode, 
     *  regenerating if necessary 
     *  @param newCenter the center location for the globe
     */
    public void showGlobe(Coordinates newCenter) {
        if (!centerCoords.equals(newCenter)) {
            recreate = true;
            centerCoords.setCoords(newCenter);
        }
        updateDisplay();
    }

    /** Starts display update thread (or creates a new one if necessary) */
    private void updateDisplay() {
        if ((showThread == null) || (!showThread.isAlive())) {
            showThread = new Thread(this, "Globe");
            showThread.start();
        } else {
            showThread.interrupt();
        }
    }

    /** the run method for the runnable interface */
    public void run() { refreshLoop(); }

    /** loop, refreshing the globe display when necessary */
    public void refreshLoop() {
        while (true) { // Endless refresh loop
            if (recreate) {
                // Regenerate globe if recreate is true, then display
                if (topo) {
                    topoSphere.drawSphere(centerCoords);
                } else {
                    marsSphere.drawSphere(centerCoords);
                }
                recreate = false;
                repaint();
            } else {
                // Pause for 2 seconds between display refreshs
                try {
                    showThread.sleep(2000);
                } catch (InterruptedException e) {}
                repaint();
            }
        }
    }

    /** Overrides paintComponent method.  Displays globe, green lines,
     *  longitude and latitude. 
     *  @param g graphics context
     */
    public void paintComponent(Graphics g) {

        // Paint black background
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);

        // Draw real or topo globe
        MarsGlobe globe = topo ? topoSphere : marsSphere;

        if (globe.isImageDone()) {
            g.drawImage(globe.getGlobeImage(), 0, 0, this);
        }

        drawUnits(g);
        drawCrossHair(g);
    }

    /** draw the dots on the globe that identify units 
     *  @param g graphics context
     */
    protected void drawUnits(Graphics g) {
        UnitUIProxy[] proxies = proxyManager.getUIProxies();
        for (int x = 0; x < proxies.length; x++) {
            if (proxies[x].isGlobeDisplayed()) {
                Coordinates unitCoords = proxies[x].getUnit().getCoordinates();
                if (centerCoords.getAngle(unitCoords) < HALF_PI) {
                    if (topo) g.setColor(proxies[x].getTopoGlobeColor());
                    else g.setColor(proxies[x].getSurfGlobeColor());
                    IntPoint tempLocation = getUnitDrawLocation(unitCoords);
                    g.fillRect(tempLocation.getiX(), tempLocation.getiY(), 1, 1);
                }
            }
        }
    }

    /** Draw green rectanges and lines (cross-hair type thingy), and
      *  write the latitude and logitude of the centerpoint of the
      *  current glove view. 
      *  @param g graphics context
      */
    protected void drawCrossHair(Graphics g) {
        g.setColor(Color.green);

        g.drawRect(57, 57, 31, 31);
        g.drawLine(0, 73, 57, 73);
        g.drawLine(90, 73, 149, 73);
        g.drawLine(73, 0, 73, 57);
        g.drawLine(73, 90, 73, 149);

        // Prepare font
        Font positionFont = new Font("Helvetica", Font.PLAIN, 10);
        FontMetrics positionMetrics = getFontMetrics(positionFont);
        g.setFont(positionFont);

        // Draw longitude and latitude strings
        int leftWidth = positionMetrics.stringWidth("Latitude:");
        int rightWidth = positionMetrics.stringWidth("Longitude:");

        g.drawString("Latitude:", 5, 130);
        g.drawString("Longitude:", 145 - rightWidth, 130);

        String latString = centerCoords.getFormattedLatitudeString();
        String longString = centerCoords.getFormattedLongitudeString();

        int latWidth = positionMetrics.stringWidth(latString);
        int longWidth = positionMetrics.stringWidth(longString);

        int latPosition = ((leftWidth - latWidth) / 2) + 5;
        int longPosition = 145 - rightWidth + ((rightWidth - longWidth) / 2);

        g.drawString(latString, latPosition, 142);
        g.drawString(longString, longPosition, 142);
    }

    /** Returns unit x, y position on globe panel 
     *  @param unitCoords the unit's location
     *  @return x, y position on globe panel
     */
    private IntPoint getUnitDrawLocation(Coordinates unitCoords) {
        double rho = width / Math.PI;
        int half_map = (int)(width / 2);
        int low_edge = 0;
        return Coordinates.findRectPosition(unitCoords, centerCoords, rho,
                half_map, low_edge);
    }
}