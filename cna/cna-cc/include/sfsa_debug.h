
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

#ifndef __SFSA_DEBUG_H__
#define __SFSA_DEBUG_H__

//#define SFSA_DEBUG_MODE
//#define SFSA_ERROR_MODE

#ifdef SFSA_DEBUG_MODE
#define _PRINT_DEBUG_MSG(msg) printf("DEBUG: %s\n",msg)
#define _PRINT_DEBUG_MSGX(msg,x) printf("DEBUG: %s %d\n",msg,x);
#define _PRINT_DEBUG_MSGXX(msg,xx) printf("DEBUG: %s %s\n",msg,xx);
#else
#define _PRINT_DEBUG_MSG(msg)
#define _PRINT_DEBUG_MSGX(msg,x)
#define _PRINT_DEBUG_MSGXX(msg,xx)
#endif

#ifdef SFSA_ERROR_MODE
#define _PRINT_ERROR_MSG(msg) printf("ERROR: %s\n",msg);
#define _PRINT_ERROR_MSGX(msg,x) printf("ERROR: %s %s\n",msg,x);
#else
#define _PRINT_ERROR_MSG(msg)
#define _PRINT_ERROR_MSGX(msg,x)
#endif

#endif
