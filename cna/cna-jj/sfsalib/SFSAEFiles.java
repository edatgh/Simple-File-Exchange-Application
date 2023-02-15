
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
 *   SFSAEFiles - Kliento eksportuojam� fail� tvarkymas. Kievienam kliento eksportuojamam failui,
 *                sukuriamas specialus deskriptorius, kuris vadinasi "SFSAEFile". �is desk. yra
 *                strukt�ra, kurioje saugomas kliento vardas, eksportuojamo failo pavadinimas ir
 *                informacija apie kliento fail� server�. Toki� desk. yra tiek kiek yra eksportuojam�
 *                fail�, o ne tiek kiek yra klient�.
 *
 *   VU MIF, CNA Pimras darbas, Java-Java
 *   Edvardas Ges, III grup�
 *   2004/03/05
 */

package sfsalib;

import java.net.*;

public class SFSAEFiles {
    /*
     *   Laukai
     */
    private SFSAEFile firstEFile; /*   rodykl� � pirm�j� s�ra�o element�   */

    /*
     *   Metodai
     */
    public synchronized SFSAEFile findEFile(String fileName,String clientName,boolean shortFileName) {

	SFSAEFile eFile=firstEFile; /*   eFile rodo � pirm� s�ra�o el.   */

	/*
	 *   Tikrinamas kiekvinas s�ra�o el. ir gra�inamas tas el. kuris atitinka s�lyg�
	 */
	while (eFile!=null) {
	    String fFileName=eFile.getFileName();

	    if (shortFileName)
		if (fFileName.lastIndexOf('/')!=-1)
		    fFileName=fFileName.substring(fFileName.lastIndexOf('/')+1);

	    if (fFileName.equals(fileName)&&eFile.getClientName().equals(clientName))
		return eFile;

	    eFile=eFile.getNext();
	}

	return null; /*   jei tokio el. n�ra, gra�inama "null" reik�m�   */
    }

    public synchronized int createEFile(String fileName,String clientName,InetAddress fsAddr,int fsPort) {

	/*
	 *   Reikia patikrinti ar s�ra�e tokio elemento dar n�ra
	 */
	if (findEFile(fileName,clientName,false)!=null)
	    return -1; /*   gra�inam klaid�, nes toks elementas jau yra   */

	/*
	 *   Sukuriame eksportuojam� fail� s�ra�o element�
	 */
	SFSAEFile eFile=new SFSAEFile();

	eFile.setFileName(fileName);
	eFile.setClientName(clientName);
	eFile.setFSAddr(fsAddr);
	eFile.setFSPort(fsPort);

	/*
	 *   Suri�ame s�ra�o elementus
	 */
	eFile.setPrev(null);
	eFile.setNext(firstEFile);

	if (firstEFile!=null)
	    firstEFile.setPrev(eFile);

	firstEFile=eFile;

	return 0;
    }

    public synchronized void deleteEFiles(String clientName) {
	SFSAEFile eFile=firstEFile;
	SFSAEFile eDead=null;

	while (eFile!=null) {
	    if (eFile.getClientName().equals(clientName)) {
		eDead=eFile;
		eFile=eFile.getNext();
		deleteEFile(eDead.getFileName(),clientName);
	    } else
		eFile=eFile.getNext();
	} /*   while   */
    }

    public synchronized int deleteEFile(String fileName,String clientName) {

	SFSAEFile deadFile=findEFile(fileName,clientName,false);

	if (deadFile==null)
	    return -1; /*   klaida, tokio elemento s�ra�e n�ra   */

	/*
	 *   dabar reikia pa�alinti surast� element� i� s�ra�o su s�lyga jog
	 *   s�ra�as liks vientisas
	 */
	if (deadFile.getPrev()!=null) {
	    if (deadFile.getPrev().setNext(deadFile.getNext())!=null)
		deadFile.getNext().setPrev(deadFile.getPrev());
	} else {
	    if (deadFile.getNext()!=null) {
		deadFile.getNext().setPrev(null);
		firstEFile=firstEFile.getNext();
	    } else {
		firstEFile=null;
	    } /*   else   */
	} /*   else   */

	return 0;
    }

    public synchronized void enumEFiles(SFSAEFileEnumerator eFileEnumerator) {

	SFSAEFile eFile=firstEFile;

	while (eFile!=null) {
	    eFileEnumerator.enum(eFile);
	    eFile=eFile.getNext();
	}
    }
}
