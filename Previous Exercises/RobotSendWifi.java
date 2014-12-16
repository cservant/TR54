import java.io.IOException;

import lejos.hardware.Button;
import lejos.utility.Delay;


public class RobotSendWifi extends Robot{
	
	final private WifiCommunication wifi = new WifiCommunication(); 
	
	public void navigation() {
		
		showMsg("On the");
		showMsg(" highway to Hell");
		//wait for touchsensor to be pressed before starting
		while(!capteur.isPressed());
		int i = 0 ;
		showMsg("Sending msgs");
		while((Button.readButtons() == 0) )
		{
			try {
				wifi.sendUDP("192.168.43.1", String.valueOf(i++) + " coucou");
				showMsg("Msg sent");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Delay.msDelay(3000);
		}
	}
}
