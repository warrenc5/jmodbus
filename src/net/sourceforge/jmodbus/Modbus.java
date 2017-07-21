/*
 * Modbus.java
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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a Modbus device.  This is a base class
 * that will be extended by classes representing both Master and Slave
 * implementations.
 *
 * @author Kelvin Proctor
 */
public class Modbus {
    
    /**
     * Flag to adjust the debug output level at compile time.
     * <P>
     * The debug levels are as follows:
     * <UL>
     * <LI> 0 - No debug output, systems runs silently
     * <LI> 1 - Very basic debug information.  Startup information and
     *          signals that a request or response cycle is starting
     * <LI> 2 - Basic debug output.  Basic send, receieve and processing 
     *          messages from both messaging and transport levels.
     * <LI> 3 - Detailed output of all operations but falling short of
     *          packet dumps.
     * <LI> 4 - Verbose output.  This includes full packet dumps at both
     *          messaging level and transport level.
     * </UL>
     */
    public static final int debug = Integer.parseInt(System.getProperty("jmodbus.debug","3"));
    private final Logger log = LoggerFactory.getLogger(Modbus.class.getName());
    
    /**
     * Command code to read multiple registers.
     */
    public static final byte READ_MULTIPLE_REGISTERS  = (byte) 0x03;
    
    /**
     * Command code to read imput registers.
     */
    public static final byte READ_INPUT_REGISTERS     = (byte) 0x04;
    
    /**
     * Command code to write multiple registers.
     */
    public static final byte WRITE_MULTIPLE_REGISTERS = (byte) 0x10;
    
    /**
     * Command code to write a single register.
     */
    public static final byte WRITE_SINGLE_REGISTER    = (byte) 0x06;

    
    /**
     * Command code to read multiple coils.
     */
    public static final byte READ_COILS               = (byte) 0x01;
    
    /**
     * Command code to write a single coil.
     */
    public static final byte WRITE_COIL               = (byte) 0x01;
    
    /**
     * Command code to read read input discretes.
     */
    public static final byte READ_INPUT_DISCRETES     = (byte) 0x02;
    
    /**
     * Command code to turn a single coil off.
     */
    public static final byte COIL_OFF                 = (byte) 0x00;
    
    /**
     * Command code to turn a single coil on.
     */
    public static final byte COIL_ON                  = (byte) 0xFF;

    
    /**
     * Command code to read the exception status.
     */
    public static final byte READ_EXCEPTION_STATUS    = (byte) 0x07;
    
    /**
     * Function code modfifer for exceptions.  This value is
     * added to the function code to signify that some sort of
     * error has occured in the function
     */
    public static final byte EXCEPTION_MODIFIER       = (byte) 0x80;

    /**
     * Exception code to signify that the function code is
     * unknown or illegal.
     */
    public static final byte ILLEGAL_FUNCTION         = (byte) 0x01;

    /**
     * Exception code to signify that the request contains
     * an illegal data address.
     */
    public static final byte ILLEGAL_DATA_ADDRESS     = (byte) 0x02;

    /**
     * Exception code to signify that the request contains
     * an illegal data value.
     */
    public static final byte ILLEGAL_DATA_VALUE       = (byte) 0x03;

    /**
     * Exception code to signify that the request whould have generated
     * an illegaly long response.
     */
    public static final byte ILLEGAL_RESPONSE_LENGTH  = (byte) 0x04;

    /**
     * Highest permissible address value.  This corresponds to a full
     * scale 16 bit unsigned integer.
     */
    public static final int ADDRESS_MAX            = 65535;		

    /**
     * Longest legal message length.
     */
    public static final int MAX_MESSAGE_LENGTH = 256;

    /**
     * The maximum value for a 16 bit unsigend integer.
     */
    public static int UINT16_MAX = (int) 0xFFFF;
    
    /**
     * The minimum value for a 16 bit unsigned integer.
     */
    public static int UINT16_MIN = (int) 0x0000;

    /**
     * The maximum value for a 8 bit unsigend integer.
     */
    public static int UINT8_MAX = (int) 0xFF;
    
    /**
     * The minimum value for a 8 bit unsigned integer.
     */
    public static int UINT8_MIN = (int) 0x00;

   /**
     * Modbus transport object that will be used to perform all
     * communication of Modbus messages.
     */
    protected ModbusTransport transport;

    /**
     * Class constructor.  This passes the class a ModbusTransport object
     * that will be used for communications.
     *
     * @author Kelvin Proctor
     *
     * @param transport The ModbusTransport object that should be used by this
     *                  Modbus object to send and receive Modbus frames.
     */
    protected Modbus(ModbusTransport transport) {
        this.transport = transport;
    } 

    /**
     * Method to send a Modbus frame via the transport media.  The return 
     * status of the function indicates if the transmission sucedded.  The
     * frame is send via the underlying transport mechanisim.
     *
     * @author Kelvin Proctor
     *
     * @param msg The Modbus Message to be sent.
     * @return    Transmission sucess flag, to indicate if the transmission
     *            was sucessful.
     */
    public boolean sendFrame(ModbusMessage msg) throws IOException {
	
	if (debug >= 4) {
	    log.debug("Modbus: Sending Frame");
	    log.debug("Transaction ID: " + msg.transID);
	    log.debug("Frame Length: " + msg.length);
	    log.debug(ByteUtils.toHex(msg.buff,msg.length));
	}

        return transport.sendFrame(msg);
    }

    /**
     * Method to receive a Modbus frame via the transport media.  The return 
     * value indicates the length of the frame.  This method will block until
     * the comminication path is terminated or a frame is sucessfully received.
     * The frame is received from the underlying transport mechanisim.     
     *
     * @author Kelvin Proctor
     *
     * @param msg The Modbus Message object for received data to be written into
     * @return    Receive sucess flag, to indicate if the receive was sucessful.
     */
    public boolean receiveFrame(ModbusMessage msg) throws IOException {
	
	if (debug >= 4) {
	    log.debug("Modbus: Receiveing Frame");
	    log.debug("Transaction ID: " + msg.transID);
	    log.debug("Frame Length: " + msg.length);
	    log.debug(ByteUtils.toHex(msg.buff,msg.length));
	}

        return transport.receiveFrame(msg);
    }
    
    public ModbusTransport getTransport() {
		return transport;
	}    
    
    public void disconnect() {
    	this.transport.disconnect();
    }
}





































































