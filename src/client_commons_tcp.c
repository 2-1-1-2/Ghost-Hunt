#include "client_commons_tcp.h"

// FONCTION SEND AVEC GESTION DES ERREURS
/// utilisee aussi pour les requetes qui n ont pas besoins d etre formattees
/// START - UNREG - GAME? - IQUIT - GLIS?
int _send(int sock, void* buff, size_t n){
    int r=send(sock, buff, n, 0);
    if(r==-1){
        printf("error sending request\n");
        return 0;
    }
    return 1;
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

/// FONCTION RECV AVEC GESTION DES ERREURS
int _read(int sock, char* buffer, int len, int affiche){
    int nbStars=0, i=0;
    if(len==-1){
        while(nbStars!=3){
            int r=recv(sock, &buffer[i], 1, 0);
            if(r==-1){
                printf("error reading socket\n");
                return 0;
            }
            if(buffer[i]=='*') nbStars++;
            else nbStars=0;
            i++;
        }
    }
    else{
        while(i!=len){
            int r=recv(sock, &buffer[i], len, 0);
            if(r==-1){
                printf("error reading socket\n");
                return 0;
            }
            i+=r;
        }
    }
    buffer[i]='\0';
    if(affiche==1) printf("%s\n", buffer);
    return 1;
}

/// RECEPTION REGNO OU REGOK
int readReplyREG(int sock, client *infoClient){
    printf("readREG\n");
    char reply[50];
    int res=_read(sock, reply, -1, 1);
    if(res!=0){
        char type[6];
        strncpy(type, reply, 5);
        type[5]='\0';
        if(strcmp(type, "REGOK")==0)
            infoClient->numPartie=(uint8_t)atoi(&reply[6]);
    }
    return res;
}

/// RECEPTION DE LISTES (GAMES - GLIS? - LIST?)
int readReplyLists(int sock, int op, int isAuto){
    char list[50];
    int res=_read(sock, list, -1, 1);
    if(res==0) return res;
    uint8_t nbIterations=(uint8_t)atoi(&list[6]);
    if(op==0){//GAMES
        uint8_t tabGames[nbIterations*sizeof(uint8_t)];
        for(int i=0; i<nbIterations; i++){
            char tmp[11+2*sizeof(uint8_t)];
            res=_read(sock, tmp, -1, 1);
            if(res==0) return res;
            tabGames[i]=(uint8_t)atoi(&tmp[6]);
        }
        if(isAuto){
            srand(time(NULL));
            return tabGames[rand()%nbIterations];
        }
    }
    else{
        if(op==1) nbIterations=(uint8_t)atoi(&list[8]);
        for(int i=0; i<nbIterations; i++){
            char tmp[50];
            res=_read(sock, tmp, -1, 1);
            if(res==0) return res;
        }
    }
    return res;
}

int readWelcomeAndPos(int sock, partie* p){
    char buff[150];
    int res=_read(sock, buff, -1, 1);//welco
    if(res==0) return res;
    int i=0;
    int offset=10+2*sizeof(uint8_t)+2*sizeof(uint16_t);
    while(buff[offset+i]!='#'){
        (p->ip)[i]=buff[offset+i];
        i++;
    }
    (p->ip)[i]='\0';
    p->portMultD=atoi(&buff[26+2*sizeof(uint8_t)+2*sizeof(uint16_t)]);
    
    //TODO: abonnement multicast -> a verifier si c'est bon
    int ok=1;
    int r=setsockopt(sock, SOL_SOCKET, SO_REUSEPORT, &ok, sizeof(ok));
    if(r==-1) return 0;
    
    struct sockaddr_in address_sock;
    address_sock.sin_family=AF_INET;
    address_sock.sin_port=htons(p->portMultiD);
    address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
    r=bind(sock, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));
    if(r==-1) return 0;
    
    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr=inet_addr(p->ip);
    mreq.imr_interface.s_addr=htonl(INADDR_ANY);
    r=setsockopt(sock, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq));
    if(r==-1) return 0;
    
    res=_read(sock, buff, -1, 1);//posit
    return res;
}

//TODO: read UDP

//TODO: recv multicast