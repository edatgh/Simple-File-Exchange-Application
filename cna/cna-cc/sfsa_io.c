
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
 *   sfsa_io.c - Socketù çvedimo/i¹vedimo auk¹tesniojo lygio realizacija. Pagrinde yra naudojamos
 *               BSD f-jos skirtos socket'ams
 *
 *   VU MIF, CNA Pimras darbas, C-C
 *   Edvardas Ges, III grupì
 *   2004/02/25
 */

#include <sfsa_common.h>
#include <sfsa_debug.h>
#include <sfsa_io.h>

/*
 *   GLOBALS
 */
SFSA_IO_DESC *p_first_desc=NULL;

/*
 *   Privatus modulio interfeisas
 */
SFSA_IO_DESC *create_desc(int,in_addr_t);
int destroy_desc(SFSA_IO_DESC *);

/*
 *   Vie¹o interfeiso realizacija
 */
SFSA_IO_DESC *establish_server(unsigned short int port)
{
    int s;                 /*   serverinio socket'o deskriptorius   */
    struct sockaddr_in sa; /*   serverinio socket'o adresas   */
    SFSA_IO_DESC *p_iod;

    if ((s=socket(PF_INET,SOCK_STREAM,0))==-1) {
	perror("sfsa_io.c: establish_server()--->socket()");
	return NULL;
    }

    if (fcntl(s,F_SETFL,O_NONBLOCK)==-1) {
	perror("sfsa_io.c: establish_server()--->fcntl()");
	return NULL;
    }

    bzero(&sa,sizeof(struct sockaddr_in));

    sa.sin_family=AF_INET;
    sa.sin_port=htons(port);
    sa.sin_addr.s_addr=htonl(INADDR_ANY);

    if (bind(s,(struct sockaddr *)&sa,sizeof(struct sockaddr))==-1) {
	perror("sfsa_io.c: establish_server()--->bind()");
	close(s);
	return NULL;
    }

    if (listen(s,BACKLOG==-1)) {
	perror("sfsa_io.c: establish_server()--->listen()");
	close(s);
	return NULL;
    }

    if ((p_iod=create_desc(s,sa.sin_addr.s_addr))==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: establish_server()--->create_desc()");
	close(s);
	return NULL;
    }

    return p_iod;
}

SFSA_IO_DESC *accept_connection(SFSA_IO_DESC *p_iod)
{
    int cs;                 /*   klientinio socket'o deskriptorius   */
    struct sockaddr_in csa; /*   klientinio socket'o deskriptorius   */
    int cal;                /*   Client Address Length   */
    SFSA_IO_DESC *p_cl_desc;
    fd_set accfds;
    struct timeval at,*p_at=&at;
    int sr;

    if (p_iod==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: accept_connection() p_iod==NULL");
	return NULL;
    }

    bzero(&at,sizeof(struct timeval));
    FD_ZERO(&accfds);
    FD_SET(p_iod->sock,&accfds);

    //p_at->tv_sec=1;

    sr=select(FD_SETSIZE,&accfds,NULL,NULL,p_at);

    switch (sr) {
    case 0:
	_PRINT_ERROR_MSG("sfsa_io.c: accept_connection() Acception timed out");
	return NULL;
	break;
    case -1:
	perror("sfsa_io.c: accept_connection()--->select()");
	return NULL;
	break;
    default:
	_PRINT_DEBUG_MSG("sfsa_io.c: accept_connection() Successful");
	break;
    }

    /*
     *   ©itoje vietoje reikia apra¹imo "accept()" f-jos, bei socket'ù veikimo principus priklausomai
     *   nuo re¾imo (blocking, non-blocking)
     */
    cal=sizeof(struct sockaddr);
    if ((cs=accept(p_iod->sock,(struct sockaddr *)&csa,&cal))==-1) {
	_PRINT_ERROR_MSG("sfsa_io.c: accept_connection()--->accept()");
	return NULL;
    }

    if ((p_cl_desc=create_desc(cs,csa.sin_addr.s_addr))==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: accept_connection()--->create_desc()");
	close(cs);
	return NULL;
    }

    return p_cl_desc;
}

