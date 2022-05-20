#include "client_commons_tcp.h"
#define OP_NEWPL 0
#define OP_REGIS 1
#define OP_LGAME 0
#define OP_LLIST 1
#define OP_LGLIS 2
#define OP_SIZE_ 3

//verifie qu'il y a 3 etoiles de fin sur les
//3 derniers caracteres
//si non, print le nombre d'etoiles manquantes
//rmq : ne verifie pas que ce qui precede est
//correct (role du serveur)
int endingOK(char request[]){
    int len=strlen(request);
    int nbStars=0;
    for (int i=0; i<3; i++){
        if(request[len-2-i]=='*') nbStars++;
    }
    if(nbStars<3){
        printf("You must end your request with [***] (brackets not included)\n");
        printf("Number of * missing : %d\n", 3-nbStars);\
        printf("(Type \\h for help)\n\n");
        return 0;
    }
    return 1;
}


int communicationBeforeStart(int sock, client *infoClient){
    int res=readReplyLists(sock, OP_LGAME, 0);
    int sentStart=0;
    while(!sentStart && res){//res==0 si erreur quelque part
        char request[100];
        char type[6];//type de la requete
        fgets(request, 100, stdin);//met deja le \0 a la fin
        //strtok(request, "\n");
        if(endingOK(request)){
            strncpy(type, request, 5);
            type[5]='\0';
            if(strcmp(type, "NEWPL")==0){
                res=sendNEWREG(sock, infoClient, request, OP_NEWPL);
                res=res && readReplyREG(sock, infoClient);
            }
            else if(strcmp(type, "REGIS")==0){
                res=sendNEWREG(sock, infoClient, request, OP_REGIS);
                res=res && readReplyREG(sock, infoClient);
            }
            else if(strcmp(type, "START")==0){
                sentStart=1;
                res=res && _send(sock, request, strlen(request));
            }
            else if(strcmp(type, "UNREG")==0){
                res=res && _send(sock, request, strlen(request));
                char reply[50];
                res=res && _read(sock, reply, -1, 1);
            }
            else if(strcmp(type, "GAME?")==0){
                res=res && _send(sock, request, strlen(request));
                res=res && readReplyLists(sock, OP_LGAME, 0);
            }
            else if(strcmp(type, "SIZE?")==0){
                res=res && sendSIZEorLIST(sock, request);
                char reply[50];
                res=res && _read(sock, reply, -1, 1);
            }
            else if(strcmp(type, "LIST?")==0){
                res=res && sendSIZEorLIST(sock, request);
                res=res && readReplyLists(sock, OP_LLIST, 0);
            }
            else{
                res=res && _send(sock, request, strlen(request));
                char reply[50];
                res=res && _read(sock, reply, -1, 1);
            }
        }
    }
    return res;
}

int communicationGame(int sock){
    int end=0;
    int res=1;
    while(!end){
        char request[100];
        char type[6];//type de la requete
        fgets(request, 100, stdin);//met deja le \0 a la fin
        if(endingOK(request)){
            /*if(strcmp(type, "UPMOV")==0 || strcmp(type, "DOMOV")==0
                || strcmp(type, "LEMOV")==0 || strcmp(type, "RIMOV")==0){
                res=res && _send(sock, request, strlen(request));
                char reply[50];
                res=res && _read(sock, reply, -1, 1);
            }
            else if(strcmp(type, "IQUIT")==0){

            }
            else if(strcmp(type, "GLIS?")==0){

            }
            else if(strcmp(type, "MALL?")==0){

            }
            else if(strcmp(type, "SEND?")==0){

            }
            else{*/
            res=res && _send(sock, request, strlen(request));
            char reply[50];
            res=res && _read(sock, reply, -1, 1);
            
            strncpy(type, reply, 5);
            type[5]='\0';
            if(strcmp(type, "GOBYE")==0) end=1;
            //}
        }
    }
    return res;
}

// 1. se connecter au serveur (main)
// communication :
// 2. lire la liste des parties en cours
// 3. while true : lire sur stdin 
//      parser la commande a envoyer au serveur
//      appeler consecutivement sendReq<requete> + readReply<requete>  
int main(int argc, char* argv[]){
    struct sockaddr_in addrsock;
    addrsock.sin_family=AF_INET;
    addrsock.sin_port=htons(6666);
    inet_aton("127.0.0.1", &addrsock.sin_addr);

    int sfd=socket(PF_INET, SOCK_STREAM, 0);
    if(sfd==-1){
        printf("error creating socket\n");
        return EXIT_FAILURE;
    }
    int r=connect(sfd, (struct sockaddr*)&addrsock, sizeof(struct sockaddr_in));
    if(r==-1){
        printf("error connecting socket\n");
        return EXIT_FAILURE;
    }

    client* infoClient=malloc(sizeof(struct client));
    infoClient->numPartie=-1;//pas encore inscrit
    int res=communicationBeforeStart(sfd, infoClient);
    
    char replyServer[100];
    partie* p=malloc(sizeof(struct partie));
    //todo : remplir les infos
    /*partie{
    int portMultD;
    char ip[16];
} partie;

    */
    res=readWelcomeAndPos(sfd, p);
    if(res!=0){//pas d'erreur, on peut continuer ?
        //TODO: abonnement multicast
        res=communicationGame(sfd);
    }
    free(infoClient);
    free(p);
    return res;
}