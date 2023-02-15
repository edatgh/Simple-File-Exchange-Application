
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

#ifndef __SFSA_USERS_H__
#define __SFSA_USERS_H__

#include <sfsa_io.h>

/*
 *   Tipai
 */
typedef struct _sfsa_file_name_t {
    char *p_file_name;                          /*   failo pavadinimas   */
    struct _sfsa_file_name_t *p_next_file_name; /*   rodyklì ç sekantç s±ra¹o element±   */
} SFSA_FILE_NAME;

typedef struct _sfsa_user_t {
    char *p_name;                      /*   vartotojo pasirinktas vardas   */
    unsigned short int fw_port;        /*   porto numeris, kur klientas laukia failo duomenù   */
    SFSA_IO_DESC *p_iod;               /*   rodyklì ç ivedimo/i¹vedimo deskriptoriù   */
    SFSA_FILE_NAME *p_first_file_name; /*   vartotojo eksportuojamù failù pavadinimù s±ra¹as   */
    struct _sfsa_user_t *p_prev_user;  /*   rodyklì ç vartotojù---   */
    struct _sfsa_user_t *p_next_user;  /*   ---s±ra¹o element±   */
} SFSA_USER;

/*
 *   Enumeracijos "callback" f-jù tipai
 */
typedef void (*P_ENUM_USERS_CALLBACK)(SFSA_USER *,void *);
typedef void (*P_ENUM_FILE_NAMES_CALLBACK)(SFSA_USER *,const char *,void *);

/*
 *   Vie¹as modulio interfeisas
 */
SFSA_USER *get_first_user(void);
SFSA_USER *add_user(const char *,unsigned short int,SFSA_IO_DESC *);
SFSA_USER *find_user(const char *);
int remove_user(SFSA_USER *);
void enum_users(P_ENUM_USERS_CALLBACK,void *);
SFSA_FILE_NAME *add_export_file_name(SFSA_USER *,const char *);
SFSA_FILE_NAME *find_export_file_name(SFSA_USER *,const char *);
int remove_export_file_name(SFSA_USER *,const char *);
void enum_file_names(SFSA_USER *,P_ENUM_FILE_NAMES_CALLBACK,void *);
void send_export_list(SFSA_USER *);
SFSA_USER *find_user_by_file_name(const char *);

#endif
