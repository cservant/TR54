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
	private String id = "192.168.43.129"; //Robot Ip address
	
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
					point = opp.getPose().getLocation();
					//showMsg("isBigB : " + isBigBlue);
				}
				
				if(!isBigBlue)
					stop = false;
				
				df.forward();
				//if we are on the entry point of the main road (this road leads to a crossroad)	
				if(isBigBlue && !stop){
					//send request for the crossroad
					// "R" : request to enter the crossroad
					// "O" : notification robot leaved the crossroad
					try {
						//we send an UDP packet to our android device (hotspot wifi ip address is always the same)
						wifi.sendUDP("192.168.43.1", "R");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						showMsg("Err : sendUDP");
					}				
					
					//showMsg("dist : " + opp.getPose().distanceTo(point));
					//check if we are close to the crossroad
					if(opp.getPose().distanceTo(point) >= 40)
					{
						//check isAuthorized to know if we received authorization from the server
						while(!isAuthorized){
							showMsg("Waiting for Auth");
							df.stop();
						}
						stop = true;
					}
					//notify the server we get out of the crossroad
					if(opp.getPose().distanceTo(point) >= 80){
						showMsg("Out");
						try{
							wifi.sendUDP("192.168.43.1", "O");
						} catch (IOException e){
							e.printStackTrace();
							showMsg("err : SendUDP");
						}
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
			showMsg("WifiPooler on");
			// TODO Auto-generated method stub
			try {
				while(true){
					//wait for a string formated as "IP;IP2;;..." with IP the ip address of all robot authorized to cross the crossroad
					String[] str = wifi.receiveUDP().split(";"); //!! blocking method !!
					showMsg("WP : received UDP");
					
					//check if our ip address is contained in the packet we received
					int i =0;
					while(!str[i].contains(id))
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
				e.printStackTrace();
				showMsg("Err : receiveUDP");
			}
		}

	}
}
