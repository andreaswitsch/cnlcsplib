//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:34
//

package AutoDiff;

import java.util.ArrayList;
import java.util.List;


/*
	 * 	Extension to Term for fuzzy constraints:
	 */
//using System.Diagnostics.Contracts;/**
/* Base class for all automatically-differentiable terms.
*///[ContractClass(typeof(TermContacts))]
public abstract class Term   
{


    public double Min = Double.NEGATIVE_INFINITY;
    public double Max = Double.POSITIVE_INFINITY;
    public List<Term> Parents = new ArrayList<Term>();
    public Term Prev;
    public Term Next;

    public static double constraint_steepness = 0.01;
    public static final Term True = new Constant(1);
    public static final Term False = new Constant(Double.MIN_VALUE);
    public static final double Epsilon = 10E-10;
    // deprecated
    public static double getConstraintSteepness() throws Exception {
        return constraint_steepness;
    }

    public static void setConstraintSteepness(double value) throws Exception {
        constraint_steepness = value;
    }

    public enum AndType
    {
        min,
        and
    }
    public enum OrType
    {
        max,
        or
    }
    public static OrType orop = OrType.max;
    public static AndType andop = AndType.min;
    public static void setAnd(AndType a) throws Exception {
        andop = a;
    }

    public static void setOr(OrType o) throws Exception {
        orop = o;
    }

    //Fuzzy operators:


    //return t1*t2;

    // return t1+t2-t1*t2;



    //return TermBuilder.Sigmoid(t1,1,0,t2,constraint_steepness);

    //return TermBuilder.Sigmoid(t2,1,0,t1,constraint_steepness);


    public Term negate() throws Exception {
        return (Term.substract(TermBuilder.Constant(1), this));
    }

//    /**
//    * Accepts a term visitor
//    *
//    *  @param visitor The term visitor to accept
//    */
//    public abstract void accept(ITermVisitor visitor) throws Exception;

    /**
    * Accepts a term visitor with a generic result
    * The type of the result from the visitor's function
    *  @param visitor The visitor to accept
    *  @return The result from the visitor's visit function.
    */
    public abstract <TResult>TResult accept(ITermVisitor<TResult> visitor) throws Exception;

    /**
    * Converts a floating point constant to a constant term.
    * 
    *  @param value The floating point constnat
    *  @return The resulting term.
    */
    public static Term cast(double value) throws Exception {
        return TermBuilder.Constant(value);
    }


    /// <summary>
    /// Constructs a sum of the two given terms.
    /// </summary>
    /// <param name="left">First term in the sum</param>
    /// <param name="right">Second term in the sum</param>
    /// <returns>A term representing the sum of <paramref name="left"/> and <paramref name="right"/>.</returns>
    public static Term add(Term left, Term right) throws Exception
    {
        if (left instanceof Zero && right instanceof Zero)
        return new Zero();
            else if (left instanceof Zero)
        return right;
            else if (right instanceof Zero)
        return left;
            else
        return TermBuilder.Sum(left, right);
    }

    /// <summary>
    /// Constructs a sum of the two given terms.
    /// </summary>
    /// <param name="left">First term in the sum</param>
    /// <returns>A term representing the sum of <paramref name="left"/> and <paramref name="right"/>.</returns>
    public Term add(Term right) throws Exception
    {
        return Term.add(this, right);
    }

    /// <summary>
    /// Constructs a product term of the two given terms.
    /// </summary>
    /// <param name="left">The first term in the product</param>
    /// <param name="right">The second term in the product</param>
    /// <returns>A term representing the product of <paramref name="left"/> and <paramref name="right"/>.</returns>
    public static Term multiply(Term left, Term right) throws Exception
    {
        return TermBuilder.Product(left, right);
    }

    /// <summary>
    /// Constructs a product term of the two given terms.
    /// </summary>
    /// <param name="right">The second term in the product</param>
    /// <returns>A term representing the product of <paramref name="left"/> and <paramref name="right"/>.</returns>
    public Term multiply(Term right) throws Exception
    {
        return Term.multiply(this, right);
    }

    /// <summary>
    /// Constructs a fraction term of the two given terms.
    /// </summary>
    /// <param name="numerator">The numerator of the fraction. That is, the "top" part.</param>
    /// <param name="denominator">The denominator of the fraction. That is, the "bottom" part.</param>
    /// <returns>A term representing the fraction <paramref name="numerator"/> over <paramref name="denominator"/>.</returns>
    public static Term divide(Term numerator, Term denominator) throws Exception
    {
        return TermBuilder.Product(numerator, TermBuilder.Power(denominator, -1));
    }

    /// <summary>
    /// Constructs a fraction term of the two given terms.
    /// </summary>
    /// <param name="denominator">The denominator of the fraction. That is, the "bottom" part.</param>
    /// <returns>A term representing the fraction <paramref name="numerator"/> over <paramref name="denominator"/>.</returns>
    public Term divide(Term denominator) throws Exception
    {
        return Term.divide(this, denominator);
    }

    /// <summary>
    /// Constructs a difference of the two given terms.
    /// </summary>
    /// <param name="left">The first term in the difference</param>
    /// <param name="right">The second term in the difference.</param>
    /// <returns>A term representing <paramref name="left"/> - <paramref name="right"/>.</returns>
    public static Term substract(Term left, Term right) throws Exception
    {
        return Term.add(left,Term.multiply(TermBuilder.Constant(-1), right));
    }

    /// <summary>
    /// Constructs a difference of the two given terms.
    /// </summary>
    /// <param name="right">The second term in the difference</param>
    /// <returns>A term representing this - <paramref name="right"/>.</returns>
    public Term substract(Term right) throws Exception
    {
        return Term.substract(this, right);
    }

    /// <summary>
    /// Constructs the inverse element
    /// </summary>
    /// <returns>A term representing the inverse element</returns>
    public Term inverse() throws Exception
    {
        return Term.multiply(TermBuilder.Constant(-1), this);
    }

    /**
    * Additions by Carpe Noctem:
    */
    public abstract Term aggregateConstants() throws Exception ;

    public abstract Term derivative(Variable v) throws Exception ;

}
/*public static Term operator >=(Term t1,Term t2) { //grr!
			return t2 | !t1;
		}*//*
    [ContractClassFor(typeof(Term))]
    abstract class TermContacts : Term
    {
        public override void Accept(ITermVisitor visitor)
        {
            Contract.Requires(visitor != null);
        }
        public override TResult Accept<TResult>(ITermVisitor<TResult> visitor)
        {
            Contract.Requires(visitor != null);
            return default(TResult);
        }
    }
	*/

/*public static Term operator >=(Term t1,Term t2) { //grr!
			return t2 | !t1;
		}*//*
    [ContractClassFor(typeof(Term))]
    abstract class TermContacts : Term
    {
        public override void Accept(ITermVisitor visitor)
        {
            Contract.Requires(visitor != null);
        }
        public override TResult Accept<TResult>(ITermVisitor<TResult> visitor)
        {
            Contract.Requires(visitor != null);
            return default(TResult);
        }
    }
	*/