using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
    /// <summary>
    /// Represents a product between two terms.
    /// </summary>
    [Serializable]
    public class Product : Term
    {
        /// <summary>
        /// Constructs a new instance of the <see cref="Product"/> type.
        /// </summary>
        /// <param name="left">The first product term</param>
        /// <param name="right">The second product term</param>
        public Product(Term left, Term right)
        {
            Left = left;
            Right = right;
        }

        /// <summary>
        /// Gets the first product term.
        /// </summary>
        public Term Left { get; private set; }

        /// <summary>
        /// Gets the second product term.
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
			return string.Format("( {0} * {1} )", Left, Right);
		}
		
		public override Term AggregateConstants()
		{
			Left = Left.AggregateConstants();
			Right = Right.AggregateConstants();
			if (Left is Constant && Right is Constant) {
				return (Left as Constant).Value * (Right as Constant).Value;
			} else if (Left is Zero) {
				return Left;
			} else if (Right is Zero) {
				return Right;			
			} 
			if (Left is Constant) {
				if (((Constant)Left).Value == 1) {
					return Right;
				}
			}
			if (Right is Constant) {
				if (((Constant)Right).Value == 1) {
					return Left;
				}			
			}
			return this;			
		}
		public override Term Derivative(Variable v)
		{
			return this.Left*this.Right.Derivative(v) + this.Right*this.Left.Derivative(v);
		}
    }
}
