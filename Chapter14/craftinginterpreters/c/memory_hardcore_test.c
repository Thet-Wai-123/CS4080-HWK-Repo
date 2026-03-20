#include <stdio.h>
#include <stdint.h>
#include <string.h>

#include "memory_hardcore.h"

static int testAllocateAndGrow(void) {
  uint8_t* bytes = (uint8_t*)reallocate(NULL, 0, 16);
  if (bytes == NULL) return 0;

  for (int i = 0; i < 16; i++) {
    bytes[i] = (uint8_t)(i + 1);
  }

  bytes = (uint8_t*)reallocate(bytes, 16, 64);
  if (bytes == NULL) return 0;

  for (int i = 0; i < 16; i++) {
    if (bytes[i] != (uint8_t)(i + 1)) {
      return 0;
    }
  }

  reallocate(bytes, 64, 0);
  return 1;
}

static int testShrink(void) {
  char* text = (char*)reallocate(NULL, 0, 64);
  if (text == NULL) return 0;

  strcpy(text, "hello-hardcore-allocator");
  text = (char*)reallocate(text, 64, 8);
  if (text == NULL) return 0;

  if (strncmp(text, "hello-ha", 8) != 0) {
    return 0;
  }

  reallocate(text, 8, 0);
  return 1;
}

static int testReuseFreedSpace(void) {
  void* a = reallocate(NULL, 0, 128);
  void* b = reallocate(NULL, 0, 128);
  if (a == NULL || b == NULL) return 0;

  reallocate(a, 128, 0);

  void* c = reallocate(NULL, 0, 64);
  if (c == NULL) return 0;

  reallocate(b, 128, 0);
  reallocate(c, 64, 0);
  return 1;
}

int main(void) {
  if (!initMemoryHardcoreHeap(1024 * 1024)) {
    printf("init failed\n");
    return 1;
  }

  int ok1 = testAllocateAndGrow();
  int ok2 = testShrink();
  int ok3 = testReuseFreedSpace();

  if (ok1 && ok2 && ok3) {
    printf("memory_hardcore tests passed, free bytes: %zu\n", memoryHardcoreBytesFree());
    return 0;
  }

  printf("memory_hardcore tests failed (grow=%d shrink=%d reuse=%d)\n", ok1, ok2, ok3);
  return 1;
}
