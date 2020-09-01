package com.pprl.messageformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class SetupForwardRequest implements Event{

	private int type;
	private String originatingFrom;
	private String nodesTraversed;
	private String sharedKeyToken;
	
	
	
	public String getOrininatingFrom() {
		return originatingFrom;
	}
	public void setOrininatingFrom(String originatingFrom) {
		this.originatingFrom = originatingFrom;
	}
	public String getNodesTraversed() {
		return nodesTraversed;
	}
	public void setNodesTraversed(String nodesTraversed) {
		this.nodesTraversed = nodesTraversed;
	}
	public String getSharedKeyToken() {
		return sharedKeyToken;
	}
	public void setSharedKeyToken(String sharedKeyToken) {
		this.sharedKeyToken = sharedKeyToken;
	}
	public void setType(int type) {
		this.type = type;
	}
	public SetupForwardRequest() {
		type = MessageTypes.SETUP_FORWARD_REQUEST.typeCode();
	}
	public SetupForwardRequest(byte[] msgBytes) {
		// TODO Auto-generated constructor stub
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msgBytes);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			
			int lengthOrigin = din.readInt();
			byte[] bytesOrigin = new byte[lengthOrigin];
			din.readFully(bytesOrigin);
			originatingFrom = new String(bytesOrigin);
			
			int lengthNodesTraversed = din.readInt();
			byte[] bytesNodesTraversed = new byte[lengthNodesTraversed];
			din.readFully(bytesNodesTraversed);
			nodesTraversed = new String(bytesNodesTraversed);
			
			int lengthToken = din.readInt();
			byte[] bytesToken = new byte[lengthToken];
			din.readFully(bytesToken);
			sharedKeyToken = new String(bytesToken);
			
			
			
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(type);
			
			byte[] originatingFromBytes = originatingFrom.getBytes();
			int originatingFromLength = originatingFromBytes.length;
			dout.writeInt(originatingFromLength);
			dout.write(originatingFromBytes);
			
			byte[] nodesTraversedBytes = nodesTraversed.getBytes();
			int nodesTraversedLength = nodesTraversedBytes.length;
			dout.writeInt(nodesTraversedLength);
			dout.write(nodesTraversedBytes);
			
			byte[] sharedKeyTokenBytes = sharedKeyToken.getBytes();
			int sharedKeyTokenLength = sharedKeyTokenBytes.length;
			dout.writeInt(sharedKeyTokenLength);
			dout.write(sharedKeyTokenBytes);
			
			
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

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	

}
