/*
 * ModbusASCIITransport.java
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
 * Class to implement a ASCII transport mechanisim for Modbus communication.  
 * This will allow a decice to communicate a serial link, using ASCII 
 * encoding of the data.
 *
 * @author Kelvin Proctor
 */
public class ModbusASCIITransport implements ModbusTransport {
    
    /**
     * The start of frame marker for Modbus ASCII messages.
     */
    public static final byte START_FRAME_MARKER = 0x3A; // ':'
    
    /**
     * The first character of the end of frame marker for 
     * Modbus ASCII messages.
     */
    public static final byte END_FRAME_MARKER_1 = 0x0D; // CR

    /**
     * The first character of the end of frame marker for 
     * Modbus ASCII messages.
     */
    public static final byte END_FRAME_MARKER_2 = 0x0A; // LF

    /** 
     * The maximum length of a Modbus ASCII message that is based
     * on the maximum length of a Modbus message, with 2 characters
     * per byte encoding and start and end delimiters.
     * <P>
     * <OL>
     * <LI>start delimeter (1 byte)
     * <LI>message body (2 * MAX_MESSAGE_LENGTH bytes)
     * <LI>LRC (2 bytes)
     * <LI>CR LF (2 bytes)
     * </OL>
     */
    public static final int MAX_ASCII_MESSAGE_LENGTH = (2*Modbus.MAX_MESSAGE_LENGTH)+5;
    
    /**
     * The serial port over which communications will be
     * conducted.
     */
    private SerialPort port;

    /**
     * The input buffer that will be used for storing 
     * the character stream messages as they are recieved.
     */
    private byte[] input_buffer;

    /**
     * The output buffer that will be used for storing 
     * the character stream messages before they are sent.
     */
    private byte[] output_buffer;

    /**
     * The PushbackInputStream is used to search for the start and end
     * of frame delimiters.  It will be wrapped around the InputSream from
     * the serial port.
     */
    private PushbackInputStream in;

    /**
     * The BufferedInputStream used to make sure that all low level 
     * read operations do not have to do any nasty byte-banning or
     * similar.
     */
    private BufferedInputStream bin;

    /**
     * The OutputStream is used to send messages down the serial port.
     */
    private OutputStream out;

