/**
 * Mars Simulation Project
 * MarsProjectFX.java
 * @version 3.08 2015-03-26
 * @author Manny Kung
 */
package org.mars_sim.msp.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.helpGenerator.HelpGenerator;
import org.mars_sim.msp.ui.javafx.svg.SvgImageLoaderFactory;

/**
 * MarsProjectFX is the main class for MSP. It creates JavaFX/8 application thread.
 */
public class MarsProjectFX extends Application  {

    /** initialized logger for this class. */
    private static Logger logger = Logger.getLogger(MarsProjectFX.class.getName());

    static String[] args;

    /** true if displaying graphic user interface. */
    private boolean useGUI = true;

    /** true if help documents should be generated from config xml files. */
    private boolean generateHelp = false;

    private MainMenu mainMenu;

    private List<String> argList;

    //private ExecutorService worker;

    private MarsProjectFX marsProjectFX;

    public MarsProjectFX() {
	   	//logger.info("MarsProjectFX's constructor is on " + Thread.currentThread().getName() + " Thread");
    	marsProjectFX = this;
		/*
		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        System.out.println(javaCompiler.toString());

        Set<SourceVersion> sourceVersion;
        sourceVersion = javaCompiler.getSourceVersions();

        for (SourceVersion version : sourceVersion) {
            System.out.print(version.name() + "\n");
        }

        System.out.print("availableProcessors = " + Runtime.getRuntime().availableProcessors() + "\n");
        */
    }

    public void init() {
	   	//logger.info("MarsProjectFX's init() is on " + Thread.currentThread().getName() + " Thread");
	   	Simulation.instance().startSimExecutor();
	   	Simulation.instance().getSimExecutor().submit(new SimulationTask());
    }


	public class SimulationTask implements Runnable {

		public void run() {
		   	new Simulation();
			setLogging();
			setDirectory();
	    	logger.info("Starting " + Simulation.WINDOW_TITLE);

			argList = Arrays.asList(args);
			useGUI = !argList.contains("-headless");
	        generateHelp = argList.contains("-generateHelp");

		    // this will generate html files for in-game help based on config xml files
		    if (generateHelp) {
		    	HelpGenerator.generateHtmlHelpFiles();
		    }

		    if (useGUI) {
		    	//System.setProperty("sun.java2d.opengl", "true"); // NOT WORKING IN MACCOSX
		    	//System.setProperty("sun.java2d.ddforcevram", "true");

		       	// Enable capability of loading of svg image using regular method
		    	//SvgImageLoaderFactory.install();

			} else { // GUI-less
			    // Initialize the simulation.
			    initializeSimulation(args); // evaluate args switches
			    // Start the simulation.
			    startSimulation();
			}
		}
    }

	public void start(Stage primaryStage) {
	   	//logger.info("MarsProjectFX's start() is on " + Thread.currentThread().getName() + " Thread");

		if (useGUI) {
		    mainMenu = new MainMenu(this); //, args, true);
		    mainMenu.initAndShowGUI(primaryStage);
		}
	}

    public void stop() {
	   	//System.out.println("MarsProjectFX's stop is on " + Thread.currentThread().getName() + " Thread");

    }

	public List<String> getArgList() {
		return argList;
	}

    /**
     * Initialize the simulation.
     * @param args the command arguments.
     * @return true if new simulation (not loaded)
     */
    boolean initializeSimulation(String[] args) {
        boolean result = false;
		//logger.info("initializeSimulation() is on " + Thread.currentThread().getName() + " Thread");

        // Create a simulation
        List<String> argList = Arrays.asList(args);

        if (argList.contains("-new")) {
            // If new argument, create new simulation.
            handleNewSimulation(); // if this fails we always exit, continuing is useless
            result = true;

        } else if (argList.contains("-load")) {
            // If load argument, load simulation from file.
            try {
                handleLoadSimulation(argList);
            } catch (Exception e) {
                showError("Could not load the desired simulation, trying to create a new Simulation...", e);
                handleNewSimulation();
                result = true;
            }
        } else {
            try {
                handleLoadDefaultSimulation();
            } catch (Exception e) {
//                showError("Could not load the default simulation, trying to create a new Simulation...", e);
                handleNewSimulation();
                result = true;
            }
        }

        return result;
    }

