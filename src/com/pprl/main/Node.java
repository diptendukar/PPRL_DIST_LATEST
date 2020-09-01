package com.pprl.main;

import java.net.Socket;

import com.pprl.messageformats.Event;



public interface Node {
	
	public void onEvent(Event e, Socket s);
	
	public void onCommand(String s);

}