    /**
     * Default constructor that will create it's own serial port
     * connection with the default port settings.
     */
    public ModbusASCIITransport() {
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
    public ModbusASCIITransport(SerialPort port) {

	this.port = port;
	
	input_buffer = new byte[MAX_ASCII_MESSAGE_LENGTH];
	output_buffer = new byte[MAX_ASCII_MESSAGE_LENGTH];
	
	// Try and attach the PushbackInputStream
	try {
	    bin = new BufferedInputStream(port.getInputStream(), MAX_ASCII_MESSAGE_LENGTH);
	    in = new PushbackInputStream(bin, MAX_ASCII_MESSAGE_LENGTH);
	}
	catch (Exception ex) {
	    if (Modbus.debug >= 1) {
		System.out.println("ModbusASCIITransport: PushbackInputStream failed!");
	    }
	    return;
	}
	
	// Try to get the OutputStream
	try {
	    out = port.getOutputStream();
	}
	catch (Exception ex) {
	    if (Modbus.debug >= 1) {
		System.out.println("ModbusASCIITransport: OutputStream failed!");
	    }
	    return;
	}

	// Print Message if in debug mode
	if (Modbus.debug >= 1) {
	    System.out.println("ModbusASCIITransport: constructor complete");
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
	
	int index = 0;
	int i = 0;
	byte newLRC = 0;

	// Print Message if in debug mode
	if (Modbus.debug >= 3) {
	    System.out.println("ModbusASCIITransport: Sending Frame.....");
	}

	// Start by setting the start delimeter
	output_buffer[index++] = START_FRAME_MARKER;

	// Now start to write all the values
	for (i=0; i<msg.length; i++) {
	    
	    // Write the high nibble
	    output_buffer[index++] = encodeNibble((byte)(((msg.buff[i] & 0xF0) >>> 4) & 0xFF));

	    // Write the low nibble
	    output_buffer[index++] = encodeNibble((byte)(((msg.buff[i] & 0x0F) >>> 0) & 0xFF));
	    
	}

	// Now calculate the LRC value
	newLRC = LRC(msg.buff, msg.length);
	
	// Write the high nibble of the LRC
	output_buffer[index++] = encodeNibble((byte)(((newLRC & 0xF0) >>> 4) & 0xFF));
	
	// Write the low nibble of the LRC
	output_buffer[index++] = encodeNibble((byte)(((newLRC & 0x0F) >>> 0) & 0xFF));
	
	// Write the end of frame markers
	output_buffer[index++] = END_FRAME_MARKER_1;
	output_buffer[index++] = END_FRAME_MARKER_2;

	// Print Message if in debug mode
	if (Modbus.debug >= 4) {
	    System.out.println("ModbusASCIITransport: Encoded Message");
	    System.out.println(ByteUtils.toHex(output_buffer,index));
	}

	// We now need to write the buffer to the output stream
	try {
	    out.write(output_buffer,0,index);
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
	    System.out.println("ModbusASCIITransport: Frame Sent.....");
	}

	// If that worked then return true
	return true;
    }
		
    /**
     * Method to receive a Modbus frame via the transport media.  The return 
     * value indicates the length of the frame.  This method will block until
     * the comminication path is terminated or a frame is sucessfully received.
     * <P>
     * Even if false is return the Modbus Message that is passed may be 
     * modified.  It's buffer is used to save excessive array copying.
     *
     * @author Kelvin Proctor
     *
     * @param msg The Modbus Message object for received data to be written into
     * @return    Receive sucess flag, to indicate if the receive was sucessful.
     */
    public boolean receiveFrame(ModbusMessage msg) {

	int count = 0;
	int recv = 0;
	int start_marker = -1;
	int end_marker = -1;
	int i = 0;
	int msg_length = 0;
	int decode_msg_length = 0;
	byte newLRC = 0;
	
	// Print Message if in debug mode
	if (Modbus.debug >= 3) {
	    System.out.println("ModbusASCIITransport: Receiveing Frame.....");
	}
	
	// We are first going to skip forward unitll we find a the
	// start of frame marker ':' 
	while (start_marker < 0 && count < MAX_ASCII_MESSAGE_LENGTH) {
	    // Start off by getting all the bytes
	    // that we are allowed to have
	    try {
		recv = in.read(input_buffer,count,MAX_ASCII_MESSAGE_LENGTH - count);
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
		    System.out.println("ModbusASCIITransport: Stream Closed, receive returning -1");
		}
		return false;
	    }
	    count += recv;
	    
	    // If we got past all of that we can assume we have read 
	    // at least 1 byte
	    
	    // Now checek to see of we can find the start of frame delimiter
	    for (i=0; i<count; i++) {
		if (input_buffer[i] == START_FRAME_MARKER) {
		    start_marker = i;
		    break;
		}
	    }
	}
	
	// We now need to see if we found the start marker.
	// if we have read a full buffer and have not found 
	// the start marker then we are in trouble and we'll
	// return false
	if (start_marker == -1) {
	    if (Modbus.debug >= 3) {
		System.out.println("ModbusASCIITransport: start marker not found");
	    }
	    return false;
	}

	// We now have a valid start marker....
	
	// We will now unread the start marker and all the bytes following so the
	// start marker will be at the front of the stream and then see how much
	// of a message we need to read to try and find the end marker
	
	// Unread some bytes
	try {
	    in.unread(input_buffer, start_marker, count-start_marker);
	}
	catch (IOException ex) {
	    if (Modbus.debug >= 3) {
		System.out.println(ex.getMessage());
		ex.printStackTrace();
	    }
	    return false;
	}
	
	// Reset the counter
	count = 0;

	// Read some more bytes
	while ((end_marker < 0) && (count < MAX_ASCII_MESSAGE_LENGTH)) {
	    // Start off by getting all the bytes
	    // that we are allowed to have
	    try {
		recv = in.read(input_buffer,count,MAX_ASCII_MESSAGE_LENGTH - count);
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
		    System.out.println("ModbusASCIITransport: Stream Closed, receive returning -1");
		}
		return false;
	    }
	    count += recv;
	    
	    // If we got past all of that we can assume we have read 
	    // at least 1 byte
	    
	    // Now checek to see of we can find the end of frame delimiter
	    // which is 2 chars so we will have to be carefull.
	    if (count > 2) {
		for (i=0; i<(count-1); i++) {
		    if ((input_buffer[i] == END_FRAME_MARKER_1) &&
		        (input_buffer[i+1] == END_FRAME_MARKER_2)) {
			end_marker = i;
			break;
		    }
		}
	    }
	}
	
	// We now need to see if we found the end marker.
	// if we have read a full buffer and have not found 
	// the end marker then we are in trouble and we'll
	// return false
	if (end_marker == -1) {
	    if (Modbus.debug >= 3) {
		System.out.println("ModbusASCIITransport: end marker not found");
	    }
	    return false;
	}
	
	// We now have a valid end marker....
	
	// We will now unread all the bytes past the end of the frame
 	try {
	    in.unread(input_buffer, end_marker+2, count-end_marker-2);
	}
	catch (IOException ex) {
	    if (Modbus.debug >= 3) {
		System.out.println(ex.getMessage());
		ex.printStackTrace();
	    }
	    return false;
	}
	
	// Set the length of the encoded message
	msg_length = end_marker + 2;
	
	// The message length *must* be at least 7 bytes long to contain
	// a valid 1 byte Modbus message.  It must also have an odd number
	// of bytes.  Check that this is the case
	if ((msg_length < 7) && (msg_length % 2 == 1)) {
	    if (Modbus.debug >= 3) {
		System.out.println("ModbusASCIITransport: ASCII message is too short or is not an odd number.  Length: " + msg_length);
	    }
	    return false;
	}
	
	// At this point we now know we have a properly delimetered frame
	// sitting in the input buffer
	
	// Print Message if in debug mode
	if (Modbus.debug >= 4) {
	    System.out.println("ModbusASCIITransport: Encoded Message");
	    System.out.println(ByteUtils.toHex(input_buffer,msg_length));
	}
	
	// We now need to start decoding the message, and then we will
	// check the LRC.  We will dump this into the message buffer, even
	// if we are going to return false.
	for (i=0; i<((msg_length-3)/2); i++) {
	    // We will put this in a try...catch loop for if a bogus
	    // character gets into the frame then this will throw an
	    // illegal argument exception an in that case we will just
	    // make the receiveFrame command return false
	    try {
		msg.buff[i] = decodeByte(input_buffer[1+2*i], input_buffer[2+2*i]);
	    }
	    catch (IllegalArgumentException ex) {
		if (Modbus.debug >= 3) {
		    System.out.println(ex.getMessage());
		}
		return false;
	    }
	}
	
	// i will be the length of the decoded message + 1 [LRC byte]
	decode_msg_length = i-1;
	
	// Calculate the LRC value
	newLRC = LRC(msg.buff, decode_msg_length);
	
	// Check the LRC value
	if (newLRC != msg.buff[decode_msg_length]) {
	    if (Modbus.debug >= 3) {
		System.out.println("ModbusASCIITransport: LRC check failed");
		System.out.println("Message LRC: " + ByteUtils.toHex(msg.buff[decode_msg_length]));
		System.out.println("Calculated LRC: " + ByteUtils.toHex(newLRC));
	    }
	    return false;
	}
	
        // If we got past that then we havea valid message in the msg buffer
	// and all that is left to do is set the message length and return true
	
	msg.length = decode_msg_length;

	// Print Message if in debug mode
	if (Modbus.debug >= 3) {
	    System.out.println("ModbusASCIITransport: Frame Received.....");
	}

	return true;
    }


