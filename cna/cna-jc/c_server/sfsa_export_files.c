
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
 *   sfsa_export_files.c - Vartotojo exportuojamù failù tvarkimo mechanizmas
 *
 *   VU MIF, CNA Pimras darbas, C-C
 *   Edvardas Ges, III grupì
 *   2004/02/25
 */

#include <sfsa_common.h>
#include <sfsa_debug.h>
#include <sfsa_users.h>

/*
 *   Privatus modulio interfeisas
 */
void stage0(SFSA_USER *,void *);
void stage1(SFSA_USER *,const char *,void *);
void send_file_name(SFSA_USER *,const char *,SFSA_USER *);

/*
 *   Vie¹o modulio interfeiso realizacija
 */
SFSA_FILE_NAME *add_export_file_name(SFSA_USER *p_user,const char *p_file_name)
{
    SFSA_FILE_NAME *p_new_fn;

    if (p_user==NULL) {
	_PRINT_ERROR_MSG("sfsa_export_files.c: add_export_file_name() p_user==NULL");
	return NULL;
    }

    if (p_file_name==NULL) {
	_PRINT_ERROR_MSG("sfsa_export_files.c: add_export_file_name() p_file_name==NULL");
	return NULL;
    }

    if (find_export_file_name(p_user,p_file_name)!=NULL) {
	_PRINT_DEBUG_MSG("sfsa_export_files.c: add_export_file_name() File name already exists");
	return NULL;
    }

    if ((p_new_fn=(SFSA_FILE_NAME *)malloc(sizeof(SFSA_FILE_NAME)))==NULL) {
	_PRINT_ERROR_MSG("sfsa_export_files.c: add_export_file_name()--->malloc()");
	return NULL;
    }

    if ((p_new_fn->p_file_name=(char *)malloc(strlen(p_file_name)+1))==NULL) {
	_PRINT_ERROR_MSG("sfsa_export_files.c: add_export_file_name()--->malloc()");
	return NULL;
    }

    strncpy(p_new_fn->p_file_name,p_file_name,strlen(p_file_name)+1);

    p_new_fn->p_next_file_name=p_user->p_first_file_name;
    p_user->p_first_file_name=p_new_fn;

    _PRINT_DEBUG_MSGXX("sfsa_export_files.c: add_export_file_name() File added successfully:",
		     p_new_fn->p_file_name);

    return p_user->p_first_file_name;
}

SFSA_FILE_NAME *find_export_file_name(SFSA_USER *p_user,const char *p_file_name)
{
    SFSA_FILE_NAME *p_s_file_name;

    if (p_user==NULL) {
	_PRINT_ERROR_MSG("sfsa_export_files.c: find_export_file_name() p_user==NULL");
	return NULL;
    }

    if (p_file_name==NULL) {
	_PRINT_ERROR_MSG("sfsa_export_files.c: find_export_file_name() p_file_name==NULL");
	return NULL;
    }

    p_s_file_name=p_user->p_first_file_name;

    while (p_s_file_name) {
	if (strcmp(p_file_name,p_s_file_name->p_file_name)==0)
	    return p_s_file_name;

	p_s_file_name=p_s_file_name->p_next_file_name;
    }

    return NULL;
}

int remove_export_file_name(SFSA_USER *p_user,const char *p_file_name)
{

}

void enum_file_names(SFSA_USER *p_user,P_ENUM_FILE_NAMES_CALLBACK p_fnc,void *p_data)
{
    SFSA_FILE_NAME *p_file_name;

    p_file_name=p_user->p_first_file_name;

    while (p_file_name) {
	p_fnc(p_user,p_file_name->p_file_name,p_data);
	p_file_name=p_file_name->p_next_file_name;
    }
}

void send_export_list(SFSA_USER *p_dest_user)
{
    SFSA_MESSAGE *p_out_msg;

    p_out_msg=build_message(MSGTYPE_OPERATION,OPERATION_TRANSMISSION_BEGIN,0,NULL);
    send_message(p_dest_user->p_iod,p_out_msg,WAIT_0);
    destroy_message(p_out_msg);

    enum_users(stage0,p_dest_user);

    p_out_msg=build_message(MSGTYPE_OPERATION,OPERATION_TRANSMISSION_END,0,NULL);
    send_message(p_dest_user->p_iod,p_out_msg,WAIT_0);
    destroy_message(p_out_msg);
}

SFSA_USER *find_user_by_file_name(const char *p_file_name)
{
    SFSA_USER *p_user;
    SFSA_FILE_NAME *p_f_file_name;

    if ((p_user=get_first_user())==NULL) {
	_PRINT_DEBUG_MSG("sfsa_export_files.c: find_user_by_file_name()--->get_first_user()");
	return NULL;
    }

    while (p_user) {
	p_f_file_name=p_user->p_first_file_name;
	while (p_f_file_name) {
	    if (strncmp(p_f_file_name->p_file_name,p_file_name,strlen(p_f_file_name->p_file_name))==0)
		return p_user;

	    p_f_file_name=p_f_file_name->p_next_file_name;
	}

	p_user=p_user->p_next_user;
    }

    return NULL;
}

/*
 *   Privataus interfeiso realizacija
 */
void stage0(SFSA_USER *p_user,void *p_data)
{
    enum_file_names(p_user,stage1,p_data);
}

void stage1(SFSA_USER *p_m_user,const char *p_file_name,void *p_data)
{
    send_file_name(p_m_user,p_file_name,(SFSA_USER *)p_data);
}

void send_file_name(SFSA_USER *p_m_user,const char *p_file_name,SFSA_USER *p_dest_user)
{
    SFSA_MESSAGE *p_out_msg;
    char *p_buf;

    if ((p_buf=(char *)malloc(sizeof(char)*(256+64)))==NULL) {
	_PRINT_ERROR_MSG("sfsa_export_files.c: send_file_name()--->malloc()");
	return;
    }

    sprintf(p_buf,"User [%s] exports: [%s]",p_m_user->p_name,p_file_name);
    p_out_msg=build_message(MSGTYPE_TEXT,OPERATION_NOOP,strlen(p_buf)+1,(void *)p_buf);
    send_message(p_dest_user->p_iod,p_out_msg,1);
    destroy_message(p_out_msg);
    free(p_buf);
}
