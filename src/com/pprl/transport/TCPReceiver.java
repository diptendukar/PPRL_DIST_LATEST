package com.pprl.transport;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pprl.main.Broker;
import com.pprl.main.Node;
import com.pprl.messageformats.EventFactory;
import com.pprl.util.LogFactory;


public class TCPReceiver implements Runnable{
	
	/* receiverSocket: The socket on which the TCPReceiver got the message*/
	private Socket receiverSocket;
	private DataInputStream din;
	private EventFactory ev;
	private Node node;
	private static Logger logger;
	private volatile boolean getOut = false;
	
	
	public TCPReceiver(Socket receiverSocket, Node node) {
		String name = "";
		
		if(node.getClass().toString().contentEquals(Broker.class.toString())) {
			name="broker-receiver.out";
		} else {
			name="publisher-receiver.out";
		}
		logger = LogFactory.getLogger(TCPReceiver.class.getName(), name);
		this.receiverSocket = receiverSocket;
		try {
			din = new DataInputStream(receiverSocket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE, "Failure to create DataInputStream: "+e.getMessage());
			e.printStackTrace();
		}
		ev = EventFactory.getInstance();
		this.node = node;
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(receiverSocket != null && !getOut) {
			try {
				
				/*=========================RECEIVING EACH MESSAGE THAT CAME=======================================*/
				
				int msgSize = din.readInt();
				if(msgSize == -1) {
					break;
				}
				
				byte[] msg = new byte[msgSize];
				
				din.readFully(msg,0,msgSize);
				//logger.info("INCOMING MESSAGE RECEIVED.............");
				//din.close();
				//logger.info("CALLING EVENTFACTORY.............");
				ev.react(msg, node, receiverSocket);
				
			} catch (EOFException e) {
				setGetOut();
				logger.log(Level.SEVERE, "Failure to read incoming message: "+e.getMessage());
				e.printStackTrace();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				setGetOut();
				logger.log(Level.SEVERE, "Failure to read incoming message: "+e.getMessage());
				e.printStackTrace();
			}
			
			
			
		}
		logger.info("RECEIVER THREAD SHUT DOWN.............");
		
	}
	
	public void setGetOut() {
		this.getOut = true;
	}
	
	public static void main(String arg[]) {
		String s = "Hello world\nHow r u\nGood Bye\n";
		for(String k: s.split("\n")){
			System.out.println(k);
		}
		System.out.println(s.split("\n").length);
	}

}
