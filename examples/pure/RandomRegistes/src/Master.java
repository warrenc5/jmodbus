/*
 * Master.java
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

import java.net.*;
import java.io.*;
import net.sourceforge.jmodbus.*;

public class Master {

    public static void main(String[] args) {
	
	ModbusTCPMaster modbus;
	String host;
	String function;
	int port;
	int offset;
	int length;
	int[] results;
	boolean retval;
	int[] values;

	if (args.length != 5) {
	    System.out.println("usage: java Master function host port offset length");
	    System.out.println("function: read_output | read_input | write_output");
	    return;
	}

	function = args[0];
	host = args[1];
	port = Integer.parseInt(args[2]);
	offset = Integer.parseInt(args[3]);
	length = Integer.parseInt(args[4]);

	results = new int[length];

	if (!function.equals("read_output") 
	    && !function.equals("read_input") 
	    && !function.equals("write_output")) {
	    System.out.println("Invalid function!");
	    return;
	}

	try {
	    modbus = new ModbusTCPMaster(host, port);
	}
	catch (Exception ex) {
	    System.out.println(ex.getMessage());
	    ex.printStackTrace();
	    return;
	}

	// setup the values to write
	values = new int[100];
	for (int i=0; i<100; i++) {
	    values[i] = i;
	}

	try {
	    System.out.println("About to send request....");

	    if (function.equals("read_output")) { 
		retval = modbus.readMultipleRegisters(0,offset,length,0,results);
	    }
	    else if (function.equals("read_input")) {
		retval = modbus.readInputRegisters(0,offset,length,0,results);
	    }
	    else {
		retval = modbus.writeMultipleRegisters(0,offset,length,0,values);
	    }

	    System.out.println("Send function returned");
	    if (retval) {
		System.out.println("Transaction sucedded");

		if (function.equals("read_output")
		    || function.equals("read_input")) {
		    for (int i=0; i<length; i++) {
			System.out.println(Integer.toHexString(results[i]));
		    }
		}

	    }
	    else {
		System.out.println("Transaction failed");
	    }
	}
	catch (Exception ex) {
	    System.out.println(ex.getMessage());
	    ex.printStackTrace();
	}
    }
}















