using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using CompileResult = AutoDiff.Compiled.TapeElement;

namespace AutoDiff
{
    partial class CompiledDifferentiator
    {
        private class Compiler : ITermVisitor<int> // int --> the index of the compiled element in the tape
        {
            private readonly List<Compiled.TapeElement> tape;
            private readonly Dictionary<Term, int> indexOf;

            public Compiler(Variable[] variables, List<Compiled.TapeElement> tape)
            {
                this.tape = tape;
                indexOf = new Dictionary<Term, int>();
				//Replacement for Linq code -- HS
				for(int i=0; i<variables.Length;i++) {
					indexOf[variables[i]] = i;
					tape.Add(new Compiled.Variable());
				}
                /*foreach (var i in Enumerable.Range(0, variables.Length))
                {
                    indexOf[variables[i]] = i;
                    tape.Add(new Compiled.Variable());
                }*/
            }

            public void Compile(Term term)
            {
                term.Accept(this);
            }

            public int Visit(Constant constant)
            {
                return Compile(constant, () => new Compiled.Constant(constant.Value) { Inputs = new Compiled.InputEdge[0] });
            }

            public int Visit(Zero zero)
            {
                return Compile(zero, () => new Compiled.Constant(0) { Inputs = new Compiled.InputEdge[0] });
            }

            public int Visit(ConstPower intPower)
            {
                return Compile(intPower, () =>
                    {
                        var baseIndex = intPower.Base.Accept(this);
                        var element = new Compiled.ConstPower
                        {
                            Base = baseIndex,
                            Exponent = intPower.Exponent,
                            Inputs = new Compiled.InputEdge[] 
                            {
                                new Compiled.InputEdge { Index = baseIndex },
                            },
                        };

                        return element;
                    });
            }

            public int Visit(TermPower power)
            {
                return Compile(power, () =>
                {
                    var baseIndex = power.Base.Accept(this);
                    var expIndex = power.Exponent.Accept(this);
                    var element = new Compiled.TermPower
                    {
                        Base = baseIndex,
                        Exponent = expIndex,
                        Inputs = new Compiled.InputEdge[]
                        {
                            new Compiled.InputEdge { Index = baseIndex },
                            new Compiled.InputEdge { Index = expIndex },
                        },
                    };

                    return element;
                });
            }

            public int Visit(Product product)
            {
                return Compile(product, () =>
                    {
                        var leftIndex = product.Left.Accept(this);
                        var rightIndex = product.Right.Accept(this);
                        var element = new Compiled.Product
                        {
                            Left = leftIndex,
                            Right = rightIndex,
                            Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = leftIndex },
                                new Compiled.InputEdge { Index = rightIndex },
                            }
                        };

                        return element;
                    });
            }

            public int Visit(Sum sum)
            {
                return Compile(sum, () =>
                    {
						//replacement for linq code -- HS:
						int[] indices = new int[sum.Terms.Count];
						Compiled.InputEdge[] inputs = new Compiled.InputEdge[indices.Length];
						for(int i=0; i<indices.Length; i++) {
							indices[i] = sum.Terms[i].Accept(this);
							inputs[i] = new Compiled.InputEdge { Index = indices[i] };
						}
						
					
						var element = new Compiled.Sum {
							Terms = indices,
							Inputs = inputs,
						};
                        return element;
                    });
            }
			
			public int Visit(Gp gp)
            {
                return Compile(gp, () =>
                    {
						int[] indices = new int[gp.Args.Length];
						Compiled.InputEdge[] inputs = new Compiled.InputEdge[indices.Length];
						for(int i=0; i<indices.Length; i++) {
							indices[i] = gp.Args[i].Accept(this);
							inputs[i] = new Compiled.InputEdge { Index = indices[i] };
						}
					
						var element = new Compiled.Gp {
							Terms = indices,
							Inputs = inputs,
							Gpr = gp.Gpr,
							dc = gp.DivCount,
						};
                        return element;
                    });
            }

            public int Visit(Variable variable)
            {
                return indexOf[variable];
            }

