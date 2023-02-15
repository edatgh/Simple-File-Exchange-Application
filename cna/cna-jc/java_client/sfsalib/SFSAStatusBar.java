
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
 *   SFSAStatusBar - Apatinis langelis, kuriame parodomas vartotoj± informuojantis prane¹imas
 *
 *   VU MIF, CNA Pimras darbas, Java-C
 *   Edvardas Ges, III grupì
 *   2004/03/14
 */

package sfsalib;

import javax.swing.*;
import java.awt.*;

public class SFSAStatusBar extends JPanel {

    /*
     *   Laukai
     */
    private JLabel label;
    private String sStatus;

    /*
     *   Metodai
     */
    public SFSAStatusBar(String sInitialStatus) {

	sStatus=sInitialStatus;

	this.setLayout(new FlowLayout(FlowLayout.LEFT));
	this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY)));

	label=new JLabel(sStatus);
	label.setFont(new Font("Courier",Font.PLAIN,10));

	this.add(label);
    }

    public void setStatus(String sStatus) {
	this.sStatus=sStatus;
	label.setText(sStatus);
    }
}
