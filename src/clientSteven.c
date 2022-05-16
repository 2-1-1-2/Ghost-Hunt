#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>

int sigSock;

/** Renvoie l'indice de début de la sous-chaîne '***'
*/
// int finTCP(char[] message, int size){
// 	int cpt=0;
// 	for(int i=0; i<size; i++){
// 		if(message[i]=='*') cpt++;
// 		else cpt=0;
// 		if(cpt==3) return i-2;
// 	}
// 	return -1;
// }

/**
 * @brief 
 * Envoie le message NEWPL au serveur
 * TODO pouvoir prendre un id de moins de 8 caractères/un port de moins de 4 caractères
 * @param sock la socket de communication
 * @param id l'id du joueur sur 8 caractères
 * @param port le port sur 4 caractères
 * @return int EXIT_SUCCESS si tout va bien, EXIT_FAILURE sinon 
 */
int sendNEWPL(int sock, char* id, char* port){
	int check=-1;
	check=atoi(port);
	if(strlen(id) != 8 || check == -1 || strlen(port) != 4) return EXIT_FAILURE;
	char* entete="NEWPL ";
	char* toSend=malloc(strlen(entete) + strlen(id) + strlen(port) + 5);
	strcpy(toSend, entete);
	strcat(toSend, id);
	strcat(toSend, " ");
	strcat(toSend, port);
	strcat(toSend, "***");
	send(sock, toSend, strlen(toSend), 0);
	free(toSend);
	return EXIT_SUCCESS;
}

/**
 * @brief 
 * Envoie le message REGIS ou NEWPL au serveur, selon la requête écrite
 * TODO pouvoir avoir strlen(id) < 8 et strlen(port) < 4
 * @param sock la socket de communication
 * @param requete la requête entière
 * @param name le type de la requête : REGIS ou NEWPL
 * @return int EXIT_SUCCESS
 */
int sendREGISorNEWPL(int sock, char* requete, char* name){
	char id[9];
	for(int i=0; i<8; i++){
		id[i]=requete[i+6];
	}
	id[8]='\0';
	char port[5];
	for(int i=0; i<4; i++){
		port[i]=requete[i+15];
	}
	port[4]='\0';
	send(sock, strcat(name, " "), strlen(name)+1, 0);
	send(sock, strcat(id, " "), strlen(id)+1, 0);
	send(sock, port, strlen (port), 0);
	if(strcmp(name, "REGIS ")==0){
		send(sock, " ", strlen(" "), 0);
		
		char buffInt[strlen(requete)-23];
		for(int i=20; i<strlen(requete)-3; i++){
			buffInt[i-20]=requete[i];
		}
		buffInt[strlen(requete)-20]='\0';
		uint8_t numPartieInt = atoi(buffInt);
		send(sock, &numPartieInt, sizeof(numPartieInt), 0);
	}
	send(sock, "***", strlen("***"), 0);
	return EXIT_SUCCESS;
}

/**
 * @brief 
 * Envoie le message GAME? au serveur
 * @param sock la socket de communication
 * @return int EXIT_SUCCESS
 */
int sendGAME(int sock){
	char* toSend="GAME?***";
	send(sock, toSend, strlen(toSend), 0);
	return EXIT_SUCCESS;
}

/**
 * @brief 
 * Envoie le message LIST? ou SIZE? au serveur, selon la requête écrite
 * @param sock la socket de communication
 * @param requete la requete entière
 * @param name le type de la requête : LIST? ou SIZE?
 * @return int EXIT_SUCCESS
 */
int sendLISTorSIZE(int sock, char* requete, char * name){
	char buffInt[strlen(requete)-10];
	for(int i=6;i<strlen(requete)-3;i++){
		buffInt[i-6] = requete[i];
	}
	buffInt[strlen(requete)-7] = '\0';
	uint8_t nbPartiesInt = atoi(buffInt);

	send(sock, strcat(name, " "), strlen(name)+1, 0);
	send(sock, &nbPartiesInt, sizeof(nbPartiesInt), 0);
	send(sock, "***", strlen("***"), 0);

	return EXIT_SUCCESS;
}

/**
 * @brief 
 * Envoie le message UNREG au serveur
 * @param sock la socket de communication
 * @return int EXIT_SUCCESS si tout va bien, EXIT_FAILURE sinon
 */
int sendUNREG(int sock){
	char* toSend="UNREG***";
	send(sock, toSend, strlen(toSend), 0);
	return EXIT_SUCCESS;
}

/**
 * @brief 
 * Envoie le message IQUIT au serveur
 * @param sock la socket de communication
 * @return int EXIT_SUCCESS si tout va bien, EXIT_FAILURE sinon
 */
int sendIQUIT(int sock){
	char* toSend="IQUIT***";
	send(sock, toSend, strlen(toSend), 0);
	return EXIT_SUCCESS;
}

int readGameAndListAndGlis(int descr){
	char buff[1000];
	int size_rec=read(descr,buff,999*sizeof(char));
	buff[size_rec]='\0';
	printf("%s\n",buff);
	char buffInt[size_rec-10];
	for(int i=6;i<size_rec-3;i++){
		buffInt[i-6] = buff[i];
	}
	buffInt[size_rec-7] = '\0';
	uint8_t nbParties = atoi(buffInt);
	fcntl(descr,F_SETFL,O_NONBLOCK);
	for(int i=0;i<nbParties;i++){
		memset(buff,'\0',100);
		size_rec=read(descr,buff,99*sizeof(char));
		buff[size_rec]='\0';
		char * token = strtok(buff,"***");
		while(token != NULL){
			printf("%s***\n",token);
			token = strtok ( NULL,"***" );
		}
	}
	int flags = fcntl(descr, F_GETFL, 0);
	fcntl(descr, F_SETFL, flags & (~O_NONBLOCK));
	return EXIT_SUCCESS;
}

