/*
 * RandomRegs.java
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

public class RandomRegs {

    public static void main(String[] args) {
	
	ServerSocket svrsocket = null;
	Socket socket = null;
	int unit_identifier = 0;
	Thread rndWorker;
	Thread modbWorker;
	ModbusTCPSlave modbus;
	int address_size;
	
	// Create modbus registers of appropriate size
	ModbusRegisterBank in_regs = new ModbusRegisterBank(100);
	ModbusRegisterBank out_regs = new ModbusRegisterBank(100);

	// Create random worker
	RandomWorker random = new RandomWorker(in_regs);

	// Now start the temperature worker as a thread
	rndWorker = new Thread(random);
	rndWorker.start();
	
	try {
	    svrsocket = new ServerSocket(ModbusTCPTransport.MODBUS_TCP_PORT);
	}
	catch (IOException ex) {
	    System.out.println(ex.getMessage());
	    ex.printStackTrace();
	    return;
	}
	
	while (true) {
	    
	    try {
		System.out.println("Waiting for request");
		socket = svrsocket.accept();
		System.out.println("Recieved a connection request");
		modbus = new ModbusTCPSlave(unit_identifier,socket);
		modbus.setInputRegisters(in_regs);
		modbus.setOutputRegisters(out_regs);
		modbWorker = new Thread(modbus);
		modbWorker.start();
	    }
	    catch (IOException ex) {
		System.out.println(ex.getMessage());
		ex.printStackTrace();
	    }
	}
    }
}








