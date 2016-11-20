using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
    /// <summary>
    /// Represents a sigmoidal function.
    /// </summary>
    [Serializable]
    public class Sigmoid : Term
    {
        /// <summary>
        /// Constructs a new instance of the <see cref="Sigmoid"/> type.
        /// </summary>
        /// <param name="arg">The argument of the sigmoid</param>
        /// <param name="mid">The midpoint of the sigmoid, where its value equals .5</param>
        /// <param name="steepness">The steepness of the sigmoid</param>
        public Sigmoid(Term arg, Term mid, double steepness)
        {
            Arg = arg;
            Mid = mid;
			Steepness = steepness;
        }

		public Sigmoid(Term arg, Term mid)
        {
            Arg = arg;
            Mid = mid;
			Steepness = 1;
        }
		
        /// <summary>
        /// Gets the Argument of the sigmoid.
        /// </summary>
        public Term Arg { get; private set; }

        /// <summary>
        /// Gets the Midpoint of the sigmoid.
        /// </summary>
        public Term Mid { get; private set; }

		/// <summary>
        /// Gets the sigmoid's steepness.
        /// </summary>
        public double Steepness { get; private set; }
		
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
        
        public override string ToString()
		{
			return string.Format("sigmoid( {0}, {1}, {2} )", Arg, Mid, Steepness);
		}
		
		public override Term AggregateConstants ()
		{
			Arg = Arg.AggregateConstants();
			Mid = Mid.AggregateConstants();
			if (Arg is Constant && Mid is Constant) {
				double e = Math.Exp (Steepness * (-(Arg as Constant).Value + (Mid as Constant).Value));
				if (Double.IsPositiveInfinity (e)) {
					return Term.Epsilon;
					//Console.WriteLine("FUCKUP {0}",e);
				} else {
					e = 1.0 / (1.0 + e);
				}
				if (e < Term.Epsilon) {
					return Term.Epsilon;
				} else {
					return e;
				}
			} else {
				return this;
			}
		}
		public override Term Derivative(Variable v)
		{
			Term t = Steepness * (this.Arg.Derivative(v) - this.Mid.Derivative(v)) * new Exp(this.Steepness * (-this.Arg+this.Mid));
			return t/ new ConstPower((new Exp(this.Steepness * this.Arg) + new Exp(this.Steepness * this.Mid)),2);			
		}
		
    }
}
