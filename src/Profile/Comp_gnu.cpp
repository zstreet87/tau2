/**
 * VampirTrace
 * http://www.tu-dresden.de/zih/vampirtrace
 *
 * Copyright (c) 2005-2008, ZIH, TU Dresden, Federal Republic of Germany
 *
 * Copyright (c) 1998-2005, Forschungszentrum Juelich GmbH, Federal
 * Republic of Germany
 *
 * See the file COPYRIGHT in the package base directory for details
 **/

/*****************************************************************************
 **			TAU Portable Profiling Package			    **
 **			http://www.cs.uoregon.edu/research/tau	            **
 *****************************************************************************
 **    Copyright 2008  						   	    **
 **    Department of Computer and Information Science, University of Oregon **
 **    Advanced Computing Laboratory, Los Alamos National Laboratory        **
 ****************************************************************************/
/*****************************************************************************
 **	File 		: Comp_gnu.cpp  				    **
 **	Description 	: TAU Profiling Package				    **
 **	Contact		: tau-bugs@cs.uoregon.edu               	    **
 **	Documentation	: See http://www.cs.uoregon.edu/research/tau        **
 **                                                                         **
 **      Description     : This file contains the hooks for GNU based       **
 **                        compiler instrumentation                         **
 **                                                                         **
 *****************************************************************************/

#ifndef TAU_XLC

#include <TAU.h>
#include <Profile/TauInit.h>

#include <vector>

#include <tau_internal.h>

#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
// #include <dirent.h>
#include <sys/types.h>
#include <unistd.h>
#include <errno.h>
#ifdef TAU_OPENMP
#  include <omp.h>
#endif /* TAU_OPENMP */

#include <Profile/TauBfd.h>
#include <Profile/TauInit.h>

#ifdef __APPLE__
#include <mach-o/dyld.h>
#endif /* __APPLE__ */

using namespace std;

/*
 *-----------------------------------------------------------------------------
 * Simple hash table to map function addresses to region names/identifier
 *-----------------------------------------------------------------------------
 */

struct HashNode
{
  HashNode() : fi(NULL), excluded(false)
  { }

  TauBfdInfo info;		///< Filename, line number, etc.
  FunctionInfo * fi;		///< Function profile information
  bool excluded;			///< Is function excluded from profiling?
};

struct HashTable : public TAU_HASH_MAP<unsigned long, HashNode*>
{
  HashTable() {
    Tau_init_initializeTAU();
  }
  virtual ~HashTable() {
    Tau_destructor_trigger();
  }
};

static HashTable & TheHashTable()
{
  static HashTable htab;
  return htab;
}

static tau_bfd_handle_t & TheBfdUnitHandle()
{
  static tau_bfd_handle_t bfdUnitHandle = TAU_BFD_NULL_HANDLE;
  if (bfdUnitHandle == TAU_BFD_NULL_HANDLE) {
    RtsLayer::LockEnv();
    if (bfdUnitHandle == TAU_BFD_NULL_HANDLE) {
      bfdUnitHandle = Tau_bfd_registerUnit();
    }
    RtsLayer::UnLockEnv();
  }
  return bfdUnitHandle;
}

/*
 * Get symbol table by using BFD
 */
static void issueBfdWarningIfNecessary()
{
#ifndef TAU_BFD
  static bool warningIssued = false;
  if (!warningIssued) {
    fprintf(stderr,"TAU Warning: Comp_gnu - "
        "BFD is not available during TAU build. Symbols may not be resolved!\n");
    fflush(stderr);
    warningIssued = true;
  }
#endif
}

bool isExcluded(char const * funcname)
{
  return funcname && (
      // Intel compiler static initializer
      (strcmp(funcname, "__sti__$E") == 0)
      // Tau Profile wrappers
      || strstr(funcname, "Tau_Profile_Wrapper"));
}

void updateHashTable(unsigned long addr, const char *funcname)
{
  HashNode * hn = TheHashTable()[addr];
  if (!hn) {
    RtsLayer::LockDB();
    hn = TheHashTable()[addr];
    if (!hn) {
      hn = new HashNode;
      TheHashTable()[addr] = hn;
    }
    RtsLayer::UnLockDB();
  }
  hn->info.funcname = funcname;
  hn->excluded = isExcluded(funcname);
}