    /**
     * Initialize the simulation.
     * @param args the command arguments.
     * @return true if new simulation (not loaded)
     */
    boolean initializeNewSimulation() {
        boolean result = false;

            handleNewSimulation(); // if this fails we always exit, continuing is useless
            result = true;

        return result;
    }

    /**
     * Exit the simulation with an error message.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void exitWithError(String message, Exception e) {
        showError(message, e);
        Platform.exit();
        System.exit(1);
    }

    /**
     * Show a modal error message dialog.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void showError(String message, Exception e) {
        if (e != null) {
            logger.log(Level.SEVERE, message, e);
        }
        else {
            logger.log(Level.SEVERE, message);
        }

        if (useGUI) {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads the simulation from the default save file.
     * @throws Exception if error loading the default saved simulation.
     */
    void handleLoadDefaultSimulation() throws Exception {
        try {
            // Load a the default simulation
            Simulation.instance().loadSimulation(null);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load default simulation", e);
            throw e;
        }
    }

    /**
     * Calls handleLoadSimulation(argList). Used by MainMenu to load th default save sim.
     * @throws Exception if error loading the default saved simulation.
     */
    void handleLoadDefaultSavedSimulation() {
    	try {
    		List<String> argList = new ArrayList<String>(1);
    		argList.add("-load");
			handleLoadSimulation(argList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Loads the simulation from a save file.
     * @param argList the command argument list.
     * @throws Exception if error loading the saved simulation.
     */
    void handleLoadSimulation(List<String> argList) throws Exception {
		//logger.info("MarsProjectFX's handleLoadSimulation() is in "+Thread.currentThread().getName() + " Thread");

        try {
            int index = argList.indexOf("-load");
            // Get the next argument as the filename.
            File loadFile = new File(argList.get(index + 1));
            if (loadFile.exists() && loadFile.canRead()) {
                Simulation.instance().loadSimulation(loadFile);


            } else {
                exitWithError("Problem loading simulation. " + argList.get(index + 1) +
                        " not found.", null);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem loading existing simulation", e);
            throw e;
        }
    }

    /**
     * Create a new simulation instance.
     */
    void handleNewSimulation() {
		//logger.info("MarsProjectFX's handleNewSimulation() is in "+Thread.currentThread().getName() + " Thread");

        try {
            SimulationConfig.loadConfig();
            if (useGUI) {
//        		System.out.println("MarsProjectFX's handleNewSimulation() is in "+Thread.currentThread().getName() + " Thread");
            	//Runnable r = new ScenarioConfigEditorFX(mainMenu, SimulationConfig.instance());
				//(new Thread(r)).start();
        	   	Simulation.instance().getSimExecutor().submit(new ConfigEditorTask());
            	// note: cannot load editor in macosx if it was a JDialog
                // ScenarioConfigEditorFX editor = new ScenarioConfigEditorFX(mainMenu, SimulationConfig.instance());
            }
        } catch (Exception e) {
            e.printStackTrace();
            exitWithError("Could not create a new simulation, startup cannot continue", e);
        }
    }

    /**
     * Start the simulation instance.
     */
    public void startSimulation() {
		//logger.info("MarsProjectFX's startSimulation() is in "+Thread.currentThread().getName() + " Thread");

        // Start the simulation.
        Simulation.instance().start();
    }

    public void setDirectory() {
        new File(System.getProperty("user.home"), ".mars-sim" + File.separator + "logs").mkdirs();
    }


    public void setLogging() {

        try {
            LogManager.getLogManager().readConfiguration(MarsProjectFX.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load logging properties", e);
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (IOException e1) {
                logger.log(Level.WARNING, "Could read logging default config", e);
            }
        }
    }

	public class ConfigEditorTask implements Runnable {
		  public void run() {
			  new ScenarioConfigEditorFX(marsProjectFX, mainMenu);
		  }
	}


    public static void main(String[] args) {
    	//logger.info("MarsProjectFX's main() is in " + Thread.currentThread().getName() + " Thread");
    	MarsProjectFX.args = args;

        //HelloNode app = new HelloNode();
        //app.setShowSettings(false);
        //app.start();

        launch(args);
    }
}