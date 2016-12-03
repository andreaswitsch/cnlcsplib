//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

/**
* Represents a term after it has been compiled for efficient evaluation/differentiation.
*/
public interface ICompiledTerm   
{
    /**
    * Evaluates the compiled term at the given point.
    * 
    *  @param arg The point at which to evaluate.
    *  @return The value of the function represented by the term at the given point.The number at 
    *  {@code arg[i]}
    *  is the value assigned to the variable 
    *  {@code Variables[i]}
    * .
    */
    double evaluate(double... arg) throws Exception ;

    /**
    * Computes the gradient of the compiled term at the given point.
    * 
    *  @param arg The point at which to differentiate.
    *  @return A tuple, where the first item is the gradient at 
    *  {@code arg}
    *  and the second item is
    * the value at 
    *  {@code arg1}
    * . That is, the second value is the same as running 
    *  {@link #evaluate}
    *  on
    * 
    *  {@code arg}
    * .The number at 
    *  {@code arg[i]}
    *  is the value assigned to the variable 
    *  {@code Variables[i]}
    * .
    */
    Tuple<Double[],Double> differentiate(double... arg) throws Exception ;

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
    *  corresponds to the i-th parameter of 
    *  {@link #differentiate}
    * 
    * and 
    *  {@link #evaluate}
    * .
    */
    Variable[] getVariables() throws Exception ;

}


