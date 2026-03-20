#ifndef clox_memory_hardcore_h
#define clox_memory_hardcore_h

#include <stddef.h>

// Initializes a single backing heap for hardcore reallocate().
// Returns false when allocation fails or size is too small.
int initMemoryHardcoreHeap(size_t heapSize);

// Optional debug helper for tests.
size_t memoryHardcoreBytesFree(void);

void* reallocate(void* pointer, size_t oldSize, size_t newSize);

#endif
