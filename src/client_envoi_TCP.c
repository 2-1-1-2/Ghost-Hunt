#include "client_envoi_TCP.h"

/// FONCTION SEND AVEC GESTION DES ERREURS
/// utilisee aussi pour les requetes qui n ont pas besoins d etre formattees
/// START - UNREG - GAME? - IQUIT - GLIS?
int _send(int sock, void* buff, size_t n){
    int r=send(sock, buff, n, 0);
    if(r==-1){
        printf("error sending request\n");
        return EXIT_FAILURE;
    }
    return EXIT_SUCCESS;
}
/// FIN

/// ENVOI NEWPL OU REGIS
/// rmq : ici pas de test REGOK/NO, on enregistre dans tous
/// les cas et reinitialisera plus tard si regno
int sendNEWREG(int sock, client *infoClient, char request[], int op){
    //enregistre les donnees id et port du client
    for(int i=0; i<8; i++)
        (infoClient->id)[i]=request[i+6];
    infoClient->portUDP=htons(atoi(&request[15]));
    if(op==1){//REGIS : num de la partie en +
        infoClient->numPartie=(uint8_t)atoi(&request[20]);
    }
    return _send(sock, request, strlen(request));
}

/// ENVOI SIZE? OU LIST? => reformattage pour envoyer m 
/// en tant qu'1 octet
int sendSIZEorLIST(int sock, char request[]){
    char tmp[7];
    strncpy(tmp, request, 6);
    tmp[6]='\0';
    int res=_send(sock, tmp, strlen(tmp));//envoi du type de la requete
    uint8_t m=atoi(&request[6]);
    res=res && _send(sock, &m, sizeof(uint8_t));
    res=res && _send(sock, "***", 3);
    return res;
}


