package com.pprl.messageformats;

public enum MessageTypes {
	REGISTRATION_REQUEST(1),
	REGISTRATION_RESPONSE(2),
	DEREGISTER_REQUEST(3),
	DEREGISTER_RESPONSE(4),
	SETUP_INITIATE_REQUEST(5),
	SETUP_INITIATE_RESPONSE(6),
	/* Telling nodes that overlay has been created and message passing can start */
	SETUP_FORWARD_REQUEST(7),
	/* Each node telling the registry that it is done passing its message */
	SETUP_COMPLETED(8),
	/* On receiving TASK_COMPLETE, registry sends this to all registered messaging nodes */
	FETCH_DATA_REQUEST(9),
	/* Each node to the registry */
	FETCH_DATA_RESPONSE(10),
	FETCH_DATA_COMPLETED(11),
	SEARCH_REQUEST(12),
	SEARCH_RESPONSE(13),
	SEARCH_COMPLETE(14)
	;
	private int type;
	
	MessageTypes(int type) {
		this.type = type;
	}
	
	public int typeCode() {
		return type;
	}
	
	public static void main(String arg[]) {
		System.out.println(MessageTypes.DEREGISTER_REQUEST.type);
	}

}
