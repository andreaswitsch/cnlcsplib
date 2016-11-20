using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
    public class TermPower : Term
    {
        public TermPower(Term baseTerm, Term exponent)
        {
            Base = baseTerm;
            Exponent = exponent;
        }

        /// <summary>
        /// Gets the base term of the power function
        /// </summary>
        public Term Base { get; private set; }

        /// <summary>
        /// Gets the exponent term of the power function.
        /// </summary>
        public Term Exponent { get; private set; }

        /// <summary>
        /// Accepts a term visitor.
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
			return string.Format("termPower( {0}, {1} )", Base, Exponent);
		}
		
		public override Term AggregateConstants ()
		{
			Base = Base.AggregateConstants();
			Exponent = Exponent.AggregateConstants();
			if (Exponent is Zero) {
				return 1;
			}
			if (Base is Constant && Exponent is Constant) {
				return Math.Pow ((Base as Constant).Value, (Exponent as Constant).Value);
			} else if (Base is Zero) {				
				return Base;
			} else if (Base is TermPower) {
				Exponent *= (Base as TermPower).Exponent;
				Base = (Base as TermPower).Base;
				return this;
			  } else {
				return this;
			}
		}
		public override Term Derivative(Variable v)
		{
			return new TermPower(this.Base,this.Exponent-1)*(this.Exponent*this.Base.Derivative(v)+this.Base*(new Log(this.Base))*this.Exponent.Derivative(v));
		}
    }
}
