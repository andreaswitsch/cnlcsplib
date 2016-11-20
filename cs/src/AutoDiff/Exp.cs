using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
    /// <summary>
    /// Represents the exponential function <c>e^x</c>
    /// </summary>
    [Serializable]
    public class Exp : Term
    {
        /// <summary>
        /// Constructs a new instance of the <see cref="Exp"/> type.
        /// </summary>
        /// <param name="arg">The exponent of the function.</param>
        public Exp(Term arg)
        {
            Arg = arg;
        }

        /// <summary>
        /// Gets the exponent term.
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
			return string.Format ("exp( {0} )", Arg);
		}
		
		public override Term AggregateConstants()
		{
			Arg = Arg.AggregateConstants();
			if (Arg is Constant) {
				return Math.Exp ((Arg as Constant).Value);
			} else {
				if (Arg is Zero) return 1;
				return this;
			}
			
		}
		public override Term Derivative(Variable v)
		{
			return this * this.Arg.Derivative(v);
		}
    }
}