int close_server(SFSA_IO_DESC *p_iod)
{
    if (p_iod==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: close_server() p_iod==NULL");
	return -1;
    }

    close(p_iod->sock);
    p_iod->sock=-1;

    destroy_desc(p_iod);

    return 0;
}

SFSA_IO_DESC *open_connection(const char *p_ip,unsigned short int port,long await_sec)
{
    int s;                 /*   klientinis socket'as   */
    struct sockaddr_in sa; /*   socket'o adresas   */
    SFSA_IO_DESC *p_iod;
    fd_set connfds;
    struct timeval at,*p_at=&at;
    int sr; /*   selection result   */

    if ((s=socket(PF_INET,SOCK_STREAM,0))==-1) {
	perror("sfsa_io.c: open_connection()--->socket()");
	return NULL;
    }

    if (fcntl(s,F_SETFL,O_NONBLOCK)==-1) {
	perror("sfsa_io.c: open_connection()--->fcntl()");
	close(s);
	return NULL;
    }

    bzero(&sa,sizeof(struct sockaddr_in));

    sa.sin_family=AF_INET;
    sa.sin_port=htons(port);
    sa.sin_addr.s_addr=inet_addr(p_ip);

    if (connect(s,(struct sockaddr *)&sa,sizeof(struct sockaddr))==-1)
	if (errno==EINPROGRESS)
	    _PRINT_DEBUG_MSG("sfsa_io.c: open_connection() Connecting...");
	else {
	    perror("sfsa_io.c: open_connection()--->connect()");
	    close(s);
	    return NULL;
	}

    bzero(&at,sizeof(struct timeval));
    FD_ZERO(&connfds);
    FD_SET(s,&connfds);

    if (await_sec==-1)
	p_at=NULL;
    else
	p_at->tv_sec=await_sec;

    sr=select(FD_SETSIZE,NULL,&connfds,NULL,p_at);

    switch (sr) {
    case 0:
	_PRINT_ERROR_MSG("sfsa_io.c: open_connection() Connection timed out");
	return NULL;
	break;
    case -1:
	perror("sfsa_io.c: open_connection()--->select()");
	return NULL;
	break;
    default:
	_PRINT_DEBUG_MSG("sfsa_io.c: open_connection() Connection successful");
	break;
    }

    if ((p_iod=create_desc(s,sa.sin_addr.s_addr))==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: open_connection()--->create_desc()");
	close(s);
	return NULL;
    }

    return p_iod;
}

int close_connection(SFSA_IO_DESC *p_iod)
{
    if (p_iod==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: close_connection()");
	return -1;
    }

    close(p_iod->sock);
    p_iod->sock=-1;

    destroy_desc(p_iod);

    return 0;
}

int send_message(SFSA_IO_DESC *p_iod,SFSA_MESSAGE *p_msg,long await_sec)
{
    fd_set writefds;
    struct timeval at,*p_at=&at;
    int sr; /*   selection result   */
    int r1;
    int r2;
    int r3;
    int r4;

    if (p_iod==NULL||p_msg==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: send_message()");
	return -1;
    }

    /*
     *   Reikia patikrinti ar galima ra¹yti duomenis ç socket'±
     */
    FD_ZERO(&writefds);
    FD_SET(p_iod->sock,&writefds);
    bzero(p_at,sizeof(struct timeval));

    if (await_sec==-1)
	p_at=NULL;
    else
	p_at->tv_sec=await_sec;

    sr=select(FD_SETSIZE,NULL,&writefds,NULL,p_at);

    switch (sr) {
    case 0:
	_PRINT_DEBUG_MSG("sfsa_io.c: send_message() Operation timed out");
	return NULL;
	break;
    case -1:
	perror("sfsa_io.c: send_message()--->select()");
	return NULL;
	break;
    default:
	_PRINT_DEBUG_MSGX("sfsa_io.c: send_message() Number of sockets available for writing:",sr);
	break;
    }

    r1=r2=r3=r4=0;

    do {
	r1=send(p_iod->sock,(void *)&p_msg->type,sizeof(int),0);
    } while (r1==-1&&errno==EAGAIN);

    do {
	r2=send(p_iod->sock,(void *)&p_msg->operation,sizeof(int),0);
    } while (r2==-1&&errno==EAGAIN);

    do {
	r3=send(p_iod->sock,(void *)&p_msg->data_size,sizeof(int),0);
    } while (r2==-1&&errno==EAGAIN);

    if (p_msg->data_size)
	do {
	    r4=send(p_iod->sock,(void *)p_msg->p_data,p_msg->data_size,0);
	} while (r4==-1&&errno==EAGAIN);

    _PRINT_DEBUG_MSGX("sfsa_io.c: send_message() Total bytes sent:",r1+r2+r3+r4);

    if (r1==-1||r2==-1||r3==-1||r4==-1) {
	_PRINT_ERROR_MSG("sfsa_io.c: send_message()--->send()");
	return -1;
    }

    if (r1+r2+r3+r4!=sizeof(int)*3+p_msg->data_size) {
	_PRINT_ERROR_MSG("sfsa_io.c: send_message(): Sent message size differs from message size");
	return -1;
    }

    return 0;
}

