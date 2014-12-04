import lejos.hardware.port.SensorPort;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;

public class Capteurs {
	final EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S1);
	final SensorModes ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S2);
	final EV3TouchSensor tSensor = new EV3TouchSensor(SensorPort.S4);
	final SimpleTouch touchSensor = new SimpleTouch(tSensor);
	private SampleProvider distance = ultrasonicSensor.getMode("Distance");
	private float[] sample = new float[distance.sampleSize()];
	
	public boolean detectColor() {
		// The formula is the "ITU-R Recommendation BT.709" one.
		float rgb[] = new float[3];
		colorSensor.getRGBMode().fetchSample(rgb, 0);
		return (0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]) < 0.1;
	}
	
	public boolean isColor(Color color){
		if( colorSensor.getColorID() == color.getColor() )
			return true;
		return false;
	}
	
	public float getDistance(){
		distance.fetchSample(sample, 0);
		return sample[0]*100;
	}
	
	public boolean isPressed(){
		return touchSensor.isPressed();
	}
}
