import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;
import dms.dss.*;

public class TestPerfDBSession {

    public TestPerfDBSession() {
		super();
    }

    /*** Beginning of main program. ***/

    public static void main(java.lang.String[] args) {

		// Create a PerfDBSession object
		DataSession session = new PerfDBSession();
		session.initialize(args[0]);
		System.out.println ("API loaded...");

		// Get the list of applications
		ListIterator applications;
		applications = session.getApplicationList();
		Application app = null;

		// loop through all the applications, and print out some info
        while(applications.hasNext())
		{
			app = (Application) applications.next();
			System.out.println ("Application ID = " + app.getID() + ", name = " + app.getName() + ", version = " + app.getVersion() + ", description = " + app.getDescription());
		}

		/* the following code shows how to select applications -
		 * use one of the following methods.  You don't have to 
		 * do all of them, just one.
		 */

		// select an application
		session.setApplication(app);
		// select an application, another way
		session.setApplication(app.getID());
		// select an application, yet another way
		session.setApplication(app.getName(), null);

		// Get the list of experiments
		ListIterator experiments;
		experiments = session.getExperimentList();
		Experiment exp = null;

        while(experiments.hasNext())
		{
			exp = (Experiment) experiments.next();
			System.out.println ("Experiment ID = " + exp.getID() + ", appid = " + exp.getApplicationID());

			// select an experiment
			session.setExperiment(exp);
			// select an experiment, another way
			session.setExperiment(exp.getID());
		}

		// Get the list of trials
		ListIterator trials;
		trials = session.getTrialList();
		Trial trial = null;
		Vector tmpTrials = new Vector();

        while(trials.hasNext())
		{
			trial = (Trial) trials.next();
			tmpTrials.addElement(trial);
			System.out.println ("Trial ID = " + trial.getID() + ", Experiment ID = " + trial.getExperimentID() + ", appid = " + trial.getApplicationID());

			// select a trial
			session.setTrial(trial);
			// select a trial, another way
			session.setTrial(trial.getID());
		}
		// select all trials
		session.setTrial(tmpTrials);

		// Get the list of functions
		ListIterator functions;
		functions = session.getFunctions();
		Function function = null;

		while (functions.hasNext())
		{
			function = (Function) functions.next();
			// System.out.println ("Function Index ID = " + function.getIndexID() + ", Function ID = " + function.getFunctionID());
			System.out.println ("Function Name = " + function.getName());
			// System.out.println ("Trial ID = " + function.getTrialID() + ", Experiment ID = " + function.getExperimentID() + ", appid = " + function.getApplicationID());
			// System.out.println ("Mean Summary = " + function.getMeanSummary().getInclusivePercentage() + ", " + function.getMeanSummary().getInclusive() + ", " + function.getMeanSummary().getExclusivePercentage() + ", " + function.getMeanSummary().getExclusive() + ", " + function.getMeanSummary().getNumCalls() + ", " + function.getMeanSummary().getNumSubroutines() + ", " + function.getMeanSummary().getInclusivePerCall());
			// System.out.println ("Total Summary = " + function.getTotalSummary().getInclusivePercentage() + ", " + function.getTotalSummary().getInclusive() + ", " + function.getTotalSummary().getExclusivePercentage() + ", " + function.getTotalSummary().getExclusive() + ", " + function.getTotalSummary().getNumCalls() + ", " + function.getTotalSummary().getNumSubroutines() + ", " + function.getTotalSummary().getInclusivePerCall());

			// select a function
			session.setFunction(function);
			// select a function, another way
			session.setFunction(function.getIndexID());
		}

		// Get the list of functions
		ListIterator userEvents;
		userEvents = session.getUserEvents();
		UserEvent userEvent = null;

		while (userEvents.hasNext())
		{
			userEvent = (UserEvent) userEvents.next();
			System.out.println ("UserEvent Name = " + userEvent.getName());

			// select a userEvent
			session.setUserEvent(userEvent);
			// select a userEvent, another way
			session.setUserEvent(userEvent.getUserEventID());
		}

		Vector nodes = new Vector();
		Integer node = new Integer(0);
		nodes.addElement(node);
		node = new Integer(1);
		nodes.addElement(node);
		session.setNode(nodes);
		session.setContext(0);
		session.setThread(0);

		// Get the data
		session.getFunctionData();

		// disconnect and exit.
		session.terminate();
		System.out.println ("Exiting.");
		return;
    }
}

