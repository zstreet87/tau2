package dms.dss;

import perfdb.util.dbinterface.*;
import perfdb.util.io.*;
import perfdb.dbmanager.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;
import java.util.Date;

/**
 * This is the top level class for the Database implementation of the API.
 *
 * <P>CVS $Id: PerfDBSession.java,v 1.34 2004/03/01 03:46:19 khuck Exp $</P>
 * @author	Kevin Huck, Robert Bell
 * @version	0.1
 */
public class PerfDBSession extends DataSession {

    private DB db = null;
    private perfdb.ConnectionManager connector;
	private Hashtable functionHash = null;
	private Hashtable userEventHash = null;

	public PerfDBSession () {
		super();
	}

	public void initialize (Object obj) {
		String configFileName = (String)(obj);
		// initialize the connection to the database,
		// using the configuration settings.
		try {
			connector = new perfdb.ConnectionManager(configFileName);
			connector.connect();
			db = connector.getDB();
		} catch ( Exception e ) {
		}
	}

	public void initialize (Object obj, String password) {
		String configFileName = (String)(obj);
		// initialize the connection to the database,
		// using the configuration settings.
		try {
			connector = new perfdb.ConnectionManager(configFileName, password);
			connector.connect();
			db = connector.getDB();
		} catch ( Exception e ) {
		}
	}

	public void terminate () {
		connector.dbclose();
	}

    public perfdb.ConnectionManager getConnector(){
		return connector;
    }

	// returns Vector of ALL Application objects
	public ListIterator getApplicationList() {
		String whereClause = "";
		return new DataSessionIterator(getApplicationList(whereClause));
	}

	// returns Vector of Application objects, filtered by where clause
	private Vector getApplicationList(String whereClause) {
		Vector applications = new Vector();
		// create a string to hit the database
		StringBuffer buf = new StringBuffer();
		buf.append("select distinct id, name, version, description, ");
		buf.append(" language, paradigm, usage_text, execution_options, ");
		buf.append(" userdata from application");
		buf.append(whereClause);
		// System.out.println(buf.toString());

		// get the results
		try {
	    	ResultSet resultSet = db.executeQuery(buf.toString());	
	    	while (resultSet.next() != false) {
				Application app = new Application();
				app.setID(resultSet.getInt(1));
				app.setName(resultSet.getString(2));
				app.setVersion(resultSet.getString(3));
				app.setDescription(resultSet.getString(4));
				app.setLanguage(resultSet.getString(5));
				app.setParaDiag(resultSet.getString(6));
				app.setUsage(resultSet.getString(7));
				app.setExecutableOptions(resultSet.getString(8));
				app.setUserData(resultSet.getString(9));
				applications.addElement(app);
	    	}
			resultSet.close(); 
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
		}
		
		return applications;
	}

	// returns Vector of Experiment objects
	public ListIterator getExperimentList() {
		String whereClause = "";
		if (application != null)
			whereClause = "where application = " + application.getID();
		return new DataSessionIterator(getExperimentList(whereClause));
	}

