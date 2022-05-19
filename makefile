all: client_auto

redo: distclean all

client_auto: client_auto.o client_fonction_tcp.o
	gcc -o ./src/client_auto client_auto.o client_fonction_tcp.o

client_fonction_tcp.o:
	gcc -c ./src/client_fonction_tcp.c -I ./include

client_auto.o :
	gcc -c ./src/client_auto.c -I ./include

client_manuel: client_manuel.o client_envoi_TCP.o client_reception_TCP.o
	gcc -Wall -o ./src/client_manuel client_manuel.o client_envoi_TCP.o client_reception_TCP.o

client_envoi_TCP.o:
	gcc -Wall -c ./src/client_envoi_TCP.c -I ./include

client_reception_TCP.o:
	gcc -Wall -c ./src/client_reception_TCP.c -I ./include

client_manuel.o:
	gcc -Wall -c ./src/client_manuel.c -I ./include

distclean: clean_client

clean: clean_o clean_client

clean_client:
	rm -f ./src/client_auto


clean_o:
	rm -f *.o
