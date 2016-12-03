//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:34
//

package AutoDiff;

//using System.Diagnostics.Contracts;
/**
* Static methods that operate on terms.
*/
public class TermUtils   
{
    /**
    * Creates a compiled representation of a given term that allows efficient evaluation of the value/gradient.
    * 
    *  @param term The term to compile.
    *  @param variables The variables contained in the term.
    *  @return A compiled representation of 
    *  {@code term}
    *  that assigns values to variables in the same order
    * as in 
    *  {@code variables}
    * 
    * The order of the variables in 
    *  {@code variables}
    *  is important. Each call to 
    *  {@code ICompiledTerm.Evaluate}
    *  or
    * 
    *  {@code ICompiledTerm.Differentiate}
    *  receives an array of numbers representing the point of evaluation. The i'th number in this array corresponds
    * to the i'th variable in 
    *  {@code variables}
    * .
    */
    public static ICompiledTerm compile(Term term, Variable... variables) throws Exception {
        return new CompiledDifferentiator(term, variables);
    }

    /**
    * Creates a compiled representation of a given term that allows efficient evaluation of the value/gradient where part of the variables serve as function
    * inputs and other variables serve as constant parameters.
    * 
    *  @param term The term to compile.
    *  @param variables The variables contained in the term.
    *  @param parameters The constant parameters in the term.
    *  @return A compiled representation of 
    *  {@code term}
    *  that assigns values to variables in the same order
    * as in 
    *  {@code variables}
    *  and 
    *  {@code parameters}
    * 
    * The order of the variables in 
    *  {@code variables}
    *  is important. Each call to 
    *  {@code ICompiledTerm.Evaluate}
    *  or
    * 
    *  {@code ICompiledTerm.Differentiate}
    *  receives an array of numbers representing the point of evaluation. The i'th number in this array corresponds
    * to the i'th variable in 
    *  {@code variables}
    * .
    */
    public static IParametricCompiledTerm compile(Term term, Variable[] variables, Variable[] parameters) throws Exception {
        return new ParametricCompiledTerm(term, variables, parameters);
    }

    /*
                Contract.Requires(variables != null);
                Contract.Requires(parameters != null);
                Contract.Requires(term != null);
                Contract.Ensures(Contract.Result<IParametricCompiledTerm>() != null);
                Contract.Ensures(Contract.Result<IParametricCompiledTerm>().Variables.Count == variables.Length);
                Contract.Ensures(Contract.ForAll(0, variables.Length, i => variables[i] == Contract.Result<IParametricCompiledTerm>().Variables[i]));
                Contract.Ensures(Contract.Result<IParametricCompiledTerm>().Parameters.Count == parameters.Length);
                Contract.Ensures(Contract.ForAll(0, parameters.Length, i => parameters[i] == Contract.Result<IParametricCompiledTerm>().Parameters[i]));
    			*/
    /**
    * Evaluates the function represented by a given term at a given point.
    * 
    *  @param term The term representing the function to evaluate.
    *  @param variables The variables used in 
    *  {@code term}
    * .
    *  @param point The values assigned to the variables in 
    *  {@code variables}
    * 
    *  @return The value of the function represented by 
    *  {@code term}
    *  at the point represented by 
    *  {@code variables}
    * 
    * and 
    *  {@code point}
    * .The i'th value in 
    *  {@code point}
    *  corresponds to the i'th variable in 
    *  {@code variables}
    * .
    */
    public static double evaluate(Term term, Variable[] variables, double[] point) throws Exception {
        return compile(term,variables).evaluate(point);
    }

    /*
                Contract.Requires(term != null);
                Contract.Requires(variables != null);
                Contract.Requires(point != null);
                Contract.Requires(variables.Length == point.Length);
    			*/
    /**
    * Computes the gradient of the function represented by a given term at a given point.
    * 
    *  @param term The term representing the function to differentiate.
    *  @param variables The variables used in 
    *  {@code term}
    * .
    *  @param point The values assigned to the variables in 
    *  {@code variables}
    * 
    *  @return The gradient of the function represented by 
    *  {@code term}
    *  at the point represented by 
    *  {@code variables}
    * 
    * and 
    *  {@code point}
    * .The i'th value in 
    *  {@code point}
    *  corresponds to the i'th variable in 
    *  {@code variables}
    * . In addition, the i'th value
    * in the resulting array is the partial derivative with respect to the i'th variable in 
    *  {@code variables}
    * .
    */
    public static Double[] differentiate(Term term, Variable[] variables, double[] point) throws Exception {
        Double[] result = compile(term, variables).differentiate(point).getItem1();
        return result;
    }

}


