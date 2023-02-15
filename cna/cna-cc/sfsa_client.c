
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
 *   sfsa_client.c - Klientin� kliento/serverio komunikavimo realizacijos dalis. Bendravimui su serveriu
 *                   naudojamos �emesnio lygio f-jos, kuri� realizacija yra kituose failuose.
 *
 *
 *   VU MIF, CNA Pimras darbas, C-C
 *   Edvardas Ges, III grup�
 *   2004/02/25
 */

#include <sfsa_common.h>
#include <sfsa_defaults.h>
#include <sfsa_debug.h>
#include <sfsa_io.h>

/*
 *   Privatus modulio interfeisas
 */
int send_export_file_names(SFSA_IO_DESC *,const char *);
char *extract_file_name(char *);
void usage(const char *);
void fs_sig_processor(int);

/*
 *   main() - Pagrindin� f-ja. Nagrin�jami programos parametrai, peled�iamas fail� serveris,
 *            vyksta prisijungimas prie serverio, komand� interpretatoriaus paleidimas.
 */
int main(int argc,char **argv)
{
    SFSA_IO_DESC *p_conn;       /*   sujungimo su serveriu deskriptorius   */
    SFSA_IO_DESC *p_fws;        /*   fal� serverio deskriptorius   */
    SFSA_IO_DESC *p_inc;        /*   prisijungim� prie fail� serverio deskriptorius   */
    SFSA_MESSAGE *p_msg;        /*   prane�imo saugojimo vieta   */
    int cmd_ok;                 /*   pagalbinis kitamasis, kuris parodo �vestos komandos korekti�kum�   */
    int t;                      /*   transmission flag   */
    char buf[64];               /*   konsoleje �vestos komandos talpinamos � �� bufer�   */
    int ch;                     /*   programos parametrams apdoroti   */
    char *p_uname;              /*   vartotoja vardas   */
    char *p_fw_port;            /*   fail� serverio porto numeris (simboliniame formate)   */
    char *p_s_ip;               /*   serverio IP adresas   */
    char *p_s_pn;               /*   serverio porto numeris u�ra�ytas simboliniame formate   */
    char *p_e_fn;               /*   eksporto failo pavadinimas   */
    unsigned short int s_port;  /*   serverio porto numeris   */
    unsigned short int fw_port; /*   fali� serverio porto numeris   */
    int refused;                /*   parodo ar serveris prieme prisijungima apdorojimui   */
    pid_t ch_id;                /*   fail� serverio proceso ID   */



    /*
     *   Kintam�j� inicializacija pagal nutyl�jim�
     */
    p_uname=NULL;
    p_fw_port=NULL;
    p_s_ip=DEFAULT_SERVER_IP;
    p_s_pn=NULL;
    p_e_fn=NULL;
    s_port=DEFAULT_SERVER_PORT;
    fw_port=DEFAULT_FILE_WAITING_PORT;

    /*
     *   Komandin�s eilut�s parametr� paemimas ir kintam�j� reik�mi� pagal nutyl�jim�
     *   pakeitimas
     */
    while ((ch=getopt(argc,argv,"u:hs:a:p:e:"))!=-1)
	switch (ch) {
	case 'u':
	    p_uname=optarg;
	    break;
	case 'h':
	    usage(argv[0]);
	    break;
	case 's':
	    p_fw_port=optarg;
	    fw_port=(unsigned short int)atoi(p_fw_port);
	    break;
	case 'a':
	    p_s_ip=optarg;
	    if (inet_addr(p_s_ip)==INADDR_NONE) {
		printf("Incorrect format of the IP address!\n");
		exit(0);
	    }
	    break;
	case 'p':
	    p_s_pn=optarg;
	    s_port=(unsigned short int)atoi(p_s_pn);
	    break;
	case 'e':
	    p_e_fn=optarg;
	    break;
	case '?':
	default:
	    usage(argv[0]);
	    break;
	}

    /*
     *   Vartotojo vardas yra vienintelis privalomas parametras komandin�je eilut�je
     */
    if (p_uname==NULL)
	usage(argv[0]);

    /*
     *   Paled�iamas fail� serveris
     */
    if ((ch_id=fork())==-1) {
	perror("sfsa_client.c: main()--->fork()");
	return 0;
    }

    if (ch_id==0) {

	/*
	 *   Fail� serveris privalo tur�ti signal� apdorojimo
	 *   procedur�
	 */
	signal(SIGTERM,fs_sig_processor);

	if ((p_fws=establish_server(fw_port))==NULL) {
	    _PRINT_ERROR_MSG("sfsa_client.c: main()--->CHILD: Unable to establish file awaiting server");
	    exit(0);
	}

	fprintf(stdout,"File server started\n");
	fflush(stdout);

	{ /*   inner block   */
	    SFSA_MESSAGE *p_in_msg;    /*   ateinan�i� prane�im� saugojimui   */
	    SFSA_MESSAGE *p_out_msg;   /*   siun�iam� prane�im� saugojimui   */
	    SFSA_IO_DESC *p_r;         /*   sujungimo su klientu-u�sakovu deskriptorius   */
	    char *p_file_name;         /*   siun�iamo failo pavadinimas   */
	    char *p_r_ip;              /*   kliento-u�sakovo IP adresas   */
	    in_addr_t r_addr;          /*   IP adresas u�ra�ytas sveikojo skai�iaus formate   */
	    struct in_addr in;         /*   IP adresui saugoti   */
	    unsigned short int r_port; /*   u�sakovo fail� serverio porto numeris   */
	    int infd;                  /*   siun�iamo failo deskriptorius   */
	    int br;                    /*   kiek bait� paviko nuskaityti i� failo   */
	    char *p_buf;               /*   buferis failo duomenyms saugoti   */
	    struct stat sb;            /*   failo sistemini� atributu lentel�   */
	    int obs;                   /*   optimalus skaitomo bloko dydis   */

	    /*
	     *   Kliento fail� serveris
	     */
	    for (;;) {

		for (;;) {
		    usleep(100);
		    p_inc=accept_connection(p_fws);
		    if (p_inc!=NULL)
			break;
		}

		if ((p_in_msg=recv_message(p_inc,WAIT_INDEFINITELY))==NULL) {
		    _PRINT_ERROR_MSG("sfsa_client.c: main()--->CHILD: Unable to receive message");
		    exit(0);
		}

		/*
		 *   Gautojo prane�imo nagrin�jimas
		 */
		switch (p_in_msg->type) {
		case MSGTYPE_OPERATION:
		    switch (p_in_msg->operation) {
		    case OPERATION_TRANSMITFILE:

			/*
			 *   Jeigu operacija rei�kia fail� perdavim�, tai kart�
			 *   su tokiu prane�imu gaunamas failo pavadinimas
			 */
			p_file_name=(char *)malloc(sizeof(char)*p_in_msg->data_size);
			memcpy(p_file_name,p_in_msg->p_data,p_in_msg->data_size);
			destroy_message(p_in_msg);

			/*
			 *   Toliau gaunamas kliento-u�sakovo fail� serverio IP
			 *   adresas
			 */
			p_in_msg=recv_message(p_inc,WAIT_INDEFINITELY);
			memcpy(&r_addr,p_in_msg->p_data,p_in_msg->data_size);
			destroy_message(p_in_msg);

			/*
			 *   Po adreso gaunamas porto numeris
			 */
			p_in_msg=recv_message(p_inc,WAIT_INDEFINITELY);
			memcpy(&r_port,p_in_msg->p_data,p_in_msg->data_size);
			destroy_message(p_in_msg);

			close_connection(p_inc);

			in.s_addr=r_addr;
			p_r_ip=(char *)inet_ntoa(in);

			/*
			 *   Bandymas prisijungti prie kliento-u�sakovo fail� serverio
			 */
			if ((p_r=open_connection(p_r_ip,r_port,WAIT_INDEFINITELY))==NULL) {
			    close_connection(p_r);
			    break;
			}

			/*
			 *   Kliento-tiek�jo fail� serveris bando atidaryti u�sakyt� fail� skaitymu�
			 *   jei to padaryti nepavyksta, klientui u�sakovui nusiun�iamas atitinkamas
			 *   prane�imas
			 */
			if ((infd=open(p_file_name,O_RDONLY))==-1) {
			    p_out_msg=build_message(MSGTYPE_OPERATION,OPERATION_FILENOTFOUND,0,NULL);
			    send_message(p_r,p_out_msg,WAIT_INDEFINITELY);
			    destroy_message(p_out_msg);
			    close_connection(p_r);
			    perror("sfsa_client.c: main()--->CHILD--->open()");
			    break;
			}

			/*
			 *   Klientui u�sakovui nusiun�iamas failo pavadinimas (ne pilnas kelias, o tik
			 *   pavadinimas)
			 */
			p_buf=extract_file_name(p_file_name);
			p_out_msg=build_message(MSGTYPE_OPERATION,OPERATION_FILETRANSMISSION_BEGIN,
						strlen(p_buf)+1,p_buf);
			send_message(p_r,p_out_msg,WAIT_INDEFINITELY);
			destroy_message(p_out_msg);

			/*
			 *   Nustatomas optimalus skaitymo buferio dydis
			 */
			if (fstat(infd,&sb)==-1) {
			    close_connection(p_r);
			    perror("sfsa_client.c: main()---CHILD--->fstat()");
			    break;
			}

			/*
			 *   Sukuriamas buferis, skaitomas failas, perskaityti duomenys talpinami
			 *   � bufer�, buferis nusiun�iamas kliento u�sakovo fail� serveriui
			 */
			/*   BUG: obs=sb.st_blksize;   */
			obs=1024;
			p_buf=(char *)malloc(sizeof(char)*obs);
			while ((br=read(infd,p_buf,obs))>0) {
			    p_out_msg=build_message(MSGTYPE_NOTYPE,OPERATION_NOOP,br,p_buf);
			    send_message(p_r,p_out_msg,WAIT_INDEFINITELY);
			    destroy_message(p_out_msg);
			}

			/*
			 *   Atlaisvinama buferio atmintis, u�daromas failas
			 */
			free(p_buf);
			close(infd);

			/*
			 *   Klientui-u�sakovui nusiun�iamas failo duomen� pabaigos po�ymis
			 */
			p_out_msg=build_message(MSGTYPE_OPERATION,OPERATION_FILETRANSMISSION_END,0,NULL);
			send_message(p_r,p_out_msg,WAIT_INDEFINITELY);
			destroy_message(p_out_msg);

			/*
			 *   Sujungimas u�daromas
			 */
			close_connection(p_r);

			break;
		    case OPERATION_FILETRANSMISSION_BEGIN:
		    {
			int outfd;

			/*
			 *   I� gauto prane�imo tapo ai�ku, jog operacija rei�kia u�sakyto failo
			 *   duomen� srauto gavim�: parodom atitinkam� prane�im�
			 */
			fprintf(stdout,"Beginning file transmission...\n");
			fflush(stdout);

			/*
			 *   Atidaromas failas, kuris turi b�ti tikra u�sakyto failo kopija
			 */
			if ((outfd=open((char *)p_in_msg->p_data,O_WRONLY|O_CREAT|O_EXCL,
					S_IRUSR|S_IWUSR|S_IRGRP|S_IWGRP))==-1) {
			    destroy_message(p_in_msg);
			    close_connection(p_inc);
			    perror("sfsa_client.c: main()--->CHILD--->open()");
			    break;
			}

			destroy_message(p_in_msg);

			/*
			 *   Parsisiun�iamas pirmas failo duomen� blokas
			 */
			p_in_msg=recv_message(p_inc,WAIT_INDEFINITELY);

			/*
			 *   Tikrinamas prane�imo tipas, jei tipas n�ra "operation", rei�kia
			 *   gavom failo duomenis. �is ciklas yra kartojamas iki to laiko, kol
			 *   gautojo prane�imo tipas nepasidarys "operation", tai reik� failo
			 *   duomen� siuntimo pabaidos po�ym�
			 */
			while (p_in_msg->type!=MSGTYPE_OPERATION) {
			    write(outfd,p_in_msg->p_data,p_in_msg->data_size);
			    destroy_message(p_in_msg);
			    p_in_msg=recv_message(p_inc,WAIT_INDEFINITELY);
			}

			destroy_message(p_in_msg);

			/*
			 *   Kai failas parsisiustas, u�daromas jo deskriptorius, u�daromas
			 *   sujungimas su tiek�jo fail� serveriu ir spausdinamas atitinkamas
			 *   prane�imas
			 */
			close(outfd);
			close_connection(p_inc);
			fprintf(stdout,"File received\n");
			fflush(stdout);
		    }
			break;
		    case OPERATION_FILENOTFOUND:

			/*
			 *   Jeigu tiek�jo fail� serveris neranda u�sakyto failo, jis nusiun�ia
			 *   atitinkam� prane�im�, kuris yra apdorojamas �itoje vietoje
			 */
			fprintf(stdout,"File was not found on the host client... It was declared to be\n"
				"available by the export file, but it is not available physically\n");
			fflush(stdout);
			close_connection(p_inc);
			break;
		    default:
			break;
		    }
		    break;
		default:
		    break;
		}
	    } /*   for   */
	} /*   inner block   */
    } /*    end child   */

    /*
     *   Bandom prisijungti prie serverio
     */
    if ((p_conn=open_connection(p_s_ip,s_port,CONNECTION_TIMEOUT))==NULL) {
	printf("Connection timeout after [%d] sec.\n",CONNECTION_TIMEOUT);
	return 0;
    }

    /*
     *   Dabar pagal protokol�, reikia nusiusti vartotojo vard�
     */
    if ((p_msg=build_message(MSGTYPE_OPERATION,OPERATION_REGISTER_USER,strlen(p_uname)+1,p_uname))==NULL) {
	close_connection(p_conn);
	printf("Unable to build registration message. Exiting...\n");
	return 0;
    }

    send_message(p_conn,p_msg,WAIT_0);
    destroy_message(p_msg);

    /*
     *   Nusiun�iamas fail� serverio porto numeris
     */
    if ((p_msg=build_message(MSGTYPE_NOTYPE,OPERATION_NOOP,sizeof(unsigned short int),&fw_port))==NULL) {
	close_connection(p_conn);
	printf("Unable to build registration message. Exiting...\n");
	return 0;
    }

    send_message(p_conn,p_msg,WAIT_0);
    destroy_message(p_msg);

    /*
     *   Klientas laukia atsakymo i� serverio
     */
    if ((p_msg=recv_message(p_conn,WAIT_INDEFINITELY))==NULL) {
	close_connection(p_conn);
	_PRINT_ERROR_MSG("Unable to receive welcome message from the server");
	return 0;
    }

    /*
     *   Nagrin�jamas gautas i� serverio prane�imas
     */
    switch (p_msg->type) {
    case MSGTYPE_CONNECTION:
	switch (p_msg->operation) {
	case CONNECTION_ACCEPTED:
	    refused=0;
	    printf("Connection was accepted by the server\n");
	    break;
	case CONNECTION_REFUSED:
	    refused=1;
	    printf("Connection was refused by the server\n");
	    break;
	default:
	    destroy_message(p_msg);
	    close_connection(p_conn);
	    printf("Incorrect welcome message received. Exiting...\n");
	    exit(0);
	    break;
	}
	break;
    default:
	destroy_message(p_msg);
	close_connection(p_conn);
	printf("Incorrect welcome message received. Exiting...\n");
	exit(0);
	break;
    }

    /*
     *   Spausdinamas serverio "welcome" prane�imas
     */
    printf("SERVER: %s\n",(char *)p_msg->p_data);
    destroy_message(p_msg);

    if (refused)
	exit(0);

    /*
     *   Paleid�iamas am�inas ciklas, kuriame nagrin�jamos ir vykdomos (korekti�kumo atveju)
     *   vartotojo �vestos komandos
     */
    for (;;) {
	cmd_ok=0;
	printf("SFSA command> ");
	scanf("%s",buf);

	/*
	 *   help - komanda parodo korekti�k� komand� s�ra��
	 */
	if (strcmp(buf,"help")==0) {
	    printf("help - this text\n");
	    printf("quit - quit the program\n");
	    printf("getfile - receive a file\n");
	    printf("show - show server's list of exported files\n");
	    printf("export - send list of exported files to the server\n");

	    cmd_ok=1; /*   korekti�kos komandos po�ymis   */
	}

	/*
	 *   quit - i�eiti i� programos
	 */
	if (strcmp(buf,"quit")==0) {
	    p_msg=build_message(MSGTYPE_OPERATION,OPERATION_UNREGISTER_USER,strlen(p_uname)+1,p_uname);

	    if (send_message(p_conn,p_msg,REGISTRATION_TIMEOUT)==-1)
		printf("Unable to send unregister message in %d sec., forcing to close connection...\n",
		       REGISTRATION_TIMEOUT);

	    destroy_message(p_msg);

	    close_connection(p_conn);

	    printf("Connection closed.\n");

	    kill(ch_id,SIGTERM);
	    exit(0);
	}

	/*
	 *   getfile - u�sisakyti norim� fail�
	 */
	if (strcmp(buf,"getfile")==0) {
	    printf("Enter file name: ");
	    scanf("%s",buf);

	    p_msg=build_message(MSGTYPE_OPERATION,OPERATION_TRANSMITFILE,strlen(buf)+1,buf);
	    send_message(p_conn,p_msg,WAIT_0);
	    destroy_message(p_msg);

	    p_msg=recv_message(p_conn,WAIT_INDEFINITELY);

	    printf("SERVER: %s\n",(char *)p_msg->p_data);

	    cmd_ok=1;
	}

	/*
	 *   show - parodo fail� s�ra�� kuriuos galima u�sisakyti (parsisiusti)
	 */
	if (strcmp(buf,"show")==0) {
	    p_msg=build_message(MSGTYPE_OPERATION,OPERATION_SENDLISTOFFILENAMES,0,NULL);
	    send_message(p_conn,p_msg,WAIT_0);
	    destroy_message(p_msg);

	    t=0;
	    printf("List of files exported by the server:\n");
	    do {
		p_msg=recv_message(p_conn,WAIT_INDEFINITELY);
		if (p_msg==NULL) {
		    printf("Unable to complete the command");
		    break;
		}

		switch (p_msg->type) {
		case MSGTYPE_OPERATION:
		    switch (p_msg->operation) {
		    case OPERATION_TRANSMISSION_BEGIN:
			t=1;
			break;
		    case OPERATION_TRANSMISSION_END:
			t=0;
			break;
		    }
		    break;
		case MSGTYPE_TEXT:
		    printf("%s\n",(char *)p_msg->p_data);
		    break;
		default:
		    break;
		}

		destroy_message(p_msg);
	    } while (p_msg&&t);

	    cmd_ok=1;
	}

	if (strcmp(buf,"export")==0) {
	    if (p_e_fn==NULL)
		printf("You didn't specify export file name, no files will be shared\n"
		       "You can only receive files shared by other users on the server\n");
	    else {
		send_export_file_names(p_conn,p_e_fn);
		printf("List of file names specified in the export file was sent successfully\n");
	    }

	    cmd_ok=1;
	}

	/*
	 *   Tikrinamas �vestos komandos korekti�kumo po�ymis, neteisingos komandos
	 *   atveju, parodomas atitinkamas prane�imas
	 */
	if (!cmd_ok) {
	    printf("Bad command, enter \"help\" for more information\n");
	}
    }

    return 0;
}

