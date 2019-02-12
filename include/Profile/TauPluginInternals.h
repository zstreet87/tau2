/****************************************************************************
 * **                      TAU Portable Profiling Package                     **
 * **                      http://www.cs.uoregon.edu/research/tau             **
 * *****************************************************************************
 * **    Copyright 1997-2017                                                  **
 * **    Department of Computer and Information Science, University of Oregon **
 * **    Advanced Computing Laboratory, Los Alamos National Laboratory        **
 * ****************************************************************************/
/******************************************************************************
 * **      File            : TauPluginInternals.h                             **
 * **      Description     : Internal API for the TAU Plugin System           **
 * **      Contact         : sramesh@cs.uoregon.edu                           **
 * **      Documentation   : See http://www.cs.uoregon.edu/research/tau       **
 * ***************************************************************************/

#ifndef _TAU_PLUGIN_INTERNALS_H_
#define _TAU_PLUGIN_INTERNALS_H_

#include <Profile/TauPluginTypes.h>

#include <map>
#include <set>
#include <string>

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

#define TAU_PLUGIN_INIT_FUNC "Tau_plugin_init_func"

class PluginKey {
   public:
   int plugin_event;
   size_t specific_event_hash;

   PluginKey(int _plugin_event, size_t _specific_event_hash) {
     plugin_event = _plugin_event;
     specific_event_hash = _specific_event_hash;
   }
   
   bool operator< (const PluginKey &rhs) const {
     if(plugin_event != rhs.plugin_event) return plugin_event < rhs.plugin_event;
     else return specific_event_hash < rhs.specific_event_hash;
   }
   
   ~PluginKey() { }
};

int Tau_initialize_plugin_system();
int Tau_util_load_and_register_plugins(PluginManager_t* plugin_manager);
void* Tau_util_load_plugin(const char *name, const char *path, PluginManager_t* plugin_manager);
void* Tau_util_register_plugin(const char *name, char **args, int num_args, void* handle, PluginManager_t* plugin_manager, unsigned int plugin_id);

int Tau_util_cleanup_all_plugins();

PluginManager_t* Tau_util_get_plugin_manager();
void Tau_util_invoke_callbacks(Tau_plugin_event_t event, const char * specific_event_name, const void * data);
void Tau_util_invoke_callbacks_for_trigger_event(Tau_plugin_event_t event, size_t hash, const void * data);

extern Tau_plugin_callbacks_active_t Tau_plugins_enabled;
extern std::map<PluginKey, std::set<unsigned int> > plugins_for_named_specific_event;

#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif /* _TAU_PLUGIN_INTERNALS_H_ */

