#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netdb.h>
#include <assert.h>

int connection(int port, const char * hote);
int reception(int sock);
int games_traitement(char * mess, int taille, int sock);
int affiche_suite(int sock, int cpt_partie, int cpt_etoile_fin, int nb_games);
