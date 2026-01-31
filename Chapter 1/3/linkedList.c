#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Doubly linked list node
typedef struct Node {
    char* data;
    struct Node* next;
    struct Node* prev;
} Node;

// Create a new node with heap-allocated string
Node* createNode(const char* str) {
    Node* node = malloc(sizeof(Node));
    if (!node) return NULL;
    
    node->data = malloc(strlen(str) + 1);
    if (!node->data) {
        free(node);
        return NULL;
    }
    
    strcpy(node->data, str);
    node->next = NULL;
    node->prev = NULL;
    return node;
}

// Insert at the beginning of the list
Node* insert(Node** head, const char* str) {
    Node* newNode = createNode(str);
    if (!newNode) return *head;
    
    if (*head) {
        (*head)->prev = newNode;
        newNode->next = *head;
    }
    *head = newNode;
    return *head;
}

// Find a node by string value
Node* find(Node* head, const char* str) {
    Node* current = head;
    while (current) {
        if (strcmp(current->data, str) == 0) {
            return current;
        }
        current = current->next;
    }
    return NULL;
}

// Delete a node by string value
Node* delete(Node** head, const char* str) {
    Node* node = find(*head, str);
    if (!node) return *head;
    
    // Update links
    if (node->prev) {
        node->prev->next = node->next;
    } else {
        *head = node->next;  // Deleting head
    }
    
    if (node->next) {
        node->next->prev = node->prev;
    }
    
    // Free memory
    free(node->data);
    free(node);
    return *head;
}

// Print the list forward
void printList(Node* head) {
    Node* current = head;
    while (current) {
        printf("%s", current->data);
        if (current->next) printf(" <-> ");
        current = current->next;
    }
    printf("\n");
}

// Simple test cases
int main() {
    Node* head = NULL;
    
    printf("Doubly Linked List\n");
    
    // Test insert
    printf("1. Inserting: one, two, three\n");
    head = insert(&head, "one");
    head = insert(&head, "two");
    head = insert(&head, "three");
    printList(head);
    
    // Test find
    printf("\n2. Finding 'two': ");
    Node* found = find(head, "two");
    printf("%s\n", found ? "Found" : "Not found");
    
    // Test delete
    printf("\n3. Deleting 'two'\n");
    head = delete(&head, "two");
    printList(head);
    
    printf("\n4. Trying to delete 'four' (doesn't exist)\n");
    head = delete(&head, "four");
    printList(head);

    return 0;
}
