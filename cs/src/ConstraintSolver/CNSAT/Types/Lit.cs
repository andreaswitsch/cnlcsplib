using System;
using System.Collections.Generic;
using AutoDiff;
namespace Alica.Reasoner.CNSAT
{
	public class Lit : ITermVisitor
	{
		public Lit (Var v, Assignment ass=Assignment.True)
		{
			Var = v;
			this.Sign = ass;
			if(ass == Assignment.True) Var.PositiveAppearance++;
			else Var.NegActivity++;
			this.IsTemporary = false;
			VariableCount = -1;
		}
		
		public bool Satisfied () {
			return Sign==Var.Assignment;
		}
		
		public bool Conflicted() {
			return Var.Assignment!=Sign && Var.Assignment!=Assignment.Unassigned;
		}
		
		public Assignment Sign {set; get;}
		public Var Var{	get; set; }
		public int VariableCount{ get; set; }
		
		public void ComputeVariableCount() {
			VariableCount = 0;
			Atom.Accept(this);
		}
		
		internal bool IsTemporary{ get; set;}
		public Lit(Term t,Assignment ass, bool temp) {
			this.IsTemporary = temp;
			this.Sign = ass;
			this.Atom = t;
			VariableCount = -1;
		}
		internal Term Atom {get; set;}
		
		public void Visit(Constant constant) {				
		}

        public void Visit(Zero zero) {					
		}

        public void Visit(ConstPower intPower) {			
			intPower.Base.Accept(this);
		}
		public void Visit(TermPower intPower) {			
			intPower.Base.Accept(this);
			intPower.Exponent.Accept(this);
		}
		

        public void Visit(Product product) {
			product.Left.Accept(this);
			product.Right.Accept(this);
		}

        public void Visit(Sigmoid sigmoid) {			
			sigmoid.Arg.Accept(this);
			sigmoid.Mid.Accept(this);
		}
		
		public void Visit(LinSigmoid sigmoid) {			
			sigmoid.Arg.Accept(this);
		}
		
        public void Visit(LTConstraint constraint) {
			constraint.Left.Accept(this);
			constraint.Right.Accept(this);
		}
		
        public void Visit(LTEConstraint constraint) {
			constraint.Left.Accept(this);
			constraint.Right.Accept(this);
		}
		
        public void Visit(Min min) {
			min.Left.Accept(this);
			min.Right.Accept(this);
		}
		
        public void Visit(Max max) {
			max.Left.Accept(this);
			max.Right.Accept(this);
		}
		
        public void Visit(And and) {
			and.Left.Accept(this);
			and.Right.Accept(this);
		}
		
        public void Visit(Or or) {
			or.Left.Accept(this);
			or.Right.Accept(this);
		}
		
        public void Visit(ConstraintUtility cu) {
			cu.Constraint.Accept(this);
			cu.Utility.Accept(this);
		}
		
        public void Visit(Sum sum) {
			foreach(Term t in sum.Terms) {
				t.Accept(this);
			}
		}
		
		public void Visit(Gp gp) {
			throw new NotImplementedException();
			/*foreach(Term t in sum.Terms) {
				t.Accept(this);
			}*/
		}

        public void Visit(AutoDiff.Variable variable) {
			VariableCount++;
		}
		
		public void Visit(Reification reif) {
			reif.Condition.Accept(this);
		}

        public void Visit(Log log) {
			log.Arg.Accept(this);
		}
		
        public void Visit(Sin sin) {
			sin.Arg.Accept(this);
		}

        public void Visit(Cos cos) {
			cos.Arg.Accept(this);
		}

        public void Visit(Abs abs) {
			abs.Arg.Accept(this);
		}		
 
        public void Visit(Exp exp) {
			exp.Arg.Accept(this);
		}
		public void Visit(Atan2 atan2) {
			atan2.Left.Accept(this);
			atan2.Right.Accept(this);
		}
	}
}

