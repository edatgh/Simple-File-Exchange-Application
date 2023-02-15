
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
 *   SFSAGUIClient - Grafi¹kos vartotojo s±sajos implementacija.
 *
 *   VU MIF, CNA Pimras darbas, Java-C
 *   Edvardas Ges, III grupì
 *   2004/03/14
 */

package sfsalib;

import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.io.*;

public class SFSAGUIClient extends Thread {

    /*
     *   Laukai
     */
    boolean waiting;
    private JFrame frame;
    private String sCaption;
    private SFSAMenu menu;
    private SFSAFilesWnd localFilesWnd;
    private SFSASendRecvBtns sendRecvBtns;
    private SFSAFilesWnd remoteFilesWnd;
    private SFSAStatusBar statusBar;

    private GridBagLayout l;
    private GridBagConstraints c;

    /*
     *   client/server
     */
    private SFSAFileServer fileServer;
    private boolean connected;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    private String userName;
    private String serverAddress;
    private int serverPortNumber;
    private int fsPortNumber;
    private String exportFileName;

    /*
     *   Metodai
     */
    public SFSAGUIClient(String sCaption) {
	this.sCaption=sCaption;
    }

    public void run() {

	/*
	 *   Sukuriamas pagrindinis langas
	 */
	frame=new JFrame(sCaption);
	frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

	Container cntPane=frame.getContentPane();

	l=new GridBagLayout();
	c=new GridBagConstraints();

	cntPane.setLayout(l);

	menu=new SFSAMenu(this);

	localFilesWnd=new SFSAFilesWnd("Local files");
	c.fill=GridBagConstraints.BOTH;
	c.anchor=GridBagConstraints.WEST;
	c.weightx=1.0;
	c.weighty=1.0;
	l.setConstraints(localFilesWnd,c);

	sendRecvBtns=new SFSASendRecvBtns(this);
	c.fill=GridBagConstraints.VERTICAL;
	c.anchor=GridBagConstraints.CENTER;
	c.weightx=0;
	c.weighty=0;
	Insets oi=c.insets;
	c.insets=new Insets(7,0,2,0);
	l.setConstraints(sendRecvBtns,c);
	c.insets=oi;

	remoteFilesWnd=new SFSAFilesWnd("Remote files");
	c.fill=GridBagConstraints.BOTH;
	c.anchor=GridBagConstraints.EAST;
	c.weightx=1.0;
	c.weighty=1.0;
	c.gridwidth=GridBagConstraints.REMAINDER;
	l.setConstraints(remoteFilesWnd,c);

	statusBar=new SFSAStatusBar("Welcome to SFSA GUI client");
	c.fill=GridBagConstraints.HORIZONTAL;
	c.anchor=GridBagConstraints.SOUTH;
	c.weightx=0;
	c.weighty=0;
	c.gridwidth=GridBagConstraints.REMAINDER;
	c.insets=new Insets(0,2,2,2);
	l.setConstraints(statusBar,c);

	frame.setJMenuBar(menu);

	cntPane.add(localFilesWnd);
	cntPane.add(sendRecvBtns);
	cntPane.add(remoteFilesWnd);
	cntPane.add(statusBar);

	frame.setSize(800,300);

	frame.show();

	/*
	 *   Sophisticated }:-)
	 */
	localFilesWnd.setPreferredSize(localFilesWnd.getSize(null));
	remoteFilesWnd.setPreferredSize(remoteFilesWnd.getSize(null));

	/*
	 *   Kol klientas nìra prisijungias prie serverio, vartotojo veiksmai nìra
	 *   apdorojami
	 */
	guiFreeze(true);
    }

    public void showOptionDialog() {
	SFSAOptionDialogThread odThread=new SFSAOptionDialogThread(this);
	odThread.start();
    }

    public synchronized void connectToServer(SFSAOptionDialogThread odThread) {

	userName=odThread.getUserName();
	serverAddress=odThread.getServerAddress();
	serverPortNumber=odThread.getServerPortNumber();
	fsPortNumber=odThread.getFsPortNumber();
	exportFileName=odThread.getExportFileName();

	if (userName==null||exportFileName==null) {
	    statusBar.setStatus("User name and export file name must be specified");
	    return;
	}

	/*
	 *   Jei FS jau buvo paleistas, tai reikia jç sustabdyti. Èia siulomas
	 *   keistas, bet veikiantis bþdas veikianèiam FS sustabdyti
	 */
	if (fileServer!=null) {
	    fileServer.quit();

	    try {
		Socket killSocket=new Socket(serverAddress,fsPortNumber);

		killSocket.close();
	    } catch (IOException e) {}

	    for (int i=0;i<1000;i++) {
		try {
		    fileServer.join();
		} catch (InterruptedException e) {}
	    }
	}

	/*
	 *   Dabar galima paleisti nauja FS (galbut, su tuo paèiu "fsPortNumber")
	 */
	fileServer=new SFSAFileServer(fsPortNumber);
	fileServer.setDaemon(true);
	fileServer.start();

	try {
	    if (connected) {
		statusBar.setStatus("Already connected");
		return;
	    }

	    statusBar.setStatus("Connecting...");

	    clientSocket=new Socket(serverAddress,serverPortNumber);
	    in=new DataInputStream(clientSocket.getInputStream());
	    out=new DataOutputStream(clientSocket.getOutputStream());

	    SFSAMessage msg=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,
					    SFSAMessage.OPERATION_REGISTER_USER,
					    userName.length()+1,userName);
	    msg.send();

	    short fsPn=(short)fsPortNumber;

	    byte[] bPn=new byte[2];

	    bPn[0]=(byte)(fsPn&0x00ff);
	    bPn[1]=(byte)(fsPn>>0x8);
	    String data=new String(bPn);

	    msg=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_NOTYPE,SFSAMessage.OPERATION_NOOP,data.length(),data);
	    msg.send();

