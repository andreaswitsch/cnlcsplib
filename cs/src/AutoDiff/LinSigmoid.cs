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
    public class LinSigmoid : Term
    {
        /// <summary>
        /// Constructs a new instance of the <see cref="Sigmoid"/> type.
        /// </summary>
        /// <param name="arg">The argument of the sigmoid</param>
        /// <param name="mid">The midpoint of the sigmoid, where its value equals .5</param>
        /// <param name="steepness">The steepness of the sigmoid</param>
        public LinSigmoid(Term arg)
        {
            Arg = arg;
        }

        /// <summary>
        /// Gets the Argument of the sigmoid.
        /// </summary>
        public Term Arg { get; private set; }

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
			return string.Format("sigmoid( {0})", Arg);
		}
		
		public override Term AggregateConstants ()
		{
			Arg = Arg.AggregateConstants();
			if (Arg is Constant) {
				double e = Math.Exp ((-(Arg as Constant).Value));
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
			return this.Arg.Derivative(v);			
		}
		
    }
}
