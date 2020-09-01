package com.pprl.util;


import java.util.Scanner;
import java.util.logging.Logger;

import com.pprl.main.Broker;
import com.pprl.main.Node;
import com.pprl.messageformats.EventFactory;




public class BrokerInputThread implements Runnable{
	
	private Scanner sc;
	boolean endmenu = false;
	int select = 0;
	private EventFactory ev;
	private Node callingNode;
	private static Logger logger;
	Scanner userInput;
	
	public void exitBrokerMenu()
	{
		endmenu = true;
	}
	
	public BrokerInputThread(Node callingNode,Scanner scan) {
		ev = EventFactory.getInstance();
		sc = scan;
		
		this.callingNode = callingNode;
	}
	
	@Override
	public void run() {
		Broker.displayBrokerMenu();
		// TODO Auto-generated method stub
		while(!endmenu) {
			//System.out.println("endmenu");
			int cmd = sc.nextInt();
			//System.out.println(cmd);
			if(String.valueOf(cmd).equals("1")) {
				((Broker) callingNode).showActivePublishers();
			}
			else if(String.valueOf(cmd).equals("2")) {
				((Broker) callingNode).sendSetupInitiateRequest();
								
			}
			
			else if(String.valueOf(cmd).equals("3")) {
				System.out.println("Enter number of rows to be fetched from each Publisher");
				int rows = sc.nextInt();
				((Broker) callingNode).sendDataFetchRequest(rows,cmd);
								
			}
			else if(String.valueOf(cmd).equals("4")) {
				((Broker) callingNode).sendDataFetchRequest(4,cmd);				
			}
			else if(String.valueOf(cmd).equals("5")) {
				((Broker) callingNode).getBrokerLookupTable();
			}
			
			else if(String.valueOf(cmd).equals("6")) {
				System.out.println("Enter the ID to search");
				String searchID = sc.next();
				((Broker) callingNode).sendSearchRequest(searchID,cmd);
			}
			
			else {
				//System.out.println("ELSE");
				System.out.println("INVALID CHOICE ! TRY AGAIN ");
			}
		}
		logger.info("CONSOLE READER EXITED");
		
	}
	
	
	
}
