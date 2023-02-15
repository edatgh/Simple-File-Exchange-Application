
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
 *   SFSAClient - Klientas. Klientinio socket'o sukurimas, u¾klausù serveriui siuntimas, failù
 *                serverio paleidimas
 *
 *   VU MIF, CNA Pimras darbas, Java-Java
 *   Edvardas Ges, III grupì
 *   2004/03/05
 */

import java.net.*;
import java.io.*;
import java.util.*;

import sfsalib.*;

/*
 *   Pagrindinì kliento klasì su "main()" metodu
 */
public class SFSAClient {

    /*
     *   Laukai
     */
    public static final int DEFAULT_SERVER_PORT_NUMBER=2048;

    /*
     *   Metodai
     */

    public static void usage(String cmd) {
	System.out.println("USAGE: "+
			   cmd+
			   " <client name> <export file> [<server IP/host>] [<server port number>] [<FS port number>]");
	System.exit(0);
    }

    public static void main(String[] args) throws IOException {

	Socket cSocket=null;
	InputStream in=null;
	OutputStream out=null;
	String thisClientName=null;
	String exportFile=null;
	int thisFSPort=SFSAFileServer.DEFAULT_FS_PORT_NUMBER;
	String serverAddress=new String("localhost");
	int serverPort=DEFAULT_SERVER_PORT_NUMBER;



	switch (args.length) {
	case 2:
	    thisClientName=args[0];
	    exportFile=args[1];
	    break;
	case 3:
	    thisClientName=args[0];
	    exportFile=args[1];
	    serverAddress=args[2];
	    break;
	case 4:
	    thisClientName=args[0];
	    exportFile=args[1];
	    serverAddress=args[2];
	    serverPort=Integer.parseInt(args[3]);
	    break;
	case 5:
	    thisClientName=args[0];
	    exportFile=args[1];
	    serverAddress=args[2];
	    serverPort=Integer.parseInt(args[3]);
	    thisFSPort=Integer.parseInt(args[4]);
	    break;
	case 0:
	case 1:
	default:
	    usage("SFSAClient");
	    break;
	}

	(new SFSAFileServer(thisFSPort)).start();

	/*
	 *   Jungiames prie serverio
	 */
	try {
	    System.out.println("Connecting to server at: ["+
			       serverAddress+
			       ":"+
			       Integer.toString(serverPort)+"]...");

	    cSocket=new Socket(serverAddress,serverPort);
	    in=cSocket.getInputStream();
	    out=cSocket.getOutputStream();

	} catch (UnknownHostException e) {
	    System.err.println("Unable to resolve host: "+serverAddress.toString());
	    System.exit(-1);
	} catch (IOException e) {
	    System.err.println("Unable to get I/O for the socket");
	    System.exit(-1);
	}

	System.out.println("Connected.");

	System.out.println("Sending login name...");

	/*
	 *   Siunèiame serveriui kliento vard±
	 */
	SFSAMessage m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,
				      SFSAMessage.OPERATION_REGISTER_USER,
				      thisClientName.length(),thisClientName);
	m.send();

	/*
	 *   Laukiame atsakymo
	 */
	m.receive();

	/*
	 *   Tikrinam ar serveris veikia pagal protokol±
	 */
	if (!(m.getType()==SFSAMessage.MSGTYPE_OPERATION&&
	      (m.getOperation()==SFSAMessage.CONNECTION_ACCEPTED||
	       m.getOperation()==SFSAMessage.CONNECTION_REFUSED))) {

	    /*   kill FS   */
	    in.close();
	    out.close();
	    cSocket.close();
	    System.err.println("Exiting with error...");
	    System.exit(0);
	}

	/*
	 *   Parodom prane¹im±, kurç atsiuntì serveris (koks jis bebþtu)
	 */
	System.out.println("SERVER: "+m.getData());

	/*
	 *   Dabar reikia patikrinti ar serveris sutinka mus aptarnauti
	 */
	if (m.getOperation()==SFSAMessage.CONNECTION_REFUSED) {
	    /*   kill FS   */
	    in.close();
	    out.close();
	    cSocket.close();
	    System.err.println("Exiting...");
	    System.exit(0);
	}

