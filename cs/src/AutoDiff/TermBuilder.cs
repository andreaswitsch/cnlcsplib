using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UFGP;
using DotNetMatrix;

namespace AutoDiff
{
    /// <summary>
    /// A collection of static methods to build new terms
    /// </summary>
    public static class TermBuilder
    {
        /// <summary>
        /// Builds a new constant term.
        /// </summary>
        /// <param name="value">The constant value</param>
        /// <returns>The constant term.</returns>
        public static Term Constant(double value)
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
        public static Sum Sum(IEnumerable<Term> terms)
        {
            //Contract.Requires(terms.Where(term => !(term is Zero)).Count() >= 2); // require at-least two non-zero terms.
            //Contract.Requires(Contract.ForAll(terms, term => term != null));
            //Contract.Ensures(Contract.Result<Sum>() != null);

            terms = terms.Where(term => !(term is Zero));
            return new Sum(terms);
        }

        /// <summary>
        /// Builds a sum of given terms.
        /// </summary>
        /// <param name="v1">The first term in the sum</param>
        /// <param name="v2">The second term in the sum</param>
        /// <param name="rest">The rest of the terms in the sum.</param>
        /// <returns>A term representing the sum of <paramref name="v1"/>, <paramref name="v2"/> and the terms in <paramref name="rest"/>.</returns>
        public static Sum Sum(Term v1, Term v2, params Term[] rest)
        {
			if(v1 == null) throw new Exception("v1 == null");
			if(v2 == null) throw new Exception("v2 == null");
            //Contract.Requires(v1 != null);
            //Contract.Requires(v2 != null);
            //Contract.Requires(Contract.ForAll(rest, term => term != null));
            //Contract.Ensures(Contract.Result<Sum>() != null);

            var allTerms = new Term[] { v1, v2 }.Concat(rest);
            return Sum(allTerms);
        }

        /// <summary>
        /// Builds a product of given terms.
        /// </summary>
        /// <param name="v1">The first term in the product</param>
        /// <param name="v2">The second term in the product</param>
        /// <param name="rest">The rest of the terms in the product</param>
        /// <returns>A term representing the product of <paramref name="v1"/>, <paramref name="v2"/> and the terms in <paramref name="rest"/>.</returns>
        public static Term Product(Term v1, Term v2, params Term[] rest)
        {
			if(v1 == null) throw new Exception("v1 == null");
			if(v2 == null) throw new Exception("v2 == null");
            //Contract.Requires(v1 != null);
            //Contract.Requires(v2 != null);
            //Contract.Requires(Contract.ForAll(rest, term => term != null));
            //Contract.Ensures(Contract.Result<Term>() != null);

            var result = new Product(v1, v2);
            foreach (var item in rest)
                result = new Product(result, item);

            return result;
        }

        /// <summary>
        /// Builds a power terms given a base and a constant exponent
        /// </summary>
        /// <param name="t">The power base term</param>
        /// <param name="power">The exponent</param>
        /// <returns>A term representing <c>t^power</c>.</returns>
        public static Term Power(Term t, double power)
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
        public static Term Power(Term baseTerm, Term exponent)
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
        public static Term Exp(Term arg)
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
        public static Term Log(Term arg)
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
        public static Term Sin(Term arg)
        {
            return new Sin(arg);
        }
		
		/// <summary>
        /// Builds a term representing the cosine function.
        /// </summary>
        /// <param name="arg">The cosine argument.</param>
        /// <returns>A term representing the cosine of <paramref name="arg"/></returns>
        public static Term Cos(Term arg)
        {
            return new Cos(arg);
        }

        /// <summary>
        /// Constructs a 2D quadratic form given the vector components x1, x2 and the matrix coefficients a11, a12, a21, a22.
        /// </summary>
        /// <param name="x1">First vector component</param>
        /// <param name="x2">Second vector component</param>
        /// <param name="a11">First row, first column matrix component</param>
        /// <param name="a12">First row, second column matrix component</param>
        /// <param name="a21">Second row, first column matrix component</param>
        /// <param name="a22">Second row, second column matrix component</param>
        /// <returns>A term describing the quadratic form</returns>
        public static Term QuadForm(Term x1, Term x2, Term a11, Term a12, Term a21, Term a22)
        {
           /* Contract.Requires(x1 != null);
            Contract.Requires(x2 != null);
            Contract.Requires(a11 != null);
            Contract.Requires(a12 != null);
            Contract.Requires(a21 != null);
            Contract.Requires(a22 != null);
            Contract.Ensures(Contract.Result<Term>() != null);
			 */
            return Sum(a11 * Power(x1, 2), (a12 + a21) * x1 * x2, a22 * Power(x2, 2));
        }
		
		public static Term NormalDistribution(TVec args, TVec mean, double variance) {
			return Exp(((args-mean).NormSquared)*(-0.5/variance)) * (1/Math.Sqrt(2.0*Math.PI*variance));	
		}
		
		public static Term Gaussian(TVec args, TVec mean, double variance) {
			return Exp(((args-mean).NormSquared)*(-0.5/variance));	
		}
		
		public static Term Sigmoid(Term arg,Term upperBound, Term lowerBound, Term mid, double steepness) {
			//return (upperBound-lowerBound)*Power(1+Exp(steepness*(-arg+mid)),-1)+lowerBound;
			return (upperBound-lowerBound)*(new Sigmoid(arg,mid,steepness))+lowerBound;
			                                           
		}
		public static Term BoundedValue(Term arg, Term leftBound,Term rightBound, double steepness) {
			//return (upperValue-lowerValue)*Power(1+Exp(steepness*(-arg+leftBound)),-1)*Power(1+Exp(steepness*(arg-rightBound)),-1)+lowerValue;
			return ((new LTConstraint(leftBound,arg,steepness)) & (new LTConstraint(arg,rightBound,steepness)));
			//return (upperValue-lowerValue)*((new Sigmoid(arg,leftBound,steepness))&(1-(new Sigmoid(arg,rightBound,steepness))))+lowerValue;			
		}
		public static Term BoundedRectangle(TVec arg, TVec leftLower, TVec rightUpper, double steepness) {
				return (BoundedValue(arg.X,leftLower.X,rightUpper.X,steepness) & BoundedValue(arg.Y,leftLower.Y,rightUpper.Y,steepness));
		}
		public static Term EuclidianDistanceSqr(TVec one, TVec two) {
			return (one-two).NormSquared;
		}
		public static Term EuclidianDistance(TVec one, TVec two) {
			return Power(EuclidianDistanceSqr(one,two),0.5);
		}
		public static Term Polynom(Term[] input, int degree, Term[] param) {
			Term ret = 0;
			for(int i=0; i<input.Length; i++) {
				Term t=1;
				for(int j=1; j<degree; j++) {
					t *= input[i];
				}
				ret += t*param[i];
			}
			return ret;
		}
    }
}
