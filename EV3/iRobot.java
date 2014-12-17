import java.io.IOException;

import lejos.hardware.Button;
import lejos.robotics.Color;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.utility.Delay;

/**
 * iRobot class which extends from the Robot class.
 * @author 	Alexis HESTIN
 * 			Clément SERVANT
 * 			Stéphane DORRE
 * 			Julien VOISIN 
 */
public class iRobot extends Robot{
	
	/* Timer */
	private long timer = 0;
	private long delaytimer = 0;
	
	/* Booleans */
	private boolean isBigBlue = true; //this boolean is used only for the road we have in practical work to detect the main roads
	private boolean stop = false; //this boolean is used only for the road we have in practical work to stop the robot at the crossroad
	private boolean isAuthorized = false; //boolean used in the wifipooler to check the current authorization of our robot
	private boolean hasRequested = false; //boolean used to delay the wifi request
	
	private int nRoad = 0; //id of the road
	private Point point = new Point(0,0); //this point will be updated as the entry point of the main roads
	private final WifiCommunication wifi = new WifiCommunication(); //object to manage the wifi communication with the android server
	
	/* Constants */
	private static final String id = "192.168.43.129"; //Robot IP address defined by the automatic DHCP (seen on the embedded screen of the robot)
	private static final String SERVER_IP = "192.168.43.1"; //Server Ip address
	private static final int TRAVEL_SPEED = 15;
	private static final int STEER_SPEED = 10;
	
	/**
	 * Navigation method which is run by the main.
	 */
	public void navigation() {
		OdometryPoseProvider opp = new OdometryPoseProvider(df);
		Thread wp = new Thread(new WifiPooler());
		
		//start message
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
			while(capteur.getDistance() <= 15){
				df.stop();
			}
			//save waiting time before moving again
			delaytimer = System.nanoTime() - delaytimer;
			
			//depending on the color we detect
			switch(capteur.colorSensor.getColorID()){
				case Color.BLACK :
					//movement on the left (the left wheel has 50% of the speed of the right wheel)
					df.steer(50);
					break;
				case Color.BLUE :
					//forward
					//if last time we saw blue was 2 sec ago or more
					if(((System.nanoTime() - timer - delaytimer)) >= 2*1e9)
					{
						isBigBlue = !isBigBlue;
						if(isBigBlue){
							//change the road index as we just changed of road
							// index is 0 or 1 in our case
							nRoad = (nRoad+1) % 2;
						}
						point = opp.getPose().getLocation();
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
								showMsg("Request R"+nRoad); //display on the embedded screen the index of the road we are on
								hasRequested = true;
								//as we will take a bend then decrease the speed to minimize color detection faults
								df.setTravelSpeed(TRAVEL_SPEED);
							} catch (IOException e) {
								showMsg("Err : sendUDP");
								e.printStackTrace();
							}
						}
						
						//check if we are close to the crossroad
						if(opp.getPose().distanceTo(point) >= 20){
							showMsg("Waiting for Auth");
							//check isAuthorized to know if we received an authorization from the server
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
							df.setTravelSpeed(STEER_SPEED);
						} catch (IOException e){
							showMsg("err : SendUDP");
							e.printStackTrace();
						}
					}
					//update the timer of main road detection
					timer = System.nanoTime();
					break;
				case Color.WHITE :
					//movement on the right (the right wheel has a speed 50% of the left wheel)
					df.steer(-50);
					break;
				default :
					//default case
					df.forward();
					break;				
			}
		}
	}
	

	/**
	 * WifiPooler class : run as a new thread, manages the pooling of authorization from the android server by the wifi communication.
	 * The authorization is shared between the two threads by the boolean isAuthorized.
	 * This boolean is set by this class and read by the navigation method in the main thread.
	 * Uses a blocking method to receive UDP packets.
	 * @author 	Alexis HESTIN
	 * 			Clément SERVANT
	 * 			Stéphane DORRE
	 * 			Julien VOISIN 
	 *
	 */
	class WifiPooler implements Runnable {

		@Override
		public void run() {
			showMsg("WifiPooler on");
			try {
				while(true){
					//wait for a string formated as "IP;IP2;;..." with IP the ip address of all robot authorized to cross the crossroad
					String tmp = wifi.receiveUDP(); //!! blocking method !!
					//only get the first chars because we received a byte[256] ( cf WifiCommunication.java:WifiCommunication.receiveUDP() )
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
				showMsg("Err : receiveUDP");
				e.printStackTrace();
			}
		}

	}
}
