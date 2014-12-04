import java.io.IOException;

import lejos.hardware.Button;
import lejos.utility.Delay;


public class RobotReceiveWifi extends Robot{
	
	final private WifiCommunication wifi = new WifiCommunication(); 
	private byte[] msg = new byte[10];
	public void navigation() {
		
		showMsg("On the");
		showMsg(" highway to Hell");
		//wait for touchsensor to be pressed before starting
		while(!capteur.isPressed());
		int i = 0 ;
		showMsg("Receiving msgs");
		while((Button.readButtons() == 0) )
		{
			try {
				showMsg(wifi.receiveUDP());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
