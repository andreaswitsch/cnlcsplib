package AutoDiff.Compiled;


import AutoDiff.Term;

/**
 * Created by awitsch on 12.11.16.
 */

public class EvalVisitor implements ITapeVisitor
{
  public TapeElement[] tape = new TapeElement[0];
  public EvalVisitor(TapeElement[] tape) throws Exception {
    this.tape = tape;
  }

  public void visit(Constant elem) throws Exception {
  }

  public void visit(Exp elem) throws Exception {
    elem.Value = Math.exp(valueOf(elem.Arg));
  }

  public void visit(Log elem) throws Exception {
    elem.Value = Math.log(valueOf(elem.Arg));
  }

  public void visit(ConstPower elem) throws Exception {
    elem.Value = Math.pow(valueOf(elem.Base), elem.Exponent);
  }

  public void visit(TermPower elem) throws Exception {
    elem.Value = Math.pow(valueOf(elem.Base), valueOf(elem.Exponent));
  }

  public void visit(Product elem) throws Exception {
    elem.Value = valueOf(elem.Left) * valueOf(elem.Right);
  }

  public void visit(Sum elem) throws Exception {
    elem.Value = 0;
    for (int i = 0;i < elem.Terms.length;++i)
      elem.Value += valueOf(elem.Terms[i]);
  }

  public void visit(Variable var) throws Exception {
  }

  private double valueOf(int index) throws Exception {
    return tape[index].Value;
  }

  //Additions by Carpe Noctem:
  public void visit(Sin elem) throws Exception {
    elem.Value = Math.sin(valueOf(elem.Arg));
  }

  public void visit(Cos elem) throws Exception {
    elem.Value = Math.cos(valueOf(elem.Arg));
  }

  public void visit(Max elem) throws Exception {
    elem.Value = Math.max(valueOf(elem.Left), valueOf(elem.Right));
  }

  public void visit(Min elem) throws Exception {
    elem.Value = Math.min(valueOf(elem.Left), valueOf(elem.Right));
  }

  public void visit(And elem) throws Exception {
    if (valueOf(elem.Left) > 0.75)
      elem.Value = valueOf(elem.Right);
    else if (valueOf(elem.Right) > 0.75)
      elem.Value = valueOf(elem.Left);
    else
      elem.Value = valueOf(elem.Left) + valueOf(elem.Right);
  }

  public void visit(Or elem) throws Exception {
    if (valueOf(elem.Left) > 0.75)
      elem.Value = valueOf(elem.Left);
    else if (valueOf(elem.Right) > 0.75)
      elem.Value = valueOf(elem.Right);
    else
      elem.Value = valueOf(elem.Left) + valueOf(elem.Right);
  }

  public void visit(Sigmoid elem) throws Exception {
    double e = Math.exp(elem.Steepness * (-valueOf(elem.Arg) + valueOf(elem.Mid)));
    if (Double.POSITIVE_INFINITY == e)
    {
      elem.Value = Term.Epsilon;
    }
    else
    {
      //Console.WriteLine("FUCKUP {0}",e);
      elem.Value = 1.0 / (1.0 + e);
    }
    if (elem.Value < Term.Epsilon)
      elem.Value = Term.Epsilon;

  }

  public void visit(LinSigmoid elem) throws Exception {
    double e = Math.exp((-valueOf(elem.Arg)));
    if (Double.POSITIVE_INFINITY == e)
    {
      elem.Value = Term.Epsilon;
    }
    else
    {
      //Console.WriteLine("FUCKUP {0}",e);
      elem.Value = 1.0 / (1.0 + e);
    }
    if (elem.Value < Term.Epsilon)
      elem.Value = Term.Epsilon;

  }

  public void visit(LTConstraint elem) throws Exception {
    if (valueOf(elem.Left) < valueOf(elem.Right))
    {
      elem.Value = 1;
    }
    else
    {
      elem.Value = elem.Steepness * (valueOf(elem.Right) - valueOf(elem.Left));
    }
  }

  public void visit(LTEConstraint elem) throws Exception {
    if (valueOf(elem.Left) <= valueOf(elem.Right))
    {
      elem.Value = 1;
    }
    else
    {
      elem.Value = elem.Steepness * (valueOf(elem.Right) - valueOf(elem.Left));
    }
  }

  public void visit(Reification elem) throws Exception {
    if (valueOf(elem.Condition) > 0)
    {
      elem.Value = elem.Max;
    }
    else
    {
      elem.Value = elem.Min;
    }
  }

  public void visit(ConstraintUtility elem) throws Exception {
    if (valueOf(elem.Constraint) < 0.999)
    {
      elem.Value = valueOf(elem.Constraint);
    }
    else
    {
      //Math.Max(0,ValueOf(elem.Constraint));
      elem.Value = valueOf(elem.Constraint) * valueOf(elem.Utility);
    }
  }

  public void visit(Abs elem) throws Exception {
    elem.Value = Math.abs(valueOf(elem.Arg));
  }

  public void visit(Atan2 elem) throws Exception {
    elem.Value = Math.atan2(valueOf(elem.Left), valueOf(elem.Right));
  }

}
