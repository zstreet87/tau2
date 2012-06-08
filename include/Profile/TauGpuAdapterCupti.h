#include <Profile/TauGpu.h>
#include <cuda.h>
#include <cupti.h>
#include <sstream>
#include <vector>

#if CUPTI_API_VERSION >= 2

#ifdef TAU_BFD
#define HAVE_DECL_BASENAME 1
#  if defined(HAVE_GNU_DEMANGLE) && HAVE_GNU_DEMANGLE
#    include <demangle.h>
#  endif /* HAVE_GNU_DEMANGLE */
#  include <bfd.h>
#endif /* TAU_BFD */

#define CUDA_CHECK_ERROR(err, str) \
	if (err != CUDA_SUCCESS) \
  { \
		fprintf(stderr, str); \
		exit(1); \
	} \

#define CUPTI_CHECK_ERROR(err, str) \
	if (err != CUPTI_SUCCESS) \
  { \
		fprintf(stderr, str); \
		exit(1); \
	} \

#define ACTIVITY_BUFFER_SIZE (4096 * 1024)

uint8_t *activityBuffer;
CUpti_SubscriberHandle subscriber;

int number_of_streams;
vector<int> streamIds;

void Tau_cupti_register_sync_event(CUcontext c, uint32_t stream);

void Tau_cupti_callback_dispatch(void *ud, CUpti_CallbackDomain domain, CUpti_CallbackId id, const void *params);

void Tau_cupti_record_activity(CUpti_Activity *record);

void __attribute__ ((constructor)) Tau_cupti_onload(void);
void __attribute__ ((destructor)) Tau_cupti_onunload(void);

void get_values_from_memcpy(const CUpti_CallbackData *info, CUpti_CallbackId id, CUpti_CallbackDomain domain, int &kind, int &count);

int getMemcpyType(int kind);
const char* demangleName(const char *n);

int getParentFunction(uint32_t id);

bool function_is_sync(CUpti_CallbackId id);
bool function_is_memcpy(CUpti_CallbackId id, CUpti_CallbackDomain domain);
bool function_is_launch(CUpti_CallbackId id);
bool function_is_exit(CUpti_CallbackId id);

bool registered_sync = false;

bool cupti_api_runtime();
bool cupti_api_driver();

map<uint32_t, FunctionInfo*> functionInfoMap;

class cuptiGpuId : public gpuId
{
public:
	uint32_t streamId;
	uint32_t contextId;
	uint32_t correlationId;

	cuptiGpuId(uint32_t s, uint32_t cn, uint32_t c) { streamId = s; contextId = cn ; correlationId = c; };
	cuptiGpuId *getCopy() const { 
		cuptiGpuId *c = new cuptiGpuId(*this);
		return c; 
	};
	char* printId() const {
		char *rtn = (char*) malloc(50*sizeof(char));
		sprintf(rtn, "%d/%d", streamId, correlationId);
		return rtn;
	};
	x_uint64 id_p1() const {
		return correlationId;
	};
	x_uint64 id_p2() const { 
		return RtsLayer::myNode(); 
	};

	bool less_than(const gpuId *other) const
	{
		if (contextId == ((cuptiGpuId *)other)->context()) {
			return streamId < ((cuptiGpuId *)other)->stream();
		} else {
			return contextId < ((cuptiGpuId *)other)->context();
		}
		/*
		if (ret) { printf("%s equals %s.\n", printId(), ((cuptiGpuId *)other)->printId()); }
		else { printf("%s does not equal %s.\n", printId(), ((cuptiGpuId *)other)->printId());}
		return ret;
		*/
	};

	double syncOffset() { return 0; };
	uint32_t stream() { return streamId; };
	uint32_t context() { return contextId; };
};


class cuptiRecord : public eventId {

	cuptiGpuId *device;
	const char *name;
	FunctionInfo *callingSite;

public:
	//cuptiRecord(const char* n, cuptiGpuId *id, FunctionInfo *site, TauGpuContextMap *m) : eventId(n, id, site, m)
	//{
	//};
	cuptiRecord(const char* n, uint32_t stream, uint32_t context, uint32_t correlation, TauGpuContextMap *m) : eventId(n, &cuptiGpuId(stream, context, correlation), getParentFunction(correlation), m) {};
	
	cuptiRecord(const char* n, cuptiGpuId *id, TauGpuContextMap *m) : eventId(n, id, getParentFunction(id->correlationId), m) {};

	FunctionInfo* getParentFunction(uint32_t id)
	{
		FunctionInfo *funcInfo = NULL;
		map<uint32_t, FunctionInfo*>::iterator it = functionInfoMap.find(id);
		if (it != functionInfoMap.end())
		{
			funcInfo = it->second;
		}
		return funcInfo;
	};


};

#define CAST_TO_RUNTIME_MEMCPY_TYPE_AND_CALL(name, id, info, kind, count) \
	if ((id) == CUPTI_RUNTIME_TRACE_CBID_##name##_v3020) \
	{ \
		kind = ((name##_v3020_params *) info->functionParams)->kind; \
		count = ((name##_v3020_params *) info->functionParams)->count; \
	}

#define S(x) #x
#define SX(x) S(x)
#define RECORD_DEVICE_METADATA(name, device) \
  std::ostringstream str_##name; \
	str_##name << device->name; \
	Tau_metadata("GPU " SX(name), str_##name.str().c_str()); 

#endif

