//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:34
//

package AutoDiff;

/**
* Represents a product between two terms.
*/
public class Product  extends Term 
{
    /**
    * Constructs a new instance of the 
    *  {@link #Product}
    *  type.
    * 
    *  @param left The first product term
    *  @param right The second product term
    */
    public Product(Term left, Term right) throws Exception {
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

    public String toString() {
        try
        {
            return String.format("( %s * %s )", getLeft(), getRight());
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
            return TermBuilder.Constant((getLeft() instanceof Constant ? (Constant)getLeft() : (Constant)null)
                .getValue() *
                (getRight() instanceof Constant ? (Constant)getRight() : (Constant)null).getValue());
        }
        else if (getLeft() instanceof Zero)
        {
            return getLeft();
        }
        else if (getRight() instanceof Zero)
        {
            return getRight();
        }
           
        if (getLeft() instanceof Constant)
        {
            if (((Constant)getLeft()).getValue() == 1)
            {
                return getRight();
            }
             
        }
         
        if (getRight() instanceof Constant)
        {
            if (((Constant)getRight()).getValue() == 1)
            {
                return getLeft();
            }
             
        }
         
        return this;
    }

    public Term derivative(Variable v) throws Exception {
        return add(multiply(this.getLeft(), this.getRight().derivative(v)), multiply(this.getRight(),
            this.getLeft().derivative(v)));
    }

}


