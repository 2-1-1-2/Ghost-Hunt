#include "client_fonction_tcp.h"
char delim_espace[] = " ";
char delim_stars[] = "*";

int connection(int port, const char * hote){
    struct addrinfo *first_info;
    struct addrinfo hints;
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
    
    int r=getaddrinfo(hote,NULL,&hints,&first_info);
    if(r==0){
        struct addrinfo *info=first_info;
        int found=0;
        struct sockaddr *saddr;
        struct sockaddr_in *addressin;
        if(info!=NULL){
            saddr=info->ai_addr;
            addressin=(struct sockaddr_in *)saddr;
            found=1;
        }
        if(found==1){
            struct sockaddr_in adress_sock;
            adress_sock.sin_family = AF_INET;
            adress_sock.sin_port = htons(port);
            adress_sock.sin_addr=addressin->sin_addr;

            int sock=socket(PF_INET, SOCK_STREAM, 0);
            if(sock==-1){
                printf("error creating socket\n");
                close(sock); 
                exit(1);
            }
            

            int c=connect(sock, (struct sockaddr*) &adress_sock, sizeof(struct sockaddr_in));

            if(c==-1){
                printf("error connecting to server\n");
                close(sock); 
                exit(1);
            }

            return sock;
        }
    }
    return -1;
}




int reception(int sock){
    char buf[100];
    int rec = recv(sock, buf, sizeof(buf), 0);
    buf[rec] = '\0';
    
    //! permet d'être sur d'avoir tout le message
    /*
    if(strncmp(buf+rec-3, "***",3)==0){
        printf("c'est bon\n");
    }
    */
    
    printf("server sent : %s\n", buf);
    if(strncmp(buf, "GAMES", 5) == 0){
        return games_traitement(buf, rec, sock);

    }
    else if(strncmp(buf, "OGAMES", 5) == 0){
        
    }
    else if(strncmp(buf, "REGOK", 5) == 0){
        
    }
    else if(strncmp(buf, "REGNO", 5) == 0){
        
    }
    else if(strncmp(buf, "UNROK", 5) == 0){
        
    }
    else if(strncmp(buf, "SIZE!", 5) == 0){
        
    }
    else if(strncmp(buf, "LIST!", 5) == 0){
        
    }
    else if(strncmp(buf, "PLAYR", 5) == 0){
        
    }
    else if(strncmp(buf, "WELCO", 5) == 0){
        
    }
    else if(strncmp(buf, "POSIT", 5) == 0){
        
    }
    else if(strncmp(buf, "MOVE!", 5) == 0){
        
    }
    else if(strncmp(buf, "MOVEF", 5) == 0){
        
    }
    else if(strncmp(buf, "GOBYE", 5) == 0){
        
    }
    else if(strncmp(buf, "GLIS ", 5) == 0){
        
    }
    else if(strncmp(buf, "GPLYR", 5) == 0){
        
    }
    else if(strncmp(buf, "MALL!", 5) == 0){
        
    }
    else if(strncmp(buf, "SEND!", 5) == 0){
        
    }
    else if(strncmp(buf, "NSEND!", 5) == 0){
        
    }
    else if(strncmp(buf, "DUNNO", 5) == 0){
        return -1;
    }

    return 1;
}