    /**
     * Method to calculate the Longitudinal Redundancy Check (LRC) on a byte
     * array, up to a specified number of bytes.
     * 
     * @author Kelvin Proctor
     * 
     * @param buff The byte array to calculate the LRC on.
     * @param length The number of bytes of the byte array to be used
     *               in the LRC claculation.
     * @return The LRC value calculated from the byte array. 
     */
    private static byte LRC(byte[] buff, int length) {
	
	int acc = 0;
	int fflsacc = 0;
	
	for (int i=0; i<length; i++) {
	    acc += buff[i];
	    acc &= 0xFF;
	}
	
	fflsacc = (0xFF - acc) & 0xFF;
	
	return ((byte)(fflsacc+1));	
    }
    
    
    /** 
     * Function to decode a byte from two ASCII character nibbles.
     *
     * @author Kelvin Proctor
     *
     * @param high_nibble The high nibble character.
     * @param low_nibble The low nibble character
     * @return The decoded byte
     */
    private static byte decodeByte(byte high_nibble, byte low_nibble) {
	return (byte)( (decodeNibble(high_nibble) << 4) + 
                        (decodeNibble(low_nibble) << 0) );
    }


    /** 
     * Function to decode a nibble from an ASCII character.
     *
     * @author Kelvin Proctor
     *
     * @param nibble The nibble character.
     * @return The decoded nibble as a whole byte (range
     *         is therefore 0x00 to 0x0F.
     */
    private static byte decodeNibble(byte nibble) {
	
	switch(nibble) {
	    
	case 0x30:
	    return ((byte) 0x00);
	case 0x31:
	    return ((byte) 0x01);
	case 0x32:
	    return ((byte) 0x02);
	case 0x33:
 	    return ((byte) 0x03);
	case 0x34:
	    return ((byte) 0x04);
	case 0x35:
	    return ((byte) 0x05);
	case 0x36:
	    return ((byte) 0x06);
	case 0x37:
	    return ((byte) 0x07);
	case 0x38:
	    return ((byte) 0x08);
	case 0x39:
	    return ((byte) 0x09);
	case 0x41:
	    return ((byte) 0x0A);
	case 0x42:
 	    return ((byte) 0x0B);
	case 0x43:
	    return ((byte) 0x0C);
	case 0x44:
	    return ((byte) 0x0D);
	case 0x45:
	    return ((byte) 0x0E);
	case 0x46:
	    return ((byte) 0x0F);
	default:
	    throw(new IllegalArgumentException("ModbusASCIITransport: illegal nibble value " + nibble));
	}
    }
    
