//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:32
//

package AutoDiff;

/**
* Represents an abs function
*/
public class Abs  extends Term 
{
    /**
    * Constructs a new instance of the 
    *  {@link #Abs}
    *  class.
    * 
    *  @param arg The argument of the abs
    */
    public Abs(Term arg) throws Exception {
        setArg(arg);
    }

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
    private Term Arg;
    public Term getArg() {
        return Arg;
    }

    public void setArg(Term value) {
        Arg = value;
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
            return String.format("abs( %s )", getArg());
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
        setArg(getArg().aggregateConstants());
        if (getArg() instanceof Constant)
        {
            return TermBuilder.Constant(Math.abs((getArg() instanceof Constant ? (Constant)getArg() : (Constant)null).getValue
                ()));
        }
        else
        {
            return this;
        } 
    }

    public Term derivative(Variable v) throws Exception {
        return Term.divide(Term.multiply(this.getArg().derivative(v), this.getArg()), this);
    }

}


