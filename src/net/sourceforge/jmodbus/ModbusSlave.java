/*
 * ModbusSlave.java
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

/**
 * Class to represent a Modbus Slave device.  This is a base class
 * that will be extended by classes representing the different transports.
 * This class contains the knowledge that is required to generate a modbus 
 * request and parse the response.  It also implements Runnable so it can be
 * wrapped up into a Thread to run independantly from other threads that might
 * on any of the modbus registers or coils.
 *
 * @author Kelvin Proctor
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ModbusSlave extends Modbus implements Runnable {
    
	private static final Logger log = LoggerFactory.getLogger(ModbusTCPTransport.class.getName());
    // Registers and coils to read from and write to
    private ModbusRegisterBank input_registers;
    private ModbusRegisterBank output_registers;
    private ModbusCoilBank input_coils;
    private ModbusCoilBank output_coils;
    
    // Flag to indicate what type of operations
    // this slave device accepts
    private boolean input_coils_enabled = false;
    private boolean output_coils_enabled = false;
    private boolean input_registers_enabled = false;
    private boolean output_registers_enabled = false;		
    
    // Modbus messages for message to be received into and	
    // sent from	
    private ModbusMessage request;	
    private ModbusMessage response;	
    
    // The slave address (or unit identifier) of this device	
    private byte slaveAddress;
    
    // Information about the query
    private int number;
    private int offset;
    private int length;
    private int value;

    /**
     * Class constructor.  Accepts a ModbusTransport object that
     * is passed to the master. 
     *
     * @author Kelvin Proctor
     *
     * @param transport The ModbusTransport object that should be used by this
     *                  Modbus object to send and recieve Modbus frames.    
     * @param slaveAddress The slave address (or unit indetifier) of this   
     *                     slave device.
     */
    public ModbusSlave(ModbusTransport transport, int slaveAddress) {
        super(transport);				
	request = new ModbusMessage();		
	response = new ModbusMessage();				
	this.slaveAddress = (byte) (slaveAddress & 0xFF);
    } 
    
    /** 
     * Set the reference to the Input Register Bank.  This
     * also tells the slave device that it is allowed to
     * process function relating to input registers.
     *
     * @author Kelvin Proctor
     *
     * @param regs The ModbusRegisterBank object that represents
     *             the input registers.
     */
    public void setInputRegisters(ModbusRegisterBank regs) {
	input_registers = regs;
	input_registers_enabled = true;

	if (debug >= 3) {
	    log.debug("ModbusSlave: Input Registers Enabled");
	}
    }
    
    /** 
     * Set the reference to the Output Register Bank.  This
     * also tells the slave device that it is allowed to
     * process function relating to output registers.
     *
     * @author Kelvin Proctor
     *
     * @param regs The ModbusRegisterBank object that represents
     *             the output registers.
     */
    public void setOutputRegisters(ModbusRegisterBank regs) {
	output_registers = regs;
	output_registers_enabled = true;

	if (debug >= 3) {
	    log.debug("ModbusSlave: Output Registers Enabled");
	}
    }
    
    /** 
     * Set the reference to the Input Coil Bank.  This
     * also tells the slave device that it is allowed to
     * process function relating to input coils.
     *
     * @author Kelvin Proctor
     *
     * @param regs The ModbusCoilBank object that represents
     *             the input coils.
     */
    public void setInputCoils(ModbusCoilBank coils) {
	input_coils = coils;
	input_coils_enabled = true;

	if (debug >= 3) {
	    log.debug("ModbusSlave: Input Coils Enabled");
	}
    }
    
    /** 
     * Set the reference to the Output Coil Bank.  This
     * also tells the slave device that it is allowed to
     * process function relating to output coils.
     *
     * @author Kelvin Proctor
     *
     * @param regs The ModbusCoilBank object that represents
     *             the output coils.
     */
    public void setOutputCoils(ModbusCoilBank coils) {
	output_coils = coils;
	output_coils_enabled = true;

	if (debug >= 3) {
	    log.debug("ModbusSlave: Output Coils Enabled");
	}
    }

    /**
     * The run method for the slave object.  This will cause
     * the object to continually scan for input requests and
     * generate responses, untill an unrecoverable error occurs,
     * when it will exit.  To do this it uses the processRequest
     * function and just loops untill it exits unsucessfully.
     *
     * @author Kelvin Proctor
     */
    public void run() {
	boolean retval = true;
	
	if (debug >= 1) {
	    log.debug("ModbusSlave: Starting run loop......");
	}

	// Just sit here and process requests all day...		
	while (retval) {
	    try {
			retval = processRequest();
		} catch (IOException e) {
            log.error(e.getMessage(),e);
		}
	}	
    }	
    
    /**
     * Function to process a single request.  This function will block untill
     * a request is recieved and then process it.  If the request can be
     * processed then the function return sucessfully.  If the request can
     * not be processed then it will return false.  If a modbus exception
     * occurs and an appropriate response is generated, this is considered
     * a sucess.
     *
     * @author Kelvin Proctor
     * @throws IOException 
     *
     */
    public boolean processRequest() throws IOException {
	
	// First we must get the request....
	if (!receiveFrame(request)) {
	    if (debug >= 2) {
		log.debug("ModbusSlave: receiveFrame failed!");
	    }			
	    return false;
	}
	
	// We must now check that the request was actually
	// addressed to us
	if (request.buff[0] != slaveAddress) {
	    
	    // We will return true for we have sucessfully processed 
	    // the request (by doing nothing as it wsn't for us)
	    if (debug >= 2) {
		log.debug("ModbusSlave: message not addressed to us, address:" + request.buff[0]);
	    }			
	    
	    return true; 
	}
	
	// We must now switch on the function code and decide what 
	// to do based on the different operations
	switch (request.buff[1]) {
	    
	case READ_MULTIPLE_REGISTERS:
	    // Print Message if in debug mode
	    if (debug >= 2) {
		log.debug("ModbusSlave: process READ_MULTIPLE_REGISTERS comand");
	    }
	    processReadMultipleRegisters();
	    break;
	    
	case READ_INPUT_REGISTERS:
	    // Print Message if in debug mode
	    if (debug >= 2) {
		log.debug("ModbusSlave: process READ_INPUT_REGISTERS comand");
	    }
	    processReadInputRegisters();
	    break;
	    
	case WRITE_MULTIPLE_REGISTERS:
	    // Print Message if in debug mode
	    if (debug >= 2) {
		log.debug("ModbusSlave: process WRITE_MULTIPLE_REGISTERS comand");
	    }
	    processWriteMultipleRegisters();
	    break;
	    
	default:
	    // The function code is unknown
	    // Print Message if in debug mode
	    if (debug >= 2) {
		log.debug("ModbusSlave: process unknown comand :" + request.buff[1]);
	    }
	    generateException(ILLEGAL_FUNCTION);
	    break;
	}
	
	// No set the transaction IT on the return message
	response.transID = request.transID;		
	if (sendFrame(response)) {

	    if (debug >= 2) {
		log.debug("ModbusSlave: Response sent correctly");
	    }

	    return true;
	}
	else {

	    if (debug >= 2) {
		log.debug("ModbusSlave: Response send failed!");
	    }

	    return false;
	}	
    }		
    
    // Prepare am exception message, given an exception code.
    private void generateException(byte exception_code) {
	
	// Print Message if in debug mode
	if (debug >= 2) {
	    log.debug("ModbusSlave: Exception with code "+ByteUtils.toHex(exception_code));
	}
	
	// Set the unit identifier
	response.buff[0] = slaveAddress;
	
	// Set function code
	response.buff[1] = (byte) (request.buff[1] + EXCEPTION_MODIFIER);
	
	// Set exception code
	response.buff[2] = exception_code;
	
	// Set the length of the response
	response.length = 3;
    }		

    // Process a message where the function code was READ_INPUT_REGISTERS
    // including preparing the message and returning the number of bytes to 
    // be returned
    private void processReadInputRegisters() {
	if (input_registers_enabled) {
	    // If this is a valid READ_INPUT_REGISTERS message
	    // then the body length must be 6.
	    // body byte 0 = unit identifier
	    // body byte 1 = function code
	    // body byte 2 = high byte of reference number
	    // body byte 3 = low byte of reference number
	    // body byte 4 = high byte of register count
	    // body byte 5 = low byte of register count
	    if (request.length != 6) {
		generateException(ILLEGAL_DATA_VALUE);
		return;
	    }
	    
	    // Get the reference number and number of registers
	    offset = ((request.buff[2] << 8) + (request.buff[3] << 0)) & 0xFFFF;
	    number = ((request.buff[4] << 8) + (request.buff[5] << 0)) & 0xFFFF;
	    // Print Message if in debug mode
	    if (debug >= 3) {
		log.debug("Offset: "+offset);
		log.debug("Number of Words: "+number);
	    }
	    
	    // We now need to check that this is within bounds of our
	    // input registers
	    if (offset+number > input_registers.getNumberRegisters()) {
		generateException(ILLEGAL_DATA_ADDRESS);
		return;
	    }
	    
	    // Calculate the length of the reply message
	    // Unit identifier / slave address (1 byte)
	    // Function Code (1 byte)
	    // Bytes to Follow (1 byte)
	    // Data Words (2 Bytes per word)
	    length = 3 + 2*number;
	    
	    // Check that the length is not greater than the maximum
	    // permissbale length
	    if (length > MAX_MESSAGE_LENGTH) {
		generateException(ILLEGAL_RESPONSE_LENGTH);
		return;
	    }
	    
	    // If we made is past all of that then start to assemble the message
	    
	    // Set the unit identifier
	    response.buff[0] = slaveAddress;
	    
	    // Set function code
	    response.buff[1] = READ_INPUT_REGISTERS;
	    
	    // Set the count of bytes
	    response.buff[2] = (byte) ((2*number) & 0xFF);
	    
	    // Set the register values
	    for (int i=0; i<number; i++) {
		value = input_registers.getRegister(i+offset);
		response.buff[3+(2*i)] = (byte) ((value >>> 8) & 0xFF);
		response.buff[4+(2*i)] = (byte) ((value >>> 0) & 0xFF);
	    } 

	    // Set the message length
	    response.length = length;
	}
	else {
	    generateException(ILLEGAL_DATA_ADDRESS);
	}
    }
    
    // Process a message where the function code was READ_MULTIPLE_REGISTERS
    // including preparing the message and returning the number of bytes to 
    // be returned
    private void processReadMultipleRegisters() {
	if (output_registers_enabled) {
	    // If this is a valid READ_MULTIPLE_REGISTERS message
	    // then the body length must be 6.
	    // body byte 0 = unit identifier
	    // body byte 1 = function code
	    // body byte 2 = high byte of reference number
	    // body byte 3 = low byte of reference number
	    // body byte 4 = high byte of register count
	    // body byte 5 = low byte of register count
	    if (request.length != 6) {
		generateException(ILLEGAL_DATA_VALUE);
		return;
	    }
	    
	    // Get the reference number and number of registers
	    offset = ((request.buff[2] << 8) + (request.buff[3] << 0)) & 0xFFFF;
	    number = ((request.buff[4] << 8) + (request.buff[5] << 0)) & 0xFFFF;
	    // Print Message if in debug mode
	    if (debug >= 3) {
		log.debug("Offset: "+offset);
		log.debug("Number of Words: "+number);
	    }
	    
	    // We now need to check that this is within bounds of our
	    // input registers
	    if (offset+number >output_registers.getNumberRegisters()) {
		generateException(ILLEGAL_DATA_ADDRESS);
		return;
	    }
	    
	    // Calculate the length of the reply message
	    // Unit identifier / slave address (1 byte)
	    // Function Code (1 byte)
	    // Bytes to Follow (1 byte)
	    // Data Words (2 Bytes per word)
	    length = 3 + 2*number;
	    
	    // Check that the length is not greater than the maximum
	    // permissbale length
	    if (length > MAX_MESSAGE_LENGTH) {
		generateException(ILLEGAL_RESPONSE_LENGTH);
		return;
	    }
	    
	    // If we made is past all of that then start to assemble the message
	    
	    // Set the unit identifier
	    response.buff[0] = slaveAddress;
	    
	    // Set function code
	    response.buff[1] = READ_MULTIPLE_REGISTERS;
	    
	    // Set the count of bytes
	    response.buff[2] = (byte) ((2*number) & 0xFF);
	    
	    // Set the register values
	    for (int i=0; i<number; i++) {
		value = output_registers.getRegister(i+offset);
		response.buff[3+(2*i)] = (byte) ((value >>> 8) & 0xFF);
		response.buff[4+(2*i)] = (byte) ((value >>> 0) & 0xFF);
	    } 
	    
	    // Set the message length
	    response.length = length;
	}
	else {
	    generateException(ILLEGAL_DATA_ADDRESS);
	}
    }
    
    // Process a message where the function code was WRITE_MULTIPLE_REGISTERS
    // including preparing the message and returning the number of bytes to 
    // be returned
    private void processWriteMultipleRegisters() {
	if (output_registers_enabled) {
	    // If this is a valid WRITE_MULTIPLE_REGISTERS message
	    // then the body length must equate with the number of
	    // words the message says it will contain.
	    // body byte 0 = unit identifier
	    // body byte 1 = function code
	    // body byte 2 = high byte of reference number
	    // body byte 3 = low byte of reference number
	    // body byte 4 = high byte of word count
	    // body byte 5 = low byte of word count
	    // body byte 6 = bytes to follow
	    // body byte 7+2n = high byte of word n
	    // body byte 8+2n = low byte of word n
	    
	    // Get the reference number and number of registers
	    offset = ((request.buff[2] << 8) + (request.buff[3] << 0)) & 0xFFFF;
	    number = ((request.buff[4] << 8) + (request.buff[5] << 0)) & 0xFFFF;
	    // Print Message if in debug mode
	    if (debug >= 3) {
		log.debug("Offset: "+offset);
		log.debug("Number of Words: "+number);
	    }
	    
	    // We now need to check that this is within bounds of our
	    // input registers
	    if (offset+number > output_registers.getNumberRegisters()) {
		generateException(ILLEGAL_DATA_ADDRESS);
		return;
	    }
	    
	    // Now check the expected length
	    if (request.length != (7+2*number)) {
		generateException(ILLEGAL_DATA_VALUE);
		return;
	    }
	    
	    // Now check that the bytes to follow adds up to twice
	    // the word count
	    if (((int) request.buff[6]) != (2*number)) {
		generateException(ILLEGAL_DATA_VALUE);
		return;
	    }
	    
	    // now start reading the values and setting the registers
	    for (int i=0; i<number; i++) {
		value = ((request.buff[7+2*i] << 8) + (request.buff[8+2*i] << 0)) & 0xFFFF;
		output_registers.setRegister(i+offset, value);
	    } 
	    
	    // If we made is past all of that then start to assemble 
	    // the reply message
	    
	    // Set the unit identifier
	    response.buff[0] = slaveAddress;
	    
	    // Set function code
	    response.buff[1] = WRITE_MULTIPLE_REGISTERS;
	    
	    // Set the reference number and word counts
	    response.buff[2] = request.buff[2]; // ref high
	    response.buff[3] = request.buff[3]; // ref low
	    response.buff[4] = request.buff[4]; // number high
	    response.buff[5] = request.buff[5]; // number low
	    
	    // Set the body length
	    response.length = 6;
	}
	else {
	    generateException(ILLEGAL_DATA_ADDRESS);	
	}
    }
}













