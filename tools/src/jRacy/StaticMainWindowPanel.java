/* 
	
	StaticMainWindowPanel.java
	
	Title:			jRacy
	Author:			Robert Bell
	Description:	
*/

package jRacy;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;


public class StaticMainWindowPanel extends JPanel implements ActionListener, MouseListener
{
	//******************************
	//Instance data.
	//******************************
	StaticMainWindow sMWindow = null;
	
	int xPanelSize = 600;
	int yPanelSize = 300;
	
	//**********
	//Popup menu definitions.
	private JPopupMenu popup = new JPopupMenu();
	//**********
	
	//**********
	//Some place holder definitions - used for cycling through data lists.
	Vector contextList = null;
	Vector threadList = null;
	Vector threadDataList = null;
	SMWServer sMWServer = null;
	SMWContext sMWContext = null;
	SMWThread sMWThread = null;
	SMWThreadDataElement sMWThreadDataElement = null;
	SMWMeanDataElement sMWMeanDataElement = null;
	//End - Place holder definitions.
	//**********
	
	//**********
	//Convenient counters.
	int serverCounter = 0;
	int contextCounter = 0;
	int threadCounter = 0;
	int sMWThreadDataElementCounter = 0;
	int colorCounter = 0;
	//End - Convenient counters.
	//**********
	
	//**********
	//Other useful variables for getToolTipText, mouseEvents, and paintComponent.
	int xCoord = -1;
	int yCoord = -1;
	Object clickedOnObject = null;
	//End - Other useful variables for getToolTipText, mouseEvents, and paintComponent.
	//**********
	
	//**********
	//Some misc stuff for the paintComponent function.
	String counterName = null;
	private int defaultBarLength = 500;
	String tmpString = null;
	double tmpSum = -1;
	double tmpDataValue = -1;
	Color tmpColor = null;
	boolean highlighted = false;
	int barXStart = -1;
	int numberOfColors = 0;
	//End - Some misc stuff for the paintComponent function.
	//**********
	
	
	//**********
	//The constructors!!
	public StaticMainWindowPanel()
	{
		try{
			//Set the default tool tip for this panel.
			this.setToolTipText("Incorrect Constructor!!!");
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SMWP01");
		}
	}
	
	public StaticMainWindowPanel(StaticMainWindow inSMW)
	{
		try{
			//Set the default tool tip for this panel.
			this.setToolTipText("Racy bar graph draw window!");
			setBackground(Color.white);
			
			//Add this object as a mouse listener.
			addMouseListener(this);
			
			//Set instance variables.
			sMWindow = inSMW;
			barXStart = 100;
			
			//Add items to the popu menu.
			JMenuItem functionDetailsItem = new JMenuItem("Show Function Details");
			functionDetailsItem.addActionListener(this);
			popup.add(functionDetailsItem);
			
			JMenuItem changeColorItem = new JMenuItem("Change Function Color");
			changeColorItem.addActionListener(this);
			popup.add(changeColorItem);
			
			JMenuItem maskFunctionItem = new JMenuItem("Reset to Generic Color");
			maskFunctionItem.addActionListener(this);
			popup.add(maskFunctionItem);
			
			JMenuItem highlightFunctionItem = new JMenuItem("Highlight this Function");
			highlightFunctionItem.addActionListener(this);
			popup.add(highlightFunctionItem);
			
			JMenuItem unHighlightFunctionItem = new JMenuItem("Un-Highlight this Function");
			unHighlightFunctionItem.addActionListener(this);
			popup.add(unHighlightFunctionItem);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SMWP02");
		}
	}
	//End - The constructors!!
	//**********
	
