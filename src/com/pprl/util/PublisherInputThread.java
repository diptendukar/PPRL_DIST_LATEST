package com.pprl.util;


import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Logger;

import com.pprl.main.Node;
import com.pprl.main.Publisher;
import com.pprl.messageformats.DeregisterRequest;
import com.pprl.messageformats.EventFactory;






public class PublisherInputThread implements Runnable{
	
	private Scanner sc;
	private EventFactory ev;
	private Node callingNode;
	private static Logger logger;
	boolean endmenu = false;
	//boolean isRegistered = false;
	int choice = 0;
	String brokerIP,brokerPort;
	//private int brokerPort = -1;
	//private InetAddress brokerInetAddress = null;
	//private static String publishersOwnIP = "";

	
	public void exitPublisherMenu()
	{
		endmenu = true;
	}
	
	public PublisherInputThread(Node callingNode,Scanner scanner) {
		ev = EventFactory.getInstance();
		sc = scanner;
		
		this.callingNode = callingNode;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//System.out.println("RUN");
		//Scanner userInput = new Scanner(System.in);
		while(!endmenu) {
			//System.out.println("endmenu");
			int cmd = sc.nextInt();
			System.out.println(cmd);
			if(String.valueOf(cmd).equals("2")) {
				//System.out.println("HERE");
				ev.reactToInputs(callingNode,String.valueOf(cmd));
				sc.close();
				//exitPublisherMenu();
			} else {
				//System.out.println("ELSE");
				ev.reactToInputs(callingNode,String.valueOf(cmd));
			}
		}
		logger.info("CONSOLE READER EXITED");
			/*System.out.println("\n*** Privacy Preserving Record Linkage ***\n");
					
			System.out.println("Press 1 to Register to Broker");
			System.out.println("Press 2 to De Register (Exit)");
			
			choice = userInput.nextInt();
			
			if(choice == 1) {
				System.out.println("Enter IP address of Broker");
				brokerIP = userInput.next();
				try {
					brokerInetAddress = InetAddress.getByName(brokerIP);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				
				System.out.println("Enter port number of Broker");
				brokerPort = userInput.next();
				System.out.println("Attempting Registration to "+brokerIP+":"+brokerPort);
				Publisher pub = null;
				try {
					pub = new Publisher(brokerIP, brokerPort);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//logger.info("Messaging Node Initialized");
				pub.startup();
				//System.out.println("MY IP - "+publishersOwnIP);
				//isRegistered = true;
			}
			else if(choice == 2) {
			
				System.out.println("Attempting De Registration");
				DeregisterRequest d = new DeregisterRequest();
				d.setIp(getLocalIP());
				d.setPort(getSocketToBrokerPort());
				byte[] msg = d.getBytes();
				sendDeregisterRequest(msg, socketToBroker);
				endmenu = true;
            	userInput.close();
			}

			else {				
				System.out.println("Invalid Choice");
			}*/
			
		} 
		
	}
	
	

