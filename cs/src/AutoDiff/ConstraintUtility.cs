using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
    /// <summary>
    /// Represents a constraint utility function.
    /// </summary>
    [Serializable]
    public class ConstraintUtility : Term
    {
        /// <summary>
        /// Constructs a new instance of the <see cref="LTConstraint"/> type.
        /// </summary>
        /// <param name="constraint">The constraint term</param>
        /// <param name="utility">The utility term</param>        
        public ConstraintUtility(Term constraint, Term utility)
        {
            Constraint = constraint;
            Utility = utility;			
        }

        /// <summary>
        /// Gets the constraint.
        /// </summary>
        public Term Constraint { get; private set; }

        /// <summary>
        /// Gets the utility.
        /// </summary>
        public Term Utility { get; private set; }
		
		
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
		public override Term Negate ()
		{
			throw new Exception("Do not negate a Constraint Utility");
			//return this;
		}
		
		public override Term AggregateConstants ()
		{
			Constraint = Constraint.AggregateConstants ();
			Utility = Utility.AggregateConstants ();
			return this;
		}
		public override Term Derivative (Variable v)
		{
			throw new Exception ("Symbolic Derivation of ConstraintUtility not supported.");
		}
		
		public override string ToString ()
		{
			return string.Format ("[ConstraintUtility: Constraint={0}, Utility={1}]", Constraint, Utility);
		}
    }
}