	public String getToolTipText(MouseEvent evt)
	{
		String S = null;
		
		
		try{
			int tmpYBegin = 0;
			int tmpYEnd = 0;
			
			//Get the location of the mouse.
			xCoord = evt.getX();
			yCoord = evt.getY();
			
			//Check to see if the click occured in the mean values bar.
			if(sMWindow.isDataLoaded())
			{
				//Grab the first member.
				if(!((sMWindow.getSMWMeanData()).isEmpty()))
				{
					sMWMeanDataElement = (SMWMeanDataElement) (sMWindow.getSMWMeanData()).elementAt(0);
					
					if((yCoord >= sMWMeanDataElement.getYBeg()) && (yCoord <= sMWMeanDataElement.getYEnd()))
					{
						//We are inside the mean values bar.  So, cycle through the elements.
						for(Enumeration gM1 = (sMWindow.getSMWMeanData()).elements(); gM1.hasMoreElements() ;)
						{
							sMWMeanDataElement = (SMWMeanDataElement) gM1.nextElement();
							
							
							//Now we are going accross in the X direction.
							if(xCoord < barXStart)
							{
								//Output data to the help window if it is showing.
								if(jRacy.helpWindow.isShowing())
								{
									//Clear the window fisrt.
									jRacy.helpWindow.clearText();
									
									//Now send the help info.
									jRacy.helpWindow.writeText("You are to the left of the mean bar.");
									jRacy.helpWindow.writeText("");
									jRacy.helpWindow.writeText("Using either the right or left mouse buttons, click once" +
															  " to display more detailed data about the" +
															  " mean values for the functions in the system.");
								}
								
								//Return a string indicating that clicking before the display bar
								//will cause thread data to be displayed.
								
								return new String("Right click here to display mean data.");
							}
							else if(xCoord < sMWMeanDataElement.getXEnd())
							{
								//Output data to the help window if it is showing.
								if(jRacy.helpWindow.isShowing())
								{
									//Clear the window fisrt.
									jRacy.helpWindow.clearText();
									
									//Now send the help info.
									jRacy.helpWindow.writeText("Your mouse is over the mean draw bar!");
									jRacy.helpWindow.writeText("");
									jRacy.helpWindow.writeText("Current function name is: " + sMWMeanDataElement.getFunctionName());
									jRacy.helpWindow.writeText("");
									jRacy.helpWindow.writeText("The mean draw bars give a visual representation of the" +
															  " mean values for the functions which have run in the system." +
															  "  The funtions are assigned a color from the current" +
															  " Racy color set.  The colors are cycled through when the" +
															  " number of funtions exceeds the number of available" +
															  " colors." +
															  "  Use the right and left mouse buttons " +
															  "to give additional information.");
								}
								
								//Return the name of the function in the current thread data object.
								return sMWMeanDataElement.getFunctionName();
							}			
						}
						
						//If in here, and at this position, it means that the mouse is not over
						//a bar. However, we might be over the misc. function section.  Check for this.
						if(xCoord <= (barXStart + defaultBarLength))
						{
							//Output data to the help window if it is showing.
							if(jRacy.helpWindow.isShowing())
							{
								//Clear the window fisrt.
								jRacy.helpWindow.clearText();
								
								//Now send the help info.
								jRacy.helpWindow.writeText("Your mouse is over the misc. function section!");
								jRacy.helpWindow.writeText("");
								jRacy.helpWindow.writeText("These are functions which have a non zero value," +
																					" but whose screen representation is less than a pixel.");
								jRacy.helpWindow.writeText("");
								jRacy.helpWindow.writeText("To view these functions, right or left click to the left of" +
																					" this bar to bring up windows which will show more detailed information.");
							}
							
							//Return the name of the function in the current thread data object.
							return "Misc function section ... see help window for details";
						}
					}
				}
				
				for(Enumeration e1 = (sMWindow.getSMWGeneralData()).elements(); e1.hasMoreElements() ;)
					{
						sMWServer = (SMWServer) e1.nextElement();
						if(yCoord <= (sMWServer.getYDrawCoord()))
						{
							//Enter the context loop for this server.
							contextList = sMWServer.getContextList();
							
							for(Enumeration e2 = contextList.elements(); e2.hasMoreElements() ;)
							{
								sMWContext = (SMWContext) e2.nextElement();
								if(yCoord <= (sMWContext.getYDrawCoord()))
								{
									//Enter the thread loop for this context.
									threadList = sMWContext.getThreadList();
									for(Enumeration e3 = threadList.elements(); e3.hasMoreElements() ;)
									{
										sMWThread = (SMWThread) e3.nextElement();
										if(yCoord <= (sMWThread.getYDrawCoord()))
										{
											//Now enter the thread loop for this thread.
											threadDataList = sMWThread.getThreadDataList();
											sMWThreadDataElementCounter = 0;
											for(Enumeration e4 = threadDataList.elements(); e4.hasMoreElements() ;)
											{
												sMWThreadDataElement = (SMWThreadDataElement) e4.nextElement();
												
												//Get the yBeg and yEnd for this thread.
												tmpYBegin = sMWThreadDataElement.getYBeg();
												tmpYEnd = sMWThreadDataElement.getYEnd();
												
												//Now we are going accross in the X direction.
												if(xCoord < barXStart)
												{
													//Make sure that the mouse is not above or below the bar
													//for this thread.  The y values from the first thread data
													//object will indicate this.
													if((yCoord >= tmpYBegin) && (yCoord <= tmpYEnd))
														{
															//Output data to the help window if it is showing.
															if(jRacy.helpWindow.isShowing())
															{
																//Clear the window fisrt.
																jRacy.helpWindow.clearText();
																
																//Now send the help info.
																jRacy.helpWindow.writeText("n,c,t stands for: Node, Context and Thread.");
																jRacy.helpWindow.writeText("");
																jRacy.helpWindow.writeText("Using either the right or left mouse buttons, click once" +
																						   " to display more detailed data about this" +
																						   " thread.");
															}
															
															//Return a string indicating that clicking before the display bar
															//will cause thread data to be displayed.
															return new String("Click once here to display more thread data.");
														}
													else
													{	
														//We do not want to keep cycling through if we have already
														//established that we are not going to draw.
														return S;
													}
												}
												else
													if(xCoord < sMWThreadDataElement.getXEnd())
													{
														if((yCoord >= tmpYBegin) && (yCoord <= tmpYEnd))
														{
															
															//Output data to the help window if it is showing.
															if(jRacy.helpWindow.isShowing())
															{
																//Clear the window fisrt.
																jRacy.helpWindow.clearText();
																
																//Now send the help info.
																jRacy.helpWindow.writeText("Your mouse is over one of the thread draw bars!");
																jRacy.helpWindow.writeText("");
																jRacy.helpWindow.writeText("Current function name is: " + sMWThreadDataElement.getFunctionName());
																jRacy.helpWindow.writeText("");
																jRacy.helpWindow.writeText("The thread draw bars give a visual representation" +
																						  " functions which have run on this thread." +
																						  "  The funtions are assigned a color from the current" +
																						  " Racy color set.  The colors are cycled through when the" +
																						  " number of funtions exceeds the number of available" +
																						  " colors." +
																						  "  Use the right and left mouse buttons " +
															  							  "to give additional information.");
															}
															
															//Return the name of the function in the current thread data object.
															return sMWThreadDataElement.getFunctionName();
														}
														else
														{
															//We do not want to keep cycling through if we have already
															//established that we are not going to draw.
															return S;
														}
													}
													else
													{
														//Update the counter.
														sMWThreadDataElementCounter = (sMWThreadDataElementCounter + 1);
													}
											}
											//If in here, and at this position, it means that the mouse is not over
											//a bar. However, we might be over the misc. function section.  Check for this.
											if((yCoord >= tmpYBegin) && (yCoord <= tmpYEnd))
											{
												if(xCoord <= (barXStart + defaultBarLength))
												{
													//Output data to the help window if it is showing.
													if(jRacy.helpWindow.isShowing())
													{
														//Clear the window fisrt.
														jRacy.helpWindow.clearText();
														
														//Now send the help info.
														jRacy.helpWindow.writeText("Your mouse is over the misc. function section!");
														jRacy.helpWindow.writeText("");
														jRacy.helpWindow.writeText("These are functions which have a non zero value," +
																											" but whose screen representation is less than a pixel.");
														jRacy.helpWindow.writeText("");
														jRacy.helpWindow.writeText("To view these functions, right or left click to the left of" +
																					" this bar to bring up windows which will show more detailed information.");
													}
													
													//Return the name of the function in the current thread data object.
													return "Misc function section ... see help window for details";
												}
											}
											return S;
										}
										//End if statement!
									}
								}
								//End if statement!
							}
							
							//At this point, we drop out of the function, returning the default string.
							return S;
						}
						//End if statement!
					} 
				
				
				//If here, means that we are not on one of the bars and so return the default string.
				return S;
			}
			
			//If here, means that we are not on one of the bars and so return the default string.
			return S;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SMWP03");
		}
		
		return S;
	}
	
