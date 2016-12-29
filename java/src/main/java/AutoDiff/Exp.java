//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

/**
* Represents the exponential function 
*  {@code e^x}
*/
public class Exp  extends Term 
{
    /**
    * Constructs a new instance of the 
    *  {@link #Exp}
    *  type.
    * 
    *  @param arg The exponent of the function.
    */
    public Exp(Term arg) throws Exception {
        setArg(arg);
    }

    /**
    * Gets the exponent term.
    */
    private Term Arg;
    public Term getArg() {
        return Arg;
    }

    public void setArg(Term value) {
        Arg = value;
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
            return String.format("exp( %s )", getArg());
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
            return TermBuilder.Constant(Math.exp((getArg() instanceof Constant ? (Constant)getArg() : (Constant)null).getValue
                ()));
        }
        else
        {
            if (getArg() instanceof Zero)
                return TermBuilder.Constant(1);
             
            return this;
        } 
    }

    public Term derivative(Variable v) throws Exception {
        return Term.multiply(this, this.getArg().derivative(v));
    }

}


