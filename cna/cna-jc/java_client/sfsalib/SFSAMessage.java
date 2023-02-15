
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
 *   VU MIF, CNA Pimras darbas, Java-C
 *   Edvardas Ges, III grupì
 *   2004/03/14
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

    private DataInputStream in;   /*   i¹ kur gauti prane¹imus   */
    private DataOutputStream out; /*   kur siusti prane¹imus   */
    private int type;
    private int operation;
    private int dataSize;
    private String data;

    /*
     *   Metodai
     */
    public SFSAMessage(DataInputStream in,DataOutputStream out) {

	this.in=in;
	this.out=out;
    }

    public SFSAMessage(DataInputStream in,DataOutputStream out,
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
	    out.write(intToBytes(type),0,4);
	    out.write(intToBytes(operation),0,4);
	    out.write(intToBytes(dataSize),0,4);

	    if (dataSize>0) {
		if (dataSize-1==data.length()) {
		    out.write(data.getBytes(),0,dataSize-1);
		    byte[] nullChar=new byte[1];
		    nullChar[0]=0x00;
		    out.write(nullChar,0,1);
		} else
		    out.write(data.getBytes(),0,dataSize);
	    }

	    out.flush();

	} catch (IOException e) {
	    System.err.println("Unable to send message");
	    return -1;
	}

	return 0;
    }

    public int receive() {

	byte[] bytes;

	try {

	    bytes=new byte[4];

	    in.read(bytes,0,4);
	    type=bytesToInt(bytes);

	    in.read(bytes,0,4);
	    operation=bytesToInt(bytes);

	    in.read(bytes,0,4);
	    dataSize=bytesToInt(bytes);

	    if (type==MSGTYPE_TEXT)
		dataSize--; /*   NULL-terminating included  */

	    if (dataSize>0) {
		bytes=new byte[dataSize];
		in.read(bytes,0,dataSize);
		data=new String(bytes);

		if (type==MSGTYPE_TEXT) {
		    byte[] fake=new byte[1];
		    in.read(fake,0,1); /*   read NULL-terminating char   */
		}
	    } else
		data=null;
	} catch (IOException e) {
	    System.err.println("Unable to receive message");
	    return -1;
	}

	return 0;
    }

    /*
     *   ©ita f-ja konvertuoja duot±jç sveik±jç skaièiù ç baitù masyv±,
     *   tokiu bþdu Java negali siusti duomenis savo formatais, ji
     *   privalo siusti baitù sak±
     */
    public byte[] intToBytes(int d) {

	byte[] bytes=new byte[4];

	for (int i=0;i<=24;i+=8)
	    bytes[i/8]=(byte)((d>>i)&0x000000ff);

	return bytes;
    }

    /*
     *   Kai gaunamas prane¹imas, baitù seka turi bþti konvertuojama i
     *   sveikojo skaièiaus format±
     */
    public int bytesToInt(byte[] bytes) {
	int d;

	d=((bytes[3]<<24)|(bytes[2]<<16)|(bytes[1]<<8)|(bytes[0]));

	return d;
    }
}