SFSA_MESSAGE *recv_message(SFSA_IO_DESC *p_iod,long await_sec)
{
    fd_set readfds;
    struct timeval at,*p_at=&at; /*   awaiting time   */
    int sr;                      /*   selection result   */
    int type;
    int operation;
    int data_size;
    void *p_data;
    int r1;
    int r2;
    int r3;
    int r4;
    SFSA_MESSAGE *p_msg;



    if (p_iod==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: recv_message()");
	return NULL;
    }

    /*
     *   Reikia patikrinti ar galima skaityti duomenis i¹ socket'o
     */
    FD_ZERO(&readfds);
    FD_SET(p_iod->sock,&readfds);
    bzero(p_at,sizeof(struct timeval));

    if (await_sec==-1)
	p_at=NULL;
    else
	p_at->tv_sec=await_sec;

    sr=select(FD_SETSIZE,&readfds,NULL,NULL,p_at);

    switch (sr) {
    case 0:
	_PRINT_DEBUG_MSG("sfsa_io.c: recv_message() Operation timed out");
	return NULL;
	break;
    case -1:
	perror("sfsa_io.c: recv_message()--->select()");
	return NULL;
	break;
    default:
	_PRINT_DEBUG_MSGX("sfsa_io.c: recv_message() Number of sockets available for reading:",sr);
	break;
    }

    r1=r2=r3=r4=0;
    p_data=NULL;

    do {
	r1=recv(p_iod->sock,(void *)&type,sizeof(int),MSG_WAITALL);
    } while (r1==-1&&errno==EAGAIN);

    do {
	r2=recv(p_iod->sock,(void *)&operation,sizeof(int),MSG_WAITALL);
    } while (r2==-1&&errno==EAGAIN);

    do {
	r3=recv(p_iod->sock,(void *)&data_size,sizeof(int),MSG_WAITALL);
    } while (r3==-1&&errno==EAGAIN);

    if (r1==-1||r2==-1||r3==-1) {
	_PRINT_ERROR_MSG("sfsa_io.c: recv_message()--->recv()");
	return NULL;
    }

    if (!(r1&&r2&&r3)) {
	_PRINT_DEBUG_MSG("sfsa_io.c: recv_message()--->recv() Zero bytes received");
	return NULL;
    }

    if (data_size) {
	if ((p_data=(void *)malloc(data_size))==NULL) {
	    _PRINT_ERROR_MSG("sfsa_io.c: recv_message()--->malloc()");
	    return NULL;
	}
	do {
	    r4=recv(p_iod->sock,(void *)p_data,data_size,MSG_WAITALL);
	} while (r4==-1&&errno==EAGAIN);
    }

    if (r4==-1) {
	_PRINT_ERROR_MSG("sfsa_io.c: recv_message()--->recv()");
	free(p_data);
	return NULL;
    }

    if (r1+r2+r3+r4!=sizeof(int)*3+data_size) {
	_PRINT_ERROR_MSG("sfsa_io.c: recv_message(): Received message size differs from message size");
	free(p_data);
	return NULL;
    }

    _PRINT_DEBUG_MSGX("sfsa_io.c: recv_message() Total bytes received in the header:",r1+r2+r3);
    _PRINT_DEBUG_MSGX("sfsa_io.c: recv_message() Value of [data_size]:",data_size);

    if ((p_msg=build_message(type,operation,data_size,p_data))==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: recv_message()--->build_message()");
	free(p_data);
	return NULL;
    }

    return p_msg;
}

