//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:32
//

package AutoDiff;

/**
* Represents a atan2 function
*/
public class Atan2  extends Term 
{
    /**
    * Constructs a new instance of the 
    *  {@link #Max}
    *  type.
    * 
    *  @param left The first max term
    *  @param right The second max term
    */
    public Atan2(Term left, Term right) throws Exception {
        setLeft(left);
        setRight(right);
    }

    /**
    * Gets the first atan2 term.
    */
    private Term __Left;
    public Term getLeft() {
        return __Left;
    }

    public void setLeft(Term value) {
        __Left = value;
    }

    /**
    * Gets the second atan2 term.
    */
    private Term __Right;
    public Term getRight() {
        return __Right;
    }

    public void setRight(Term value) {
        __Right = value;
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
            return String.format("atan2( %s, %s )", getLeft(), getRight());
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
            return TermBuilder.Constant(Math.atan2((getLeft() instanceof Constant ? (Constant)getLeft() : (Constant)null)
                    .getValue(),
                (getRight() instanceof Constant ? (Constant)getRight() : (Constant)null).getValue()));
        }
        else
        {
            return this;
        } 
    }

    public Term derivative(Variable v) throws Exception {
        Term t = this.getLeft().multiply(this.getRight().derivative(v)).substract(this.getRight().multiply(this.getLeft()
            .derivative(v)));
        return t.divide(new ConstPower(this.getLeft(), 2).add(new ConstPower(this.getRight(), 2)));
    }

}


