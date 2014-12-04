import lejos.hardware.lcd.LCD;
import lejos.robotics.navigation.DifferentialPilot;

public abstract class Robot {
	final PilotageMoteur pilote = new PilotageMoteur();
	final Capteurs capteur = new Capteurs();
	final DifferentialPilot df = pilote.getDifferentialPilot();
	private int line = 0;
	public Robot() {
		LCD.clear();
		df.stop();
		df.setRotateSpeed(90);
		df.setTravelSpeed(15);
	}

	public abstract void navigation();

	public void showMsg(final String msg) {
		if(line >= 5){
			line = 0;
			LCD.clear();
		}
		LCD.drawString(msg, 0, line);
		line++;
	}
}
