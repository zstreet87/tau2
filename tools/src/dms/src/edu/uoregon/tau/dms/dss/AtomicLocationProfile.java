package edu.uoregon.tau.dms.dss;

import edu.uoregon.tau.dms.database.DB;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
/**
 * Holds all the data for a atomic event data object in the database.
 * This object is returned by the DataSession class and all of its subtypes.
 * The AtomicEventData object contains all the information associated with
 * an atomic event location instance from which the TAU performance data has been generated.
 * A atomic event location is associated with one node, context, thread, atomic event, trial, 
 * experiment and application.
 * <p>
 * A AtomicEventData object has information
 * related to one particular atomic event location in the trial, including the ID of the atomic event,
 * the node, context and thread that identify the location, and the data collected for this
 * location, such as sample count, maximum value, minimum value, mean value and standard deviation.  
 *
 * <P>CVS $Id: AtomicLocationProfile.java,v 1.1 2004/05/05 17:43:29 khuck Exp $</P>
 * @author	Kevin Huck, Robert Bell
 * @version	0.1
 * @since	0.1
 * @see		DataSession#getAtomicEventData
 * @see		DataSession#setAtomicEvent
 * @see		DataSession#setNode
 * @see		DataSession#setContext
 * @see		DataSession#setThread
 * @see		DataSession#setMetric
 * @see		AtomicEvent
 */
