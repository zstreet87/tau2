/*
	StaticSystemData.java
	
	
	Title:			jRacy
	Author:			Robert Bell
	Description:	This class is the heart of Racy's static data system.
					This class is rather an ongoing project.  Much work needs
					to be done with respect to data format.
					The use of tokenizers here could impact the performance
					with large data sets, but for now, this will be sufficient.
					The naming and creation of the tokenizers has been done mainly
					to improve the readability of the code.
					
					It must also be noted that the correct funtioning of this
					class is heavily dependent on the format of the pprof -d format.
					It is NOT resistant to change in that format at all.
*/

package jRacy;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

public class StaticSystemData implements Serializable
{
	//Constructor.
	public StaticSystemData()
	{
		globalMapping = new GlobalMapping();
		StaticServerList = new Vector();
		positionOfName = -1;
		positionOfUserEventName = -1;
		counterName = null;
		heading = null;
		userEventHeading = null;
		isUserEventHeadingSet = false;
	}
	
	public Vector getStaticServerList()
	{
		return StaticServerList;
	}
	
	//The following funtion initializes the GlobalMapping object.
	//Since we are in the static mode, the number of mappings is known,
	//therefore, the appropriate number of GlobalMappingElements are created.
	void initializeGlobalMapping(int inNumberOfMappings, int mappingSelection)
	{
		for(int i=0; i<inNumberOfMappings; i++)
		{
			//globalMapping.addGlobalMapping("Error ... the mapping name has not been set!");
			globalMapping.addGlobalMapping(null, mappingSelection);
		}
	}
	
	//Rest of the public functions.
	GlobalMapping getGlobalMapping()
	{
		return globalMapping;
	}
	
	public String getCounterName()
	{
		return counterName;
	}
	
