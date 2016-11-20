using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff
{
    /// <summary>
    /// A visitor for the terms that has a result from its computations
    /// </summary>
    /// <typeparam name="TResult">The type of the computation results</typeparam>
    public interface ITermVisitor<TResult>
    {
        /// <summary>
        /// Computes a value for a constant term.
        /// </summary>
        /// <param name="constant">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Constant constant);

        /// <summary>
        /// Computes a value for a zero term.
        /// </summary>
        /// <param name="zero">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Zero zero);

        /// <summary>
        /// Computes a value for a power term.
        /// </summary>
        /// <param name="power">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(ConstPower power);

        /// <summary>
        /// Computes a value for a power term.
        /// </summary>
        /// <param name="power">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(TermPower power);

        /// <summary>
        /// Computes a value for a product term.
        /// </summary>
        /// <param name="product">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Product product);

        /// <summary>
        /// Computes a value for a sum term.
        /// </summary>
        /// <param name="sum">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Sum sum);
		
		/// <summary>
        /// Computes a value for a Gp term.
        /// </summary>
        /// <param name="Gp">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Gp gp);

        /// <summary>
        /// Computes a value for a variable term.
        /// </summary>
        /// <param name="variable">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Variable variable);

        /// <summary>
        /// Computes a value for a logarithm term.
        /// </summary>
        /// <param name="log">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Log log);

        /// <summary>
        /// Computes a value for an exponential function term.
        /// </summary>
        /// <param name="exp">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Exp exp);
		
		
		///Additions by Carpe Noctem:
		
		/// <summary>
        /// Computes a value for a sigmoid term.
        /// </summary>
        /// <param name="sigmoid">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Sigmoid sigmoid);
		
		/// <summary>
        /// Computes a value for a sigmoid term.
        /// </summary>
        /// <param name="sigmoid">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(LinSigmoid sigmoid);
		
		
		/// <summary>
        /// Computes a value for a less-than constraint term.
        /// </summary>
        /// <param name="constraint">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(LTConstraint constraint);
		
		/// <summary>
        /// Computes a value for a less-than-or-equal constraint term.
        /// </summary>
        /// <param name="constraint">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(LTEConstraint constraint);
		
		/// <summary>
        /// Computes a value for a min term.
        /// </summary>
        /// <param name="sigmoid">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Min min);
		
		/// <summary>
        /// Computes a value for a max term.
        /// </summary>
        /// <param name="max">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Max max);
		
		/// <summary>
        /// Computes a value for an and term.
        /// </summary>
        /// <param name="sigmoid">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(And and);
		
		/// <summary>
        /// Computes a value for an or term.
        /// </summary>
        /// <param name="max">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Or or);
		
		/// <summary>
        /// Computes a value for a ConstraintUtility term.
        /// </summary>
        /// <param name="cu">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(ConstraintUtility cu);
		
		
        /// <summary>
        /// Computes a value for a sine term.
        /// </summary>
        /// <param name="sin">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Sin sin);
		
		/// <summary>
        /// Computes a value for a cosine term.
        /// </summary>
        /// <param name="cos">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Cos cos);
       
		
		/// <summary>
        /// Computes a value for an exponential function term.
        /// </summary>
        /// <param name="abs">The input term.</param>
        /// <returns>The result of the computation.</returns>
        TResult Visit(Abs abs);
		
		TResult Visit(Atan2 atan2);
		
		TResult Visit(Reification r);
      
    }
}
