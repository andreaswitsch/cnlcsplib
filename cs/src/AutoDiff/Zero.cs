using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
    /// <summary>
    /// A constant zero term. Similar to <see cref="Constant"/> but represents only the value 0.
    /// </summary>
    [Serializable]
    public class Zero : Term
    {
		

        /// <summary>
        /// Accepts a term visitor
        /// </summary>
        /// <param name="visitor">The term visitor to accept.</param>
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
			return "0";
		}
		
		public override Term AggregateConstants()
		{
			return this;
		}
		public override Term Derivative(Variable v)
		{
			return this;
		}
		public override bool Equals (object obj)
		{
			if (obj is Zero) return true;
			return false;
			
		}
    }
}
