
//FunctionLedgerWindowPanel.

/* 
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


public class FunctionLedgerWindowPanel extends JPanel implements ActionListener, MouseListener
{
	int xPanelSize = 300;
	int yPanelSize = 400;
	
	int barHeight;
	int barSpacing;
	
	
	public FunctionLedgerWindowPanel()
	{
		try{
			setSize(new java.awt.Dimension(xPanelSize, yPanelSize));
			
			//Schedule a repaint of this panel.
			this.repaint();
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "FLWP01");
		}
	
	}
	
	public FunctionLedgerWindowPanel(Vector inNameIDMapping)
	{
		try{
			setSize(new java.awt.Dimension(xPanelSize, yPanelSize));
			setBackground(Color.white);
			
			NameIDMapping = inNameIDMapping;
			
			//Add this object as a mouse listener.
			addMouseListener(this);
			
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
			
			//JMenuItem toGenericColorItem = new JMenuItem("Mask Function");
			//toGenericColorItem.addActionListener(this);
			//popup.add(toGenericColorItem);
			
			
			//Schedule a repaint of this panel.
			this.repaint();
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "FLWP02");
		}
		
		
	
	}
	

	public void paintComponent(Graphics g)
	{
		try{
			super.paintComponent(g);
			
			//Set the numberOfColors variable.
			numberOfColors = jRacy.clrChooser.getNumberOfColors();
			
			//Cycle through the id mapping list.
			GlobalMappingElement globalMappingElement = null;
			String tmpString;
			Color tmpColor;
		
			//Note that trying to set the coords of the top left corner of this componant
			//caused problems with redrawing.  Specifically, with the scroll pane, negative
			//values were sometimes returned.  So,  I am just starting at 0,0.  It would
			//be nice to know why this was occuring!
			int xCoord = 0;
			int yCoord = 0;
			
			int nWidth = 0;
			int cWidth = 0;
			int tWidth = 0;
			
			//An XCoord used in drawing the bar graphs.
			int barXCoord = 0;
			
			int tmpXWidthCalc = 0;
		
			xCoord = 5;
			
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
				
				jRacy.jRacyPreferences.setBarDetails(maxFontAscent, maxFontAscent);
				
				jRacy.jRacyPreferences.setSliders(maxFontAscent, maxFontAscent);
			}
			
			//Set local spacing and bar heights.
			barSpacing = jRacy.jRacyPreferences.getBarSpacing();
			barHeight = jRacy.jRacyPreferences.getBarHeight();
			
			//Create font.
			Font font = new Font(jRacy.jRacyPreferences.getJRacyFont(), jRacy.jRacyPreferences.getFontStyle(), barHeight);
			g.setFont(font);
			FontMetrics fmFont = g.getFontMetrics(font);
		
			yCoord = yCoord + (barSpacing);
			
			//**********
			//Draw the counter name if required.
			counterName = jRacy.staticSystemData.getCounterName();
			if(counterName != null){
				g.drawString("COUNTER NAME: " + counterName, 5, yCoord);
				yCoord = yCoord + (barSpacing);
			}
			//End - Draw the counter name if required.
			//**********
		
			int tmpCounter = 0;
			
			for(Enumeration e1 = NameIDMapping.elements(); e1.hasMoreElements() ;)
			{
				globalMappingElement = (GlobalMappingElement) e1.nextElement();
				if(!((globalMappingElement.getFunctionName()).equals("Error ... the function name has not been set!")))
				{
				
						//For consistancy in drawing, the y coord is updated at the beggining of the loop.
						yCoord = yCoord + (barSpacing);
						
						//First draw the function color box.
						tmpColor = globalMappingElement.getFunctionColor();
						g.setColor(tmpColor);
						
						g.fillRect(xCoord, (yCoord - barHeight), barHeight, barHeight);
						
						if((globalMappingElement.getGlobalID()) == (jRacy.clrChooser.getHighlightColorFunctionID()))
						{
							g.setColor(jRacy.clrChooser.getHighlightColor());
							g.drawRect(xCoord, (yCoord - barHeight), barHeight, barHeight);
							g.drawRect(xCoord + 1, (yCoord - barHeight) + 1, barHeight - 2, barHeight - 2);
						}
						else
						{
							g.setColor(Color.black);
							g.drawRect(xCoord, (yCoord - barHeight), barHeight, barHeight);
						}
						
						//Update the xCoord to draw the function name.
						xCoord = xCoord + (barHeight + 10);
						//Reset the drawing color to the text color ... in this case, black.
						g.setColor(Color.black);
						
						//Draw the function name.
						tmpString = globalMappingElement.getFunctionName();
						
						g.drawString(tmpString, xCoord, yCoord);
						
						//Figure out how wide that string was for x coord reasons.
						int tmpWidth = 5 + barHeight + (fmFont.stringWidth(tmpString));
						
						//Figure out how wide that string was for x coord reasons.
						if(tmpXWidthCalc < tmpWidth)
						{
							tmpXWidthCalc = (tmpWidth + 11);
						}
						
						globalMappingElement.setDrawCoords(0, tmpWidth, (yCoord - barHeight), yCoord);
						
						//Reset the xCoord.
						xCoord = xCoord - (barHeight + 10);
				}
						
												
				tmpCounter++;
																		

			}
			//Resize the panel if needed.
			if((yCoord >= yPanelSize) || (tmpXWidthCalc >= xPanelSize))
			{
				yPanelSize = yCoord + 1;
				xPanelSize = tmpXWidthCalc + 1;
				
				revalidate();
			}
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "FLWP03");
		}
		
	}
	
	//ActionListener code.
	public void actionPerformed(ActionEvent evt)
	{
		try{
			Object EventSrc = evt.getSource();
			
			GlobalMappingElement tmpGlobalMappingElement = null;
			
			if(EventSrc instanceof JMenuItem)
			{
				String arg = evt.getActionCommand();
				if(arg.equals("Show Function Details"))
				{
					
					if(clickedOnObject instanceof GlobalMappingElement)
					{
						tmpGlobalMappingElement = (GlobalMappingElement) clickedOnObject;
						//Bring up an expanded data window for this function, and set this function as highlighted.
						jRacy.clrChooser.setHighlightColorFunctionID(tmpGlobalMappingElement.getGlobalID());
						FunctionDataWindow tmpRef = new FunctionDataWindow(tmpGlobalMappingElement.getFunctionName(), jRacy.staticMainWindow.getSMWData());
						jRacy.systemEvents.addObserver(tmpRef);
						tmpRef.show();
					}
				}
				else if(arg.equals("Change Function Color"))
				{	
					if(clickedOnObject instanceof GlobalMappingElement)
						tmpGlobalMappingElement = (GlobalMappingElement) clickedOnObject;
					
					Color tmpCol = tmpGlobalMappingElement.getFunctionColor();
					
					JColorChooser tmpJColorChooser = new JColorChooser();
					tmpCol = tmpJColorChooser.showDialog(this, "Please select a new color", tmpCol);
					if(tmpCol != null)
					{
						tmpGlobalMappingElement.setSpecificColor(tmpCol);
						tmpGlobalMappingElement.setColorFlag(true);
						
						jRacy.systemEvents.updateRegisteredObjects("colorEvent");
					}
				}
				
				else if(arg.equals("Reset to Generic Color"))
				{	
					
					if(clickedOnObject instanceof GlobalMappingElement)
						tmpGlobalMappingElement = (GlobalMappingElement) clickedOnObject;
					
					tmpGlobalMappingElement.setColorFlag(false);
					
					jRacy.systemEvents.updateRegisteredObjects("colorEvent");
				}
			}
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "FLWP04");
		}
	}
	
	//Ok, now the mouse listeners for this panel.
	public void mouseClicked(MouseEvent evt)
	{
		try{
			//Get the location of the mouse.
			int xCoord = evt.getX();
			int yCoord = evt.getY();
			
			//Get the number of times clicked.
			int clickCount = evt.getClickCount();
			
			//Cycle through the id mapping list.
			GlobalMappingElement globalMappingElement = null;
			
			for(Enumeration e1 = NameIDMapping.elements(); e1.hasMoreElements() ;)
			{
				globalMappingElement = (GlobalMappingElement) e1.nextElement();
									
				if(yCoord <= (globalMappingElement.getYEnd()))
				{
					if((yCoord >= (globalMappingElement.getYBeg())) && (xCoord >= (globalMappingElement.getXBeg()))
																		  && (xCoord <= (globalMappingElement.getXEnd())))
					{
						if((evt.getModifiers() & InputEvent.BUTTON1_MASK) == 0)
						{
							//Set the clickedSMWMeanDataElement.
							clickedOnObject = globalMappingElement;
							popup.show(this, evt.getX(), evt.getY());
							
							//Return from this function.
							return;
						}
						else
						{
							//Want to set the clicked on function to the current highlight color or, if the one
							//clicked on is already the current highlighted one, set it back to normal.
							if((jRacy.clrChooser.getHighlightColorFunctionID()) == -1)
							{
								jRacy.clrChooser.setHighlightColorFunctionID(globalMappingElement.getGlobalID());
							}
							else
							{
								if(!((jRacy.clrChooser.getHighlightColorFunctionID()) == (globalMappingElement.getGlobalID())))
									jRacy.clrChooser.setHighlightColorFunctionID(globalMappingElement.getGlobalID());
								else
									jRacy.clrChooser.setHighlightColorFunctionID(-1);
							}
						}
						//Nothing more to do ... return.
						return;
					}
					else
					{
						//If we get here, it means that we are outside the function draw area.  That is, we
						//are either to the left or right of the draw area, or just above it.
						//It is better to return here as we do not want the system to cycle through the
						//rest of the objects, which would be pointless as we know that it will not be
						//one of the others.  Significantly improves performance.
						return;
					}
				}
			}
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "FLWP05");
		}
	}
	
	public void mousePressed(MouseEvent evt) {}
	public void mouseReleased(MouseEvent evt) {}
	public void mouseEntered(MouseEvent evt) {}
	public void mouseExited(MouseEvent evt) {}
	
	
	public Dimension getPreferredSize()
	{
		return new Dimension((xPanelSize + 10), (yPanelSize + 10));
	}
	
	
	
	//******************************
	//Instance data.
	//******************************
	Vector NameIDMapping;
	int numberOfColors = 0;
	
	//**********
	//Popup menu definitions.
	private JPopupMenu popup = new JPopupMenu();
	//**********
 	
 	//**********
	//Other useful variables.
	String counterName = null;
	Object clickedOnObject = null;
	//End - Other useful variables.
	//**********
		
	
	//******************************
	//End - Instance data.
	//******************************
}