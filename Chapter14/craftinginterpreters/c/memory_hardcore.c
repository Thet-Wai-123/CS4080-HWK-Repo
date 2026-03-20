#include "memory_hardcore.h"

#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#define HARDCORE_DEFAULT_HEAP_BYTES (1024u * 1024u * 16u)

typedef struct BlockHeader {
	size_t payloadSize;
	int isFree;
	struct BlockHeader* next;
	struct BlockHeader* prev;
} BlockHeader;

static void* gHeapStart = NULL;
static size_t gHeapSize = 0;
static BlockHeader* gFirstBlock = NULL;

static size_t alignUp(size_t value) {
	const size_t alignment = sizeof(max_align_t);
	return (value + alignment - 1) & ~(alignment - 1);
}

static size_t headerSize(void) {
	return alignUp(sizeof(BlockHeader));
}

static void splitBlock(BlockHeader* block, size_t desiredPayload) {
	const size_t hdr = headerSize();
	if (block->payloadSize <= desiredPayload + hdr + sizeof(max_align_t)) {
		return;
	}

	uint8_t* raw = (uint8_t*)block;
	BlockHeader* newBlock = (BlockHeader*)(raw + hdr + desiredPayload);
	newBlock->payloadSize = block->payloadSize - desiredPayload - hdr;
	newBlock->isFree = 1;
	newBlock->prev = block;
	newBlock->next = block->next;

	if (block->next != NULL) {
		block->next->prev = newBlock;
	}

	block->next = newBlock;
	block->payloadSize = desiredPayload;
}

static void mergeWithNext(BlockHeader* block) {
	const size_t hdr = headerSize();
	BlockHeader* next = block->next;

	if (next == NULL || !next->isFree) {
		return;
	}

	block->payloadSize += hdr + next->payloadSize;
	block->next = next->next;
	if (next->next != NULL) {
		next->next->prev = block;
	}
}

static BlockHeader* findFreeBlock(size_t payloadSize) {
	BlockHeader* cursor = gFirstBlock;
	while (cursor != NULL) {
		if (cursor->isFree && cursor->payloadSize >= payloadSize) {
			return cursor;
		}
		cursor = cursor->next;
	}
	return NULL;
}

static void* payloadFromBlock(BlockHeader* block) {
	return (void*)((uint8_t*)block + headerSize());
}

static BlockHeader* blockFromPayload(void* payload) {
	return (BlockHeader*)((uint8_t*)payload - headerSize());
}

int initMemoryHardcoreHeap(size_t heapSize) {
	const size_t hdr = headerSize();
	if (gHeapStart != NULL) {
		return 1;
	}

	heapSize = alignUp(heapSize);
	if (heapSize <= hdr + sizeof(max_align_t)) {
		return 0;
	}

	gHeapStart = malloc(heapSize);
	if (gHeapStart == NULL) {
		return 0;
	}

	gHeapSize = heapSize;
	gFirstBlock = (BlockHeader*)gHeapStart;
	gFirstBlock->payloadSize = heapSize - hdr;
	gFirstBlock->isFree = 1;
	gFirstBlock->next = NULL;
	gFirstBlock->prev = NULL;
	return 1;
}

size_t memoryHardcoreBytesFree(void) {
	size_t freeBytes = 0;
	BlockHeader* cursor = gFirstBlock;
	while (cursor != NULL) {
		if (cursor->isFree) {
			freeBytes += cursor->payloadSize;
		}
		cursor = cursor->next;
	}
	return freeBytes;
}

void* reallocate(void* pointer, size_t oldSize, size_t newSize) {
	(void)oldSize;

	if (gHeapStart == NULL) {
		if (!initMemoryHardcoreHeap(HARDCORE_DEFAULT_HEAP_BYTES)) {
			return NULL;
		}
	}

	if (newSize == 0) {
		if (pointer == NULL) {
			return NULL;
		}

		BlockHeader* block = blockFromPayload(pointer);
		block->isFree = 1;

		mergeWithNext(block);
		if (block->prev != NULL && block->prev->isFree) {
			mergeWithNext(block->prev);
		}
		return NULL;
	}

	size_t alignedNewSize = alignUp(newSize);

	if (pointer == NULL) {
		BlockHeader* freeBlock = findFreeBlock(alignedNewSize);
		if (freeBlock == NULL) {
			return NULL;
		}

		splitBlock(freeBlock, alignedNewSize);
		freeBlock->isFree = 0;
		return payloadFromBlock(freeBlock);
	}

	BlockHeader* block = blockFromPayload(pointer);
	if (block->payloadSize >= alignedNewSize) {
		splitBlock(block, alignedNewSize);
		return pointer;
	}

	if (block->next != NULL && block->next->isFree) {
		const size_t total = block->payloadSize + headerSize() + block->next->payloadSize;
		if (total >= alignedNewSize) {
			mergeWithNext(block);
			splitBlock(block, alignedNewSize);
			return pointer;
		}
	}

	BlockHeader* newBlock = findFreeBlock(alignedNewSize);
	if (newBlock == NULL) {
		return NULL;
	}

	splitBlock(newBlock, alignedNewSize);
	newBlock->isFree = 0;

	void* newPointer = payloadFromBlock(newBlock);
	memcpy(newPointer, pointer, block->payloadSize);

	block->isFree = 1;
	mergeWithNext(block);
	if (block->prev != NULL && block->prev->isFree) {
		mergeWithNext(block->prev);
	}

	return newPointer;
}