    /** 
     * Function to encode a nibble ASCII character from a nibble byte.
     *
     * @author Kelvin Proctor
     *
     * @param nibble The nibble byte (must be in the range
     *               0x00 to 0x0F).
     * @return The encoded nibble as an ASCII character.
     */
    private static byte encodeNibble(byte nibble) {
	
	switch(nibble) {
	    
	case 0x00:
	    return ((byte) 0x30);
	case 0x01:
	    return ((byte) 0x31);
	case 0x02:
	    return ((byte) 0x32);
	case 0x03:
 	    return ((byte) 0x33);
	case 0x04:
	    return ((byte) 0x34);
	case 0x05:
	    return ((byte) 0x35);
	case 0x06:
	    return ((byte) 0x36);
	case 0x07:
	    return ((byte) 0x37);
	case 0x08:
	    return ((byte) 0x38);
	case 0x09:
	    return ((byte) 0x39);
	case 0x0A:
	    return ((byte) 0x41);
	case 0x0B:
 	    return ((byte) 0x42);
	case 0x0C:
	    return ((byte) 0x43);
	case 0x0D:
	    return ((byte) 0x44);
	case 0x0E:
	    return ((byte) 0x45);
	case 0x0F:
	    return ((byte) 0x46);
	default:
	    throw(new IllegalArgumentException("ModbusASCIITransport: illegal nibble value" + nibble));
	}
    }
}












