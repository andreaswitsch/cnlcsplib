using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UFGP;
using DotNetMatrix;

namespace AutoDiff
{
    /// <summary>
    /// Represents the exponential function <c>e^x</c>
    /// </summary>
    [Serializable]
    public class Gp : Term
    {
        /// <summary>
        /// Constructs a new instance of the <see cref="Exp"/> type.
        /// </summary>
        /// <param name="arg">The exponent of the function.</param>
        public Gp(Term[] args, GaussianProcess gp, int dc)
        {
			DivCount = dc;
            Args = args;
			Gpr = gp;
			//GeneralMatrix cur = new GeneralMatrix(0, args.Length);
        }

		/// <summary>
        /// Accepts a term visitor
        /// </summary>
        /// <param name="visitor">The term visitor to accept</param>
        public override void Accept(ITermVisitor visitor)
        {
            visitor.Visit(this);
        }

        /// <summary>
        /// Accepts a term visitor with a generic result
        /// </summary>
        /// <typeparam name="TResult">The type of the result from the visitor's function</typeparam>
        /// <param name="visitor">The visitor to accept</param>
        /// <returns>
        /// The result from the visitor's visit function.
        /// </returns>
        public override TResult Accept<TResult>(ITermVisitor<TResult> visitor)
        {
            return visitor.Visit(this);
        }
		
		
		public override Term AggregateConstants()
		{
			return this;
		}
		
        /// <summary>
        /// Gets the exponent term.
        /// </summary>
        public Term[] Args { get; private set; }
		public int DivCount { get; private set; }
		public GaussianProcess Gpr { get; private set; }

        public override string ToString()
		{
			return Gpr.ToString();
		}
		
		public double Eval() {
			/*for(int i=0; i<args.Length; i++) {
				cur.SetElement(0,i, (Variables[i] as Constant).Value);
			}
			return Gpr.Evaluate(cur, X);*/
			return 0;
		}
		
		public override Term Derivative(Variable v)
		{
			throw new NotImplementedException();
			/*int d=0;
			for(int i=0; i<Variables.Length; i++) {
				if(Variables[i]==v) {
					d=i;
				}
				// Wie krieg ich hier bitte den wert von v?
				cur.SetElement(0,i, ValueOf((Variables[i]));
			}
			return Gpr.PartialDerivative(cur, X, d);*/
		}
    }
}
