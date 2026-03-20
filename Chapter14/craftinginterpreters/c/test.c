#include <stdio.h>
#include "chunk.h"

int main() {
    Chunk chunk;
    initChunk(&chunk);

    // simulate instructions on lines
    writeChunk(&chunk, 1, 10);
    writeChunk(&chunk, 2, 10);
    writeChunk(&chunk, 3, 10);
    writeChunk(&chunk, 4, 20);
    writeChunk(&chunk, 5, 20);
    writeChunk(&chunk, 6, 30);

    for (int i = 0; i < chunk.count; i++) {
        printf("Instruction %d -> line %d\n", i, getLine(&chunk, i));
    }

    freeChunk(&chunk);
    return 0;
}
