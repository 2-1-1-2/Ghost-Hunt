#include "client_fonction_tcp.h"
#include "client.h"

    /* ----- rappel taille en octet -----*/
    //* n      : nombre de partie               : 1 octet - uint8 
    //* m      : numéro de partie               : 1 octet - uint8 
    //* s      : nombre d'inscription partie    : 1 octet - uint8
    //* id     : identifiant utilisateur        : char de taille 8   
    //* port   : port                           : char de taille 4 
    //* h      : hauteur du labyrinthe < 1000   : 2 octet - uint16 (little end) 
    //* w      : largeur du labyrinthe < 1000   : 2 octet - uint16 (little end) 
    //* f      : nombre de fantôme              : 1 octet - uint8  
    //* ip     : adresse IPv4 (complété avec #) : 15 octet - char de taille 15
    //* x      : n° de ligne (complété avec 0)  : 3 octet - char de taille 3
    //* y      : n° de col (complété avec 0)    : 3 octet - char de taille 3
    //* d      : n° de dist (complété avec 0)   : 3 octet - char de taille 3
    //* p      : point (complété avec 0)        : char de taille 4
    //* mess   : message                        : < 200 char

int main(int argc, char const *argv[]){
    /*--- connection au server - TCP ---*/
    /*int port=6666;
    if(argc==2) port=atoi(argv[1]);

    //TODO: a partir de la :)
    int sock_tcp = connection(port, "localhost");

    if(sock_tcp == -1){
        perror("sock tcp\n");
        exit(1);
    }
    printf("connexion faite\n");
    /*------------ PSEUDO ------------*/

    /*srand(time(NULL));
    char * name = calloc(1, 8);
    random_name(name);
    

    // TODO : Parser le nombre de partie
    reception(sock_tcp);
    // TODO : Parser les numeros de partie

    // TODO : Créer une partie [NEWPL id port***]

    // TODO : Choisir une partie [REGIS id port m***]
    // ! port udp unique par personne pour les MP

    // TODO : Gérer REGNO

    /*--- avant envoie START  ---*/
    // TODO : Gérer REGOK
    //* UNREG m***
    //* SIZE? m*** reception : SIZE! m h w***
    //* LIST? m*** reception : LIST! m s*** + s fois PLAYR id***
    //* GAME? m*** reception : GAMES n*** + n fois 0GAME m s***

    /*--- après envoie START ---*/
    // 

    //* UPMOV d
    //* DOMOV d
    //* LEMOV d
    //* RIMOV d
    //* IQUIT
    
    
    /*
    NEWPL id portUDP***
    REGIS id portUDP game.numGame***
    UNREG***
    GAME?***
    SIZE? game.numGame***
    LIST? game.numGame***
    START***


    ACTEX***
   (CLOEX***)
    UPMOV nbPas***
    RIMOV nbPas***
    DOMOV nbPas***
    LEMOV nbPas***

    CHKIT***
    DRPIT***

    GLIS?***
    MALL? message***
    SEND? player.username message***

    IQUIT*** -> quit()
    */
    return 0;
}

void random_name(char * name){
    const char alphabet[] = "abcdefghijklmnopqrstuvwxyz0123456789";
    for (int i = 0; i < 8; ++i){
        name[i] = alphabet[ rand() % (strlen(alphabet)) ];
    }

}

//fonction de parser 