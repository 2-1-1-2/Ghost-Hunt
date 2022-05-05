all: client_auto

redo: distclean all

client_auto: client_auto.o client_fonction_tcp.o
	gcc -o client_auto client_auto.o client_fonction_tcp.o

client_fonction_tcp.o:
	gcc -c ./src/client_fonction_tcp.c -I ./include

client_auto.o :
	gcc -c ./src/client_auto.c -I ./include


distclean: clean_client

clean: clean_o clean_client

clean_client:
	rm -f client_auto


clean_o:
	rm -f *.o