            public int Visit(Log log)
            {
                return Compile(log, () =>
                    {
                        var argIndex = log.Arg.Accept(this);
                        var element = new Compiled.Log 
                        { 
                            Arg = argIndex,
                            Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = argIndex },
                            },
                        };

                        return element;
                    });
            }

            public int Visit(Exp exp)
            {
                return Compile(exp, () =>
                    {
                        var argIndex = exp.Arg.Accept(this);
                        var element = new Compiled.Exp
                        {
                            Arg = argIndex,
                            Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = argIndex },
                            },
                        };

                        return element;
                    });
            }
            

            private int Compile(Term term, Func<CompileResult> compiler)
            {
                int index;
                if (!indexOf.TryGetValue(term, out index))
                {
                    var compileResult = compiler();
                    tape.Add(compileResult);

                    index = tape.Count - 1;
                    indexOf.Add(term, index);
                }

                return index;
            }
			
			//Additions by Carpe Noctem:
			public int Visit(Min min)
            {
                return Compile(min, () =>
                    {
                        var leftIndex = min.Left.Accept(this);
                        var rightIndex = min.Right.Accept(this);
                        var element = new Compiled.Min
                        {
                            Left = leftIndex,
                            Right = rightIndex,
							Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = leftIndex },
                                new Compiled.InputEdge { Index = rightIndex },
                            }
                        };
						return element;
                        
                    });
            }
			public int Visit(Max max)
            {
                return Compile(max, () =>
                    {
                        var leftIndex = max.Left.Accept(this);
                        var rightIndex = max.Right.Accept(this);
                        var element = new Compiled.Max
                        {
                            Left = leftIndex,
                            Right = rightIndex,
							Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = leftIndex },
                                new Compiled.InputEdge { Index = rightIndex },
                            }
                        };
						return element;
                    });
            }
			public int Visit(Reification dis)
            {
                return Compile(dis, () =>
                    {                        
						var conIndex   = dis.Condition.Accept(this);
						var negConIndex = dis.NegatedCondition.Accept(this);
                        var element = new Compiled.Reification
                        {
                            Min = dis.Min,
                            Max = dis.Max,
							Condition = conIndex,
							NegatedCondition = negConIndex,
							Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = conIndex },
                                new Compiled.InputEdge { Index = negConIndex },
                            }
                        };
						return element;                        
                    });
            }
			public int Visit(And and)
            {
                return Compile(and, () =>
                    {
                        var leftIndex = and.Left.Accept(this);
                        var rightIndex = and.Right.Accept(this);
                        var element = new Compiled.And
                        {
                            Left = leftIndex,
                            Right = rightIndex,
							Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = leftIndex },
                                new Compiled.InputEdge { Index = rightIndex },
                            }
                        };
						return element;
                     });
            }
			public int Visit(Or or)
            {
                return Compile(or, () =>
                    {
                        var leftIndex = or.Left.Accept(this);
                        var rightIndex = or.Right.Accept(this);
                        var element = new Compiled.Or
                        {
                            Left = leftIndex,
                            Right = rightIndex,
                        	Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = leftIndex },
                                new Compiled.InputEdge { Index = rightIndex },
                            }
                        };
						return element;
                    });
            }
			public int Visit(ConstraintUtility cu)
            {
                return Compile(cu, () =>
                    {
						var constraint = cu.Constraint.Accept(this);
                        var util = cu.Utility.Accept(this);
                        
                        var element = new Compiled.ConstraintUtility
                        {
                            Constraint = constraint,
                            Utility = util,
                        	Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = constraint },
                                new Compiled.InputEdge { Index = util },
                            }
                        };
						return element;
                    });
            }
			
			public int Visit(Sigmoid sigmoid)
            {
                return Compile(sigmoid, () =>
                    {
                        var argIndex = sigmoid.Arg.Accept(this);
                        var midIndex = sigmoid.Mid.Accept(this);
						
                        var element = new Compiled.Sigmoid
                        {
                            Arg = argIndex,
                            Mid = midIndex,
							Steepness = sigmoid.Steepness,
                        	Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = argIndex },
                                new Compiled.InputEdge { Index = midIndex },
                            }
                        };
						return element;
                    });
            }
			
			public int Visit(LinSigmoid sigmoid)
            {
                return Compile(sigmoid, () =>
                    {
                        var argIndex = sigmoid.Arg.Accept(this);
						
                        var element = new Compiled.Sigmoid
                        {
                            Arg = argIndex,
                        	Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = argIndex },
                            }
                        };
						return element;
                    });
            }
			
			public int Visit(LTConstraint constraint)
            {
                return Compile(constraint, () =>
                    {
                        var lIndex = constraint.Left.Accept(this);
                        var rIndex = constraint.Right.Accept(this);
						
                        var element = new Compiled.LTConstraint
                        {
                            Left = lIndex,
                            Right = rIndex,
							Steepness = constraint.Steepness,
                        	Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = lIndex },
                                new Compiled.InputEdge { Index = rIndex },
                            }
                        };
						return element;
                    });
            }
			public int Visit(LTEConstraint constraint)
            {
                return Compile(constraint, () =>
                    {
                        var lIndex = constraint.Left.Accept(this);
                        var rIndex = constraint.Right.Accept(this);
						
                        var element = new Compiled.LTEConstraint
                        {
                            Left = lIndex,
                            Right = rIndex,
							Steepness = constraint.Steepness,
                        	Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = lIndex },
                                new Compiled.InputEdge { Index = rIndex },
                            }
                        };
						return element;
                    });
            }
			public int Visit(Sin sin)
            {
                return Compile(sin, () =>
                    {
                        var argIndex = sin.Arg.Accept(this);
                        var element = new Compiled.Sin { 
							Arg = argIndex,
                            Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = argIndex },                               
                            }
                        };
						return element;
                    });
            }
			public int Visit(Cos cos)
            {
                return Compile(cos, () =>
                    {
                        var argIndex = cos.Arg.Accept(this);
                        var element = new Compiled.Cos { 
							Arg = argIndex,
                            Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = argIndex },                               
                            }
                        };
						return element;
                    });
            }
			public int Visit(Abs abs)
            {
                return Compile(abs, () =>
                    {
                        var argIndex = abs.Arg.Accept(this);
                        var element = new Compiled.Abs { 
							Arg = argIndex,
                            Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = argIndex },                               
                            }
                        };
						return element;
                    });
            }
			public int Visit(Atan2 atan2)
            {
                return Compile(atan2, () =>
                    {
                        var lIndex = atan2.Left.Accept(this);
                        var rIndex = atan2.Right.Accept(this);
						
                        var element = new Compiled.Atan2
                        {
                            Left = lIndex,
                            Right = rIndex,							
                        	Inputs = new Compiled.InputEdge[]
                            {
                                new Compiled.InputEdge { Index = lIndex },
                                new Compiled.InputEdge { Index = rIndex },
                            }
                        };
						return element;
                    });
            }

        }
    }
}
