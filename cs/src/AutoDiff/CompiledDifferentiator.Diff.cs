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
        private class DiffVisitor : Compiled.ITapeVisitor
        {
            private readonly Compiled.TapeElement[] tape;
            public double LocalDerivative;
            public int ArgumentIndex;
			private static double Epsilon = 10E-5;
			
            public DiffVisitor(Compiled.TapeElement[] tape)
            {
                this.tape = tape;
            }

            public void Visit(Compiled.Constant elem)
            {

            }

            public void Visit(Compiled.Exp elem)
            {
                LocalDerivative = elem.Adjoint * elem.Value;
            }

            public void Visit(Compiled.Log elem)
            {
                LocalDerivative = elem.Adjoint / ValueOf(elem.Arg);
            }

            public void Visit(Compiled.ConstPower elem)
            {
                LocalDerivative = elem.Adjoint * elem.Exponent * Math.Pow(ValueOf(elem.Base), elem.Exponent - 1);
            }

            public void Visit(Compiled.TermPower elem)
            {
                Debug.Assert(ArgumentIndex == 0 || ArgumentIndex == 1);

                if (ArgumentIndex == 0)
                {
                    var exponent = ValueOf(elem.Exponent);
                    LocalDerivative = elem.Adjoint * exponent * Math.Pow(ValueOf(elem.Base), exponent - 1);
                }
                else
                {
                    var baseValue = ValueOf(elem.Base);
                    LocalDerivative = elem.Adjoint * Math.Pow(baseValue, ValueOf(elem.Exponent)) * Math.Log(baseValue);
                }
            }

            public void Visit(Compiled.Product elem)
            {
                Debug.Assert(ArgumentIndex == 0 || ArgumentIndex == 1);
                if (ArgumentIndex == 0)
                    LocalDerivative = elem.Adjoint * ValueOf(elem.Right);
                else
                    LocalDerivative = elem.Adjoint * ValueOf(elem.Left);
            }
            

            public void Visit(Compiled.Sum elem)
            {
                LocalDerivative = elem.Adjoint;
            }
			
			public void Visit(Compiled.Gp elem)
            {
				GeneralMatrix cur = new GeneralMatrix(1, elem.Terms.Length);
				for (int i = 0; i < elem.Terms.Length; ++i) {
					cur.SetElement(0, i, ValueOf(elem.Terms[i]));
				}
				LocalDerivative = elem.Gpr.PartialDerivative(cur, ArgumentIndex);
            }

            
            public void Visit(Compiled.Variable var)
            {
            }

            private double ValueOf(int index)
            {
                return tape[index].Value;
            }
			
			///Additions by Carpe Noctem:
			
			public void Visit(Compiled.Sin elem)
            {
                LocalDerivative = elem.Adjoint * Math.Cos(ValueOf(elem.Arg));
            }			
 			public void Visit(Compiled.Cos elem)
            {
                LocalDerivative = - elem.Adjoint * Math.Sin(ValueOf(elem.Arg));;
            }
			
			public void Visit(Compiled.Abs elem)
            {
				if (ValueOf(elem.Arg) >= 0) {
					LocalDerivative = elem.Adjoint;
				} else if (ValueOf(elem.Arg) < 0) {
					LocalDerivative = -elem.Adjoint;
				}
			}
			public void Visit(Compiled.Reification elem) {
				if (ArgumentIndex == 0) {
					if (ValueOf(elem.Condition) > 0) {						
						LocalDerivative = 0;
					}
					else {
						LocalDerivative = elem.Adjoint * (elem.Max-elem.Min); //*elem.Max/Math.Abs(ValueOf(elem.Condition));
					}
					
				} else {
					if (ValueOf(elem.Condition) <= 0) {	
						LocalDerivative = 0;
					} else {
						LocalDerivative = -elem.Adjoint * (elem.Max-elem.Min);
					}
				}
			}
			
			public void Visit(Compiled.Min elem) {
				if (ArgumentIndex == 0) {
					if (ValueOf(elem.Left) < ValueOf(elem.Right)) {
						LocalDerivative = elem.Adjoint;
					}
					else if (ValueOf(elem.Left) == ValueOf(elem.Right)) {
						if (ValueOf(elem.Left)< 0.5) LocalDerivative = elem.Adjoint;
						else LocalDerivative = 0;//elem.Derivative*.5;
					}
					else {
						LocalDerivative = 0;
					}
				} else {
					if (ValueOf(elem.Left) > ValueOf(elem.Right)) {
						LocalDerivative = elem.Adjoint;
					} else if (ValueOf(elem.Left) == ValueOf(elem.Right)) {
						LocalDerivative = 0;//elem.Derivative*.5;
					} else {
						LocalDerivative = 0;
					}
				}				
			}
			
			public void Visit(Compiled.Max elem) {
				if (ArgumentIndex == 0) {
					if (ValueOf(elem.Left) > ValueOf(elem.Right)) {
						LocalDerivative = elem.Adjoint;
					} else if (ValueOf(elem.Left) == ValueOf(elem.Right)) {
						if (ValueOf(elem.Left) <= 0.5) LocalDerivative = elem.Adjoint;
						else LocalDerivative = 0;
					}
					else LocalDerivative = 0;
				} else {
					if (ValueOf(elem.Right) > ValueOf(elem.Left)) {
						LocalDerivative = elem.Adjoint;
					} else {
						LocalDerivative = 0;
					}
				}				
			}
			
			
			public void Visit(Compiled.And elem) {
				if (ArgumentIndex == 0) {
					if (ValueOf(elem.Left) > 0.75) {
						LocalDerivative = 0;
					}
					else LocalDerivative = elem.Adjoint;					
				} else {
					if (ValueOf(elem.Right) > 0.75) {
						LocalDerivative = 0;
					} else LocalDerivative = elem.Adjoint;
				}				
			}
			public void Visit(Compiled.Or elem) {
				if (ArgumentIndex == 0) {
					if (ValueOf(elem.Right) > 0.75) {
						LocalDerivative = 0;
					} else LocalDerivative = elem.Adjoint;
				} else {
					if (ValueOf(elem.Left) > 0.75) {
						LocalDerivative = 0;
					} else {
						LocalDerivative = elem.Adjoint;
					}
				}				
			}
			
			public void Visit(Compiled.Sigmoid elem)
            {
				Debug.Assert(ArgumentIndex == 0 || ArgumentIndex == 1);
				double e = Math.Exp(elem.Steepness * (ValueOf(elem.Mid) - ValueOf(elem.Arg) ));
//Console.WriteLine("e: {0} deriv: {1}",e,elem.Derivative);				
				if (Double.IsPositiveInfinity(e) || e == 0) {
				
					if (ArgumentIndex == 0) {
						LocalDerivative = elem.Steepness * elem.Adjoint * Epsilon;
					} else {
						LocalDerivative = - elem.Steepness * elem.Adjoint * Epsilon;
					}
				} else
				if (ArgumentIndex == 0) {
					LocalDerivative = elem.Steepness * elem.Adjoint * e / ((e+1)*(e+1));
					if (Math.Abs(LocalDerivative) < Math.Abs(elem.Steepness * elem.Adjoint * Epsilon)) {
						LocalDerivative  = elem.Steepness * elem.Adjoint * Epsilon;
					}
				} else {
					LocalDerivative = -elem.Steepness * elem.Adjoint * e / ((e+1)*(e+1));
					if (Math.Abs(LocalDerivative) < Math.Abs(elem.Steepness * elem.Adjoint * Epsilon)) {
						LocalDerivative  = - elem.Steepness * elem.Adjoint * Epsilon;
					}
				}
				
            }
			
			public void Visit(Compiled.LinSigmoid elem)
            {
				Debug.Assert(ArgumentIndex == 0 || ArgumentIndex == 1);
				double e = Math.Exp((-ValueOf(elem.Arg) ));
				if (Double.IsPositiveInfinity(e) || e == 0) {
					if (ArgumentIndex == 0) {
						LocalDerivative = elem.Adjoint * Epsilon;
					} else {
						LocalDerivative = - elem.Adjoint * Epsilon;
					}
				} else
				if (ArgumentIndex == 0) {
					LocalDerivative = elem.Adjoint * e / ((e+1)*(e+1));
					if (Math.Abs(LocalDerivative) < Math.Abs(elem.Adjoint * Epsilon)) {
						LocalDerivative  = elem.Adjoint * Epsilon;
					}
				} else {
					LocalDerivative = -elem.Adjoint * e / ((e+1)*(e+1));
					if (Math.Abs(LocalDerivative) < Math.Abs(elem.Adjoint * Epsilon)) {
						LocalDerivative  = -elem.Adjoint * Epsilon;
					}
				}
				
            }
			
			public void Visit(Compiled.LTConstraint elem)
            {
				double diff = ValueOf(elem.Left) - ValueOf(elem.Right);
				if (diff < 0) {
					LocalDerivative = 0;
				}
				/*else {
					if (ArgumentIndex == 0) {
						LocalDerivative = - elem.Derivative * Math.Max(elem.Steepness,(ValueOf(elem.Left)-ValueOf(elem.Right)));	
					} else {
						LocalDerivative = elem.Derivative * Math.Max(elem.Steepness,(ValueOf(elem.Left)-ValueOf(elem.Right)));
					}
				}*/
				//Normal behaviour:
				else {
					if (ArgumentIndex == 0) {
						LocalDerivative = - elem.Steepness * elem.Adjoint;
					} else {
						LocalDerivative = elem.Steepness * elem.Adjoint;
					}
				}
				
			}
			public void Visit(Compiled.LTEConstraint elem)
            {
				double diff = ValueOf(elem.Left) - ValueOf(elem.Right);
				if (diff <= 0) {
					LocalDerivative = 0;
				}				
				//Normal behaviour:
				else {
					if (ArgumentIndex == 0) {
						LocalDerivative = - elem.Steepness * elem.Adjoint;
					} else {
						LocalDerivative = elem.Steepness * elem.Adjoint;
					}
				}
				
			}
			public void Visit(Compiled.ConstraintUtility elem)
            {
				if (ArgumentIndex == 0) {
					if (ValueOf(elem.Constraint) < 0.999) {
						LocalDerivative = elem.Adjoint;
					}
					else LocalDerivative = 0;
				} else {
					if (ValueOf(elem.Constraint) < 0.999) {
						LocalDerivative = 0;
					} else {
						LocalDerivative = ValueOf(elem.Constraint) * elem.Adjoint;
					}
				}			
			}
			public void Visit(Compiled.Atan2 elem) {
				double denom = ValueOf(elem.Left)*ValueOf(elem.Left) + ValueOf(elem.Right)*ValueOf(elem.Right);
				if (ArgumentIndex == 0) {
					LocalDerivative = - ValueOf(elem.Right)*elem.Adjoint / denom;
				} else {
					LocalDerivative = ValueOf(elem.Left)*elem.Adjoint / denom;
				}				
			}
			
			
        }
    }
}
