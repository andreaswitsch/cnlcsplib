using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using DotNetMatrix;

namespace AutoDiff
{
    partial class CompiledDifferentiator
    {
        private class ForwardSweepVisitor : Compiled.ITapeVisitor
        {
            private Compiled.TapeElement[] tape;

            public ForwardSweepVisitor(Compiled.TapeElement[] tape)
            {
                this.tape = tape;
            }

            public void Visit(Compiled.Constant elem)
            {
            }

            public void Visit(Compiled.Exp elem)
            {
                elem.Value = Math.Exp(ValueOf(elem.Arg));
                elem.Inputs[0].Weight = elem.Value;
            }

            public void Visit(Compiled.Log elem)
            {
                double arg = ValueOf(elem.Arg);
                elem.Value = Math.Log(arg);
                elem.Inputs[0].Weight = 1 / arg;
            }

            public void Visit(Compiled.ConstPower elem)
            {
                double baseVal = ValueOf(elem.Base);
				//modified to remove one Math.Pow -- HS
				double r = Math.Pow(baseVal, elem.Exponent - 1);
				elem.Value = r*baseVal;
				elem.Inputs[0].Weight = elem.Exponent * r;
                //elem.Value = Math.Pow(baseVal, elem.Exponent);
                //elem.Inputs[0].Weight = elem.Exponent * Math.Pow(baseVal, elem.Exponent - 1);
            }

            public void Visit(Compiled.TermPower elem)
            {
                double baseVal = ValueOf(elem.Base);
                double exponent = ValueOf(elem.Exponent);

                elem.Value = Math.Pow(baseVal, exponent);
                elem.Inputs[0].Weight = exponent * Math.Pow(baseVal, exponent - 1);
                elem.Inputs[1].Weight = elem.Value * Math.Log(baseVal);
            }

            public void Visit(Compiled.Product elem)
            {
                double left = ValueOf(elem.Left);
                double right = ValueOf(elem.Right);

                elem.Value = left * right;
                elem.Inputs[0].Weight = right;
                elem.Inputs[1].Weight = left;
            }

            public void Visit(Compiled.Sum elem)
            {
                elem.Value = 0;
                for (int i = 0; i < elem.Terms.Length; ++i)
                    elem.Value += ValueOf(elem.Terms[i]);

                for (int i = 0; i < elem.Inputs.Length; ++i)
                    elem.Inputs[i].Weight = 1;
            }
			
			public void Visit(Compiled.Gp elem)
            {
				GeneralMatrix cur = new GeneralMatrix(1, elem.Terms.Length);
				for (int i = 0; i < elem.Terms.Length; ++i) {
					cur.SetElement(0, i, ValueOf(elem.Terms[i]));
				}
				elem.Value = elem.Gpr.Evaluate(cur).GetElement(0,0);

                for (int i = 0; i < elem.Inputs.Length; ++i) {
                    if(i<elem.dc) {
						//elem.Inputs[i].Weight = elem.Gpr.PartialDerivative(cur, i);
						cur.SetElement(0, i, cur.GetElement(0,i)+0.1);
						elem.Inputs[i].Weight = (elem.Gpr.Evaluate(cur).GetElement(0,0)-elem.Value)/0.1;
						cur.SetElement(0, i, cur.GetElement(0,i)-0.1);
					}
					else elem.Inputs[i].Weight = 0;
				}
				
				
				//Here Starts a dirty Hack
				/*double sum=0;
				for(int n=0; n<elem.Inputs.Length; n++) {
					sum += elem.Inputs[n].Weight;
				}
				if(sum < 0.00001) {
					double val = elem.Value;
					
					double minDist = Double.MaxValue;
					int iMin=-1;
					double dist = 0, tmp;
					for(int n=0; n<elem.X.RowDimension; n++) {
						if(elem.Gpr.Y.GetElement(0, n) > val) {
							dist = 0;
							for(int m=0; m<elem.X.ColumnDimension; m++) {
								tmp = elem.X.GetElement(n, m) - cur.GetElement(0, m);
								dist += tmp*tmp;
							}
							if(dist < minDist) {
								minDist = dist;
								iMin = n;
							}
						}
					}
					if(iMin>=0) {
						for(int m=0; m<elem.X.ColumnDimension; m++) {
							elem.Inputs[m].Weight = elem.X.GetElement(m, iMin) - cur.GetElement(0, m);
						}
					}
				}*/
            }

