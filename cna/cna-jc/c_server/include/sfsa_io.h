
/*
 * Copyright (c) 2004, Edward Hess
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */



/*
 *   VU MIF, CNA Pimras darbas, C-C
 *   Edvardas Ges, III grupì
 *   2004/02/25
 */

#ifndef __SFSA_IO_H__
#define __SFSA_IO_H__

/*
 *   Konstantos
 */
#define OPERATION_ADDFILENAMETOLIST      0
#define OPERATION_SENDLISTOFFILENAMES    1
#define OPERATION_TRANSMISSION_BEGIN     2
#define OPERATION_TRANSMISSION_END       3
#define OPERATION_REGISTER_USER          4
#define OPERATION_UNREGISTER_USER        5
#define OPERATION_TRANSMITFILE           6 /*   client/server side   */
#define OPERATION_FILETRANSMISSION_BEGIN 7
#define OPERATION_FILEDATA               8
#define OPERATION_FILETRANSMISSION_END   9
#define OPERATION_FILENOTFOUND           10
#define OPERATION_NOOP                  -1

#define CONNECTION_ACCEPTED 0
#define CONNECTION_REFUSED  1

#define MSGTYPE_TEXT       0
#define MSGTYPE_OPERATION  1
#define MSGTYPE_CONNECTION 2
#define MSGTYPE_NOTYPE    -1

#define BACKLOG 8

#define WAIT_INDEFINITELY -1
#define WAIT_0             0

/*
 *   Tipai
 */
typedef struct _sfsa_io_desc_t {
    int sock; /*   BSD socket descripor   */
    in_addr_t inaddr;
    struct _sfsa_io_desc_t *p_prev_desc;
    struct _sfsa_io_desc_t *p_next_desc;
} SFSA_IO_DESC;

typedef struct _sfsa_message_t {
    int type; /*   nenaudojamas   */
    int operation;
    int data_size;
    void *p_data;
} SFSA_MESSAGE;

/*
 *   Vie¹as modulio interfeisas
 */
SFSA_IO_DESC *establish_server(unsigned short int);
SFSA_IO_DESC *accept_connection(SFSA_IO_DESC *);
int close_server(SFSA_IO_DESC *);
SFSA_IO_DESC *open_connection(const char *,unsigned short int,long);
int close_connection(SFSA_IO_DESC *);
int send_message(SFSA_IO_DESC *,SFSA_MESSAGE *,long);
SFSA_MESSAGE *recv_message(SFSA_IO_DESC *,long);
int is_connection_dead(SFSA_IO_DESC *);

SFSA_MESSAGE *build_message(int,int,int,const void *);
int destroy_message(SFSA_MESSAGE *);

#endif
