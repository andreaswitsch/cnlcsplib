package Reasoner;

public class RpropResult implements Comparable<RpropResult>
{
  public Double[] initialValue;
  public Double[] finalValue;
  public double initialUtil;
  public double finalUtil;
  public boolean aborted;
  public int compareTo(RpropResult other) {
    return (this.finalUtil > other.finalUtil) ? -1 : 1;
  }
}
