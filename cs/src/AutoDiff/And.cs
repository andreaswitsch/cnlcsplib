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
    public class And : Term
    {
        /// <summary>
        /// Constructs a new instance of the <see cref="Min"/> type.
        /// </summary>
        /// <param name="left">The first min term</param>
        /// <param name="right">The second min term</param>
        public And(Term left, Term right)
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
		public override Term Negate()
		{
			return (Left.Negate() | Right.Negate());			
		}

		public override string ToString()
		{
			return string.Format ("and( {0}, {1} )", Left, Right);
		}
		
		public override Term AggregateConstants() 
		{
			Left = Left.AggregateConstants();
			if (Left == Term.False) return Left;			
			Right = Right.AggregateConstants();
			if (Left == Term.True) return Right;
			if (Right == Term.False) return Right;
			if (Right == Term.True) return Left;
			if (Left is Constant && Right is Constant) {
				if ((Left as Constant).Value > 0.75 && (Right as Constant).Value > 0.75)
					return Term.True;
				else 
					return Term.False;
			} else {
				return this;
			}
		}
		public override Term Derivative(Variable v)
		{
			throw new Exception("Symbolic Derivation of And not supported");
		}
    }
}
