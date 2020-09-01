package com.pprl.messageformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SearchRequest implements Event{
	
	private int type;
	private String id;

	public SearchRequest(byte[] msgBytes) {
		// TODO Auto-generated constructor stub
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msgBytes);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			
			int idLength = din.readInt();
			byte[] tmpByteArr = new byte[idLength];
			din.readFully(tmpByteArr);
			setId(new String(tmpByteArr));
			
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	


	public void setType(int type) {
		this.type = type;
	}

	public SearchRequest() {
		type = MessageTypes.SEARCH_REQUEST.typeCode();
	}
	@Override
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(type);
			
			byte[] idBytes = id.getBytes();
			int elementLength = idBytes.length;
			dout.writeInt(elementLength);
			dout.write(idBytes);
			
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




	public String getId() {
		return id;
	}




	public void setId(String id) {
		this.id = id;
	}

	

}
