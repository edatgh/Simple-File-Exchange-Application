
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
 *   SFSASOptionDialogThread - Langas, kuriame vartotojas privalìs çvesti tam tikrus duomenis,
 *                             kuriu reikia clientui
 *
 *   VU MIF, CNA Pimras darbas, Java-C
 *   Edvardas Ges, III grupì
 *   2004/03/14
 */

package sfsalib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SFSAOptionDialogThread extends Thread implements ActionListener {

    /*
     *   Laukai
     */
    private SFSAGUIClient parentThread;

    public static final String DEFAULT_SERVER_ADDRESS="localhost";
    public static final int DEFAULT_SERVER_PORT_NUMBER=2048;
    public static final int DEFAULT_FS_PORT_NUMBER=DEFAULT_SERVER_PORT_NUMBER+1;

    private String userName=null; /*   privalomas   */
    private String serverAddress=DEFAULT_SERVER_ADDRESS;
    private int serverPortNumber=DEFAULT_SERVER_PORT_NUMBER;
    private int fsPortNumber=DEFAULT_FS_PORT_NUMBER;
    private String exportFileName=null; /*   privalomas   */

    JFrame frame;

    JTextField userNameF;
    JTextField serverAddressF;
    JTextField serverPortNumberF;
    JTextField fsPortNumberF;
    JTextField exportFileNameF;

    /*
     *   Metodai
     */
    public SFSAOptionDialogThread(SFSAGUIClient parentThread) {
	this.parentThread=parentThread;
    }

    public void run() {

	frame=new JFrame("SFSA Client options");
	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

	Container cntPane=frame.getContentPane();

	cntPane.setLayout(new GridLayout(2,1,2,2));

	Font defaultFont=new Font("Courier",Font.PLAIN,10);

	JPanel p1=new JPanel();
	p1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY)));
	p1.setLayout(new GridLayout(5,2));

	JLabel userNameL=new JLabel("User name");
	userNameL.setFont(defaultFont);
	userNameF=new JTextField();
	userNameF.setFont(defaultFont);

	JLabel serverAddressL=new JLabel("Server address");
	serverAddressL.setFont(defaultFont);
	serverAddressF=new JTextField(DEFAULT_SERVER_ADDRESS);
	serverAddressF.setFont(defaultFont);

	JLabel serverPortNumberL=new JLabel("Server port number");
	serverPortNumberL.setFont(defaultFont);
	serverPortNumberF=new JTextField(Integer.toString(DEFAULT_SERVER_PORT_NUMBER));
	serverPortNumberF.setFont(defaultFont);

	JLabel fsPortNumberL=new JLabel("FS port number");
	fsPortNumberL.setFont(defaultFont);
	fsPortNumberF=new JTextField(Integer.toString(DEFAULT_FS_PORT_NUMBER));
	fsPortNumberF.setFont(defaultFont);

	JLabel exportFileNameL=new JLabel("Export file name");
	exportFileNameL.setFont(defaultFont);
	exportFileNameF=new JTextField();
	exportFileNameF.setFont(defaultFont);

	p1.add(userNameL);
	p1.add(userNameF);

	p1.add(serverAddressL);
	p1.add(serverAddressF);

	p1.add(serverPortNumberL);
	p1.add(serverPortNumberF);

	p1.add(fsPortNumberL);
	p1.add(fsPortNumberF);

	p1.add(exportFileNameL);
	p1.add(exportFileNameF);

	JPanel p2=new JPanel();
	p2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY)));
	GridBagLayout l=new GridBagLayout();
	GridBagConstraints c=new GridBagConstraints();
	p2.setLayout(l);

	JButton confirmBtn=new JButton("Confirm");
	c.anchor=GridBagConstraints.CENTER;
	l.setConstraints(confirmBtn,c);

	JButton cancelBtn=new JButton("Cancel");
	l.setConstraints(cancelBtn,c);

	confirmBtn.addActionListener(this);
	cancelBtn.addActionListener(this);

	confirmBtn.setFont(defaultFont);
	cancelBtn.setFont(defaultFont);

	p2.add(confirmBtn);
	p2.add(cancelBtn);

	cntPane.add(p1);
	cntPane.add(p2);

	frame.setSize(300,200);
	frame.show();
    }

    public void actionPerformed(ActionEvent e) {

	if (((JButton)e.getSource()).getText().equals("Confirm")) {

	    String s;

	    s=userNameF.getText();
	    if (s.equals("")) {
		JOptionPane.showMessageDialog(null,"User name must be entered","Error",JOptionPane.ERROR_MESSAGE);
		return;
	    }
	    userName=userNameF.getText();

	    s=serverAddressF.getText();
	    if (!s.equals(""))
		serverAddress=serverAddressF.getText();

	    s=serverPortNumberF.getText();
	    if (!s.equals(""))
		serverPortNumber=Integer.parseInt(serverPortNumberF.getText());

	    s=fsPortNumberF.getText();
	    if (!s.equals(""))
		fsPortNumber=Integer.parseInt(fsPortNumberF.getText());

	    s=exportFileNameF.getText();
	    if (s.equals("")) {
		JOptionPane.showMessageDialog(null,"Export file name must be entered","Error",
					      JOptionPane.ERROR_MESSAGE);
		return;
	    }
	    exportFileName=exportFileNameF.getText();

	    parentThread.connectToServer(this);
	}
	frame.dispose();
    }

    public String getUserName() { return userName; }
    public String getServerAddress() { return serverAddress; }
    public int getServerPortNumber() { return serverPortNumber; }
    public int getFsPortNumber() { return fsPortNumber; }
    public String getExportFileName() { return exportFileName; }

    public JFrame getFrame() { return frame; }
}
