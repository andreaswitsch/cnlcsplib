//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

/**
* Represents a constraint utility function.
*/
public class ConstraintUtility  extends Term 
{
    /**
    * Constructs a new instance of the 
    *  {@link #LTConstraint}
    *  type.
    * 
    *  @param constraint The constraint term
    *  @param utility The utility term
    */
    public ConstraintUtility(Term constraint, Term utility) throws Exception {
        setConstraint(constraint);
        setUtility(utility);
    }

    /**
    * Gets the constraint.
    */
    private Term __Constraint;
    public Term getConstraint() {
        return __Constraint;
    }

    public void setConstraint(Term value) {
        __Constraint = value;
    }

    /**
    * Gets the utility.
    */
    private Term __Utility;
    public Term getUtility() {
        return __Utility;
    }

    public void setUtility(Term value) {
        __Utility = value;
    }

//    /**
//    * Accepts a term visitor
//    *
//    *  @param visitor The term visitor to accept
//    */
//    public void accept(ITermVisitor visitor) throws Exception {
//        visitor.visit(this);
//    }

    /**
    * Accepts a term visitor with a generic result
    * The type of the result from the visitor's function
    *  @param visitor The visitor to accept
    *  @return 
    * The result from the visitor's visit function.
    */
    public <TResult>TResult accept(ITermVisitor<TResult> visitor) throws Exception {
        return visitor.visit(this);
    }

    public Term negate() throws Exception {
        throw new Exception("Do not negate a Constraint Utility");
    }

    //return this;
    public Term aggregateConstants() throws Exception {
        setConstraint(getConstraint().aggregateConstants());
        setUtility(getUtility().aggregateConstants());
        return this;
    }

    public Term derivative(Variable v) throws Exception {
        throw new Exception("Symbolic Derivation of ConstraintUtility not supported.");
    }

    public String toString() {
        try
        {
            return String.format("[ConstraintUtility: Constraint=%s, Utility=%s]", getConstraint(), getUtility());
        }
        catch (RuntimeException __dummyCatchVar0)
        {
            throw __dummyCatchVar0;
        }
        catch (Exception __dummyCatchVar0)
        {
            throw new RuntimeException(__dummyCatchVar0);
        }
    
    }

}


