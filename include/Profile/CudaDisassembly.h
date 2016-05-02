#include <stdio.h>
#include <iostream>
#include <string>
#include <sstream>
#include <vector>
#include <map>
#include <set>

#ifdef TAU_BFD
#define HAVE_DECL_BASENAME 1
#  if defined(HAVE_GNU_DEMANGLE) && HAVE_GNU_DEMANGLE
#    include <demangle.h>
#  endif /* HAVE_GNU_DEMANGLE */
#  include <bfd.h>
#endif /* TAU_BFD */

using namespace std;

// Look up ops
enum InstructionMix { FloatingPoint = 0, Integer = 1, Conversion = 2, Move = 3, Predicate = 4, 
		      Texture = 5, LoadStore = 6, Surface = 7, Control = 8, SIMD = 9, Misc = 10};

static std::set<std::string> s_FP;
static std::set<std::string> s_Int;
static std::set<std::string> s_Conv;
static std::set<std::string> s_Move;
static std::set<std::string> s_Pred;
static std::set<std::string> s_Tex;
static std::set<std::string> s_LdSt;
static std::set<std::string> s_Surf;
static std::set<std::string> s_Ctrl;
static std::set<std::string> s_SIMD;
static std::set<std::string> s_Misc;

static bool init_instruction = false;

struct ImixStats {
public:
  std::string kernel;
  int flops_raw;
  int ctrlops_raw;
  int memops_raw;
  int totops_raw;
  double flops_pct;
  double ctrlops_pct;
  double memops_pct;
};

class CudaOps 
{
 public:
  std::string kernel;
  std::string filename;
  std::string lineno;
  std::string instruction;
  std::string pcoffset;
  int deviceid;
 CudaOps(std::string krnl, std::string fname, std::string lno, std::string instr, std::string pc, int deviceId) : \
  kernel(krnl), filename(fname), lineno(lno), instruction(instr), pcoffset(pc), deviceid(deviceId) { }
  ~CudaOps();

};

static std::vector<CudaOps*> v_cudaOps;

CudaOps::~CudaOps()
{
  for (int i = 0; i < v_cudaOps.size(); i++)
    delete v_cudaOps[i];
  v_cudaOps.clear();
  s_FP.clear();
  s_Int.clear();
  s_Conv.clear();
  s_Move.clear();
  s_Pred.clear();
  s_Tex.clear();
  s_LdSt.clear();
  s_Surf.clear();
  s_Ctrl.clear();
  s_SIMD.clear();
  s_Misc.clear();
}

std::vector<std::string> get_disassem_from_out(std::string cmd);

void parse_disassem(std::vector<std::string> vec, int device_id);

std::map<std::string, ImixStats> print_instruction_mixes(char* cubin_file, int device_id, FILE* fp_imix_out);

std::vector<std::string> &split(const std::string &s, char delim, std::vector<std::string> &elems);

std::vector<std::string> split(const std::string &s, char delim);

void print_vector(std::vector<std::string> vec);

std::map<std::string, ImixStats> write_disassem(FILE* fp_imix_out);

void init_instruction_set();

int get_instruction_mix_category(string instr);

void insert_instructions(std::set<std::string> *s_set, std::string Ins[], int size);

std::string sanitize_instruction(std::string instr);

const char *demangleName(const char* name);
