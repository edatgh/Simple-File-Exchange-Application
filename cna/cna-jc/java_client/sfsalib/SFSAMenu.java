
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
 *   SFSAMenu - Pagrindinis programos meniu
 *
 *   VU MIF, CNA Pimras darbas, Java-C
 *   Edvardas Ges, III grupì
 *   2004/03/14
 */

package sfsalib;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class SFSAMenu extends JMenuBar implements ActionListener {

    /*
     *   Laukai
     */
    private final JMenu[] menus={ new JMenu("File"),
				  new JMenu("Help") };

    private final JMenuItem[] fmItems={ new JMenuItem("Connect"),
					new JMenuItem("Disconnect"),
					new JMenuItem("Refresh"),
					new JMenuItem("Exit") };

    private final JMenuItem[] hmItems={ new JMenuItem("About") };

    private SFSAGUIClient nexus;

    /*
     *   Metodai
     */
    public SFSAMenu(SFSAGUIClient nexus) {

	this.nexus=nexus;

	/*
	 *   Konstruojame "File" meniù
	 */
	for (int i=0;i<fmItems.length;i++) {
	    fmItems[i].addActionListener(this);
	    fmItems[i].setFont(new Font("Courier",Font.PLAIN,10));
	    menus[0].add(fmItems[i]);
	}

	/*
	 *   "Help" meniù
	 */
	for (int i=0;i<hmItems.length;i++) {
	    hmItems[i].addActionListener(this);
	    hmItems[i].setFont(new Font("Courier",Font.PLAIN,10));
	    menus[1].add(hmItems[i]);
	}

	/*
	 *   Dabar sukuriame "Meniu bar"
	 */
	for (int i=0;i<menus.length;i++) {
	    menus[i].setFont(new Font("Courier",Font.PLAIN,10));
	    this.add(menus[i]);
	}
    }

    public void actionPerformed(ActionEvent e) {

	if (((JMenuItem)e.getSource()).getText().equals("Connect")) {
	    nexus.showOptionDialog();
	}

	if (((JMenuItem)e.getSource()).getText().equals("Disconnect")) {
	    nexus.closeConnection();
	}

	if (((JMenuItem)e.getSource()).getText().equals("Exit")) {
	    nexus.terminateApplication();
	}

	if (((JMenuItem)e.getSource()).getText().equals("Refresh")) {
	    nexus.refreshRemoteFilesWnd();
	    nexus.addExportFiles();
	}

	if (((JMenuItem)e.getSource()).getText().equals("Exit")) {
	    nexus.terminateApplication();
	}

	if (((JMenuItem)e.getSource()).getText().equals("About")) {
	    JOptionPane.showMessageDialog(null,"Simple file sharing application","SFSA",
					  JOptionPane.INFORMATION_MESSAGE);
	}
    }
}
