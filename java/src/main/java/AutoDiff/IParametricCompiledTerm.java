//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

/**
* Represents a parametric term after it has been compiled for efficient evaluation/differentiation. A parametric
* term has some variables that function as "constant parameters" and others that function as actual variables.
*/
public interface IParametricCompiledTerm   
{
    /**
    * Evaluates the compiled term at the given point.
    * 
    *  @param arg The point at which to evaluate..
    *  @param parameters The parameter values
    *  @return The value of the function represented by the term at the given point.The number at 
    *  {@code arg[i]}
    *  is the value assigned to the variable 
    *  {@code Variables[i]}
    * .
    */
    double evaluate(double[] arg, double[] parameters) throws Exception ;

    /**
    * Computes the gradient of the compiled term at the given point.
    * 
    *  @param arg The point at which to differentiate.
    *  @param parameters The parameter values
    *  @return A tuple, where the first item is the gradient at 
    *  {@code arg}
    *  and the second item is
    * the value at 
    *  {@code arg}
    * . That is, the second value is the same as running 
    *  {@link #evaluate}
    *  on
    * 
    *  {@code arg}
    *  and 
    *  {@code parameters}
    * .The number at 
    *  {@code arg[i]}
    *  is the value assigned to the variable 
    *  {@code Variables[i]}
    * .
    */
    Tuple<Double[],Double> differentiate(double[] arg, double[] parameters) throws Exception ;

    /**
    * The collection of variables contained in this compiled term.
    * 
    * The order of variables in this collection specifies the meaning of each argument in 
    *  {@link #differentiate}
    *  or
    * 
    *  {@link #evaluate}
    * . That is, the variable at 
    *  {@code Variables[i]}
    *  corresponds to the i-th element in the 
    *  {@code arg}
    *  parameter of 
    *  {@link #differentiate}
    * 
    * and 
    *  {@link #evaluate}
    * .
    */
    Variable[] getVariables() throws Exception ;

    /**
    * The collection of parameter variables contained in this compiled term.
    * 
    * The order of variables in this collection specifies the meaning of each argument in 
    *  {@link #differentiate}
    *  or
    * 
    *  {@link #evaluate}
    * . That is, the variable at 
    *  {@code Variables[i]}
    *  corresponds to the i-th element in the 
    *  {@code parameters}
    *  parameter of 
    *  {@link #differentiate}
    * 
    * and 
    *  {@link #evaluate}
    * .
    */
    Variable[] getParameters() throws Exception ;

}


