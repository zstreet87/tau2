package server;

import org.jfree.data.xy.AbstractXYDataset;
import clustering.RawDataInterface;

/**
 * Dataset to store scatterplot data.
 * The JFreeChart API requires that client applications extend the 
 * AbstractXYDataset class to implement the data to be plotted in a scatterplot.
 * This is essentially a wrapper around the RawDataInterface class.
 * 
 * <P>CVS $Id: PCAPlotDataset.java,v 1.3 2005/09/27 19:46:32 khuck Exp $</P>
 * @author  Kevin Huck
 * @version 0.1
 * @since   0.1
 */
public class PCAPlotDataset extends AbstractXYDataset {

	// KAH private RawDataInterface pcaData = null;
	// KAH private RawDataInterface rawData = null;
	// KAH private KMeansClusterInterface clusterer = null;
	// KAH private Instances[] clusters = null;
	private RawDataInterface[] clusters = null;
	// KAH private int x = 0;
	// KAH private int y = 1;
	// KAH private int k = 0;

	/**
	 * Constructor.
	 * 
	 * @param pcaData
	 * @param rawData
	 * @param clusterer
	 */
	/*
	public PCAPlotDataset(RawDataInterface pcaData, RawDataInterface rawData, KMeansClusterInterface clusterer, int engine) {
		super();
		this.pcaData = rawData;
		// get a reference to the clusterer
		this.clusterer = clusterer;
		// get the number of clusters
		this.k = clusterer.getK();

			this.clusters = new Instances[k];
			Instances pca = (Instances) pcaData.getData();
			for (int i = 0 ; i < k ; i++) 
				this.clusters[i] = new Instances(pca, 0);
		 	// after PCA, the two greatest components are at the END of the list
		 	// of components.  Therefore, get the last and second-to-last
		 	// components.
			//System.out.println("numAttributes: " + pca.numAttributes());
			if (pca.numAttributes() > 1) {
				x = pca.numAttributes() - 1;
				y = pca.numAttributes() - 2;
			} else {
				y = 0;
			}
			
		 	// For each element in the raw data, determine which cluster it
		 	// belongs in.  That will determine what color the point should be. 
			for (int i = 0 ; i < rawData.numVectors() ; i++) {
				int location = clusterer.clusterInstance(i);
				clusters[location].add(pca.instance(i));
			}
	}
*/
	public PCAPlotDataset(RawDataInterface[] clusters) {
		this.clusters = clusters;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.SeriesDataset#getSeriesCount()
	 */
	public int getSeriesCount() {
		if (clusters == null)
			System.exit(1);
		return java.lang.reflect.Array.getLength(clusters);
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.SeriesDataset#getSeriesName(int)
	 */
	public String getSeriesName(int arg0) {
		return new String("Cluster " + arg0);
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 */
	public int getItemCount(int arg0) {
		return clusters[arg0].numVectors();
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	public Number getX(int arg0, int arg1) {
		return new Double(clusters[arg0].getValue(arg1,0));
		//return new Double(data.getValue(arg1, x));
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	public Number getY(int arg0, int arg1) {
		//System.out.println("Getting Y: " + arg0 + ", " + arg1 + ", " + y);
		return new Double(clusters[arg0].getValue(arg1,1));
		//return new Double(clusters[arg0].instance(arg1).value(y));
		//return new Double(data.getValue(arg1, y));
	}
}
