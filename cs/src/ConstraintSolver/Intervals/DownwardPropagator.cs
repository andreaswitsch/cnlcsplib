//#define DEBUG_DP
using System;
using System.IO;
using System.Threading;
using System.Collections.Generic;
//using Alica.Reasoner;
//using Al=Alica;
using AutoDiff;


namespace Alica.Reasoner.IntervalPropagation
{
		
	public class DownwardPropagator : ITermVisitor<bool>
	{
		internal TermList Changed;
		//internal Queue<Term> Changed;
		private void AddChanged(Term t) {
			if(!Changed.Contains(t)) Changed.Enqueue(t);
			//Changed.Enqueue(t);
			//Changed.MoveToEnd(t);
		}
		public DownwardPropagator() {
		}
		
		
        public bool Visit(Constant constant) {			
			return false;
		}

        public bool Visit(Zero zero) {			
			return false;
		}

        public bool Visit(ConstPower intPower) {
			if (intPower.Max == Double.PositiveInfinity || intPower.Min == Double.NegativeInfinity) return false;
			double a = Math.Pow(intPower.Min,1/intPower.Exponent);
			double b = Math.Pow(intPower.Max,1/intPower.Exponent);
						
			bool isRational = intPower.Exponent != Math.Round(intPower.Exponent);
			if (isRational) {			
				if(UpdateInterval(intPower.Base,Math.Max(0,Math.Min(a,b)),Math.Max(a,Math.Max(-a,Math.Max(b,-b))))) {
					//Console.WriteLine("From DW intpower (ir) {0}",intPower);
					AddChanged(intPower.Base);
					return true;
				}
			} else {
				double min;
				double max;
				if (intPower.Exponent >= 0) {
					if (intPower.Base.Max <= 0) {
						max = Math.Max(-Math.Abs(a),-Math.Abs(b));
					} else max = Math.Max(a,Math.Max(-a,Math.Max(b,-b)));
					if (intPower.Base.Min >= 0) {
						min = Math.Min(Math.Abs(a),Math.Abs(b));					
					} else min = Math.Min(a,Math.Min(-a,Math.Min(b,-b)));
				} else { //this case can be improved
					max = Math.Max(a,Math.Max(-a,Math.Max(b,-b)));
					min = Math.Min(a,Math.Min(-a,Math.Min(b,-b)));
				}
				if(UpdateInterval(intPower.Base,min,max)) {
					//Console.WriteLine("From DW intpower {0} [{1} : {2}]",intPower,intPower.Min,intPower.Max);
					AddChanged(intPower.Base);
					return true;
				}
			}
			return false;
		}
		public bool Visit(TermPower tp) {
			throw new NotImplementedException("Propagation for TemPower not implemented");					
		}
		public bool Visit(Gp Gp) {
			throw new NotImplementedException("Propagation for TemPower not implemented");					
		}
        public bool Visit(Product product) {
			/*
			 * a*b = c
			 * ==> a = c/b
			 * */
			if (product.Left == product.Right) {
				double a = Math.Sqrt(product.Min); 
				double b = Math.Sqrt(product.Max);
				double min;
				double max;
				if (product.Left.Max <= 0) {
					max = Math.Max(-a,-b);
				} else max = Math.Max(a,b);
				if (product.Left.Min >= 0) {
					min = Math.Min(a,b);
				} else min = Math.Min(-a,-b);
				if (UpdateInterval(product.Left,min,max)) {					
					AddChanged(product.Left);
					return true;
				}
				
			} else {
				bool c=false, d = false;
				if (product.Right.Min * product.Right.Max > 0) {
					//Left:
					double aa = product.Min / product.Right.Min;
					double ab = product.Min / product.Right.Max;
					double ba = product.Max / product.Right.Min;
					double bb = product.Max / product.Right.Max;
										
					double min = Math.Min(aa,Math.Min(ab,Math.Min(ba,bb)));
					double max = Math.Max(aa,Math.Max(ab,Math.Max(ba,bb)));
					
					c = UpdateInterval(product.Left,min,max);
					if (c) AddChanged(product.Left);
				}
				if (product.Left.Min * product.Left.Max > 0) {
					//Right:
					double aa = product.Min / product.Left.Min;
					double ab = product.Min / product.Left.Max;
					double ba = product.Max / product.Left.Min;
					double bb = product.Max / product.Left.Max;
										
					double min = Math.Min(aa,Math.Min(ab,Math.Min(ba,bb)));
					double max = Math.Max(aa,Math.Max(ab,Math.Max(ba,bb)));
				
					d = UpdateInterval(product.Right,min,max);
					if(d) AddChanged(product.Right);
									
				}
				return c || d;	
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
			bool changed = false;
			if (constraint.Min > 0) {
				if(UpdateInterval(constraint.Right,constraint.Left.Min,Double.PositiveInfinity)) {
					AddChanged(constraint.Right);
					changed = true;
				}			
				if(UpdateInterval(constraint.Left,Double.NegativeInfinity,constraint.Right.Max)) {					
					AddChanged(constraint.Left);
					changed = true;
				}
			}
			else if (constraint.Max <= 0) {
				if(UpdateInterval(constraint.Right,Double.NegativeInfinity,constraint.Left.Max)) {
					AddChanged(constraint.Right);
					changed = true;
				}			
				if(UpdateInterval(constraint.Left,constraint.Right.Min,Double.PositiveInfinity)) {					
					AddChanged(constraint.Left);
					changed = true;
				}				
			}
			return changed;
		}
		
        public bool Visit(LTEConstraint constraint) {
			bool changed = false;
			if (constraint.Min > 0) {
				if(UpdateInterval(constraint.Right,constraint.Left.Min,Double.PositiveInfinity)) {
					AddChanged(constraint.Right);
					changed = true;
				}			
				if(UpdateInterval(constraint.Left,Double.NegativeInfinity,constraint.Right.Max)) {					
					AddChanged(constraint.Left);
					changed = true;
				}
			} else if (constraint.Max <= 0) {
				if(UpdateInterval(constraint.Right,Double.NegativeInfinity,constraint.Left.Max)) {
					AddChanged(constraint.Right);
					changed = true;
				}			
				if(UpdateInterval(constraint.Left,constraint.Right.Min,Double.PositiveInfinity)) {					
					AddChanged(constraint.Left);
					changed = true;
				}				
			}
			return changed;
		}
		
        public bool Visit(Min min) {
			bool c1 = UpdateInterval(min.Left,min.Min,Double.PositiveInfinity);
			bool c2 = UpdateInterval(min.Right,min.Min,Double.PositiveInfinity);
			if(c1) AddChanged(min.Left);
			if(c2) AddChanged(min.Right);
			return c1 || c2;
		}
		
        public bool Visit(Max max) {
			if (max.Min > 0) {
				bool c=false;
				if (max.Left.Max <= 0) {
					bool c1 = UpdateInterval(max.Right,1,1);	
					if(c1) AddChanged(max.Right);
					c |= c1;
				}
				if (max.Right.Max <= 0) {
					bool c2 = UpdateInterval(max.Left,1,1);					
					if(c2) AddChanged(max.Left);
					c |= c2;					
				}
				return c;
			}	
			//bool c3 = UpdateInterval(max.Left,Double.MinValue,max.Max);
			//bool c4 = UpdateInterval(max.Right,Double.MinValue,max.Max);
			return false;
		}
		
        public bool Visit(And and) {
			bool changed = false;
			if(and.Min > 0) {
				if(UpdateInterval(and.Left,1,1)) {	
					AddChanged(and.Left);
					changed = true;
				}
				if(UpdateInterval(and.Right,1,1)) {
					AddChanged(and.Right);
					changed = true;
				}
			}
			return changed;
		}
		
        public bool Visit(Or or) {
			throw new NotImplementedException("Or operator progation not implemented (max is used)");
			//return false;
		}
		
        public bool Visit(ConstraintUtility cu) {
			bool c = false;
			if(cu.Min >= 1) {
				if (UpdateInterval(cu.Constraint,1,1)) {
					AddChanged(cu.Constraint);
					c = true;
				}
				if (UpdateInterval(cu.Utility,1,cu.Max)) {
					AddChanged(cu.Utility);
					c =true;
				}
			}
			return c;
		}
		
        public bool Visit(Sum sum) {
			//a+b= c
			// a= b-c			
			//a:
			bool changed = false;
			bool anychange = false;
			do {
				changed = false;
				for(int i=0; i<sum.Terms.Count; ++i) {
					double minother = 0;
					double maxother = 0;
					for(int j=0; j<sum.Terms.Count; ++j) {
						if (i==j) continue;
						minother += sum.Terms[j].Min;
						maxother += sum.Terms[j].Max;
					}
					/*Console.WriteLine("-______S({0} {1})",sum.Min,sum.Max);
					Console.WriteLine("-______O({0} {1})",maxother,minother);
					Console.WriteLine("-______>DW {0} to {1} {2} I am {3}",t,sum.Min-maxother,sum.Max-minother,sum);
					*/
					if (UpdateInterval(sum.Terms[i],sum.Min-maxother,sum.Max-minother)) {
						AddChanged(sum.Terms[i]);
						changed = true;
						anychange = true;
					}
				}					
			}
			while(changed);
			return anychange;
		}

        public bool Visit(AutoDiff.Variable variable) {
			return false;
		}
		
		public bool Visit(Reification reif) {
			bool c = false;		
			if (reif.Max < reif.MaxVal) {
				c = UpdateInterval(reif,reif.MinVal,reif.MinVal);
				if(c) AddChanged(reif);
				if(UpdateInterval(reif.Condition,Double.NegativeInfinity,0)) {
					AddChanged(reif.Condition);
					c = true;
				}
			} else if (reif.Min > reif.MinVal) {
				c = UpdateInterval(reif,reif.MaxVal,reif.MaxVal);
				if (c) AddChanged(reif);
				if(UpdateInterval(reif.Condition,1,1)) {
					AddChanged(reif.Condition);
					c = true;
				}
			}
			
			return c;
		}

        public bool Visit(Log log) {
			double a = Math.Exp(log.Min);
			double b = Math.Exp(log.Max);
			if ( UpdateInterval(log.Arg,a,b)) {
				AddChanged(log.Arg);
				return true;
			}
			return false;
			
		}
        public bool Visit(Sin sin) {
			if (sin.Min == -1.0 && sin.Max == 1.0) return false;
			double cdist = sin.Arg.Max - sin.Arg.Min;
			if(cdist >= Math.PI) return false;
//Console.WriteLine("Sine Prop Sine interval: [{0}, {1}]",sin.Min,sin.Max);			
//Console.WriteLine("Arg interval: [{0}, {1}]",sin.Arg.Min,sin.Arg.Max);
			double a = Math.Asin(sin.Min);
			double b = Math.Asin(sin.Max); //-pi/2..pi/2
			double t;
			if (a > b) {
				t = b;
				b = a;
				a = t;
			} //now a<= b; 
			
			double c = Math.PI - b;
			double d = Math.PI - a;
			
			double n1 = Math.Ceiling((sin.Arg.Min - a) / (2*Math.PI));
			//double n1a = Math.Floor((sin.Arg.Max - a) / (2*Math.PI));
			double n2 = Math.Floor((sin.Arg.Max - b)   /   (2*Math.PI));
			//double n2a = Math.Ceiling((sin.Arg.Min - b)   /   (2*Math.PI));
			double n3 = Math.Ceiling((sin.Arg.Min - c) / (2*Math.PI));
			//double n3a = Math.Floor((sin.Arg.Max - c) / (2*Math.PI));			
			double n4 = Math.Floor((sin.Arg.Max - d)   /   (2*Math.PI));
			//double n4a = Math.Ceiling((sin.Arg.Min - d)   /   (2*Math.PI));
//Console.WriteLine("N: {0} {1} {2} {3}",n1,n2,n3,n4);
//Console.WriteLine("P: {0} {1} {2} {3}",n1*2*Math.PI+a,n2*2*Math.PI+b,n3*2*Math.PI+c,n4*2*Math.PI+d);
			double min = Double.MaxValue;
			double max = Double.MinValue;
			double n1a = n1*2*Math.PI +a;
			double n2b = n2*2*Math.PI +b;
			bool faulty = true;
			if (n1a <= sin.Arg.Max && n2b >= sin.Arg.Min) { //interval 1 completely enclosed
				min = Math.Min(min,n1a);
				max = Math.Max(max,n2b);
				faulty = false;
			} else { //no bound is inside as adding interval is smaller than pi
			}
			
			double n3c = n3*2*Math.PI +c;
			double n4d = n4*2*Math.PI +d;
			
			if (n3c <= sin.Arg.Max && n4d >= sin.Arg.Min) { //interval 2 completely enclosed
				min = Math.Min(min,n3c);
				max = Math.Max(max,n4d);
				faulty = false;
			} else { //no bound is inside as adding interval is smaller than pi
			}
			
			if (faulty) {
				throw new UnsolveableException();
				//return false; //UpdateInterval(sin.Arg,sin.Arg.Max,sin.Arg.Min); //no solution possible
			}
			/*if (n1 == n2) { //bound within interval
				min = Math.Min(min,n1*2*Math.PI+a);
				max = Math.Max(max,n2*2*Math.PI+b);
			} else {
				if (n1 > n2) { //lower bound cut
					
					min = Math.Min(min,sin.Arg.Min);
					max = Math.Max(max,n2*2*Math.PI+b);
					
					double k = 
					
				}
			}
			
			//if (n1 == n2 && n3 == n4) { //bind to rectangle:
				double min = Math.Min(n1*2*Math.PI+a,n3*2*Math.PI+c);
				double max = Math.Max(n2*2*Math.PI+b,n4*2*Math.PI+d);
			*/	
			//}
			if (min == Double.MaxValue) min = Double.NegativeInfinity;
			if (max == Double.MinValue) max = Double.PositiveInfinity;
			if(UpdateInterval(sin.Arg,min,max)) {
				AddChanged(sin.Arg);
				return true;
			}
			return false;
						
		}

        public bool Visit(Cos cos) {
			if (cos.Min == -1.0 && cos.Max == 1.0) return false;
			double cdist = cos.Arg.Max - cos.Arg.Min;
			if(cdist >= Math.PI) return false;
//Console.WriteLine("Cos Prop Sine interval: [{0}, {1}]",cos.Min,cos.Max);			
//Console.WriteLine("Arg interval: [{0}, {1}]",cos.Arg.Min,cos.Arg.Max);
			double a = Math.Acos(cos.Min);
			double b = Math.Acos(cos.Max); //0..pi
			double t;
			if (a > b) {
				t = b;
				b = a;
				a = t;
			} //now a<= b; 
			
			double c = - b;
			double d = - a;
			
			double n1 = Math.Ceiling((cos.Arg.Min - a) / (2*Math.PI));
			//double n1a = Math.Floor((sin.Arg.Max - a) / (2*Math.PI));
			double n2 = Math.Floor((cos.Arg.Max - b)   /   (2*Math.PI));
			//double n2a = Math.Ceiling((sin.Arg.Min - b)   /   (2*Math.PI));
			double n3 = Math.Ceiling((cos.Arg.Min - c) / (2*Math.PI));
			//double n3a = Math.Floor((sin.Arg.Max - c) / (2*Math.PI));			
			double n4 = Math.Floor((cos.Arg.Max - d)   /   (2*Math.PI));
			//double n4a = Math.Ceiling((sin.Arg.Min - d)   /   (2*Math.PI));
//Console.WriteLine("N: {0} {1} {2} {3}",n1,n2,n3,n4);
//Console.WriteLine("P: {0} {1} {2} {3}",n1*2*Math.PI+a,n2*2*Math.PI+b,n3*2*Math.PI+c,n4*2*Math.PI+d);
			double min = Double.MaxValue;
			double max = Double.MinValue;
			double n1a = n1*2*Math.PI +a;
			double n2b = n2*2*Math.PI +b;
			bool faulty = true;
			if (n1a <= cos.Arg.Max && n2b >= cos.Arg.Min) { //interval 1 completely enclosed
				min = Math.Min(min,n1a);
				max = Math.Max(max,n2b);
				faulty = false;
			} else { //no bound is inside as adding interval is smaller than pi
			}
			
			double n3c = n3*2*Math.PI +c;
			double n4d = n4*2*Math.PI +d;
			
			if (n3c <= cos.Arg.Max && n4d >= cos.Arg.Min) { //interval 2 completely enclosed
				min = Math.Min(min,n3c);
				max = Math.Max(max,n4d);
				faulty = false;
			} else { //no bound is inside as adding interval is smaller than pi
			}
			if (faulty) {
				throw new UnsolveableException();
				//return false;//return UpdateInterval(cos.Arg,cos.Arg.Max,cos.Arg.Min); //no solution possible
			}
			
			
			if (min == Double.MaxValue) min = Double.MinValue;
			if (max == Double.MinValue) max = Double.MaxValue;
			if(UpdateInterval(cos.Arg,min,max)) {
				AddChanged(cos.Arg);
				return true;
			}
			return false;
		}

        public bool Visit(Abs abs) {
			if(UpdateInterval(abs.Arg,-abs.Max,abs.Max)) {
				AddChanged(abs.Arg);
				return true;
			}
			return false;
		}
		
 
        public bool Visit(Exp exp) {
			double a = Math.Log(exp.Min);
			double b = Math.Log(exp.Max);
			if(UpdateInterval(exp.Arg,a,b)) {
				AddChanged(exp.Arg);
				return true;
			}
			return false;
		}
		public bool Visit(Atan2 atan2) {
			throw new NotImplementedException("Atan2 propagation not implemented");
			//return false;
		}
				
		protected void OutputChange(Term t,double oldmin, double oldmax) {
			//Console.WriteLine("DW: Interval of {0} is now [{1}, {2}]",t,t.Min,t.Max);
			double oldwidth = oldmax - oldmin;
			double newwidth = t.Max - t.Min;
			if(t is AutoDiff.Variable) Console.WriteLine("DW shrinking [{0}..{1}] to [{2}..{3}] by {4} ({5}%)",oldmin,oldmax,t.Min,t.Max,oldwidth-newwidth,(oldwidth-newwidth)/oldwidth*100);
		}
		protected bool UpdateInterval(Term t, double min, double max) {
			bool ret = t.Min < min || t.Max > max;
#if DEBUG_DP
			double oldmin = t.Min;
			double oldmax = t.Max;
#endif 			
			if(!Double.IsNaN(min)) t.Min = Math.Max(t.Min,min);
			if(!Double.IsNaN(max)) t.Max = Math.Min(t.Max,max);
			if(ret) IntervalPropagator.updates++;
			IntervalPropagator.visits++;
#if DEBUG_DP
			if (ret) OutputChange(t,oldmin,oldmax);
#endif
			if (t.Min > t.Max) throw new UnsolveableException();
			return ret;
		}
		
	}
	
}