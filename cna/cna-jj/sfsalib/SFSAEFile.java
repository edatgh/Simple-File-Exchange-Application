
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
 *   SFSAEFile - Kliento deskriptorius
 *
 *   VU MIF, CNA Pimras darbas, Java-Java
 *   Edvardas Ges, III grupì
 *   2004/03/05
 */

package sfsalib;

import java.net.*;

public class SFSAEFile {
    /*
     *   Laukai
     */
    private String fileName;     /*   eksportuojamo failo pavadinimas   */
    private String clientName;   /*   kliento  vardas   */
    private InetAddress fsAddr;  /*   kliento failù serverio IP adresas   */
    private int fsPort;          /*   kliento failù serverio porto numeris   */

    private SFSAEFile prevEFile; /*   rodyklìs bþtinos s±ra¹o   */
    private SFSAEFile nextEFile; /*   elementus sury¹ti   */

    /*
     *   Metodai
     */
    public void setFileName(String fileName) { this.fileName=fileName; }
    public String getFileName() { return fileName; }

    public void setClientName(String clientName) { this.clientName=clientName; }
    public String getClientName() { return clientName; }

    public void setFSAddr(InetAddress fsAddr) { this.fsAddr=fsAddr; }
    public InetAddress getFSAddr() { return fsAddr; }

    public void setFSPort(int fsPort) { this.fsPort=fsPort; }
    public int getFSPort() { return fsPort; }

    public SFSAEFile setPrev(SFSAEFile prevEFile) {

	this.prevEFile=prevEFile;

	return prevEFile;

    }
    public SFSAEFile getPrev() { return prevEFile; }

    public SFSAEFile setNext(SFSAEFile nextEFile) {

	this.nextEFile=nextEFile;

	return nextEFile;
    }
    public SFSAEFile getNext() { return nextEFile; }
}
