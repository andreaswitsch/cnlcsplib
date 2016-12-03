//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

/**
* Represents a less than constraint function.
*/
public class LTEConstraint  extends Term 
{
    /**
    * Constructs a LTEConstraint
    * 
    *  @param x The smaller term
    *  @param y The larger term
    *  @param steepness The steepness of the sigmoid
    */
    public LTEConstraint(Term x, Term y, double steepness) throws Exception {
        setLeft(x);
        setRight(y);
        setSteepness(steepness);
    }

    public LTEConstraint(Term x, Term y, double steepness, Term negatedForm) throws Exception {
        this(x, y, steepness);
        this.setNegatedForm(negatedForm);
    }

    private Term __NegatedForm;
    public Term getNegatedForm() {
        return __NegatedForm;
    }

    public void setNegatedForm(Term value) {
        __NegatedForm = value;
    }

    /**
    * Gets the smaller argument of the constraint.
    */
    private Term __Left;
    public Term getLeft() {
        return __Left;
    }

    public void setLeft(Term value) {
        __Left = value;
    }

    /**
    * Gets the larger argument of the constraint.
    */
    private Term __Right;
    public Term getRight() {
        return __Right;
    }

    public void setRight(Term value) {
        __Right = value;
    }

    /**
    * Gets the sigmoid's steepness.
    */
    private double __Steepness;
    public double getSteepness() {
        return __Steepness;
    }

    public void setSteepness(double value) {
        __Steepness = value;
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

    public Term negate() throws Exception {
        if (this.getNegatedForm() == null)
        {
            this.setNegatedForm(new LTConstraint(getRight(),getLeft(),getSteepness(),this));
        }
         
        return this.getNegatedForm();
    }

    public String toString() {
        try
        {
            return String.format("%s <= %s", getLeft(), getRight());
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
        setLeft(getLeft().aggregateConstants());
        setRight(getRight().aggregateConstants());
        if (getLeft() instanceof Constant && getRight() instanceof Constant)
        {
            if ((getLeft() instanceof Constant ? (Constant)getLeft() : (Constant)null).getValue() <= (getRight() instanceof Constant ? (Constant)getRight() : (Constant)null).getValue())
                return True;
            else
                return False;
        }
        else
        {
            return this;
        } 
    }

    public Term derivative(Variable v) throws Exception {
        throw new Exception("Symbolic Derivation of Less-Than-Or-Equal not supported.");
    }

}


