
using System;

namespace AutoDiff
{
	/*
	 * 	Extension to Term for fuzzy constraints:
	 */

	public abstract partial class Term
	{
		public static double constraint_steepness = 0.01;
		
		public static readonly Term True = 1;
		public static readonly Term False = Double.MinValue;
		public static readonly double Epsilon = 10E-10;
		
		public static double ConstraintSteepness{// deprecated
			get {return constraint_steepness; }
			set {constraint_steepness = value;}
		}
		public enum AndType {
			min,and
		}
		public enum OrType {
			max,or
		}
		public static OrType orop = Term.OrType.max;
		public static AndType andop = Term.AndType.min;
		public static void SetAnd(AndType a) {
				andop = a;
		}
		public static void SetOr(OrType o) {
				orop = o;
		}
		//Fuzzy operators:
		
		public static Term operator !(Term t)
        {
            return t.Negate();
        }
		public static Term operator &(Term t1,Term t2)
        {
			if (andop == Term.AndType.and) {
				if (t1 == Term.True || t2 == Term.False) {
					return t2;
				} else if (t2 == Term.True || t1 == Term.False) {
					return t1;
				}
				return new And (t1, t2);
			} else {
				if (t1 == Term.True || t2 == Term.False) {
					return t2;
				} else if (t2 == Term.True || t1 == Term.False) {
					return t1;
				}
				return new Min (t1, t2);
			}
            //return t1*t2;
        }
		public static Term operator |(Term t1,Term t2)
        {
			if(orop == Term.OrType.or)  {
				if (t1 == Term.True || (t2 == Term.False && t1 == Term.False)) {
					return t1;
				} else if (t2 == Term.True) {
					return t2;
				}
				return new Or (t1, t2);
			} else {
				if (t1 == Term.True || (t2 == Term.False && t1 == Term.False)) {
					return t1;
				} else if (t2 == Term.True) {
					return t2;
				}
				return new Max (t1, t2);
			}
			
           // return t1+t2-t1*t2;
        }
		public static Term operator %(Term t1,Term t2)
		{
			return t1*t2;
		}
		public static Term operator ^(Term t1,Term t2)
		{
			return !(!t1%!t2);
		}


		public static Term operator >(Term t1,Term t2) {
			return new LTConstraint(t2,t1,constraint_steepness);
			//return TermBuilder.Sigmoid(t1,1,0,t2,constraint_steepness);			
		}
		public static Term operator <(Term t1,Term t2) {
			return new LTConstraint(t1,t2,constraint_steepness);
			//return TermBuilder.Sigmoid(t2,1,0,t1,constraint_steepness);			
		}
		public static Term operator <=(Term t1,Term t2) {
			return new LTEConstraint(t1,t2,constraint_steepness);		
		}
		public static Term operator >=(Term t1,Term t2) {
			return new LTEConstraint(t2,t1,constraint_steepness);			
		}
		
		public virtual Term Negate() {
			return (1-this);
		}
		
		
		/*public static Term operator >=(Term t1,Term t2) { //grr!
			return t2 | !t1;
		}*/
	}
}
