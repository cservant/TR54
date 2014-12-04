import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;



public class WifiCommunication {
	private int port;
	
	public WifiCommunication(){
		this.port = 6789;
	}
	
	public WifiCommunication(int port){
		this.port = port;
	}
	/**
	 * Send socket with UDP protocol
	 * @throws IOException
	 */
	public void sendUDP(String ip, String data) throws IOException{
		
		DatagramSocket datagramSocket = new DatagramSocket();
		byte[] buffer = data.getBytes();
		InetAddress receiverAddress = InetAddress.getByName(ip);
		DatagramPacket packet = new DatagramPacket( buffer, buffer.length, receiverAddress, this.port);
		
		datagramSocket.send(packet);
	}
	
	/**
	 * Receive socket with UDP protocol
	 * @throws IOException
	 */
	public String receiveUDP() throws IOException{
		
		DatagramSocket datagramSocket = new DatagramSocket(this.port);
		byte[] buffer = new byte[10];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		datagramSocket.receive(packet);
		datagramSocket.close();
		return new String(packet.getData(), "UTF-8");
		
		//return packet.getData();
	}
}
