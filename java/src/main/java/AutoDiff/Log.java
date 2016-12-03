//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

/**
* Represents a natural logarithm function
*/
public class Log extends Term
{
    /**
    * Constructs a new instance of the 
    *  {@link #Log}
    *  class.
    * 
    *  @param arg The argument of the natural logarithm
    */
    public Log(Term arg) throws Exception {
        setArg(arg);
    }
//
//    /**
//    * Accepts a terms visitor
//    *
//    *  @param visitor The term visitor to accept
//    */
//    public void accept(ITermVisitor visitor) throws Exception {
//        visitor.visit(this);
//    }

    /**
    * Gets the natural logarithm argument.
    */
    private Term __Arg;
    public Term getArg() {
        return __Arg;
    }

    public void setArg(Term value) {
        __Arg = value;
    }

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
            return String.format("log( %s )", getArg());
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

    @Override
    public Term aggregateConstants() throws Exception {
        setArg(getArg().aggregateConstants());
        if (getArg() instanceof Constant)
        {
            return TermBuilder.Constant(Math.log((getArg() instanceof Constant ? (Constant)getArg() : (Constant)null).getValue
                ()));
        }
        else
        {
            return this;
        } 
    }

    @Override
    public Term derivative(Variable v) throws Exception {
        return divide(this.getArg().derivative(v), this.getArg());
    }

}


