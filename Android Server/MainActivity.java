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

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView info, infoip, msg, broadcast;
	private ServerSocket serverSocket;
	private String message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		info = (TextView) findViewById(R.id.info);
		infoip = (TextView) findViewById(R.id.infoip);
		msg = (TextView) findViewById(R.id.msg);
		broadcast = (TextView) findViewById(R.id.broadcast);

		infoip.setText(getIpAddress());

		Thread broadcast = new Thread(new SendBroadcast());
		broadcast.start();
//		Thread socketServerThread = new Thread(new SocketServerThread());
//		socketServerThread.start();
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

		static final int SocketServerPORT = 6789;
		int count = 0;

		@Override
		public void run() {
			try {
				
				serverSocket = new ServerSocket(SocketServerPORT);
				MainActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						info.setText("I'm waiting here: "
								+ serverSocket.getLocalPort());
					}
				});

				while (true) {
					count++;
					
					DatagramSocket datagramSocketReceive = new DatagramSocket(6789);
					byte[] buffer = new byte[10];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

					datagramSocketReceive.receive(packet);
					buffer = packet.getData();
					
					message = new String(buffer,"UTF-8");


					MainActivity.this.runOnUiThread(new Runnable() { 

						@Override
						public void run() {
							msg.setText(message);
						}
					});
					
					datagramSocketReceive.close();

					
					
//					SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
//					socket, count);
//					socketServerReplyThread.run();
					
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
		 
		private int count = 0;
		
		@Override
		public void run() {

			InetAddress receiverAddress;
			DatagramSocket datagramSocketSend = null;
			byte[] buffer;
			String msg = null;
			
			while (true) {
				count++;
			
				try {
					msg = "Test";
					buffer = msg.getBytes(Charset.forName("UTF-8"));
					receiverAddress = InetAddress.getByName("192.168.43.255"); // Marche pas avec .255
					datagramSocketSend = new DatagramSocket();
					datagramSocketSend.setBroadcast(true);
					DatagramPacket packetSend = new DatagramPacket( buffer, buffer.length, receiverAddress, 6789);
					datagramSocketSend.send(packetSend);

					// Update UI in the main thread
					runOnUiThread(new Runnable() {
					     @Override
					     public void run() {
					    	 broadcast.setText("Broadcast n°"+count+" send.");
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