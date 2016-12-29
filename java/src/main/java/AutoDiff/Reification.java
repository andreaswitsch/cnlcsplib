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
    private Term Condition;
    public Term getCondition() {
        return Condition;
    }

    public void setCondition(Term value) {
        Condition = value;
    }

    /**
    * The constraint's negation. Set automatically.
    * 
    * The negated constraint.
    */
    private Term NegatedCondition;
    public Term getNegatedCondition() {
        return NegatedCondition;
    }

    public void setNegatedCondition(Term value) {
        NegatedCondition = value;
    }

    /**
    * Gets or sets the minimum.
    * 
    * The value representing a violated constraint.
    */
    private double MinVal;
    public double getMinVal() {
        return MinVal;
    }

    public void setMinVal(double value) {
        MinVal = value;
    }

    /**
    * Gets or sets the maximum.
    * 
    * The value representing a satisfied constraint.
    */
    private double MaxVal;
    public double getMaxVal() {
        return MaxVal;
    }

    public void setMaxVal(double value) {
        MaxVal = value;
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
        catch (RuntimeException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            throw new RuntimeException(exception);
        }
    
    }

    public Term aggregateConstants() throws Exception {
        return this;
    }

    public Term derivative(Variable v) throws Exception {
        throw new Exception("Symbolic Derivation of Discretizer not supported.");
    }

}


