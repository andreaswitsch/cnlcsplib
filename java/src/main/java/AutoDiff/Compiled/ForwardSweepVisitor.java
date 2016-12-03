package AutoDiff.Compiled;


import AutoDiff.Term;

/**
 * Created by awitsch on 12.11.16.
 */


public class ForwardSweepVisitor implements ITapeVisitor
{
  private TapeElement[] tape = new TapeElement[0];
  public ForwardSweepVisitor(TapeElement[] tape) throws Exception {
    this.tape = tape;
  }

  @Override
  public void visit(final Constant elem) throws Exception
  {
  }

  @Override
  public void visit(Exp elem) throws Exception {
    elem.Value = Math.exp(valueOf(elem.Arg));
    elem.Inputs[0].Weight = elem.Value;
  }

  @Override
  public void visit(Log elem) throws Exception {
    double arg = valueOf(elem.Arg);
    elem.Value = Math.log(arg);
    elem.Inputs[0].Weight = 1 / arg;
  }

  @Override
  public void visit(ConstPower elem) throws Exception {
    double baseVal = valueOf(elem.Base);
    //modified to remove one Math.Pow -- HS
    double r = Math.pow(baseVal, elem.Exponent - 1);
    elem.Value = r * baseVal;
    elem.Inputs[0].Weight = elem.Exponent * r;
  }

  @Override
  public void visit(TermPower elem) throws Exception {
    double baseVal = valueOf(elem.Base);
    double exponent = valueOf(elem.Exponent);
    elem.Value = Math.pow(baseVal, exponent);
    elem.Inputs[0].Weight = exponent * Math.pow(baseVal, exponent - 1);
    elem.Inputs[1].Weight = elem.Value * Math.log(baseVal);
  }

  @Override
  public void visit(Product elem) throws Exception {
    double left = valueOf(elem.Left);
    double right = valueOf(elem.Right);
    elem.Value = left * right;
    elem.Inputs[0].Weight = right;
    elem.Inputs[1].Weight = left;
  }

  @Override
  public void visit(Sum elem) throws Exception {
    elem.Value = 0;
    for (int i = 0;i < elem.Terms.length;++i)
      elem.Value += valueOf(elem.Terms[i]);
    for (int i = 0;i < elem.Inputs.length;++i)
      elem.Inputs[i].Weight = 1;
  }


  @Override
  public void visit(Variable var) throws Exception {
  }

  private double valueOf(int index) throws Exception {
    return tape[index].Value;
  }

  /**
   * Additions By Carpe Noctem:
   */
  @Override
  public void visit(Sin elem) throws Exception {
    double arg = valueOf(elem.Arg);
    elem.Value = Math.sin(arg);
    elem.Inputs[0].Weight = Math.cos(arg);
  }

  @Override
  public void visit(Cos elem) throws Exception {
    double arg = valueOf(elem.Arg);
    elem.Value = Math.cos(arg);
    elem.Inputs[0].Weight = -Math.sin(arg);
  }

  @Override
  public void visit(Abs elem) throws Exception {
    double arg = valueOf(elem.Arg);
    elem.Value = Math.abs(arg);
    if (arg > 0)
    {
      elem.Inputs[0].Weight = 1;
    }
    else
    {
      elem.Inputs[0].Weight = -1;
    }
  }

  @Override
  public void visit(Reification elem) throws Exception {
    double condition = valueOf(elem.Condition);
    //double negcondition = ValueOf(elem.NegatedCondition);
    double d = elem.Max - elem.Min;
    if (condition > 0)
    {
      elem.Value = elem.Max;
      elem.Inputs[0].Weight = 0;
      elem.Inputs[0].Weight = -d;
    }
    else
    {
      elem.Value = elem.Min;
      elem.Inputs[0].Weight = d;
      elem.Inputs[0].Weight = 0;
    }
  }

  @Override
  public void visit(Min elem) throws Exception {
    double left = valueOf(elem.Left);
    double right = valueOf(elem.Right);
    elem.Value = Math.min(left, right);
    if (left < right)
    {
      elem.Inputs[0].Weight = 1;
      elem.Inputs[1].Weight = 0;
    }
    else
    {
      elem.Inputs[0].Weight = 0;
      elem.Inputs[1].Weight = 1;
    }
  }

  @Override
  public void visit(Max elem) throws Exception {
    double left = valueOf(elem.Left);
    double right = valueOf(elem.Right);
    elem.Value = Math.max(left, right);
    if (left > right)
    {
      elem.Inputs[0].Weight = 1;
      elem.Inputs[1].Weight = 0;
    }
    else
    {
      elem.Inputs[0].Weight = 0;
      elem.Inputs[1].Weight = 1;
    }
  }

