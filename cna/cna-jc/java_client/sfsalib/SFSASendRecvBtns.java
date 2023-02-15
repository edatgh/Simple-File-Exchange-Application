
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
 *   SFSASendRecvBtns - Mygtukai, kuriù pagalba vykdomas apsikeitimas failais
 *
 *   VU MIF, CNA Pimras darbas, Java-C
 *   Edvardas Ges, III grupì
 *   2004/03/14
 */

package sfsalib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SFSASendRecvBtns extends JPanel implements ActionListener {

    /*
     *   Laukai
     */

    public static final String SEND_AB="--->";
    public static final String RECV_AB="<---";

    private JButton sendBtn;
    private JButton recvBtn;
    private SFSAGUIClient nexus;

    /*
     *   Metodai
     */
    public SFSASendRecvBtns(SFSAGUIClient nexus) {

	this.nexus=nexus;

	GridBagLayout l=new GridBagLayout();
	GridBagConstraints c=new GridBagConstraints();

	this.setLayout(l);
	this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY)));

	sendBtn=new JButton(SEND_AB);
	c.anchor=GridBagConstraints.CENTER;
	c.gridwidth=GridBagConstraints.REMAINDER;
	l.setConstraints(sendBtn,c);

	recvBtn=new JButton(RECV_AB);
	l.setConstraints(recvBtn,c);

	sendBtn.setFont(new Font("Courier",Font.BOLD,20));
	recvBtn.setFont(new Font("Courier",Font.BOLD,20));

	this.add(sendBtn);
	this.add(recvBtn);

	sendBtn.addActionListener(this);
	recvBtn.addActionListener(this);
    }

    public void freezeInput(boolean f) {
	sendBtn.setEnabled(!f);
	recvBtn.setEnabled(!f);
    }

    /*
     *   ActionListener
     */
    public void actionPerformed(ActionEvent e) {
	if (((JButton)e.getSource()).getText().equals(RECV_AB))
	    nexus.getFile();

	if (((JButton)e.getSource()).getText().equals(SEND_AB))
	    nexus.exportFile();
    }
}
