import lejos.hardware.Button;

public class RobotPoisson extends Robot {

	public void navigation() {
		while (Button.readButtons() == 0) {
			int angle = 15;
			showMsg("Depart");
			while (capteur.detectColor())
				df.forward(); // non-bloquant

			showMsg("Perte couleur");

			while (!capteur.detectColor()) {
				if (!capteur.detectColor())
					df.rotate(angle);
				if (!capteur.detectColor())
					df.rotate(-angle * 2);
				if (!capteur.detectColor())
					df.rotate(angle);
				angle *= 2;
			}

			showMsg("Couleur trouvee");
		}
	}
}
