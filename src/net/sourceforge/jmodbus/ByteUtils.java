/*
 * ByteUtils.java
 */

/* 
 * The jModbus project is distrubuted under the following license terms
 * 
 * Copyright (c) 2001 by The Java Modbus Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *  1.  Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer. 
 *  2.  Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution. 
 *  3.  Neither the name of the The Java Modbus Project nor the names of 
 *      its contributors may be used to endorse or promote products 
 *      derived from this software without specific prior written 
 *      permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.jmodbus;

/**
 * This class contains basic utilities for dumping byte arrays 
 * to standout oputput.  These are probably implemented somewhere in 
 * in the java API, but I can't find where.
 *
 * This class does not use string is a particulary efficient and
 * may have a sever performace impact on small footprint machines
 * that do not handle string well, such as a Dalllas Semi TINI.
 *
 * @author Kelvin Proctor
 */
public class ByteUtils {
 
    /**
     * Convert a single byte into a string representation of the byte
     * as two hexadecimal characters.
     *
     * @author Kelvin Proctor
     * 
     * @param b The byte to convert
     * @return String representation of the input byte.
     */	
    public static String toHex(byte b) {
	
	String temp = Integer.toHexString(b).toUpperCase();
	
	if (temp.length() == 1) {
	    temp = "0"+temp;
	}
	else if (temp.length() == 2) {
	    // do nothing
	}
	else {
	    temp = temp.substring(temp.length()-2, temp.length());
	}
	
	return temp;
	
    }
    
    /**
     * Convert a single short into a string representation of the short
     * as four hexadecimal characters.
     *
     * @author Kelvin Proctor
     * 
     * @param s The short to convert
     * @return String representation of the input short.
     */    
    public static String toHex(short s) {
	
	String temp = Integer.toHexString(s).toUpperCase();
	
	if (temp.length() == 1) {
	    temp = "000"+temp;
	}
	else if (temp.length() == 2) {
	    temp = "00"+temp;
	}
	else if (temp.length() == 3) {
	    temp = "0"+temp;
	}
	else if (temp.length() == 4) {
	    // do nothing
	}
	else {
	    temp = temp.substring(temp.length()-4, temp.length());
	}
	
	return temp;
	
    }
    
    /**
     * Convert a byte array into a string hex dump of the bytes
     * with a coulmn width of 8 bytes and a 2 byte offset number
     * printed on the side.  This function will add the required 
     * new line characters.
     *
     * @author Kelvin Proctor
     * 
     * @param b The byte array to dump
     * @param length The length of the array that is required
     *               to be dumped
     * @return String representation of the input byte array.
     */   
    public static String toHex(byte[] b, int length) {
	
	short len = (short) length;
	short count = 0;
	String str = "";
	
	while (len > 0) {
	    if (len <= 8) {
		str += toHex(count);
		str += ": ";
		while (len > 0) {
		    str += toHex(b[count]);
		    str += " ";
		    count++;
		    len--;
		}
	    }
	    else {
		str += toHex(count);
		str += ": ";
		for (int i=0; i<8; i++) {
		    str += toHex(b[count]);
		    str += " ";
		    count++;
		    len--;
		}
		str += "\n";
	    }
	}
	
	return str;
    }
    
    
}
