//#define DEBUG_UP
using System;
using System.IO;
using System.Threading;
using System.Collections.Generic;
//using Alica.Reasoner;
//using Al=Alica;
using AutoDiff;


namespace Alica.Reasoner.IntervalPropagation
{
		
	public class UpwardPropagator : ITermVisitor<bool>
	{
		internal TermList Changed;
		//internal Queue<Term> Changed;
		/*private void AddAllChanged(List<Term> l) {
			foreach(Term t in l) {
				if(!Changed.Contains(t)) Changed.Enqueue(t);
			}
		}*/
		private void AddChanged(Term t) {			
			/*foreach(Term s in t.Parents) {
				Changed.Enqueue(s);
			}
			Changed.Enqueue(t);*/
			foreach(Term s in t.Parents) {
				//Changed.Enqueue(s);
				if(!Changed.Contains(s)) Changed.Enqueue(s);
				//Changed.MoveToEnd(s);
			}
			if(!Changed.Contains(t)) Changed.Enqueue(t);
			//Changed.Enqueue(t);
		}
		public UpwardPropagator() {
		}
		
		internal DownwardPropagator DP {get; set;}
		
		
        public bool Visit(Constant constant) {
			
			return false;
		}

        public bool Visit(Zero zero) {
			
			return false;
		}

        public bool Visit(ConstPower intPower) {
			bool includesZero = intPower.Base.Min*intPower.Base.Max <= 0;	
			if (intPower.Exponent > 0) {
				double a = Math.Pow(intPower.Base.Max,intPower.Exponent);
				double b = Math.Pow(intPower.Base.Min,intPower.Exponent);
				if (includesZero) {
					if(UpdateInterval(intPower,Math.Min(0,Math.Min(a,b)),Math.Max(0,Math.Max(a,b)))) {
					//if(UpdateInterval(intPower,Math.Min(0,Math.Pow(intPower.Base.Min,intPower.Exponent)),Math.Max(0,Math.Pow(intPower.Base.Max,intPower.Exponent)))) {
						AddChanged(intPower);
						return true;
					}
				} else {
					if(UpdateInterval(intPower,Math.Min(a,b),Math.Max(a,b))) {
					//if(UpdateInterval(intPower,Math.Pow(intPower.Base.Min,intPower.Exponent),Math.Pow(intPower.Base.Max,intPower.Exponent))) {
						AddChanged(intPower);
						return true;
					}
				}				
			} 
			else if (!includesZero) {
				double a = Math.Pow(intPower.Base.Max,intPower.Exponent);
				double b = Math.Pow(intPower.Base.Min,intPower.Exponent);

				//Console.WriteLine("Cur: {0} [{1} : {2}]",intPower,intPower.Min,intPower.Max);
				//Console.WriteLine("Base: [{0} : {1}]",intPower.Base.Min,intPower.Base.Max);				
				
				if(UpdateInterval(intPower,Math.Min(a,b),Math.Max(a,b))) {
					//Console.WriteLine("From UW intpower {0}",intPower.Exponent);
				//if(UpdateInterval(intPower,Math.Pow(intPower.Base.Max,intPower.Exponent),Math.Pow(intPower.Base.Min,intPower.Exponent))) {
					AddChanged(intPower);
					return true;
				}
			} //else +- Infinity is possible
			return false;
		}
		public bool Visit(TermPower tp) {
			throw new NotImplementedException("Propagation for TemPower not implemented");					
		}
		public bool Visit(Gp gp) {
			throw new NotImplementedException("Propagation for TemPower not implemented");					
		}

        public bool Visit(Product product) {
			double aa = product.Left.Min*product.Right.Min;
			double bb = product.Left.Max*product.Right.Max;
			double max;
			double min;
			if (product.Left == product.Right) {
				min = Math.Min(aa,bb);
				max = Math.Max(aa,bb);
				if (product.Left.Min*product.Left.Max <= 0) min = 0;								
			}
			else {
				double ab = product.Left.Min*product.Right.Max;
				double ba = product.Left.Max*product.Right.Min;
				max = Math.Max(aa,Math.Max(ab,Math.Max(ba,bb)));
				min = Math.Min(aa,Math.Min(ab,Math.Min(ba,bb)));				
			}			
			if(UpdateInterval(product,min,max)) {	
				AddChanged(product);
				return true;
			}			
			return false;			
		}

