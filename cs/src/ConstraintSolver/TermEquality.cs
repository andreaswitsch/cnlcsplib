#define FORMULATRANS_DEBUG
using System;
using System.Collections.Generic;
using AutoDiff;

namespace Alica.Reasoner.CNSAT
{
	
	
	public class TermEquality
	{
		
		public bool EqualTerms(Term a, Term b) {
			if (a==b) return true;
			Type ta = a.GetType();
			Type tb = b.GetType();
			if (tb != ta) return false;
			if (a is Zero) return true;
			if (a is Constant) {
				Constant ca = (Constant) a;
				Constant cb = (Constant) b;
				return ca.Value == cb.Value;
			}
			if (a is AutoDiff.Variable) {
				return false;
				//return a == b;
			}
			if (a is LTConstraint) {
				LTConstraint ca = (LTConstraint) a;
				LTConstraint cb = (LTConstraint) b;
				return EqualTerms(ca.Left,cb.Left) && EqualTerms(ca.Right,cb.Right);
			}
			if (a is LTEConstraint) {
				LTEConstraint ca = (LTEConstraint) a;
				LTEConstraint cb = (LTEConstraint) b;
				return EqualTerms(ca.Left,cb.Left) && EqualTerms(ca.Right,cb.Right);
			}
			if (a is Product) {
				Product ca = (Product) a;
				Product cb = (Product) b;
				return EqualTerms(ca.Left,cb.Left) && EqualTerms(ca.Right,cb.Right);
			}
			if (a is ConstPower) {
				ConstPower ca = (ConstPower) a;
				ConstPower cb = (ConstPower) b;
				if (ca.Exponent != cb.Exponent) return false;
				return EqualTerms(ca.Base,cb.Base);				
			}
			if (a is Sum) {
				Sum ca = (Sum) a;
				Sum cb = (Sum) b;
				if (ca.Terms.Count != cb.Terms.Count) return false;
				for(int i=0; i<ca.Terms.Count; i++) {
					if(!EqualTerms(ca.Terms[i],cb.Terms[i])) return false;
				}
				return true;
			}
			if (a is Max) {
				Max ca = (Max) a;
				Max cb = (Max) b;
				return EqualTerms(ca.Left,cb.Left) && EqualTerms(ca.Right,cb.Right);
			}
			if (a is And) {
				And ca = (And) a;
				And cb = (And) b;
				return EqualTerms(ca.Left,cb.Left) && EqualTerms(ca.Right,cb.Right);
			}
			if (a is Sin) {
				Sin ca = (Sin) a;
				Sin cb = (Sin) b;
				return EqualTerms(ca.Arg,cb.Arg);
			}
			if (a is Cos) {
				Cos ca = (Cos) a;
				Cos cb = (Cos) b;
				return EqualTerms(ca.Arg,cb.Arg);
			}
			if (a is Abs) {
				Abs ca = (Abs) a;
				Abs cb = (Abs) b;
				return EqualTerms(ca.Arg,cb.Arg);
			}
			Console.WriteLine("Unknown Termtype in TermEquality: {0}",a);

			return false;
			
		}
		
		
		
	}
}

