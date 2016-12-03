package AutoDiff.Compiled;



/**
 * Created by awitsch on 12.11.16.
 */

public class DiffVisitor implements ITapeVisitor
{
  private final TapeElement[] tape;
  public double LocalDerivative;
  public int ArgumentIndex;
  private static double Epsilon = 10E-5;
  public DiffVisitor(TapeElement[] tape) throws Exception {
    this.tape = tape;
  }

  @Override
  public void visit(final Constant elem) throws Exception
  {
  }

  @Override
  public void visit(Exp elem) throws Exception {
    LocalDerivative = elem.Adjoint * elem.Value;
  }

  @Override
  public void visit(Log elem) throws Exception {
    LocalDerivative = elem.Adjoint / valueOf(elem.Arg);
  }

  @Override
  public void visit(ConstPower elem) throws Exception {
    LocalDerivative = elem.Adjoint * elem.Exponent * Math.pow(valueOf(elem.Base), elem.Exponent - 1);
  }

  @Override
  public void visit(TermPower elem) throws Exception {
//    Debug.Assert(ArgumentIndex == 0 || ArgumentIndex == 1);
    if (ArgumentIndex == 0)
    {
      double exponent = valueOf(elem.Exponent);
      LocalDerivative = elem.Adjoint * exponent * Math.pow(valueOf(elem.Base), exponent - 1);
    }
    else
    {
      double baseValue = valueOf(elem.Base);
      LocalDerivative = elem.Adjoint * Math.pow(baseValue, valueOf(elem.Exponent)) * Math.log(baseValue);
    }
  }

  @Override
  public void visit(Product elem) throws Exception {
    //            Debug.Assert(ArgumentIndex == 0 || ArgumentIndex == 1);
    if (ArgumentIndex == 0)
      LocalDerivative = elem.Adjoint * valueOf(elem.Right);
    else
      LocalDerivative = elem.Adjoint * valueOf(elem.Left);
  }

  @Override
  public void visit(Sum elem) throws Exception {
    LocalDerivative = elem.Adjoint;
  }

  @Override
  public void visit(Variable var) throws Exception {
  }

  private double valueOf(int index) throws Exception {
    return tape[index].Value;
  }

  /**
   * Additions by Carpe Noctem:
   */
  @Override
  public void visit(Sin elem) throws Exception {
    LocalDerivative = elem.Adjoint * Math.cos(valueOf(elem.Arg));
  }

  @Override
  public void visit(Cos elem) throws Exception {
    LocalDerivative = -elem.Adjoint * Math.sin(valueOf(elem.Arg));
    ;
  }

  @Override
  public void visit(Abs elem) throws Exception {
    if (valueOf(elem.Arg) >= 0)
    {
      LocalDerivative = elem.Adjoint;
    }
    else if (valueOf(elem.Arg) < 0)
    {
      LocalDerivative = -elem.Adjoint;
    }

  }

  @Override
  public void visit(Reification elem) throws Exception {
    if (ArgumentIndex == 0)
    {
      if (valueOf(elem.Condition) > 0)
      {
        LocalDerivative = 0;
      }
      else
      {
        LocalDerivative = elem.Adjoint * (elem.Max - elem.Min);
      }
    }
    else
    {
      //*elem.Max/Math.Abs(ValueOf(elem.Condition));
      if (valueOf(elem.Condition) <= 0)
      {
        LocalDerivative = 0;
      }
      else
      {
        LocalDerivative = -elem.Adjoint * (elem.Max - elem.Min);
      }
    }
  }

  @Override
  public void visit(Min elem) throws Exception {
    if (ArgumentIndex == 0)
    {
      if (valueOf(elem.Left) < valueOf(elem.Right))
      {
        LocalDerivative = elem.Adjoint;
      }
      else if (valueOf(elem.Left) == valueOf(elem.Right))
      {
        if (valueOf(elem.Left) < 0.5)
          LocalDerivative = elem.Adjoint;
        else
          LocalDerivative = 0;
      }
      else
      {
        //elem.Derivative*.5;
        LocalDerivative = 0;
      }
    }
    else
    {
      if (valueOf(elem.Left) > valueOf(elem.Right))
      {
        LocalDerivative = elem.Adjoint;
      }
      else if (valueOf(elem.Left) == valueOf(elem.Right))
      {
        LocalDerivative = 0;
      }
      else
      {
        //elem.Derivative*.5;
        LocalDerivative = 0;
      }
    }
  }

  @Override
  public void visit(Max elem) throws Exception {
    if (ArgumentIndex == 0)
    {
      if (valueOf(elem.Left) > valueOf(elem.Right))
      {
        LocalDerivative = elem.Adjoint;
      }
      else if (valueOf(elem.Left) == valueOf(elem.Right))
      {
        if (valueOf(elem.Left) <= 0.5)
          LocalDerivative = elem.Adjoint;
        else
          LocalDerivative = 0;
      }
      else
        LocalDerivative = 0;
    }
    else
    {
      if (valueOf(elem.Right) > valueOf(elem.Left))
      {
        LocalDerivative = elem.Adjoint;
      }
      else
      {
        LocalDerivative = 0;
      }
    }
  }

