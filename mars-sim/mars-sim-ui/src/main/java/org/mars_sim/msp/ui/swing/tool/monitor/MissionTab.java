/**
 * Mars Simulation Project
 * MissionTab.java
 * @version 3.08 2015-07-10

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;

/**
 * This class represents a mission table displayed within the Monitor Window.
 */
public class MissionTab
extends TableTab {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

    public final static String ICON = "briefcase_32";

	/**
	 * Constructor.
	 */
	public MissionTab(final MonitorWindow window) {
		// Use TableTab constructor
		super(window, new MissionTableModel(), true, true, ICON);
	}

	/**
	 * Display selected mission in mission tool.
	 * @param desktop the main desktop.
	 */
	public void displayMission(MainDesktopPane desktop) {
		List<?> selection = getSelection();
		if (selection.size() > 0) {
			Object selected = selection.get(0);
			if (selected instanceof Mission) {
				((MissionWindow) desktop.getToolWindow(MissionWindow.NAME)).selectMission((Mission) selected);
				desktop.openToolWindow(MissionWindow.NAME);
			}
		}
	}

	/**
	 * Center the map on the first selected row.
	 * @param desktop Main window of application.
	 */
	public void centerMap(MainDesktopPane desktop) {
		List<?> rows = getSelection();
		Iterator<?> it = rows.iterator();
		if (it.hasNext()) {
			Mission mission = (Mission) it.next();
			if (mission.getMembersNumber() > 0) {
			    MissionMember member = (MissionMember) mission.getMembers().toArray()[0];
				desktop.centerMapGlobe(member.getCoordinates());
			}
		}
	}
}