public class AtomicLocationProfile {
	private int atomicEventID;
	private int profileID;
	private int node;
	private int context;
	private int thread;
	private int sampleCount;
	private double maximumValue;
	private double minimumValue;
	private double meanValue;
	private double standardDeviation;

/**
 * Returns the unique ID for the atomic event that owns this data
 *
 * @return	the atomic event ID.
 * @see		AtomicEvent
 */
	public int getAtomicEventID () {
		return this.atomicEventID;
	}

/**
 * Returns the unique ID for this data object. 
 *
 * @return	the atomic event data ID.
 */
	public int getProfileID () {
		return this.profileID;
	}

/**
 * Returns the node for this data location.
 *
 * @return the node index.
 */
	public int getNode () {
		return this.node;
	}

/**
 * Returns the context for this data location.
 *
 * @return the context index.
 */
	public int getContext () {
		return this.context;
	}

/**
 * Returns the thread for this data location.
 *
 * @return the thread index.
 */
	public int getThread () {
		return this.thread;
	}

/**
 * Returns the number of calls to this function at this location.
 *
 * @return	the number of calls.
 */
	public int getSampleCount () {
		return this.sampleCount;
	}

/**
 * Returns the maximum value recorded for this atomic event.
 *
 * @return	the maximum value.
 */
	public double getMaximumValue () {
		return this.maximumValue;
	}

/**
 * Returns the minimum value recorded for this atomic event.
 *
 * @return	the minimum value.
 */
	public double getMinimumValue () {
		return this.minimumValue;
	}

/**
 * Returns the mean value recorded for this atomic event.
 *
 * @return	the mean value.
 */
	public double getMeanValue () {
		return this.meanValue;
	}

/**
 * Returns the standard deviation calculated for this atomic event.
 *
 * @return	the standard deviation value.
 */
	public double getStandardDeviation () {
		return this.standardDeviation;
	}

/**
 * Sets the unique atomic event ID for the atomic event at this location.
 * <i> NOTE: This method is used by the DataSession object to initialize
 * the object.  Not currently intended for use by any other code.</i>
 *
 * @param	atomicEventID a unique atomic event ID.
 */
	public void setAtomicEventID (int atomicEventID) {
		this.atomicEventID = atomicEventID;
	}

/**
 * Sets the unique ID for this data object. 
 * <i> NOTE: This method is used by the DataSession object to initialize
 * the object.  Not currently intended for use by any other code.</i>
 *
 * @param	profileID	the unique atomic event data ID.
 */
	public void setProfileID (int profileID) {
		this.profileID = profileID;
	}

/**
 * Sets the node of the current location that this data object represents.
 * <i> NOTE: This method is used by the DataSession object to initialize
 * the object.  Not currently intended for use by any other code.</i>
 *
 * @param	node the node for this location.
 */
	public void setNode (int node) {
		this.node = node;
	}

/**
 * Sets the context of the current location that this data object represents.
 * <i> NOTE: This method is used by the DataSession object to initialize
 * the object.  Not currently intended for use by any other code.</i>
 *
 * @param	context the context for this location.
 */
	public void setContext (int context) {
		this.context = context;
	}

/**
 * Sets the thread of the current location that this data object represents.
 * <i> NOTE: This method is used by the DataSession object to initialize
 * the object.  Not currently intended for use by any other code.</i>
 *
 * @param	thread the thread for this location.
 */
	public void setThread (int thread) {
		this.thread = thread;
	}

/**
 * Sets the number of times the atomic event occurred at this location.
 * <i> NOTE: This method is used by the DataSession object to initialize
 * the object.  Not currently intended for use by any other code.</i>
 *
 * @param	sampleCount the sample count at this location
 */
	public void setSampleCount (int sampleCount) {
		this.sampleCount = sampleCount;
	}

/**
 * Sets the maximum value recorded for this atomic event at this location.
 * <i> NOTE: This method is used by the DataSession object to initialize
 * the object.  Not currently intended for use by any other code.</i>
 *
 * @param	maximumValue the maximum value at this location
 */
	public void setMaximumValue (double maximumValue) {
		this.maximumValue = maximumValue;
	}

/**
 * Sets the minimum value recorded for this atomic event at this location.
 * <i> NOTE: This method is used by the DataSession object to initialize
 * the object.  Not currently intended for use by any other code.</i>
 *
 * @param	minimumValue the minimum value at this location
 */
	public void setMinimumValue (double minimumValue) {
		this.minimumValue = minimumValue;
	}

/**
 * Sets the mean value calculated for this atomic event at this location.
 * <i> NOTE: This method is used by the DataSession object to initialize
 * the object.  Not currently intended for use by any other code.</i>
 *
 * @param	meanValue the mean value at this location
 */
	public void setMeanValue (double meanValue) {
		this.meanValue = meanValue;
	}

/**
 * Sets the standard deviation value calculated for this atomic event at this location.
 * <i> NOTE: This method is used by the DataSession object to initialize
 * the object.  Not currently intended for use by any other code.</i>
 *
 * @param	standardDeviation the standard deviation value at this location
 */
	public void setStandardDeviation (double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public static Vector getAtomicEventData(DB db, String whereClause) {
		Vector atomicEventData = new Vector();
		// create a string to hit the database
		StringBuffer buf = new StringBuffer();
		buf.append("select p.id, p.atomic_event, p.node, ");
		buf.append("p.context, p.thread, p.sample_count, ");
		buf.append("p.maximum_value, p.minimum_value, p.mean_value, ");
		buf.append("p.standard_deviation, u.trial ");
		buf.append("from atomic_location_profile p ");
		buf.append("inner join atomic_event u on u.id = p.atomic_event ");
		buf.append("inner join trial t on u.trial = t.id ");
		buf.append("inner join experiment e on e.id = t.experiment ");
		buf.append(whereClause);
		buf.append(" order by p.node, p.context, p.thread, p.atomic_event");
		// System.out.println(buf.toString());

		// get the results
		try {
	    	ResultSet resultSet = db.executeQuery(buf.toString());	
	    	while (resultSet.next() != false) {
				AtomicLocationProfile ueDO = new AtomicLocationProfile();
				ueDO.setAtomicEventID(resultSet.getInt(2));
				ueDO.setNode(resultSet.getInt(3));
				ueDO.setContext(resultSet.getInt(4));
				ueDO.setThread(resultSet.getInt(5));
				ueDO.setSampleCount(resultSet.getInt(6));
				ueDO.setMaximumValue(resultSet.getDouble(7));
				ueDO.setMinimumValue(resultSet.getDouble(8));
				ueDO.setMeanValue(resultSet.getDouble(9));
				ueDO.setStandardDeviation(resultSet.getDouble(10));
				atomicEventData.addElement(ueDO);
	    	}
			resultSet.close(); 
		}catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
		}
		return atomicEventData;
	}

	public void saveAtomicEventData(DB db, int atomicEventID) {
		try {
			PreparedStatement statement = null;
			statement = db.prepareStatement("INSERT INTO atomic_location_profile (atomic_event, node, context, thread, sample_count, maximum_value, minimum_value, mean_value, standard_deviation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, atomicEventID);
			statement.setInt(2, node);
			statement.setInt(3, context);
			statement.setInt(4, thread);
			statement.setInt(5, sampleCount);
			statement.setDouble(6, maximumValue);
			statement.setDouble(7, minimumValue);
			statement.setDouble(8, meanValue);
			statement.setDouble(9, standardDeviation);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println("An error occurred while saving the trial.");
			e.printStackTrace();
			System.exit(0);
		}
	}
}

