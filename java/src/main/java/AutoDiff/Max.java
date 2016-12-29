//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

/**
* Represents a max function as used by the constraint solver.
*/
public class Max  extends Term 
{
    /**
    * Constructs a new instance of the 
    *  {@link #Max}
    *  type.
    * 
    *  @param left The first max term
    *  @param right The second max term
    */
    public Max(Term left, Term right) throws Exception {
        setLeft(left);
        setRight(right);
    }

    /**
    * Gets the first product term.
    */
    private Term Left;
    public Term getLeft() {
        return Left;
    }

    public void setLeft(Term value) {
        Left = value;
    }

    /**
    * Gets the second product term.
    */
    private Term Right;
    public Term getRight() {
        return Right;
    }

    public void setRight(Term value) {
        Right = value;
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
        return new And(getLeft().negate(), getRight().negate());
    }

    public String toString() {
        try
        {
            return String.format("max( %s, %s )", getLeft(), getRight());
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
        if (getLeft() == True)
            return getLeft();
         
        setRight(getRight().aggregateConstants());
        if (getLeft() == False)
            return getRight();
         
        if (getRight() == True)
            return getRight();
         
        if (getRight() == False)
            return getLeft();
         
        if (getLeft() instanceof Constant && getRight() instanceof Constant)
        {
            return TermBuilder.Constant(Math.max((getLeft() instanceof Constant ? (Constant)getLeft() : (Constant)null).getValue(),
                (getRight() instanceof Constant ? (Constant)getRight() : (Constant)null).getValue()));
        }
        else
        {
            return this;
        } 
    }

    public Term derivative(Variable v) throws Exception {
        throw new Exception("Symbolic Derivation of Max not supported.");
    }

}


