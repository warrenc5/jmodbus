/*
 * TemperatureWorker.java
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

import java.io.*;
import java.util.*;
import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;
import net.sourceforge.jmodbus.*;

/** 
 * This class runs network of DS18S20 and DS1920 1-wire sensors
 * of family code 0x10 as a thread and writes the data into a 
 * ModbusRegisterBank.
 */
public class TemperatureWorker implements Runnable {

    // Tini External 1-wire adapter used for 1-wire comms 	
    private TINIExternalAdapter adapter;

    // 1-wire contained 10 (hex) to represent 
    // DS18S20 or DS1920 chips
    private OneWireContainer10 tempSensor = new OneWireContainer10();

    // Array of addresses of the devices attached to the network
    private byte[][] devices;

    // Reference to the ModbusRegisters to which sample data is to be written
    private ModbusRegisterBank registers = null;
    
    /** 
     * Constructor
     */
    public TemperatureWorker()
    {
	// Create the 1-wire adapter
	adapter = new TINIExternalAdapter();
	  // Setup the adapter to only look
	// for family codc 0x10 devices
	adapter.targetFamily(0x10);
	
	// Print Message if in debug mode
	if (Modbus.debug >= 1) {
	    System.out.println("TempWorker: Construction Complete");
	}
    }
    
    /**
     * Get the number of temperature sensors attached to
     * the 1-wire network
     */
    public int getNumberOfDevices() {
	
	Vector v = new Vector();
	byte[] address = new byte[8];
	
	try {
	    // Reset the adapter
	    adapter.reset();
	    
	    // See if we have any devices
	    if (adapter.findFirstDevice()) {

		// if we do then get the addess of the device
		adapter.getAddress(address);
		
		// and add it to the vector
		v.addElement(address);
		
		// Now loo through all the other devices doing the same
		while (adapter.findNextDevice()) {
		    byte[] temp = new byte[8];
		    adapter.getAddress(temp);
		    v.addElement(temp);
		}   
	    }
	    else {
		
		// Print Message if in debug mode
		if (Modbus.debug >= 2) {
		    System.out.println("TempWorker: getNumberofDevices - No devices found!");
		}
		
	    }
	}
	catch (OneWireIOException ex) {
	    
	    // Print Message if in debug mode
	    if (Modbus.debug >= 2) {
		System.out.println("TempWorker: getNumberOfDevices - OneWireIOException");
	    }
	    
	}
	catch (OneWireException ex) {
	    
	    // Print Message if in debug mode
	    if (Modbus.debug >= 2) {
		System.out.println("TempWorker: getNumberOfDevices - OneWireException");
	    }
	    
	}
	
	// Trim the vector to size
	v.trimToSize();
	
	// Setup the deviec byte array
	devices = new byte[v.size()][8];
	
	// Copy the device addresses into the byte array
	v.copyInto(devices);
	
	// Print Message if in debug mode
	if (Modbus.debug >= 2) {
	    System.out.println("TempWorker: found "+ devices.length +" 0x10 chips");
	}
	
	// Return the number of devices
	return devices.length;
    }
    
    
    /**
     * Set the refernece to the modbus registers for the thread
     */
    public void setModbusRegisters(ModbusRegisterBank registers) {
	this.registers = registers;
    }
    
    
    /** 
     * Run the Temperature server thread
     */
    public void run()
    {
	// if the calling thread has not set the modbus registers we should die
	if (registers == null) {
	    return;  
	}
	
	// Otherwise we have the registers and we can start sampling
	byte[] state;
	int i = 0;
	int temp;
	
	while (true) {
	    try {
		for (i=0; i<devices.length; i++) 
		    {
			// Select the device
			tempSensor.setupContainer(adapter, devices[i]);
			
			// Read the state memory
			state = tempSensor.readDevice();
			
			// Get device to perform conversion
			tempSensor.doTemperatureConvert(state);
			
			// Read the memory out of the device
			state = tempSensor.readDevice();
			
			// Get the temperature from the state memory
			temp = (int) state[0];
			if (state[1] == 0x00) {
			    temp = temp | 0x0100;
			}

			// Print Message if in debug mode
			if (Modbus.debug >= 2) {
			    System.out.println("TempWorker: run - read value: " + temp);
			}
			
			// Write the value into the registers
			registers.setRegister(i, temp);
		    }
	    }
	    catch (OneWireIOException ex) {
		
		// Print Message if in debug mode
		if (Modbus.debug >= 2) {
		    System.out.println("TempWorker: run - OneWireIOException");
		}
		
	    }
	    catch (OneWireException ex) {
		
		// Print Message if in debug mode
		if (Modbus.debug >= 2) {
		    System.out.println("TempWorker: run - OneWireException");
		}
		
	    }
	}
    }
}
    
    