int envoie(int socket, const char* toSend){
    return send(socket, toSend, strlen(toSend), 0);
}

    /* ----- traitement -----*/
    //* GAMES n***                  : nombre de partie
    int games_traitement(char * mess, int taille, int sock){
        //* GAMES n
        char * tmp = calloc(1, taille);
        sprintf(tmp, "%s", mess);
        char *num = strtok(tmp, delim_stars); //délimite "*"
        printf("%s\n", num);

        //on récupère le nombre de partie
        num = strtok(num, delim_espace); //delimite " "
        num = strtok(NULL, delim_espace); //on récupère n
        uint8_t nb_games = atoi(num);
        printf("nombre de partie : %u\n", nb_games);

        //créer une partie si elle n'existe pas
        if(nb_games == 0){
            char * envoie = "NEWPL 12345678 6667***";
            send(sock, envoie, strlen(envoie), 0);
            printf("ici");
        }
        
        //on affiche les parties qu'on a récupérées
        int cpt_partie = 0;

        
        char * tmp2 = calloc(1, taille);
        sprintf(tmp2, "%s", mess);
        char *ptr2 = strtok(tmp2, delim_stars); //on passe la ligne GAMES n
        ptr2 = strtok(NULL, delim_stars);
        while(ptr2 != NULL){
            cpt_partie +=1; 
            printf("%s\n", ptr2);
            ptr2 = strtok(NULL, delim_stars);
        }
        printf("nombre compté : %d\n", cpt_partie);
        
        //on verifie s'il y a tout le message/nb de partie

        if(strncmp(mess+taille-3, "***", 3)==0 && cpt_partie == nb_games){
            printf("c'est bon\n");
        }
        else{
            int cpt_etoile_fin = 0;
            if(strncmp(mess+taille-3, "***",3)==0) cpt_etoile_fin = 3;
            else if(strncmp(mess+taille-2, "**",2)==0) cpt_etoile_fin = 2;
            else if(strncmp(mess+taille-1, "*",1)==0) cpt_etoile_fin = 1;

            affiche_suite(sock, cpt_partie, cpt_etoile_fin, nb_games);
            
        }

        return 1;
    }

    int affiche_suite(int sock, int cpt_partie, int cpt_etoile_fin, int nb_games){
        char suite[100];
        printf("\nsuite cpt : %d\n\n", cpt_partie);
        int rec = recv(sock, suite, sizeof(suite), 0);

        char * tmp = calloc(1, rec);
        sprintf(tmp, "%s", suite);

        char *ptr = strtok(tmp, delim_stars); //on passe la ligne GAMES n
        while(ptr != NULL){
            cpt_partie +=1; 
            printf("%s\n", ptr);
            ptr = strtok(NULL, delim_stars);
        }
        printf("nombre compté : %d\n", cpt_partie);

        if((strncmp(suite+rec-3, "***", 3)==0
         || (cpt_etoile_fin=1 && strncmp(suite+rec-2, "**", 2)==0)
         || (cpt_etoile_fin=2 && strncmp(suite+rec-1, "*", 1)==0))
         && cpt_partie >= nb_games)
            printf("c'est bon\n");
        else{
            int cpt_etoile_fin = 0;
            if(strncmp(suite+rec-3, "***",3)==0) cpt_etoile_fin = 3;
            else if(strncmp(suite+rec-2, "**",2)==0) cpt_etoile_fin = 2;
            else if(strncmp(suite+rec-1, "*",1)==0) cpt_etoile_fin = 1;

            affiche_suite(sock, cpt_partie, cpt_etoile_fin, nb_games);
            
        }
        return 1;
    }

    //* OGAMES m s***               : informations partie - n° de partie - nb inscrit             
    //* REGOK m***                  : inscription correcte - n° de partie    
    //* REGNO***                    : inscription incorrecte          
    //* UNROK m***                  : désinscription - n° de partie                          
    //* SIZE! m h w***              : information labyrinthe - n° de partie - hauteur - largeur
    //* LIST! m s***                : nb joueur dans la partie - n° de partie - nb inscription  
    //* PLAYR id***                 : information joueur - id joueur
    //* WELCO m h w f ip port***    : début de partie - n° partie - haut - larg - nb fantome - ip multidif - port multidif
    //* POSIT id x y***             : position - id joueur - ligne - colonne
    //* MOVE! x y***                : déplacement sans fantome - ligne - colonne
    //* MOVEF x y p***              : déplacement avec fantome - ligne - colonne - point
    //* GOBYE***                    : fin de partie > déconnexion
    //* GLIS s***                   : nb de joueurs présents
    //* GPLYR id x y p***           : information joueur - id - lig - col - point
    //* MALL!***                    : réponse à notre de demande de msg
    //* SEND!***                    : réponse à notre de demande de MP
    //* NSEND!***                   : réponse négative à notre de demande de MP
    //* DUNNO***                    : idk - traiter directement dans le code 