	// returns Vector of Experiment objects
	public Vector getExperimentList(String whereClause) {
		Vector experiments = new Vector();
		// create a string to hit the database
		StringBuffer buf = new StringBuffer();
		buf.append("select distinct id, application, name, system_name, ");
		buf.append("system_machine_type, system_arch, system_os, ");
		buf.append("system_memory_size, system_processor_amt, ");
		buf.append("system_l1_cache_size, system_l2_cache_size, ");
		buf.append("system_userdata, ");
		buf.append("configure_prefix, configure_arch, configure_cpp, ");
		buf.append("configure_cc, configure_jdk, configure_profile, ");
		buf.append("configure_userdata, ");
		buf.append("compiler_cpp_name, compiler_cpp_version, ");
		buf.append("compiler_cc_name, compiler_cc_version, ");
		buf.append("compiler_java_dirpath, compiler_java_version, ");
		buf.append("compiler_userdata, userdata from experiment ");
		buf.append(whereClause);
		// System.out.println(buf.toString());

		// get the results
		try {
	    	ResultSet resultSet = db.executeQuery(buf.toString());	
	    	while (resultSet.next() != false) {
				Experiment exp = new Experiment();
				exp.setID(resultSet.getInt(1));
				exp.setApplicationID(resultSet.getInt(2));
    			exp.setName(resultSet.getString(3));
    			exp.setSystemName(resultSet.getString(4));
    			exp.setSystemMachineType(resultSet.getString(5));
    			exp.setSystemArch(resultSet.getString(6));
    			exp.setSystemOS(resultSet.getString(7));
    			exp.setSystemMemorySize(resultSet.getString(8));
    			exp.setSystemProcessorAmount(resultSet.getString(9));
    			exp.setSystemL1CacheSize(resultSet.getString(10));
    			exp.setSystemL2CacheSize(resultSet.getString(11));
    			exp.setSystemUserData(resultSet.getString(12));
    			exp.setConfigurationPrefix(resultSet.getString(13));
    			exp.setConfigurationArchitecture(resultSet.getString(14));
    			exp.setConfigurationCpp(resultSet.getString(15));
    			exp.setConfigurationCc(resultSet.getString(16));
    			exp.setConfigurationJdk(resultSet.getString(17));
    			exp.setConfigurationProfile(resultSet.getString(18));
    			exp.setConfigurationUserData(resultSet.getString(19));
    			exp.setCompilerCppName(resultSet.getString(20));
    			exp.setCompilerCppVersion(resultSet.getString(21));
    			exp.setCompilerCcName(resultSet.getString(22));
    			exp.setCompilerCcVersion(resultSet.getString(23));
    			exp.setCompilerJavaDirpath(resultSet.getString(24));
    			exp.setCompilerJavaVersion(resultSet.getString(25));
    			exp.setCompilerUserData(resultSet.getString(26));
    			exp.setUserData(resultSet.getString(27));
				experiments.addElement(exp);
	    	}
			resultSet.close(); 
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
		}
		
		return experiments;
	}

	// returns Vector of Trial objects
	public ListIterator getTrialList() {
		StringBuffer whereClause = new StringBuffer();
		if (experiment != null) {
			whereClause.append("where t.experiment = " + experiment.getID());
		} else if (application != null) {
			whereClause.append("where e.application = " + application.getID());
		}
		return new DataSessionIterator(getTrialList(whereClause.toString()));
	}

	// gets the metric data for the trial
	private void getTrialMetrics(Trial trial) {
		// create a string to hit the database
		StringBuffer buf = new StringBuffer();
		buf.append("select distinct m.name, m.id ");
		buf.append("from metric m ");
		buf.append("inner join xml_file x on x.metric = m.id ");
		buf.append("where x.trial = ");
		buf.append(trial.getID());
		buf.append(" order by m.id;");
		// System.out.println(buf.toString());

		// get the results
		try {
	    	ResultSet resultSet = db.executeQuery(buf.toString());	
	    	while (resultSet.next() != false) {
				String name = new String();
				name = resultSet.getString(1);
				trial.addMetric(name);
	    	}
			resultSet.close(); 
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return;
		}
		return;
	}

