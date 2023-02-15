
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
 *   SFSAServer - Serveris. Serverinio socket'o sukurimas, klientus aptarnaujanèios gijos
 *                sukurimas bei paleidimas
 *
 *   VU MIF, CNA Pimras darbas, Java-Java
 *   Edvardas Ges, III grupì
 *   2004/03/05
 */

import java.net.*;
import java.io.*;
import sfsalib.*;

/*
 *   Pagrindinì serverio klasì su "main()" metodu
 */
public class SFSAServer {

    /*
     *   Laukai
     */
    public static final int DEFAULT_SERVER_PORT_NUMBER=2048;

    private static int serverPortNumber=DEFAULT_SERVER_PORT_NUMBER;

    /*
     *   Metodai
     */
    public static void main(String[] args) throws IOException {

	/*
	 *   Jei buvo nurodytas komandinìs eilutìs argumentas, interpretuojam jç kaip
	 *   serverio porto numerç
	 */
	if (args.length==1)
	    try {
		serverPortNumber=Integer.parseInt(args[0]);
	    } catch (NumberFormatException e) {
		printUsage();
	    }

	if (args.length>1)
	    printUsage();

	SFSAEFiles eFiles=new SFSAEFiles();

	ServerSocket serverSocket=null;

	System.out.println("Starting server on port: ["+serverPortNumber+"]...");

	try {
	    serverSocket=new ServerSocket(serverPortNumber);
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    System.err.println("Unable to start server on port: ["+serverPortNumber+"]");
	    System.exit(-1);
	}

	System.out.println("Started.");

	/*
	 *   Serveris niekada nebaigia savo darbo
	 */
	while (true)
	    new SFSAClientProcessor(serverSocket.accept(),eFiles).start();

	/*
	 *   UNREACHABLE
	 */

	/* serverSocket.close(); */
	/* System.exit(0); */

    } /*   main()   */

    public static void printUsage() {
	System.err.println("USAGE: server [port number]");
	System.exit(0);
    }
}
