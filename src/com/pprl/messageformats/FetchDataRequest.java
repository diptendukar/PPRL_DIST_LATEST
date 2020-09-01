package com.pprl.messageformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FetchDataRequest implements Event{

	private int msgType;
	
	// 0 for normal results 1 for encrypted results 
	
	private int encryptedResults;
	private int numberofRows;
	
	
	public FetchDataRequest(byte[] msgBytes) {
		// TODO Auto-generated constructor stub
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msgBytes);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			msgType = din.readInt();
			encryptedResults = din.readInt();
			numberofRows = din.readInt();
			
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FetchDataRequest() {
		msgType = MessageTypes.FETCH_DATA_REQUEST.typeCode();
	}
	@Override
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(msgType);
			dout.writeInt(encryptedResults);
			dout.writeInt(numberofRows);
			
			dout.flush();
			marshalledBytes = baOutputStream.toByteArray();
			baOutputStream.close();
			dout.close();
			return marshalledBytes;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public int getEncryptedResults() {
		return encryptedResults;
	}

	public void setEncryptedResults(int encryptedResults) {
		this.encryptedResults = encryptedResults;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumberofRows() {
		return numberofRows;
	}

	public void setNumberofRows(int numberofRows) {
		this.numberofRows = numberofRows;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

}
