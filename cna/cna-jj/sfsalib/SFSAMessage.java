
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
 *   SFSAMessage - Prane¹imù mechanizmas. Tokiu budu bendrauja SFSA klientù failù serveriai ir
 *                 klientai su pagrindiniu serveriu
 *
 *   VU MIF, CNA Pimras darbas, Java-Java
 *   Edvardas Ges, III grupì
 *   2004/03/05
 */

package sfsalib;

import java.io.*;

public class SFSAMessage {
    /*
     *   Laukai
     */

    public static final int OPERATION_ADDFILENAMETOLIST=     0;
    public static final int OPERATION_SENDLISTOFFILENAMES=   1;
    public static final int OPERATION_TRANSMISSION_BEGIN=    2;
    public static final int OPERATION_TRANSMISSION_END=      3;
    public static final int OPERATION_REGISTER_USER=         4;
    public static final int OPERATION_UNREGISTER_USER=       5;
    public static final int OPERATION_TRANSMITFILE=          6;
    public static final int OPERATION_FILETRANSMISSION_BEGIN=7;
    public static final int OPERATION_FILEDATA=              8;
    public static final int OPERATION_FILETRANSMISSION_END=  9;
    public static final int OPERATION_FILENOTFOUND=          10;
    public static final int OPERATION_NOOP=                 -1;

    public static final int CONNECTION_ACCEPTED=0;
    public static final int CONNECTION_REFUSED=1;

    public static final int MSGTYPE_TEXT=0;
    public static final int MSGTYPE_OPERATION=1;
    public static final int MSGTYPE_CONNECTION=2;
    public static final int MSGTYPE_NOTYPE=-1;

    private InputStream in;   /*   i¹ kur gauti prane¹imus   */
    private OutputStream out; /*   kur siusti prane¹imus   */
    private int type;
    private int operation;
    private int dataSize;
    private String data;

    /*
     *   Metodai
     */
    public SFSAMessage(InputStream in,OutputStream out) {

	this.in=in;
	this.out=out;
    }

    public SFSAMessage(InputStream in,OutputStream out,
		       int type,int operation,int dataSize,String data) {

	this.in=in;
	this.out=out;

	this.type=type;
	this.operation=operation;
	this.dataSize=dataSize;
	this.data=data;
    }

    public int getType() { return type; }
    public int getOperation() { return operation; }
    public int getDataSize() { return dataSize; }
    public String getData() { return data; }

    public int send() {

	try {
	    out.write(type);
	    out.write(operation);
	    out.write(dataSize);

	    if (dataSize>0)
		out.write(data.getBytes(),0,dataSize);

	} catch (IOException e) {
	    System.err.println("Unable to send message");
	    return -1;
	}

	return 0;
    }

    public int receive() {

	byte[] bytes;

	try {

	    type=in.read();
	    operation=in.read();
	    dataSize=in.read();

	    if (dataSize>0) {
		bytes=new byte[dataSize];
		in.read(bytes,0,dataSize);
		data=new String(bytes);
	    } else
		data=null;
	} catch (IOException e) {
	    System.err.println("Unable to receive message");
	    return -1;
	}

	return 0;
    }
}
