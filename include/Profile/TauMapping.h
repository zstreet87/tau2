/****************************************************************************
**			TAU Portable Profiling Package			   **
**			http://www.cs.uoregon.edu/research/tau	           **
*****************************************************************************
**    Copyright 1999-2009					   	   **
**    Department of Computer and Information Science, University of Oregon **
**    Advanced Computing Laboratory, Los Alamos National Laboratory        **
****************************************************************************/
/****************************************************************************
**	File 		: Profiler.cpp					   **
**	Description 	: TAU Mappings for relating profile data from one  **
**			  layer to another				   **
**	Author		: Sameer Shende					   **
**	Contact		: tau-bugs@cs.uoregon.edu                	   **
**	Documentation	: See http://www.cs.uoregon.edu/research/tau       **
****************************************************************************/

#ifndef _TAU_MAPPING_H_
#define _TAU_MAPPING_H_

#if (PROFILING_ON || TRACING_ON)
// For Mapping, global variables used between layers
void *& TheTauMapFI(TauGroup_t key=TAU_DEFAULT);

#define TAU_MAPPING(stmt, key) \
  { \
    static void *TauMapFI = NULL; \
    tauCreateFI(&TauMapFI, #stmt, " " , key, #key); \
    TheTauMapFI(key) = TauMapFI; \
    Tau_start_timer(TauMapFI, 0); \
    stmt; \
    Tau_stop_timer(TauMapFI); \
  } 

#define TAU_MAPPING_REGISTER(stmt, key)  { \
    static void *TauMapFI = NULL; \
    tauCreateFI(&TauMapFI,stmt, " " , key, #key); \
    TheTauMapFI(key) = TauMapFI; \
  } 

#define TAU_MAPPING_CREATE(name, type, key, groupname, tid)  { \
    void *TauMapFI = NULL; \
    tauCreateFI(&TauMapFI, name, type, key, groupname); \
    if (TauMapFI == 0) { \
	printf("ERROR: new returned NULL"); exit(1); \
    } \
    TheTauMapFI(key) = TauMapFI; \
  } 

#define TAU_MAPPING_CREATE1(name, type, key, groupid, groupname, tid)  { \
    void *TauMapFI = NULL; \
    tauCreateFI(&TauMapFI, name, type, groupid, groupname); \
    if (TauMapFI == 0) { \
	printf("ERROR: new returned NULL"); exit(1); \
    } \
    TheTauMapFI(key) = TauMapFI; \
  } 

/* TAU_MAPPING_TIMER_CREATE creates a functionInfo pointer with a specified 
   group name. */
#define TAU_MAPPING_TIMER_CREATE(t, name, type, gr, groupname) \
  tauCreateFI(&t, name, type, gr, groupname);

/* TAU_MAPPING_OBJECT creates a functionInfo pointer that may be stored in the 
   object that is used to relate a lower level layer with a higher level layer */
#define TAU_MAPPING_OBJECT(timer) void *timer = NULL;

/* TAU_MAPPING_LINK gets in a var the function info object associated with the given key (Group) */
#define TAU_MAPPING_LINK(timer, key) timer = TheTauMapFI(key); 

/* TAU_MAPPING_PROFILE profiles the entire routine by creating a profiler objeca
   and this behaves pretty much like TAU_PROFILE macro, except this gives in the
   FunctionInfo object pointer instead of name and type strings. */
#define TAU_MAPPING_PROFILE(timer) Tau_Profile_Wrapper timer##tauFP(timer);

/* TAU_MAPPING_PROFILE_TIMER acts like TAU_PROFILE_TIMER by creating a profiler
   object that can be subsequently used with TAU_PROFILE_START and TAU_PROFILE_STOP */
#define TAU_MAPPING_PROFILE_TIMER(timer, FuncInfoVar, tid) void *timer = FuncInfoVar;
   
/* TAU_MAPPING_PROFILE_START acts like TAU_PROFILE_START by starting the timer */
#define TAU_MAPPING_PROFILE_START(timer, tid) Tau_start_timer(timer, 0);

/* TAU_MAPPING_PROFILE_STOP acts like TAU_PROFILE_STOP by stopping the timer */
#define TAU_MAPPING_PROFILE_STOP(tid) TAU_GLOBAL_TIMER_STOP();

#define TAU_MAPPING_PROFILE_EXIT(msg, tid)  TAU_PROFILE_EXIT(msg); 
#define TAU_MAPPING_DB_DUMP(tid)  TAU_DB_DUMP(); 
#define TAU_MAPPING_DB_PURGE(tid)  TAU_DB_PURGE(); 
#define TAU_MAPPING_PROFILE_SET_NODE(node, tid)  TAU_PROFILE_SET_NODE(node); 

#define TAU_MAPPING_PROFILE_SET_NAME(timer, name) Tau_profile_set_name(timer,name);
#define TAU_MAPPING_PROFILE_SET_TYPE(timer, name) Tau_profile_set_type(timer,name);
#define TAU_MAPPING_PROFILE_SET_GROUP(timer, id) Tau_profile_set_group(timer,id); 
#define TAU_MAPPING_PROFILE_SET_GROUP_NAME(timer, name) Tau_profile_set_group_name(timer,name);

#define TAU_MAPPING_PROFILE_GET_NAME(timer) Tau_profile_get_name(timer)
#define TAU_MAPPING_PROFILE_GET_TYPE(timer) Tau_profile_get_type(timer)
#define TAU_MAPPING_PROFILE_GET_GROUP(timer) Tau_profile_get_group(timer)
#define TAU_MAPPING_PROFILE_GET_GROUP_NAME(timer) Tau_profile_get_group_name(timer)



#else
/* Create null , except the main statement which should be executed as it is*/
#define TAU_MAPPING(stmt, group) stmt
#define TAU_MAPPING_OBJECT(FuncInfoVar) 
#define TAU_MAPPING_LINK(FuncInfoVar, Group) 
#define TAU_MAPPING_PROFILE(FuncInfoVar) 
#define TAU_MAPPING_CREATE(name, type, key, groupname, tid) 
#define TAU_MAPPING_PROFILE_TIMER(Timer, FuncInfoVar, tid)
#define TAU_MAPPING_TIMER_CREATE(t, name, type, gr, group_name)
#define TAU_MAPPING_PROFILE_START(Timer, tid) 
#define TAU_MAPPING_PROFILE_STOP(tid) 
#define TAU_MAPPING_PROFILE_EXIT(msg, tid)  
#define TAU_MAPPING_DB_DUMP(tid)
#define TAU_MAPPING_DB_PURGE(tid)
#define TAU_MAPPING_PROFILE_SET_NODE(node, tid)  
#define TAU_MAPPING_PROFILE_SET_GROUP_NAME(timer, name)
#define TAU_MAPPING_PROFILE_SET_NAME(timer, name) 
#define TAU_MAPPING_PROFILE_SET_TYPE(timer, name)
#define TAU_MAPPING_PROFILE_SET_GROUP(timer, id) 
#define TAU_MAPPING_PROFILE_GET_GROUP_NAME(timer) 
#define TAU_MAPPING_PROFILE_GET_GROUP(timer) 
#define TAU_MAPPING_PROFILE_GET_NAME(timer) 
#define TAU_MAPPING_PROFILE_GET_TYPE(timer) 

#endif /* PROFILING_ON or TRACING_ON  */
#endif /* _TAU_MAPPING_H_ */
