import java.io.IOException;

import lejos.hardware.Button;
import lejos.robotics.Color;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.utility.Delay;


public class iRobot extends Robot{
	private long timer = 0;
	private long delaytimer = 0;
	private boolean isBigBlue = true;
	private boolean stop = false;
	private Point point = new Point(0,0);
	final private WifiCommunication wifi = new WifiCommunication();
	private boolean isAuthorized = false;
	private String id = "192.168.43.129";
	
	public void navigation() {
		OdometryPoseProvider opp = new OdometryPoseProvider(df);
		Thread wp = new Thread(new WifiPooler());
		
		
		showMsg("On the");
		showMsg("highway to Hell");
		//wait for touchsensor to be pressed before starting
		while(!capteur.isPressed());
		
		//start the wifi pooler
		wp.start();
		//initiate the timer
		timer = System.nanoTime();
		//while all buttons are released
		while (Button.readButtons() == 0) {
			delaytimer = System.nanoTime();
			//management of low range collisions
			while(capteur.getDistance() <= 15)
				df.stop();
			delaytimer = System.nanoTime() - delaytimer;
			
			//depending on the color we detect
			switch(capteur.colorSensor.getColorID()){
			case Color.BLACK :
				//showMsg("Color : BLACK");
				//movement on the left (the left wheel has 50% of the speed of the right wheel)
				df.steer(50);
				break;
			case Color.BLUE :
				//showMsg("Color : BLUE");
				//forward
				//if last time we saw blue was 2 sec ago or more
				if(((System.nanoTime() - timer - delaytimer)) >= 2*1e9)
				{
					isBigBlue = !isBigBlue;
					point = opp.getPose().getLocation();
					//showMsg("isBigB : " + isBigBlue);
				}
				
				if(!isBigBlue)
					stop = false;
				
				df.forward();
								
				if(isBigBlue && !stop){
					
					//send request for the shared zone
					// "R" : request to enter the shared zone
					// "O" : notification robot leaved the shared zone
					try {
						wifi.sendUDP("192.168.43.1", "R");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						showMsg("Err : sendUDP");
					}
					
					//receive answer from server
					//receive list of authorized robot
					//check if this robot is contained in this list
					
					
					//showMsg("dist : " + opp.getPose().distanceTo(point));
					if(opp.getPose().distanceTo(point) >= 40)
					{
						//check isAuthorized to know if we received authorization from the server
						while(!isAuthorized){
							showMsg("Waiting for Auth");
							df.stop();
						}
							

						stop = true;
					}
					df.forward();
				}
				
				timer = System.nanoTime();
				break;
			case Color.WHITE :
				//showMsg("Color : WHITE");
				//movement on the right (the right wheel has a speed 50% of the left wheel)
				df.steer(-50);
				break;
			default :
				//default case
				showMsg("Color : XXX");
				df.forward();
				break;				
			}
		}
	}
	


	class WifiPooler implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				while(true){
					//wait for a string formated as "IP:AUTH;IP2;AUTH2;..." with IP the ip address of a robot and AUTH its authorization state {'A', 'R'}
					showMsg("WifiPooler on");
					String[] str = wifi.receiveUDP().split(";");
					showMsg("WP : received UDP");
					int i =0;
					//get the ip address from the string which is the first part of the string
					while(str[i].split(":")[0].compareTo(id) == 0)
						i++;
					
					showMsg(str[i]);
					
					if(str[i].split(":")[1].compareTo("A") == 0){
						//we are authorized to go through the shared zone
						showMsg("received Auth");
						isAuthorized = true;
					}
					else{
						//server refused to give us authorization
						showMsg("refused Auth");
						isAuthorized = false;
					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				showMsg("Err : receiveUDP");
			}
		}

	}
}
