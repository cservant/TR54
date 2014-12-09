import java.io.IOException;

import lejos.hardware.Button;
import lejos.robotics.Color;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.utility.Delay;


public class iRobot extends Robot{
	private long timer = 0;
	private long delaytimer = 0;
	private boolean isBigBlue = true; //this boolean is used only for the road we have in practical work to detect the main roads
	private boolean stop = false; //this boolean is used only for the road we have in practical work to stop the robot at the crossroad
	private Point point = new Point(0,0); //this point will be updated as the entry point of the main roads
	final private WifiCommunication wifi = new WifiCommunication();
	private boolean isAuthorized = false; //boolean used in the wifipooler to check the current authorization of our robot
	private boolean hasRequested = false; //boolean used to delay the wifi request
	private int nRoad = 0; //id of the road
	private String id = "192.168.43.129"; //Robot Ip address
	private static final String SERVER_IP = "192.168.43.1"; 
	
	public void navigation() {
		OdometryPoseProvider opp = new OdometryPoseProvider(df);
		Thread wp = new Thread(new WifiPooler());
		
		
		showMsg("On the");
		showMsg("Highway to Hell");
		//wait for touchsensor to be pressed before starting
		while(!capteur.isPressed());
		
		//start the wifi pooler
		wp.start();
		//initiate the timer
		timer = System.nanoTime();
		//while all buttons are released
		while (Button.readButtons() == 0) {
			delaytimer = System.nanoTime();
			//management of close range detection
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
					if(isBigBlue)
						nRoad = (nRoad1+) % 2;
					point = opp.getPose().getLocation();
					//showMsg("isBigB : " + isBigBlue);
				}
				
				if(!isBigBlue)
					stop = false;
				
				df.forward();
				//if we are on the entry point of the main road (this road leads to a crossroad)	
				if(isBigBlue && !stop){
					showMsg("Road " + nRoad);
					//send request for the crossroad
					// "R" : request to enter the crossroad
					// "O" : notification robot leaved the crossroad
					if(!hasRequested){
						try {
							//we send an UDP packet to our android device (hotspot wifi ip address is always the same)
							wifi.sendUDP(SERVER_IP, "R");
							showMsg("Request R"+nRoad);
							hasRequested = true;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							showMsg("Err : sendUDP");
							e.printStackTrace();
						}
					}
					
					//showMsg("dist : " + opp.getPose().distanceTo(point));
					//check if we are close to the crossroad
					if(opp.getPose().distanceTo(point) >= 20)
					{
						showMsg("Waiting for Auth");
						//check isAuthorized to know if we received authorization from the server
						while(!isAuthorized){
							df.stop();
						}
						showMsg("Crossing...");
						stop = true;
					}
					df.forward();
				}
				
				//notify the server we get out of the crossroad
				if(opp.getPose().distanceTo(point) >= 70){
					try{
						wifi.sendUDP(SERVER_IP, "O");
						showMsg("Out");
						hasRequested = false;
					} catch (IOException e){
						showMsg("err : SendUDP");
						e.printStackTrace();
					}
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
				//showMsg("Color : XXX");
				df.forward();
				break;				
			}
		}
	}
	


	class WifiPooler implements Runnable {

		@Override
		public void run() {
			showMsg("WifiPooler on");
			// TODO Auto-generated method stub
			try {
				while(true){
					//wait for a string formated as "IP;IP2;;..." with IP the ip address of all robot authorized to cross the crossroad
					String tmp = wifi.receiveUDP(); //!! blocking method !!
					//only get the first chars because we received a byte[256]
					tmp = tmp.substring(0, tmp.indexOf("\0"));
					String[] str = tmp.split(";");//split by IP addresses
					
					//check if our ip address is contained in the packet we received
					int i =0;
					while( (i < str.length) && !str[i].contains(id))
						i++;
					
					//if we didn't find our ip address
					if(i >= str.length){
						showMsg("Auth Denied");
						isAuthorized = false;
						continue;
					}
					
					//otherwise we found our ip address
					showMsg("received Auth");
					isAuthorized = true;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				showMsg("Err : receiveUDP");
				e.printStackTrace();
			}
		}

	}
}
