/*
 * ModbusMessage.java
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
 * Class to represent a Modbus message.  This will encapsulate all the
 * elements of a message that should be considered common to all implementations
 * of Modbus, even if some elements are not used.
 * <P>
 * <B>WARNING:</B> This class is currently <B>very</B> weak and does 
 * not offer protection that incorrect message lengths etc... will 
 * not be set.  It shoudl also use get/set methods eventually but we first
 * need to examine how much overhead that will create for the system.
 *
 * @author Kelvin Proctor
 */
public class ModbusMessage {
    
    /**
     * The byte array containing the message.  This is fixed in length to
     * the maximum length, use the length field to check it's length.
     */
    public byte[] buff;

    /**
     * Length field to indicate how much of the byte buffer contains
     * valid data.
     */
    public int length;

    /**
     * The transaction ID of this message, this is only really used by
     * the TCP implementations.
     */
    public int transID;
    
    /**
     * Class constructor that will allocate memory for the byte buffer
     * and initilize the message object.
     */
    public ModbusMessage() {
	buff = new byte[Modbus.MAX_MESSAGE_LENGTH];
	length = 0;
	transID = 0;
    }
}

