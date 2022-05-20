#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netdb.h>
#include <time.h>

typedef struct client{
    char id[9];
    short portUDP;
    uint8_t numPartie;
} client;

int _send(int sock, void* buff, size_t n);
int sendNEWREG(int sock, client *infoClient, char request[], int op);
int sendSIZEorLIST(int sock, char request[]);

int _read(int sock, char* buffer, int len, int affiche);
int readReplyREG(int sock, client *infoClient);
int readReplyLists(int sock, int op, int isAuto);
int readReplySIZE(int sock);