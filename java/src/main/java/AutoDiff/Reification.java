//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:34
//

package AutoDiff;

/**
* Represents a reified constraint.
*/
public class Reification  extends Term 
{
    public Reification(Term condition, double min, double max) throws Exception {
        setCondition(condition);
        setNegatedCondition(condition.negate());
        setMinVal(min);
        setMaxVal(max);
    }

    /**
    * Gets or sets the constraint to reify
    * 
    * The constraint.
    */
    private Term __Condition;
    public Term getCondition() {
        return __Condition;
    }

    public void setCondition(Term value) {
        __Condition = value;
    }

    /**
    * The constraint's negation. Set automatically.
    * 
    * The negated constraint.
    */
    private Term __NegatedCondition;
    public Term getNegatedCondition() {
        return __NegatedCondition;
    }

    public void setNegatedCondition(Term value) {
        __NegatedCondition = value;
    }

    /**
    * Gets or sets the minimum.
    * 
    * The value representing a violated constraint.
    */
    private double __MinVal;
    public double getMinVal() {
        return __MinVal;
    }

    public void setMinVal(double value) {
        __MinVal = value;
    }

    /**
    * Gets or sets the maximum.
    * 
    * The value representing a satisfied constraint.
    */
    private double __MaxVal;
    public double getMaxVal() {
        return __MaxVal;
    }

    public void setMaxVal(double value) {
        __MaxVal = value;
    }
//
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

    public String toString() {
        try
        {
            return String.format("Discretizer( %s, %f, %f )", getCondition(), Min, Max);
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

    public Term aggregateConstants() throws Exception {
        return this;
    }

    public Term derivative(Variable v) throws Exception {
        throw new Exception("Symbolic Derivation of Discretizer not supported.");
    }

}