	// returns Vector of Trial objects
	public Vector getTrialList(String whereClause) {
		Vector trials = new Vector();
		// create a string to hit the database
		StringBuffer buf = new StringBuffer();
		buf.append("select distinct t.id, t.experiment, e.application, ");
		buf.append("t.time, t.problem_definition, t.node_count, ");
		buf.append("t.contexts_per_node, t.threads_per_context, ");
		buf.append("t.name, t.userdata ");
		buf.append("from trial t inner join experiment e ");
		buf.append("on t.experiment = e.id ");
		buf.append(whereClause);
		// System.out.println(buf.toString());

		// get the results
		try {
	    	ResultSet resultSet = db.executeQuery(buf.toString());	
	    	while (resultSet.next() != false) {
				Trial trial = new Trial();
				trial.setID(resultSet.getInt(1));
				trial.setExperimentID(resultSet.getInt(2));
				trial.setApplicationID(resultSet.getInt(3));
				trial.setTime(resultSet.getString(4));
				trial.setProblemDefinition(resultSet.getString(5));
				trial.setNodeCount(resultSet.getInt(6));
				trial.setNumContextsPerNode(resultSet.getInt(7));
				trial.setNumThreadsPerContext(resultSet.getInt(8));
				trial.setName(resultSet.getString(9));
				trial.setUserData(resultSet.getString(10));
				trials.addElement(trial);
	    	}
			resultSet.close(); 
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
		}
		
		// get the function details
		Enumeration enum = trials.elements();
		Trial trial;
		while (enum.hasMoreElements()) {
			trial = (Trial)enum.nextElement();
			getTrialMetrics(trial);
		}

		return trials;
	}

	// set the Application for this session
	public Application setApplication(int id) {
		this.application = null;
		this.experiment = null;
		this.trial = null;
		this.functionHash = null;
		this.userEventHash = null;
		// create a string to hit the database
		String whereClause = " where id = " + id;
		Vector applications = getApplicationList(whereClause);
		if (applications.size() == 1) {
			this.application = (Application)applications.elementAt(0);
		} // else exception?
		return this.application;
	}

	public Application setApplication(String name, String version) {
		this.application = null;
		this.experiment = null;
		this.trial = null;
		this.functionHash = null;
		this.userEventHash = null;
		// create a string to hit the database
		StringBuffer whereClause = new StringBuffer();
		whereClause.append(" where name = '" + name + "'");
		if (version != null) {
			whereClause.append(" and version = " + version);
		}
		Vector applications = getApplicationList(whereClause.toString());
		if (applications.size() == 1) {
			this.application = (Application)applications.elementAt(0);
		} // else exception?
		return this.application;
	}
	
	// set the Experiment for this session
	public Experiment setExperiment(int id) {
		this.experiment = null;
		this.trial = null;
		this.functionHash = null;
		this.userEventHash = null;
		// create a string to hit the database
		String whereClause;
		whereClause = " where id = " + id;
		Vector experiments = getExperimentList(whereClause);
		if (experiments.size() == 1) {
			this.experiment = (Experiment)experiments.elementAt(0);
		} //else exception?

		/* don't know if we want to do this
		if (this.application == null && this.experiment != null) {
			setApplication(experiment.getApplicationID());
		}*/
		return this.experiment;
	}

	// set the Trial for this session
	public Trial setTrial(int id) {
		Trial trial = null;
		this.trial = null;
		this.functionHash = null;
		this.userEventHash = null;
		// create a string to hit the database
		String whereClause;
		whereClause = " where t.id = " + id;
		Vector trials = getTrialList(whereClause);
		if (trials.size() == 1) {
			trial = (Trial)trials.elementAt(0);
			this.trial = trial;
		} //else exception?
		
		/* don't know if we want to do this
		if (this.experiment == null && this.trial != null) {
			setExperiment(trial.getExperimentID());
		}*/
		return trial;
	}

	// returns a ListIterator of Functions
	public ListIterator getFunctions() {
		StringBuffer whereClause = new StringBuffer();
		if (trial != null) {
			whereClause.append(" where trial = " + trial.getID());
		} else if (experiment != null) {
			whereClause.append(" where experiment = " + experiment.getID());
		} else if (application != null) {
			whereClause.append(" where application = " + application.getID());
		}

		return new DataSessionIterator(getFunctions(whereClause.toString()));
	}