int is_connection_dead(SFSA_IO_DESC *p_conn)
{
    fd_set readfds;
    struct timeval at;
    int sr;
    int pr;
    int buf;



    if (p_conn==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: is_connection_dead()");
	return -1;
    }

    FD_ZERO(&readfds);
    FD_SET(p_conn->sock,&readfds);
    bzero(&at,sizeof(struct timeval));

    sr=select(FD_SETSIZE,&readfds,NULL,NULL,&at);

    switch (sr) {
    case 0:
	_PRINT_DEBUG_MSG("sfsa_io.c: is_connection_dead() Operation timed out");
	return 0;
	break;
    case -1:
	perror("sfsa_io.c: is_connection_dead()--->select()");
	return -1;
	break;
    default:
	pr=recv(p_conn->sock,&buf,sizeof(int),MSG_PEEK);
	if (!pr)
	    return 1;
	break;
    }

    return 0;
}

SFSA_MESSAGE *build_message(int type,int operation,int data_size,const void *p_data)
{
    SFSA_MESSAGE *p_new_msg;

    if ((p_new_msg=(SFSA_MESSAGE *)malloc(sizeof(SFSA_MESSAGE)))==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: build_message()--->malloc()");
	return NULL;
    }

    p_new_msg->type=type;
    p_new_msg->operation=operation;
    p_new_msg->data_size=data_size;
    p_new_msg->p_data=NULL;

    if (data_size) {
	if ((p_new_msg->p_data=(void *)malloc(data_size))==NULL) {
	    _PRINT_ERROR_MSG("sfsa_io.c: build_message()--->malloc()");
	    free(p_new_msg);
	    return NULL;
	}
	memcpy(p_new_msg->p_data,p_data,data_size);
    }

    return p_new_msg;
}

int destroy_message(SFSA_MESSAGE *p_msg)
{
    if (p_msg==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: destroy_message()");
	return -1;
    }

    free(p_msg->p_data);
    free(p_msg);

    return 0;
}

/*
 *   Privataus interfeiso realizacija
 */
SFSA_IO_DESC *create_desc(int sock,in_addr_t inaddr)
{
    SFSA_IO_DESC *p_new_desc;

    if ((p_new_desc=(SFSA_IO_DESC *)malloc(sizeof(SFSA_IO_DESC)))==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: create_desc()--->malloc()");
	return NULL;
    }

    p_new_desc->sock=sock;
    p_new_desc->inaddr=inaddr;

    p_new_desc->p_prev_desc=NULL;
    p_new_desc->p_next_desc=p_first_desc;

    if (p_first_desc!=NULL)
	p_first_desc->p_prev_desc=p_new_desc;

    p_first_desc=p_new_desc;

    return p_first_desc;
}

int destroy_desc(SFSA_IO_DESC *p_iod)
{
    if (p_iod==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: destroy_desc() p_iod==NULL");
	return -1;
    }

    if (p_iod->sock!=-1) {
	_PRINT_ERROR_MSG("sfsa_io: destroy_desc(): Trying to destroy connected descriptor");
	return -1;
    }

    if (p_iod->p_prev_desc!=NULL) {
	if (p_iod->p_prev_desc->p_next_desc=p_iod->p_next_desc)
	    p_iod->p_next_desc->p_prev_desc=p_iod->p_prev_desc;
    } else {
	if (p_iod->p_next_desc!=NULL) {
	    p_iod->p_next_desc->p_prev_desc=NULL;
	    p_first_desc=p_first_desc->p_next_desc;
	} else {
	    p_first_desc=NULL;
	}
    }
    free(p_iod);

    return 0;
}
