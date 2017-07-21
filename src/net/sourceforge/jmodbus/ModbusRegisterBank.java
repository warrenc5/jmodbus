/*
 * ModbusRegisterBank.java
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
 * Class to represent the registers (16 bit data items) in a Modbus device.  
 * The classes uses int varibales internally to represent registers
 * as the minimum block allocation of memory for a JVM is 4 bytes 
 * so using a short etc.. does not make any difference.   *
 * @author Kelvin Proctor
 */
public class ModbusRegisterBank {
    
    /**
     * The size (number of) registers in this bank of registers.
     */    
    private int register_size;
    
    /**
     * The int array that represents this bank of registers.
     */
    private int[] registers;
    
    /**
     * Class constructor that, given the size of the register bank
     * required, will allocate suficient memory for the register
     * bank and initilize all the values to zero
     * 
     * @author Kelvin Proctor
     * 
     * @param coils_size The number of registers to be created in
     *                   this register bank.
     */    
    public ModbusRegisterBank(int register_size) {
	
	if (register_size < 1) {
	    this.register_size = 1;
	}
	else if (register_size > Modbus.ADDRESS_MAX) {
	    this.register_size = Modbus.ADDRESS_MAX;
	}
	else {
	    this.register_size = register_size;
	}
	
	registers = new int[this.register_size];
	
	for (int i = 0; i < this.register_size; i++) {
	    registers[i] = 0;
	}
    }
    
    /**
     * Get the size of this bank of registers.
     * 
     * @author Kelvin Proctor
     * 
     * @return The size of the bank of registers.
     */
    public int getNumberRegisters() {
	return register_size;
    }
    
    /**
     * Gets the value of a particular register.
     * <P>
     * <B>NOTE:</B> Attempting to get a register outside the range
     * of this register bank will result in a 
     * ArrayIndexOutOfBoundsException being thrown
     * 
     * @author Kelvin Proctor
     * 
     * @param index The address of the desired register (note that
     *              this is a zero based index)
     * @return The value of the requested register.
     */
    public int getRegister(int index) {
	return registers[index];
    }
    
    /**
     * Sets the value of a particular register.
     * <P>
     * <B>NOTE:</B> Attempting to set a register outside the range
     * of this register bank will result in a 
     * ArrayIndexOutOfBoundsException being thrown
     * 
     * @author Kelvin Proctor
     * 
     * @param index The address of the desired register (note that
     *              this is a zero based index)
     * @param value The value of the register to be set.
     */
    public void setRegister(int index, int value) {
	registers[index] = value;
    }
    
    /**
     * Gets the value of a particular block of registers.
     * <P>
     * <B>NOTE:</B> Attempting to get a register outside the range
     * of this register bank will result in a 
     * ArrayIndexOutOfBoundsException being thrown
     * 
     * @author Kelvin Proctor
     * 
     * @param index The address of the desired starting register (note 
     *              that this is a zero based index)
     * @param length The number of registers to be retrieved
     * @return Array containing the values of the requested registers.
     */	
    public int[] getRegisters(int index, int length) {
	int[] regs = new int[length];
	for (int i=0; i<length; i++) {
	    regs[i] = registers[i+index];
	}
	return regs;
    }
    
    /**
     * Sets the value of a particular block of registers.
     * <P>
     * <B>NOTE:</B> Attempting to set a register outside the range
     * of this register bank will result in a 
     * ArrayIndexOutOfBoundsException being thrown
     * 
     * @author Kelvin Proctor
     * 
     * @param index The address of the desired starting register (note 
     *              that this is a zero based index)
     * @param coil_set Array of ints containing the values 
     *                 of the registers to be set.
     */
    public void setRegister(int index, int[] regs) {
	for (int i=0; i<regs.length; i++) {
	    registers[index+i] = regs[i];
	}
    }
    
}


