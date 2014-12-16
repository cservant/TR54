import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.robotics.Color;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.utility.Delay;

public class RobotIntelligent extends Robot{
	private long timer = 0;
	private long delaytimer = 0;
	private boolean isBigBlue = true;
	private boolean stop = false;
	private Point point = new Point(0,0);
	
	public void navigation() {
		OdometryPoseProvider opp = new OdometryPoseProvider(df);

		showMsg("On the");
		showMsg("highway to Hell");
		//wait for touchsensor to be pressed before starting
		while(!capteur.isPressed());
		
		timer = System.nanoTime();
		//while all buttons are released
		while (Button.readButtons() == 0) {
			delaytimer = System.nanoTime();
			while(capteur.getDistance() <= 15)
				df.stop();
			delaytimer = System.nanoTime() - delaytimer;
			
			
			switch(capteur.colorSensor.getColorID()){
			case Color.BLACK :
				showMsg("Color : BLACK");
				//movement on the left (the left wheel has a speed 50% of the right wheel)
				df.steer(50);
				break;
			case Color.BLUE :
				showMsg("Color : BLUE");
				//forward
				//if last time we saw blue was 2 sec ago or more
				if(((System.nanoTime() - timer - delaytimer)) >= 2*1e9)
				{
					isBigBlue = !isBigBlue;
					point = opp.getPose().getLocation();
					showMsg("isBigB : " + isBigBlue);
				}
				
				if(!isBigBlue)
					stop = false;
				
				df.forward();
								
				if(isBigBlue && !stop){
					showMsg("dist : " + opp.getPose().distanceTo(point));
					if(opp.getPose().distanceTo(point) >= 40)
					{
						df.stop();
						Delay.msDelay(2000);
						stop = true;
					}
					df.forward();
				}
				
				timer = System.nanoTime();
				break;
			case Color.WHITE :
				showMsg("Color : WHITE");
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
}
