package com.pprl.messageformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FetchDataResponse implements Event{
	
	private int type;
	private int isEncrypted;
	private String ipAddress;
	private String encryptedData;
	private String keyConverter;
	private String hostName;

	public FetchDataResponse(byte[] msgBytes) {
		// TODO Auto-generated constructor stub
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msgBytes);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			
			isEncrypted = din.readInt();
			
			int ipLength = din.readInt();
			byte[] tmpByteArr = new byte[ipLength];
			din.readFully(tmpByteArr);
			ipAddress = new String(tmpByteArr);
			
			int lengthEncData = din.readInt();
			byte[] bytesEncData = new byte[lengthEncData];
			din.readFully(bytesEncData);
			encryptedData = new String(bytesEncData);
			
			int lengthKeyConverter = din.readInt();
			byte[] bytesKeyConverter = new byte[lengthKeyConverter];
			din.readFully(bytesKeyConverter);
			keyConverter = new String(bytesKeyConverter);
			
			int lengthHostName = din.readInt();
			byte[] bytesHostName = new byte[lengthHostName];
			din.readFully(bytesHostName);
			hostName = new String(bytesHostName);
			
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

	public String getEncryptedData() {
		return encryptedData;
	}

	public void setEncryptedData(String encryptedData) {
		this.encryptedData = encryptedData;
	}

	public String getKeyConverter() {
		return keyConverter;
	}

	public void setKeyConverter(String keyConverter) {
		this.keyConverter = keyConverter;
	}

	public void setType(int type) {
		this.type = type;
	}

	public FetchDataResponse() {
		type = MessageTypes.FETCH_DATA_RESPONSE.typeCode();
	}
	@Override
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(type);
			
			dout.writeInt(isEncrypted);
			
			byte[] ipBytes = ipAddress.getBytes();
			int elementLength = ipBytes.length;
			dout.writeInt(elementLength);
			dout.write(ipBytes);
			
			byte[] encDataBytes = encryptedData.getBytes();
			int encDataLength = encDataBytes.length;
			dout.writeInt(encDataLength);
			dout.write(encDataBytes);
			
			byte[] keyConverterBytes = keyConverter.getBytes();
			int keyConverterLength = keyConverterBytes.length;
			dout.writeInt(keyConverterLength);
			dout.write(keyConverterBytes);
			
			byte[] hostNameBytes = hostName.getBytes();
			int hostNameLength = hostNameBytes.length;
			dout.writeInt(hostNameLength);
			dout.write(hostNameBytes);
			
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

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getIsEncrypted() {
		return isEncrypted;
	}

	public void setIsEncrypted(int isEncrypted) {
		this.isEncrypted = isEncrypted;
	}

}
