#include <stdint.h>

typedef struct client{
    char id[8];
    short portUDP;
    uint8_t numPartie;
} client;
