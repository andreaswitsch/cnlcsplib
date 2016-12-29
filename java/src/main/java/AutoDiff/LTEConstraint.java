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

    private Term NegatedForm;
    public Term getNegatedForm() {
        return NegatedForm;
    }

    public void setNegatedForm(Term value) {
        NegatedForm = value;
    }

    /**
    * Gets the smaller argument of the constraint.
    */
    private Term Left;
    public Term getLeft() {
        return Left;
    }

    public void setLeft(Term value) {
        Left = value;
    }

    /**
    * Gets the larger argument of the constraint.
    */
    private Term Right;
    public Term getRight() {
        return Right;
    }

    public void setRight(Term value) {
        Right = value;
    }

    /**
    * Gets the sigmoid's steepness.
    */
    private double Steepness;
    public double getSteepness() {
        return Steepness;
    }

    public void setSteepness(double value) {
        Steepness = value;
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