	    msg.receive();

	    switch (msg.getType()) {
	    case SFSAMessage.MSGTYPE_CONNECTION:
		switch (msg.getOperation()) {
		case SFSAMessage.CONNECTION_ACCEPTED:
		    guiFreeze(false); /*   leid¾iamas vartotojo veiksmù aprdorojimas   */
		    connected=true;
		    statusBar.setStatus("Connected");
		    break;
		case SFSAMessage.CONNECTION_REFUSED:
		    statusBar.setStatus("Connection refused");
		    break;
		default:
		    closeConnection();
		    statusBar.setStatus("Server violates protocol");
		}
		break;
	    default:
		closeConnection();
		statusBar.setStatus("Server violates protocol");
		break;
	    }

	} catch (IOException e) {
	    statusBar.setStatus("Exception occured");
	}
    }

    public void refreshRemoteFilesWnd() {

	if (!connected) {
	    statusBar.setStatus("Not connected");
	    return;
	}

	guiFreeze(true);

	statusBar.setStatus("Updating list of available files...");

	/*
	 *   Trinam visus (jei tokie yra) failù pavadinimus, nes
	 *   tuoj bus gautas naujas s±ra¹as i¹ serverio
	 */
	remoteFilesWnd.removeAllFileNames();

	SFSAMessage msg=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,
					SFSAMessage.OPERATION_SENDLISTOFFILENAMES,0,null);
	msg.send();

	boolean t=false; /*   parodo ar vyksta duomenù perdavimas   */

	do {

	    msg.receive();

	    switch (msg.getType()) {
	    case SFSAMessage.MSGTYPE_OPERATION:
		switch (msg.getOperation()) {
		case SFSAMessage.OPERATION_TRANSMISSION_BEGIN:
		    t=true;
		    break;
		case SFSAMessage.OPERATION_TRANSMISSION_END:
		    t=false;
		    break;
		}
		break;
	    case SFSAMessage.MSGTYPE_TEXT:
		remoteFilesWnd.addFileName(msg.getData());
		break;
	    default:
		break;
	    }
	} while (t);

	statusBar.setStatus("Done");

	guiFreeze(false);
    }

    public void exportFile() {

	if (!connected) {
	    statusBar.setStatus("Not connected");
	    return;
	}

	guiFreeze(true);

	String fileName=localFilesWnd.getSelectedValue();

	if (fileName==null) {
	    statusBar.setStatus("No files selected");
	    guiFreeze(false);
	    return;
	}

	statusBar.setStatus("Exporting file name...");
	SFSAMessage msg=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,SFSAMessage.OPERATION_ADDFILENAMETOLIST,
					fileName.length()+1,fileName);
	msg.send();
	statusBar.setStatus("Done");

	guiFreeze(false);
	refreshRemoteFilesWnd();
    }

    public void getFile() {

	if (!connected) {
	    statusBar.setStatus("Not connected");
	    return;
	}

	guiFreeze(true);

	String remoteWndEntry=remoteFilesWnd.getSelectedValue();

	if (remoteWndEntry==null) {
	    statusBar.setStatus("No files selected");
	    guiFreeze(false);
	    return;
	}

	String fileName=remoteWndEntry.substring(remoteWndEntry.lastIndexOf('[')+1,
						 remoteWndEntry.lastIndexOf(']'));

	statusBar.setStatus("Receiving file: ["+fileName+"]...");

	SFSAMessage msg=new SFSAMessage(in,out,SFSAMessage.MSGTYPE_OPERATION,SFSAMessage.OPERATION_TRANSMITFILE,
					fileName.length()+1,fileName);
	msg.send();

	msg.receive();

	statusBar.setStatus(msg.getData());

	guiFreeze(false);
    }

    public void addExportFiles() {

	localFilesWnd.removeAllFileNames();

	statusBar.setStatus("Reading export file...");

	try {
	    BufferedReader eIn=new BufferedReader(new FileReader(exportFileName));

	    String fileName=null;

	    while ((fileName=eIn.readLine())!=null) {
		localFilesWnd.addFileName(fileName);
	    }

	    eIn.close();

	    statusBar.setStatus("Done");

	} catch (IOException e) {
	    statusBar.setStatus("Error reading export file");
	}
    }

    public void closeConnection() {

	if (!connected) {
	    statusBar.setStatus("Not connected");
	    return;
	}
	try {
	    if (in!=null)
		in.close();

	    if (out!=null)
		out.close();

	    if (clientSocket!=null)
		clientSocket.close();
	} catch (IOException e) {
	    statusBar.setStatus("Unable to close connection");
	}

	connected=false;
	guiFreeze(true);
	statusBar.setStatus("Disconnected");
    }

    public void terminateApplication() {
	closeConnection();
	frame.dispose();
	System.exit(0);
    }

    public void guiFreeze(boolean f) {
	sendRecvBtns.freezeInput(f);
    }

    public SFSAFilesWnd getLocalFilesWnd() { return localFilesWnd; }
    public SFSAFilesWnd getRemoteFilesWnd() { return remoteFilesWnd; }
}
