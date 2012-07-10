/****************************************************************************
**			TAU Portable Profiling Package			   **
**			http://www.cs.uoregon.edu/research/tau	           **
*****************************************************************************
**    Copyright 2010  						   	   **
**    Department of Computer and Information Science, University of Oregon **
**    Advanced Computing Laboratory, Los Alamos National Laboratory        **
****************************************************************************/
/****************************************************************************
**	File 		: TauMetaData.h  				   **
**	Description 	: TAU Profiling Package				   **
**	Contact		: tau-bugs@cs.uoregon.edu               	   **
**	Documentation	: See http://www.cs.uoregon.edu/research/tau       **
**                                                                         **
**      Description     : This file contains metadata related routines     **
**                                                                         **
****************************************************************************/


#ifndef _TAU_METADATA_H_
#define _TAU_METADATA_H_


#include <TauUtil.h>
#include <string>
#include <map>
// Putting "using namespace" statements in header files can create ambiguity
// between user-defined symbols and std symbols, creating unparsable code
// or even changing the behavior of user codes.  This is also widely considered
// to be bad practice.  Here's a code PDT can't parse because of this line:
//   EX: #include <complex>
//   EX: typedef double real;
//
//using namespace std;

std::map<std::string,std::string> &Tau_metadata_getMetaData();
int Tau_metadata_writeMetaData(Tau_util_outputDevice *out, int counter);
int Tau_metadata_writeMetaData(FILE *fp, int counter);
int Tau_metadata_writeMetaData(Tau_util_outputDevice *out);
int Tau_metadata_fillMetaData();
Tau_util_outputDevice *Tau_metadata_generateMergeBuffer();
void Tau_metadata_removeDuplicates(char *buffer, int buflen);

void Tau_metadata_register(char *name, int value);
int Tau_metadata_mergeMetaData();

#endif /* _TAU_METADATA_H_ */
