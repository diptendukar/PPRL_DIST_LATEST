package com.pprl.main;


import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

import com.pprl.messageformats.DataFetchCompleted;
import com.pprl.messageformats.DeregisterRequest;
import com.pprl.messageformats.DeregisterResponse;
import com.pprl.messageformats.Event;
import com.pprl.messageformats.FetchDataRequest;
import com.pprl.messageformats.FetchDataResponse;
import com.pprl.messageformats.MessageTypes;
import com.pprl.messageformats.RegisterResponse;
import com.pprl.messageformats.RegistrationRequestMessage;
import com.pprl.messageformats.SearchCompleted;
import com.pprl.messageformats.SearchRequest;
import com.pprl.messageformats.SearchResponse;
import com.pprl.messageformats.SetupCompletedResponse;
import com.pprl.messageformats.SetupForwardRequest;
import com.pprl.messageformats.SetupInitiateRequest;
import com.pprl.transport.TCPReceiver;
import com.pprl.transport.TCPSender;
import com.pprl.transport.TCPServer;
import com.pprl.util.DBConnection;
import com.pprl.util.HostInfo;
import com.pprl.util.LogFactory;
import com.pprl.util.PublisherInputThread;













public class Publisher implements Node{
	
	

	


	private String brokerIP;
	private String brokerPort;
	
	/* This is a single ServerSocket object responsible for listening to any
	 * Request coming in to this Publisher node */
	private ServerSocket publisherServerSocket;
	
	/* THIS IS THE SOCKET USED TO COMMUNICATE WITH THE Broker */
	private static Socket socketToBroker; 
	/* THIS IS THE SOCKET PORT FOR THE ABOVE*/
	private static int socketToBrokerPort;
	
	/* THIS IS NEEDED TO INFORM OTHER PUBLISHERS/BROKER NODES ABOUT THE SERVERSOCKET OF THIS MESSAGING NODE*/
	private int publisherServerPort;
	
	private static Logger logger;
	
	/* Hostname of this publisher (my own hostname / this machine hostname) */
	public static String hostName;
	
	/* Ip of the this publisher (my own IP / this machine IP) */
	public static String localIP;
	private TCPSender publisherTCPSender;
	private TCPServer publisherTCPServer;
	private TCPReceiver publisherTCPReceiver;
	private List<TCPReceiver> receiverThreadList;
	PublisherInputThread publisherInput;
	
	private BigInteger publisherSecretNumber;
	private BigInteger publisherSecretKey;
	private BigInteger publisherRandomNumber;
	private BigInteger publisherRandomNumberInverse;
	private String publisherKeyConverter;
	private BigInteger publisherMessageEncryptionKey;
	
	private boolean addRandomness = false;
	private Map<String, HostInfo> publishersNeighborInfo;
	private List<String> processedPublishers;
	
	private BigInteger brokerPrime;
	private BigInteger brokerGenerator;
	private BigInteger brokerPublicKey;
	private Map<String, HostInfo> knownNeighborMap;
	
	private static Socket socketToNextPublisher; 
	
	public Publisher(String brokerIP,String brokerPort) throws UnknownHostException {
		
		hostName = InetAddress.getLocalHost().getHostName();
		localIP = InetAddress.getLocalHost().getHostAddress();
		logger = LogFactory.getLogger(Publisher.class.getName(), "publisher-"+hostName+".out");
		
		logger.info("Logger Initialized in Publisher on "+hostName);
		publisherTCPSender = new TCPSender("publisher Node-"+hostName+".out");
		
		String hostStr = InetAddress.getByName(brokerIP).toString();
		this.brokerIP = hostStr.split("/")[1];
		this.brokerPort = brokerPort;
		knownNeighborMap = new HashMap<String, HostInfo>();
		receiverThreadList = new ArrayList<TCPReceiver>();
		processedPublishers = new ArrayList<String>();
		
		try {
			/* Dynamically Assign a port to this socket */
			publisherServerSocket = new ServerSocket(0);
			publisherServerPort = publisherServerSocket.getLocalPort();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.severe("ServerSocket creation failed at "+ hostName);
			e.printStackTrace();
		}
		logger.severe("ServerSocket created at "+ hostName+":"+ publisherServerSocket.getLocalPort());
		
	}

