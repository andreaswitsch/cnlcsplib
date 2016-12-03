//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:32
//

package AutoDiff;

/**
* Represents a sigmoidal function.
*/
public class And  extends Term 
{
    /**
    * Constructs a new instance of the 
    *  {@link #Min}
    *  type.
    * 
    *  @param left The first min term
    *  @param right The second min term
    */
    public And(Term left, Term right) throws Exception {
        setLeft(left);
        setRight(right);
    }

    /**
    * Gets the first product term.
    */
    private Term __Left;
    public Term getLeft() {
        return __Left;
    }

    public void setLeft(Term value) {
        __Left = value;
    }

    /**
    * Gets the second product term.
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

    public Term negate() throws Exception {
        return new Or(getLeft().negate(), getRight().negate());
    }

    public String toString() {
        try
        {
            return String.format("and( %s, %s )", getLeft(), getRight());
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
        if (getLeft() == False)
            return getLeft();
         
        setRight(getRight().aggregateConstants());
        if (getLeft() == True)
            return getRight();
         
        if (getRight() == False)
            return getRight();
         
        if (getRight() == True)
            return getLeft();
         
        if (getLeft() instanceof Constant && getRight() instanceof Constant)
        {
            if ((getLeft() instanceof Constant ? (Constant)getLeft() : (Constant)null).getValue() > 0.75 && (getRight() instanceof Constant ? (Constant)getRight() : (Constant)null).getValue() > 0.75)
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
        throw new Exception("Symbolic Derivation of And not supported");
    }

}


