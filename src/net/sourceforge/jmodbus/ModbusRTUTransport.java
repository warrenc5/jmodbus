/*
 * ModbusRTUTransport.java
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

import javax.comm.*;
import java.io.*;

/**
 * Class to implement a RTU transport mechanisim for Modbus communication.  
 * This will allow a decice to communicate a serial link, using RTU (binary) 
 * encoding of the data.
 *
 * @author Kelvin Proctor
 */
public class ModbusRTUTransport implements ModbusTransport {
    
    /**
     * Frame seperator length.  This is the number of 
     * character times that is defined as a break in the
     * modbus RTU protocol.
     */
    public static double FRAME_SEPERATOR_LENGTH = 3.5;
    
    /**
     * The maximum length of a Modbus RTU message which is
     * the maximum Modbus message length plus one byte for
     * the CRC16.
     */
    public static int MAX_RTU_MESSAGE_LENGTH = Modbus.MAX_MESSAGE_LENGTH + 1 ;
    
    /**
     * Interger for the value that represents the number of milli
     * secods that should be used for the receieve timeout for the
     * serial port.
     */
    private int receieveTimeout;
    
    /**
     * Integer that represents the number of millis seconds that
     * is used to signify a break.  This will be based on
     * rounding up the number of milliseconds the the frame
     * seperator represents at the given baud rate
     */
    private int frameBreakTime;

    /**
     * The serial port over which communications will be
     * conducted.
     */
    private SerialPort port;

    /**
     * The OutputStream is used to send messages down the serial port.
     */
    private OutputStream out;

    /**
     * The InputStream is used to receive messages from the serial port.
     */
    private InputStream in;

    /**
     * Default constructor that will create it's own serial port
     * connection with the default port settings.
     */
    public ModbusRTUTransport() {
	//FIXME: This constructor will either be deleted
	// or code to create a serial port will go here...
    }

    /**
     * Constructor that passes a open serial port.  The serial port
     * may have any parameters, but must alredy be opened, ready for
     * communication.
     *
     * @author Kelvin Proctor
     *
     * @param port The serial port to conduct serial communications
     *             over.
     */
    public ModbusRTUTransport(SerialPort port) {

	this.port = port;

	// We need to work out how many bit times are in
	// a character.  We know we will allways have 1 start 
	// bit so start with that
	double characterLength = 1.0;

	// Get the number of data bits
	switch (port.getDataBits()) {
	case SerialPort.DATABITS_5:
	    characterLength += 5.0;
	    break;
	case SerialPort.DATABITS_6:
	    characterLength += 6.0;
	    break;
	case SerialPort.DATABITS_7:
	    characterLength += 7.0;
	    break;
	case SerialPort.DATABITS_8:
	    characterLength += 8.0;
	    break;
	default:
	    System.out.println("ModbusRTUTransport: Unknown number of data bits!");
	    return;
	}

	// Get the number of parity bits
	switch (port.getParity()) {
	case SerialPort.PARITY_NONE:
	    // nothing to add here
	    break;
	case SerialPort.PARITY_ODD:
	case SerialPort.PARITY_EVEN:
	case SerialPort.PARITY_MARK:
	case SerialPort.PARITY_SPACE:
	    characterLength += 1.0;
	    break;
	default:
	    System.out.println("ModbusRTUTransport: Unknown number of parity bits!");
	    return;
	}

	// Get the number of stop bits
	switch (port.getStopBits()) {
	case SerialPort.STOPBITS_1:
	    characterLength += 1.0;
	    break;
	case SerialPort.STOPBITS_2:
	    characterLength += 2.0;
	    break;
	case SerialPort.STOPBITS_1_5:
	    characterLength += 1.5;
	    break;
	default:
	    System.out.println("ModbusRTUTransport: Unknown number of stop bits!");
	    return;
	}
	
	// Now calculated the frame break time based on the
	// baud rate, number of bits per character and the
	// frame seperator length
       	frameBreakTime = (int) Math.ceil( FRAME_SEPERATOR_LENGTH *
                                          characterLength *
					  1000 / port.getBaudRate() );

	// Try and get the InputStream
	try {
	    port.setInputBufferSize(MAX_RTU_MESSAGE_LENGTH);
	    in = port.getInputStream();
	}
	catch (Exception ex) {
	    System.out.println("ModbusRTUTransport: InputStream failed!");
	    return;
	}
	
	// Try to get the OutputStream
	try {
	    port.setOutputBufferSize(MAX_RTU_MESSAGE_LENGTH);
	    out = port.getOutputStream();
	}
	catch (Exception ex) {
	    System.out.println("ModbusASCIITransport: OutputStream failed!");
	    return;
	}

	// Print Message if in debug mode
	if (Modbus.debug >= 1) {
	    System.out.println("ModbusRTUTransport: constructor complete");
	}

    }

    /**
     * Method to send a Modbus frame via the transport media.  The return 
     * status of the function indicates if the transmission sucedded.
     *
     * @author Kelvin Proctor
     *
     * @param msg The Modbus Message to be sent.
     * @return    Transmission sucess flag, to indicate if the transmission
     *            was sucessful.
     */
    public boolean sendFrame(ModbusMessage msg) {
	return false;
    }
    
    /**
     * Method to receive a Modbus frame via the transport media.  The return 
     * value indicates the length of the frame.  This method will block until
     * the comminication path is terminated or a frame is sucessfully received.
     *
     * @author Kelvin Proctor
     *
     * @param msg The Modbus Message object for received data to be written into
     * @return    Receive sucess flag, to indicate if the receive was sucessful.
     */
    public boolean receiveFrame(ModbusMessage msg) {
	return false;
    }
    
    /**
     * Method to calculate the Cyclic Redundancy Check (CRC) on a byte
     * array, up to a specified number of bytes.
     * 
     * @author Kelvin Proctor
     * 
     * @param buff The byte array to calculate the CRC on.
     * @param length The number of bytes of the byte array to be used
     *               in the CRC claculation.
     * @return The CRC value calculated from the byte array. 
     */
    private static byte CRC(byte[] buff, int length) {
	
	// FIXME:  I know this is really a LRC at the
	// moment but I'll fix that later.

	int acc = 0;
	int fflsacc = 0;
	
	for (int i=0; i<length; i++) {
	    acc += buff[i];
	    acc &= 0xFF;
	}
	
	fflsacc = (0xFF - acc) & 0xFF;
	
	return ((byte)(fflsacc+1));	
    }

    public void disconnect() {}
}











