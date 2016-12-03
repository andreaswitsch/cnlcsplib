//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;


/**
* A constant value term
*/
public class Constant extends Term
{
    /**
    * Constructs a new instance of the 
    *  {@link #Constant}
    *  class
    * 
    *  @param value The value of the constant
    */
    public Constant(double value) {
        setValue(value);
    }

    /**
    * Gets the value of this constant
    */
    private double __Value;
    public double getValue() {
        return __Value;
    }

    public void setValue(double value) {
        __Value = value;
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
            if (this == True)
                return "true";
             
            if (this == False)
                return "false";
             
            return String.format("%f", getValue());
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
        return this;
    }

    @Override
    public Term derivative(Variable v) throws Exception {
        return TermBuilder.Constant(0);
    }

}


