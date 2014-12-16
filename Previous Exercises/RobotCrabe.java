import lejos.hardware.Button;

public class RobotCrabe extends Robot {

	public void navigation() {
		int selection = 0;
		pilote.getMotorLeft().setSpeed(100);
		pilote.getMotorRight().setSpeed(100);

		showMsg("Depart");

		while (Button.readButtons() == 0) {
			while (capteur.detectColor())
				pilote.getMotor(selection).forward();
			pilote.getMotor(selection).stop();

			selection = (selection + 1) % 2;
			showMsg("Moteur " + selection + " utilise");

			while (!capteur.detectColor())
				pilote.getMotor(selection).forward();
			pilote.getMotor(selection).stop();
		}
	}
}