            public void Visit(Compiled.Variable var)
            {
            }
			/*
            public void Visit(Compiled.UnaryFunc elem)
            {
                double arg = ValueOf(elem.Arg);
                elem.Value = elem.Eval(arg);
                elem.Inputs[0].Weight = elem.Diff(arg);
            }

            public void Visit(Compiled.BinaryFunc elem)
            {
                double left = ValueOf(elem.Left);
                double right = ValueOf(elem.Right);

                elem.Value = elem.Eval(left, right);
                var grad = elem.Diff(left, right);
                elem.Inputs[0].Weight = grad.Item1;
                elem.Inputs[1].Weight = grad.Item2;
            }

            public void Visit(Compiled.NaryFunc elem)
            {
                double[] args = new double[elem.Terms.Length];
                for (int i = 0; i < args.Length; i++)
                    args[i] = ValueOf(elem.Terms[i]);

                elem.Value = elem.Eval(args);
                var grad = elem.Diff(args);
                for (int i = 0; i < grad.Length; ++i)
                    elem.Inputs[i].Weight = grad[i];
            }
			*/
            private double ValueOf(int index)
            {
                return tape[index].Value;
            }
			///Additions By Carpe Noctem:
			public void Visit(Compiled.Sin elem) {
				double arg = ValueOf(elem.Arg);
                elem.Value = Math.Sin(arg);
                elem.Inputs[0].Weight = Math.Cos(arg);
			}
			public void Visit(Compiled.Cos elem) {
				double arg = ValueOf(elem.Arg);
                elem.Value = Math.Cos(arg);
                elem.Inputs[0].Weight = - Math.Sin(arg);
			}
			public void Visit(Compiled.Abs elem) {
				double arg = ValueOf(elem.Arg);
                elem.Value = Math.Abs(arg);
                if(arg > 0) {
					elem.Inputs[0].Weight = 1;	
				} else {
					elem.Inputs[0].Weight = -1;
				}
			}
			public void Visit(Compiled.Reification elem) {
				double condition = ValueOf(elem.Condition);
				//double negcondition = ValueOf(elem.NegatedCondition);
				double d = elem.Max - elem.Min;
				if(condition > 0) {
					elem.Value = elem.Max;
					elem.Inputs[0].Weight = 0;					
					elem.Inputs[0].Weight = -d;
				} else {
					elem.Value = elem.Min;
					elem.Inputs[0].Weight = d;					
					elem.Inputs[0].Weight = 0;
				}
			}
			public void Visit(Compiled.Min elem)
            {
                double left = ValueOf(elem.Left);
                double right = ValueOf(elem.Right);
				
                elem.Value = Math.Min(left, right);
                if (left < right) {
                	elem.Inputs[0].Weight = 1;
                	elem.Inputs[1].Weight = 0;
				} else {
                	elem.Inputs[0].Weight = 0;
                	elem.Inputs[1].Weight = 1;
				}
            }
			public void Visit(Compiled.Max elem)
            {
                double left = ValueOf(elem.Left);
                double right = ValueOf(elem.Right);

                elem.Value = Math.Max(left, right);
                if (left > right) {
                	elem.Inputs[0].Weight = 1;
                	elem.Inputs[1].Weight = 0;
				} else {
                	elem.Inputs[0].Weight = 0;
                	elem.Inputs[1].Weight = 1;
				}
            }
			public void Visit(Compiled.And elem)
            {
                double left = ValueOf(elem.Left);
                double right = ValueOf(elem.Right);
				if (left > 0.75 && right > 0.75) {
					elem.Value=1;
					elem.Inputs[0].Weight = 0;
                	elem.Inputs[1].Weight = 0;
					return;
				}
				elem.Value = 0;
                if (left <= 0) {
					elem.Value += left;
					elem.Inputs[0].Weight = 1;
				}
				if (right <= 0) {
					elem.Value += right;
					elem.Inputs[1].Weight = 1;					
				}
            }
			public void Visit(Compiled.Or elem)
            {
                double left = ValueOf(elem.Left);
                double right = ValueOf(elem.Right);
				if (left > 0.75 || right > 0.75) {
					elem.Value=1;
					elem.Inputs[0].Weight = 0;
                	elem.Inputs[1].Weight = 0;
					return;
				}
				elem.Value = 0;              
				if(left <= 0) {
					elem.Value += left;
					elem.Inputs[0].Weight = 1;
				} else {
					elem.Inputs[0].Weight = 0;
				}
				if (right <= 0) {
					elem.Value += right;
					elem.Inputs[1].Weight = 1;
				} else {
					elem.Inputs[1].Weight = 0;
				}
            }
			public void Visit(Compiled.Sigmoid elem)
            {
                double arg = ValueOf(elem.Arg);
                double mid = ValueOf(elem.Mid);
				double e = Math.Exp(elem.Steepness*(-arg + mid));
				if (Double.IsPositiveInfinity(e)) {
					elem.Value = Term.Epsilon;					
				} else {
                	elem.Value = 1.0 / (1.0 + e);
				}
				if (elem.Value < Term.Epsilon) elem.Value = Term.Epsilon;
				if (e==0.0 || Double.IsPositiveInfinity(e)) {
					elem.Inputs[0].Weight= elem.Steepness * Term.Epsilon;
					elem.Inputs[1].Weight= -elem.Steepness * Term.Epsilon;
					return;
				}
				double e2 = elem.Steepness * e / ((e+1)*(e+1));
				elem.Inputs[0].Weight = e2;
				elem.Inputs[1].Weight = -e2;
            }
			public void Visit(Compiled.LinSigmoid elem)
            {
                double arg = ValueOf(elem.Arg);
				double e = Math.Exp(-arg);
				if (Double.IsPositiveInfinity(e)) {
					elem.Value = Term.Epsilon;					
				} else {
                	elem.Value = 1.0 / (1.0 + e);
				}
				if (elem.Value < Term.Epsilon) elem.Value = Term.Epsilon;
				if (e==0.0 || Double.IsPositiveInfinity(e)) {
					elem.Inputs[0].Weight= Term.Epsilon;
					elem.Inputs[1].Weight= -Term.Epsilon;
					return;
				}
				double e2 = e / ((e+1)*(e+1));
				elem.Inputs[0].Weight = e2;
				elem.Inputs[1].Weight = -e2;
            }
			
