package com.pprl.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.pprl.main.Broker;
import com.pprl.main.Node;
import com.pprl.main.Publisher;
import com.pprl.util.LogFactory;
import com.pprl.util.NodeGroup;



public class TCPServer implements Runnable {
	
	private ServerSocket receiverSocket;
	private NodeGroup nodeGroup;
	private Node currentNode;
	private Map<String, Socket> addrToSocketMap;
	private static Logger logger;
	private volatile boolean getOut = false;
	private boolean isRegistry = true;
	
	/* FOR PUBLISHER */
	public TCPServer(ServerSocket receiverSocket, NodeGroup nodeGroup, Node callingNode) {
		logger = LogFactory.getLogger(ServerSocket.class.getName(), "registry-server.out");
		this.receiverSocket = receiverSocket;
		this.nodeGroup = nodeGroup;
		this.currentNode = callingNode;
		addrToSocketMap = new HashMap<String, Socket>();
		isRegistry = false;
	}

	/* FOR BROKER */
	public TCPServer(ServerSocket receiverSocket, NodeGroup nodeGroup, Node callingNode, Map<String, Socket> addrToSocketMap) {
		logger = LogFactory.getLogger(ServerSocket.class.getName(), "registry-server.out");
		this.receiverSocket = receiverSocket;
		this.nodeGroup = nodeGroup;
		this.currentNode = callingNode;
		this.addrToSocketMap = addrToSocketMap;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!getOut) {
			if(isRegistry) {
				try {
					/* Accept an incoming request for connection */
					/* Each Messaging Node will have one Socket object on Client Side */
					
					Socket registerSideSocket = receiverSocket.accept();
					
					logger.info("Connection accepted from "+ registerSideSocket.getInetAddress()+":"+registerSideSocket.getPort());
					logger.info("Connection accepted at "+ registerSideSocket.getLocalAddress()+":"+registerSideSocket.getLocalPort());
					
					/* Put the socket entry in the map */
					String socketKey = getKey(registerSideSocket);
	
					/*
					 * Create a Map here keeping track of incoming socket
					 * connections
					 */
					Broker broker = (Broker) currentNode;
					broker.addToAddrToSocketMap(socketKey, registerSideSocket);
					//addEntry(socketKey, registerSideSocket);
					
					/*====================STARTING A TCP RECEIVER THREAD ON A PORT FOR EACH MESSAGING NODE=============*/
					
					Thread rcvThread = new Thread(new TCPReceiver(registerSideSocket,currentNode));
					rcvThread.start();
	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					Socket mNodeSideSocket = receiverSocket.accept();
					
					logger.info("Connection Request accepted from "+ mNodeSideSocket.getInetAddress()+":"+mNodeSideSocket.getPort());
					logger.info("Connection Request accepted at "+ mNodeSideSocket.getLocalAddress()+":"+mNodeSideSocket.getLocalPort());
					
					TCPReceiver rcvr = new TCPReceiver(mNodeSideSocket,currentNode);
					
					Publisher pub = (Publisher)currentNode;
					pub.addToReceiverThreadList(rcvr);
					Thread rcvThread = new Thread(rcvr);
					rcvThread.start();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
		
	}

	public String getKey(Socket s) {
		String keyStr = "";
		if(s.getInetAddress() == null || !s.getInetAddress().toString().contains("/")) {
			logger.severe("GOT NULL IP ADDRESS FROM SOCKET");
			return null;
		}
		String ip = s.getInetAddress().toString().split("/")[1];
		keyStr = ip+":"+s.getPort();
		
		return keyStr;
	}
	
	private synchronized void addEntry(String key, Socket sock) {
		addrToSocketMap.put(key, sock);
	}
	

	public void setGetOut() {
		this.getOut = true;
	}
	
	
	public static void main(String arg[]) {
		String str="-4";
		System.out.println(str.matches("^-?\\d+$"));
		//System.out.println(Integer.valueOf(" "));
	}
	
	
}