	/*
	 *   Interpretuojam vartotojo komandas
	 */
	BufferedReader cin=new BufferedReader(new InputStreamReader(System.in));

	String cmd;
	boolean cmd_ok;

	System.out.print("Enter command> ");
	while ((cmd=cin.readLine())!=null) {
	    cmd_ok=false;
	    if (cmd.length()==0) {
		cmd_ok=true;
	    }

	    if (cmd.equals("help")) {
		cmd_ok=true;
		System.out.println("help - this message");
		System.out.println("quit - exit the program");
		System.out.println("show - display list of files available on the server");
		System.out.println("export - export a file");
		System.out.println("getfile - receive a file from the server");
	    }

	    if (cmd.equals("quit")) {
		System.out.println("Exiting...");
		try {
		    in.close();
		    out.close();
		    cSocket.close();
		    cin.close();
		    System.exit(0);
		} catch (IOException e) {
		    System.err.println("Unable to close one or more descriptors");
		    System.exit(-1);
		}
	    }

	    if (cmd.equals("show")) {
		cmd_ok=true;
		m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,
				  SFSAMessage.OPERATION_SENDLISTOFFILENAMES,0,null);
		m.send();

		m.receive();
		if (m.getOperation()==SFSAMessage.OPERATION_TRANSMISSION_BEGIN) {
		    System.out.println("SERVER: "+m.getData());
		} else {
		    System.out.println("Unable to complete request");
		    System.out.print("Enter command> ");
		    continue;
		}

		int r=m.receive();
		while (r==0&&m.getOperation()!=SFSAMessage.OPERATION_TRANSMISSION_END) {
		    System.out.print("Client: "+m.getData()+", ");

		    m.receive();

		    System.out.println("File name: "+m.getData());

		    r=m.receive();
		}

		System.out.println("SERVER: "+m.getData());
	    }

	    if (cmd.equals("export")) {
		cmd_ok=true;
		try {
		    BufferedReader eIn=new BufferedReader(new FileReader(exportFile));

		    String fileName=null;

		    while ((fileName=eIn.readLine())!=null) {

			System.out.print("SERVER <--- ["+fileName+"]... ");

			/*
			 *   Siunèiame failo pavadinim±, kurç norime eksportuot
			 */
			m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,
					  SFSAMessage.OPERATION_ADDFILENAMETOLIST,
					  fileName.length(),fileName);
			m.send();

			/*
			 *   Dabar, pagal protokol± reikia nusiusti FS porto numerç
			 */
			m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_NOTYPE,SFSAMessage.OPERATION_NOOP,
					  Integer.toString(thisFSPort).length(),Integer.toString(thisFSPort));
			m.send();

			/*
			 *   Laukiam atsakymo. Kai gaunam, parodom, serverio prane¹im±
			 */
			m.receive();
			System.out.println(m.getData());
		    } /*   while   */

		    eIn.close();

		} catch (FileNotFoundException e) {
		    System.err.println("Unable to open export file: ["+exportFile+"]");

		} catch (IOException e) {

		}
	    }

	    if (cmd.equals("getfile")) {
		cmd_ok=true;

		System.out.print("Enter client name: ");
		String clientName=cin.readLine();

		System.out.print("Enter file name: ");
		String fileName=cin.readLine();

		m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,SFSAMessage.OPERATION_TRANSMITFILE,
				  fileName.length(),fileName);
		m.send();

		m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,SFSAMessage.OPERATION_TRANSMITFILE,
				  clientName.length(),clientName);
		m.send();

		m=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,SFSAMessage.OPERATION_TRANSMITFILE,
				  Integer.toString(thisFSPort).length(),Integer.toString(thisFSPort));
		m.send();

		m.receive();

		System.out.println("SERVER: "+m.getData());
	    }

	    if (!cmd_ok) {
		System.out.println("Bad command. User \"help\" for more information");
	    }

	    System.out.print("Enter command> ");
	}
    }
}
