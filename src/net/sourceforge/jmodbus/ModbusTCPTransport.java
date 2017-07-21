/*
 * ModbusTCPTransport.java
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

import java.io.*;
import java.net.*;

/**
 * Class to implement a TCP transport mechanisim for Modbus communication.  
 * This will allow a decice to communicate via ModbusTCP, also known as
 * Modnet.
 *
 * @author Kelvin Proctor
 */
public class ModbusTCPTransport implements ModbusTransport {
    
    /**
     * The TCP port number that Modbus TCP services should operate
     * over.
     */	
    public static final int MODBUS_TCP_PORT = 502;
    
    /**
     * The length (in bytes) of the header of a Modbus TCP packet.
     */
    public static final int HEADER_LENGTH          = 6;
    
    /**
     * The maximum number of bytes in the body of a Modbus TCP packet.
     */
    public static final int DATA_MAX               = 255;             
    
    /**
     * The maximum length (in bytes) of a Modbus TCP packet.
     */	
    public static final int MAX_TRANSACTION_LENGTH = HEADER_LENGTH + DATA_MAX;
    
    /**
     * The protocol identifier for Modbus TCP.  This value is used
     * to confirm that a message is acually a modbus TCP message and
     * not some other data.
     */	
    public static final short PROTOCOL_IDENTIFIER  = (short) 0x0000;
    
    // Socket that the transport will use to communicate via.  This
    // class is passed a socket to allow a slave implementation to
    // easily use ServerSockets to implement a multi threaded server.
    private Socket socket;
    
    // BufferedInputStream used for communication via the socket.
    private BufferedInputStream in;
    
    // OutputStream used for communication via the socket.
    private BufferedOutputStream out;
    
    // Small byte arrady for reading the ehader into each time
    private byte[] receive_header = new byte[HEADER_LENGTH];	
    private byte[] send_header = new byte[HEADER_LENGTH];
    
    // Counter for reading / writing / parsing byte buffers
    private int count = 0;
    private int recv = 0;
    
    // Protocol identifiers 
    private short protocol_identifier;
    
    // Flag to indicate if the header has been validated
    private boolean header_check = false;
    
    // Request and reply length variables
    private int request_body_length;
    private int reply_length;
    
    /**
     * Constructor for the ModbusTCPTransport.  The class requires
     * a socket to communicate over.  This constructor passes a reference
     * to this socket
     */	
    public ModbusTCPTransport(Socket socket) {
	this.socket = socket;
	// Setup the inoput and output streams
	try {
	    out = new BufferedOutputStream(socket.getOutputStream());
	    in = new BufferedInputStream(socket.getInputStream());
	}
	catch (IOException ex) {
	    // Print Message if in debug mode
	    if (Modbus.debug >= 1) {
		System.out.println(ex.getMessage());
		ex.printStackTrace();
	    }
	    return;
	}
	
	// Print Message if in debug mode
	if (Modbus.debug >= 1) {
	    System.out.println("ModbusTCPTransport: constructor complete");
	}
    }
    