			public void Visit(Compiled.LTConstraint elem)
            {
                double left = ValueOf(elem.Left);
                double right = ValueOf(elem.Right);
				if (left < right) {
					elem.Value=1;
					elem.Inputs[0].Weight = 0;
                	elem.Inputs[1].Weight = 0;					
				}
				else {
					elem.Value = elem.Steepness*(right - left);
					elem.Inputs[0].Weight = -elem.Steepness;
                	elem.Inputs[1].Weight = elem.Steepness;
				}				
            }
			public void Visit(Compiled.LTEConstraint elem)
            {
                double left = ValueOf(elem.Left);
                double right = ValueOf(elem.Right);
				if (left <= right) {
					elem.Value=1;
					elem.Inputs[0].Weight = 0;
                	elem.Inputs[1].Weight = 0;					
				}
				else {
					elem.Value = elem.Steepness*(right - left);
					elem.Inputs[0].Weight = -elem.Steepness;
                	elem.Inputs[1].Weight = elem.Steepness;
				}				
            }
			public void Visit(Compiled.ConstraintUtility elem)
            {
                double constraint = ValueOf(elem.Constraint);
                //double utility = ValueOf(elem.Utility);
				if (constraint > 0) {
					elem.Value = ValueOf(elem.Utility); //utility;
					elem.Inputs[0].Weight = 0;
                	elem.Inputs[1].Weight = 1;					
				}
				else {
					elem.Value = constraint;
					elem.Inputs[0].Weight = 1;
                	elem.Inputs[1].Weight = 0;
				}				
            }
			public void Visit(Compiled.Atan2 elem)
            {
                double left = ValueOf(elem.Left);
                double right = ValueOf(elem.Right);
				elem.Value=Math.Atan2(left,right);
				double denom =left*left + right*right;
				elem.Inputs[0].Weight = -right/denom;
                elem.Inputs[1].Weight = left/denom;							
            }
			
        }
    }
}