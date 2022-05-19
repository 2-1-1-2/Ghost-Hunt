#include "client_envoi_TCP.h"

/// FONCTION SEND AVEC GESTION DES ERREURS
/// utilisee aussi pour les requetes qui n ont pas besoins d etre formattees
/// START - UNREG - GAME? - IQUIT - GLIS?
int _send(int sock, char buff[]){
    int r=send(sock, buff, strlen(buff)*sizeof(char), 0);
    if(r==-1){
        printf("error sending request\n");
        return EXIT_FAILURE;
    }
    return EXIT_SUCCESS;
}
/// FIN

/// ENVOI NEWPL OU REGIS
int sendNEWREG(int sock, client *infoClient, char request[], int op){
    //enregistre les donnees id et port du client
    char numPartie[4];
    
}