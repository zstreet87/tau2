/*
 * Created on Mar 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.uoregon.tau.paraprof;

import edu.uoregon.tau.dms.dss.Function;
import edu.uoregon.tau.paraprof.enums.ValueType;
import edu.uoregon.tau.paraprof.enums.VisType;
import edu.uoregon.tau.paraprof.vis.Vec;
import edu.uoregon.tau.paraprof.vis.Axes.Orientation;


/**
 * @author amorris
 *
 * TODO ...
 */
public class ThreeDeeSettings implements Cloneable {


    
    private float plotWidth, plotHeight, plotDepth;
//    private Plot plot;
//    private Axes axes;
    
    private int heightMetricID, colorMetricID;
    private ValueType colorValue = ValueType.EXCLUSIVE, heightValue = ValueType.EXCLUSIVE;
    private VisType visType = VisType.TRIANGLE_MESH_PLOT;
    //private VisType visType = VisType.BAR_PLOT;
    
    private Orientation axisOrientation = Orientation.NW;
    private boolean axesEnabled = true;
    
    
    
    private int[] scatterMetricIDs = { 0, 0, 0, 0};
    private ValueType[] scatterValueTypes = {ValueType.EXCLUSIVE, ValueType.EXCLUSIVE, ValueType.EXCLUSIVE, ValueType.EXCLUSIVE};
    private Function[] scatterFunctions = new Function[4];
    
    
    private int[] selections = {-1, 0};
    
    private Vec scatterAim = new Vec(7.5f, 7.5f, 7.5f), scatterEye;
    private Vec regularAim, regularEye;
    
    /**
     * @return Returns the visType.
     */
    public VisType getVisType() {
        return visType;
    }
    /**
     * @param visType The visType to set.
     */
    public void setVisType(VisType visType) {
        this.visType = visType;
    }
    /**
     * @return Returns the colorValue.
     */
    public ValueType getColorValue() {
        return colorValue;
    }
    /**
     * @param colorValue The colorValue to set.
     */
    public void setColorValue(ValueType colorValue) {
        this.colorValue = colorValue;
    }
    /**
     * @return Returns the heightValue.
     */
    public ValueType getHeightValue() {
        return heightValue;
    }
    /**
     * @param heightValue The heightValue to set.
     */
    public void setHeightValue(ValueType heightValue) {
        this.heightValue = heightValue;
    }
    public void setSize(float plotWidth, float plotDepth, float plotHeight) {
        this.plotWidth = plotWidth;
        this.plotDepth = plotDepth;
        this.plotHeight = plotHeight;
        
    }
    
    public float getPlotHeight() {
        return plotHeight;
    }

    public float getPlotDepth() {
        return plotDepth;
    }

    public float getPlotWidth() {
        return plotWidth;
    }

    public void setHeightMetricID(int metricID) {
        this.heightMetricID = metricID;
    }

    public void setColorMetricID(int metricID) {
        this.colorMetricID = metricID;
    }

    public int getHeightMetricID() {
        return this.heightMetricID;
    }

    public int getColorMetricID() {
        return this.colorMetricID;
    }

    public Object clone() {
        ThreeDeeSettings newSettings = new ThreeDeeSettings();
        
        newSettings.plotDepth = this.plotDepth;
        newSettings.plotHeight = this.plotHeight;
        newSettings.plotWidth = this.plotWidth;
        newSettings.heightMetricID = this.heightMetricID;
        newSettings.colorMetricID = this.colorMetricID;
        newSettings.colorValue = this.colorValue;
        newSettings.heightValue = this.heightValue;
        
        newSettings.visType = this.visType;
        newSettings.axisOrientation = this.axisOrientation;
        newSettings.axesEnabled = this.axesEnabled;
        

        newSettings.scatterMetricIDs = (int[])this.scatterMetricIDs.clone();
        newSettings.scatterValueTypes = (ValueType[])this.scatterValueTypes.clone();
        newSettings.scatterFunctions = (Function[])this.scatterFunctions.clone();
    
        newSettings.regularAim = this.regularAim;
        newSettings.regularEye = this.regularEye;
        
        newSettings.scatterAim = this.scatterAim;
        newSettings.scatterEye = this.scatterEye;
        
        newSettings.selections = (int[])this.selections.clone();
        
        return newSettings;
    }
    
    
//    public Plot getPlot() {
//        return plot;
//    }
    
//    public Axes getAxes() {
//        return axes;
//    }
    /**
     * @return Returns the axisOrientation.
     */
    public Orientation getAxisOrientation() {
        return axisOrientation;
    }
    /**
     * @param axisOrientation The axisOrientation to set.
     */
    public void setAxisOrientation(Orientation axisOrientation) {
        this.axisOrientation = axisOrientation;
    }
    public boolean isAxesEnabled() {
        return axesEnabled;
    }
    public void setAxesEnabled(boolean axesEnabled) {
        this.axesEnabled = axesEnabled;
    }
    public Function[] getScatterFunctions() {
        return scatterFunctions;
    }
    
    public void setScatterFunction(Function function, int index) {
        this.scatterFunctions[index] = function;
    }

    public int[] getScatterMetricIDs() {
        return scatterMetricIDs;
    }
    public void setScatterMetricID(int scatterMetricID, int index) {
        this.scatterMetricIDs[index] = scatterMetricID;
    }
    public ValueType[] getScatterValueTypes() {
        return scatterValueTypes;
    }
    public void setScatterValueType(ValueType scatterValueType, int index) {
        this.scatterValueTypes[index] = scatterValueType;
    }
    public Vec getRegularAim() {
        return regularAim;
    }
    public void setRegularAim(Vec regularAim) {
        this.regularAim = regularAim;
    }
    public Vec getRegularEye() {
        return regularEye;
    }
    public void setRegularEye(Vec regularEye) {
        this.regularEye = regularEye;
    }
    public Vec getScatterAim() {
        return scatterAim;
    }
    public void setScatterAim(Vec scatterAim) {
        this.scatterAim = scatterAim;
    }
    public Vec getScatterEye() {
        return scatterEye;
    }
    public void setScatterEye(Vec scatterEye) {
        this.scatterEye = scatterEye;
    }


    public int[] getSelections() {
        return this.selections;
    }
    
    
    public void setSelection(int index, int value) {
        this.selections[index] = value;
    }
    
}
