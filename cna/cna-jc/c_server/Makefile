#
#   CNA Server (Java-C) Makefile
#
#   NOTE: Use GNU make
#
#   Edward Hess
#

#
#   Variables
#

#
#   Common stuff
#
CC=gcc
CFLAGS=-I $(INCLUDE_PREFIX)
INCLUDE_PREFIX=./include

#
#   Server stuff
#
SERVER_INCLUDES=$(INCLUDE_PREFIX)/sfsa_common.h $(INCLUDE_PREFIX)/sfsa_io.h \
                $(INCLUDE_PREFIX)/sfsa_defaults.h $(INCLUDE_PREFIX)/sfsa_debug.h \
                $(INCLUDE_PREFIX)/sfsa_users.h
SERVER_SRCS=sfsa_server.c sfsa_io.c sfsa_users.c sfsa_export_files.c
SERVER_OBJS=sfsa_server.o sfsa_io.o sfsa_users.o sfsa_export_files.o

#
#   General (phony) targets
#
all: server

clean :
	- rm *.o server

#
#   "server" makeing ruleset
#
server : $(SERVER_OBJS)
	$(CC) -o $@ $^

$(SERVER_OBJS) : $(SERVER_SRCS) $(SERVER_INCLUDES)

#
#   Pseudo target declaration
#
.PHONY: all clean
