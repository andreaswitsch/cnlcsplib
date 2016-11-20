using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
    /// <summary>
    /// Represents a less than constraint function.
    /// </summary>
    [Serializable]
    public class LTEConstraint : Term
    {
        /// <summary>
        /// Constructs a new instance of the <see cref="LTConstraint"/> type.
        /// </summary>
        /// <param name="x">The smaller term</param>
        /// <param name="y">The larger term</param>
        /// <param name="steepness">The steepness of the sigmoid</param>
        public LTEConstraint(Term x, Term y, double steepness)
        {
            Left = x;
            Right = y;
			Steepness = steepness;
        }
		internal LTEConstraint(Term x, Term y, double steepness, Term negatedForm) : this(x,y,steepness) {
			this.NegatedForm = negatedForm;	
		}
		public Term NegatedForm {
			get;
			private set;
		}

        /// <summary>
        /// Gets the smaller argument of the constraint.
        /// </summary>
        public Term Left { get; private set; }

        /// <summary>
        /// Gets the larger argument of the constraint.
        /// </summary>
        public Term Right { get; private set; }

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
		public override Term Negate()
		{
			if (this.NegatedForm == null) {
				this.NegatedForm = new LTConstraint(Right,Left,Steepness,this);
			}
			return this.NegatedForm;
		}
		
		public override string ToString()
		{
			return string.Format("{0} <= {1}", Left, Right);
		}
		
		public override Term AggregateConstants()
		{
			Left = Left.AggregateConstants();
			Right = Right.AggregateConstants();
			if (Left is Constant && Right is Constant) {
				if ((Left as Constant).Value <= (Right as Constant).Value)
					return Term.True;
				else
					return Term.False;
			} else {
				return this;
			}
		}
		public override Term Derivative(Variable v)
		{
			throw new Exception("Symbolic Derivation of Less-Than-Or-Equal not supported.");
		}

    }
}