	// returns a Vector of Functions
	public void getFunctionDetail(Function function) {
		// create a string to hit the database
		StringBuffer buf = new StringBuffer();
		buf.append("select ");
		buf.append("mean_inclusive_percentage, mean_inclusive, ");
		buf.append("mean_exclusive_percentage, mean_exclusive, ");
		buf.append("mean_call, mean_subroutines, mean_inclusive_per_call, ");
		buf.append("metric, ");
		buf.append("total_inclusive_percentage, total_inclusive, ");
		buf.append("total_exclusive_percentage, total_exclusive, ");
		buf.append("total_call, total_subroutines, total_inclusive_per_call, ");
		buf.append("trial ");
		buf.append("from function_detail ");
		buf.append("where id = " + function.getIndexID());
		if (metrics != null) {
			buf.append(" and metric in ('");
			String metric;
        	for(Enumeration en = metrics.elements(); en.hasMoreElements() ;) {
				metric = (String) en.nextElement();
				buf.append(metric);
				if (en.hasMoreElements())
					buf.append("', '");
				else
					buf.append("') ");
			}
		}
		buf.append(" order by id, metric");
		// System.out.println(buf.toString());

		// get the results
		try {
	    	ResultSet resultSet = db.executeQuery(buf.toString());	
			Function tmpFunction = null;
			int metricIndex = 0;
			FunctionDataObject funMS = new FunctionDataObject();
			FunctionDataObject funTS = new FunctionDataObject();
	    	while (resultSet.next() != false) {
				// get the mean summary data
				funMS.setInclusivePercentage(metricIndex, resultSet.getDouble(1));
				funMS.setInclusive(metricIndex, resultSet.getDouble(2));
				funMS.setExclusivePercentage(metricIndex, resultSet.getDouble(3));
				funMS.setExclusive(metricIndex, resultSet.getDouble(4));
				funMS.setNumCalls((int)(resultSet.getDouble(5)));
				funMS.setNumSubroutines((int)(resultSet.getDouble(6)));
				funMS.setInclusivePerCall(metricIndex, resultSet.getDouble(7));
				function.addMeanSummary(funMS);
				// get the total summary data
				funTS.setInclusivePercentage(metricIndex, resultSet.getDouble(8));
				funTS.setInclusive(metricIndex, resultSet.getDouble(9));
				funTS.setExclusivePercentage(metricIndex, resultSet.getDouble(10));
				funTS.setExclusive(metricIndex, resultSet.getDouble(11));
				funTS.setNumCalls((int)(resultSet.getDouble(12)));
				funTS.setNumSubroutines((int)(resultSet.getDouble(13)));
				funTS.setInclusivePerCall(metricIndex, resultSet.getDouble(14));
				function.addTotalSummary(funTS);
	    	}
			resultSet.close(); 
		}catch (Exception ex) {
	    	ex.printStackTrace();
		}
	}

	// returns a Vector of Functions
	public Vector getFunctions(String whereClause) {
		if (functionHash == null)
			functionHash = new Hashtable();
		Vector funs = new Vector();
		// create a string to hit the database
		StringBuffer buf = new StringBuffer();
		buf.append("select distinct id, function_number, name, ");
		buf.append("group_name, trial, experiment, application ");
		buf.append("from function_trial_experiment_view ");
		buf.append(whereClause);
		// System.out.println(buf.toString());

		// get the results
		try {
	    	ResultSet resultSet = db.executeQuery(buf.toString());	
			Function tmpFunction = null;
	    	while (resultSet.next() != false) {
				Function fun = new Function(this);
				fun.setIndexID(resultSet.getInt(1));
				fun.setFunctionID(resultSet.getInt(2));
				fun.setName(resultSet.getString(3));
				fun.setGroup(resultSet.getString(4));
				fun.setTrialID(resultSet.getInt(5));
				fun.setExperimentID(resultSet.getInt(6));
				fun.setApplicationID(resultSet.getInt(7));
				funs.addElement(fun);
				tmpFunction = (Function)functionHash.get(new Integer(fun.getIndexID()));
				if (tmpFunction == null) {
					functionHash.put(new Integer(fun.getIndexID()), fun);
				}
	    	}
			resultSet.close(); 
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
		}
		
		// get the function details
		/*
		Enumeration enum = funs.elements();
		Function fun;
		while (enum.hasMoreElements()) {
			fun = (Function)enum.nextElement();
			getFunctionDetail(fun);
		}
		*/

		return funs;
	}