        public bool Visit(Sigmoid sigmoid) {			
			throw new NotImplementedException("Sigmoidal propagation not implemented");
		}
		
		public bool Visit(LinSigmoid sigmoid) {			
			throw new NotImplementedException("Sigmoidal propagation not implemented");
		}
		
        public bool Visit(LTConstraint constraint) {
			if (constraint.Left.Max < constraint.Right.Min) {
				if(UpdateInterval(constraint,1,1)) {
					AddChanged(constraint);
					return true;
				}
			}
			else if (constraint.Left.Min >= constraint.Right.Max) {
				//Console.WriteLine("LT UP negated: {0} {1}",constraint.Left.Min ,constraint.Right.Max);
				if(UpdateInterval(constraint,Double.NegativeInfinity,0)) {
					AddChanged(constraint);
					return true;
				}
			}
			return false;
		}
		
        public bool Visit(LTEConstraint constraint) {			
			if (constraint.Left.Max <= constraint.Right.Min) {
				if(UpdateInterval(constraint,1,1)) {
					AddChanged(constraint);
					return true;
				}
			}
			else if (constraint.Left.Min > constraint.Right.Max) {
				if(UpdateInterval(constraint,Double.NegativeInfinity,0)) {
					AddChanged(constraint);
					return true;
				}
			}

			return false;
		}
		
        public bool Visit(Min min) {
			if(UpdateInterval(min,Math.Min(min.Left.Min,min.Right.Min),Math.Max(min.Left.Max,min.Right.Max))) {
				AddChanged(min);
				return true;
			}
			return false;
		}
		
        public bool Visit(Max max) {
			if(UpdateInterval(max,Math.Min(max.Left.Min,max.Right.Min),Math.Max(max.Left.Max,max.Right.Max))) {
				AddChanged(max);
				return true;
			}
			return false;
		}
		
        public bool Visit(And and) {			
			if(and.Left.Min > 0 && and.Right.Min > 0) {
				if(UpdateInterval(and,1,1)) {
					AddChanged(and);
					return true;
				}
			}	
			else if(and.Left.Max <= 0 || and.Right.Max <= 0) {
				if(UpdateInterval(and,Double.NegativeInfinity,0)) {
					AddChanged(and);
					return true;
				}
			}
			return false;
		}
		
        public bool Visit(Or or) {
			if(or.Left.Min > 0 || or.Right.Min > 0) {
				if(UpdateInterval(or,1,1)) {
					AddChanged(or);
					return true;
				}
			}	
			else if(or.Left.Max <= 0 && or.Right.Max <= 0) {
				if(UpdateInterval(or,Double.NegativeInfinity,0)) {
					AddChanged(or);
					return true;
				}
			}			
			return false;
		}
		
        public bool Visit(ConstraintUtility cu) {
			if (cu.Constraint.Max < 1) {
				if (UpdateInterval(cu,Double.NegativeInfinity,cu.Constraint.Max)) {
					AddChanged(cu);
					return true;
				}
			}
			if (UpdateInterval(cu,Double.NegativeInfinity,cu.Utility.Max)) {
				AddChanged(cu);
				return true;
			}
			return false;
		}
		
		public bool Visit(Reification reif) {
			if (reif.Condition.Min > 0) {
				if(UpdateInterval(reif,reif.MaxVal,reif.MaxVal)) {
					AddChanged(reif);
					return true;
				}
			}
			else if (reif.Condition.Max < 0) {
				if(UpdateInterval(reif,reif.MinVal,reif.MinVal)) {
					AddChanged(reif);
					return true;
				}				
			}
			return false;
		}
		
        public bool Visit(Sum sum) {			
			double min = 0;
			double max = 0;

			for(int i=sum.Terms.Count-1; i>=0; --i) {
				min += sum.Terms[i].Min;
				max += sum.Terms[i].Max;
			}			
			if(UpdateInterval(sum,min,max)) {
				AddChanged(sum);
				return true;
			}
			return false;
		}

        public bool Visit(AutoDiff.Variable variable) {
			return true;
		}

