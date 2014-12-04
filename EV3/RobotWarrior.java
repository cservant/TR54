import lejos.hardware.Button;

public class RobotWarrior extends Robot {

	public void navigation() {
		while (Button.readButtons() == 0) {
			int angle = 15;
			int sens = 1; //inversion du coté de rotation € {1,-1}
			showMsg("Depart");
			while (capteur.detectColor())
				df.forward(); // non-bloquant

			showMsg("Perte couleur");

			while (!capteur.detectColor()) {
				if (!capteur.detectColor())
					df.rotate(sens*angle);
				if (!capteur.detectColor())
					df.rotate(-sens*angle * 2);
				if (!capteur.detectColor()){
					df.rotate(sens*angle);
					sens*=-1;
				}
				angle *= 2;
			}

			showMsg("Couleur trouvee");
		}
	}
}
