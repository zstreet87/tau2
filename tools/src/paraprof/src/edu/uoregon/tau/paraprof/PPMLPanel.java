/*  
    PPMLPanel.java

    Title:      ParaProf
    Author:     Robert Bell
    Description:  
*/

package edu.uoregon.tau.paraprof;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import edu.uoregon.tau.dms.dss.*;

public class PPMLPanel extends JPanel implements ActionListener{ 
    
    public PPMLPanel(ParaProfManager paraProfManager){
	this.paraProfManager = paraProfManager;

	//####################################
	//Window Stuff.
	//####################################
	int windowWidth = 800;
	int windowHeight = 200;
	setSize(new java.awt.Dimension(windowWidth, windowHeight));
	//####################################
	//End - Window Stuff.
	//####################################

	//####################################
	//Create and add the components.
	//####################################
	//Setting up the layout system for the main window.
	GridBagLayout gbl = new GridBagLayout();
	this.setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.insets = new Insets(5, 5, 5, 5);

 	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.weightx = 0;
	gbc.weighty = 0;
	addCompItem(new JLabel("Argument 1:"), gbc, 0, 0, 1, 1);

	gbc.fill = GridBagConstraints.BOTH;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.weightx = 100;
	gbc.weighty = 0;
	addCompItem(arg1Field, gbc, 1, 0, 2, 1);

	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.weightx = 0;
	gbc.weighty = 0;
	addCompItem(new JLabel("Argument 2:"), gbc, 0, 1, 1, 1);
	    
	gbc.fill = GridBagConstraints.BOTH;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.weightx = 100;
	gbc.weighty = 0;
	addCompItem(arg2Field, gbc, 1, 1, 2, 1);

	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.weightx = 0;
	gbc.weighty = 0;
	addCompItem(operation, gbc, 1, 2, 1, 1);

	JButton jButton = new JButton("Apply operation");
	jButton.addActionListener(this);
	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.EAST;
	gbc.weightx = 0;
	gbc.weighty = 0;
	addCompItem(jButton, gbc, 2, 2, 1, 1);
	//####################################
	//End - Create and add the components.
	//####################################
    }

    public void setArg1Field(String arg1){
	arg1Field.setText(arg1);}

    public String getArg1Field(){
	return arg1Field.getText().trim();}

    public void setArg2Field(String arg2){
	arg2Field.setText(arg2);}

    public String getArg2Field(){
	return arg2Field.getText().trim();}

    public void applyOperation(){
	Metric metric = PPML.applyOperation(paraProfManager.getOperand1(),
					    paraProfManager.getOperand2(),
					    (String) operation.getSelectedItem());
	if(metric!=null){
	    if(metric.getTrial().dBTrial())
		paraProfManager.uploadMetric(metric);
	    paraProfManager.populateTrialMetrics(metric.getTrial());
	}
    }
    
    //####################################
    //Interface code.
    //####################################
    
    //######
    //ActionListener.
    //######
    public void actionPerformed(ActionEvent evt){
	try{
	    Object EventSrc = evt.getSource();
	    String arg = evt.getActionCommand();
	    if(arg.equals("Apply operation")){
		this.applyOperation();
	    }
	}
	catch(Exception e){
	    UtilFncs.systemError(e, null, "DBC02");
	}
    }
    //######
    //End - ActionListener.
    //######

    //####################################
    //End - Interface code.
    //####################################

    //####################################
    //Private Section.
    //####################################
    private void addCompItem(Component c, GridBagConstraints gbc, int x, int y, int w, int h){
	try{
	    gbc.gridx = x;
	    gbc.gridy = y;
	    gbc.gridwidth = w;
	    gbc.gridheight = h;
	    
	    this.add(c, gbc);
	}
	catch(Exception e){
	    UtilFncs.systemError(e, null, "DBC03");
	}
    }
    
    //####################################
    //End - Private Section.
    //####################################
    
    //####################################
    //Instance data.
    //####################################
    ParaProfManager paraProfManager = null;
    JTextField arg1Field = new JTextField("Argument 1 (x:x:x:x)", 15);
    JTextField arg2Field = new JTextField("Argument 2 (x:x:x:x)", 15);
    String operationStrings[] = {"Add", "Subtract", "Multiply", "Divide"};
    JComboBox operation = new JComboBox(operationStrings);
    //####################################
    //End - Instance data.
    //#################################### 
}
