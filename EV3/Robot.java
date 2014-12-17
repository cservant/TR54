import lejos.hardware.lcd.LCD;
import lejos.robotics.navigation.DifferentialPilot;

/**
 * Abstract Robot class.
 * @author 	Alexis HESTIN
 * 			Clément SERVANT
 * 			Stéphane DORRE
 * 			Julien VOISIN 
 */
public abstract class Robot {
	
	private final PilotageMoteur pilote = new PilotageMoteur();
	private final Capteurs capteur = new Capteurs();
	private final DifferentialPilot df = pilote.getDifferentialPilot();
	private int line = 0;
	
	/**
	 * Default constructor
	 */
	public Robot() {
		LCD.clear();
		df.stop();
		df.setRotateSpeed(90);
		df.setTravelSpeed(15);
	}

	/**
	 * Navigation abstract class to implement when we create a new Robot.
	 * This is the main method which will be run by the Main class.
	 */
	public abstract void navigation();

	/**
	 * Display a message on the first available line of the embedded 8x16 char screen of the robot.
	 * If no lines are free then clear the screen and print the message on the first line again.
	 */
	public void showMsg(final String msg) {
		if(line >= 5){
			line = 0;
			LCD.clear();
		}
		LCD.drawString(msg, 0, line);
		line++;
	}
}