	public static void main(String[] args) {
		
		String brokerIP,brokerPort;
		
			Scanner userInput = new Scanner(System.in);
			System.out.println("\n*** Privacy Preserving Record Linkage ***\n");
			
			//System.out.println("Press 1 to Register to Broker");
			//System.out.println("Press 2 to De Register (Exit)");
			

			
			
				System.out.println("Enter IP address of Broker");
				brokerIP = userInput.next();
				
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
				pub.startup(userInput);
				//System.out.println("MY IP - "+publishersOwnIP);
				//isRegistered = true;
				//System.out.println("done");
			

	}

public void startup(Scanner userInput) {
		
		/*
		 *  This is the Server Thread that is in charge of listening to incoming requests on this
		 * messaging node */
		
		publisherTCPServer = new TCPServer(publisherServerSocket, null, this);
		
		/* This is the Message Node Server Thread that listens to incoming connections */
		Thread publisherServerThread = new Thread(publisherTCPServer);

		/*=====================SERVER THREAD STARTED==================================*/
		
		publisherServerThread.start();
		logger.info("Publisher Server Thread started");
		
	/* Starting Up the Thread In charge of reading from Console */
		
		publisherInput = new PublisherInputThread(this,userInput);
		Thread consoleReaderThread = new Thread(publisherInput);
		
		/*=====================CONSOLE READER THREAD STARTED==================================*/
		
		consoleReaderThread.start();
		logger.info("Messaging Node Console Reader started");
		logger.info("BROKER IP IS========================:"+getBrokerIP());
		
		
		
		/*==================STARTING A RECEIVER THREAD=========================================*/
		
		socketToBroker = startupMessagingNodeLinkToRegistry();
		publisherTCPReceiver = new TCPReceiver(socketToBroker, this);
		Thread publisherReceiverFromServerThread = new Thread(publisherTCPReceiver);
		publisherReceiverFromServerThread.start();
		
		
		
		/*==================SENDING OUT REGISTRATION REQUEST==================================*/
		
		socketToBrokerPort = socketToBroker.getLocalPort();
		
		sendRegisterRequest(socketToBroker);
		
		
		
		try {
			
			publisherServerThread.join();
			consoleReaderThread.join();
			publisherReceiverFromServerThread.join();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

public void sendRegisterRequest(Socket s) {
	RegistrationRequestMessage regreqMsg = constructRegisterMessage(s);
	byte[] msg = regreqMsg.getBytes();
	
	TCPSender sender = new TCPSender(s,"overlay-messagingNode-"+hostName+".out");
	
	sender.sendBytes(msg);
	logger.info("MESSAGING NODE HAS DISPATCHED REGISTRATION REQUEST....");
	//req.
}

/* ALLOWS MESSAGING NODE TO SEND REQUEST TO REGISTRY */

public Socket startupMessagingNodeLinkToRegistry() {
	Socket s = null;
	try {
		System.out.println("BROKER IP IS========================:"+getBrokerIP());
		logger.info("BROKER IP IS========================:"+getBrokerIP());
		logger.info("BROKER: "+getBrokerIP()+":::REGISTRY_PORT:"+getBrokerPort());
		s = new Socket(getBrokerIP(), Integer.valueOf(getBrokerPort()));
	} catch (UnknownHostException e) {
		logger.severe("Unable to connect to RegistryNode from "+ hostName+"(UnknownHost):"+e.getMessage());
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		logger.severe("Unable to connect to RegistryNode from "+ hostName+"(IOException):"+e.getMessage());
		e.printStackTrace();
	}
	return s;
}

public RegistrationRequestMessage constructRegisterMessage(Socket s) {
	RegistrationRequestMessage req = new RegistrationRequestMessage();
	req.setMsgType(MessageTypes.REGISTRATION_REQUEST.typeCode());
	req.setIpAddress(localIP);
	req.setServerPortNum(publisherServerPort);
	req.setSocketPortNum(socketToBrokerPort);
	
	return req;
}

public static void sendDeregisterRequest(byte[] msg, Socket s) {
	
	TCPSender sender = new TCPSender(s,"publisher-"+hostName+".out");
	
	sender.sendBytes(msg);
	logger.info("PUBLISHER NODE "+hostName+" HAS DISPATCHED DEREGISTRATION REQUEST....");
}

public synchronized List<TCPReceiver> getReceiverThreadList() {
	return receiverThreadList;
}


public synchronized void addToReceiverThreadList(TCPReceiver h) {
	this.receiverThreadList.add(h);
}


public synchronized void setReceiverThreadList(List<TCPReceiver> receiverThreadMap) {
	this.receiverThreadList = receiverThreadMap;
}

@Override
public void onEvent(Event e, Socket s) {
	//System.out.println("HERE");
	// TODO Auto-generated method stub
	logger.info("Publisher "+hostName+" received some message");
	if(RegisterResponse.class.toString().equals(e.getClass().toString())) {
		RegisterResponse regRsp = (RegisterResponse) e;
		logger.info("Message Received from Broker. Status: "+regRsp.getStatus()+", INFO: "+regRsp.getInfoMsg());
		System.out.println("PRESS 2 to Deregister this publisher");
		
	}
	else if(DeregisterRequest.class.toString().equals(e.getClass().toString())) {
		DeregisterRequest d = (DeregisterRequest) e;
		d.setIp(getLocalIP());
		d.setPort(getSocketToBrokerPort());
		byte[] msg = d.getBytes();
		sendDeregisterRequest(msg, socketToBroker);
		
	}
	else if(DeregisterResponse.class.toString().equals(e.getClass().toString())) {
		DeregisterResponse d = (DeregisterResponse)e;
		if(d.getStatus().contains("SUCCESS")) {
			
			logger.info("REGISTER RETURNED SUCCESS MESSAGE...SHUTTING DOWN THIS NODE");
			
			
			publisherTCPReceiver.setGetOut();
			publisherTCPServer.setGetOut();
			//logger.info("SHUTTING DOWN CONSOLE READER");
			publisherInput.exitPublisherMenu();
			try {
				socketToBroker.close();
				logger.info("SOCKET TO REGISTER SHUT DOWN SUCCESSFULLY");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				logger.info("FAILURE TO SHUT DOWN SOCKET TO BROKER");
				e1.printStackTrace();
			}
			
			
			try {
				publisherServerSocket.close();
				logger.info("PUBLISHER SERVER SOCKET SHUT DOWN SUCCESSFULLY");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				logger.info("FAILURE TO SHUT DOWN SERVER SOCKET");
				e1.printStackTrace();
			}
			
		} else {
			logger.info("REGISTER DID NOT RETURN SUCCESS MESSAGE");
		}
		
	}
	else if(SetupInitiateRequest.class.toString().equals(e.getClass().toString())) {
		logger.info("RECEIVED SETUP INITIATE REQUEST");
		
		SetupInitiateRequest setupreq = (SetupInitiateRequest) e;
		//logger.info("PRIME NUM RECEIVED FROM BROKER = "+setupreq.getPrimeNumber());
		//logger.info("GENERATOR RECEIVED FROM BROKER = "+setupreq.getGenerator());
		//logger.info("PUBLIC KEY RECEIVED FROM BROKER = "+setupreq.getPublicKey());
		// GENERATING THE SECRET NUM AND RANDOM NUMBER FOR THIS PUBLISHER
		brokerPrime = new BigInteger(setupreq.getPrimeNumber());
		brokerGenerator =  new BigInteger(setupreq.getGenerator());
		brokerPublicKey =  new BigInteger(setupreq.getPublicKey());
		
		publishersNeighborInfo = setupreq.getPublishersNeighbor();
		//System.out.println(publishersNeighborInfo.keySet());
		String myKey = localIP+":"+String.valueOf(socketToBrokerPort);
		//System.out.println(myKey);
		String nextPublisherIP =  publishersNeighborInfo.get(myKey).getIp();
		int nextPublisherListeningPort = Integer.valueOf(publishersNeighborInfo.get(myKey).getServerSocketPortNo());
		
		logger.info("NEXT PUBLISHER IS "+nextPublisherIP+" LISTENING PORT IS "+nextPublisherListeningPort);
		
		publisherSecretNumber = getRandomBigInteger(brokerPrime);
		// CREATING 16 bit random number
		publisherRandomNumber = getRandomBigInteger(16);
		// random inverse
		publisherRandomNumberInverse = findModInverse(publisherRandomNumber, brokerPrime);
		
		// secret key
		publisherSecretKey = brokerGenerator.modPow(publisherSecretNumber, brokerPrime);
		
		// Message Enc Key SK * R
		publisherMessageEncryptionKey = (publisherSecretKey.multiply(publisherRandomNumber)).mod(brokerPrime);
		
		// encypt R Inv with broker pub key
		BigInteger[] encPublisherRandNumWithBrokerPubKey = encrypt(publisherRandomNumberInverse, brokerPrime, brokerGenerator, brokerPublicKey, publisherSecretNumber);
		
		socketToNextPublisher = startupNextPublisherLink(nextPublisherIP,nextPublisherListeningPort);
		
		dispatchSetupForwardRequest(nextPublisherIP,socketToNextPublisher,myKey,encPublisherRandNumWithBrokerPubKey);
		/*if(publisherSecretKey == null || publisherSecretKey.trim() == "" || publisherSecretKey.trim() == " ") {
			generatePublisherSecretKey();
		}*/
	}
	else if(SetupForwardRequest.class.toString().equals(e.getClass().toString())) {
		logger.info("RECEIVED SETUP FORWARD REQUEST");
		
		SetupForwardRequest sfr = (SetupForwardRequest) e;
		
		String origin = sfr.getOrininatingFrom();
		logger.info("THIS MESSAGE ORIGINATED FROM "+origin);
		
		// Check if forward was initiated by this publisher
		if(origin.equalsIgnoreCase(localIP)) {
			logger.info("MY SETUP FORWARD MESSAGE HAS RETURNED TO ME");
			boolean setupcomplete = false;
			// get the nodes traversed
			String[] nodesVisited = sfr.getNodesTraversed().split("_");
			
			System.out.println(Arrays.asList(nodesVisited));
			System.out.println(publishersNeighborInfo.keySet());
			
			// check if all the nodes are covered from the neighborinfo map shared by broker during setup
			for (int i=0;i<nodesVisited.length;i++) {
				if(publishersNeighborInfo.keySet().toString().contains(nodesVisited[i])) {
					setupcomplete = true;
				}
			}
			if(setupcomplete) {
				publisherKeyConverter = sfr.getSharedKeyToken();
				String successmsg = "Publisher "+localIP+" has completed setup.....";
				SetupCompletedResponse scr = new SetupCompletedResponse(
						MessageTypes.SETUP_COMPLETED.typeCode(),"SUCCESS",successmsg);
				byte[] msg = scr.getBytes();
				
				TCPSender sender = new TCPSender(socketToBroker,"publisher Node-"+hostName+".out");
				
				sender.sendBytes(msg);
				logger.info("Publisher "+localIP+" HAS DISPATCHED SETUP COMPLETE RESPONSE....");
				processedPublishers.clear();
				logger.info("Processed publisher list cleared ");
				
			}
			else {
				logger.severe("SOMETHING IS WRONG. SETUP PHASE FAULT");
			}
			
		}
		// not initiated by this publisher
		else {
			logger.info("THIS IS NOT MY SETUP FOWARD MESSAGE.");
			if(processedPublishers.contains(origin)) {
				SetupForwardRequest newsfr = sfr;
				
				Socket nextPubSock = knownNeighborMap.get(localIP).getSock();
				
				byte[] forwardMsg = newsfr.getBytes();
				TCPSender sender = new TCPSender(nextPubSock,"publisher Node-"+hostName+".out");
				
				sender.sendBytes(forwardMsg);
				
				logger.info("PUBLISHER "+localIP+" HAS PROCESSED THIS ORIGIN MESSAGE BEFORE. FORWARDING TO "+knownNeighborMap.get(localIP).getIp());
			}
			else {
			
			SetupForwardRequest newsfr = new SetupForwardRequest();
			newsfr.setType(MessageTypes.SETUP_FORWARD_REQUEST.typeCode());
			newsfr.setOrininatingFrom(sfr.getOrininatingFrom());
			newsfr.setNodesTraversed(sfr.getNodesTraversed()+"_"+localIP);
			
			// encrypt secret key with broker public key and homomorphic multiply to incoming token
			BigInteger[] encPubSecKeyWithBrokerPubKey = encrypt(publisherSecretKey, brokerPrime, brokerGenerator, brokerPublicKey, publisherSecretNumber);
			BigInteger[] incomingToken = new BigInteger[2];
			incomingToken[0] = new BigInteger(sfr.getSharedKeyToken().split("_")[0]);
			incomingToken[1] = new BigInteger(sfr.getSharedKeyToken().split("_")[1]);
			
			BigInteger[] homomorphicToken = goHomomorphic(encPubSecKeyWithBrokerPubKey, incomingToken);
			newsfr.setSharedKeyToken(homomorphicToken[0].toString()+"_"+homomorphicToken[1].toString());
			// to record that this originator has been processed.
			processedPublishers.add(origin);
			Socket nextPubSock = knownNeighborMap.get(localIP).getSock();
			
			byte[] forwardMsg = newsfr.getBytes();
			TCPSender sender = new TCPSender(nextPubSock,"publisher Node-"+hostName+".out");
			
			sender.sendBytes(forwardMsg);
			
			logger.info("PUBLISHER "+localIP+" HAS ADDED OWN SECRET KEY AND FORWARDED TO "+knownNeighborMap.get(localIP).getIp());
			}
		}
	}
	else if(FetchDataRequest.class.toString().equals(e.getClass().toString())) {
		logger.info("RECEIVED DATA FETCH REQUEST");
		
		FetchDataRequest fdr = (FetchDataRequest) e;
		int numofRows = fdr.getNumberofRows();
		int encryptReqd = fdr.getEncryptedResults();
			if (encryptReqd == 0) {
				logger.info("NORMAL DATA FETCH REQUEST");
				// NORMAL RESULTS
				// table name temp_p4 and testp4table
				String emptyTable = "truncate table temp_p4";
				String insert = "insert into temp_p4 select * from testp4table order by rand() limit " + numofRows;
				String fetchResults = "select * from temp_p4";
				StringBuilder resultString = new StringBuilder();
				logger.info("Broker has requested " + numofRows + " to be fetched");
				logger.info(emptyTable);
				logger.info(insert);
				logger.info(fetchResults);
				Connection con = DBConnection.createConnection();
				try {
					// empty the contents
					Statement st1 = con.createStatement();
					st1.executeUpdate(emptyTable);

					// add random rows from main table testp4table into temp_p4
					Statement st2 = con.createStatement();
					st2.executeUpdate(insert);

					Statement st3 = con.createStatement();
					ResultSet rs = st3.executeQuery(fetchResults);

					while (rs.next()) {
						resultString.append(rs.getString(1));
						resultString.append(",");
						resultString.append(rs.getString(2));
						resultString.append(",");
						resultString.append(rs.getString(3));
						resultString.append(",");
						resultString.append(rs.getString(4));
						resultString.append(",");
						resultString.append(rs.getString(5));
						resultString.append(",");
						if(rs.getString(6) == null || rs.getString(6).trim() == "") {
							resultString.append("NONE");
							resultString.append(",");
						}
						else {
						resultString.append(rs.getString(6));
						resultString.append(",");
						}
					}
					if (resultString.length() > 0) {
						resultString.setLength(resultString.length() - 1);
					}

					dispatchNormalDataFetchResponse(resultString.toString());
					dispatchDataFetchCompleted();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
			else if (encryptReqd == 1) {
				// send encrypted results
				logger.info("ENCRYPTED DATA FETCH REQUEST");
				//logger.info("Broker has requested " + numofRows + " to be fetched");

				// testing purpose

				/*
				 * String data = "diptendu"; BigInteger input; try { input = new
				 * BigInteger(data); } catch (NumberFormatException ex) { byte[]
				 * ba = data.getBytes(); input = new BigInteger(ba); }
				 * 
				 * // encrypting data BigInteger[] encPubData = encrypt(input,
				 * brokerPrime, brokerGenerator, publisherMessageEncryptionKey,
				 * brokerPublicKey);
				 * dispatchEncryptedDataFetchResponse(encPubData[1].toString());
				 * dispatchDataFetchCompleted();
				 */
				String fetchResults = "select * from temp_p4";
				StringBuilder resultString = new StringBuilder();
				Connection con = DBConnection.createConnection();
				ArrayList<String> pubTableData = new ArrayList<String>();
				ArrayList<String> pubTableMapping = new ArrayList<String>();
				try {
					Statement st = con.createStatement();
					ResultSet rs = st.executeQuery(fetchResults);

					while (rs.next()) {
						pubTableData.add(rs.getString(1));
						pubTableData.add(rs.getString(2));
						pubTableData.add(rs.getString(3));
						pubTableData.add(rs.getString(4));
						pubTableData.add(rs.getString(5));
						if(rs.getString(6) == null || rs.getString(6).trim() == "") {
							pubTableData.add("NONE");
						}
						else {
							pubTableData.add(rs.getString(6));
						}
					}

				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (pubTableData != null) {
					for (String str : pubTableData) {
						if ((pubTableData.indexOf(str) % 6) == 3 || (pubTableData.indexOf(str) % 6) == 4 || (pubTableData.indexOf(str) % 6) == 5) {
							resultString.append(str);
							resultString.append(",");
						}
						else {
							BigInteger input;
							try {
								input = new BigInteger(str);
							} catch (NumberFormatException ex) {
								byte[] ba = str.getBytes();
								input = new BigInteger(ba);
							}
							BigInteger[] encPartyData = encrypt(input, brokerPrime, brokerGenerator,
									publisherMessageEncryptionKey, brokerPublicKey);
							resultString.append(encPartyData[1].toString());
							resultString.append(",");
							if ((pubTableData.indexOf(str) % 6) == 0) {
								pubTableMapping.add(str);
								pubTableMapping.add(encPartyData[1].toString());
							}
						}
					}
				}
				
				if (resultString.length() > 0) {
					resultString.setLength(resultString.length() - 1);
				}

				dispatchEncryptedDataFetchResponse(resultString.toString());
				dispatchDataFetchCompleted();
				doTableInserts(pubTableMapping);
			}
	}
	else if(SearchRequest.class.toString().equals(e.getClass().toString())) {
		logger.info("RECEIVED SEARCH REQUEST FROM BROKER");
		SearchRequest sr = (SearchRequest) e;
		String lookupID = sr.getId();
		Connection con = DBConnection.createConnection();
		StringBuilder searchResult = new StringBuilder();
		try {
			logger.info("LOOKING UP ID "+lookupID);
			String query = "select * from testp4table where ID = "
					+ "(select PLAINTEXT_ID from p4_mapping where ENCR_ID = '"+lookupID+"')";
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next()) {
				searchResult.append(rs.getString(1));
				searchResult.append(",");
				searchResult.append(rs.getString(2));
				searchResult.append(",");
				searchResult.append(rs.getString(3));
				searchResult.append(",");
				searchResult.append(rs.getString(4));
				searchResult.append(",");
				searchResult.append(rs.getString(5));
				searchResult.append(",");
				searchResult.append(rs.getString(6));
				searchResult.append(",");
			}
			if (searchResult != null && searchResult.length() > 0) {
				searchResult.setLength(searchResult.length() - 1);
			}
			dispatchSearchResponse(searchResult.toString());
			dispatchSearchCompleted();

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
	}
	
}


private void dispatchSearchCompleted() {
	// TODO Auto-generated method stub
	SearchCompleted dfc = new SearchCompleted();
	dfc.setType(MessageTypes.SEARCH_COMPLETE.typeCode());
	dfc.setIpAddress(localIP);
	dfc.setHostName(hostName);
	dfc.setStatus("done");
	
	byte[] dataFetchCompleteMsg = dfc.getBytes();
	TCPSender sender = new TCPSender(socketToBroker,"publisher Node-"+hostName+".out");
	sender.sendBytes(dataFetchCompleteMsg);
	logger.info("PUBLISHER "+localIP+" HAS DISPATCHED SEARCH COMPLETE MESSAGE");
	
}

private void dispatchSearchResponse(String result) {
	// TODO Auto-generated method stub
	SearchResponse sr = new SearchResponse();
	sr.setType(MessageTypes.SEARCH_RESPONSE.typeCode());
	sr.setIpAddress(localIP);
	sr.setEncryptedData(result);
	sr.setHostName(hostName);
	
	byte[] dataFetchResponseMsg = sr.getBytes();
	TCPSender sender = new TCPSender(socketToBroker,"publisher Node-"+hostName+".out");
	sender.sendBytes(dataFetchResponseMsg);
	logger.info("PUBLISHER "+localIP+" HAS DISPATCHED SEARCH RESPONSE");
	
}

private void doTableInserts(ArrayList<String> pubTableMapping) {
	// TODO Auto-generated method stub
	// INSERT THE MAPPING VALUES TO p4_mapping table
	logger.info("INSERTING MAPPING TABLE VALUES");
	if (pubTableMapping != null) {
		Connection con = DBConnection.createConnection();
		try {
			for (int i = 0; i < pubTableMapping.size(); i = i + 2) {
				String alreadypresent = "select * from p4_mapping where PLAINTEXT_ID =" + "'"
						+ pubTableMapping.get(i) + "'";
				Statement s1 = con.createStatement();
				ResultSet res1 = s1.executeQuery(alreadypresent);
				if (res1.equals(null) || res1 == null || !res1.next()) {
					Statement inst = con.createStatement();
					String insert = "INSERT INTO p4_mapping VALUES" + " (" + "'" + pubTableMapping.get(i)
							+ "'," + "'" + pubTableMapping.get(i + 1) + "')";
					inst.executeUpdate(insert);
				} else {
					// System.out.println("PREV RESULT");
					Statement inst = con.createStatement();
					String update = "UPDATE p4_mapping SET ENCR_ID = " + "'" + pubTableMapping.get(i + 1)
							+ "'" + "WHERE PLAINTEXT_ID = " + "'" + pubTableMapping.get(i) + "'";
					inst.executeUpdate(update);
				}
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		logger.info("PUBLISHER MAPPING TABLE INSERT COMPLETE !");
	}
}

private void dispatchDataFetchCompleted() {
	// TODO Auto-generated method stub
	DataFetchCompleted dfc = new DataFetchCompleted();
	dfc.setType(MessageTypes.FETCH_DATA_COMPLETED.typeCode());
	dfc.setIpAddress(localIP);
	dfc.setHostName(hostName);
	dfc.setStatus("done");
	
	byte[] dataFetchCompleteMsg = dfc.getBytes();
	TCPSender sender = new TCPSender(socketToBroker,"publisher Node-"+hostName+".out");
	sender.sendBytes(dataFetchCompleteMsg);
	logger.info("PUBLISHER "+localIP+" HAS DISPATCHED DATA FETCH COMPLETE MESSAGE");
	
}

private void dispatchNormalDataFetchResponse(String toSend) {
	// TODO Auto-generated method stub
	FetchDataResponse fdr = new FetchDataResponse();
	fdr.setType(MessageTypes.FETCH_DATA_RESPONSE.typeCode());
	fdr.setIsEncrypted(0);
	fdr.setIpAddress(localIP);
	fdr.setEncryptedData(toSend);
	fdr.setKeyConverter(publisherKeyConverter);
	fdr.setHostName(hostName);
	
	byte[] dataFetchResponseMsg = fdr.getBytes();
	TCPSender sender = new TCPSender(socketToBroker,"publisher Node-"+hostName+".out");
	sender.sendBytes(dataFetchResponseMsg);
	logger.info("PUBLISHER "+localIP+" HAS DISPATCHED NORMAL DATA FETCH RESPONSE");
	
}

private void dispatchEncryptedDataFetchResponse(String toSend) {
	// TODO Auto-generated method stub
	FetchDataResponse fdr = new FetchDataResponse();
	fdr.setType(MessageTypes.FETCH_DATA_RESPONSE.typeCode());
	fdr.setIsEncrypted(1);
	fdr.setIpAddress(localIP);
	fdr.setEncryptedData(toSend);
	fdr.setKeyConverter(publisherKeyConverter);
	fdr.setHostName(hostName);
	
	byte[] dataFetchResponseMsg = fdr.getBytes();
	TCPSender sender = new TCPSender(socketToBroker,"publisher Node-"+hostName+".out");
	sender.sendBytes(dataFetchResponseMsg);
	logger.info("PUBLISHER "+localIP+" HAS DISPATCHED ENCRYPTED DATA FETCH RESPONSE");
	
}

private void dispatchSetupForwardRequest(String nextPublisherIP, Socket socketToNextPublisher2, String myKey, BigInteger[] encPublisherRandNumWithBrokerPubKey) {
	// TODO Auto-generated method stub
	SetupForwardRequest req = new SetupForwardRequest();
	req.setType(MessageTypes.SETUP_FORWARD_REQUEST.typeCode());
	req.setOrininatingFrom(localIP);
	req.setNodesTraversed(localIP);
	req.setSharedKeyToken(encPublisherRandNumWithBrokerPubKey[0].toString()+"_"+encPublisherRandNumWithBrokerPubKey[1].toString());
	byte[] setupForwardMsg = req.getBytes();
	TCPSender sender = new TCPSender(socketToNextPublisher2,"publisher Node-"+hostName+".out");
	
	sender.sendBytes(setupForwardMsg);
	
	logger.info("PUBLISHER "+localIP+" HAS DISPATCHED SETUP FORWARD REQUEST TO "+nextPublisherIP);
}


public Socket startupNextPublisherLink(String nextPublisherIP, int nextPublisherListeningPort) {
	Socket s = null;
	try {
		
		s = new Socket(nextPublisherIP, nextPublisherListeningPort);
		String key = localIP;
		HostInfo h = new HostInfo();
		h.setIp(nextPublisherIP);
		h.setServerSocketPortNo(nextPublisherListeningPort);
		h.setSock(s);
		h.setSocketPortNo(s.getPort());
		
		/*===========KEEP A LIST OF NEIGHBORS AND THEIR IP:LISTENING_PORT=============*/
		/*===========ADDING TO KNOWNHOSTS MAP===================*/
		
		addToNeighborMap(h, key);
	} catch (UnknownHostException e) {
		logger.severe("Unable to connect to "+ nextPublisherIP+" from "+ hostName+"(UnknownHost):"+e.getMessage());
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		logger.severe("Unable to connect to "+ nextPublisherIP+" from "+ hostName+"(UnknownHost):"+e.getMessage());
		e.printStackTrace();
	}
	return s;
}

//CREATES A RANDOM BIGINT WITHIN THE PROVIDED UPPER LIMIT
	public static BigInteger getRandomBigInteger(BigInteger upperlimit) {
		Random rand = new Random();
		BigInteger randomNBitBigInteger;
		do {
			randomNBitBigInteger = new BigInteger(upperlimit.bitLength(), rand); 
		}while(randomNBitBigInteger.compareTo(upperlimit) >= 0);
		return randomNBitBigInteger;
		
		
	}
	
	
	/**
	 * CREATE THE N BIT RANDOM NUMBER BIGINT
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
	
	public static BigInteger findModInverse(BigInteger number, BigInteger prime) {
		BigInteger modinverse;
		do{
			modinverse = number.modInverse(prime);
		}while(modinverse.compareTo(prime)>=0);
		return modinverse;
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
	 * METHOD FOR HOMOMORPHIC ENCRYPTION
	 * 
	 * @param first encrypted message
	 * @param second encrypted message
	 * @return the product c1 * c1 and c2 * c2
	 */
	public static BigInteger[] goHomomorphic(BigInteger[] first, BigInteger[] second) {
		try {
			BigInteger[] product = new BigInteger[2];
			product[0] = first[0].multiply(second[0]);
			product[1] = first[1].multiply(second[1]);
			return product;
		} catch (ArithmeticException e) {
			throw new RuntimeException("EXECPTION WHILE HOMOMORPHIC MULTIPLICATION");
		}
		
	}
@Override
public void onCommand(String s) {
	// TODO Auto-generated method stub
	
}

public String getBrokerIP() {
	return brokerIP;
}

public void setBrokerIP(String brokerIP) {
	this.brokerIP = brokerIP;
}

public String getBrokerPort() {
	return brokerPort;
}

public void setBrokerPort(String brokerPort) {
	this.brokerPort = brokerPort;
}

public static String getLocalIP() {
	return localIP;
}



public void setLocalIP(String localIP) {
	this.localIP = localIP;
}

public static int getSocketToBrokerPort() {
	return socketToBrokerPort;
}

public void setSocketToBrokerPort(int socketToBrokerPort) {
	this.socketToBrokerPort = socketToBrokerPort;
}
public synchronized Map<String, HostInfo> getKnownNeighborMap() {
	return knownNeighborMap;
}

public synchronized void addToNeighborMap(HostInfo h, String key) {
	knownNeighborMap.put(key,h);
}


public synchronized void setKnownNeighborMap(Map<String, HostInfo> knownNeighborMap) {
	this.knownNeighborMap = knownNeighborMap;
}


}
