package fr.utbm.tr54.app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView info, infoip, msg;
	private ServerSocket serverSocket;
	private Map<String, String> robots; 	// Key : IP, Value : A:authorized, D:denied
	private LinkedList<String> waitingList;	// List of robots who are waiting
	private static final int SOCKET_SERVER_PORT = 6789;
	private static final String BROADCAST_ADDRESS = "192.168.43.255";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/* Set the view */
		info = (TextView) findViewById(R.id.info);
		infoip = (TextView) findViewById(R.id.infoip);
		msg = (TextView) findViewById(R.id.msg);

		infoip.setText(getIpAddress());

		/* Initialize variables */
		robots = new HashMap<>();
		waitingList = new LinkedList<>();
		
		/* Launch threads */
		Thread socketServerThread = new Thread(new SocketServerThread());
		socketServerThread.start();
		
		Thread broadcast = new Thread(new SendBroadcast());
		broadcast.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Receive a message. 
	 */
	private class SocketServerThread extends Thread {

		private String message;	// Message which is received
		private char voie; 		// Number of the authorized way (0 or 1)

		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(SOCKET_SERVER_PORT);
				
				// Update the UI
				MainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						info.setText("I'm waiting here: "
								+ serverSocket.getLocalPort());
					}
				});

				while (true) {
					DatagramSocket datagramSocketReceive = new DatagramSocket(SOCKET_SERVER_PORT);
					byte[] buffer = new byte[2];
					final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					
					datagramSocketReceive.receive(packet);
					
					String address = packet.getAddress().toString();
					String[] ipAddress = address.split("\\.");
					
					if(!ipAddress[3].equals("1")){ // Avoid reception of server's broadcasts. Server IP:192.168.43.1
						
						buffer = packet.getData();
						message = new String(buffer,"UTF-8");
						
						// Update UI
						MainActivity.this.runOnUiThread(new Runnable() { 
							@Override
							public void run() {
								msg.setText(msg.getText()+"\n\t"+message+" "+ packet.getAddress().toString() + " received");
							}
						});
						
						// Prevent concurrent access
						synchronized (robots) {
							// If the robot is known
							if(!robots.containsKey(packet.getAddress())){
								robots.put(packet.getAddress().toString(),"D");
							}
							
							// Test the request type : Request the way / Out of the way
							if(message.contains("R") ){
								char current = message.charAt(1);
								// If the crossing is busy
								if(robots.containsValue("A")){
									// In the same way
									if(current == voie){
										robots.put(packet.getAddress().toString(),"A");
									}
									else{
										waitingList.add(packet.getAddress().toString());
									}
								}
								else{
									voie = current;
									robots.put(packet.getAddress().toString(),"A");
						        }
							}
							else{
								robots.put(packet.getAddress().toString(),"D");
								// If anyone is authorized
								if(!robots.containsValue("A")){
									if(!waitingList.isEmpty()){
										if(voie == '0')
											voie = '1';
										else
											voie = '0';
										
										// If robots are waiting, authorize the first
										while(!waitingList.isEmpty()){
											robots.put(waitingList.removeFirst(),"A");
										}
									}
								}
							}
						}
					}
					
					datagramSocketReceive.close();
				}
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}

	}

	/**
	 * Send a broadcast message.
	 * @author Alexis
	 *
	 */
	private class SendBroadcast extends Thread {
		 
		private String message;
		
		@Override
		public void run() {

			InetAddress receiverAddress;
			DatagramSocket datagramSocketSend = null;
			byte[] buffer;
			
			while (true) {
				// Prevent concurrent access
				synchronized (robots) {
					// If a robot is crossing a way
					if(robots.containsValue("A")){
						message="";
						for ( String key : robots.keySet() ) {
							if(robots.get(key).equals("A")){
								message+= key+";";
							}
						}
					}
					else{
						// No one is crossing the way
						message ="0.0.0.0"; 
					}
					// Add an end-sequence character
					message = message+"\0";
				}
				
				try {
					buffer = message.getBytes(Charset.forName("UTF-8"));
					receiverAddress = InetAddress.getByName(BROADCAST_ADDRESS);
					datagramSocketSend = new DatagramSocket();
					datagramSocketSend.setBroadcast(true);
					DatagramPacket packetSend = new DatagramPacket( buffer, buffer.length, receiverAddress, SOCKET_SERVER_PORT);
					datagramSocketSend.send(packetSend);

					// Update UI in the main thread
					runOnUiThread(new Runnable() {
					     @Override
					     public void run() {
					    	 msg.setText(msg.getText()+"\nBroadcast " +message+" send.");
					    }
					});
					
					// Wait 1 sec
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally{
					// Close the socket
					if(datagramSocketSend != null)
						datagramSocketSend.close();
				}
			}
		}
	}

	/**
	 * Get the local ip address
	 * @return
	 */
	private String getIpAddress() {
		String ip = "";
		try {
			Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
					.getNetworkInterfaces();
			while (enumNetworkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = enumNetworkInterfaces
						.nextElement();
				Enumeration<InetAddress> enumInetAddress = networkInterface
						.getInetAddresses();
				while (enumInetAddress.hasMoreElements()) {
					InetAddress inetAddress = enumInetAddress.nextElement();

					if (inetAddress.isSiteLocalAddress()) {
						ip += "SiteLocalAddress: "
								+ inetAddress.getHostAddress() + "\n";
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
			ip += "Something Wrong! " + e.toString() + "\n";
		}
		return ip;
	}
}