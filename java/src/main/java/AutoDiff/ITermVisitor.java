//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

/**
* A visitor for the terms that has a result from its computations
* The type of the computation results
*/
public interface ITermVisitor <TResult>  
{
    /**
    * Computes a value for a constant term.
    * 
    *  @param constant The input term.
    *  @return The result of the computation.
    */
    TResult visit(Constant constant) throws Exception ;

    /**
    * Computes a value for a zero term.
    * 
    *  @param zero The input term.
    *  @return The result of the computation.
    */
    TResult visit(Zero zero) throws Exception ;

    /**
    * Computes a value for a power term.
    * 
    *  @param power The input term.
    *  @return The result of the computation.
    */
    TResult visit(ConstPower power) throws Exception ;

    /**
    * Computes a value for a power term.
    * 
    *  @param power The input term.
    *  @return The result of the computation.
    */
    TResult visit(TermPower power) throws Exception ;

    /**
    * Computes a value for a product term.
    * 
    *  @param product The input term.
    *  @return The result of the computation.
    */
    TResult visit(Product product) throws Exception ;

    /**
    * Computes a value for a sum term.
    * 
    *  @param sum The input term.
    *  @return The result of the computation.
    */
    TResult visit(Sum sum) throws Exception ;

    /**
    * Computes a value for a variable term.
    * 
    *  @param variable The input term.
    *  @return The result of the computation.
    */
    TResult visit(Variable variable) throws Exception ;

    /**
    * Computes a value for a logarithm term.
    * 
    *  @param log The input term.
    *  @return The result of the computation.
    */
    TResult visit(Log log) throws Exception ;

    /**
    * Computes a value for an exponential function term.
    * 
    *  @param exp The input term.
    *  @return The result of the computation.
    */
    TResult visit(Exp exp) throws Exception ;

    /**
    * Additions by Carpe Noctem:
    * 
    * Computes a value for a sigmoid term.
    * 
    *  @param sigmoid The input term.
    *  @return The result of the computation.
    */
    TResult visit(Sigmoid sigmoid) throws Exception ;

    /**
    * Computes a value for a sigmoid term.
    * 
    *  @param sigmoid The input term.
    *  @return The result of the computation.
    */
    TResult visit(LinSigmoid sigmoid) throws Exception ;

    /**
    * Computes a value for a less-than constraint term.
    * 
    *  @param constraint The input term.
    *  @return The result of the computation.
    */
    TResult visit(LTConstraint constraint) throws Exception ;

    /**
    * Computes a value for a less-than-or-equal constraint term.
    * 
    *  @param constraint The input term.
    *  @return The result of the computation.
    */
    TResult visit(LTEConstraint constraint) throws Exception ;

    /**
    * Computes a value for a min term.
    * 
    *  @param sigmoid The input term.
    *  @return The result of the computation.
    */
    TResult visit(Min min) throws Exception ;

    /**
    * Computes a value for a max term.
    * 
    *  @param max The input term.
    *  @return The result of the computation.
    */
    TResult visit(Max max) throws Exception ;

    /**
    * Computes a value for an and term.
    * 
    *  @param sigmoid The input term.
    *  @return The result of the computation.
    */
    TResult visit(And and) throws Exception ;

    /**
    * Computes a value for an or term.
    * 
    *  @param max The input term.
    *  @return The result of the computation.
    */
    TResult visit(Or or) throws Exception ;

    /**
    * Computes a value for a ConstraintUtility term.
    * 
    *  @param cu The input term.
    *  @return The result of the computation.
    */
    TResult visit(ConstraintUtility cu) throws Exception ;

    /**
    * Computes a value for a sine term.
    * 
    *  @param sin The input term.
    *  @return The result of the computation.
    */
    TResult visit(Sin sin) throws Exception ;

    /**
    * Computes a value for a cosine term.
    * 
    *  @param cos The input term.
    *  @return The result of the computation.
    */
    TResult visit(Cos cos) throws Exception ;

    /**
    * Computes a value for an exponential function term.
    * 
    *  @param abs The input term.
    *  @return The result of the computation.
    */
    TResult visit(Abs abs) throws Exception ;

    TResult visit(Atan2 atan2) throws Exception ;

    TResult visit(Reification r) throws Exception ;

    TResult visit(Negation r) throws Exception ;

}