    /**
     * Constructor for the ModbusTCPTransport.  The class requires
     * a socket to communicate over.  This constructor will create a
     * new socket on the specified port to the specified host. 
     */	
    public ModbusTCPTransport(String host, int port) {		
	
	// Setup the socket and input and output streams
	try {
	    socket = new Socket(host, port);	
	    out = new BufferedOutputStream(socket.getOutputStream());
	    in = new BufferedInputStream(socket.getInputStream());
	}
	catch (IOException ex) {
	    // Print Message if in debug mode
	    if (Modbus.debug >= 1) {
		System.out.println(ex.getMessage());
		ex.printStackTrace();
	    }
	    return;
	}
	
	// Print Message if in debug mode
	if (Modbus.debug >= 1) {
	    System.out.println("ModbusTCPTransport: constructor complete");
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
	
	// Print Message if in debug mode
	if (Modbus.debug >= 3) {
	    System.out.println("ModbusTCPTransport: Sending Frame.....");
	}
	
	// First create a header...
	// First place the transaction ID into the message
	send_header[0] = (byte) ((msg.transID >> 8) & 0xFF);
	send_header[1] = (byte) (msg.transID & 0xFF);
	
	// Then the protocol identifier
	send_header[2] = (byte) ((PROTOCOL_IDENTIFIER >> 8) & 0xFF);
	send_header[3] = (byte) (PROTOCOL_IDENTIFIER & 0xFF);
	
	// Then the length of the message to follow the header
	send_header[4] = (byte) 0x00;
	send_header[5] = (byte) (msg.length & 0xFF);
	
	try {
	    // Print Message if in debug mode
	    if (Modbus.debug >= 4) {
		System.out.println("ModbusTCPTransport: Header");
		System.out.println(ByteUtils.toHex(send_header,HEADER_LENGTH));
	    }
	    
	    // Now send the header then the body of the mesage			
	    out.write(send_header,0,HEADER_LENGTH);
	    out.write(msg.buff,0,msg.length);
	    out.flush();
	    
	}
	catch (IOException ex) {
	    if (Modbus.debug >= 3) {
		System.out.println(ex.getMessage());
		ex.printStackTrace();
	    }
	    return false;
	}
	
	// Print Message if in debug mode
	if (Modbus.debug >= 3) {
	    System.out.println("ModbusTCPTransport: Frame sent");
	}				
	return true;
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
	// Print Message if in debug mode
	if (Modbus.debug >= 3) {
	    System.out.println("ModbusTCPTransport: Receiveing Frame.....");
	}
	
	// Read HEADER_LENGTH bytes into the message buffer.  
	// Keep trying to read more bytes until a full
	// header worth of bytes has been received
	count = 0;
	while (count < HEADER_LENGTH) {
	    try {
		recv = in.read(receive_header,count,HEADER_LENGTH - count);
	    }
	    catch (IOException ex) {
		if (Modbus.debug >= 3) {
		    System.out.println(ex.getMessage());
		    ex.printStackTrace();
		}
		return false;
	    }
	    if (recv == -1) {
		// Print Message if in debug mode
		if (Modbus.debug >= 2) {
		    System.out.println("ModbusTCPTransport: Stream Closed, receive returning -1");
		}
		return false;
	    }
	    count += recv;
	}
	
	// Print Message if in debug mode
	if (Modbus.debug >= 4) {
	    System.out.println("ModbusTCPTransport: Header Received");
	    System.out.println(ByteUtils.toHex(receive_header,HEADER_LENGTH));
	}
	
	// We now need to see if the header is of a form that
	// we understand.  If it is then we get and process the 
	// rest of the message.  If not then we CLOSE the socket.
	header_check = true;
	
	// In Modbus TCP the transaction identifier is ignored
	// and blindly copied from query to response, so we should
	// copy that value to the integet passed to us for the
	// transaction ID now
	msg.transID = (int) ((receive_header[0] << 8) + receive_header[1]);
	
	
	// Check the protocol idnetifier
	protocol_identifier = (short) ((receive_header[2] << 8) + receive_header[3]);
	if (protocol_identifier != PROTOCOL_IDENTIFIER) {
	    // Print Message if in debug mode
	    if (Modbus.debug >= 3) {
		System.out.println("ModbusTCPTransport: incorrect protocol identifier: " + protocol_identifier);
	    }
	    header_check = false;
	}
	
	// Check the upper byte of the length field is 0 as
	// the maximum message size is 256 bytes
	if (receive_header[4] != (byte) 0x00) {
	    // Print Message if in debug mode
	    if (Modbus.debug >= 3) {
		System.out.println("ModbusTCPTransport: incorrect length, upper byte: " + receive_header[4]);
	    }
	    header_check = false;
	}
	
	// Now check that the lower byte of the length field is
	// greater than or equal to 2 as otherwise it can't be a 
	// valid message
	if (receive_header[5] == (byte) 0x00 || receive_header[5] == (byte) 0x01) {
	    // Print Message if in debug mode
	    if (Modbus.debug >= 3) {
		System.out.println("ModbusTCPTransport: incorrect length, lower byte: " + receive_header[5]);
	    }
	    header_check = false;
	}
	
	// If the header check for the packet failed then
	// the socket is to be closed, as per the Modbus TCP spec.
	if (!header_check) {
	    // Print Message if in debug mode
	    if (Modbus.debug >= 3) {
		System.out.println("ModbusTCPTransport: Header Check Failed!");
	    }
	    try {
		socket.close();
	    }
	    catch (Exception ex) {
				// do nothing
	    }
	    return false;
	}
	
	// Get the length of the message
	request_body_length = (int) receive_header[5];
	
	// Print Message if in debug mode
	if (Modbus.debug >= 3) {
	    System.out.println("ModbusTCPTransport: Message Body Length is " + request_body_length);
	}
	
	// Now get the rest of the message
	count = 0;
	while (count < request_body_length) {
	    try {
		recv = in.read(msg.buff,
			       count,
			       request_body_length - count);
	    }
	    catch (IOException ex) {
				// Print Message if in debug mode
		if (Modbus.debug >= 3) {
		    System.out.println(ex.getMessage());
		    ex.printStackTrace();
		}
		return false;
	    }
	    if (recv == -1) {
		// Print Message if in debug mode
		if (Modbus.debug >= 2) {
		    System.out.println("ModbusTCPTransport: Stream Closed, receive returning -1");
		}
		return false;
	    }
	    count += recv;
	}
	
	// Set the length field of the ModbusMessage
	msg.length = request_body_length;
		
	// Print Message if in debug mode
	if (Modbus.debug >= 3) {
	    System.out.println("ModbusTCPTransport: Frame receieved");
	}				
	return true;
    }
}







