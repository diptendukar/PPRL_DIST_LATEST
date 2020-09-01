package com.pprl.messageformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.pprl.util.HostInfo;

public class SetupInitiateRequest implements Event{
	
	private int type;
	private String primeNumber;
	private String generator;
	private String publicKey;
	
	private Map<String,HostInfo> publishersNeighbor;
	
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	
	public String getPrimeNumber() {
		return primeNumber;
	}
	public void setPrimeNumber(String primeNumber) {
		this.primeNumber = primeNumber;
	}
	public String getGenerator() {
		return generator;
	}
	public void setGenerator(String generator) {
		this.generator = generator;
	}
	public Map<String, HostInfo> getPublishersNeighbor() {
		return publishersNeighbor;
	}
	public void setPublishersNeighbor(Map<String, HostInfo> publishersNeighbor) {
		this.publishersNeighbor = publishersNeighbor;
	}
	
	public SetupInitiateRequest() {
		type = MessageTypes.SETUP_INITIATE_REQUEST.typeCode();
	}
	
	/*MARSHALLING*/
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(type);
			
			byte[] primeNumBytes = primeNumber.getBytes();
			int primeLength = primeNumBytes.length;
			dout.writeInt(primeLength);
			dout.write(primeNumBytes);
			
			byte[] generatorBytes = generator.getBytes();
			int generatorLength = generatorBytes.length;
			dout.writeInt(generatorLength);
			dout.write(generatorBytes);
			
			byte[] publicKeyBytes = publicKey.getBytes();
			int publicKeyLength = publicKeyBytes.length;
			dout.writeInt(publicKeyLength);
			dout.write(publicKeyBytes);
			
			// Converting the map to string for sending
			String publisherNeighborString = createStringFromMap(publishersNeighbor);
			
			//System.out.println("STRING TO SEND = "+publisherNeighborString);
			
			byte[] neighborInfo = publisherNeighborString.getBytes();
			int neighborInfoLength = neighborInfo.length;
			dout.writeInt(neighborInfoLength);
			dout.write(neighborInfo);
			
			
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
	
	/*UNMARSHALLING*/
	
	private String createStringFromMap(Map<String, HostInfo> publishersNeighbor2) {
		StringBuilder generatedString =new StringBuilder();
		Iterator iterator = publishersNeighbor2.keySet().iterator();

		while (iterator.hasNext()) {
			String ipport = iterator.next().toString();
			generatedString.append(ipport);
			generatedString.append("_");
			HostInfo value = publishersNeighbor.get(ipport);
			generatedString.append(value.getIp());
			generatedString.append(":");
			generatedString.append(value.getServerSocketPortNo());
			generatedString.append(",");
		}
		
		String outputString = generatedString.toString();
		return outputString.substring(0, outputString.length() - 1);
		
	}
	public SetupInitiateRequest(byte[] msgBytes) {
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msgBytes);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			
			int lengthPrime = din.readInt();
			byte[] bytesPrime = new byte[lengthPrime];
			din.readFully(bytesPrime);
			primeNumber = new String(bytesPrime);
			
			int lengthGenerator = din.readInt();
			byte[] bytesGenerator = new byte[lengthGenerator];
			din.readFully(bytesGenerator);
			generator = new String(bytesGenerator);
			
			int lengthPublicKey = din.readInt();
			byte[] bytesPublicKey = new byte[lengthPublicKey];
			din.readFully(bytesPublicKey);
			publicKey = new String(bytesPublicKey);
			
			// converting the string back to MAP
			int lengthNeighborInfo = din.readInt();
			byte[] bytesNeighborInfo = new byte[lengthNeighborInfo];
			din.readFully(bytesNeighborInfo);
			String neighborInfoString = new String(bytesNeighborInfo);
			publishersNeighbor = createMapfromString(neighborInfoString);
			
			
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private Map<String, HostInfo> createMapfromString(String neighborInfoString) {
		// TODO Auto-generated method stub
		Map<String, HostInfo> neighbors = new HashMap<String,HostInfo>();
		String[] eachMapping = neighborInfoString.split(",");
		for (int i=0;i<eachMapping.length;i++) {
			
			String[] mappingElements = eachMapping[i].split("_");
			HostInfo host = new HostInfo();
			host.setIp(mappingElements[1].split(":")[0]);
			host.setServerSocketPortNo(Integer.parseInt(mappingElements[1].split(":")[1]));
			neighbors.put(mappingElements[0], host);
		}
		
		return neighbors;
	}
	

}