	// returns a ListIterator of UserEvents
	public ListIterator getUserEvents() {
		StringBuffer whereClause = new StringBuffer();
		boolean gotWhile = false;
		if (application != null) {
			whereClause.append(" where e.application = " + application.getID());
			gotWhile = true;
		}
		if (experiment != null) {
			if (gotWhile)
				whereClause.append(" and");
			else
				whereClause.append(" where");
			whereClause.append(" t.experiment = " + experiment.getID());
			gotWhile = true;
		}
		if (trial != null) {
			if (gotWhile)
				whereClause.append(" and");
			else
				whereClause.append(" where");
			whereClause.append(" t.id = " + trial.getID());
			gotWhile = true;
		}

		return new DataSessionIterator(getUserEvents(whereClause.toString()));
	}

	// returns a Vector of UserEvents
	public Vector getUserEvents(String whereClause) {
		if (userEventHash == null)
			userEventHash = new Hashtable();
		Vector userEvents = new Vector();
		// create a string to hit the database
		StringBuffer buf = new StringBuffer();
		buf.append("select distinct u.id, u.trial, u.name, ");
		buf.append("u.group_name ");
		buf.append("from user_event u inner join trial t on u.trial = t.id ");
		buf.append("inner join experiment e on t.experiment = e.id ");
		buf.append(whereClause);
		// System.out.println(buf.toString());

		// get the results
		try {
	    	ResultSet resultSet = db.executeQuery(buf.toString());	
			UserEvent tmpUserEvent = null;
	    	while (resultSet.next() != false) {
				UserEvent ue = new UserEvent();
				ue.setUserEventID(resultSet.getInt(1));
				ue.setTrialID(resultSet.getInt(2));
				ue.setName(resultSet.getString(3));
				ue.setGroup(resultSet.getString(4));
				userEvents.addElement(ue);
				tmpUserEvent = (UserEvent)userEventHash.get(new Integer(ue.getUserEventID()));
				if (tmpUserEvent == null)
					userEventHash.put(new Integer(ue.getUserEventID()), ue);
	    	}
			resultSet.close(); 
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
		}
		
		return userEvents;
	}

	public Function setFunction(int id) {
		Function function = null;
		this.functions = new Vector();
		function = getFunction(id);
		if (function != null)
			this.functions.addElement(function);
		return function;
	}

	public UserEvent setUserEvent(int id) {
		UserEvent userEvent = null;
		this.userEvents = new Vector();
		userEvent = getUserEvent(id);
		if (userEvent != null)
			this.userEvents.addElement(userEvent);
		return userEvent;
	}

