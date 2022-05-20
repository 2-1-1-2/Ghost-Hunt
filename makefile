all: client_manuel serveur

redo: clean  all

redo: distclean all

client_auto: client_auto.o client_fonction_tcp.o
	gcc -o ./src/client_auto client_auto.o client_fonction_tcp.o

client_fonction_tcp.o:
	gcc -c ./src/client_fonction_tcp.c -I ./include

client_auto.o :
	gcc -c ./src/client_auto.c -I ./include

client_manuel: client_manuel.o client_commons_tcp.o 
	gcc -Wall -o ./src/client_manuel client_manuel.o client_commons_tcp.o

client_commons_tcp.o:
	gcc -Wall -c ./src/client_commons_tcp.c -I ./include

client_manuel.o:
	gcc -Wall -c ./src/client_manuel.c -I ./include

distclean: clean_client

serveur:
	javac ./src/*.java

_client:
	./src/client_manuel

	
_serveur:
	cd src && java Server


clean: clean_o clean_client clean_serv

clean_client:
	rm -f ./src/client_auto



clean_serv:
	rm -f ./src/*.class


clean_o:
	rm -f *.o
