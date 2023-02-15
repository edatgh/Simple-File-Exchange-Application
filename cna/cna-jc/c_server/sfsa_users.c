
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
 *   sfsa_users.c - Vartotojù tvarkimo mechanizmo realizacija
 *
 *   VU MIF, CNA Pimras darbas, C-C
 *   Edvardas Ges, III grupì
 *   2004/02/25
 */

#include <sfsa_common.h>
#include <sfsa_debug.h>
#include <sfsa_users.h>

/*
 *   GLOBALS
 */
SFSA_USER *p_first_user=NULL;

/*
 *   Vie¹o modulio interfeiso realizacija
 */
SFSA_USER *get_first_user(void)
{
    return p_first_user;
}

SFSA_USER *add_user(const char *p_name,unsigned short int fw_port,SFSA_IO_DESC *p_iod)
{
    SFSA_USER *p_new_user;

    if (p_iod==NULL) {
	_PRINT_ERROR_MSG("sfsa_users.c: add_user() p_iod==NULL");
	return NULL;
    }

    if (p_name==NULL) {
	_PRINT_ERROR_MSG("sfsa_users.c: add_user() p_name==NULL");
	return NULL;
    }

    if (find_user(p_name)!=NULL) {
	_PRINT_DEBUG_MSG("sfsa_users.c: add_user() User already exists");
	return NULL;
    }

    if ((p_new_user=(SFSA_USER *)malloc(sizeof(SFSA_USER)))==NULL) {
	_PRINT_ERROR_MSG("sfsa_users.c: add_user()--->malloc()");
	return NULL;
    }

    if ((p_new_user->p_name=(char *)malloc(strlen(p_name)+1))==NULL) {
	_PRINT_ERROR_MSG("sfsa_users.c: add_user()--->malloc()");
	free(p_new_user);
	return NULL;
    }

    strncpy(p_new_user->p_name,p_name,strlen(p_name)+1); /*   Kopijuojam vartotojo varda   */
    p_new_user->fw_port=fw_port;
    p_new_user->p_iod=p_iod;
    p_new_user->p_first_file_name=NULL;

    p_new_user->p_prev_user=NULL;
    p_new_user->p_next_user=p_first_user;

    if (p_first_user!=NULL)
	p_first_user->p_prev_user=p_new_user;

    p_first_user=p_new_user;

    return p_first_user;
}

SFSA_USER *find_user(const char *p_uname)
{
    SFSA_USER *p_user=p_first_user;

    if (p_uname==NULL) {
	_PRINT_ERROR_MSG("sfsa_users.c: find_user() p_uname==NULL");
	return NULL;
    }

    while (p_user) {
	if (strncmp(p_user->p_name,p_uname,strlen(p_user->p_name))==0)
	    return p_user;

	p_user=p_user->p_next_user;
    }

    return NULL;
}

int remove_user(SFSA_USER *p_user)
{
    SFSA_FILE_NAME *p_dead_fn;

    if (p_user==NULL) {
	_PRINT_ERROR_MSG("sfsa_io.c: remove_user() p_user==NULL");
	return -1;
    }

    _PRINT_DEBUG_MSGXX("sfsa_io.c: remove_user() Removing user:",(char *)p_user->p_name);

    free(p_user->p_name);            /*   atlaisvinam atmintç kur buvo vartotojo vardas   */
    close_connection(p_user->p_iod); /*   deskriptorius bus automati¹kai i¹trintas   */

    /*
     *   Reikia i¹trinti s±ra¹± pavadinimù eksportuojamù failù
     */
    while (p_user->p_first_file_name) {
	p_dead_fn=p_user->p_first_file_name;
	free(p_dead_fn->p_file_name);
	p_user->p_first_file_name=p_user->p_first_file_name->p_next_file_name;
	free(p_dead_fn);
    }

    /*
     *   Reikia suri¹ti vartotojù s±ra¹±, kad jis bþtù vientisas ir vienu elementu ma¾esnis, t.y.
     *   s±ra¹e nebebus to elemento, kuris atitinka naikinamajç vartotoj±
     */

    if (p_user->p_prev_user!=NULL) {
	if (p_user->p_prev_user->p_next_user=p_user->p_next_user)
	    p_user->p_next_user->p_prev_user=p_user->p_prev_user;
    } else {
	if (p_user->p_next_user!=NULL) {
	    p_user->p_next_user->p_prev_user=NULL;
	    p_first_user=p_first_user->p_next_user;
	} else {
	    p_first_user=NULL;
	}
    }
    free(p_user);

    return 0;
}

void enum_users(P_ENUM_USERS_CALLBACK p_uc,void *p_data)
{
    SFSA_USER *p_user;
    SFSA_USER *p_p_user;

    p_user=p_first_user;

    while (p_user) {
	p_p_user=p_user;
	p_user=p_user->p_next_user;
	p_uc(p_p_user,p_data);
    }
}
