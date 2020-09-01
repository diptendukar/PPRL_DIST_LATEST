package com.pprl.main;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pprl.messageformats.DataFetchCompleted;
import com.pprl.messageformats.DeregisterRequest;
import com.pprl.messageformats.DeregisterResponse;
import com.pprl.messageformats.Event;
import com.pprl.messageformats.EventFactory;
import com.pprl.messageformats.FetchDataRequest;
import com.pprl.messageformats.FetchDataResponse;
import com.pprl.messageformats.MessageTypes;
import com.pprl.messageformats.RegisterResponse;
import com.pprl.messageformats.RegistrationRequestMessage;
import com.pprl.messageformats.SearchCompleted;
import com.pprl.messageformats.SearchRequest;
import com.pprl.messageformats.SearchResponse;
import com.pprl.messageformats.SetupCompletedResponse;
import com.pprl.messageformats.SetupInitiateRequest;
import com.pprl.transport.TCPReceiver;
import com.pprl.transport.TCPSender;
import com.pprl.transport.TCPServer;
import com.pprl.util.BrokerInputThread;
import com.pprl.util.DBConnection;
import com.pprl.util.HostInfo;
import com.pprl.util.LogFactory;
import com.pprl.util.NodeGroup;













public class Broker implements Node{
	
	private static Logger logger;
	private TCPServer brokerTCPServer;
	private TCPReceiver brokerTCPReceiver;
	private TCPSender brokerTCPSender;
	private ServerSocket brokerServerSocket;
	private EventFactory eventFactory;
	
	// Listening port of the broker
	private int brokerServerPort;
	
	// map keeps track of registered publishers
	private Map<String,HostInfo> knownHosts;
	
	// map to keep track of publishers and their neighbors
	
	private Map<String,HostInfo> publishersNeighbor;
	
	// map containing registered publishers "ip:port" and socket allocated
	private Map<String, Socket> addrToSocketMap;
	
	private NodeGroup nodeGroup;
	
	private ArrayList<String> publisherList;
	private ArrayList<String> searchRequestSentList;
	private ArrayList<String> searchRequestReceivedList;
	private ArrayList<String> brokerTableMapping;
	
	private Map<String, TableDataFormat> compareList;
	private Map<String, TableDataFormat> compareSearch;
	private Map<String, String> receiveCompletedList;
	// Keep track of setup has been executed or not
	private boolean setupExecutedOnce = false;
	private String brokerPrimeNumber;
	private String brokerPublicKey;
	private String brokerGenerator;
	private String brokerSecretKey;
	
	long startTime = 0;
	long endTime = 0;
	
