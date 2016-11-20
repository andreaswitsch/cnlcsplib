using System;
using System.IO;
using System.Threading;
using System.Collections.Generic;
//using Alica.Reasoner;
//using Al=Alica;
using AutoDiff;


namespace Alica.Reasoner.IntervalPropagation
{
		
	public class SetParents : ITermVisitor<bool>
	{
		
		public SetParents() {
		}
		
        public bool Visit(Constant constant) {			
			return false;
			//return UpdateInterval(constant,constant.Value,constant.Value);
		}

        public bool Visit(Zero zero) {		
			return false;
			//return UpdateInterval(zero,0,0);
		}

        public bool Visit(ConstPower intPower) {
			intPower.Base.Parents.Add(intPower);			
			//intPower.Base.Accept(this);		
			return false;
			
		}
		public bool Visit(TermPower tp) {
			tp.Base.Parents.Add(tp);
			tp.Exponent.Parents.Add(tp);
			return false;
		}
		public bool Visit(Gp gp) {
			throw new NotImplementedException();
			return false;
		}
        public bool Visit(Product product) {
			product.Left.Parents.Add(product);
			product.Right.Parents.Add(product);
			
			//product.Left.Accept(this);
			//product.Right.Accept(this);			
			return false;
		}

        public bool Visit(Sigmoid sigmoid) {
			sigmoid.Arg.Parents.Add(sigmoid);
			sigmoid.Mid.Parents.Add(sigmoid);
			//sigmoid.Arg.Accept(this);
			//sigmoid.Mid.Accept(this);			
			return false;
		}
		
		public bool Visit(LinSigmoid sigmoid) {
			sigmoid.Arg.Parents.Add(sigmoid);
			//sigmoid.Arg.Accept(this);
			//sigmoid.Mid.Accept(this);			
			return false;
		}
		
        public bool Visit(LTConstraint constraint) {
			constraint.Left.Parents.Add(constraint);
			constraint.Right.Parents.Add(constraint);
			//constraint.Left.Accept(this);
			//constraint.Right.Accept(this);
			return false;
		}
		
        public bool Visit(LTEConstraint constraint) {
			constraint.Left.Parents.Add(constraint);
			constraint.Right.Parents.Add(constraint);			
			//constraint.Left.Accept(this);
			//constraint.Right.Accept(this);
			return false;
		}
		
        public bool Visit(Min min) {
			min.Left.Parents.Add(min);
			min.Right.Parents.Add(min);

			//min.Left.Accept(this);
			//min.Right.Accept(this);			
			return false;
		}
		
		public bool Visit(Reification reif) {
			reif.Condition.Parents.Add(reif);
			//reif.Condition.Accept(this);
			return false;
		}
		
        public bool Visit(Max max) {
			max.Left.Parents.Add(max);
			max.Right.Parents.Add(max);
			//max.Left.Accept(this);
			//max.Right.Accept(this);			
			return false;
		}
		
        public bool Visit(And and) {
			and.Left.Parents.Add(and);
			and.Right.Parents.Add(and);
			//and.Left.Accept(this);
			//and.Right.Accept(this);
			return false;
		}
		
        public bool Visit(Or or) {
			or.Left.Parents.Add(or);
			or.Right.Parents.Add(or);
			//or.Left.Accept(this);
			//or.Right.Accept(this);
			return false;
		}
		
        public bool Visit(ConstraintUtility cu) {
			cu.Constraint.Parents.Add(cu);
			cu.Utility.Parents.Add(cu);
			//cu.Constraint.Accept(this);
			//cu.Utility.Accept(this);			
			return false;
		}
		
        public bool Visit(Sum sum) {
			foreach(Term t in sum.Terms) {
				t.Parents.Add(sum);
				//t.Accept(this);
			}
			return false;
		}

        public bool Visit(AutoDiff.Variable variable) {
			return false;
		}

        public bool Visit(Log log) {
			log.Arg.Parents.Add(log);
			//log.Arg.Accept(this);
			return false;
		}
        public bool Visit(Sin sin) {
			sin.Arg.Parents.Add(sin);
			//sin.Arg.Accept(this);
			return false;
		}

        public bool Visit(Cos cos) {
			cos.Arg.Parents.Add(cos);
			//cos.Arg.Accept(this);
			return false;
		}

        public bool Visit(Abs abs) {
			abs.Arg.Parents.Add(abs);
			//abs.Arg.Accept(this);
			return false;
		}
		
 
        public bool Visit(Exp exp) {
			exp.Arg.Parents.Add(exp);
			//exp.Arg.Accept(this);
			return false;
		}
		public bool Visit(Atan2 atan2) {
			atan2.Left.Parents.Add(atan2);
			atan2.Right.Parents.Add(atan2);
			//atan2.Left.Accept(this);
			//atan2.Right.Accept(this);
			return false;
		}
		
	
		
	}
	
}