  @Override
  public void visit(And elem) throws Exception {
    double left = valueOf(elem.Left);
    double right = valueOf(elem.Right);
    if (left > 0.75 && right > 0.75)
    {
      elem.Value = 1;
      elem.Inputs[0].Weight = 0;
      elem.Inputs[1].Weight = 0;
      return ;
    }

    elem.Value = 0;
    if (left <= 0)
    {
      elem.Value += left;
      elem.Inputs[0].Weight = 1;
    }

    if (right <= 0)
    {
      elem.Value += right;
      elem.Inputs[1].Weight = 1;
    }

  }

  @Override
  public void visit(Or elem) throws Exception {
    double left = valueOf(elem.Left);
    double right = valueOf(elem.Right);
    if (left > 0.75 || right > 0.75)
    {
      elem.Value = 1;
      elem.Inputs[0].Weight = 0;
      elem.Inputs[1].Weight = 0;
      return ;
    }

    elem.Value = 0;
    if (left <= 0)
    {
      elem.Value += left;
      elem.Inputs[0].Weight = 1;
    }
    else
    {
      elem.Inputs[0].Weight = 0;
    }
    if (right <= 0)
    {
      elem.Value += right;
      elem.Inputs[1].Weight = 1;
    }
    else
    {
      elem.Inputs[1].Weight = 0;
    }
  }

  @Override
  public void visit(Sigmoid elem) throws Exception {
    double arg = valueOf(elem.Arg);
    double mid = valueOf(elem.Mid);
    double e = Math.exp(elem.Steepness * (-arg + mid));
    if (Double.POSITIVE_INFINITY == e)
    {
      elem.Value = Term.Epsilon;
    }
    else
    {
      elem.Value = 1.0 / (1.0 + e);
    }
    if (elem.Value < Term.Epsilon)
      elem.Value = Term.Epsilon;

    if (e == 0.0 || Double.POSITIVE_INFINITY == e)
    {
      elem.Inputs[0].Weight = elem.Steepness * Term.Epsilon;
      elem.Inputs[1].Weight = -elem.Steepness * Term.Epsilon;
      return ;
    }

    double e2 = elem.Steepness * e / ((e + 1) * (e + 1));
    elem.Inputs[0].Weight = e2;
    elem.Inputs[1].Weight = -e2;
  }

  @Override
  public void visit(LinSigmoid elem) throws Exception {
    double arg = valueOf(elem.Arg);
    double e = Math.exp(-arg);
    if (Double.POSITIVE_INFINITY==e)
    {
      elem.Value = Term.Epsilon;
    }
    else
    {
      elem.Value = 1.0 / (1.0 + e);
    }
    if (elem.Value < Term.Epsilon)
      elem.Value = Term.Epsilon;

    if (e == 0.0 || Double.POSITIVE_INFINITY==e)
    {
      elem.Inputs[0].Weight = Term.Epsilon;
      elem.Inputs[1].Weight = -Term.Epsilon;
      return ;
    }

    double e2 = e / ((e + 1) * (e + 1));
    elem.Inputs[0].Weight = e2;
    elem.Inputs[1].Weight = -e2;
  }

  @Override
  public void visit(LTConstraint elem) throws Exception {
    double left = valueOf(elem.Left);
    double right = valueOf(elem.Right);
    if (left < right)
    {
      elem.Value = 1;
      elem.Inputs[0].Weight = 0;
      elem.Inputs[1].Weight = 0;
    }
    else
    {
      elem.Value = elem.Steepness * (right - left);
      elem.Inputs[0].Weight = -elem.Steepness;
      elem.Inputs[1].Weight = elem.Steepness;
    }
  }

  @Override
  public void visit(LTEConstraint elem) throws Exception {
    double left = valueOf(elem.Left);
    double right = valueOf(elem.Right);
    if (left <= right)
    {
      elem.Value = 1;
      elem.Inputs[0].Weight = 0;
      elem.Inputs[1].Weight = 0;
    }
    else
    {
      elem.Value = elem.Steepness * (right - left);
      elem.Inputs[0].Weight = -elem.Steepness;
      elem.Inputs[1].Weight = elem.Steepness;
    }
  }

  @Override
  public void visit(ConstraintUtility elem) throws Exception {
    double constraint = valueOf(elem.Constraint);
    //double utility = ValueOf(elem.Utility);
    if (constraint > 0)
    {
      elem.Value = valueOf(elem.Utility);
      //utility;
      elem.Inputs[0].Weight = 0;
      elem.Inputs[1].Weight = 1;
    }
    else
    {
      elem.Value = constraint;
      elem.Inputs[0].Weight = 1;
      elem.Inputs[1].Weight = 0;
    }
  }

  @Override
  public void visit(Atan2 elem) throws Exception {
    double left = valueOf(elem.Left);
    double right = valueOf(elem.Right);
    elem.Value = Math.atan2(left, right);
    double denom = left * left + right * right;
    elem.Inputs[0].Weight = -right / denom;
    elem.Inputs[1].Weight = left / denom;
  }

}
