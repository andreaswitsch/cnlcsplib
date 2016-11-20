using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
    /// <summary>
    /// Visitor for terms that has no result from its computations.
    /// </summary>
    public interface ITermVisitor
    {
        /// <summary>
        /// Performs an action for a constant term.
        /// </summary>
        /// <param name="constant">The input term.</param>
        void Visit(Constant constant);

        /// <summary>
        /// Performs an action for a zero term.
        /// </summary>
        /// <param name="zero">The input term.</param>
        void Visit(Zero zero);

        /// <summary>
        /// Performs an action for a constant power term.
        /// </summary>
        /// <param name="power">The input term.</param>
        void Visit(ConstPower power);

        /// <summary>
        /// Performs an action for a power term.
        /// </summary>
        /// <param name="power">The input term.</param>
        void Visit(TermPower power);

        /// <summary>
        /// Performs an action for a product term.
        /// </summary>
        /// <param name="product">The input term.</param>
        void Visit(Product product);

        /// <summary>
        /// Performs an action for a sum term.
        /// </summary>
        /// <param name="sum">The input term.</param>
        void Visit(Sum sum);
		
		/// <summary>
        /// Performs an action for a Gp term.
        /// </summary>
        /// <param name="sum">The input term.</param>
        void Visit(Gp gp);

        /// <summary>
        /// Performs an action for a variable term.
        /// </summary>
        /// <param name="variable">The input term.</param>
        void Visit(Variable variable);

        /// <summary>
        /// Performs an action for a logarithm term.
        /// </summary>
        /// <param name="log">The input term.</param>
        void Visit(Log log);

        /// <summary>
        /// Performs an action for an exponential function term.
        /// </summary>
        /// <param name="exp">The input term.</param>
        void Visit(Exp exp);
		
		///Additions by Carpe Noctem:
		
		/// <summary>
        /// Performs an action for a sigmoid term.
        /// </summary>
        /// <param name="sigmoid">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(Sigmoid sigmoid);
		
		/// <summary>
        /// Performs an action for a sigmoid term.
        /// </summary>
        /// <param name="sigmoid">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(LinSigmoid sigmoid);
		
		/// <summary>
        /// Performs an action for a less-than constraint.
        /// </summary>
        /// <param name="constraint">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(LTConstraint constraint);	
		
		/// <summary>
        /// Performs an action for a less-than-or-equal constraint.
        /// </summary>
        /// <param name="constraint">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(LTEConstraint constraint);	
		
		/// <summary>
        /// Performs an action for a min term.
        /// </summary>
        /// <param name="min">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(Min min);
		
		
		/// <summary>
        /// Performs an action for a max term.
        /// </summary>
        /// <param name="max">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(Max max);		
		
		/// <summary>
        /// Performs an action for an and term.
        /// </summary>
        /// <param name="min">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(And and);
		
		/// <summary>
        /// Performs an action for an or term.
        /// </summary>
        /// <param name="min">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(Or or);
		
		/// <summary>
        /// Performs an action for a ConstraintUtility term.
        /// </summary>
        /// <param name="cu">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(ConstraintUtility cu);	
       
		 /// <summary>
        /// Performs an action for a sine term.
        /// </summary>
        /// <param name="sin">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(Sin sin);		

        /// <summary>
        /// Performs an action for a cosine term.
        /// </summary>
        /// <param name="cos">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(Cos cos);		

        /// <summary>
        /// Performs an action for an abs term.
        /// </summary>
        /// <param name="abs">The input term.</param>
        /// <returns>The result of the computation.</returns>
        void Visit(Abs abs);	
		
		/// <summary>
		/// Performs an action for an atan2 term.
		/// </summary>
		/// <param name='atan2'>
		/// Atan2.
		/// </param>
		void Visit(Atan2 atan2);
		
		/// <summary>
		/// Performs an action for a reified constraint
		/// </summary>
		/// <param name='r'>
		/// R.
		/// </param>
		void Visit(Reification r);
		
		
    }
}
