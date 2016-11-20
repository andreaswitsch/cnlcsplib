using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UFGP;
using DotNetMatrix;

namespace AutoDiff
{
    partial class CompiledDifferentiator
    {
        private class EvalVisitor : Compiled.ITapeVisitor
        {
            public readonly Compiled.TapeElement[] tape;

            public EvalVisitor(Compiled.TapeElement[] tape)
            {
                this.tape = tape;
            }

            public void Visit(Compiled.Constant elem)
            {
            }

            public void Visit(Compiled.Exp elem)
            {
                elem.Value = Math.Exp(ValueOf(elem.Arg));
            }

            public void Visit(Compiled.Log elem)
            {
                elem.Value = Math.Log(ValueOf(elem.Arg));
            }

            public void Visit(Compiled.ConstPower elem)
            {
                elem.Value = Math.Pow(ValueOf(elem.Base), elem.Exponent);
            }

            public void Visit(Compiled.TermPower elem)
            {
                elem.Value = Math.Pow(ValueOf(elem.Base), ValueOf(elem.Exponent));
            }

            public void Visit(Compiled.Product elem)
            {
                elem.Value = ValueOf(elem.Left) * ValueOf(elem.Right);
            }

            public void Visit(Compiled.Sum elem)
            {
                elem.Value = 0;
                for (int i = 0; i < elem.Terms.Length; ++i)
                    elem.Value += ValueOf(elem.Terms[i]);
            }
			
			public void Visit(Compiled.Gp elem)
            {
				GeneralMatrix cur = new GeneralMatrix(1, elem.Terms.Length);
				for (int i = 0; i < elem.Terms.Length; ++i) {
					cur.SetElement(0, i, ValueOf(elem.Terms[i]));
				}
				elem.Value = elem.Gpr.Evaluate(cur).GetElement(0,0);
            }

            public void Visit(Compiled.Variable var)
            {
            }
           

            private double ValueOf(int index)
            {
                return tape[index].Value;
            }
			
			//Additions by Carpe Noctem:
			
			public void Visit(Compiled.Sin elem)
            {
                elem.Value = Math.Sin(ValueOf(elem.Arg));
            }
 			public void Visit(Compiled.Cos elem)
            {
                elem.Value = Math.Cos(ValueOf(elem.Arg));
            }
			public void Visit(Compiled.Max elem)
            {
                elem.Value = Math.Max(ValueOf(elem.Left) , ValueOf(elem.Right));
            }
			public void Visit(Compiled.Min elem)
            {
                elem.Value = Math.Min(ValueOf(elem.Left) , ValueOf(elem.Right));
            }
			public void Visit(Compiled.And elem)
            {
				if(ValueOf(elem.Left) > 0.75) elem.Value = ValueOf(elem.Right);
				else if(ValueOf(elem.Right) > 0.75) elem.Value = ValueOf(elem.Left);
				else  elem.Value = ValueOf(elem.Left) + ValueOf(elem.Right);
                
            }
			public void Visit(Compiled.Or elem)
            {
				if (ValueOf(elem.Left) > 0.75) elem.Value = ValueOf(elem.Left);
				else if (ValueOf(elem.Right) > 0.75) elem.Value = ValueOf(elem.Right);
                else elem.Value = ValueOf(elem.Left) + ValueOf(elem.Right);
            }
			
			public void Visit(Compiled.Sigmoid elem)
            {
				double e = Math.Exp(elem.Steepness*(-ValueOf(elem.Arg) + ValueOf(elem.Mid)));
				if (Double.IsPositiveInfinity(e)) {
					elem.Value = Term.Epsilon;
					//Console.WriteLine("FUCKUP {0}",e);
				} else {
                	elem.Value = 1.0 / (1.0 + e);
				}
				if (elem.Value < Term.Epsilon) elem.Value = Term.Epsilon;
            }
			
			public void Visit(Compiled.LinSigmoid elem)
            {
				double e = Math.Exp((-ValueOf(elem.Arg)));
				if (Double.IsPositiveInfinity(e)) {
					elem.Value = Term.Epsilon;
					//Console.WriteLine("FUCKUP {0}",e);
				} else {
                	elem.Value = 1.0 / (1.0 + e);
				}
				if (elem.Value < Term.Epsilon) elem.Value = Term.Epsilon;
            }
			
			public void Visit(Compiled.LTConstraint elem)
            {
				if (ValueOf(elem.Left) < ValueOf(elem.Right)) {
					elem.Value = 1;
				} else {
					elem.Value = elem.Steepness*(ValueOf(elem.Right) - ValueOf(elem.Left));
				}				
            }
			public void Visit(Compiled.LTEConstraint elem)
            {
				if (ValueOf(elem.Left) <= ValueOf(elem.Right)) {
					elem.Value = 1;
				} else {
					elem.Value = elem.Steepness*(ValueOf(elem.Right) - ValueOf(elem.Left));
				}				
            }
			public void Visit(Compiled.Reification elem)
            {
				if (ValueOf(elem.Condition)>0) {
					elem.Value = elem.Max;
				} else {
					elem.Value = elem.Min;
				}				
            }
			public void Visit(Compiled.ConstraintUtility elem)
            {
				if (ValueOf(elem.Constraint) < 0.999) {
					elem.Value = ValueOf(elem.Constraint);//Math.Max(0,ValueOf(elem.Constraint));
				} else {
					elem.Value = ValueOf(elem.Constraint)*ValueOf(elem.Utility);
				}				
            }
			public void Visit(Compiled.Abs elem)
            {
                 elem.Value = Math.Abs(ValueOf(elem.Arg));
            }
			public void Visit(Compiled.Atan2 elem)
            {
                elem.Value = Math.Atan2(ValueOf(elem.Left) , ValueOf(elem.Right));
            }
        }
    }
}