	//The core public function of this class.  It reads pprof dump files ... that is pprof
	//run with the -d option.  If any changes occur to the "pprof -d" file output format,
	//the working of this function might be affected.
	public void buildStaticData(File inFile)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(inFile));
			
			//Some useful strings.
			String inputString;
			String tokenString;
			String mappingNameString = null;
			String groupNamesString = null;
			String userEventNameString = null;
			
			StringTokenizer genericTokenizer;
			
			int mappingID = -1;
			int userEventID = -1;
			double value = -1;
			double percentValue = -1;
			int node = -1;
			int context = -1;
			int thread = -1;
			int numberOfMappings = -1;
			int numberOfUserEvents = -1;
			
			GlobalMappingElement tmpGlobalMappingElement;
			
			GlobalServer currentGlobalServer = null;
			GlobalContext currentGlobalContext = null;
			GlobalThread currentGlobalThread = null;
			GlobalThreadDataElement tmpGlobalThreadDataElement = null;
			
			int lastNode = -1;
			int lastContext = -1;
			int lastThread = -1;
			
			int counter = 0;
			
			
			//A loop counter.
			bSDCounter = 0;
			
			//Another one.
			int i=0;
			int maxID = 0;
			//Read in the file line by line!
			while((inputString = br.readLine()) != null)
			{	
				//Skip over processing the first line ... not needed.
				if(bSDCounter>0)
				{
					//Set up some tokenizers.
					//I want one that will first parse the line so that I can look for certian words.
					genericTokenizer = new StringTokenizer(inputString, " \t\n\r");
					
					//Now I want to search for the tokens which interest me.
					if(!(bSDCounter==1))
					{	
						//Now, skip line three!
						if(!(bSDCounter==2))
						{
							//A lot of work goes on in this section.  Certain lines are searched for,
							//and then action is taken depending on those lines.
							
							
							//Check to See if the String begins with a t.
							if(checkForBeginningT(inputString))
							{
								counter++;
								if(checkForExclusiveWithTOrM(inputString))
								{	
									//Grab the mapping name.
									mappingNameString = getMappingName(inputString);
									
									//Grab the mapping ID.
									mappingID = getMappingID(inputString);
									if(mappingID > maxID)
										maxID = mappingID;
										
									//Grab the group names.
									groupNamesString = getGroupNames(inputString);
									if(groupNamesString != null){
										StringTokenizer st = new StringTokenizer(groupNamesString, " |");
									    while (st.hasMoreTokens()){
									         String tmpString = st.nextToken();
									         if(tmpString != null){
									         	//The potential new group is added here.  If the group is already present, the the addGlobalMapping
									         	//function will just return the already existing group id.  See the GlobalMapping class for more details.
									         	int tmpInt = globalMapping.addGlobalMapping(tmpString, 1);
									         	//The group is either already present, or has just been added in the above line.  Now, using the addGroup
									         	//function, update this mapping to be a member of this group.
									         	globalMapping.addGroup(mappingID, tmpInt, 0);
									         	if((tmpInt != -1) && (jRacy.debugIsOn))
									         		System.out.println("Adding " + tmpString + " group with id: " + tmpInt + " to mapping: " + mappingNameString);
									      	 }   	
										}    
									}
									
									//Now that we have the mapping name and id, fill in the global mapping element
									//for this mapping.  I am assuming here that pprof's output lists only the
									//global ids.
									if(!(globalMapping.setMappingNameAt(mappingNameString, mappingID, 0)))
										System.out.println("There was an error adding mapping to the global mapping");
									
									//Grab the value.
									value = getValue(inputString);
									
									
									//Set the value for this mapping.
									if(!(globalMapping.setTotalExclusiveValueAt(value, mappingID, 0)))
										System.out.println("There was an error setting Exc/Inc total time");
									
									
								}
								else if(checkForInclusiveWithTOrM(inputString))
								{
									//Grab the mapping ID.
									mappingID = getMappingID(inputString);
									//Grab the value.
									value = getValue(inputString);
									//Set the value for this mapping.
									if(!(globalMapping.setTotalInclusiveValueAt(value, mappingID, 0)))
										System.out.println("There was an error setting Exc/Inc total time");
								}
							} //End - Check to See if the String begins with a t.
							//Check to See if the String begins with a mt.
							else if(checkForBeginningM(inputString))
							{
								if(checkForExclusiveWithTOrM(inputString))
								{
									//Grab the mapping ID.
									mappingID = getMappingID(inputString);
									//Grab the value.
									value = getValue(inputString);
									percentValue = getPercentValue(inputString);
									
									//Grab the correct global mapping element.
									tmpGlobalMappingElement = globalMapping.getGlobalMappingElement(mappingID, 0);
									
									//Now set the values correctly.
									if(maxMeanExclusiveValue < value)
									{
										maxMeanExclusiveValue = value;
									}
									
									if(maxMeanExclusivePercentValue < percentValue)
									{
										maxMeanExclusivePercentValue = percentValue;
									}
									
									tmpGlobalMappingElement.setMeanExclusiveValue(value);
									tmpGlobalMappingElement.setMeanExclusivePercentValue(percentValue);
								}
								else if(checkForInclusiveWithTOrM(inputString))
								{
									//Grab the mapping ID.
									mappingID = getMappingID(inputString);
									//Grab the value.
									value = getValue(inputString);
									percentValue = getPercentValue(inputString);
									
									//Grab the correct global mapping element.
									tmpGlobalMappingElement = globalMapping.getGlobalMappingElement(mappingID, 0);
									
									//Now set the values correctly.
									if(maxMeanInclusiveValue < value)
									{
										maxMeanInclusiveValue = value;
									}
									
									if(maxMeanInclusivePercentValue < percentValue)
									{
										maxMeanInclusivePercentValue = percentValue;
									}
									
									tmpGlobalMappingElement.setMeanInclusiveValue(value);
									tmpGlobalMappingElement.setMeanInclusivePercentValue(percentValue);
									
									//Set the total stat string.
									//The next string in the file should be the correct one.  Assume it.
									//If the file format changes, we are all screwed anyway.
									inputString = br.readLine();
									//Set the total stat string.
									
									double numberOfCalls = getNumberOfCalls(inputString);
									double numberOfSubRoutines = getNumberOfSubRoutines(inputString);
									
									//Now set the values correctly.
									if(maxMeanNumberOfCalls < numberOfCalls)
									{
										maxMeanNumberOfCalls = numberOfCalls;
									}
									
									if(maxMeanNumberOfSubRoutines < numberOfSubRoutines)
									{
										maxMeanNumberOfSubRoutines = numberOfSubRoutines;
									}
										
									tmpGlobalMappingElement.setMeanNumberOfCalls(numberOfCalls);
									tmpGlobalMappingElement.setMeanNumberOfSubRoutines(numberOfSubRoutines);
									
									tmpGlobalMappingElement.setMeanTotalStatString(inputString);
									tmpGlobalMappingElement.setMeanValuesSet(true);
									//Now extract the other info from this string.
								}
							}//End - Check to See if the String begins with a m.
							//String does not begin with either an m or a t, the rest of the checks go here.
							else
							{
								if(checkForExclusive(inputString))
								{
									//Grab the mapping ID.
									mappingID = getMappingID(inputString);
									//Grab the value.
									value = getValue(inputString);
									percentValue = getPercentValue(inputString);
									
									//Update the max values if required.
									//Grab the correct global mapping element.
									tmpGlobalMappingElement = globalMapping.getGlobalMappingElement(mappingID, 0);
									
									if((tmpGlobalMappingElement.getMaxExclusiveValue()) < value)
										tmpGlobalMappingElement.setMaxExclusiveValue(value);
										
									if((tmpGlobalMappingElement.getMaxExclusivePercentValue()) < percentValue)
										tmpGlobalMappingElement.setMaxExclusivePercentValue(percentValue);
									
									//Print out the node,context,thread.
									node = getNode(inputString, false);
									context = getContext(inputString, false);
									thread = getThread(inputString, false);
									//Now the complicated part.  Setting up the node,context,thread data.
									
									
									//These first two if statements force a change if the current node or
									//current context changes from the last, but without a corresponding change
									//in the thread number.  For example, if we have the sequence:
									//0,0,0 - 1,0,0 - 2,0,0 or 0,0,0 - 0,1,0 - 1,0,0.
									if(lastNode != node)
									{
										lastContext = -1;
										lastThread = -1;
									}
									
									if(lastContext != context)
									{
										lastThread = -1;
									}
									
									if(lastThread != thread)
									{
									
										if(thread == 0)
										{
											//Create a new thread ... and set it to be the current thread.
											currentGlobalThread = new GlobalThread();
											//Add the correct number of global thread data elements.
											for(i=0;i<numberOfMappings;i++)
											{
												GlobalThreadDataElement tmpRef = null;
												
												//Add it to the currentGlobalThreadObject.
												currentGlobalThread.addThreadDataElement(tmpRef);
											}
											
											//Update the thread number.
											lastThread = thread;
											
											//Set the appropriate global thread data element.
											Vector tmpVector = currentGlobalThread.getThreadDataList();
											GlobalThreadDataElement tmpGTDE = null;
											
											tmpGTDE = (GlobalThreadDataElement) tmpVector.elementAt(mappingID);
											
											if(tmpGTDE == null)
											{
												tmpGTDE = new GlobalThreadDataElement();
												tmpGTDE.setMappingID(mappingID);
												currentGlobalThread.addThreadDataElement(tmpGTDE, mappingID);
											}
											tmpGTDE.setMappingExists();
											tmpGTDE.setExclusiveValue(value);
											tmpGTDE.setExclusivePercentValue(percentValue);
											//Now check the max values on this thread.
											if((currentGlobalThread.getMaxExclusiveValue()) < value)
												currentGlobalThread.setMaxExclusiveValue(value);
											if((currentGlobalThread.getMaxExclusivePercentValue()) < value)
												currentGlobalThread.setMaxExclusivePercentValue(percentValue);
											
											//Check to see if the context is zero.
											if(context == 0)
											{
												//Create a new context ... and set it to be the current context.
												currentGlobalContext = new GlobalContext("Context Name Not Set!");
												//Add the current thread
												currentGlobalContext.addThread(currentGlobalThread);
												
												//Create a new server ... and set it to be the current server.
												currentGlobalServer = new GlobalServer("Server Name Not Set!");
												//Add the current context.
												currentGlobalServer.addContext(currentGlobalContext);
												//Add the current server.
												StaticServerList.addElement(currentGlobalServer);
												
												//Update last context and last node.
												lastContext = context;
												lastNode = node;
											}
											else
											{
												//Context number is not zero.  Create a new context ... and set it to be current.
												currentGlobalContext = new GlobalContext("Context Name Not Set!");
												//Add the current thread
												currentGlobalContext.addThread(currentGlobalThread);
												
												//Add the current context.
												currentGlobalServer.addContext(currentGlobalContext);
												
												//Update last context and last node.
												lastContext = context;
											}
												
											
											
										}
										else
										{
											//Thread number is not zero.  Create a new thread ... and set it to be the current thread.
											currentGlobalThread = new GlobalThread();
											//Add the correct number of global thread data elements.
											for(i=0;i<numberOfMappings;i++)
											{
												GlobalThreadDataElement tmpRef = null;
												
												//Add it to the currentGlobalThreadObject.
												currentGlobalThread.addThreadDataElement(tmpRef);
											}
											
											//Update the thread number.
											lastThread = thread;
											
											//Not thread changes.  Just set the appropriate global thread data element.
											Vector tmpVector = currentGlobalThread.getThreadDataList();
											GlobalThreadDataElement tmpGTDE = null;
											tmpGTDE = (GlobalThreadDataElement) tmpVector.elementAt(mappingID);
											
											
											if(tmpGTDE == null)
											{
												tmpGTDE = new GlobalThreadDataElement();
												tmpGTDE.setMappingID(mappingID);
												currentGlobalThread.addThreadDataElement(tmpGTDE, mappingID);
											}
											
											tmpGTDE.setMappingExists();
											tmpGTDE.setExclusiveValue(value);
											tmpGTDE.setExclusivePercentValue(percentValue);
											//Now check the max values on this thread.
											if((currentGlobalThread.getMaxExclusiveValue()) < value)
												currentGlobalThread.setMaxExclusiveValue(value);
											if((currentGlobalThread.getMaxExclusivePercentValue()) < value)
												currentGlobalThread.setMaxExclusivePercentValue(percentValue);
											
											//Add the current thread
											currentGlobalContext.addThread(currentGlobalThread);
										}
									}
									else
									{
										//Not thread changes.  Just set the appropriate global thread data element.
										Vector tmpVector = currentGlobalThread.getThreadDataList();
										GlobalThreadDataElement tmpGTDE = null;
										tmpGTDE = (GlobalThreadDataElement) tmpVector.elementAt(mappingID);
									
											
										if(tmpGTDE == null)
										{
											tmpGTDE = new GlobalThreadDataElement();
											tmpGTDE.setMappingID(mappingID);
											currentGlobalThread.addThreadDataElement(tmpGTDE, mappingID);
										}
										
										tmpGTDE.setMappingExists();
										tmpGTDE.setExclusiveValue(value);
										tmpGTDE.setExclusivePercentValue(percentValue);
										//Now check the max values on this thread.
										if((currentGlobalThread.getMaxExclusiveValue()) < value)
											currentGlobalThread.setMaxExclusiveValue(value);
										if((currentGlobalThread.getMaxExclusivePercentValue()) < percentValue)
											currentGlobalThread.setMaxExclusivePercentValue(percentValue);
									}
								}
								else if(checkForInclusive(inputString))
								{
									//Grab the mapping ID.
									mappingID = getMappingID(inputString);
									//Grab the value.
									value = getValue(inputString);
									percentValue = getPercentValue(inputString);
									
									
									//Update the max values if required.
									//Grab the correct global mapping element.
									tmpGlobalMappingElement = globalMapping.getGlobalMappingElement(mappingID, 0);
									
									if((tmpGlobalMappingElement.getMaxInclusiveValue()) < value)
										tmpGlobalMappingElement.setMaxInclusiveValue(value);
										
									if((tmpGlobalMappingElement.getMaxInclusivePercentValue()) < percentValue)
										tmpGlobalMappingElement.setMaxInclusivePercentValue(percentValue);
									
									
									//Print out the node,context,thread.
									node = getNode(inputString, false);
									context = getContext(inputString, false);
									thread = getThread(inputString, false);
									
									//Find the correct global thread data element.
									GlobalServer tmpGS = (GlobalServer) StaticServerList.elementAt(node);
									Vector tmpGlobalContextList = tmpGS.getContextList();
									GlobalContext tmpGC = (GlobalContext) tmpGlobalContextList.elementAt(context);
									Vector tmpGlobalThreadList = tmpGC.getThreadList();
									GlobalThread tmpGT = (GlobalThread) tmpGlobalThreadList.elementAt(thread);
									Vector tmpGlobalThreadDataElementList = tmpGT.getThreadDataList();
									
									GlobalThreadDataElement tmpGTDE = (GlobalThreadDataElement) tmpGlobalThreadDataElementList.elementAt(mappingID);
									//Now set the inclusive value!
									
									if(tmpGTDE == null)
									{
										tmpGTDE = new GlobalThreadDataElement();
										tmpGTDE.setMappingID(mappingID);
										currentGlobalThread.addThreadDataElement(tmpGTDE, mappingID);
									}
									
									
									tmpGTDE.setInclusiveValue(value);
									tmpGTDE.setInclusivePercentValue(percentValue);
									//Now check the max values on this thread.
									if((currentGlobalThread.getMaxInclusiveValue()) < value)
										currentGlobalThread.setMaxInclusiveValue(value);
									if((currentGlobalThread.getMaxInclusivePercentValue()) < percentValue)
										currentGlobalThread.setMaxInclusivePercentValue(percentValue);
									
									//Get the number of calls and number of sub routines, and then set the total stat string.
									//The next string in the file should be the correct one.  Assume it.
									//If the file format changes, we are all screwed anyway.
									inputString = br.readLine();
									
									int numberOfCalls = (int) getNumberOfCalls(inputString);
									int numberOfSubRoutines = (int) getNumberOfSubRoutines(inputString);
									
									//Update max values.
									if((tmpGlobalMappingElement.getMaxNumberOfCalls()) < numberOfCalls)
										tmpGlobalMappingElement.setMaxNumberOfCalls(numberOfCalls);
									if((tmpGlobalMappingElement.getMaxNumberOfSubRoutines()) < numberOfSubRoutines)
										tmpGlobalMappingElement.setMaxNumberOfSubRoutines(numberOfSubRoutines);
										
									if((currentGlobalThread.getMaxNumberOfCalls()) < numberOfCalls)
										currentGlobalThread.setMaxNumberOfCalls(numberOfCalls);
									if((currentGlobalThread.getMaxNumberOfSubRoutines()) < numberOfSubRoutines)
										currentGlobalThread.setMaxNumberOfSubRoutines(numberOfSubRoutines);
										
									tmpGTDE.setNumberOfCalls(numberOfCalls);
									tmpGTDE.setNumberOfSubRoutines(numberOfSubRoutines);
									
									//Set the total stat string.
									tmpGTDE.setTStatString(inputString);
									//Now extract the other info from this string.
									
								}
								else if(checkForUserEvents(inputString))
								{
									//The first time a user event string is encountered, get the number of user events and 
									//initialize the global mapping for mapping position 2.
									if(!(this.userEventsPresent())){
										//Get the number of user events.
										numberOfUserEvents = getNumberOfUserEvents(inputString);
										initializeGlobalMapping(numberOfUserEvents, 2);
										if(jRacy.debugIsOn){
											System.out.println("The number of user events defined is: " + numberOfUserEvents);
											System.out.println("Initializing mapping selection 2 (The loaction of the user event mapping) for " +
																numberOfUserEvents + " mappings.");
										}
									} 
									
									//The first line will be the user event heading ... get it.
									inputString = br.readLine();
									userEventHeading = inputString;
									
									positionOfUserEventName = inputString.indexOf("Event Name");
									
									//Find the correct global thread data element.
									GlobalServer tmpGSUE = null;
									Vector tmpGlobalContextListUE = null;;
									GlobalContext tmpGCUE = null;;
									Vector tmpGlobalThreadListUE = null;;
									GlobalThread tmpGTUE = null;;
									Vector tmpGlobalThreadDataElementListUE = null;
									
									//Now that we know how many user events to expect, we can grab that number of lines.
									for(int j=0; j<numberOfUserEvents; j++)
									{
										inputString = br.readLine();
										
										//Initialize the user list for this thread.
										if(j == 0)
										{
											//Note that this works correctly because we process the user events in a different manner.
											//ALL the user events for each THREAD NODE are processed in the above for-loop.  Therefore,
											//the below for-loop is only run once on each THREAD NODE.  If you do not believe it, turn on
											//debugging.
											if(jRacy.debugIsOn)
												System.out.println("Creating the list for node,context,thread: " +node+","+context+","+thread);
											
											//Get the node,context,thread.
											node = getNode(inputString, true);
											context = getContext(inputString, true);
											thread = getThread(inputString, true);
											
											//Find the correct global thread data element.
											tmpGSUE = (GlobalServer) StaticServerList.elementAt(node);
											tmpGlobalContextListUE = tmpGSUE.getContextList();
											tmpGCUE = (GlobalContext) tmpGlobalContextListUE.elementAt(context);
											tmpGlobalThreadListUE = tmpGCUE.getThreadList();
											tmpGTUE = (GlobalThread) tmpGlobalThreadListUE.elementAt(thread);
											
											for(int k=0; k<numberOfUserEvents; k++)
											{
												tmpGTUE.addUserThreadDataElement(new GlobalThreadDataElement());
											}
											
											tmpGlobalThreadDataElementListUE = tmpGTUE.getUserThreadDataList();
										}
										
										
										//Extract all the information out of the string that I need.
										
										//Grab the mapping ID.
										userEventID = getUserEventID(inputString);
										
										//Only need to set the name in the global mapping once.
										if(!(this.userEventsPresent())){
											//Grab the mapping name.
											userEventNameString = getUserEventName(inputString);
											if(!(globalMapping.setMappingNameAt(userEventNameString, userEventID, 2)))
												System.out.println("There was an error adding mapping to the global mapping");
											if(jRacy.debugIsOn){
												System.out.println("The user event ID: " + userEventID);
												System.out.println("The user event name is: " + userEventNameString);
											}
										}
										
										int userEventNumberValue = getUENValue(inputString);
										double userEventMinValue = getUEMinValue(inputString);
										double userEventMaxValue = getUEMaxValue(inputString);
										double userEventMeanValue = getUEMeanValue(inputString);
										
										//Update the max values if required.
										//Grab the correct global mapping element.
										tmpGlobalMappingElement = globalMapping.getGlobalMappingElement(userEventID, 2);
										
										if((tmpGlobalMappingElement.getMaxUserEventNumberValue()) < userEventNumberValue)
											tmpGlobalMappingElement.setMaxUserEventNumberValue(userEventNumberValue);
											
										if((tmpGlobalMappingElement.getMaxUserEventMinValue()) < userEventMinValue)
											tmpGlobalMappingElement.setMaxUserEventMinValue(userEventMinValue);
											
										if((tmpGlobalMappingElement.getMaxUserEventMaxValue()) < userEventMaxValue)
											tmpGlobalMappingElement.setMaxUserEventMaxValue(userEventMaxValue);
										
										if((tmpGlobalMappingElement.getMaxUserEventMeanValue()) < userEventMeanValue)
											tmpGlobalMappingElement.setMaxUserEventMeanValue(userEventMeanValue);
										
										
										GlobalThreadDataElement tmpGTDEUE = (GlobalThreadDataElement) tmpGlobalThreadDataElementListUE.elementAt(userEventID);
										//Ok, now set the instance data elements.
										tmpGTDEUE.setUserEventID(userEventID);
										tmpGTDEUE.setUserEventNumberValue(userEventNumberValue);
										tmpGTDEUE.setUserEventMinValue(userEventMinValue);
										tmpGTDEUE.setUserEventMaxValue(userEventMaxValue);
										tmpGTDEUE.setUserEventMeanValue(userEventMeanValue);
										
										//Ok, now get the next string as that is the stat string for this event.
										inputString = br.readLine();
										tmpGTDEUE.setUserEventStatString(inputString);		
										
									}
									
									//Now set the userEvents flag.
									this.setUserEventsPresent(true);
								}
							//End - String does not begin with either an m or a t, the rest of the checks go here.
							}
						}
						else
						{
							heading = inputString;
							positionOfName = inputString.indexOf("name");
						}
					
					}
					else
					{
						//This is the second line of the file.  It's first token will
						//be the number of mappings present.  Get it.
						tokenString = genericTokenizer.nextToken();
						
						//Set the number of mappings.
						numberOfMappings = Integer.parseInt(tokenString);
						
						//Now initialize the global mapping with the correct number of mappings for mapping position 0.
						initializeGlobalMapping(Integer.parseInt(tokenString), 0);
						
						//Set the counter name.
						counterName = getCounterName(inputString);
						
						//For testing purposes, print it out.
						if(counterName != null)
							System.out.println("Pprof output for counter: " + counterName);
						System.out.println("The number of mappings in the system is: " + tokenString);
					}
						
				}
				//Increment the loop counter.
			bSDCounter++;
			}
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD01");
		}
	}
		
	
	//******************************
	//Helper functions for buildStatic data.
	//******************************
	boolean checkForBeginningT(String inString)
	{
		try{
			StringTokenizer checkForBeginningTTokenizer = new StringTokenizer(inString, " \t\n\r");
			
			String tmpString;
			
			tmpString = checkForBeginningTTokenizer.nextToken();
				
			if(tmpString.equals("t"))
				return true;
			else
				return false;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD02");
		}
		
		return false;
	}
	
	boolean checkForBeginningM(String inString)
	{
		
		try{
			StringTokenizer checkForBeginningTTokenizer = new StringTokenizer(inString, " \t\n\r");
			
			String tmpString;
			
			tmpString = checkForBeginningTTokenizer.nextToken();
				
			if(tmpString.equals("m"))
				return true;
			else
				return false;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD03");
		}
		
		return false;
	}
	
	boolean checkForExclusive(String inString)
	{
		
		try{
			//In this function I need to be careful.  If the mapping name contains "excl", I
			//might interpret this line as being the exclusive line when in fact it is not.
			
			//Check for the right string.
			StringTokenizer checkTokenizer = new StringTokenizer(inString," ");
			String tmpString2 = checkTokenizer.nextToken();
			if((tmpString2.indexOf(",")) != -1)
			{
				//Ok, so at least we have the correct string.
				//Now, we want to grab the substring that occurs AFTER the SECOND '"'.
				//At present, pprof does not seem to allow an '"' in the mapping name.  So
				//, I can be assured that I will not find more than two before the "excl" or "incl".
				StringTokenizer checkQuotesTokenizer = new StringTokenizer(inString,"\"");
				
				//Need to get the third token.  Could do it in a loop, just as quick this way.
				String tmpString = checkQuotesTokenizer.nextToken();
				tmpString = checkQuotesTokenizer.nextToken();
				tmpString = checkQuotesTokenizer.nextToken();
				
				//Ok, now, the string in tmpString should include at least "excl" or "incl", and
				//also, the first token should be either "excl" or "incl".
				StringTokenizer checkForExclusiveTokenizer = new StringTokenizer(tmpString, " \t\n\r");
				tmpString = checkForExclusiveTokenizer.nextToken();
					
				//At last, do the check.	
				if(tmpString.equals("excl"))
				{
					return true;
				}
			}
			
			//If here, it means that we are not looking at the correct string or that we did not
			//find a match.  Therefore, return false.
			return false;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD04");
		}
		
		return false;
	}
	
	boolean checkForExclusiveWithTOrM(String inString)
	{
		
		try{
			//In this function I need to be careful.  If the mapping name contains "excl", I
			//might interpret this line as being the exclusive line when in fact it is not.
			
			//Ok, so at least we have the correct string.
			//Now, we want to grab the substring that occurs AFTER the SECOND '"'.
			//At present, pprof does not seem to allow an '"' in the mapping name.  So
			//, I can be assured that I will not find more than two before the "excl" or "incl".
			StringTokenizer checkQuotesTokenizer = new StringTokenizer(inString,"\"");
			
			//Need to get the third token.  Could do it in a loop, just as quick this way.
			String tmpString = checkQuotesTokenizer.nextToken();
			tmpString = checkQuotesTokenizer.nextToken();
			tmpString = checkQuotesTokenizer.nextToken();
			
			//Ok, now, the string in tmpString should include at least "excl" or "incl", and
			//also, the first token should be either "excl" or "incl".
			StringTokenizer checkForExclusiveTokenizer = new StringTokenizer(tmpString, " \t\n\r");
			tmpString = checkForExclusiveTokenizer.nextToken();
				
			//At last, do the check.	
			if(tmpString.equals("excl"))
			{
				return true;
			}
			
			//If here, it means that we are not looking at the correct string or that we did not
			//find a match.  Therefore, return false.
			return false;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD05");
		}
		
		return false;
	}
	
	boolean checkForInclusive(String inString)
	{
		
		try{
			//In this function I need to be careful.  If the mapping name contains "incl", I
			//might interpret this line as being the inclusive line when in fact it is not.
			
			
			//Check for the right string.
			StringTokenizer checkTokenizer = new StringTokenizer(inString," ");
			String tmpString2 = checkTokenizer.nextToken();
			if((tmpString2.indexOf(",")) != -1)
			{
			
				//Now, we want to grab the substring that occurs AFTER the SECOND '"'.
				//At present, pprof does not seem to allow an '"' in the mapping name.  So
				//, I can be assured that I will not find more than two before the "excl" or "incl".
				StringTokenizer checkQuotesTokenizer = new StringTokenizer(inString,"\"");
				
				//Need to get the third token.  Could do it in a loop, just as quick this way.
				String tmpString = checkQuotesTokenizer.nextToken();
				tmpString = checkQuotesTokenizer.nextToken();
				tmpString = checkQuotesTokenizer.nextToken();
				
				//Ok, now, the string in tmpString should include at least "excl" or "incl", and
				//also, the first token should be either "excl" or "incl".
				StringTokenizer checkForInclusiveTokenizer = new StringTokenizer(tmpString, " \t\n\r");
				tmpString = checkForInclusiveTokenizer.nextToken();
					
				//At last, do the check.	
				if(tmpString.equals("incl"))
				{
					return true;
				}
			}
			//If here, it means that we are not looking at the correct string or that we did not
			//find a match.  Therefore, return false.
			return false;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD06");
		}
		
		return false;
	}
	
	boolean checkForInclusiveWithTOrM(String inString)
	{
		
		try{
			//In this function I need to be careful.  If the mapping name contains "incl", I
			//might interpret this line as being the inclusive line when in fact it is not.

			//Ok, so at least we have the correct string.
			//Now, we want to grab the substring that occurs AFTER the SECOND '"'.
			//At present, pprof does not seem to allow an '"' in the mapping name.  So
			//, I can be assured that I will not find more than two before the "excl" or "incl".
			StringTokenizer checkQuotesTokenizer = new StringTokenizer(inString,"\"");
			
			//Need to get the third token.  Could do it in a loop, just as quick this way.
			String tmpString = checkQuotesTokenizer.nextToken();
			tmpString = checkQuotesTokenizer.nextToken();
			tmpString = checkQuotesTokenizer.nextToken();
			
			//Ok, now, the string in tmpString should include at least "excl" or "incl", and
			//also, the first token should be either "excl" or "incl".
			StringTokenizer checkForInclusiveTokenizer = new StringTokenizer(tmpString, " \t\n\r");
			tmpString = checkForInclusiveTokenizer.nextToken();
				
			//At last, do the check.	
			if(tmpString.equals("incl"))
			{
				return true;
			}
			
			//If here, it means that we are not looking at the correct string or that we did not
			//find a match.  Therefore, return false.
			return false;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD07");
		}
		
		return false;
	}
	
	String getMappingName(String inString)
	{
		try{
			String tmpString;
			
			StringTokenizer getMappingNameTokenizer = new StringTokenizer(inString, "\"");
			
			//Since we know that the mapping name is the only one in the quotes, just ignore the
			//first token, and then grab the next.
			
			//Grab the first token.
			tmpString = getMappingNameTokenizer.nextToken();
			
			//Grab the second token.
			tmpString = getMappingNameTokenizer.nextToken();
			
			//Now return the second string.
			return tmpString;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD08");
		}
		
		return null;
	}
	
	int getMappingID(String inString)
	{
		try{
			String tmpString;
			
			StringTokenizer getMappingIDTokenizer = new StringTokenizer(inString, " \t\n\r");
			
			//The mapping id will be the second token on its line.
			
			//Grab the first token.
			tmpString = getMappingIDTokenizer.nextToken();
			
			//Grab the second token.
			tmpString = getMappingIDTokenizer.nextToken();
			
			
			//Now return the id.
			//Integer tmpInteger = new Integer(tmpString);
			//int tmpInt = tmpInteger.intValue();
			return Integer.parseInt(tmpString);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD09");
		}
		
		return -1;
	}
	
	double getNumberOfCalls(String inString)
	{
		try{
			String tmpString;
			
			StringTokenizer getMappingIDTokenizer = new StringTokenizer(inString, " \t\n\r");
			
			//The number of calls will be the fourth token on its line.
			
			//Grab the first token.
			tmpString = getMappingIDTokenizer.nextToken();
			
			//Grab the second token.
			tmpString = getMappingIDTokenizer.nextToken();
			
			//Grab the third token.
			tmpString = getMappingIDTokenizer.nextToken();
			
			//Grab the forth token.
			tmpString = getMappingIDTokenizer.nextToken();
			
			//Now return the number of calls.
			return Double.parseDouble(tmpString);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD10");
		}
		
		return -1;
	}
	
	double getNumberOfSubRoutines(String inString)
	{
		try{
			String tmpString;
			
			StringTokenizer getMappingIDTokenizer = new StringTokenizer(inString, " \t\n\r");
			
			//The number of calls will be the fifth token on its line.
			
			//Grab the first token.
			tmpString = getMappingIDTokenizer.nextToken();
			
			//Grab the second token.
			tmpString = getMappingIDTokenizer.nextToken();
			
			//Grab the third token.
			tmpString = getMappingIDTokenizer.nextToken();
			
			//Grab the forth token.
			tmpString = getMappingIDTokenizer.nextToken();
			
			//Grab the fifth token.
			tmpString = getMappingIDTokenizer.nextToken();
			
			//Now return the number of subroutines.
			return Double.parseDouble(tmpString);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD11");
		}
		
		return -1;
	}
	
	String getGroupNames(String inString)
	{
		
		try{	
				String tmpString = null;
				
				StringTokenizer getMappingNameTokenizer = new StringTokenizer(inString, "\"");
				
				//Grab the first token.
				tmpString = getMappingNameTokenizer.nextToken();
				//Grab the second token.
				tmpString = getMappingNameTokenizer.nextToken();
				//Grab the third token.
				tmpString = getMappingNameTokenizer.nextToken();
				
				//Just do the group check once.
				if(!groupNamesCheck)
				{
					//If present, "GROUP=" will be in this token.
					int tmpInt = tmpString.indexOf("GROUP=");
					if(tmpInt > 0)
					{
						groupNamesPresent = true;
					}
					
					groupNamesCheck = true;
					
				}
				
				if(groupNamesPresent)
				{
					//We can grab the group name.
					
					//Grab the forth token.
					tmpString = getMappingNameTokenizer.nextToken();
					return tmpString;
				}
				
				//If here, this profile file does not track the group names.
				return null;

			}
			catch(Exception e)
			{
				jRacy.systemError(null, "SSD12");
			}
		
		return null;
	}
	
	double getValue(String inString)
	{
		try{
			String tmpString;
			
			//First strip away the portion of the string not needed.
			StringTokenizer valueQuotesTokenizer = new StringTokenizer(inString,"\"");
			
			//Grab the third token.
			tmpString = valueQuotesTokenizer.nextToken();
			tmpString = valueQuotesTokenizer.nextToken();
			tmpString = valueQuotesTokenizer.nextToken();
			
			//Ok, now concentrate on the third token.  The token in question should be the second.
			StringTokenizer valueTokenizer = new StringTokenizer(tmpString, " \t\n\r");
			tmpString = valueTokenizer.nextToken();
			tmpString = valueTokenizer.nextToken();
			
			//Now return the value obtained as an int.
			return (int)Double.parseDouble(tmpString);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD13");
		}
		
		return -1;
	}
	
	double getPercentValue(String inString)
	{
		try{
			String tmpString;
			
			//First strip away the portion of the string not needed.
			StringTokenizer percentValueQuotesTokenizer = new StringTokenizer(inString,"\"");
			
			//Grab the third token.
			tmpString = percentValueQuotesTokenizer.nextToken();
			tmpString = percentValueQuotesTokenizer.nextToken();
			tmpString = percentValueQuotesTokenizer.nextToken();
			
			//Ok, now concentrate on the third token.  The token in question should be the third.
			StringTokenizer percentValueTokenizer = new StringTokenizer(tmpString, " \t\n\r");
			tmpString = percentValueTokenizer.nextToken();
			tmpString = percentValueTokenizer.nextToken();
			tmpString = percentValueTokenizer.nextToken();
			
			//Now return the value obtained.
			return Double.parseDouble(tmpString);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD14");
		}
		
		return -1;
	}
	
	boolean checkForUserEvents(String inString)
	{
		try{
			String tmpString;
			
			StringTokenizer checkForUserEventsTokenizer = new StringTokenizer(inString, " \t\n\r");
			
			//Looking for the second token ... no danger of conflict here.
			
			//Grab the first token.
			tmpString = checkForUserEventsTokenizer.nextToken();
			
			//Grab the second token.
			tmpString = checkForUserEventsTokenizer.nextToken();
			
			//No do the check.
			if(tmpString.equals("userevents"))
				return true;
			else
				return false;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD15");
		}
		
		return false;	
	}
	
	int getNumberOfUserEvents(String inString)
	{
		try{
			StringTokenizer getNumberOfUserEventsTokenizer = new StringTokenizer(inString, " \t\n\r");
			
			String tmpString;
			
			//It will be the first token.
			tmpString = getNumberOfUserEventsTokenizer.nextToken();
			
			//Now return the number of user events number.
			return Integer.parseInt(tmpString);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD16");
		}
		
		return -1;
	}
										
	String getUserEventName(String inString)
	{
		try{
			String tmpString;
			
			StringTokenizer getUserEventNameTokenizer = new StringTokenizer(inString, "\"");
			
			//Since we know that the user event name is the only one in the quotes, just ignore the
			//first token, and then grab the next.
			
			//Grab the first token.
			tmpString = getUserEventNameTokenizer.nextToken();
			
			//Grab the second token.
			tmpString = getUserEventNameTokenizer.nextToken();
			
			//Now return the second string.
			return tmpString;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD17");
		}
		
		return null;
	}
	
	int getUserEventID(String inString)
	{
		try{
			String tmpString;
			
			StringTokenizer getUserEventIDTokenizer = new StringTokenizer(inString, " \t\n\r");
			
			//The mapping id will be the third token on its line.
			
			//Grab the first token.
			tmpString = getUserEventIDTokenizer.nextToken();
			
			//Grab the second token.
			tmpString = getUserEventIDTokenizer.nextToken();
			
			//Grab the second token.
			tmpString = getUserEventIDTokenizer.nextToken();
			
			return Integer.parseInt(tmpString);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD18");
		}
		
		return -1;
	}
	
	int getUENValue(String inString)
	{
		
		try{
			String tmpString;
			
			//First strip away the portion of the string not needed.
			StringTokenizer uEQuotesTokenizer = new StringTokenizer(inString,"\"");
			
			//Grab the third token.
			tmpString = uEQuotesTokenizer.nextToken();
			tmpString = uEQuotesTokenizer.nextToken();
			tmpString = uEQuotesTokenizer.nextToken();
			
			//Ok, now concentrate on the third token.  The token in question should be the first.
			StringTokenizer uETokenizer = new StringTokenizer(tmpString, " \t\n\r");
			tmpString = uETokenizer.nextToken();
			
			//Now return the value obtained as an int.
			return (int)Double.parseDouble(tmpString);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD19");
		}
		
		return -1;
	}
	
	double getUEMinValue(String inString)
	{
		try{
			String tmpString;
			
			//First strip away the portion of the string not needed.
			StringTokenizer uEQuotesTokenizer = new StringTokenizer(inString,"\"");
			
			//Grab the third token.
			tmpString = uEQuotesTokenizer.nextToken();
			tmpString = uEQuotesTokenizer.nextToken();
			tmpString = uEQuotesTokenizer.nextToken();
			
			//Ok, now concentrate on the third token.  The token in question should be the third.
			StringTokenizer uETokenizer = new StringTokenizer(tmpString, " \t\n\r");
			tmpString = uETokenizer.nextToken();
			tmpString = uETokenizer.nextToken();
			tmpString = uETokenizer.nextToken();
			
			//Now return the value obtained.
			return Double.parseDouble(tmpString);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD20");
		}
		
		return -1;
	}
	
	double getUEMaxValue(String inString)
	{
		try{
			String tmpString;
			
			//First strip away the portion of the string not needed.
			StringTokenizer uEQuotesTokenizer = new StringTokenizer(inString,"\"");
			
			//Grab the third token.
			tmpString = uEQuotesTokenizer.nextToken();
			tmpString = uEQuotesTokenizer.nextToken();
			tmpString = uEQuotesTokenizer.nextToken();
			
			//Ok, now concentrate on the third token.  The token in question should be the second.
			StringTokenizer uETokenizer = new StringTokenizer(tmpString, " \t\n\r");
			tmpString = uETokenizer.nextToken();
			tmpString = uETokenizer.nextToken();
			
			//Now return the value obtained.
			return Double.parseDouble(tmpString);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD21");
		}
		
		return -1;
	}
	
	double getUEMeanValue(String inString)
	{
		try{
			String tmpString;
			
			//First strip away the portion of the string not needed.
			StringTokenizer uEQuotesTokenizer = new StringTokenizer(inString,"\"");
			
			//Grab the third token.
			tmpString = uEQuotesTokenizer.nextToken();
			tmpString = uEQuotesTokenizer.nextToken();
			tmpString = uEQuotesTokenizer.nextToken();
			
			//Ok, now concentrate on the third token.  The token in question should be the forth.
			StringTokenizer uETokenizer = new StringTokenizer(tmpString, " \t\n\r");
			tmpString = uETokenizer.nextToken();
			tmpString = uETokenizer.nextToken();
			tmpString = uETokenizer.nextToken();
			tmpString = uETokenizer.nextToken();
			
			//Now return the value obtained.
			return Double.parseDouble(tmpString);
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD22");
		}
		
		return -1;
	}
	
	int getNode(String inString, boolean UEvent)
	{
		try{
			StringTokenizer getNodeTokenizer = new StringTokenizer(inString, ", \t\n\r");
			
			String tmpString;
			
			if(UEvent)
			{
				//Need to strip off the first token.
				tmpString = getNodeTokenizer.nextToken();
			}
			
			//Get the first token.
			tmpString = getNodeTokenizer.nextToken();
			
			//Now return the node number.
			Integer tmpInteger = new Integer(tmpString);
			int tmpInt = tmpInteger.intValue();
			return tmpInt;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD23");
		}
		
		return -1;
		
	}
	
	int getContext(String inString, boolean UEvent)
	{
		try{
			StringTokenizer getContextTokenizer = new StringTokenizer(inString, ", \t\n\r");
			
			String tmpString;
			
			if(UEvent)
			{
				//Need to strip off the first token.
				tmpString = getContextTokenizer.nextToken();
			}
			
			//Get the first token.
			tmpString = getContextTokenizer.nextToken();
			
			//Get the second.
			tmpString = getContextTokenizer.nextToken();
			
			//Now return the context number.
			Integer tmpInteger = new Integer(tmpString);
			int tmpInt = tmpInteger.intValue();
			return tmpInt;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD24");
		}
		
		return -1;
	}
	
	int getThread(String inString, boolean UEvent)
	{		
		try{
			StringTokenizer getThreadTokenizer = new StringTokenizer(inString, ", \t\n\r");
			
			String tmpString;
			
			if(UEvent)
			{
				//Need to strip off the first token.
				tmpString = getThreadTokenizer.nextToken();
			}
			
			//Get the first token.
			tmpString = getThreadTokenizer.nextToken();
			
			//Get the second.
			tmpString = getThreadTokenizer.nextToken();
			
			//Get the third token.
			tmpString = getThreadTokenizer.nextToken();
			
			//Now return the context number.
			Integer tmpInteger = new Integer(tmpString);
			int tmpInt = tmpInteger.intValue();
			return tmpInt;
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD25");
		}
		
		return -1;
	}
	
	String getCounterName(String inString)
	{
		try{
			String tmpString = null;
			int tmpInt = inString.indexOf("_MULTI_");
			
			if(tmpInt > 0)
			{
				//We are reading data from a multiple counter run.
				//Grab the counter name.
				tmpString = inString.substring(tmpInt+7);
				return tmpString;
			}
			
			//We are not reading data from a multiple counter run.
			return tmpString;	
			
		}
		catch(Exception e)
		{
			jRacy.systemError(null, "SSD26");
		}
		
		return null;
	}
	
	//******************************
	//End - Helper functions for buildStatic data.
	//******************************
	
	
	
	//******************************
	//Useful functions to help the drawing windows.
	//
	//For the most part, these functions just return data
	//items that are easier to calculate whilst building the global
	//lists
	//******************************
	
	public int getPositionOfName()
	{
		return positionOfName;
	}
	
	public int getPositionOfUserEventName()
	{
		return positionOfUserEventName;
	}
	
	public String getHeading()
	{
		return heading;
	}
	
	public String getUserEventHeading()
	{
		return userEventHeading;
	}
	
	public void setMaxMeanInclusiveValue(double inDouble)
	{
		maxMeanInclusiveValue = inDouble;
	}
	
	public double getMaxMeanInclusiveValue()
	{
		return maxMeanInclusiveValue;
	}
	
	public void setMaxMeanExclusiveValue(double inDouble)
	{
		maxMeanExclusiveValue = inDouble;
	}
	
	public double getMaxMeanExclusiveValue()
	{
		return maxMeanExclusiveValue;
	}
	
	public void setMaxMeanInclusivePercentValue(double inDouble)
	{
		maxMeanInclusivePercentValue = inDouble;
	}
	
	public double getMaxMeanInclusivePercentValue()
	{
		return maxMeanInclusivePercentValue;
	}
	
	public void setMaxMeanExclusivePercentValue(double inDouble)
	{
		maxMeanExclusivePercentValue = inDouble;
	}
	
	public double getMaxMeanExclusivePercentValue()
	{
		return maxMeanExclusivePercentValue;
	}
	
	public void setMaxMeanNumberOfCalls(double inDouble)
	{
		maxMeanNumberOfCalls = inDouble;
	}
	
	public double getMaxMeanNumberOfCalls()
	{
		return maxMeanNumberOfCalls;
	}
	
	public void setMaxMeanNumberOfSubRoutines(double inDouble)
	{
		maxMeanNumberOfSubRoutines = inDouble;
	}
	
	public double getMaxMeanNumberOfSubRoutines()
	{
		return maxMeanNumberOfSubRoutines;
	}
	
	public boolean groupNamesPresent(){
		return groupNamesPresent;
	}
	
	private void setUserEventsPresent(boolean inBoolean){
		userEventsPresent = inBoolean;
	}
	
	public boolean userEventsPresent(){
		return userEventsPresent;
	}
	//******************************
	//End - Useful functions to help the drawing windows.
	//******************************
	
	
	//******************************
	//Instance data.
	//******************************
	GlobalMapping globalMapping;
	private Vector StaticServerList;
	private int positionOfName;
	private int positionOfUserEventName;
	private String counterName;
	private String heading;
	private String userEventHeading;
	private boolean isUserEventHeadingSet;
	boolean groupNamesCheck = false;
	boolean groupNamesPresent = false;
	boolean userEventsPresent = false;
	int bSDCounter;
	
	//Max mean values.
	double maxMeanInclusiveValue = 0;
	double maxMeanExclusiveValue = 0;
	double maxMeanInclusivePercentValue = 0;
	double maxMeanExclusivePercentValue = 0;
	double maxMeanNumberOfCalls = 0;
	double maxMeanNumberOfSubRoutines = 0;
	
	//******************************
	//End - Instance data.
	//******************************

}