/*
 *   Privataus interfeiso implementacija
 */

/*
 *   send_export_file() - Naudojant duot�j� sujungimo deskriptori�, nusiun�ia eksportuojam� fail�
 *                        s�ra��
 */
int send_export_file_names(SFSA_IO_DESC *p_conn,const char *p_e_fn)
{
    FILE *p_e_file;          /*   eksporto failo deskriptorius   */
    char buf[256];           /*   �ia talpinamos nuskaitytos eilut�s   */
    SFSA_MESSAGE *p_out_msg; /*   prane�imas siuntimui   */



    if ((p_e_file=fopen(p_e_fn,"r"))==NULL) {
	perror("sfsa_client.c: send_export_file_names()");
	return -1;
    }

    while (!feof(p_e_file)) {
	fscanf(p_e_file,"%s\n",buf);
	if ((p_out_msg=build_message(MSGTYPE_OPERATION,OPERATION_ADDFILENAMETOLIST,strlen(buf)+1,buf))==NULL) {
	    _PRINT_ERROR_MSG("sfsa_client.c: send_export_file_names()");
	    return -1;
	}

	send_message(p_conn,p_out_msg,WAIT_0);
	destroy_message(p_out_msg);
    }

    fclose(p_e_file);

    return 0;
}

/*
 *   extract_file_name() - Pagal duot�j� piln� keli� iki failo,
 *                         gra�ina failo pavadinim�
 */
char *extract_file_name(char *p_file_name)
{
    char *p_str;

    p_str=p_file_name;
    while (*p_str++);
    while (*--p_str!='/'&&p_str>=p_file_name);

    return ++p_str;
}

/*
 *   usage() - Parodo programos paleidimo parametrus
 */
void usage(const char *p_prog_name)
{
    printf("USAGE: %s -u <user name> [-h] [-s <port number>]"
	   " [-a <server IP>] [-p <port number>] [-e <export file>]\n",p_prog_name);
    exit(0);
}

/*
 *   sig_processor() - Signal� apdorojimo procedura
 */
void fs_sig_processor(int sig)
{
    switch (sig) {
    case SIGTERM:
	fprintf(stdout,"File server: SIGTERM signal received, exiting...\n");
	fflush(stdout);
	exit(0);
	break;
    default:
	fprintf(stdout,"Unknown signal received: [%d]\n",sig);
	fflush(stdout);
	break;
    }
}