static int executionFinished = 0;
void runOnExit()
{
  executionFinished = 1;
  Tau_destructor_trigger();
}

//
// Instrumentation callback functions
//
extern "C"
{

// Prevent accidental instrumentation of the instrumentation functions
// It's highly unlikely because you'd have to compile TAU with
// -finstrument-functions, but better safe than sorry.

__attribute__((no_instrument_function))
void __cyg_profile_func_enter(void*, void*);

__attribute__((no_instrument_function))
void _cyg_profile_func_enter(void*, void*);

__attribute__((no_instrument_function))
void __pat_tp_func_entry(const void *, const void *);

__attribute__((no_instrument_function))
void ___cyg_profile_func_enter(void*, void*);

__attribute__((no_instrument_function))
void __cyg_profile_func_exit(void*, void*);

__attribute__((no_instrument_function))
void _cyg_profile_func_exit(void*, void*);

__attribute__((no_instrument_function))
void ___cyg_profile_func_exit(void*, void*);

__attribute__((no_instrument_function))
void __pat_tp_func_return(const void *ea, const void *ra);

__attribute__((no_instrument_function))
void profile_func_enter(void*, void*);

__attribute__((no_instrument_function))
void profile_func_exit(void*, void*);


#if (defined(TAU_SICORTEX) || defined(TAU_SCOREP))
#pragma weak __cyg_profile_func_enter
#endif /* SICORTEX || TAU_SCOREP */
void __cyg_profile_func_enter(void* func, void* callsite)
{
  static bool gnu_init = true;

  // These checks must be done before anything else.

  // Don't profile if we're done executing.
  if (executionFinished) return;

  // Don't profile if we're still initializing.
  if (Tau_init_initializingTAU()) return;

  // Protect TAU from itself.  This MUST occur here before we query the TID or
  // use the hash table.  Any later and TAU's memory wrapper will profile TAU
  // and crash or deadlock.
  { // BEGIN inside TAU

    // Both Sampling and Memory wrapper require us to protect TAU immediately upon
    // entering the compiler wrappers. Sampling because it can interrupt the
    // application anywhere and memory because the hash table lookup allocates memory.
    TauInternalFunctionGuard protects_this_function(
        TauEnv_get_ebs_enabled() || Tau_memory_wrapper_is_registered());

    void * funcptr = func;
#ifdef __ia64__
    funcptr = *( void ** )func;
#endif

    unsigned long addr = Tau_convert_ptr_to_unsigned_long(funcptr);

    HashNode * node = TheHashTable()[addr];
    if (!node) {
      RtsLayer::LockDB();
      node = TheHashTable()[addr];
      if (!node) {
        node = new HashNode;
        TheHashTable()[addr] = node;
      }
      RtsLayer::UnLockDB();
    }
    HashNode & hn = *node;

    // Skip excluded functions
    if (hn.excluded) return;
  
    // Don't profile TAU internals
    // Note that this also prevents reentrency into this routine.
    if (Tau_global_get_insideTAU() > 0) {
      return;
    }

    { // BEGIN inside TAU
      TauInternalFunctionGuard protects_this_function;
      
      // Get BFD handle
      tau_bfd_handle_t & bfdUnitHandle = TheBfdUnitHandle();

      if (gnu_init) {
        gnu_init = false;

        Tau_init_initializeTAU();

        issueBfdWarningIfNecessary();

        // Create hashtable entries for all symbols in the executable
        // via a fast scan of the executable's symbol table.
        // It makes sense to load the entire symbol table because all
        // symbols in the executable are likely to be encountered
        // during the run
        Tau_bfd_processBfdExecInfo(bfdUnitHandle, updateHashTable);

        TheUsingCompInst() = 1;

        // For UPC: Initialize the node
        if (RtsLayer::myNode() == -1) {
          TAU_PROFILE_SET_NODE(0);
        }

        // we register this here at the end so that it is called
        // before the VT objects are destroyed.  Objects are destroyed and atexit targets are
        // called in the opposite order in which they are created and registered.
        // Note: This doesn't work work VT with MPI, they re-register their atexit routine
        //       During MPI_Init.
        atexit(runOnExit);
      }

      // Start the timer if it's not an excluded function
      if (!hn.fi) {
        RtsLayer::LockDB();    // lock, then check again
        if (!hn.fi) {
          // Resolve function info if it hasn't already been retrieved
          if (!hn.info.probeAddr) {
            Tau_bfd_resolveBfdInfo(bfdUnitHandle, addr, hn.info);
          }

          //Do not profile this routine, causes crashes with the intel compilers.
          if (isExcluded(hn.info.funcname)) {
            hn.excluded = true;
          }

          // Build routine name for TAU function info
          unsigned int size = strlen(hn.info.funcname) + strlen(hn.info.filename) + 128;
          char * routine = (char*)malloc(size);
          sprintf(routine, "%s [{%s} {%d,0}]", hn.info.funcname, hn.info.filename, hn.info.lineno);

          // Create function info
          void *handle = NULL;
          TAU_PROFILER_CREATE(handle, routine, "", TAU_DEFAULT);
          hn.fi = (FunctionInfo*)handle;

          // Cleanup
          free((void*)routine);
        }
        RtsLayer::UnLockDB();
      }

      if (!hn.excluded) {
        //GNU has some internal routines that occur before main in entered. To
        //ensure that a single top-level timer is present start the dummy '.TAU
        //application' timer. -SB
        Tau_create_top_level_timer_if_necessary();
        Tau_start_timer(hn.fi, 0, Tau_get_tid());
      }

      if (!(hn.fi->GetProfileGroup() & RtsLayer::TheProfileMask())) {
        //printf("COMP_GNU >>>>>>>>>> Excluding: %s, addr: %d, throttled.\n", hn.fi->GetName(), addr);
        hn.excluded = true;
      }
    }    // END inside TAU (late check)
  }    // END inside TAU (early check)
}

void _cyg_profile_func_enter(void* func, void* callsite)
{
  __cyg_profile_func_enter(func, callsite);
}

void __pat_tp_func_entry(const void *ea, const void *ra)
{
  __cyg_profile_func_enter((void *)ea, (void *)ra);

}

void profile_func_enter(void* func, void* callsite)
{
  __cyg_profile_func_enter(func, callsite);
}

void ___cyg_profile_func_enter(void* func, void* callsite)
{
  __cyg_profile_func_enter(func, callsite);
}

#if (defined(TAU_SICORTEX) || defined(TAU_SCOREP))
#pragma weak __cyg_profile_func_exit
#endif /* SICORTEX || TAU_SCOREP */
void __cyg_profile_func_exit(void* func, void* callsite)
{
  // These checks must be done before anything else.

  // Don't profile if we're done executing.
  if (executionFinished) return;

  // Don't profile if we're still initializing.
  if (Tau_init_initializingTAU()) return;


  // Protect TAU from itself.  This MUST occur here before we query the TID or
  // use the hash table.  Any later and TAU's memory wrapper will profile TAU
  // and crash or deadlock.
  // Note that this also prevents reentrency into this routine.
  {
    if (TauEnv_get_ebs_enabled() || Tau_memory_wrapper_is_registered())
    {
      TauInternalFunctionGuard protects_this_function;
    }

    void * funcptr = func;
#ifdef __ia64__
    funcptr = *( void ** )func;
#endif
    unsigned long addr = Tau_convert_ptr_to_unsigned_long(funcptr);

    HashNode * hn = TheHashTable()[addr];
    if (hn && !hn->excluded && hn->fi) {
      // Don't profile TAU internals
      if (Tau_global_get_insideTAU() > 0) return;
      TauInternalFunctionGuard protects_this_function;
      issueBfdWarningIfNecessary();
      Tau_stop_timer(hn->fi, Tau_get_tid());
    }
  }    // END inside TAU
}

void _cyg_profile_func_exit(void* func, void* callsite)
{
  __cyg_profile_func_exit(func, callsite);
}

void ___cyg_profile_func_exit(void* func, void* callsite)
{
  __cyg_profile_func_exit(func, callsite);
}

void profile_func_exit(void* func, void* callsite)
{
  __cyg_profile_func_exit(func, callsite);
}

void __pat_tp_func_return(const void *ea, const void *ra)
{
  __cyg_profile_func_exit((void *)ea, (void *)ra);
}

}    // extern "C"

#endif /* TAU_XLC */
