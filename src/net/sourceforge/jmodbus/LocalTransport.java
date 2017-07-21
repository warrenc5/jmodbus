package net.sourceforge.jmodbus;

import java.io.IOException;

public class LocalTransport implements ModbusTransport {

    java.util.concurrent.BlockingQueue<ModbusMessage> q = new java.util.concurrent.ArrayBlockingQueue(1000);
	@Override
	public boolean sendFrame(ModbusMessage msg) throws IOException {
        try {
			q.put(msg);
            return true;
		} catch (InterruptedException e) {
    		return false;
		}
	}

	@Override
	public boolean receiveFrame(ModbusMessage msg) throws IOException {
		try {
			ModbusMessage msg2 = q.take();
            msg.buff = msg2.buff;
            msg.length = msg2.length;
            msg.transID = msg2.transID;
            return true;
		} catch (InterruptedException e) {
    		return false;
		}
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}
    
	@Override
	public String toString() {
        return "@Local";
	}
    
}
