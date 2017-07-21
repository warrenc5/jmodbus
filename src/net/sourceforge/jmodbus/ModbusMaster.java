/*
 * ModbusMaster.java
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
 * Class to represent a Modbus Master device.  This is a base class
 * that will be extended by classes representing the different transports.
 * This class contains the knowledge that is required to generate a modbus 
 * request and parse the response.
 *
 * @author Kelvin Proctor
 */
public class ModbusMaster extends Modbus {
    
    // Modbus messages for message to be received into and	
    // sent from	
    private ModbusMessage request;	
    private ModbusMessage response;	
    
    /**
     * Class constructor.  Accepts a ModbusTransport object that
     * is passed to the master. 
     *
     * @author Kelvin Proctor
     *
     * @param transport The ModbusTransport object that should be used by this
     *                  Modbus object to send and recieve Modbus frames.
     */
    public ModbusMaster(ModbusTransport transport) {
        super(transport);
	
	request = new ModbusMessage();
	response = new ModbusMessage();
    } 
   
 
    /**
     * Function to read multiple registers from the slave device.
     * The function is passed a reference to an int array, the
     * reference of the first register and the number of registers
     * to read.  A boolean is returned indicating if the function 
     * returned sucessfully.  The the int array is too small then
     * an array index out of bounds exception will be thrown.
     * 
     * @author Kelvin Proctor
     *
     * @param reference The refernece number of the first
     *                 register to read.
     * @param length The number of register to be read.
     * @param results Refernce to the int array to which 
     *                results should be written.
     */
    public boolean readMultipleRegisters(int reference, 
					 int length,
					 int[] results) 
	throws IllegalArgumentException {
	return readMultipleRegisters(0,reference,length,0,results);
    }


