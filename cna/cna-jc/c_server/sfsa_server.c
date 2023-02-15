
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
 *   sfsa_server.c - Serverinì kliento/serverio komunikavimo realizacijos dalis.
 *
 *   VU MIF, CNA Pimras darbas, C-C
 *   Edvardas Ges, III grupì
 *   2004/03/14
 */

#include <sfsa_common.h>
#include <sfsa_defaults.h>
#include <sfsa_debug.h>
#include <sfsa_users.h>
#include <sfsa_io.h>

/*
 *   Privaèiù f-jù prototipai
 */
void process_user(SFSA_USER *,void *);
void usage(const char *);

/*
 *   main() - Pagrindinì serverio f-ja. Klientù registravimas serveryje.
 */
int main(int argc,char **argv)
{
    SFSA_IO_DESC *p_srv;       /*   serverio deskriptorius   */
    SFSA_IO_DESC *p_inc;       /*   sujungimo su klientù deskriptorius   */
    SFSA_USER *p_user;         /*   vartotojo deskriptorius   */
    SFSA_MESSAGE *p_in_msg;    /*   klientù prane¹imai serveriui   */
    SFSA_MESSAGE *p_out_msg;   /*   serverio prane¹imai klientams   */
    SFSA_MESSAGE *p_aux_msg;   /*   pagalbinis prane¹imas   */
    char w_msg[64];            /*   serverio "welcome" prane¹imas   */
    int w_o;                   /*   "welcome operation" - dinami¹kam operacijos tipui nustatymui   */
    int ch;                    /*   komandinìs eilutìs parametrams apdoroti   */
    char *p_s_pn;              /*   serverio porto numeris (simboliniame formate)   */
    unsigned short int s_port; /*   serverio porto numeris   */


    /*
     *   Parametrù reik¹mìs pagal nutylìjim±
     */
    p_s_pn=NULL;
    s_port=DEFAULT_SERVER_PORT;

    /*
     *   Komandinìs eilutì parametrù apdorojimas
     */
    while ((ch=getopt(argc,argv,"hp:"))!=-1)
	switch (ch) {
	case 'h':
	    usage(argv[0]);
	    break;
	case 'p':
	    p_s_pn=optarg;
	    s_port=(unsigned short int)atoi(p_s_pn);
	    break;
	case '?':
	default:
	    usage(argv[0]);
	    break;
	}

    /*
     *   Paleid¾imas serveris
     */
    if ((p_srv=establish_server(s_port))==NULL) {
	_PRINT_ERROR_MSG("sfsa_server.c: main() Unable to establish server");
	return 0;
    }

    fprintf(stdout,"Established server at: [%d]\n",s_port);

    /*
     *   Paleid¾iamas am¾inas klientù apdorojimo ciklas
     */
    for (;;) {
	usleep(100);
	p_inc=accept_connection(p_srv);

	enum_users(process_user,NULL);

	if (p_inc==NULL)
	    continue;

	if ((p_in_msg=recv_message(p_inc,REGISTRATION_TIMEOUT))==NULL) {
	    close_connection(p_inc);
	    _PRINT_DEBUG_MSGX("Registration timeout: ",REGISTRATION_TIMEOUT);
	    continue;
	}

	switch (p_in_msg->type) {
	case MSGTYPE_OPERATION:
	    switch (p_in_msg->operation) {
	    case OPERATION_REGISTER_USER:

		_PRINT_DEBUG_MSG("sfsa_server.c: main() OPERATION_REGISTER_USER message received");

		if ((p_aux_msg=recv_message(p_inc,REGISTRATION_TIMEOUT))==NULL) {
		    close_connection(p_inc);
		    _PRINT_DEBUG_MSGX("Registration timeout: ",REGISTRATION_TIMEOUT);
		    continue;
		}

		if ((p_user=add_user((char *)p_in_msg->p_data,*((short int *)p_aux_msg->p_data),p_inc))==NULL) {
		    sprintf(w_msg,"Unable to register the user");
		    w_o=CONNECTION_REFUSED;
		    _PRINT_DEBUG_MSG("sfsa_server.c: main() Unable to add user");
		} else {
		    sprintf(w_msg,"User [%s] was registered on the server",(char *)p_in_msg->p_data);
		    w_o=CONNECTION_ACCEPTED;
		    _PRINT_DEBUG_MSG("sfsa_server.c: main() User added successfully");
		}

		destroy_message(p_aux_msg);

		if ((p_out_msg=build_message(MSGTYPE_CONNECTION,
					     w_o,strlen(w_msg)+1,w_msg))==NULL) {
		    _PRINT_DEBUG_MSG("sfsa_server.c: main() Unable to buid message");
		    break;
		}

		if (send_message(p_inc,p_out_msg,WAIT_0)!=0)
		    _PRINT_DEBUG_MSG("sfsa_server.c: main() Unable to send message");

		destroy_message(p_out_msg);
		break;
	    default:
		close_connection(p_inc);
		break;
	    }
	    break;
	default:
	    close_connection(p_inc);
	    break;
	}

	destroy_message(p_in_msg);
    }

    return 0;
}