  @Override
  public void visit(And elem) throws Exception {
    if (ArgumentIndex == 0)
    {
      if (valueOf(elem.Left) > 0.75)
      {
        LocalDerivative = 0;
      }
      else
        LocalDerivative = elem.Adjoint;
    }
    else
    {
      if (valueOf(elem.Right) > 0.75)
      {
        LocalDerivative = 0;
      }
      else
        LocalDerivative = elem.Adjoint;
    }
  }

  @Override
  public void visit(Or elem) throws Exception {
    if (ArgumentIndex == 0)
    {
      if (valueOf(elem.Right) > 0.75)
      {
        LocalDerivative = 0;
      }
      else
        LocalDerivative = elem.Adjoint;
    }
    else
    {
      if (valueOf(elem.Left) > 0.75)
      {
        LocalDerivative = 0;
      }
      else
      {
        LocalDerivative = elem.Adjoint;
      }
    }
  }

  @Override
  public void visit(Sigmoid elem) throws Exception {
    //Debug.Assert(ArgumentIndex == 0 || ArgumentIndex == 1);
    double e = Math.exp(elem.Steepness * (valueOf(elem.Mid) - valueOf(elem.Arg)));
    //Console.WriteLine("e: {0} deriv: {1}",e,elem.Derivative);
    if (Double.POSITIVE_INFINITY == e || e == 0)
    {
      if (ArgumentIndex == 0)
      {
        LocalDerivative = elem.Steepness * elem.Adjoint * Epsilon;
      }
      else
      {
        LocalDerivative = -elem.Steepness * elem.Adjoint * Epsilon;
      }
    }
    else if (ArgumentIndex == 0)
    {
      LocalDerivative = elem.Steepness * elem.Adjoint * e / ((e + 1) * (e + 1));
      if (Math.abs(LocalDerivative) < Math.abs(elem.Steepness * elem.Adjoint * Epsilon))
      {
        LocalDerivative = elem.Steepness * elem.Adjoint * Epsilon;
      }

    }
    else
    {
      LocalDerivative = -elem.Steepness * elem.Adjoint * e / ((e + 1) * (e + 1));
      if (Math.abs(LocalDerivative) < Math.abs(elem.Steepness * elem.Adjoint * Epsilon))
      {
        LocalDerivative = -elem.Steepness * elem.Adjoint * Epsilon;
      }

    }
  }

  @Override
  public void visit(LinSigmoid elem) throws Exception {
    //Debug.Assert(ArgumentIndex == 0 || ArgumentIndex == 1);
    double e = Math.exp((-valueOf(elem.Arg)));
    if (Double.POSITIVE_INFINITY == e || e == 0)
    {
      if (ArgumentIndex == 0)
      {
        LocalDerivative = elem.Adjoint * Epsilon;
      }
      else
      {
        LocalDerivative = -elem.Adjoint * Epsilon;
      }
    }
    else if (ArgumentIndex == 0)
    {
      LocalDerivative = elem.Adjoint * e / ((e + 1) * (e + 1));
      if (Math.abs(LocalDerivative) < Math.abs(elem.Adjoint * Epsilon))
      {
        LocalDerivative = elem.Adjoint * Epsilon;
      }

    }
    else
    {
      LocalDerivative = -elem.Adjoint * e / ((e + 1) * (e + 1));
      if (Math.abs(LocalDerivative) < Math.abs(elem.Adjoint * Epsilon))
      {
        LocalDerivative = -elem.Adjoint * Epsilon;
      }

    }
  }

  @Override
  public void visit(LTConstraint elem) throws Exception {
    double diff = valueOf(elem.Left) - valueOf(elem.Right);
    if (diff < 0)
    {
      LocalDerivative = 0;
    }
    else
    {
      if (ArgumentIndex == 0)
      {
        LocalDerivative = -elem.Steepness * elem.Adjoint;
      }
      else
      {
        LocalDerivative = elem.Steepness * elem.Adjoint;
      }
    }
  }

  @Override
  public void visit(LTEConstraint elem) throws Exception {
    double diff = valueOf(elem.Left) - valueOf(elem.Right);
    if (diff <= 0)
    {
      LocalDerivative = 0;
    }
    else
    {
      //Normal behaviour:
      if (ArgumentIndex == 0)
      {
        LocalDerivative = -elem.Steepness * elem.Adjoint;
      }
      else
      {
        LocalDerivative = elem.Steepness * elem.Adjoint;
      }
    }
  }

  @Override
  public void visit(ConstraintUtility elem) throws Exception {
    if (ArgumentIndex == 0)
    {
      if (valueOf(elem.Constraint) < 0.999)
      {
        LocalDerivative = elem.Adjoint;
      }
      else
        LocalDerivative = 0;
    }
    else
    {
      if (valueOf(elem.Constraint) < 0.999)
      {
        LocalDerivative = 0;
      }
      else
      {
        LocalDerivative = valueOf(elem.Constraint) * elem.Adjoint;
      }
    }
  }

  @Override
  public void visit(Atan2 elem) throws Exception {
    double denom = valueOf(elem.Left) * valueOf(elem.Left) + valueOf(elem.Right) * valueOf(elem.Right);
    if (ArgumentIndex == 0)
    {
      LocalDerivative = -valueOf(elem.Right) * elem.Adjoint / denom;
    }
    else
    {
      LocalDerivative = valueOf(elem.Left) * elem.Adjoint / denom;
    }
  }

}