	public ListIterator getFunctionData() {
		// get the hash of function names first
		if (functions == null) {
			getFunctions();
		}

		// get the metric count
		boolean gotWhile = false;
		int metricCount = 0;
		if (metrics != null) {
			metricCount = metrics.size();
		} else {
		StringBuffer buf2 = new StringBuffer();
		buf2.append("select count (distinct m.id) from metric m ");
		buf2.append("inner join xml_file x on m.id = x.metric ");
		buf2.append("inner join trial t on x.trial = t.id ");
		buf2.append("inner join experiment e on e.id = t.experiment ");
		buf2.append("inner join function f on t.id = f.trial ");
		if (application != null) {
			buf2.append(" where e.application = " + application.getID());
			gotWhile = true;
		}
		if (experiment != null) {
			if (gotWhile)
				buf2.append(" and");
			else
				buf2.append(" where");
			buf2.append(" t.experiment = " + experiment.getID());
			gotWhile = true;
		}
		if (trial != null) {
			if (gotWhile)
				buf2.append(" and");
			else
				buf2.append(" where");
			buf2.append(" t.id = " + trial.getID());
			gotWhile = true;
		}
		if (functions != null) {
			if (gotWhile)
				buf2.append(" and f.id in ('");
			else
				buf2.append(" where f.id in ('");
			Function function;;
        	for(Enumeration en = functions.elements(); en.hasMoreElements() ;) {
				function = (Function) en.nextElement();
				buf2.append(function.getIndexID());
				if (en.hasMoreElements())
					buf2.append("', '");
				else
					buf2.append("') ");
			}
			gotWhile = true;
		}
		try {
			// System.out.println(buf2.toString());
	    	ResultSet resultSet = db.executeQuery(buf2.toString());	
	    	if (resultSet.next() != false) {
				metricCount = resultSet.getInt(1);
			}
			resultSet.close();
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
		}
		}

		// create a string to hit the database
		StringBuffer buf = new StringBuffer();
		buf.append("select trial, function, metric, node, context, thread, ");
		buf.append("inclusive_percentage, ");
		buf.append("inclusive, exclusive_percentage, exclusive, ");
		buf.append("call, subroutines, inclusive_per_call ");
		buf.append("from ");
		if (trial != null) {
			buf.append(" function_interval1 where trial = " + trial.getID());
		}
		else if (experiment != null) {
			buf.append(" function_interval2 where experiment = " + experiment.getID());
		}
		else if (application != null) {
			buf.append(" function_interval where application = " + application.getID());
		}
		if (nodes != null) {
			buf.append(" and node in (");
			Integer node;
        	for(Enumeration en = nodes.elements(); en.hasMoreElements() ;) {
				node = (Integer) en.nextElement();
				buf.append(node);
				if (en.hasMoreElements())
					buf.append(", ");
				else
					buf.append(") ");
			}
		}
		if (contexts != null) {
			buf.append(" and context in (");
			Integer context;
        	for(Enumeration en = contexts.elements(); en.hasMoreElements() ;) {
				context = (Integer) en.nextElement();
				buf.append(context);
				if (en.hasMoreElements())
					buf.append(", ");
				else
					buf.append(") ");
			}
			gotWhile = true;
		}
		if (threads != null) {
			buf.append(" and thread in (");
			Integer thread;
        	for(Enumeration en = threads.elements(); en.hasMoreElements() ;) {
				thread = (Integer) en.nextElement();
				buf.append(thread);
				if (en.hasMoreElements())
					buf.append(", ");
				else
					buf.append(") ");
			}
			gotWhile = true;
		}
		if (functions != null) {
			buf.append(" and function in (");
			Function function;
        	for(Enumeration en = functions.elements(); en.hasMoreElements() ;) {
				function = (Function) en.nextElement();
				buf.append(function.getIndexID());
				if (en.hasMoreElements())
					buf.append(", ");
				else
					buf.append(") ");
			}
		}
		if (metrics != null) {
			buf.append(" and metric in ('");
			String metric;
        	for(Enumeration en = metrics.elements(); en.hasMoreElements() ;) {
				metric = (String) en.nextElement();
				buf.append(metric);
				if (en.hasMoreElements())
					buf.append("', '");
				else
					buf.append("') ");
			}
			gotWhile = true;
		}
		buf.append(" order by trial, function, metric, node, context, thread ");
		// System.out.println(buf.toString());

		int[] node = null;
		int[] context = null;
		int[] thread = null;
		int[] function = null;
		int[] numCalls = null;
		int[] numSubroutines = null;
		double[] data = null;
		FunctionDataIterator fdi = null;
		int size = 0;
		// get the results
		try {
            Date start = new Date();
            ResultSet resultSet = db.executeQuery(buf.toString());
            Date end = new Date();
            long interval = end.getTime() - start.getTime();
            System.out.print(interval);
			resultSet.last();
			size = resultSet.getRow();
			resultSet.beforeFirst();
			node = new int[(size/metricCount)];
			context = new int[(size/metricCount)];
			thread = new int[(size/metricCount)];
			function = new int[(size/metricCount)];
			numCalls = new int[(size/metricCount)];
			numSubroutines = new int[(size/metricCount)];
			data = new double[(size * 5)];
			int index = 0;
	    	while (resultSet.next() != false) {
				int metricIndex = 0;
				function[index] = resultSet.getInt(2);
				node[index] = resultSet.getInt(4);
				context[index] = resultSet.getInt(5);
				thread[index] = resultSet.getInt(6);
				numCalls[index] = (int)(resultSet.getDouble(11));
				numSubroutines[index] = (int)(resultSet.getDouble(12));

				int current = index * metricCount * 5;
				data[current++] = resultSet.getDouble(7);
				data[current++] = resultSet.getDouble(8);
				data[current++] = resultSet.getDouble(9);
				data[current++] = resultSet.getDouble(10);
				data[current++] = resultSet.getDouble(13);
				for (int i = 1 ; i < metricCount ; i++) {
	    			if (resultSet.next() == false) { break; }
					data[current++] = resultSet.getDouble(7);
					data[current++] = resultSet.getDouble(8);
					data[current++] = resultSet.getDouble(9);
					data[current++] = resultSet.getDouble(10);
					data[current++] = resultSet.getDouble(13);
				}
				index++;
	    	}
			resultSet.close(); 
			fdi = new FunctionDataIterator(size/metricCount, metricCount, node, context, thread, function, numCalls, numSubroutines, data);
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
		}
		return (fdi);
	}
	
