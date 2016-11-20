using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
    /// <summary>
    /// Represents a atan2 function 
    /// </summary>
    [Serializable]
    public class Atan2 : Term
    {
        /// <summary>
        /// Constructs a new instance of the <see cref="Max"/> type.
        /// </summary>
        /// <param name="left">The first max term</param>
        /// <param name="right">The second max term</param>
        public Atan2(Term left, Term right)
        {
            Left = left;
            Right = right;
        }

   		/// <summary>
        /// Gets the first atan2 term.
        /// </summary>
        public Term Left { get; private set; }

        /// <summary>
        /// Gets the second atan2 term.
        /// </summary>
        public Term Right { get; private set; }

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
			return string.Format("atan2( {0}, {1} )", Left, Right);
		}
		
		public override Term AggregateConstants()
		{
			Left = Left.AggregateConstants ();
			Right = Right.AggregateConstants ();
			if (Left is Constant && Right is Constant) {
				return Math.Atan2((Left as Constant).Value, (Right as Constant).Value);
			} else {
				return this;
			}
		}
		public override Term Derivative(Variable v)
		{
			Term t = this.Left*this.Right.Derivative(v) - this.Right*this.Left.Derivative(v);
			return t/ (new ConstPower(this.Left,2) + 	new ConstPower(this.Right,2));
		}
    }
}
