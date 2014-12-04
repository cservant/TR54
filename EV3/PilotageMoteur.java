import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

public class PilotageMoteur {
	private DifferentialPilot df;
	private RegulatedMotor motorLeft = new EV3LargeRegulatedMotor(MotorPort.B);
	private RegulatedMotor motorRight = new EV3LargeRegulatedMotor(MotorPort.C);

	public PilotageMoteur() {
		// diametre - ecartement des roues - moteur gauch - moteur droit
		// les deux premiers patrametre ont les meme unites
		df = new DifferentialPilot(5.6, 11.78, this.getMotorLeft(),
				this.getMotorRight());
	}

	public DifferentialPilot getDifferentialPilot() {
		return df;
	}

	public void forward() {
		motorLeft.forward();
		motorRight.forward();
	}

	public void stop() {
		motorLeft.stop();
		motorRight.stop();
	}

	public RegulatedMotor getMotorLeft() {
		return motorLeft;
	}

	public RegulatedMotor getMotor(int motor) {
		if (motor == 0)
			return this.motorLeft;
		return this.motorRight;

	}

	public void setMotorLeft(RegulatedMotor motorLeft) {
		this.motorLeft = motorLeft;
	}

	public RegulatedMotor getMotorRight() {
		return motorRight;
	}

	public void setMotorRight(RegulatedMotor motorRight) {
		this.motorRight = motorRight;
	}
}