	public ListIterator getUserEventData() {
		// get the hash of userEvent names first
		if (userEvents == null)
			getUserEvents();

		Vector userEventData = new Vector();
		// create a string to hit the database
		StringBuffer buf = new StringBuffer();
		buf.append("select distinct p.id, p.user_event, p.node, ");
		buf.append("p.context, p.thread, p.sample_count, ");
		buf.append("p.maximum_value, p.minimum_value, p.mean_value, ");
		buf.append("p.standard_deviation, u.trial ");
		buf.append("from atomic_location_profile p ");
		buf.append("inner join user_event u on u.id = p.user_event ");
		buf.append("inner join trial t on u.trial = t.id ");
		buf.append("inner join experiment e on e.id = t.experiment ");
		boolean gotWhile = false;
		if (application != null) {
			buf.append(" where e.application = " + application.getID());
			gotWhile = true;
		}
		if (experiment != null) {
			if (gotWhile)
				buf.append(" and");
			else
				buf.append(" where");
			buf.append(" t.experiment = " + experiment.getID());
			gotWhile = true;
		}
		if (trial != null) {
			if (gotWhile)
				buf.append(" and");
			else
				buf.append(" where");
			buf.append(" t.id = " + trial.getID());
			gotWhile = true;
		}
		if (nodes != null) {
			if (gotWhile)
				buf.append(" and p.node in (");
			else
				buf.append(" where p.node in (");
			Integer node;
        	for(Enumeration en = nodes.elements(); en.hasMoreElements() ;) {
				node = (Integer) en.nextElement();
				buf.append(node);
				if (en.hasMoreElements())
					buf.append(", ");
				else
					buf.append(") ");
			}
			gotWhile = true;
		}
		if (contexts != null) {
			if (gotWhile)
				buf.append(" and p.context in (");
			else
				buf.append(" where p.context in (");
			Integer context;
        	for(Enumeration en = contexts.elements(); en.hasMoreElements() ;) {
				context = (Integer) en.nextElement();
				buf.append(context);
				if (en.hasMoreElements())
					buf.append(", ");
				else
					buf.append(") ");
			}
			gotWhile = true;
		}
		if (threads != null) {
			if (gotWhile)
				buf.append(" and p.thread in (");
			else
				buf.append(" where p.thread in (");
			Integer thread;
        	for(Enumeration en = threads.elements(); en.hasMoreElements() ;) {
				thread = (Integer) en.nextElement();
				buf.append(thread);
				if (en.hasMoreElements())
					buf.append(", ");
				else
					buf.append(") ");
			}
			gotWhile = true;
		}
		if (userEvents != null) {
			if (gotWhile)
				buf.append(" and u.id in (");
			else
				buf.append(" where u.id in (");
			UserEvent userEvent;
        	for(Enumeration en = userEvents.elements(); en.hasMoreElements() ;) {
				userEvent = (UserEvent) en.nextElement();
				buf.append(userEvent.getUserEventID());
				if (en.hasMoreElements())
					buf.append(", ");
				else
					buf.append(") ");
			}
			gotWhile = true;
		}
		buf.append(" order by u.trial, p.node, p.context, p.thread, p.user_event");
		// System.out.println(buf.toString());

		// get the results
		try {
	    	ResultSet resultSet = db.executeQuery(buf.toString());	
	    	while (resultSet.next() != false) {
				UserEventDataObject ueDO = new UserEventDataObject();
				ueDO.setUserEventID(resultSet.getInt(2));
				ueDO.setNode(resultSet.getInt(3));
				ueDO.setContext(resultSet.getInt(4));
				ueDO.setThread(resultSet.getInt(5));
				ueDO.setSampleCount(resultSet.getInt(6));
				ueDO.setMaximumValue(resultSet.getDouble(7));
				ueDO.setMinimumValue(resultSet.getDouble(8));
				ueDO.setMeanValue(resultSet.getDouble(9));
				ueDO.setStandardDeviation(resultSet.getDouble(10));
				userEventData.addElement(ueDO);
	    	}
			resultSet.close(); 
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
		}
		return new DataSessionIterator(userEventData);
	}
	
