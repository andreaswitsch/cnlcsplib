package AutoDiff;

/**
 * Created by awitsch on 12.11.16.
 */
public class TermBuilder
{
  /// <summary>
  /// Builds a new constant term.
  /// </summary>
  /// <param name="value">The constant value</param>
  /// <returns>The constant term.</returns>
  public static Term Constant(double value) throws Exception
  {
    //Contract.Ensures(Contract.Result<Term>() != null);

    if (value == 0)
      return new Zero();
    else
      return new Constant(value);
  }

  /// <summary>
  /// Builds a sum of given terms.
  /// </summary>
  /// <param name="terms">The collection of terms in the sum.</param>
  /// <returns>A term representing the sum of the terms in <paramref name="terms"/>.</returns>
  public static Sum Sum(Term[] terms) throws Exception
  {
    int count = 0;
    for (final Term term : terms) {
      if(!(term instanceof Zero)) {
        count++;
      }
    }
    Term[] allTerms = new Term[count];
    count=0;
    for (int i = 0; i < terms.length; i++) {
      if(!(terms[i] instanceof Zero)) {
        allTerms[count] = terms[i];
        count++;
      }
    }
    return new Sum(allTerms);
  }

  /// <summary>
  /// Builds a sum of given terms.
  /// </summary>
  /// <param name="v1">The first term in the sum</param>
  /// <param name="v2">The second term in the sum</param>
  /// <param name="rest">The rest of the terms in the sum.</param>
  /// <returns>A term representing the sum of <paramref name="v1"/>, <paramref name="v2"/> and the terms in <paramref name="rest"/>.</returns>
  public static Sum Sum(Term v1, Term v2, Term... rest) throws Exception
  {
    if(v1 == null) throw new Exception("v1 == null");
    if(v2 == null) throw new Exception("v2 == null");

    Term[] allTerms = new Term[rest.length+2];
    allTerms[0] = v1;
    allTerms[1] = v2;
    int i=2;
    for(Term it : rest) {
      allTerms[i] = it;
      i++;
    }
    return Sum(allTerms);
  }

  /// <summary>
  /// Builds a product of given terms.
  /// </summary>
  /// <param name="v1">The first term in the product</param>
  /// <param name="v2">The second term in the product</param>
  /// <param name="rest">The rest of the terms in the product</param>
  /// <returns>A term representing the product of <paramref name="v1"/>, <paramref name="v2"/> and the terms in <paramref name="rest"/>.</returns>
  public static Term Product(Term v1, Term v2, Term... rest) throws Exception
  {
    if(v1 == null) throw new Exception("v1 == null");
    if(v2 == null) throw new Exception("v2 == null");
    //Contract.Requires(v1 != null);
    //Contract.Requires(v2 != null);
    //Contract.Requires(Contract.ForAll(rest, term => term != null));
    //Contract.Ensures(Contract.Result<Term>() != null);

    Product result = new Product(v1, v2);
    for (Term item : rest)
    result = new Product(result, item);

    return result;
  }

  /// <summary>
  /// Builds a power terms given a base and a constant exponent
  /// </summary>
  /// <param name="t">The power base term</param>
  /// <param name="power">The exponent</param>
  /// <returns>A term representing <c>t^power</c>.</returns>
  public static Term Power(Term t, double power) throws Exception
  {
    if(t == null) throw new Exception("t == null");
    //Contract.Requires(t != null);
    //Contract.Ensures(Contract.Result<Term>() != null);

    return new ConstPower(t, power);
  }

  /// <summary>
  /// Builds a power term given a base term and an exponent term.
  /// </summary>
  /// <param name="baseTerm">The base term</param>
  /// <param name="exponent">The exponent term</param>
  /// <returns></returns>
  public static Term Power(Term baseTerm, Term exponent) throws Exception
  {
           /*
            Contract.Requires(baseTerm != null);
            Contract.Requires(exponent != null);
            Contract.Ensures(Contract.Result<Term>() != null);
            */
    return new TermPower(baseTerm, exponent);
  }


  /// <summary>
  /// Builds a term representing the exponential function e^x.
  /// </summary>
  /// <param name="arg">The function's exponent</param>
  /// <returns>A term representing e^arg.</returns>
  public static Term Exp(Term arg) throws Exception
  {
    //Contract.Requires(arg != null);
    //Contract.Ensures(Contract.Result<Term>() != null);

    return new Exp(arg);
  }

  /// <summary>
  /// Builds a term representing the natural logarithm.
  /// </summary>
  /// <param name="arg">The natural logarithm's argument.</param>
  /// <returns>A term representing the natural logarithm of <paramref name="arg"/></returns>
  public static Term Log(Term arg) throws Exception
  {
    // Contract.Requires(arg != null);
    //Contract.Ensures(Contract.Result<Term>() != null);

    return new Log(arg);
  }

  /// <summary>
  /// Builds a term representing the sine function.
  /// </summary>
  /// <param name="arg">The sine argument.</param>
  /// <returns>A term representing the sine of <paramref name="arg"/></returns>
  public static Term Sin(Term arg) throws Exception
  {
    return new Sin(arg);
  }

  /// <summary>
  /// Builds a term representing the cosine function.
  /// </summary>
  /// <param name="arg">The cosine argument.</param>
  /// <returns>A term representing the cosine of <paramref name="arg"/></returns>
  public static Term Cos(Term arg) throws Exception
  {
    return new Cos(arg);
  }


//  public static Term NormalDistribution(TVec args, TVec mean, double variance) {
//    return Exp(((args-mean).NormSquared)*(-0.5/variance)) * (1/Math.Sqrt(2.0*Math.PI*variance));
//  }
//
//  public static Term Gaussian(TVec args, TVec mean, double variance) {
//    return Exp(((args-mean).NormSquared)*(-0.5/variance));
//  }
//
//  public static Term Sigmoid(Term arg,Term upperBound, Term lowerBound, Term mid, double steepness) {
//    //return (upperBound-lowerBound)*Power(1+Exp(steepness*(-arg+mid)),-1)+lowerBound;
//    return (upperBound-lowerBound)*(new Sigmoid(arg,mid,steepness))+lowerBound;
//
//  }
//  public static Term BoundedValue(Term arg, Term leftBound,Term rightBound, double steepness) {
//    //return (upperValue-lowerValue)*Power(1+Exp(steepness*(-arg+leftBound)),-1)*Power(1+Exp(steepness*(arg-rightBound)),-1)+lowerValue;
//    return ((new LTConstraint(leftBound,arg,steepness)) & (new LTConstraint(arg,rightBound,steepness)));
//    //return (upperValue-lowerValue)*((new Sigmoid(arg,leftBound,steepness))&(1-(new Sigmoid(arg,rightBound,steepness))))+lowerValue;
//  }
//  public static Term BoundedRectangle(TVec arg, TVec leftLower, TVec rightUpper, double steepness) {
//    return (BoundedValue(arg.X,leftLower.X,rightUpper.X,steepness) & BoundedValue(arg.Y,leftLower.Y,rightUpper.Y,steepness));
//  }
//  public static Term EuclidianDistanceSqr(TVec one, TVec two) {
//    return (one-two).NormSquared;
//  }
//  public static Term EuclidianDistance(TVec one, TVec two) {
//    return Power(EuclidianDistanceSqr(one,two),0.5);
//  }
//  public static Term Polynom(Term[] input, int degree, Term[] param) {
//    Term ret = 0;
//    for(int i=0; i<input.Length; i++) {
//      Term t=1;
//      for(int j=1; j<degree; j++) {
//        t *= input[i];
//      }
//      ret += t*param[i];
//    }
//    return ret;
//  }
}

