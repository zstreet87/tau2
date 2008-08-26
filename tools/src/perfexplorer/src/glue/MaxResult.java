/**
 * 
 */
package glue;

import edu.uoregon.tau.perfdmf.Trial;

/**
 * @author khuck
 *
 */
public class MaxResult extends DefaultResult {

	/**
	 * 
	 */
	public MaxResult() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param input
	 */
	public MaxResult(PerformanceResult input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

	public MaxResult(PerformanceResult input, boolean doFullCopy) {
		super(input, doFullCopy);
	}

	/* (non-Javadoc)
	 * @see glue.AbstractResult#putDataPoint(java.lang.Integer, java.lang.String, java.lang.String, int, double)
	 */
	@Override
	public void putDataPoint(Integer thread, String event, String metric, int type, double value) {
		double oldValue = super.getDataPoint(thread, event, metric, type);
		if (value > oldValue) {
			super.putDataPoint(thread, event, metric, type, value);
		}
	}

	public String toString() {
		return "MAX";
	}

}