int readSize(int descr){
	char buff[1000];
	int size_rec=read(descr,buff,999*sizeof(char));
	buff[size_rec]='\0';
	char * requete = malloc(7);
	for(int i = 0;i<7;i++){
		requete[i] = buff[i];
	}
	char tab1[2];
	tab1[0] = buff[8];
	tab1[1] = buff[9];
	char tab2[2];
	tab2[0] = buff[11];
	tab2[1] = buff[12];
	int h = (tab1[0] - '0')+ (tab1[1] - '0');
	int w = (tab2[0] - '0')+ (tab2[1] - '0');
	printf("%s %d %d***\n",requete,h,w);
	free(requete);
}

int readStart(int descr){
	char buff[1000];
	int size_rec=read(descr,buff,999*sizeof(char));
	buff[size_rec]='\0';
	char * requete = malloc(7);
	for(int i = 0;i<7;i++){
		requete[i] = buff[i];
	}
	char tab1[2];
	tab1[0] = buff[8];
	tab1[1] = buff[9];
	char tab2[2];
	tab2[0] = buff[11];
	tab2[1] = buff[12];
	int h = (tab1[0] - '0')+ (tab1[1] - '0');
	int w = (tab2[0] - '0')+ (tab2[1] - '0');
	char * reste = malloc(1000);
	for(int i = 13;i<strlen(buff);i++){
		reste[i-13] = buff[i]; 
	}
	printf("%s %d %d%s\n",requete,h,w,reste);

	free(requete);
	free(reste);
	return EXIT_SUCCESS;
}

int readRegis(int descr){
	char buff[1000];
	int size_rec=read(descr,buff,999*sizeof(char));
	buff[size_rec]='\0';
	printf("%s\n",buff);
	return EXIT_SUCCESS;
}

/**
 * @brief 
 * Méthode de lecture générale
 * @param descr la socket de communication
 * @return int -1 si on lit une réponse GOBYE***, EXIT_SUCCESS sinon
 */
int readGen(int descr){
	char buff[1000];
	int size_rec=read(descr,buff,999*sizeof(char));
	buff[size_rec]='\0';
	printf("%s\n",buff);
	if(strcmp(buff, "GOBYE***")==0){
		return -1;
	}
	return EXIT_SUCCESS;
}

/**
 * @brief 
 * Gestion du Ctrl-C à la façon d'un message IQUIT
 */
void sigintHandler(int sig){
	char *requete = "IQUIT***";
	send(sigSock, requete, strlen(requete), 0);
	printf("\n");
	readGen(sigSock);
}

/**
 * @brief 
 * Méthode principale
 */
int communication(int argc, char **argv){
	signal(SIGINT, sigintHandler);
	struct sockaddr_in address_sock;
	address_sock.sin_family = AF_INET;
	address_sock.sin_port = htons(6666);
	inet_aton("127.0.0.1",&address_sock.sin_addr);

	int descr=socket(PF_INET,SOCK_STREAM,0);
	sigSock=descr;
	int r=connect(descr,(struct sockaddr *)&address_sock,
	sizeof(struct sockaddr_in));
	int start=EXIT_SUCCESS-1;
	if(r!=-1){
		readGameAndListAndGlis(descr);
		while(1){
			char buff[100];
			char * requete = malloc(1000);
			fgets(requete,1000,stdin);
			int size_ptr = strlen(requete);
			requete[size_ptr-1] = '\0';
			char * requete_name = malloc(1000);
			strncpy(requete_name, requete, 5);
			requete_name[5] = '\0';
			if(start!=EXIT_SUCCESS){
				if (strcmp(requete_name, "GAME?") == 0 || strcmp(requete_name,"LIST?") == 0){
					if(strcmp(requete_name,"LIST?") == 0) sendLISTorSIZE(descr, requete, requete_name);
					else sendGAME(descr);
					readGameAndListAndGlis(descr);
				}
				else if(strcmp(requete_name, "SIZE?")==0){
					sendLISTorSIZE(descr, requete, requete_name);
					readSize(descr);
				}
				else if(strcmp(requete_name, "NEWPL")==0 || strcmp(requete_name,"REGIS") == 0){
					sendREGISorNEWPL(descr, requete, requete_name);
					readGen(descr);
				}
				else if(strcmp(requete_name,"START") == 0){
					send(descr,requete,strlen(requete),0);
					start=readStart(descr);
				}
				else {
					send(descr,requete,strlen(requete),0);
					if(readGen(descr)==-1){
						free(requete);
						free(requete_name);
						break;
					}
				}
			}
			else {
				if(strcmp(requete_name, "GLIS?")==0){
					send(descr, requete, strlen(requete), 0);
					readGameAndListAndGlis(descr);
				}
				else {
					send(descr, requete,strlen(requete), 0);
					if(readGen(descr)==-1){
						free(requete);
						free(requete_name);
						break;
					}
				}
			}
			free(requete);
			free(requete_name);
		}
		//close(descr);
	}
}

int main(int argc, char **argv){
	return communication(argc, argv);
}