/*
 *   process_user() - Vartotojo u¾klausù apdorojimas (f-ja paleid¾iama kiekvienam vartotojui
 *                    atskirai)
 */
void process_user(SFSA_USER *p_user,void *p_data)
{
    SFSA_MESSAGE *p_in_msg;
    SFSA_MESSAGE *p_out_msg;
    SFSA_USER *p_fs_user;
    char buf[64];
    char *fs_ip; /*   Java comp.   */
    struct in_addr in;
    char fs_port[8];



    /*
     *   Tikrinami visi serveryje u¾registruoti vartotojai, ie¹komas
     *   "dead connection" (arba "zombie user") po¾ymis, t.y. tokie
     *   vartotojai sujungimas su kuriai jau nebeedzistuoja, bet
     *   jie dar liko u¾registruoti serveryje. Tokie vartotojai
     *   yra automati¹kai pa¹alinami ir sujungimas us jais nutraukiamas.
     */
    if (is_connection_dead(p_user->p_iod)>0) {
	_PRINT_DEBUG_MSGXX("Removing zombie user: ",p_user->p_name);
	remove_user(p_user);
	return;
    }

    /*
     *   Gaunamas vartotojo prane¹imas
     */
    if ((p_in_msg=recv_message(p_user->p_iod,WAIT_0))==NULL)
	return;

    /*
     *   Nagrinìjamas gautasis prane¹imas
     */
    switch (p_in_msg->type) {
    case MSGTYPE_OPERATION:
	switch (p_in_msg->operation) {
	case OPERATION_ADDFILENAMETOLIST:
	    add_export_file_name(p_user,(char *)p_in_msg->p_data);
	    break;
	case OPERATION_SENDLISTOFFILENAMES:
	    send_export_list(p_user);
	    break;
	case OPERATION_UNREGISTER_USER:
	    remove_user(p_user);
	    break;
	case OPERATION_TRANSMITFILE:
	    if ((p_fs_user=find_user_by_file_name(p_in_msg->p_data))==NULL)
		sprintf(buf,"Unable to locate user having that file");
	    else {
		SFSA_IO_DESC *p_host;

		/*
		 *   Tikriname ar failas priklauso skirtingiems vartotojams
		 */
		if (p_fs_user==p_user) {
		    sprintf(buf,"This file belongs to you");
		    p_out_msg=build_message(MSGTYPE_TEXT,OPERATION_NOOP,strlen(buf)+1,buf);
		    send_message(p_user->p_iod,p_out_msg,WAIT_0);
		    destroy_message(p_out_msg);
		    break;
		}

		p_host=open_connection((char *)inet_ntoa(p_fs_user->p_iod->inaddr),
				       p_fs_user->fw_port,10);

		if (p_host==NULL) {
		    _PRINT_DEBUG_MSG("sfsa_server.c: process_user() Unable to open connection");
		    break;
		}

		p_out_msg=build_message(MSGTYPE_OPERATION,OPERATION_TRANSMITFILE,p_in_msg->data_size,
					p_in_msg->p_data);
		send_message(p_host,p_out_msg,10);
		destroy_message(p_out_msg);

		/*
		 *   Java compatibility
		 */
		bzero(&in,sizeof(struct in_addr));
		in.s_addr=p_user->p_iod->inaddr;
		fs_ip=(char *)inet_ntoa(in);

		p_out_msg=build_message(MSGTYPE_NOTYPE,OPERATION_NOOP,strlen(fs_ip),fs_ip);
		send_message(p_host,p_out_msg,10);
		destroy_message(p_out_msg);

		sprintf(fs_port,"%u",p_user->fw_port);
		p_out_msg=build_message(MSGTYPE_NOTYPE,OPERATION_NOOP,strlen(fs_port),
					fs_port);
		send_message(p_host,p_out_msg,10);
		destroy_message(p_out_msg);

		close_connection(p_host);

		sprintf(buf,"Request for the file was sent to the host user");
	    }

	    p_out_msg=build_message(MSGTYPE_TEXT,OPERATION_NOOP,strlen(buf)+1,buf);
	    send_message(p_user->p_iod,p_out_msg,WAIT_0);
	    destroy_message(p_out_msg);

	    break;
	case OPERATION_NOOP:
	    break;
	default:
	    _PRINT_DEBUG_MSG("sfsa_server.c: process_user() Unknown operation received.");
	    break;
	}
	break;
    default:
	_PRINT_DEBUG_MSG("sfsa_server.c: process_user() Unknown message received.");
	break;
    }

    destroy_message(p_in_msg);
}

/*
 *   usage() - Parodo programos paleidimo parametrus
 */
void usage(const char *p_prog_name)
{
    printf("USAGE: %s [-h] [-p <port number>]\n",p_prog_name);
    exit(0);
}
