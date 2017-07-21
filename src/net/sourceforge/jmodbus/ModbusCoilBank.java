/*
 * ModbusCoilBank.java
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
 * Class to represent the coils (1 bit data items) in a Modbus device.  
 * The class uses boolean varibales internally to represent coils
 * as the minimum block allocation of memory for a JVM is 4 bytes 
 * so type does not matter.  Thus booleans were chosed to make
 * logic comparisons easy.
 *
 * @author Kelvin Proctor
 *
 */
public class ModbusCoilBank {
    
    /**
     * The size (number of) coils in this bank of coils.
     */	
    private int coils_size;		
    
    /**
     * The boolean array that represents this bank of coils.
     */
    private boolean[] coils;
    
    /**
     * Class constructor that, given the size of the coil bank
     * required, will allocate suficient memory for the coil
     * bank and initilize all the values to zero
     * 
     * @author Kelvin Proctor
     * 
     * @param coils_size The number of coils to be created in
     *                   this coil bank.
     */	
    public ModbusCoilBank(int coils_size) {
	
	if (coils_size < 1) {
	    this.coils_size = 1;
	}
	else if (coils_size > Modbus.ADDRESS_MAX) {
	    this.coils_size = Modbus.ADDRESS_MAX;
	}
	else {
	    this.coils_size = coils_size;
	}
	
	coils = new boolean[this.coils_size];
	
	for (int i = 0; i < this.coils_size; i++) {
	    coils[i] = false;
	}
    }
    
    /**
     * Get the size of this bank of coils.
     * 
     * @author Kelvin Proctor
     * 
     * @return The size of the bank of coils.
     */
    public int getNumberCoils() {
	return coils_size;
    }
    
    /**
     * Gets the value of a particular coil.
     * <P>
     * <B>NOTE:</B> Attempting to get a coil outside the range
     * of this coil bank will result in a 
     * ArrayIndexOutOfBoundsException being thrown
     * 
     * @author Kelvin Proctor
     * 
     * @param index The address of the desired coil (note that
     *              this is a zero based index)
     * @return The value of the requested coil.
     */
    public boolean getCoil(int index) {
	return coils[index];
    }
    
    /**
     * Sets the value of a particular coil.
     * <P>
     * <B>NOTE:</B> Attempting to set a coil outside the range
     * of this coil bank will result in a 
     * ArrayIndexOutOfBoundsException being thrown
     * 
     * @author Kelvin Proctor
     * 
     * @param index The address of the desired coil (note that
     *              this is a zero based index)
     * @param value The value of the coil to be set.
     */
    public void setCoil(int index, boolean value) {
	coils[index] = value;
    }
    
    /**
     * Gets the value of a particular block of coils.
     * <P>
     * <B>NOTE:</B> Attempting to get a coil outside the range
     * of this coil bank will result in a 
     * ArrayIndexOutOfBoundsException being thrown
     * 
     * @author Kelvin Proctor
     * 
     * @param index The address of the desired starting coil (note 
     *              that this is a zero based index)
     * @param length The number of coils to be retrieved
     * @return Array containing the values of the requested coils.
     */
    public boolean[] getCoils(int index, int length) {
	boolean[] coil_set = new boolean[length];
	for (int i=0; i<length; i++) {
	    coil_set[i] = coils[i+index];
	}
	return coil_set;
    }
    
    /**
     * Sets the value of a particular block of coils.
     * <P>
     * <B>NOTE:</B> Attempting to set a coil outside the range
     * of this coil bank will result in a 
     * ArrayIndexOutOfBoundsException being thrown
     * 
     * @author Kelvin Proctor
     * 
     * @param index The address of the desired starting coil (note 
     *              that this is a zero based index)
     * @param coil_set Array of booleans containing the values 
     *                 of the coils to be set.
     */
    public void setCoils(int index, boolean[] coil_set) {
	for (int i=0; i<coil_set.length; i++) {
	    coils[index+i] = coil_set[i];
	}
    }
}

     
     
     