	public Function getFunction(int id) {
		Function function = null;
		if (functionHash != null) {
			function = (Function)functionHash.get(new Integer(id));
		}
		if (function == null) {
			// create a string to hit the database
			String whereClause;
			whereClause = " where id = " + id;
			Vector functions = getFunctions(whereClause);
			if (functions.size() == 1) {
				function = (Function)functions.elementAt(0);
			} //else exception?
		}
		return function;
	}
	
	public UserEvent getUserEvent(int id) {
		UserEvent userEvent = null;
		if (userEventHash != null) {
			userEvent = (UserEvent)userEventHash.get(new Integer(id));
		}
		if (userEvent == null) {
			// create a string to hit the database
			String whereClause;
			whereClause = " where u.id = " + id;
			Vector userEvents = getUserEvents(whereClause);
			if (userEvents.size() == 1) {
				userEvent = (UserEvent)userEvents.elementAt(0);
			} //else exception?
		}
		return userEvent;
	}
	
	// override the setMetric method
	public void setMetric(String metricName) {
		if (metricName == null) {
			this.metrics = null;
			return;
		}
		this.metrics = new Vector();
		String sql = "select id from metric where name = '" + metricName + "';";
		try {
	    	ResultSet resultSet = db.executeQuery(sql);	
	    	while (resultSet.next() != false) {
				String tmpStr = new String(resultSet.getString(1));
				this.metrics.addElement(tmpStr);
			}
			resultSet.close(); 
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return;
		}
	}

	// override the setMetric method
	public void setMetric(Vector metricNames) {
		if (metricNames == null) {
			this.metrics = null;
			return;
		}
		this.metrics = new Vector();
		StringBuffer sql = new StringBuffer();
		sql.append ("select id from metric where name = ");
		for (int i=0; i < metricNames.size() ; i++) {
			sql.append ("'" + metricNames.elementAt(i) + "'");
		}
		sql.append (";");
		try {
	    	ResultSet resultSet = db.executeQuery(sql.toString());	
	    	while (resultSet.next() != false) {
				String tmpStr = new String(resultSet.getString(1));
				this.metrics.addElement(tmpStr);
			}
			resultSet.close(); 
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return;
		}
	}

};