	//******************************
	//Event listener code!!
	//******************************
	
	
	//ActionListener code.
	public void actionPerformed(ActionEvent evt)
	{
		try{
			Object EventSrc = evt.getSource();
			
			SMWThreadDataElement tmpSMWThreadDataElement = null;
			SMWMeanDataElement tmpSMWMeanDataElement = null;
			
			if(EventSrc instanceof JMenuItem)
			{
				String arg = evt.getActionCommand();
				if(arg.equals("Show Function Details"))
				{
					
					if(clickedOnObject instanceof SMWThreadDataElement)
					{
						tmpSMWThreadDataElement = (SMWThreadDataElement) clickedOnObject;
						//Bring up an expanded data window for this function, and set this function as highlighted.
						jRacy.clrChooser.setHighlightColorFunctionID(tmpSMWThreadDataElement.getFunctionID());
						FunctionDataWindow tmpRef = new FunctionDataWindow(tmpSMWThreadDataElement.getFunctionName(), (sMWindow.getSMWData()));
						jRacy.systemEvents.addObserver(tmpRef);
						tmpRef.show();
					}
					else
					{
						tmpSMWMeanDataElement = (SMWMeanDataElement) clickedOnObject;
						//Bring up an expanded data window for this function, and set this function as highlighted.
						jRacy.clrChooser.setHighlightColorFunctionID(tmpSMWMeanDataElement.getFunctionID());
						FunctionDataWindow tmpRef = new FunctionDataWindow(tmpSMWMeanDataElement.getFunctionName(), (sMWindow.getSMWData()));
						jRacy.systemEvents.addObserver(tmpRef);
						tmpRef.show();
					}
				}
				else if(arg.equals("Change Function Color"))
				{	
					int functionID = -1;
					
					//Get the clicked on object.
					if(clickedOnObject instanceof SMWThreadDataElement)
						functionID = ((SMWThreadDataElement) clickedOnObject).getFunctionID();
					else
						functionID = ((SMWMeanDataElement) clickedOnObject).getFunctionID();
					
					GlobalMapping globalMappingReference = jRacy.staticSystemData.getGlobalMapping();
					GlobalMappingElement tmpGME = (GlobalMappingElement) globalMappingReference.getGlobalMappingElement(functionID);
					
					Color tmpCol = tmpGME.getFunctionColor();
					
					JColorChooser tmpJColorChooser = new JColorChooser();
					tmpCol = tmpJColorChooser.showDialog(this, "Please select a new color", tmpCol);
					if(tmpCol != null)
					{
						tmpGME.setSpecificColor(tmpCol);
						tmpGME.setColorFlag(true);
						
						jRacy.systemEvents.updateRegisteredObjects("colorEvent");
					}
				}
				
				else if(arg.equals("Reset to Generic Color"))
				{	
					
					int functionID = -1;
					
					//Get the clicked on object.
					if(clickedOnObject instanceof SMWThreadDataElement)
						functionID = ((SMWThreadDataElement) clickedOnObject).getFunctionID();
					else
						functionID = ((SMWMeanDataElement) clickedOnObject).getFunctionID();
					
					GlobalMapping globalMappingReference = jRacy.staticSystemData.getGlobalMapping();
					GlobalMappingElement tmpGME = (GlobalMappingElement) globalMappingReference.getGlobalMappingElement(functionID);
					
					tmpGME.setColorFlag(false);
					jRacy.systemEvents.updateRegisteredObjects("colorEvent");
				}
				else if(arg.equals("Highlight this Function"))
				{		
					int functionID = -1;
					
					//Get the clicked on object.
					if(clickedOnObject instanceof SMWThreadDataElement)
						functionID = ((SMWThreadDataElement) clickedOnObject).getFunctionID();
					else
						functionID = ((SMWMeanDataElement) clickedOnObject).getFunctionID();
					
					jRacy.clrChooser.setHighlightColorFunctionID(functionID);
				}
				else if(arg.equals("Un-Highlight this Function"))
				{		
					jRacy.clrChooser.setHighlightColorFunctionID(-1);
				}
			}
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SMWP04");
		}
	}
				
	
	//**********
	//Mouse listeners for this panel.
	public void mouseClicked(MouseEvent evt)
	{
		try
		{
			//Get the location of the mouse.
			xCoord = evt.getX();
			yCoord = evt.getY();
			
			//Get the number of times clicked.
			int clickCount = evt.getClickCount();
			
			//if(meanBarTest(evt, clickCount, xCoord, yCoord))
				//return;
			
			//**********
			//Reset the counters.
			serverCounter = contextCounter = threadCounter = sMWThreadDataElementCounter = 0;
			//End - Reset the counters.
			//**********
			
			//**********
			//Check to see if the click occured in the mean values bar.
			if(!((sMWindow.getSMWMeanData()).isEmpty()))
			{
				sMWMeanDataElement = (SMWMeanDataElement) (sMWindow.getSMWMeanData()).elementAt(0);
				
				if((yCoord >= sMWMeanDataElement.getYBeg()) && (yCoord <= sMWMeanDataElement.getYEnd()))
				{
					//System.out.println("We are in the mean area.");
					
					//We are inside the mean values bar.  So, cycle through the elements.
					for(Enumeration gM1 = (sMWindow.getSMWMeanData()).elements(); gM1.hasMoreElements() ;)
					{
						sMWMeanDataElement = (SMWMeanDataElement) gM1.nextElement();
						
						
						//Now we are going accross in the X direction.
						if(xCoord < barXStart)
						{
							//Bring up the thread data window for this thread object!
							if((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
							{
								MeanDataWindow tmpRef = new MeanDataWindow(sMWindow.getSMWData());
								jRacy.systemEvents.addObserver(tmpRef);
								tmpRef.show();
							}
							else
							{
								//Bring up the total stat window here!
								//System.out.println("Will bring up the total stat window for the mean.");
								MeanTotalStatWindow tmpRef = new MeanTotalStatWindow(sMWindow.getSMWData());
								
								jRacy.systemEvents.addObserver(tmpRef);
								tmpRef.show();
							}
							//Return from this function.
							return;
						}
						else if(xCoord < sMWMeanDataElement.getXEnd())
						{
							if((evt.getModifiers() & InputEvent.BUTTON1_MASK) == 0)
							{
								//Set the clickedSMWMeanDataElement.
								clickedOnObject = sMWMeanDataElement;
								popup.show(this, evt.getX(), evt.getY());
								
								//Return from this function.
								return;
							}
							else
							{
								
								jRacy.clrChooser.setHighlightColorFunctionID(sMWMeanDataElement.getFunctionID());
								FunctionDataWindow tmpRef = new FunctionDataWindow(sMWMeanDataElement.getFunctionName(), (sMWindow.getSMWData()));
								jRacy.systemEvents.addObserver(tmpRef);
								tmpRef.show();
								
								//Want to set the clicked on function to the current highlight color or, if the one
								//clicked on is already the current highlighted one, set it back to normal.
								//if((jRacy.clrChooser.getHighlightColorFunctionID()) == -1)
								//{
									//jRacy.clrChooser.setHighlightColorFunctionID(sMWMeanDataElement.getFunctionID());
								//}
								//else
								//{
									//if(!((jRacy.clrChooser.getHighlightColorFunctionID()) == (sMWMeanDataElement.getFunctionID())))
										//jRacy.clrChooser.setHighlightColorFunctionID(sMWMeanDataElement.getFunctionID());
									//else
										//jRacy.clrChooser.setHighlightColorFunctionID(-1);
								//}
							}
							
							//Return from this function.
							return;
						}			
					}
				}
			}
			//End - Check to see if the click occured in the mean values bar.
			//**********
			
			//**********
			//Check for clicking in the rest of the window.
			for(Enumeration e1 = (sMWindow.getSMWGeneralData()).elements(); e1.hasMoreElements() ;)
			{
				sMWServer = (SMWServer) e1.nextElement();
				if(yCoord <= (sMWServer.getYDrawCoord()))
				{
					//Enter the context loop for this server.
					contextList = sMWServer.getContextList();
					
					for(Enumeration e2 = contextList.elements(); e2.hasMoreElements() ;)
					{
						sMWContext = (SMWContext) e2.nextElement();
						if(yCoord <= (sMWContext.getYDrawCoord()))
						{
							//Enter the thread loop for this context.
							threadList = sMWContext.getThreadList();
							for(Enumeration e3 = threadList.elements(); e3.hasMoreElements() ;)
							{
								sMWThread = (SMWThread) e3.nextElement();
								if(yCoord <= (sMWThread.getYDrawCoord()))
								{
									//Now enter the thread loop for this thread.
									threadDataList = sMWThread.getThreadDataList();
									sMWThreadDataElementCounter = 0;
									for(Enumeration e4 = threadDataList.elements(); e4.hasMoreElements() ;)
									{
										sMWThreadDataElement = (SMWThreadDataElement) e4.nextElement();
										//Now we are going accross in the X direction.
										if(xCoord < barXStart)
										{
											//Make sure that the mouse is not above or below the bar
											//for this thread.  The y values from the first thread data
											//object will indicate this.
											if((yCoord >= sMWThreadDataElement.getYBeg()) && (yCoord <= sMWThreadDataElement.getYEnd()))
											{
													//Bring up the thread data window for this thread object!
													if((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
													{
														ThreadDataWindow tmpRef = new ThreadDataWindow(serverCounter, contextCounter,
																								   threadCounter, sMWindow.getSMWData());
																								   
																								   
														jRacy.systemEvents.addObserver(tmpRef);
														tmpRef.show();
													}
													else
													{
														TotalStatWindow tmpRef = new TotalStatWindow(serverCounter, contextCounter,
																								   threadCounter, sMWindow.getSMWData());
														jRacy.systemEvents.addObserver(tmpRef);
														tmpRef.show();
													}
											}
											//Return from this function.
											return;
										}
										else
											if(xCoord < sMWThreadDataElement.getXEnd())
											{
												
												if((yCoord >= sMWThreadDataElement.getYBeg()) && (yCoord <= sMWThreadDataElement.getYEnd()))
												{
													
													if((evt.getModifiers() & InputEvent.BUTTON1_MASK) == 0)
													{
														//Set the clickedSMWMeanDataElement.
														clickedOnObject = sMWThreadDataElement;
														popup.show(this, evt.getX(), evt.getY());
														
														//Return from this function.
														return;
													}
													else
													{
														jRacy.clrChooser.setHighlightColorFunctionID(sMWThreadDataElement.getFunctionID());
														FunctionDataWindow tmpRef = new FunctionDataWindow(sMWThreadDataElement.getFunctionName(), (sMWindow.getSMWData()));
														jRacy.systemEvents.addObserver(tmpRef);
														tmpRef.show();
														
														//Want to set the clicked on function to the current highlight color or, if the one
														//clicked on is already the current highlighted one, set it back to normal.
														//if((jRacy.clrChooser.getHighlightColorFunctionID()) == -1)
														//{
															//jRacy.clrChooser.setHighlightColorFunctionID(sMWThreadDataElement.getFunctionID());
														//}
														//else
														//{
															//if(!((jRacy.clrChooser.getHighlightColorFunctionID()) == (sMWThreadDataElement.getFunctionID())))
																//jRacy.clrChooser.setHighlightColorFunctionID(sMWThreadDataElement.getFunctionID());
															//else
																//jRacy.clrChooser.setHighlightColorFunctionID(-1);
														//}
													}
													
													//Return from this function.
													return;
												}
											}
											else
											{
												//Update the counter.
												sMWThreadDataElementCounter = (sMWThreadDataElementCounter + 1);
											}
									}
								}
								//End if statement!
								
								//Update the thread counter.
								threadCounter++;
							}
						}
						//End if statement!
						
						//Update the context counter.
						contextCounter++;
					}
				}
				//End if statement!
				
				//Update the server counter!
				serverCounter++;
			}
			//End - Check for clicking in the rest of the window.
			//**********
		}	
		catch(Exception e)
		{
			jRacy.systemError(null, "SMWP05");
		}
	}
	public void mousePressed(MouseEvent evt){}
	public void mouseReleased(MouseEvent evt){}
	public void mouseEntered(MouseEvent evt){}
	public void mouseExited(MouseEvent evt){}
	//End - Mouse listeners for this panel.
	//**********
	
	
	
	
	public void paintComponent(Graphics g)
	{
		try
		{
			super.paintComponent(g);
			
			//Set the numberOfColors variable.
			numberOfColors = jRacy.clrChooser.getNumberOfColors(); 
			
			if(sMWindow.isDataLoaded())
			{	
				//**********
				//Reset the counters.
				serverCounter = contextCounter = threadCounter = sMWThreadDataElementCounter = colorCounter = 0;
				//End - Reset the counters.
				//**********
				
				//**********
				//Other initializations.
				highlighted = false;
				xCoord = yCoord = 0;
				//End - Other initializations.
				//**********
				
				//**********
				//Do the standard font and spacing stuff.
				if(!(jRacy.jRacyPreferences.areBarDetailsSet()))
				{
					
					//Create font.
					Font font = new Font(jRacy.jRacyPreferences.getJRacyFont(), jRacy.jRacyPreferences.getFontStyle(), 12);
					g.setFont(font);
					FontMetrics fmFont = g.getFontMetrics(font);
					
					//Set up the bar details.
					
					//Compute the font metrics.
					int maxFontAscent = fmFont.getAscent();
					int maxFontDescent = fmFont.getMaxDescent();
					
					int tmpInt = maxFontAscent + maxFontDescent;
					
					jRacy.jRacyPreferences.setBarDetails(maxFontAscent, (tmpInt + 5));
					
					jRacy.jRacyPreferences.setSliders(maxFontAscent, (tmpInt + 5));
				}
				//End - Do the standard font and spacing stuff.
				//**********
				
				//Set local spacing and bar heights.
				int barSpacing = jRacy.jRacyPreferences.getBarSpacing();
				int barHeight = jRacy.jRacyPreferences.getBarHeight();
				
				//Create font.
				Font font = new Font(jRacy.jRacyPreferences.getJRacyFont(), jRacy.jRacyPreferences.getFontStyle(), barHeight);
				g.setFont(font);
				FontMetrics fmFont = g.getFontMetrics(font);
				
				
				//**********
				//Calculating the starting positions of drawing.
				String tmpString2 = new String("n,c,t 99,99,99");
				int stringWidth = fmFont.stringWidth(tmpString2);
				barXStart = stringWidth + 15;
				int tmpXWidthCalc = barXStart + defaultBarLength;
				int barXCoord = barXStart;
				yCoord = yCoord + (barSpacing);
				//End - Calculating the starting positions of drawing.
				//**********
				
				//**********
				//Draw the counter name if required.
				counterName = jRacy.staticSystemData.getCounterName();
				if(counterName != null){
					g.drawString("COUNTER NAME: " + counterName, 5, yCoord);
					yCoord = yCoord + (barSpacing);
				}
				//End - Draw the counter name if required.
				//**********
				
				yCoord = yCoord + (barSpacing);
				
				//**********
				//Drawing the mean bar.
				String meanString = "Mean";
				int tmpMeanStringWidth = fmFont.stringWidth(meanString);
				g.drawString(meanString, (barXStart - tmpMeanStringWidth - 5), yCoord);
				
				//Now draw the bar of values.
				
				//Cycle through the mean data values to get the total.
				tmpSum = 0.0;
				for(Enumeration gM1 = (sMWindow.getSMWMeanData()).elements(); gM1.hasMoreElements() ;)
				{
					sMWMeanDataElement = (SMWMeanDataElement) gM1.nextElement();
					tmpSum = tmpSum + (sMWMeanDataElement.getValue());
				}
				
				//Now that we have the total, can begin drawing.
				colorCounter = 0;
				barXCoord = barXStart;
				for(Enumeration gM2 = (sMWindow.getSMWMeanData()).elements(); gM2.hasMoreElements() ;)
				{
					sMWMeanDataElement = (SMWMeanDataElement) gM2.nextElement();
					
					tmpDataValue = sMWMeanDataElement.getValue();
					
					if(tmpDataValue > 0.0)		//Don't want to draw a bar if the value is zero.
					{
						//Now compute the length of the bar for this object.
						//The default length for the bar shall be 200.
						int xLength;
						double tmpDouble;
						tmpDouble = (tmpDataValue / tmpSum);
						xLength = (int) (tmpDouble * defaultBarLength);
						
						if(xLength > 2) 	//Only draw if there is something to draw.
						{		
							if(barHeight > 2)
							{
								tmpColor = sMWMeanDataElement.getFunctionColor();
								g.setColor(tmpColor);
								g.fillRect(barXCoord + 1, (yCoord - barHeight) + 1, xLength - 1, barHeight - 1);
								
								if((sMWMeanDataElement.getFunctionID()) == (jRacy.clrChooser.getHighlightColorFunctionID()))
								{	
									highlighted = true;
									g.setColor(jRacy.clrChooser.getHighlightColor());
									g.drawRect(barXCoord, (yCoord - barHeight), xLength, barHeight);
									g.drawRect(barXCoord + 1, (yCoord - barHeight) + 1, xLength - 2, barHeight - 2);
								}
								else
								{
									g.setColor(Color.black);
									if(highlighted)
									{
										//Manually draw in the lines for consistancy.
										g.drawLine(barXCoord + 1, (yCoord - barHeight), barXCoord + 1 + xLength, (yCoord - barHeight));
										g.drawLine(barXCoord + 1, yCoord, barXCoord + 1 + xLength, yCoord);
										g.drawLine(barXCoord + 1 + xLength, (yCoord - barHeight), barXCoord + 1 + xLength, yCoord);
										
										//g.drawRect(barXCoord + 1, (yCoord - barHeight), xLength, barHeight);
										highlighted = false;
									}
									else
									{
										g.drawRect(barXCoord, (yCoord - barHeight), xLength, barHeight);
									}
								}
								
								//Set the draw coords.
								sMWMeanDataElement.setDrawCoords(barXCoord, (barXCoord + xLength), (yCoord - barHeight), yCoord);
								
								//Update barXCoord.
								barXCoord = (barXCoord + xLength);
							}
							else
							{
								//Now set the color values for drawing!
								//Get the appropriate color.
								if((jRacy.clrChooser.getHighlightColorFunctionID()) != -1)
								{
									if((sMWMeanDataElement.getFunctionID()) == (jRacy.clrChooser.getHighlightColorFunctionID()))
										g.setColor(jRacy.clrChooser.getHighlightColor());
									else
									{
										tmpColor = sMWMeanDataElement.getFunctionColor();
										g.setColor(tmpColor);
									}
								}
								else
								{
									tmpColor = sMWMeanDataElement.getFunctionColor();
									g.setColor(tmpColor);
								}
								g.fillRect(barXCoord, (yCoord - barHeight), xLength, barHeight);
								g.setColor(Color.black);
								g.drawRect(barXCoord, (yCoord - barHeight), xLength, barHeight);
								
								//Set the draw coords.
								sMWMeanDataElement.setDrawCoords(barXCoord, (barXCoord + xLength), (yCoord - barHeight), yCoord);
								
								//Update barXCoord.
								barXCoord = (barXCoord + xLength);
							}
						}
						
						//Still want to set the draw coords for this function, were it to be none zero.
						//This aids in mouse click and tool tip events.
						sMWMeanDataElement.setDrawCoords(barXCoord, barXCoord, (yCoord - barHeight), yCoord);
						
					}
					else
					{
						//Still want to set the draw coords for this function, were it to be none zero.
						//This aids in mouse click and tool tip events.
						sMWMeanDataElement.setDrawCoords(barXCoord, barXCoord, (yCoord - barHeight), yCoord);
					}
						
						
					
					colorCounter = (colorCounter + 1) % numberOfColors;		//Want to cycle to the next color
																//whether we have drawn or not.
																
				}
				
				//We have reached the end of the cycle for this thread.  However, we might be less
				//than the max length of the bar.  Therefore, fill in the rest of the bar with the
				//misc. function colour.
				if(barXCoord < (defaultBarLength + barXStart))
				{
					g.setColor(jRacy.clrChooser.getMiscFunctionsColor());
					g.fillRect(barXCoord, (yCoord - barHeight), ((defaultBarLength + barXStart) - barXCoord), barHeight);
					g.setColor(Color.black);
					g.drawRect(barXCoord, (yCoord - barHeight), ((defaultBarLength + barXStart) - barXCoord), barHeight);
				}
				//End - Drawing the mean bar.
				//**********
					
				
				//Set the drawing color to the text color ... in this case, black.
				g.setColor(Color.black);
				
				
				
				
				//**********
				//Draw the thread data bars.
				
				//Setting the server counter to zero ... not that it is really required.
				serverCounter = 0;
				for(Enumeration e1 = (sMWindow.getSMWGeneralData()).elements(); e1.hasMoreElements() ;)
				{
					//Get the next server.
					sMWServer = (SMWServer) e1.nextElement();
					
					//Get the context list for this server.
					contextList = sMWServer.getContextList();
					
					//Setting the context counter to zero ... this is really required.
					contextCounter = 0;
					for(Enumeration e2 = contextList.elements(); e2.hasMoreElements() ;)
					{
						//Get the next context.
						sMWContext = (SMWContext) e2.nextElement();
						
						//Get the thread list for this context.
						threadList = sMWContext.getThreadList();
						
						//Setting the context counter to zero ... this is really required as well. :-)
						threadCounter = 0;
						for(Enumeration e3 = threadList.elements(); e3.hasMoreElements() ;)
						{
							//Reset the highlighted boolean.
							highlighted = false;
							
							//For consistancy in drawing, the y coord is updated at the beggining of the loop.
							yCoord = yCoord + (barSpacing);
							
							//Get the current thread object.
							sMWThread = (SMWThread) e3.nextElement();
							
							//Draw the n,c,t string to the left of the bar start position.
							String s1 = "n,c,t   " + serverCounter + "," + contextCounter + "," + threadCounter;
							int tmpStringWidth = fmFont.stringWidth(s1);
							g.drawString(s1, (barXStart - tmpStringWidth - 5), yCoord);
							
							//Now, at last, draw some data.getThreadDataList()
							threadDataList = sMWThread.getThreadDataList();
							//Cycle through the data values for this thread to get the total.
							tmpSum = 0.00;
							for(Enumeration e4 = threadDataList.elements(); e4.hasMoreElements() ;)
							{
								sMWThreadDataElement = (SMWThreadDataElement) e4.nextElement();
								tmpSum = tmpSum + (sMWThreadDataElement.getValue());
							}
							//Now that we have the total, can begin drawing.
							colorCounter = 0;
							barXCoord = barXStart;
							for(Enumeration e5 = threadDataList.elements(); e5.hasMoreElements() ;)
							{
								sMWThreadDataElement = (SMWThreadDataElement) e5.nextElement();
								tmpDataValue = sMWThreadDataElement.getValue();
								
								if(tmpDataValue > 0.0)		//Don't want to draw a bar if the value is zero.
								{
									//Now compute the length of the bar for this object.
									//The default length for the bar shall be 200.
									int xLength;
									double tmpDouble;
									tmpDouble = (tmpDataValue / tmpSum);
									xLength = (int) (tmpDouble * defaultBarLength);
									if(xLength > 2) 	//Only draw if there is something to draw.
									{		
										if(barHeight > 2)
										{
											tmpColor = sMWThreadDataElement.getFunctionColor();
											g.setColor(tmpColor);
											g.fillRect(barXCoord + 1, (yCoord - barHeight) + 1, xLength - 1, barHeight - 1);
											
											if((sMWThreadDataElement.getFunctionID()) == (jRacy.clrChooser.getHighlightColorFunctionID()))
											{	
												highlighted = true;
												g.setColor(jRacy.clrChooser.getHighlightColor());
												g.drawRect(barXCoord, (yCoord - barHeight), xLength, barHeight);
												g.drawRect(barXCoord + 1, (yCoord - barHeight) + 1, xLength - 2, barHeight - 2);
											}
											else
											{
												g.setColor(Color.black);
												if(highlighted)
												{
													//Manually draw in the lines for consistancy.
													g.drawLine(barXCoord + 1, (yCoord - barHeight), barXCoord + 1 + xLength, (yCoord - barHeight));
													g.drawLine(barXCoord + 1, yCoord, barXCoord + 1 + xLength, yCoord);
													g.drawLine(barXCoord + 1 + xLength, (yCoord - barHeight), barXCoord + 1 + xLength, yCoord);
													
													//g.drawRect(barXCoord + 1, (yCoord - barHeight), xLength, barHeight);
													highlighted = false;
												}
												else
												{
													g.drawRect(barXCoord, (yCoord - barHeight), xLength, barHeight);
												}
											}
											
											//Set the draw coords.
											sMWThreadDataElement.setDrawCoords(barXCoord, (barXCoord + xLength), (yCoord - barHeight), yCoord);
											
											//Update barXCoord.
											barXCoord = (barXCoord + xLength);
										}
										else
										{
											//Now set the color values for drawing!
											//Get the appropriate color.
											if((jRacy.clrChooser.getHighlightColorFunctionID()) != -1)
											{
												if((sMWThreadDataElement.getFunctionID()) == (jRacy.clrChooser.getHighlightColorFunctionID()))
													g.setColor(jRacy.clrChooser.getHighlightColor());
												else
												{
													tmpColor = sMWThreadDataElement.getFunctionColor();
													g.setColor(tmpColor);
												}
											}
											else
											{
												tmpColor = sMWThreadDataElement.getFunctionColor();
												g.setColor(tmpColor);
											}
											g.fillRect(barXCoord, (yCoord - barHeight), xLength, barHeight);
											g.setColor(Color.black);
											g.drawRect(barXCoord, (yCoord - barHeight), xLength, barHeight);
											
											//Set the draw coords.
											sMWThreadDataElement.setDrawCoords(barXCoord, (barXCoord + xLength), (yCoord - barHeight), yCoord);
											
											//Update barXCoord.
											barXCoord = (barXCoord + xLength);
										}
											
									}
									
									//Still want to set the draw coords for this function, were it to be none zero.
									//This aids in mouse click and tool tip events.
									sMWThreadDataElement.setDrawCoords(barXCoord, barXCoord, (yCoord - barHeight), yCoord);
									
								}
								else
								{
									//Still want to set the draw coords for this function, were it to be none zero.
									//This aids in mouse click and tool tip events.
									sMWThreadDataElement.setDrawCoords(barXCoord, barXCoord, (yCoord - barHeight), yCoord);
								}
									
									
								
								colorCounter = (colorCounter + 1) % numberOfColors;		//Want to cycle to the next color
																			//whether we have drawn or not.
																			
							}
							
							//We have reached the end of the cycle for this thread.  However, we might be less
							//than the max length of the bar.  Therefore, fill in the rest of the bar with the
							//misc. function colour.
							if(barXCoord < (defaultBarLength + barXStart))
							{
								g.setColor(jRacy.clrChooser.getMiscFunctionsColor());
								g.fillRect(barXCoord, (yCoord - barHeight), ((defaultBarLength + barXStart) - barXCoord), barHeight);
								g.setColor(Color.black);
								g.drawRect(barXCoord, (yCoord - barHeight), ((defaultBarLength + barXStart) - barXCoord), barHeight);
							}
							
							//Reset the drawing color to the text color ... in this case, black.
							g.setColor(Color.black);
							
							//We are about to move on to drawing the next thread.  Thus, record the
							//max y draw value for this thread.
							sMWThread.setYDrawCoord(yCoord);
							
							//Update the thread counter.
							threadCounter++;
							
						}
						//We are about to move on to drawing the next context.  Thus, record the
						//max y draw value for this context.
						sMWContext.setYDrawCoord(yCoord);
						
						//Update the context counter.
						contextCounter++;
					}
					
					//We are about to move on to drawing the next server.  Thus, record the
					//max y draw value for this server.
					sMWServer.setYDrawCoord(yCoord);
					
					//Update the server counter.
					serverCounter++;
				}
				
				
				boolean sizeChange = false;		
				//Resize the panel if needed.
				if(tmpXWidthCalc > 600){
					xPanelSize = tmpXWidthCalc + 1;
					sizeChange = true;
				}
				
				if(yCoord > 300){
					yPanelSize = yCoord + 1;
					sizeChange = true;
				}
				
				if(sizeChange)
					revalidate();
				
				/*//Resize the panel if needed.
				if((yCoord >= yPanelSize)  || (tmpXWidthCalc  >= xPanelSize))
				{
					xPanelSize = tmpXWidthCalc + 1;
					yPanelSize = yCoord + 1;
					revalidate();
				}
				*/
			}
			//End - Draw the thread data bars.
			//**********
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SMWP06");
		}
	}
	
	public void changeInMultiples()
	{
		computeDefaultBarLength();
		this.repaint();
	}
	
	public Dimension getPreferredSize()
	{
		return new Dimension(xPanelSize + 10, (yPanelSize + 10));
	}
	
	public void computeDefaultBarLength()
	{
		try
		{
			double sliderValue = (double) sMWindow.getSliderValue();
			double sliderMultiple = sMWindow.getSliderMultiple();
			double result = 500*sliderValue*sliderMultiple;
			
			defaultBarLength = (int) result;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SMWP07");
		}
	}
	
}


