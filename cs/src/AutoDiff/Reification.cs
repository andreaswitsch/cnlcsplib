using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
	[Serializable]
	 /// <summary>
    /// Represents a reified constraint.
    /// </summary>
    public class Reification : Term
    {
       
        public Reification(Term condition, double min,double max)
        {
            Condition = condition;
			NegatedCondition = condition.Negate();
            MinVal = min;
			MaxVal = max;
        }
		/// <summary>
		/// Gets or sets the constraint to reify
		/// </summary>
		/// <value>
		/// The constraint.
		/// </value>
        public Term Condition { get; private set; }
		/// <summary>
		/// The constraint's negation. Set automatically.
		/// </summary>
		/// <value>
		/// The negated constraint.
		/// </value>
		public Term NegatedCondition { get; private set; }
		/// <summary>
		/// Gets or sets the minimum.
		/// </summary>
		/// <value>
		/// The value representing a violated constraint.
		/// </value>
	    public double MinVal { get; private set; }
		/// <summary>
		/// Gets or sets the maximum.
		/// </summary>
		/// <value>
		/// The value representing a satisfied constraint.
		/// </value>
		public double MaxVal { get; private set; }

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
			return string.Format("Discretizer( {0}, {1}, {2} )",Condition,Min, Max);
		}
		
		public override Term AggregateConstants()
		{
			return this;
		}
		public override Term Derivative(Variable v)
		{
			throw new Exception("Symbolic Derivation of Discretizer not supported.");
		}

    }
}
