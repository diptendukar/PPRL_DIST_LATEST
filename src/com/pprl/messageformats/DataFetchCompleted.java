package com.pprl.messageformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataFetchCompleted implements Event{
	
	private int type;
	private String ipAddress;
	private String hostName;
	private String status;

	public DataFetchCompleted(byte[] msgBytes) {
		// TODO Auto-generated constructor stub
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msgBytes);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			
			int ipLength = din.readInt();
			byte[] tmpByteArr = new byte[ipLength];
			din.readFully(tmpByteArr);
			ipAddress = new String(tmpByteArr);
			
			int lengthHostName = din.readInt();
			byte[] bytesHostName = new byte[lengthHostName];
			din.readFully(bytesHostName);
			hostName = new String(bytesHostName);
			
			int lengthStatus = din.readInt();
			byte[] bytesStatus = new byte[lengthStatus];
			din.readFully(bytesStatus);
			status = new String(bytesStatus);
			
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}


	public void setType(int type) {
		this.type = type;
	}

	public DataFetchCompleted() {
		type = MessageTypes.FETCH_DATA_COMPLETED.typeCode();
	}
	@Override
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(type);
			
			byte[] ipBytes = ipAddress.getBytes();
			int elementLength = ipBytes.length;
			dout.writeInt(elementLength);
			dout.write(ipBytes);
			
			byte[] hostNameBytes = hostName.getBytes();
			int hostNameLength = hostNameBytes.length;
			dout.writeInt(hostNameLength);
			dout.write(hostNameBytes);
			
			byte[] statusBytes = status.getBytes();
			int statusLength = statusBytes.length;
			dout.writeInt(statusLength);
			dout.write(statusBytes);
			
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

}
