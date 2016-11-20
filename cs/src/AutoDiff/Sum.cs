using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections.ObjectModel;
using System.Diagnostics;

namespace AutoDiff
{
    /// <summary>
    /// Represents a sum of at least two terms.
    /// </summary>
    [Serializable]
    [DebuggerDisplay("Sum: {Terms.Count}")]
    public class Sum : Term
    {
        /// <summary>
        /// Constructs an instance of the <see cref="Sum"/> class.
        /// </summary>
        /// <param name="first">The first term in the sum</param>
        /// <param name="second">The second term in the sum</param>
        /// <param name="rest">The rest of the terms in the sum.</param>
        public Sum(Term first, Term second, params Term[] rest)
        {
            var allTerms = 
                (new Term[] { first, second}).Concat(rest);

            Terms = allTerms.ToList().AsReadOnly();
        }

        internal Sum(IEnumerable<Term> terms)
        {
            Terms = Array.AsReadOnly(terms.ToArray());
        }

        /// <summary>
        /// Gets the terms of this sum.
        /// </summary>
        public ReadOnlyCollection<Term> Terms { get; private set; }
        
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
			string ret = string.Format ("( {0}",Terms[0].ToString());
			for (int i = 1; i < Terms.Count; i++) {
				ret += string.Format (" + {0}", Terms[i].ToString());
			}
			return ret += " )";
		}
		
		public override Term AggregateConstants()
		{			
			Term curSummand;
			bool foundConst = false;
			double sum = 0;
			List<Term> nonConstTerms = new List<Term>();
			for (int i = 0; i < Terms.Count; i++) {
				curSummand = Terms[i].AggregateConstants();
				if (curSummand is Constant) {
					sum += (curSummand as Constant).Value;
					foundConst = true;
				} else {
					if(!(curSummand is Zero)) nonConstTerms.Add(curSummand);
				}
			}
			if (nonConstTerms.Count == 0) {
				return sum;
			} else if (!foundConst && nonConstTerms.Count == 1) {
				return nonConstTerms[0];
			}
			if (foundConst){			
				nonConstTerms.Add(sum);				
			}
			Terms = new ReadOnlyCollection<Term>(nonConstTerms);
			return this;
		}
		public override Term Derivative (Variable v)
		{
			List<Term> t = new List<Term>();
			for(int i=0; i < Terms.Count; i++) {
				t.Add(Terms[i].Derivative(v));
			}
			return new Sum(t);
		}
    }
}
