import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

public class RobotFouine extends Robot {
	public void navigation() {
		int rotation = 0;

		while (Button.readButtons() == 0) {
			LCD.clear();
			LCD.drawString("Depart", 0, 0);
			while (capteur.detectColor())
				df.forward(); // non-bloquant

			showMsg("Perte couleur");

			while (!capteur.detectColor() && rotation < 90) {
				df.rotate(5);
				rotation += 5;
			}

			if (rotation == 90) {
				df.rotate(-rotation);
				rotation = 0;

				while (!capteur.detectColor() && rotation < 90) {
					df.rotate(-5);
					rotation += 5;
				}
			}
			rotation = 0;
		}
	}
}
