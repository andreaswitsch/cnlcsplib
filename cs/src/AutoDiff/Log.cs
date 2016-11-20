using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
    /// <summary>
    /// Represents a natural logarithm function
    /// </summary>
    [Serializable]
    public class Log : Term
    {
        /// <summary>
        /// Constructs a new instance of the <see cref="Log"/> class.
        /// </summary>
        /// <param name="arg">The argument of the natural logarithm</param>
        public Log(Term arg)
        {
            Arg = arg;
        }

        /// <summary>
        /// Accepts a terms visitor
        /// </summary>
        /// <param name="visitor">The term visitor to accept</param>
        public override void Accept(ITermVisitor visitor)
        {
            visitor.Visit(this);
        }

        /// <summary>
        /// Gets the natural logarithm argument.
        /// </summary>
        public Term Arg { get; private set; }

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
			return string.Format ("log( {0} )", Arg);
		}
		
		public override Term AggregateConstants()
		{
			Arg = Arg.AggregateConstants();
			if (Arg is Constant) {
				return Math.Log ((Arg as Constant).Value);
			} else {
				return this;
			}
		}
		public override Term Derivative(Variable v)
		{
			return this.Arg.Derivative(v) / this.Arg;
		}
    }
}
