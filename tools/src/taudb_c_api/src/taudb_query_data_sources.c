#include "taudb_api.h"
#include "libpq-fe.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

TAUDB_DATA_SOURCE* taudb_query_data_sources(TAUDB_CONNECTION* connection) {
#ifdef TAUDB_DEBUG_DEBUG
  printf("Calling taudb_query_data_sources()\n");
#endif
  void *res;
  int nFields;
  int i, j;

  //if the connection already has the data, return it.
  if (connection->data_sources != NULL && connection->data_source_count > 0) {
    taudb_numItems = connection->data_source_count;
    return connection->data_sources;
  }

  char my_query[256];
  sprintf(my_query,"select * from data_source");
#ifdef TAUDB_DEBUG
  printf("Query: %s\n", my_query);
#endif
  res = taudb_execute_query(connection, my_query);

  int nRows = taudb_get_num_rows(res);
  taudb_numItems = nRows;

  TAUDB_DATA_SOURCE* data_sources = taudb_create_data_sources(taudb_numItems);

  nFields = taudb_get_num_columns(res);

  /* the rows */
  for (i = 0; i < nRows; i++)
  {
    TAUDB_DATA_SOURCE* data_source = &(data_sources[i]);
    /* the columns */
    for (j = 0; j < nFields; j++) {
	  if (strcmp(taudb_get_column_name(res, j), "id") == 0) {
	    data_source->id = atoi(taudb_get_value(res, i, j));
	  } else if (strcmp(taudb_get_column_name(res, j), "name") == 0) {
	    data_source->name = taudb_create_and_copy_string(taudb_get_value(res,i,j));
	  } else if (strcmp(taudb_get_column_name(res, j), "description") == 0) {
	    data_source->description = taudb_create_and_copy_string(taudb_get_value(res, i, j));
	  } else {
	    printf("Error: unknown column '%s'\n", taudb_get_column_name(res, j));
	    taudb_exit_nicely(connection);
	  }
	} 
	HASH_ADD_INT(data_sources, id, &(data_sources[i]));
  }

  taudb_clear_result(res);
  taudb_close_transaction(res);

  return (data_sources);
}

TAUDB_DATA_SOURCE* taudb_get_data_source(TAUDB_DATA_SOURCE* data_sources, const int id) {
#ifdef TAUDB_DEBUG_DEBUG
  printf("Calling taudb_get_data_source(%p,%d)\n", data_sources, id);
#endif
  if (data_sources == NULL) {
    fprintf(stderr, "Error: data_source parameter null. Please provide a valid set of data_sources.\n");
    return NULL;
  }
  if (id == 0) {
    fprintf(stderr, "Error: name parameter null. Please provide a valid name.\n");
    return NULL;
  }

  TAUDB_DATA_SOURCE* data_source = NULL;
  HASH_FIND_INT(data_sources, &(id), data_source);
  return data_source;
}

