package Reasoner;

import AutoDiff.Term;
import AutoDiff.Variable;
import org.apache.commons.lang3.mutable.MutableDouble;

/**
 * Created by awitsch on 29.12.16.
 */
public interface ISolver
{
  public Double[] solve(Term equation, Variable[] args, Double[][] limits, MutableDouble util) throws Exception;

  public Double[] solve(AutoDiff.Term equation, AutoDiff.Variable[] args, Double[][] limits, Double[][] seeds,
      double sufficientUtility, MutableDouble util) throws Exception;
}
