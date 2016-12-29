//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

/**
* Represents a cos function
*/
public class Cos  extends Term 
{
    /**
    * Constructs a new instance of the 
    *  {@link #Cos}
    *  class.
    * 
    *  @param arg The argument of the cosine
    */
    public Cos(Term arg) throws Exception {
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
            return String.format("cos( %s )", getArg());
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
            return TermBuilder.Constant(Math.cos((getArg() instanceof Constant ? (Constant)getArg() : (Constant)null).getValue
                ()));
        }
        else
        {
            if (getArg() instanceof Zero)
                return TermBuilder.Constant(0);
             
            return this;
        } 
    }

    public Term derivative(Variable v) throws Exception {
        return Term.multiply(new Sin(this.getArg()),
            Term.multiply(TermBuilder.Constant(-1),this.getArg().derivative(v)));
    }

}


