
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
 *   SFSAFileServer - Failù serveris. Kiekvienas klientas privalo turìti toki serverç, tam, kad
 *                    kitù klientù failù serveriai galetu prie jo prisijungti ir siusti u¾klausas.
 *                    Failù serveris paleid¾iamas kaip gija.
 *
 *   VU MIF, CNA Pimras darbas, Java-Java
 *   Edvardas Ges, III grupì
 *   2004/03/05
 */

package sfsalib;

import java.net.*;
import java.io.*;

public class SFSAFileServer extends Thread {

    /*
     *   Laukai
     */
    public static final int DEFAULT_FS_PORT_NUMBER=2049;
    private int fsPort=DEFAULT_FS_PORT_NUMBER;

    /*
     *   Metodai
     */

    public SFSAFileServer(int fsPort) {
	this.fsPort=fsPort;
    }

    public void run() {

	ServerSocket fsSocket=null;
	Socket incFS=null;
	boolean listening=true;
	InputStream in=null;
	OutputStream out=null;

	try {
	    fsSocket=new ServerSocket(fsPort);
	} catch (IOException e) {
	    System.err.println("Unable to start FS at: ["+fsPort+"]");
	    return;
	    /*   how to return from the thread?   */
	}

	System.out.println("FS started at ["+fsPort+"]");

	while (listening) {
	    try {
		if ((incFS=fsSocket.accept())!=null) {
		    in=incFS.getInputStream();
		    out=incFS.getOutputStream();
		    SFSAMessage m=new SFSAMessage(in,out);

		    if (m.receive()==-1) {
			in.close();
			out.close();
			incFS.close();
			continue;
		    }

		    switch (m.getType()) {
		    case SFSAMessage.MSGTYPE_OPERATION:
			switch (m.getOperation()) {
			case SFSAMessage.OPERATION_TRANSMITFILE:

			    String fileName=m.getData();

			    System.out.println("FS: transmiting file: ["+fileName+"]");

			    m.receive();
			    String reqFSAddr=m.getData();

			    m.receive();
			    String reqFSPort=m.getData();

			    try {
				Socket clientSocket=null;
				InputStream fsin=null;
				OutputStream fsout=null;

				clientSocket=new Socket(reqFSAddr,Integer.parseInt(reqFSPort));

				fsin=clientSocket.getInputStream();
				fsout=clientSocket.getOutputStream();

				try {
				    FileInputStream fis=new FileInputStream(fileName);

				    String shortFileName;

				    if (fileName.lastIndexOf('/')!=-1)
					shortFileName=fileName.substring(fileName.lastIndexOf('/')+1);
				    else
					shortFileName=fileName;

				    m=new SFSAMessage(fsin,fsout,
						      SFSAMessage.MSGTYPE_OPERATION,
						      SFSAMessage.OPERATION_FILETRANSMISSION_BEGIN,
						      shortFileName.length(),shortFileName);
				    m.send();

				    /*
				     *   Keista, bet jeigu "buf" masyvo dydis yra pvz. 1Kb,
				     *   tai kito kliento FS gauna nes±moningus duomenis.
				     *   Tiksliai ne¾inau kodel taip yra... Viska i¹band¾iau.
				     *   Manau, tai gali bþti dìl Java+FreeBSD nesuderinamumo,
				     *   nes klaidos randomos, taisomos... gal i¹taisys kada nors...
				     */
				    int br;
				    byte[] buf=new byte[128];
				    while ((br=fis.read(buf,0,buf.length))>0) {

					String data=new String(buf);

					m=new SFSAMessage(fsin,fsout,
							  SFSAMessage.MSGTYPE_OPERATION,
							  SFSAMessage.OPERATION_FILEDATA,
							  br,data);
					m.send();
				    } /*   while   */

				    m=new SFSAMessage(fsin,fsout,
						      SFSAMessage.MSGTYPE_OPERATION,
						      SFSAMessage.OPERATION_FILETRANSMISSION_END,
						      0,null);
				    m.send();

				    m.receive();

				    fsin.close();
				    fsout.close();
				    clientSocket.close();

				} catch (IOException e) {
				    String err=new String("Unable to open/read requested file");
				    m=new SFSAMessage(fsin,fsout,
						      SFSAMessage.MSGTYPE_OPERATION,
						      SFSAMessage.OPERATION_FILENOTFOUND,
						      err.length(),err);
				    m.send();

				    fsin.close();
				    fsout.close();
				    clientSocket.close();
				}
			    } catch (IOException e) {
				System.err.println("Error occured while connecting to the requesters FS");
			    }

			    break;
			case SFSAMessage.OPERATION_FILETRANSMISSION_BEGIN:

			    fileName=m.getData();

			    try {
				FileOutputStream fos=new FileOutputStream(fileName);

				System.out.println("Beginning file transmission...");

				m.receive();
				while (m.getOperation()!=SFSAMessage.OPERATION_FILETRANSMISSION_END) {
				    if (m.getDataSize()>0)
					fos.write(m.getData().getBytes(),0,m.getDataSize());

				    System.out.println("data size: "+m.getDataSize());

				    m.receive();
				}

				m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_NOTYPE,
						  SFSAMessage.OPERATION_NOOP,0,null);
				m.send();

				fos.close();

				System.out.println("File received.");

			    } catch (IOException e) {
				System.out.println("Unable to create file: ["+fileName+"]");
			    }
			    break;
			case SFSAMessage.OPERATION_FILENOTFOUND:
			    System.out.println("File not found");
			    break;
			    default:
				break;
			}
			break;
		    default:
			in.close();
			out.close();
			incFS.close();
			break;
		    } /*   switch   */
		    in.close();
		    out.close();
		    incFS.close();
		} /*   if   */
	    } catch (IOException e) {
		System.err.println("FS I/O error");
		continue;
	    }
	} /*   while   */
    }
}
