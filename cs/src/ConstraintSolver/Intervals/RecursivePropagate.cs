#define USEQUEUE
using System;
using System.IO;
using System.Threading;
using System.Collections.Generic;
//using Alica.Reasoner;
//using Al=Alica;
using AutoDiff;


namespace Alica.Reasoner.IntervalPropagation
{
		
	public class RecursivePropagate : ITermVisitor
	{
		
		//internal Queue<Term> Changed;
		internal TermList Changed;
		
		DownwardPropagator dp = new DownwardPropagator();
		UpwardPropagator up = new UpwardPropagator();
		SetParents sp = new SetParents();
		
		public RecursivePropagate() {
			//this.Changed = new Queue<Term>();
			this.Changed = new TermList();
			dp.Changed = this.Changed;
			up.Changed = this.Changed;
			
		}	
		public void Propagate(Term t) {	
			//for(int i=0; i<2; i++) {
			this.Changed.Clear();
			t.Accept(this);
#if RecPropDEBUG
			Console.WriteLine("Queued Terms: ");
			foreach(Term q in this.Changed) {
					Console.WriteLine(q);					
			}
			Console.WriteLine("------------------------------------");
#endif
			/*
			foreach(Term q in this.Changed) {
				q.Accept(this.sp);
			}
			while(this.Changed.Count > 0) {
				Term cur = this.Changed.Dequeue();
				cur.Accept(this.dp);
				cur.Accept(this.up);				
			}*/
			
			
			Term cur = this.Changed.First;
			while(cur!=null) {
				cur.Accept(this.sp);
				cur = cur.Next;
			}
			
			
			cur =  this.Changed.Dequeue();
			while(cur != null) {
				cur.Accept(this.dp);
				cur.Accept(this.up);
				cur = this.Changed.Dequeue();
			}
		
			//}
			/*cur = this.Changed.First;
			while(cur!=null) {				
				cur.Accept(this.dp);				
				//Term next = cur.Next;
				
				if(cur.Accept(this.up)) {
					/*Term prev = cur.Prev;
					this.Changed.MoveToEnd(cur);					
					if (prev == null) cur = this.Changed.First;
					else cur = prev.Next;*/
					//cur = cur.Next;	
			//	}				
			//	cur = cur.Next;
			//}
			//*/
		}
		private void AddToQueue(Term t) {
			if (!this.Changed.Contains(t)) this.Changed.Enqueue(t);
			//this.Changed.Enqueue(t);
			//this.Changed.MoveToEnd(t);
		}
		
		
        public void Visit(Constant constant) {				
		//	return false;
		}

        public void Visit(Zero zero) {					
		//	return false;
		}

        public void Visit(ConstPower intPower) {			
			AddToQueue(intPower);			
			intPower.Base.Accept(this);
			//return true;
		}
		public void Visit(TermPower intPower) {			
			AddToQueue(intPower);			
			intPower.Base.Accept(this);
			intPower.Exponent.Accept(this);
			//return true;
		}
		public void Visit(Gp gp) {			
			throw new NotImplementedException();
			//return true;
		}
		

        public void Visit(Product product) {
			AddToQueue(product);			
			product.Left.Accept(this);
			product.Right.Accept(this);
			//return true;
		}

        public void Visit(Sigmoid sigmoid) {			
			AddToQueue(sigmoid);
			sigmoid.Arg.Accept(this);
			sigmoid.Mid.Accept(this);
			//return true;
		}
		
		public void Visit(LinSigmoid sigmoid) {			
			AddToQueue(sigmoid);
			sigmoid.Arg.Accept(this);
			//return true;
		}
		
        public void Visit(LTConstraint constraint) {
			AddToQueue(constraint);
			constraint.Left.Accept(this);
			constraint.Right.Accept(this);
			//return true;
		}
		
        public void Visit(LTEConstraint constraint) {
			AddToQueue(constraint);
			constraint.Left.Accept(this);
			constraint.Right.Accept(this);
			//return true;			
		}
		
        public void Visit(Min min) {
			AddToQueue(min);
			min.Left.Accept(this);
			min.Right.Accept(this);
			//return true;
		}
		
        public void Visit(Max max) {
			AddToQueue(max);
			max.Left.Accept(this);
			max.Right.Accept(this);
			//return true;
		}
		
        public void Visit(And and) {
			AddToQueue(and);
			and.Left.Accept(this);
			and.Right.Accept(this);
			//return true;

		}
		
        public void Visit(Or or) {
			AddToQueue(or);
			or.Left.Accept(this);
			or.Right.Accept(this);
			//return true;
		}
		
        public void Visit(ConstraintUtility cu) {
			AddToQueue(cu);
			cu.Constraint.Accept(this);
			cu.Utility.Accept(this);
			//return true;
		}
		
        public void Visit(Sum sum) {
			AddToQueue(sum);
			foreach(Term t in sum.Terms) {
				t.Accept(this);
			}
			//return true;
		}

        public void Visit(AutoDiff.Variable variable) {
			AddToQueue(variable);
			//return true;
		}
		
		public void Visit(Reification reif) {
			AddToQueue(reif);
			reif.Condition.Accept(this);
			//return true;
		}

        public void Visit(Log log) {
			AddToQueue(log);
			log.Arg.Accept(this);
			//return true;			
		}
		
        public void Visit(Sin sin) {
			AddToQueue(sin);
			sin.Arg.Accept(this);
			//return true;						
		}

        public void Visit(Cos cos) {
			AddToQueue(cos);
			cos.Arg.Accept(this);
			//return true;
		}

        public void Visit(Abs abs) {
			AddToQueue(abs);
			abs.Arg.Accept(this);
			//return true;	
		}		
 
        public void Visit(Exp exp) {
			AddToQueue(exp);
			exp.Arg.Accept(this);
			//return true;
		}
		public void Visit(Atan2 atan2) {
			AddToQueue(atan2);
			atan2.Left.Accept(this);
			atan2.Right.Accept(this);
			//return true;
		}
				
		
		
	}
	
}