using System;
using System.IO;
using System.Threading;
using System.Collections.Generic;
//using Alica.Reasoner;
//using Al=Alica;
using AutoDiff;


namespace Alica.Reasoner.IntervalPropagation
{
		
	public class ResetIntervals : ITermVisitor<bool>
	{
		
		public ResetIntervals() {
		}
		
		
        public bool Visit(Constant constant) {			
			constant.Parents.Clear();
			UpdateInterval(constant,constant.Value,constant.Value);
			return true;
		}

        public bool Visit(Zero zero) {	
			zero.Parents.Clear();
			UpdateInterval(zero,0,0);
			return true;		
		}

        public bool Visit(ConstPower intPower) {
			intPower.Parents.Clear();
			
			intPower.Base.Accept(this);
			
			if (intPower.Exponent == 0) {
				UpdateInterval(intPower,0,0);
				return true;
			}
			double e = Math.Round(intPower.Exponent);
			if(intPower.Exponent == e && ((int)e)%2 == 0) {
				UpdateInterval(intPower,0,Double.PositiveInfinity);
				return true;
			}
			UpdateInterval(intPower,Double.NegativeInfinity,Double.PositiveInfinity);
			return false;
			
		}
		public bool Visit(TermPower tp) {
			tp.Parents.Clear();			
			tp.Base.Accept(this);
			tp.Exponent.Accept(this);
			UpdateInterval(tp,Double.NegativeInfinity,Double.PositiveInfinity);
			return false;
		}
		
		public bool Visit(Gp gp) {
			throw new NotImplementedException();
			return false;
		}
		
        public bool Visit(Product product) {
			product.Parents.Clear();
			UpdateInterval(product,Double.NegativeInfinity,Double.PositiveInfinity);
			product.Left.Accept(this);
			product.Right.Accept(this);			
			return false;
		}

        public bool Visit(Sigmoid sigmoid) {	
			sigmoid.Parents.Clear();
			sigmoid.Arg.Accept(this);
			sigmoid.Mid.Accept(this);			
			UpdateInterval(sigmoid,0,1);
			return true;
		}
		
		public bool Visit(LinSigmoid sigmoid) {	
			sigmoid.Parents.Clear();
			sigmoid.Arg.Accept(this);
			UpdateInterval(sigmoid,0,1);
			return true;
		}
		
        public bool Visit(LTConstraint constraint) {
			constraint.Parents.Clear();
			constraint.Left.Accept(this);
			constraint.Right.Accept(this);
			UpdateInterval(constraint,Double.NegativeInfinity,1);
			return true;
		}
		
        public bool Visit(LTEConstraint constraint) {
			constraint.Parents.Clear();
			constraint.Left.Accept(this);
			constraint.Right.Accept(this);
			UpdateInterval(constraint,Double.NegativeInfinity,1);
			return true;
		}
		
        public bool Visit(Min min) {
			min.Parents.Clear();
			min.Left.Accept(this);
			min.Right.Accept(this);			
			UpdateInterval(min,Double.NegativeInfinity,Double.PositiveInfinity);
			return true;
		}
		
		public bool Visit(Reification reif) {
			reif.Parents.Clear();
			reif.Condition.Accept(this);
			UpdateInterval(reif,reif.MinVal,reif.MaxVal);			
			return true;
		}
		
        public bool Visit(Max max) {
			max.Parents.Clear();
			max.Left.Accept(this);
			max.Right.Accept(this);			
			UpdateInterval(max,Double.NegativeInfinity,Double.PositiveInfinity);
			return true;
		}
		
        public bool Visit(And and) {
			and.Parents.Clear();
			and.Left.Accept(this);
			and.Right.Accept(this);
			UpdateInterval(and,Double.NegativeInfinity,1);
			//UpdateInterval(and,1,1); //enforce the purely conjunctive problem
			return true;
		}
		
        public bool Visit(Or or) {
			or.Parents.Clear();
			or.Left.Accept(this);
			or.Right.Accept(this);
			UpdateInterval(or,Double.NegativeInfinity,1);
			return true;
		}
		
        public bool Visit(ConstraintUtility cu) {
			cu.Parents.Clear();
			cu.Constraint.Accept(this);
			cu.Utility.Accept(this);			
			UpdateInterval(cu,1,Double.PositiveInfinity);
			return true;
		}
		
        public bool Visit(Sum sum) {
			sum.Parents.Clear();
			foreach(Term t in sum.Terms) t.Accept(this);
			UpdateInterval(sum,Double.NegativeInfinity,Double.PositiveInfinity);
			return true;
		}

        public bool Visit(AutoDiff.Variable variable) {
			variable.Parents.Clear();
			UpdateInterval(variable,variable.GlobalMin,variable.GlobalMax);
			return true;
		}

        public bool Visit(Log log) {
			log.Parents.Clear();
			log.Arg.Accept(this);
			UpdateInterval(log,Double.NegativeInfinity,Double.PositiveInfinity);
			return true;
		}
        public bool Visit(Sin sin) {
			sin.Parents.Clear();
			sin.Arg.Accept(this);
			UpdateInterval(sin,-1,1);
			return true;
		}

        public bool Visit(Cos cos) {
			cos.Parents.Clear();
			cos.Arg.Accept(this);
			UpdateInterval(cos,-1,1);
			return true;
		}

        public bool Visit(Abs abs) {
			abs.Parents.Clear();
			abs.Arg.Accept(this);
			UpdateInterval(abs,0,Double.PositiveInfinity);
			return true;
		}
		
 
        public bool Visit(Exp exp) {
			exp.Parents.Clear();
			exp.Arg.Accept(this);
			UpdateInterval(exp,0,Double.PositiveInfinity);
			return true;
		}
		public bool Visit(Atan2 atan2) {
			atan2.Parents.Clear();
			atan2.Left.Accept(this);
			atan2.Right.Accept(this);
			UpdateInterval(atan2,-Math.PI,Math.PI);			
			return true;
		}
		
		
		private void UpdateInterval(Term t, double min, double max) {			
			t.Min = min;
			t.Max = max;
			return;
		}
		
	}
	
}