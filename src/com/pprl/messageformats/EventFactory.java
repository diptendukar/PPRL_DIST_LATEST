package com.pprl.messageformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import com.pprl.main.Node;



public class EventFactory {
	
	private static EventFactory eventFactory;
	
	public static synchronized EventFactory getInstance() {
		if(eventFactory == null) {
			eventFactory = new EventFactory();
		}
		return eventFactory;
	}
	
	/* react to incoming message */
	
	public void react(byte[] msg, Node callingNode, Socket sock) {
		try {
			
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msg);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			/* Getting the message type */
			int msgTypeInt = din.readInt();
			
			baInputStream.close();
			din.close();
			
			//System.out.println("======================MESSAGE TYPE IS:" +msgTypeInt+"===========================");
			/*============CASE OF REGISTRY RECEIVING A REGISTER REQUEST==================*/
			if(msgTypeInt == MessageTypes.REGISTRATION_REQUEST.typeCode()) {
				RegistrationRequestMessage resgisterRequest = new RegistrationRequestMessage(msg);
				callingNode.onEvent(resgisterRequest,sock);
			} else if(msgTypeInt == MessageTypes.REGISTRATION_RESPONSE.typeCode()) {
				RegisterResponse rsp = new RegisterResponse(msg);
				callingNode.onEvent(rsp, sock);
			} else if(msgTypeInt == MessageTypes.DEREGISTER_REQUEST.typeCode()) {
				DeregisterRequest req = new DeregisterRequest(msg);
				callingNode.onEvent(req, sock);
			} else if(msgTypeInt == MessageTypes.DEREGISTER_RESPONSE.typeCode()) {
				DeregisterResponse rsp = new DeregisterResponse(msg);
				callingNode.onEvent(rsp, sock);
			} else if(msgTypeInt == MessageTypes.SETUP_INITIATE_REQUEST.typeCode()) {
				SetupInitiateRequest req = new SetupInitiateRequest(msg);
				callingNode.onEvent(req, sock);
			}else if(msgTypeInt == MessageTypes.SETUP_FORWARD_REQUEST.typeCode()) {
				SetupForwardRequest req = new SetupForwardRequest(msg);
				callingNode.onEvent(req, sock);
			}  else if(msgTypeInt == MessageTypes.SETUP_COMPLETED.typeCode()) {
				SetupCompletedResponse req = new SetupCompletedResponse(msg);
				callingNode.onEvent(req, sock);
			} else if(msgTypeInt == MessageTypes.FETCH_DATA_REQUEST.typeCode()) {
				FetchDataRequest req = new FetchDataRequest(msg);
				callingNode.onEvent(req, sock);
			} else if(msgTypeInt == MessageTypes.FETCH_DATA_RESPONSE.typeCode()) {
				FetchDataResponse req = new FetchDataResponse(msg);
				callingNode.onEvent(req, sock);
			} else if(msgTypeInt == MessageTypes.FETCH_DATA_COMPLETED.typeCode()) {
				DataFetchCompleted req = new DataFetchCompleted(msg);
				callingNode.onEvent(req, sock);
			} else if(msgTypeInt == MessageTypes.SEARCH_REQUEST.typeCode()) {
				SearchRequest nr = new SearchRequest(msg);
				callingNode.onEvent(nr, sock);
			} else if(msgTypeInt == MessageTypes.SEARCH_RESPONSE.typeCode()) {
				SearchResponse nr = new SearchResponse(msg);
				callingNode.onEvent(nr, sock);
			} 
			else if(msgTypeInt == MessageTypes.SEARCH_COMPLETE.typeCode()) {
				SearchCompleted nr = new SearchCompleted(msg);
				callingNode.onEvent(nr, sock);
			} 
	
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void reactToInputs(Node callingNode, String cmd) {
		if("2".equalsIgnoreCase(cmd)) {
			
			//System.out.println("EF");
			DeregisterRequest d = new DeregisterRequest();
			callingNode.onEvent(d, null);
		} 
		else {
			callingNode.onCommand(cmd);
		}
	}
	
	public void handleRegisterRequest(DataInputStream in) {
		
	}
	
	

}