	public Broker(int brokerStartPort) {
		try {
			
			logger = LogFactory.getLogger(Broker.class.getName(), "brokerlog.out");
			
			logger.info("Logger Initialized in Broker.");
			
			knownHosts = new HashMap<String,HostInfo>();
			publisherList = new ArrayList<String>();
			searchRequestSentList = new ArrayList<String>();
			searchRequestReceivedList = new ArrayList<String>();
			brokerTableMapping = new ArrayList<String>();
			compareList =  new HashMap<String,TableDataFormat>();
			compareSearch =  new HashMap<String,TableDataFormat>();
			receiveCompletedList =  new HashMap<String,String>();
			publishersNeighbor = new HashMap<String,HostInfo>();
			/* Getting Singleton Instance from EventFactory */
			eventFactory = EventFactory.getInstance();
			
			setNodeGroup(new NodeGroup());
			brokerServerPort = brokerStartPort;
			
			/*======================CREATING NEW SERVER SOCKET FOR REGISTRY==========================*/
			
			setBrokerServerSocket(new ServerSocket(brokerStartPort));
			logger.info("Broker ServerSocket created at port "+ brokerServerSocket.getLocalPort());
			
			addrToSocketMap = new HashMap<String, Socket>();
			//hostsWhoHaveFinished = new ArrayList<String>();
			//hostsWhoHaveSummarised = new ArrayList<String>();
			//hostToTrafficSummaryListMap = new HashMap<String, List<TrafficSummary>>();
			
			logger.info("Broker Initialization Successful");
			//nodeToEdgeMap = new HashMap<Integer, List<Edge>>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) {
		
		Scanner userInput = new Scanner(System.in);
		System.out.println("Enter the port number to start Broker");
		
		int brokerStartPort = userInput.nextInt();
		
		
		Broker broker = new Broker(brokerStartPort);
		
		broker.startup(userInput);
		// TODO Auto-generated method stub
		
		// DISPLAY MENU AT BROKER END
		//boolean endmenu = false;
		//int select = 0;
		//EventFactory ev = EventFactory.getInstance();
		//Node callingNode = broker;
		
		/*do {
			userInput = new Scanner(System.in);
			
			displayBrokerMenu();
			
			
			select = userInput.nextInt();
			
			switch(select)
            {
            case 1:
                System.out.println("Checking Publisher Status");
                //ev.reactInternal(callingNode,String.valueOf(select));
                break;
            case 2:
                System.out.println("Starting Setup Phase");
                ev.reactInternal(callingNode,String.valueOf(select));
                break;
            case 3:
                System.out.println("Enter number of rows to be fetched");
                ev.reactInternal(callingNode,String.valueOf(select));
                break;
            case 4:
            	System.out.println("Enter number of rows to be fetched");
            	ev.reactInternal(callingNode,String.valueOf(select));
                break;
            case 5:
            	System.out.println("Checking broker mapping table");
            	//ev.reactInternal(callingNode,String.valueOf(select));
            	break;
            case 6:
            	System.out.println("Enter the ID to search");
            	//ev.reactInternal(callingNode,String.valueOf(select));
            	break;
            case 7:
            	System.out.println("Exiting program");
            	//ev.reactInternal(callingNode,String.valueOf(select));
            	endmenu = true;
            	userInput.close();
            	break;
            default:
               System.out.println("Invalid Choice");
            }
			
			
		}while(!endmenu);*/
		
		
		//System.out.println("EXITING PROGRAM !");
	}



	public static void displayBrokerMenu() {
		System.out.println("\n*** Privacy Preserving Record Linkage ***\n");
		System.out.println("1. Check Publisher Status");
		System.out.println("2. Execute Setup Phase");
		System.out.println("3. Fetch Records - Normal");
		System.out.println("4. Fetch Records - Encrypted");
		System.out.println("5. Lookup Broker Mapping");
		System.out.println("6. Search (Retrospective Query)");
		System.out.println("7. Exit");
		System.out.println("**********************");
		System.out.println("Please enter your choice:");
	}
	
	public void startup(Scanner userInput) {
		/* This will be the main registry server with
		 * ServerSocket object that waits listening for incoming connections */
		
		/*=======================STARTING UP REGISTRY SERVER THREAD=================================*/
		brokerTCPServer = new TCPServer(getBrokerServerSocket(), getNodeGroup(), this, addrToSocketMap);
		
		/* This is the Registry Server Thread that listens to incoming connections */
		Thread brokerServerThread = new Thread(brokerTCPServer,"BrokerServerThread");
		
		brokerServerThread.start();
		
		/*=======================STARTING UP CONSOLE READER THREAD===================================*/
		BrokerInputThread brokerinput = new BrokerInputThread(this,userInput);
		Thread userinputThread = new Thread(brokerinput);
		userinputThread.start();
		
		try {
			
			brokerServerThread.join();
			userinputThread.join();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	


	@Override
	public void onEvent(Event e, Socket s) {
		// TODO Auto-generated method stub
		logger.info("Registry Node received SOMETHING");
		if(RegistrationRequestMessage.class.toString().equals(e.getClass().toString())) {
			RegistrationRequestMessage regReq = (RegistrationRequestMessage) e;
			logger.info("Registry Node received a RegisterRequest from "+regReq.getIpAddress()+":"+regReq.getSocketPortNum());
			handleRegisterRequest(regReq,s);
			
		}
		else if(DeregisterRequest.class.toString().equals(e.getClass().toString())) {
			
			DeregisterRequest req = (DeregisterRequest)e;
			logger.info("Registry Node received a DeregisterRequest from "+req.getIp()+":"+req.getPort());
			handleDeregisterRequest(req, s);
			
		}
		else if(SetupCompletedResponse.class.toString().equals(e.getClass().toString())) {
			
			SetupCompletedResponse regRsp = (SetupCompletedResponse) e;
			logger.info("SETUP COMPLETE Message Received from Publisher. Status: "+regRsp.getStatus()+", INFO: "+regRsp.getInfoMsg());
		}
		
		else if(FetchDataResponse.class.toString().equals(e.getClass().toString())) {
			
			FetchDataResponse fdr = (FetchDataResponse) e;
			logger.info("RECEIVED DATA FETCH RESPONSE FROM - "+fdr.getIpAddress());
			if(fdr.getIsEncrypted() == 0) {
				// NORMAL FETCH
				logger.info("RECEIVED NORMAL DATA FETCH RESPONSE FROM - "+fdr.getIpAddress());
				logger.info(fdr.getEncryptedData());
				String[] allData = fdr.getEncryptedData().split(",");
				for(int i=0;i<allData.length;i=i+6) {
					String key = allData[i]+"_"+allData[i+1]+"_"+allData[i+2];
					if(compareList.get(key) == null) {
						TableDataFormat tdf =  new TableDataFormat();
						tdf.setId(allData[i]);
						tdf.setName(allData[i+1]);
						tdf.setAddress(allData[i+2]);
						tdf.setDiagnosis(allData[i+3]);
						tdf.setTreatment(allData[i+4]+"-"+fdr.getHostName());
						if(allData[i+5] == null || allData[i+5].trim() == "") {
						tdf.setComments("NONE");
						}
						else {
							tdf.setComments(allData[i+5]);
						}
						tdf.setSource(fdr.getIpAddress()+" = "+fdr.getHostName());
						compareList.put(key, tdf);
					}
					else if(compareList.get(key) != null){
						TableDataFormat tdf =  compareList.get(key);
						if(!tdf.getSource().contains(fdr.getIpAddress())) {
							tdf.setSource(tdf.getSource()+","+fdr.getIpAddress()+" = "+fdr.getHostName());
							compareList.put(key, tdf);
						}
						if(!tdf.getTreatment().contains(allData[i+4]+"-"+fdr.getHostName())) {
							tdf.setTreatment(tdf.getTreatment()+","+allData[i+4]+"-"+fdr.getHostName());
						}
					}
				}
				
				
			}
			else if (fdr.getIsEncrypted() == 1) {
				logger.info("RECEIVED ENCRYPTED DATA FETCH RESPONSE FROM - " + fdr.getIpAddress());
				String receivedData = fdr.getEncryptedData();
				String encKeyConverter = fdr.getKeyConverter();
				
				BigInteger[] incomingToken = new BigInteger[2];
				incomingToken[0] = new BigInteger(encKeyConverter.split("_")[0]);
				incomingToken[1] = new BigInteger(encKeyConverter.split("_")[1]);

				BigInteger decryptedKeyConverter = decrypt(incomingToken, new BigInteger(brokerPrimeNumber),
						new BigInteger(brokerSecretKey));
				
				String[] allData = receivedData.split(",");
				
				for(int i=0;i<allData.length;i=i+6) {
					BigInteger[] reEncID = encrypt(new BigInteger(allData[i]), new BigInteger(brokerPrimeNumber),
							new BigInteger(brokerGenerator), decryptedKeyConverter, new BigInteger(brokerPublicKey));
					BigInteger[] reEncName = encrypt(new BigInteger(allData[i+1]), new BigInteger(brokerPrimeNumber),
							new BigInteger(brokerGenerator), decryptedKeyConverter, new BigInteger(brokerPublicKey));
					BigInteger[] reEncAddr = encrypt(new BigInteger(allData[i+2]), new BigInteger(brokerPrimeNumber),
							new BigInteger(brokerGenerator), decryptedKeyConverter, new BigInteger(brokerPublicKey));
					String key = reEncID[1].toString()+"_"+reEncName[1].toString()+"_"+reEncAddr[1].toString();
					
					brokerTableMapping.add(allData[i]);
					brokerTableMapping.add(reEncID[1].toString());
					brokerTableMapping.add(fdr.getIpAddress()+"="+fdr.getHostName());
					
					if(compareList.get(key) == null) {
						TableDataFormat tdf =  new TableDataFormat();
						tdf.setId(reEncID[1].toString());
						tdf.setName(reEncName[1].toString());
						tdf.setAddress(reEncAddr[1].toString());
						tdf.setDiagnosis(allData[i+3]);
						tdf.setTreatment(allData[i+4]+"-"+fdr.getHostName());
						if(allData[i+5] == null || allData[i+5].trim() == "") {
							tdf.setComments("NONE");
							}
							else {
								tdf.setComments(allData[i+5]);
							}
						tdf.setSource(fdr.getIpAddress()+" = "+fdr.getHostName());
						compareList.put(key, tdf);
					}
					else if(compareList.get(key) != null){
						TableDataFormat tdf =  compareList.get(key);
						if(!tdf.getSource().contains(fdr.getIpAddress())) {
							tdf.setSource(tdf.getSource()+","+fdr.getIpAddress()+" = "+fdr.getHostName());
							compareList.put(key, tdf);
						}
						if(!tdf.getTreatment().contains(allData[i+4]+"-"+fdr.getHostName())) {
							tdf.setTreatment(tdf.getTreatment()+","+allData[i+4]+"-"+fdr.getHostName());
						}
					}
					
				}

			}
		}
		else if(DataFetchCompleted.class.toString().equals(e.getClass().toString())) {
			DataFetchCompleted dfc = (DataFetchCompleted) e;
			String incomingIP = dfc.getIpAddress();
			String incomingHostName = dfc.getHostName();
			logger.info("RECEIVED DATA FETCH COMPLETED FROM - "+incomingIP+" HOSTNAME = "+incomingHostName);
			if(publishersNeighbor.keySet().toString().contains(dfc.getIpAddress())) {
				if(receiveCompletedList.keySet().toString().contains(incomingIP)) {
					logger.severe("DUPLICATE COMPLETED MESSAGE FROM PUBLISHER "+incomingIP);
				}
				else {
					receiveCompletedList.put(incomingIP, dfc.getStatus());
					if(receiveCompletedList.keySet().size() == publishersNeighbor.keySet().size()) {
						logger.info("RECEIVED DATA SEND COMPLETE MESSAGE FROM ALL PUBLISHERS");
						
						int commonRecords = 0;
						int totalRecords = compareList.keySet().size();
						// display results
						System.out.println();
						System.out.println("------------------------- UNIQUE RECORDS ------------------------");
						System.out.printf("%15s  %-30s   %-25s   %-25s %-35s %-25s %-25s%n", "ID", "NAME", "ADDRESS", "DIAGNOSIS",
								"TREATMENT","COMMENTS","SOURCE");
						for(String key : compareList.keySet()) {
							TableDataFormat tdf = compareList.get(key);
							if(!tdf.getSource().contains(",")) {
								System.out.printf("%15s  %-30s   %-25s   %-25s %-35s %-25s %-25s%n", tdf.getId(), tdf.getName(), tdf.getAddress(),
										tdf.getDiagnosis(),tdf.getTreatment(),tdf.getComments(),tdf.getSource());
							}
							
						}
						System.out.println();
						System.out.println("------------------------- COMMON RECORDS ------------------------");
						System.out.printf("%15s  %-30s   %-25s   %-25s %-35s %-25s %-25s%n", "ID", "NAME", "ADDRESS", "DIAGNOSIS",
								"TREATMENT","COMMENTS","SOURCE");
						for(String key : compareList.keySet()) {
							TableDataFormat tdf = compareList.get(key);
							if(tdf.getSource().contains(",")) {
								System.out.printf("%15s  %-30s   %-25s   %-25s %-35s %-25s %-25s%n", tdf.getId(), tdf.getName(), tdf.getAddress(),
										tdf.getDiagnosis(),tdf.getTreatment(),tdf.getComments(),tdf.getSource());
								commonRecords++;
							}
							
						}
						System.out.println("TOTAL COMMON RECORDS = "+commonRecords);
						System.out.println("TOTAL UNIQUE RECORDS = "+(totalRecords - commonRecords));
						endTime = System.currentTimeMillis();
						System.out.println("TIME TAKEN = "+(endTime - startTime) + " MS");
						
						// insert broker mapping table values
						if(brokerTableMapping.size() > 0) {
						doBrokerMapping(brokerTableMapping);
						insertBrokerLookup(compareList);
						}
						
					}
				}
			}
			else {
				
				logger.severe("THE INCOMING MESSAGE SOURCE HAS NO EXISTENCE IN LIST OF PUBLISHERS");
			}
		}
		
		else if(SearchResponse.class.toString().equals(e.getClass().toString())) {
			
			SearchResponse sr = (SearchResponse) e;
			
			logger.info("RECEIVED SEARCH RESULT FROM PUBLISHER "+sr.getIpAddress()+"-"+sr.getHostName());
			String result = sr.getEncryptedData();
			
			if (result.trim().equalsIgnoreCase("") || result == null) {
				logger.severe("RECEIVED EMPTY SEARCH RESULT FROM PUBLISHER "+sr.getIpAddress()+"-"+sr.getHostName());
				String key = sr.getIpAddress();
				TableDataFormat tdf = new TableDataFormat();
				tdf.setId("NOT FOUND");
				tdf.setName("NOT FOUND");
				tdf.setAddress("NOT FOUND");
				tdf.setDiagnosis("NOT FOUND");
				tdf.setTreatment("NOT FOUND");
				tdf.setComments("NOT FOUND");
				tdf.setSource(sr.getIpAddress() + "=" + sr.getHostName());
				compareSearch.put(key, tdf);
			}
			else {
				String[] allData = result.split(",");
				System.out.println("RESULT ="+result);
				for (int i = 0; i < allData.length; i = i + 6) {
					String key = sr.getIpAddress();
					if (compareSearch.get(key) == null) {
						TableDataFormat tdf = new TableDataFormat();
						tdf.setId(allData[i]);
						tdf.setName(allData[i + 1]);
						tdf.setAddress(allData[i + 2]);
						tdf.setDiagnosis(allData[i + 3]);
						tdf.setTreatment(allData[i + 4]);
						tdf.setComments(allData[i + 5]);
						tdf.setSource(sr.getIpAddress() + "=" + sr.getHostName());
						compareSearch.put(key, tdf);
					} else if (compareSearch.get(key) != null) {
						logger.severe("DUPLICATE SEARCH RESULT MESSAGE FROM " + sr.getIpAddress());
					}
				}
			}
			
		}
		
		else if(SearchCompleted.class.toString().equals(e.getClass().toString())) {
			
			SearchCompleted sc = (SearchCompleted) e;
			String incomingIP = sc.getIpAddress();
			String incomingHostName = sc.getHostName();
			//System.out.println(searchRequestSentList);
			logger.info("RECEIVED SEARCH COMPLETED FROM - "+incomingIP+" HOSTNAME = "+incomingHostName);
			if(searchRequestSentList.contains(sc.getIpAddress())) {
				//logger.info("KNOWNHOST PASS");
				if(searchRequestReceivedList.contains(incomingIP)) {
					logger.severe("DUPLICATE SEARCH COMPLETED MESSAGE FROM PUBLISHER "+incomingIP);
				}
				else {
					
					searchRequestReceivedList.add(incomingIP);
					if(searchRequestSentList.size() == searchRequestReceivedList.size()) {
						logger.info("RECEIVED DATA SEARCH COMPLETE MESSAGE FROM ALL PUBLISHERS");
						
						System.out.println("------------------------- SEARCH RESULTS ------------------------");
						System.out.printf("%15s  %-30s   %-25s   %-25s %-35s %-25s %-25s%n", "ID", "NAME", "ADDRESS", "DIAGNOSIS",
								"TREATMENT","COMMENTS","SOURCE");
						for(String key : compareSearch.keySet()) {
							TableDataFormat tdf = compareSearch.get(key);
								System.out.printf("%15s  %-30s   %-25s   %-25s %-35s %-25s %-25s%n", tdf.getId(), tdf.getName(), tdf.getAddress(),
										tdf.getDiagnosis(),tdf.getTreatment(),tdf.getComments(),tdf.getSource());
							
						}
					}
				}
			}
			else {
				
				logger.severe("THE INCOMING MESSAGE SOURCE HAS NO EXISTENCE IN LIST OF PUBLISHERS");
			}
		}
	}
	
	private void insertBrokerLookup(Map<String, TableDataFormat> compareList2) {
		// TODO Auto-generated method stub
		logger.info("INSERTING BROKER LOOKUP TABLE VALUES");
		if(compareList2 != null) {
			String emptyTable = "truncate table t_broker_data";
			Connection con = DBConnection.createConnection();
			try{
				Statement st1 = con.createStatement();
				st1.executeUpdate(emptyTable);
				
				for(String key : compareList2.keySet()) {
					TableDataFormat tdf = compareList2.get(key);
					String insert = "INSERT INTO t_broker_data VALUES" + "("
					+"'"+tdf.getId()+"','"+tdf.getName()+"','"+tdf.getAddress()+"','"+tdf.getDiagnosis()+
					"','"+tdf.getTreatment()+"','"+tdf.getComments()+"','"+tdf.getSource()+"'"+
							")";
					Statement st2 = con.createStatement();
					st2.executeUpdate(insert);
				}
				
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			logger.info("BROKER LOOKUP TABLE VALUES INSERTED !!");
		}
	}



	private void doBrokerMapping(ArrayList<String> brokerTableMapping2) {
		// TODO Auto-generated method stub
		logger.info("INSERTING BROKER MAPPING TABLE VALUES");
		if (brokerTableMapping2 != null) {
			//System.out.println(brokerTableMapping2);
			Connection con = DBConnection.createConnection();
			try {
				for (int i = 0; i < brokerTableMapping2.size(); i = i + 3) {
					String alreadypresent = "select * from broker_mapping where BROKER_GENERATED_ID ="
							+ "'"+ brokerTableMapping2.get(i+1)+"' AND SOURCE="+"'"+brokerTableMapping2.get(i+2)+"'";
					//System.out.println("SQL ="+alreadypresent);
					Statement s1 = con.createStatement();
					ResultSet res1 = s1.executeQuery(alreadypresent);
					//System.out.println("res1 = "+res1);
					if (res1.equals(null) || res1 == null || !res1.next()) {
						//System.out.println("NO RESULT");
						Statement inst=con.createStatement();
						String insert = "INSERT INTO broker_mapping VALUES" + " ("
								+"'"+brokerTableMapping2.get(i+1) + "',"
								+ "'" + brokerTableMapping2.get(i) +"',"
								+ "'" + brokerTableMapping2.get(i+2)+"')";
						inst.executeUpdate(insert);
					}
					else {
						String existingsource = res1.getString(3);
						if (existingsource.equalsIgnoreCase(brokerTableMapping2.get(i + 2))) {

							// System.out.println("PREV RESULT");
							Statement inst = con.createStatement();
							String update = "UPDATE broker_mapping SET INCOMING_ID = " + "'"
									+ brokerTableMapping2.get(i) + "'" + "WHERE BROKER_GENERATED_ID = " + "'"
									+ brokerTableMapping2.get(i + 1) + "' AND SOURCE = " + "'"
									+ brokerTableMapping2.get(i + 2) + "'";
							inst.executeUpdate(update);
						}
					}
				}

			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			logger.info("BROKER MAPPING TABLE INSERT COMPLETE !");
		}
		
	}



	/**
	 * METHOD FOR EL GAMAL ENCRYPTION 
	 * @param m - message to encrypt
	 * @param p - prime number 
	 * @param g - generator
	 * @param b - public key
	 * @param k - sender random
	 * @return - a big integer array which contain the cipher text c1 and c2 in bia[0] and bia[1] respectively
	 */
	public static BigInteger[] encrypt(BigInteger m, BigInteger p,
			BigInteger g, BigInteger b, BigInteger k) {

		try {
			BigInteger[] bia = new BigInteger[2];
			bia[0] = g.modPow(k, p);
			bia[1] = b.modPow(k, p);
			bia[1] = bia[1].multiply(m);
			bia[1] = bia[1].mod(p);
			return bia;
		} catch (ArithmeticException e) {
			throw new RuntimeException("EXECPTION WHILE ENCRYPTING");
		}
	}

	
	/**
	 * METHOD FOR EL GAMAL DECRYPTION
	 * @param bia - contains the cipher text c1 and c2 in bia[0] and bia[1] respectively 
	 * @param p - the prime number
	 * @param a - the secret key
	 * @return - the original message
	 * @throws ArithmeticException
	 */
	public static BigInteger decrypt(BigInteger[] bia, BigInteger p,
			BigInteger a) throws ArithmeticException {
		try {
			BigInteger temp = bia[0].modPow(a, p);
			temp = temp.modInverse(p);
			BigInteger recover = temp.multiply(bia[1]);
			recover = recover.mod(p);
			return recover;
		} catch (ArithmeticException e) {
			throw new RuntimeException("EXECPTION WHILE DECRYPTING");
		}
	}

	public synchronized void handleRegisterRequest(RegistrationRequestMessage r, Socket s) {
		String key = r.getIpAddress()+":"+ r.getSocketPortNum();
		RegisterResponse rsp;
		
		if(searchKnownHosts(key)!= null){
			String info="NODE BY THIS IP:PORT ALREADY EXISTS";
			rsp = new RegisterResponse(MessageTypes.REGISTRATION_RESPONSE.typeCode(), "FAILURE", info);
			logger.info(info);
			
		} else {
			HostInfo h = new HostInfo();
			h.setIp(r.getIpAddress());
			h.setSocketPortNo(r.getSocketPortNum());
			h.setServerSocketPortNo(r.getServerPortNum());
			if(addrToSocketMap.get(key) == null) {
				logger.severe("REQUEST PORT INFO DOES NOT MATCH WITH PORT IN ADDRTOSOCKET MAP");
			}
			h.setSock(addrToSocketMap.get(key));
			//knownHosts.put(key,h);
			addKnownHost(key,h);
			buildPublisherNeighbors(knownHosts);
			String info="WELCOME TO THE CLUSTER ! THERE ARE CURRENTLY "+getNumKnownHosts()+" NODE(S) IN THE REGISTRY, INCLUDING YOU.";
			rsp = new RegisterResponse(MessageTypes.REGISTRATION_RESPONSE.typeCode(), "SUCCESS", info);
			logger.info(info);
			//System.out.println(Arrays.asList(knownHosts));
			// display known hosts
			/*Iterator iterator = knownHosts.keySet().iterator();

			while (iterator.hasNext()) {
			   String ipport = iterator.next().toString();
			   HostInfo value = knownHosts.get(ipport);

			   System.out.println(ipport + " " + value.getIp()+" "+value.getSocketPortNo()+" "+value.getServerSocketPortNo());
			}*/
			
		}
		byte[] msg = rsp.getBytes();
		TCPSender sender = new TCPSender(s,"brokerlog.out");
		
		sender.sendBytes(msg);
		logger.info("Registration Response sent !");
		if(setupExecutedOnce) {
			System.out.println("SINCE A PUBLISHER HAS BEEN ADDED , SETUP IS TO BE EXECUTED AGAIN");
			logger.info("SINCE A PUBLISHER HAS BEEN ADDED , SETUP IS TO BE EXECUTED AGAIN");
		}
		displayBrokerMenu();
	}
	
/* SENDS OUT A DEREGISTER RESPONSE TO THE PUBLISHER NODE */
	
	public void handleDeregisterRequest(DeregisterRequest r, Socket s) {
		/* THE IP AND PORT RECEIVED IS THE IP AND PORT OF THE MESSAGING NODE SIDE SOCKET */
		String key = r.getIp()+":"+ r.getPort();
		
		boolean validity = checkValidityDreq(r.getIp(), r.getPort(), key, s);
		DeregisterResponse rsp;
		if(!validity){
			
			String info="INVALID DEREGISTRATION REQUEST";
			rsp = new DeregisterResponse(MessageTypes.DEREGISTER_RESPONSE.typeCode(), info);
			
		} else {
			String info="SUCCESS. PLEASE REMOVE YOURSELF.";
			rsp = new DeregisterResponse(MessageTypes.DEREGISTER_RESPONSE.typeCode(), info);
			
		}
		byte[] msg = rsp.getBytes();
		logger.info("DEREGISTRATION_RESPONSE SENT OUT FROM REGISTER...");
		TCPSender sender = new TCPSender(s,"brokerlog.out");
		
		sender.sendBytes(msg);
		
		if(validity) {
			removeKnownHostEntry(key);
			buildPublisherNeighbors(knownHosts);
		}
		if(setupExecutedOnce && getNumKnownHosts() >= 2) {
			System.out.println("SINCE A PUBLISHER HAS BEEN REMOVED , SETUP IS TO BE EXECUTED AGAIN");
			logger.info("SINCE A PUBLISHER HAS BEEN REMOVED , SETUP IS TO BE EXECUTED AGAIN");
		}
		displayBrokerMenu();
	}
	
/* CHECKS VALIDITY OF DEREGISTER REQUEST */
	
	private void buildPublisherNeighbors(Map<String, HostInfo> updatedknownHosts) {
		// TODO Auto-generated method stub
		
		//System.out.println("KNOWN HOSTS = "+updatedknownHosts.keySet());
		publishersNeighbor.clear();
		publisherList.clear();
		//System.out.println(publisherList);
		publisherList.addAll(updatedknownHosts.keySet());
		
		for(int i=0;i<publisherList.size();i++) {
			publishersNeighbor.put(publisherList.get(i), updatedknownHosts.get(publisherList.get((i+1) % publisherList.size() )));
		}
		/*Iterator iterator = publishersNeighbor.keySet().iterator();

		while (iterator.hasNext()) {
		   String ipport = iterator.next().toString();
		   HostInfo value = publishersNeighbor.get(ipport);

		   System.out.println(ipport + " " + value.getIp()+" "+value.getSocketPortNo()+" "+value.getServerSocketPortNo());
		}*/
		
	}



	public boolean checkValidityDreq(String ip, int port, String key, Socket s) {
		
		String remoteIp = s.getInetAddress().toString().split("/")[1];
		if(!ip.equals(remoteIp)) {
			return false;
		}
		
		if(!findKnownHostEntry(key)) {
			return false;
		}
		return true;
	}
	
	
	public void showActivePublishers() {
		
		if(publishersNeighbor.size() == 0) {
			System.out.println("There are no active publishers");
		}
		else if(publishersNeighbor.size() == 1) {
			
			System.out.println("There is only 1 active publisher. No neighbors are present");
			System.out.println(publishersNeighbor.keySet());
		}		
		else {
			Iterator iterator = publishersNeighbor.keySet().iterator();

			while (iterator.hasNext()) {
				String ipport = iterator.next().toString();
				HostInfo value = publishersNeighbor.get(ipport);
				System.out.printf("%20s  %-12s   %-12s   %-21s%n", "Publisher", "Neighbor", "Socket", "Listening ON");
				System.out.printf("%20s  %-12s   %-12s   %-21s%n", ipport, value.getIp(), value.getSocketPortNo(),
						" Listening on - " + value.getServerSocketPortNo());
			}
		}
	}
	
	// SENDS SETUP INITIATE REQUEST TO ALL ACTIVE PUBLISHERS
	public void sendSetupInitiateRequest() {
	
		if(getNumKnownHosts() <= 1) {
			logger.severe("TO EXECUTE SETUP AT LEAST 2 PUBLISHERS ARE REQUIRED");
		}
		else {
		brokerPrimeNumber = "13256401";
		brokerGenerator = "957";
		brokerPublicKey = "9730883";
		brokerSecretKey = "1591864";
		
		Iterator iterator = knownHosts.keySet().iterator();

		while (iterator.hasNext()) {
				String ipport = iterator.next().toString();
				HostInfo value = knownHosts.get(ipport);
				Socket sendToPublisher = value.getSock();
				dispatchSetupRequest(ipport, sendToPublisher);
			}
		}
		setupExecutedOnce = true;
		
	}
	
	
	
	private void dispatchSetupRequest(String ipport, Socket sendToPublisher) {
		// TODO Auto-generated method stub
		SetupInitiateRequest sir = createSetupInitiateRequest();
		byte[] setupInitiateMsg = sir.getBytes();
		TCPSender sender = new TCPSender(sendToPublisher,"brokerlog.out");
		
		sender.sendBytes(setupInitiateMsg);
		
		logger.info("BROKER HAS DISPATCHED SETUP INITIATE REQUEST TO "+ipport);
	}



	private SetupInitiateRequest createSetupInitiateRequest() {
		// TODO Auto-generated method stub
		SetupInitiateRequest req = new SetupInitiateRequest();
		req.setType(MessageTypes.SETUP_INITIATE_REQUEST.typeCode());
		req.setPrimeNumber(brokerPrimeNumber);
		req.setGenerator(brokerGenerator);
		req.setPublicKey(brokerPublicKey);
		req.setPublishersNeighbor(publishersNeighbor);
		return req;
	}
	
	public void sendDataFetchRequest(int numofRows,int cmd) {
		// TODO Auto-generated method stub
		if(numofRows == 0) {
			logger.severe("NO NEED TO FETCH 0 ROWS");
		}
		if(getNumKnownHosts() <= 1) {
			logger.severe("LESS THAN 2 PUBLISHERS. CANNOT COMPARE FETCHED RECORDS");
		}
		else {
			// reset the compare list which contains the incoming data
		compareList.clear();
		receiveCompletedList.clear();
		brokerTableMapping.clear();
		Iterator iterator = knownHosts.keySet().iterator();

		while (iterator.hasNext()) {
				String ipport = iterator.next().toString();
				HostInfo value = knownHosts.get(ipport);
				Socket sendToPublisher = value.getSock();
				dispatchDataFetchRequest(ipport, sendToPublisher,numofRows,cmd);
			}
		}
		
	}
	
	
	
	private void dispatchDataFetchRequest(String ipport, Socket sendToPublisher, int numofRows,int cmd) {
		// TODO Auto-generated method stub
		FetchDataRequest fdr = new FetchDataRequest();
		
		fdr.setMsgType(MessageTypes.FETCH_DATA_REQUEST.typeCode());
		if(cmd==4) {
		fdr.setEncryptedResults(1);
		}
		else if(cmd == 3){
			fdr.setEncryptedResults(0);
		}
		fdr.setNumberofRows(numofRows);
		
		byte[] dataFetchMsg = fdr.getBytes();
		TCPSender sender = new TCPSender(sendToPublisher,"brokerlog.out");
		
		sender.sendBytes(dataFetchMsg);
		
		startTime = System.currentTimeMillis();
		logger.info("BROKER HAS DISPATCHED DATA FETCH REQUEST TO "+ipport);
		
	}



		// CREATES A RANDOM BIGINT WITHIN THE PROVIDED UPPER LIMIT
		public static BigInteger getRandomBigInteger(BigInteger upperlimit) {
			Random rand = new Random();
			BigInteger randomNBitBigInteger;
			do {
				randomNBitBigInteger = new BigInteger(upperlimit.bitLength(), rand); 
			}while(randomNBitBigInteger.compareTo(upperlimit) >= 0);
			return randomNBitBigInteger;
			
			
		}
		
		
		/**
		 * CREATE THE N BIT RANDOM NUMBER
		 */
		public static BigInteger getRandomBigInteger(int bitlength) {
			Random rand = new Random();
			BigInteger randomNBitBigInteger;
			int y=bitlength - 1; // here y is set to 255 as we need to ensure the random number to be of 256 bits
			BigInteger bigNumber = new BigInteger("2");
			BigInteger b2 = bigNumber.pow(y).subtract(BigInteger.ONE); // generates the value of 2^255 - 1 
			do {
				randomNBitBigInteger = new BigInteger(bitlength, rand); // generates random number from 0 to 2 ^256 - 1 
			}while(randomNBitBigInteger.compareTo(b2) <= 0); // compare if random number is > than 2^255 - 1 so that we always have 256 bit random number
			return randomNBitBigInteger;
			
			
		}



	public synchronized boolean findKnownHostEntry(String key) {
		
		if(knownHosts.get(key) != null) {
			return true;
		}
		
		return false;
	}
	
	public synchronized void removeKnownHostEntry(String key) {
		System.out.println("Removing "+key+" from the cluster");
		knownHosts.remove(key);
	}
	

	@Override
	public void onCommand(String s) {
		// TODO Auto-generated method stub
		
	}
	public synchronized Map<String, Socket> getAddrToSocketMap() {
		return addrToSocketMap;
	}
	
	public synchronized void addToAddrToSocketMap(String key, Socket sock) {
		addrToSocketMap.put(key, sock);
	}

	public synchronized void setAddrToSocketMap(Map<String, Socket> addrToSocketMap) {
		this.addrToSocketMap = addrToSocketMap;
	}
	public NodeGroup getNodeGroup() {
		return nodeGroup;
	}

	public void setNodeGroup(NodeGroup nodeGroup) {
		this.nodeGroup = nodeGroup;
	}
	
	public ServerSocket getBrokerServerSocket() {
		return brokerServerSocket;
	}



	public void setBrokerServerSocket(ServerSocket brokerServerSocket) {
		this.brokerServerSocket = brokerServerSocket;
	}
	
	private synchronized HostInfo searchKnownHosts(String key) {
		// TODO Auto-generated method stub
		return knownHosts.get(key);
	}

	private synchronized void addKnownHost(String key, HostInfo h) {
		System.out.println("Adding "+key+" to the cluster");
		knownHosts.put(key,h);
	}
	
	public synchronized int getNumKnownHosts() {
		return knownHosts.size();
	}
	
	public synchronized Map<String, HostInfo> getKnownHosts() {
		return knownHosts;
	}

	public synchronized void setKnownHosts(Map<String, HostInfo> knownHosts) {
		this.knownHosts = knownHosts;
	}



	public void getBrokerLookupTable() {
		// TODO Auto-generated method stub
		Connection con = DBConnection.createConnection();
		String query = "select * from t_broker_data";
		
		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(query);
			System.out.println("---------------------------------- BROKER DATA TABLE ----------------------------------");
			System.out.printf("%15s  %-30s   %-25s   %-25s %-35s %-25s %-25s%n", "ID", "NAME", "ADDRESS", "DIAGNOSIS",
					"TREATMENT","COMMENTS","SOURCE");
			
			while(rs.next()) {
				
				System.out.printf("%15s  %-30s   %-25s   %-25s %-35s %-25s %-25s%n", rs.getString(1), rs.getString(2),
						rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7));
				
			}
			
		}
		catch(SQLException ex) {
			ex.printStackTrace();
		}
		displayBrokerMenu();
	}



	public void sendSearchRequest(String searchID, int cmd) {
		// TODO Auto-generated method stub
		Connection con = DBConnection.createConnection();
		searchRequestSentList.clear();
		searchRequestReceivedList.clear();
		compareSearch.clear();
		String query = "select * from broker_mapping where BROKER_GENERATED_ID = "+searchID;
		String check = "select * from broker_mapping where BROKER_GENERATED_ID = "+searchID;
		try {
			Statement st = con.createStatement();
			Statement st1 = con.createStatement();
			ResultSet rs = st.executeQuery(query);
			ResultSet rs1 = st1.executeQuery(check);
			
			if(rs1.equals(null) || rs1 == null || !rs1.next()) {
				logger.severe("THE PROVIDED ID IS NOT IN BROKER MAPPING TABLE");
			}
			else {
				while (rs.next()) {
					String incomingID = rs.getString(2);
					String source = rs.getString(3).split("=")[0];
					dispatchSearchRequest(incomingID, source);
				}
			}
		}
		catch(SQLException ex) {
			ex.printStackTrace();
		}
		
		
	}



	private void dispatchSearchRequest(String incomingID, String source) {
		// TODO Auto-generated method stub
		SearchRequest sr = new SearchRequest();
		sr.setType(MessageTypes.SEARCH_REQUEST.typeCode());
		sr.setId(incomingID);
		searchRequestSentList.add(source);
		Iterator iterator = knownHosts.keySet().iterator();

		while (iterator.hasNext()) {
			String ipport = iterator.next().toString();
			if (ipport.contains(source)) {
				HostInfo value = knownHosts.get(ipport);
				Socket sendToPublisher = value.getSock();
				byte[] searchInitiateMsg = sr.getBytes();
				TCPSender sender = new TCPSender(sendToPublisher,"brokerlog.out");
				
				sender.sendBytes(searchInitiateMsg);
				
				logger.info("BROKER HAS DISPATCHED SEARCH INITIATE REQUEST TO "+source);
			}
		}
		
	}



}
