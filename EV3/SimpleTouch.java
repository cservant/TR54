import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;

public class SimpleTouch extends AbstractFilter {
  private float[] sample;

  public SimpleTouch(SampleProvider source) {
    super(source);
    sample = new float[sampleSize];
  }

  public boolean isPressed() {
    super.fetchSample(sample, 0);
    return (sample[0] != 0)
  }

}
