/*
 * RandomWorker.java
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

import java.util.*;
import net.sourceforge.jmodbus.*;

/** 
 * This class will randomly change the value fo the first 100
 * registers of the ModbusRegisterBank that is is passed a reference 
 * to.  It will to this every 5 seconds
 */
public class RandomWorker implements Runnable {

    // Reference to the ModbusRegisters to which sample data is to be written
    private ModbusRegisterBank registers = null;

    // Random number generator
    private Random rnd = new Random();
    
    /** 
     * Constructor
     */
    public RandomWorker(ModbusRegisterBank registers)
    {
	// Set the registers
	this.registers = registers;

	// Print Message if in debug mode
	if (Modbus.debug > 1) {
	    System.out.println("RandomWorker: Construction Complete");
	}
    }
    
    /** 
     * Run the Random thread
     */
    public void run()
    {
	// if the calling thread has not set the modbus registers we should die
	if (registers == null) {
	    return;  
	}
	
	int i=0;	

	while (true) {
            
            for (i=0; i<100; i++) {
                registers.setRegister(i, rnd.nextInt(65535));
            }

	    try {
                Thread.sleep(300000);
            }
            catch (Exception e) {
                // do nothing.....
            }
        }

    }
}	
    
