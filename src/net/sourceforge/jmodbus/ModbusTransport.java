/*
 * ModbusTransport.java
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
 * Interface for Modbus transport mechanisims.  This is the interface
 * that will be implemented by classes representing the transports for the
 * different flavours of Modbus.  Examples are ModbusRTU, ModbusASCII
 * and ModbusTCP.
 *
 * Classes implementing this interface are reponsible for the framing 
 * and deframing of Modbus messages from ther transport media and the 
 * actual sending and recieving of data.
 *
 * @author Kelvin Proctor
 */
public interface ModbusTransport {
    
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
    public boolean sendFrame(ModbusMessage msg);
    
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
    public boolean receiveFrame(ModbusMessage msg);
}

