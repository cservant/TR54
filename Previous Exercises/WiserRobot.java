import lejos.hardware.Button;
import lejos.robotics.Color;

public class WiserRobot extends Robot{
	
	public void navigation() {		
		showMsg("Depart");
		while (Button.readButtons() == 0) {
			switch(capteur.colorSensor.getColorID()){
			case Color.BLACK :
				showMsg("Color : BLACK");
				//movement on the left
				pilote.getMotorLeft().setSpeed(150);
				pilote.getMotorRight().setSpeed(300);
				break;
			case Color.BLUE :
				showMsg("Color : BLUE");
				//forward
				pilote.getMotorLeft().setSpeed(400);
				pilote.getMotorRight().setSpeed(400);
				break;
			case Color.WHITE :
				showMsg("Color : WHITE");
				//movement on the right
				pilote.getMotorLeft().setSpeed(300);
				pilote.getMotorRight().setSpeed(150);
				break;
			default :
				//default case
				showMsg("Color : XXX");
				pilote.getMotorLeft().setSpeed(400);
				pilote.getMotorRight().setSpeed(400);
				break;
			}
			pilote.forward();
		}
	}
}