    /**
     * Function to read multiple registers from the slave device.
     * The function is passed a reference to an int array, the
     * reference of the first register and the number of registers
     * to read.  A boolean is returned indicating if the function 
     * returned sucessfully.  The the int array is too small then
     * an array index out of bounds exception will be thrown.
     * 
     * @author Kelvin Proctor
     *
     * @param unitID The unit ID of the device to talk
     *               to (set to zero is unknown).
     * @param reference The refernece number of the first
     *                 register to read.
     * @param length The number of register to be read.
     * @param transID The transaction ID to use for this 
     *                transaction.
     * @param results Refernce to the int array to which 
     *                results should be written.
     */
    public boolean readMultipleRegisters(int unitID,
					 int reference, 
					 int length,
					 int transID,
					 int[] results) 
	throws IllegalArgumentException {
	
	if (debug >= 2) {
	    System.out.println("ModbusMaster: Preparing READ_MULTIPLE_REGISTERS command");
	}

	// The first thing we want to do is perform bounds
	// checking on the input variables.
	if (unitID > UINT8_MAX) {
	    throw new IllegalArgumentException("Unit ID is out of range of 8 bit UINT");
	}
	
	if (unitID < UINT8_MIN) {
	    throw new IllegalArgumentException("Unit ID is out of range of 8 bit UINT");
	}
	
	if (reference > UINT16_MAX) {
	    throw new IllegalArgumentException("Reference number is out of range of 16 bit UINT");
	}
	
	if (reference < UINT16_MIN) {
	    throw new IllegalArgumentException("Reference number is out of range of 16 bit UINT");
	}
	
	if (length > UINT16_MAX) {
	    throw new IllegalArgumentException("Length is out of range of 16 bit UINT");
	}
	
	if (length < (UINT16_MIN+1)) {
	    throw new IllegalArgumentException("Length is out of range of 16 bit UINT or is zero");
	}
	
	if (transID > UINT16_MAX) {
	    throw new IllegalArgumentException("Transaction ID is out of range of 16 bit UINT");
	}
	
	if (transID < (UINT16_MIN)) {
	    throw new IllegalArgumentException("Transaction ID is out of range of 16 bit UINT or is zero");
	}
	
	// Now that we know that the values are in range we must
	// craft the query, which will hvae the following format
	// byte 0 = unit identifier
	// byte 1 = function code
	// byte 2 = high byte of reference number
	// byte 3 = low byte of reference number
	// byte 4 = high byte of word count
	// byte 5 = low byte of word count	
	request.buff[0] = (byte) ((unitID >> 0) & 0xFF);
	request.buff[1] = READ_MULTIPLE_REGISTERS;
	request.buff[2] = (byte) ((reference >> 8) & 0xFF);
	request.buff[3] = (byte) ((reference >> 0) & 0xFF);
	request.buff[4] = (byte) ((length >> 8) & 0xFF);
	request.buff[5] = (byte) ((length >> 0) & 0xFF);
	
	// Set the snegth of the request to 6
	request.length = 6;

	// Se the transaction ID
	request.transID = transID;
	
	// We must now send the request
	if (!sendFrame(request)) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: sendFrame failed!");
	    }			
	    return false;
	}
	
	// First we must get the request....
	if (!receiveFrame(response)) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: receiveFrame failed!");
	    }			
	    return false;
	}
	
	// We must now start to parse the response
	
	// The response must be at least 3 bytes long 
	// (in the case of an exception, so make sure it is)
	if (response.length < 3) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Invalid response length");
	    }			
	    return false;
	}
	
	// Check that the request actually
	// has the correct transaction ID
	if (response.transID != transID) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Incorrect response transaction ID");
	    }			
	    return false;
	}
	
	// Check that the request actually
	// has the correct unit identifier
	if (response.buff[0] != ((byte)((unitID >> 0) & 0xFF))) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Incorrect unit ID");
	    }			
	    return false;
	}
	
	// For an exception
	if (response.buff[1] == (READ_MULTIPLE_REGISTERS & EXCEPTION_MODIFIER)) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Modbus Exception");
	    }			
	    return false;
	}
	
	// Check that the request actually
	// has the correct function code
	if (response.buff[1] != READ_MULTIPLE_REGISTERS) {
	    if (debug >= 3) {
		System.out.println("ModbusMaster: Incorrect return function code");
	    }			
	    return false;
	}
	
	// Now check the expected length, based on the
	// number of registers we requested and the
	// byte count to follow value
	if (response.length != (3+response.buff[2])) {
	    if (debug >= 3) {
		System.out.println("ModbusMaster: Invalid length - bytes to follow");
	    }			
	    return false;
	}
	if (response.length != (3+2*length)) {
	    if (debug >= 3) {
		System.out.println("ModbusMaster: Invalid length - requested registers");
	    }			
	    return false;
	}

	// At this stage we now know that we havea  valid response and all
	// we are required to do is parse the register values.
	for (int i=0; i<length; i++) {
	    results[i] = ((response.buff[3+2*i] << 8) + (response.buff[4+2*i] << 0)) & 0xFFFF;
	} 

	// Assuming this worked and did not thow an 
	// array index out o fbounds exception then we
	// will return true.
	return true;
    }
   
 
    /**
     * Function to read input registers from the slave device.
     * The function is passed a reference to an int array, the
     * reference of the first register and the number of registers
     * to read.  A boolean is returned indicating if the function 
     * returned sucessfully.  The the int array is too small then
     * an array index out of bounds exception will be thrown.
     * 
     * @author Kelvin Proctor
     *
     * @param reference The refernece number of the first
     *                 register to read.
     * @param length The number of register to be read.
     * @param results Refernce to the int array to which 
     *                results should be written.
     */
    public boolean readInputRegisters(int reference, 
				      int length,
				      int[] results) 
	throws IllegalArgumentException {
	return readInputRegisters(0,reference,length,0,results);
    }


    /**
     * Function to read input registers from the slave device.
     * The function is passed a reference to an int array, the
     * reference of the first register and the number of registers
     * to read.  A boolean is returned indicating if the function 
     * returned sucessfully.  The the int array is too small then
     * an array index out of bounds exception will be thrown.
     * 
     * @author Kelvin Proctor
     *
     * @param unitID The unit ID of the device to talk
     *               to (set to zero is unknown).
     * @param reference The refernece number of the first
     *                 register to read.
     * @param length The number of register to be read.
     * @param transID The transaction ID to use for this 
     *                transaction.
     * @param results Refernce to the int array to which 
     *                results should be written.
     */
    public boolean readInputRegisters(int unitID,
				      int reference, 
				      int length,
				      int transID,
				      int[] results) 
	throws IllegalArgumentException {
	
	if (debug >= 2) {
	    System.out.println("ModbusMaster: Preparing READ_INPUT_REGISTERS command");
	}

	// The first thing we want to do is perform bounds
	// checking on the input variables.
	if (unitID > UINT8_MAX) {
	    throw new IllegalArgumentException("Unit ID is out of range of 8 bit UINT");
	}
	
	if (unitID < UINT8_MIN) {
	    throw new IllegalArgumentException("Unit ID is out of range of 8 bit UINT");
	}
	
	if (reference > UINT16_MAX) {
	    throw new IllegalArgumentException("Reference number is out of range of 16 bit UINT");
	}
	
	if (reference < UINT16_MIN) {
	    throw new IllegalArgumentException("Reference number is out of range of 16 bit UINT");
	}
	
	if (length > UINT16_MAX) {
	    throw new IllegalArgumentException("Length is out of range of 16 bit UINT");
	}
	
	if (length < (UINT16_MIN+1)) {
	    throw new IllegalArgumentException("Length is out of range of 16 bit UINT or is zero");
	}
	
	if (transID > UINT16_MAX) {
	    throw new IllegalArgumentException("Transaction ID is out of range of 16 bit UINT");
	}
	
	if (transID < (UINT16_MIN)) {
	    throw new IllegalArgumentException("Transaction ID is out of range of 16 bit UINT or is zero");
	}
	
	// Now that we know that the values are in range we must
	// craft the query, which will hvae the following format
	// byte 0 = unit identifier
	// byte 1 = function code
	// byte 2 = high byte of reference number
	// byte 3 = low byte of reference number
	// byte 4 = high byte of word count
	// byte 5 = low byte of word count	
	request.buff[0] = (byte) ((unitID >> 0) & 0xFF);
	request.buff[1] = READ_INPUT_REGISTERS;
	request.buff[2] = (byte) ((reference >> 8) & 0xFF);
	request.buff[3] = (byte) ((reference >> 0) & 0xFF);
	request.buff[4] = (byte) ((length >> 8) & 0xFF);
	request.buff[5] = (byte) ((length >> 0) & 0xFF);
	
	// Set the snegth of the request to 6
	request.length = 6;

	// Se the transaction ID
	request.transID = transID;
	
	// We must now send the request
	if (!sendFrame(request)) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: sendFrame failed!");
	    }			
	    return false;
	}
	
	// First we must get the request....
	if (!receiveFrame(response)) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: receiveFrame failed!");
	    }			
	    return false;
	}
	
	// We must now start to parse the response
	
	// The response must be at least 3 bytes long 
	// (in the case of an exception, so make sure it is)
	if (response.length < 3) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Invalid response length");
	    }			
	    return false;
	}
	
	// Check that the request actually
	// has the correct transaction ID
	if (response.transID != transID) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Incorrect response transaction ID");
	    }			
	    return false;
	}
	
	// Check that the request actually
	// has the correct unit identifier
	if (response.buff[0] != ((byte)((unitID >> 0) & 0xFF))) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Incorrect unit ID");
	    }			
	    return false;
	}
	
	// For an exception
	if (response.buff[1] == (READ_INPUT_REGISTERS & EXCEPTION_MODIFIER)) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Modbus Exception");
	    }			
	    return false;
	}
	
	// Check that the request actually
	// has the correct function code
	if (response.buff[1] != READ_INPUT_REGISTERS) {
	    if (debug >= 3) {
		System.out.println("ModbusMaster: Incorrect return function code");
	    }			
	    return false;
	}
	
	// Now check the expected length, based on the
	// number of registers we requested and the
	// byte count to follow value
	if (response.length != (3+response.buff[2])) {
	    if (debug >= 3) {
		System.out.println("ModbusMaster: Invalid length - bytes to follow");
	    }			
	    return false;
	}
	if (response.length != (3+2*length)) {
	    if (debug >= 3) {
		System.out.println("ModbusMaster: Invalid length - requested registers");
	    }			
	    return false;
	}

	// At this stage we now know that we havea  valid response and all
	// we are required to do is parse the register values.
	for (int i=0; i<length; i++) {
	    results[i] = ((response.buff[3+2*i] << 8) + (response.buff[4+2*i] << 0)) & 0xFFFF;
	} 

	// Assuming this worked and did not thow an 
	// array index out o fbounds exception then we
	// will return true.
	return true;
    }    


    /**
     * Function to write multiple registers from the slave device.
     * The function is passed a reference to an int array, the
     * reference of the first register and the number of registers
     * to read.  A boolean is returned indicating if the function 
     * returned sucessfully.  The the int array is too small then
     * an array index out of bounds exception will be thrown.
     * 
     * @author Kelvin Proctor
     *
     * @param reference The refernece number of the first
     *                 register to read.
     * @param length The number of register to be read.
     * @param valuess Refernce to the int array to which 
     *                results should be written.
     */
    public boolean writeMultipleRegisters(int reference, 
				      int length,
				      int[] values) 
	throws IllegalArgumentException {
	return writeMultipleRegisters(0,reference,length,0,values);
    }


    /**
     * Function to write multiple registers to the slave device.
     * The function is passed a reference to an int array, the
     * reference of the first register and the number of registers
     * to write.  A boolean is returned indicating if the function 
     * returned sucessfully.  The the int array is too small then
     * an array index out of bounds exception will be thrown.
     * 
     * @author Kelvin Proctor
     *
     * @param unitID The unit ID of the device to talk
     *               to (set to zero is unknown).
     * @param reference The refernece number of the first
     *                 register to read.
     * @param length The number of register to be read.
     * @param transID The transaction ID to use for this 
     *                transaction.
     * @param values Refernce to the int array to which 
     *                results should be written.
     */
    public boolean writeMultipleRegisters(int unitID,
					  int reference, 
					  int length,
					  int transID,
					  int[] values) 
	throws IllegalArgumentException {
	
	if (debug >= 2) {
	    System.out.println("ModbusMaster: Preparing WRITE_MULTIPLE_REGISTERS command");
	}

	// The first thing we want to do is perform bounds
	// checking on the input variables.
	if (unitID > UINT8_MAX) {
	    throw new IllegalArgumentException("Unit ID is out of range of 8 bit UINT");
	}
	
	if (unitID < UINT8_MIN) {
	    throw new IllegalArgumentException("Unit ID is out of range of 8 bit UINT");
	}
	
	if (reference > UINT16_MAX) {
	    throw new IllegalArgumentException("Reference number is out of range of 16 bit UINT");
	}
	
	if (reference < UINT16_MIN) {
	    throw new IllegalArgumentException("Reference number is out of range of 16 bit UINT");
	}
	
	if (length > UINT16_MAX) {
	    throw new IllegalArgumentException("Length is out of range of 16 bit UINT");
	}
	
	if (length < (UINT16_MIN+1)) {
	    throw new IllegalArgumentException("Length is out of range of 16 bit UINT or is zero");
	}
	
	if (transID > UINT16_MAX) {
	    throw new IllegalArgumentException("Transaction ID is out of range of 16 bit UINT");
	}
	
	if (transID < (UINT16_MIN)) {
	    throw new IllegalArgumentException("Transaction ID is out of range of 16 bit UINT or is zero");
	}
	
	// Now that we know that the values are in range we must
	// craft the query, which will have the following format
	// byte 0 = unit identifier
	// byte 1 = function code
	// byte 2 = high byte of reference number
	// byte 3 = low byte of reference number
	// byte 4 = high byte of word count
	// byte 5 = low byte of word count	
	// byte 6 = bytes to follow
	// byte 7+2n = high byte of word n
	// byte 8+2n = low byte of word n
	request.buff[0] = (byte) ((unitID >> 0) & 0xFF);
	request.buff[1] = WRITE_MULTIPLE_REGISTERS;
	request.buff[2] = (byte) ((reference >> 8) & 0xFF);
	request.buff[3] = (byte) ((reference >> 0) & 0xFF);
	request.buff[4] = (byte) ((length >> 8) & 0xFF);
	request.buff[5] = (byte) ((length >> 0) & 0xFF);
	request.buff[6] = (byte) (((2*length) >> 0) & 0xFF);

	// Now read and set the values
	for (int i=0; i<length; i++) {
	    request.buff[7+(2*i)] = (byte) ((values[i] >> 8) & 0xFF);
	    request.buff[8+(2*i)] = (byte) ((values[i] >> 0) & 0xFF);
	} 
	
	// Set the length of the request to 6
	request.length = 7 + 2*length;

	// Se the transaction ID
	request.transID = transID;
	
	// We must now send the request
	if (!sendFrame(request)) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: sendFrame failed!");
	    }			
	    return false;
	}
	
	// First we must get the request....
	if (!receiveFrame(response)) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: receiveFrame failed!");
	    }			
	    return false;
	}
	
	// We must now start to parse the response
	
	// The response must be at least 3 bytes long 
	// (in the case of an exception, so make sure it is)
	if (response.length < 3) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Invalid response length");
	    }			
	    return false;
	}
	
	// Check that the request actually
	// has the correct transaction ID
	if (response.transID != transID) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Incorrect response transaction ID");
	    }			
	    return false;
	}
	
	// Check that the request actually
	// has the correct unit identifier
	if (response.buff[0] != ((byte)((unitID >> 0) & 0xFF))) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Incorrect unit ID");
	    }			
	    return false;
	}
	
	// For an exception
	if (response.buff[1] == (WRITE_MULTIPLE_REGISTERS & EXCEPTION_MODIFIER)) {
	    if (debug >= 2) {
		System.out.println("ModbusMaster: Modbus Exception");
	    }			
	    return false;
	}
	
	// Check that the request actually
	// has the correct function code
	if (response.buff[1] != WRITE_MULTIPLE_REGISTERS) {
	    if (debug >= 3) {
		System.out.println("ModbusMaster: Incorrect return function code");
	    }			
	    return false;
	}
	
	// We expect the response to be
	// byte 0 = unit ID
	// byte 1 = function
	// byte 2 = high byte of reference
	// byte 3 = low byte of reference
	// byte 4 = high byte of word count
	// byte 5 = low byte of word count
	//
	// Now check the expected length, which we
	// expect to be 6
	if (response.length != 6) {
	    if (debug >= 3) {
		System.out.println("ModbusMaster: Invalid length - requested registers");
	    }			
	    return false;
	}

	// Check that the reference number
	if (reference != ((response.buff[2] << 8) + (response.buff[3] << 0) & 0xFFFF)) {
	    if (debug >= 3) {
		System.out.println("ModbusMaster: Incorrect return reference number");
	    }			
	    return false;
	}

	// Check that the word count
	if (length != ((response.buff[4] << 8) + (response.buff[5] << 0) & 0xFFFF )) {
	    if (debug >= 3) {
		System.out.println("ModbusMaster: Incorrect return word count");
	    }			
	    return false;
	}

	// Assuming this worked and did not thow an 
	// array index out of  bounds exception then we
	// will return true.
	return true;
    }    


}