        public bool Visit(Log log) {
			if(UpdateInterval(log,Math.Log(log.Arg.Min),Math.Log(log.Arg.Max))) {
				AddChanged(log);
				return true;
			}
			return false;
		}
        public bool Visit(Sin sin) {
			double size = sin.Arg.Max - sin.Arg.Min;
			bool c = false;
			if (size <= 2*Math.PI) {
				double a = Math.Sin(sin.Arg.Max);
				double b = Math.Sin(sin.Arg.Min);
				double halfPI = Math.PI/2;
				double x = Math.Ceiling((sin.Arg.Min-halfPI) / Math.PI);
				double y = Math.Floor((sin.Arg.Max-halfPI)/ Math.PI);
				if (x==y) { //single extrema
					if(((int)x)%2 == 0) { //maxima
						c = UpdateInterval(sin,Math.Min(a,b),1);
					} else { //minima
						c = UpdateInterval(sin,-1,Math.Max(a,b));
					}						
				} else if (x > y) { //no extrema
					c = UpdateInterval(sin,Math.Min(a,b),Math.Max(a,b));
				} //multiple extrema, don't update
			}
			if(c) AddChanged(sin);
			return c;
		}

        public bool Visit(Cos cos) {
			double size = cos.Arg.Max - cos.Arg.Min;
			bool c = false;
			if (size <= 2*Math.PI) {
				double a = Math.Cos(cos.Arg.Max);
				double b = Math.Cos(cos.Arg.Min);				
				double x = Math.Ceiling(cos.Arg.Min / Math.PI);
				double y = Math.Floor(cos.Arg.Max / Math.PI);
				if (x==y) { //single extrema
					if(((int)x)%2 == 0) { //maxima
						c = UpdateInterval(cos,Math.Min(a,b),1);
					} else { //minima
						c = UpdateInterval(cos,-1,Math.Max(a,b));
					}						
				} else if (x > y) { //no extrema
					c = UpdateInterval(cos,Math.Min(a,b),Math.Max(a,b));
				} //multiple extrema, don't update
			}
			if (c) AddChanged(cos);
			return c;
		}

        public bool Visit(Abs abs) {
			bool containsZero = abs.Arg.Min * abs.Arg.Max <= 0;
			bool c = false;
			if (containsZero) c = UpdateInterval(abs,0, Math.Max(Math.Abs(abs.Arg.Min),Math.Abs(abs.Arg.Max)));
			else c = UpdateInterval(abs,Math.Min(Math.Abs(abs.Arg.Min),Math.Abs(abs.Arg.Max)), Math.Max(Math.Abs(abs.Arg.Min),Math.Abs(abs.Arg.Max)));
			if (c) AddChanged(abs);
			return c;
		}
		
 
        public bool Visit(Exp exp) {
			if(UpdateInterval(exp,Math.Exp(exp.Arg.Min),Math.Exp(exp.Arg.Max))) {
				AddChanged(exp);
				return true;
			}
			return false;
		}
		public bool Visit(Atan2 atan2) {
			throw new NotImplementedException("Atan2 prop not implemented!");
		}
				
		protected void OutputChange(Term t,double oldmin, double oldmax) {
			//Console.WriteLine("UW: Interval of {0} is now [{1}, {2}]",t,t.Min,t.Max);
			double oldwidth = oldmax - oldmin;
			double newwidth = t.Max - t.Min;
			if(t is AutoDiff.Variable) Console.WriteLine("UW shrinking [{0}..{1}] to [{2}..{3}] by {4} ({5}%)",oldmin,oldmax,t.Min,t.Max,oldwidth-newwidth,(oldwidth-newwidth)/oldwidth*100);
		}
		protected bool UpdateInterval(Term t, double min, double max) {
			bool ret = t.Min < min || t.Max > max;
#if DEBUG_UP
			double oldmin = t.Min;
			double oldmax = t.Max;
#endif 		
			if(!Double.IsNaN(min)) t.Min = Math.Max(t.Min,min);
			if(!Double.IsNaN(max)) t.Max = Math.Min(t.Max,max);
			if(ret) IntervalPropagator.updates++;
			IntervalPropagator.visits++;
#if DEBUG_UP
			if (ret) OutputChange(t,oldmin,oldmax);
#endif			
			if (t.Min > t.Max) throw new UnsolveableException();
			return ret;
		}
		
	}
	
}