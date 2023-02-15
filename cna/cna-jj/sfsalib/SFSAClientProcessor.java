
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
 *   SFSAClientProcessor - Prisijungusio kliento aptarnavimas. Kaskart kai klientas prisijungia
 *                         prie serverio, paled¾iama nauja gija kuriai perduodamas kliento
 *                         socket'o deskriptorius ir rodykle i "SFSAEFiles" objekt± kuris u¾siima
 *                         eksportuojamù failù tvarkimu
 *
 *   VU MIF, CNA Pimras darbas, Java-Java
 *   Edvardas Ges, III grupì
 *   2004/03/05
 */

package sfsalib;

import java.net.*;
import java.io.*;

public class SFSAClientProcessor extends Thread {
    /*
     *   Laukai
     */
    private Socket incConn;    /*   prisijungusio kliento socket'o deskriptorius   */
    private SFSAEFiles eFiles; /*   PASTABA: ©is objektas yra vienintelis, t.y. bendras visoms gijoms   */

    private InputStream in;
    private OutputStream out;

    private String clientName;

    /*
     *   Metodai
     */

    /*
     *   Konstruktorius
     */
    public SFSAClientProcessor(Socket incConn,SFSAEFiles eFiles) {

	/*
	 *   Inicializuojam objekto laukus
	 */
	this.incConn=incConn;
	this.eFiles=eFiles;
    }

    public void closeAll() throws IOException {

	in.close();
	out.close();
	incConn.close();
	System.out.println("Connection with ["+clientName+"] was terminated");
	System.out.print("Removing all files associated with ["+clientName+"]... ");
	eFiles.deleteEFiles(clientName);
	System.out.println("Done");
    }

    public void run() {

	try {
	    in=incConn.getInputStream();
	    out=incConn.getOutputStream();

	    SFSAMessage m=new SFSAMessage(in,out);
	    boolean process=true;

	    /*
	     *   Pagal protokol± dabar klientas turìtu atsiusti savo prisijungimo
	     *   vard±
	     */
	    m.receive();

	    /*
	     *   Tikrinam ar klientas veikia pagal protokol±
	     */
	    if (!(m.getType()==SFSAMessage.MSGTYPE_OPERATION&&
		  m.getOperation()==SFSAMessage.OPERATION_REGISTER_USER)) {

		String text=new String("You are not accepted");
		m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,
				  SFSAMessage.CONNECTION_REFUSED,text.length(),text);
		m.send();

		/*   stop this thread somehow   */
		in.close();
		out.close();
		incConn.close();
	    }

	    /*
	     *   Viskas gerai, turim kliento vard±
	     */
	    clientName=m.getData();

	    /*
	     *   Siunèiam klientui "welcome" prane¹im±
	     */
	    String text=new String("Client ["+clientName+"] was accepted by the server");
	    m=new SFSAMessage(in,out,
			      SFSAMessage.MSGTYPE_OPERATION,
			      SFSAMessage.CONNECTION_ACCEPTED,
			      text.length(),text);
	    m.send();

	    while (process) {

		/*
		 *   Laukiame komandos
		 */
		m.receive();

		switch (m.getType()) {
		case SFSAMessage.MSGTYPE_OPERATION:
		    switch (m.getOperation()) {
		    case SFSAMessage.OPERATION_ADDFILENAMETOLIST:
			String fileName=m.getData();

			m.receive();
			int fsPort=Integer.parseInt(m.getData());

			InetAddress ia=incConn.getInetAddress();

			eFiles.createEFile(fileName,clientName,ia,fsPort);

			String ok=new String("OK");
			m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_TEXT,SFSAMessage.OPERATION_NOOP,
					  ok.length(),ok);
			m.send();

			break;
		    case SFSAMessage.OPERATION_SENDLISTOFFILENAMES:
			String sr=new String("Available files:");

			m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,
					  SFSAMessage.OPERATION_TRANSMISSION_BEGIN,sr.length(),sr);
			m.send();

			eFiles.enumEFiles(new SFSAEFileEnumerator() {
				public void enum(SFSAEFile eFile) {

				    SFSAMessage fm;

				    fm=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_TEXT,
						       SFSAMessage.OPERATION_NOOP,
						       eFile.getClientName().length(),eFile.getClientName());
				    fm.send();

				    String shortFileName=eFile.getFileName();

				    if (shortFileName.lastIndexOf('/')!=-1)
					shortFileName=shortFileName.substring(shortFileName.lastIndexOf('/')+1);

				    fm=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_TEXT,
						       SFSAMessage.OPERATION_NOOP,
						       shortFileName.length(),shortFileName);
				    fm.send();
				}
			    });
			sr=new String("End of list");
			m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,
					  SFSAMessage.OPERATION_TRANSMISSION_END,sr.length(),sr);
			m.send();
			break;
		    case SFSAMessage.OPERATION_TRANSMITFILE:

			String shortFileName=m.getData();

			m.receive();
			String fsClientName=m.getData();

			m.receive();
			String reqFSPort=m.getData();

			InetAddress reqFSAddr=incConn.getInetAddress();

			SFSAEFile eFile=eFiles.findEFile(shortFileName,fsClientName,true);

			if (eFile==null) {
			    String err=new String("Unable to locate client having that file");

			    m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_TEXT,SFSAMessage.OPERATION_NOOP,
					      err.length(),err);
			    m.send();

			    break;
			}

			try {
			    Socket clientSocket=null;
			    InputStream fsin=null;
			    OutputStream fsout=null;

			    clientSocket=new Socket(eFile.getFSAddr(),eFile.getFSPort());

			    fsin=clientSocket.getInputStream();
			    fsout=clientSocket.getOutputStream();

			    m=new SFSAMessage(fsin,fsout,
					      SFSAMessage.MSGTYPE_OPERATION,
					      SFSAMessage.OPERATION_TRANSMITFILE,
					      eFile.getFileName().length(),eFile.getFileName());
			    m.send();

			    m=new SFSAMessage(fsin,fsout,
					      SFSAMessage.MSGTYPE_OPERATION,
					      SFSAMessage.OPERATION_TRANSMITFILE,
					      reqFSAddr.getHostAddress().length(),reqFSAddr.getHostAddress());
			    m.send();

			    m=new SFSAMessage(fsin,fsout,
					      SFSAMessage.MSGTYPE_OPERATION,
					      SFSAMessage.OPERATION_TRANSMITFILE,
					      reqFSPort.length(),reqFSPort);
			    m.send();

			    String msg_ok="Request was sent to the client's File Server";

			    m=new SFSAMessage(in,out,
					      SFSAMessage.MSGTYPE_TEXT,
					      SFSAMessage.OPERATION_NOOP,
					      msg_ok.length(),msg_ok);
			    m.send();

			    fsin.close();
			    fsout.close();
			    clientSocket.close();


			} catch (IOException e) {
			    String err=new String("Error occured during connection to the client's File server");

			    m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_TEXT,SFSAMessage.OPERATION_NOOP,
					      err.length(),err);
			    m.send();

			    break;
			}
			break;
		    default:
			process=false;
			closeAll();
			break;
		    }
		    break;
		default:
		    process=false;
		    closeAll();
		    break;
		}
	    }
	} catch (IOException e) {

	} /*   catch   */
    }
}
