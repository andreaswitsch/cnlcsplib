//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

/**
* Represents a constant-power function x^n, where n is constant.
*/
public class ConstPower  extends Term 
{
    /**
    * Constructs a new instance of the 
    *  {@link #ConstPower}
    *  class.
    * 
    *  @param baseTerm The base of the power function
    *  @param exponent The exponent of the power function
    */
    public ConstPower(Term baseTerm, double exponent) throws Exception {
        setBase(baseTerm);
        setExponent(exponent);
    }

    /**
    * Gets the base term of the power function
    */
    private Term Base;
    public Term getBase() {
        return Base;
    }

    public void setBase(Term value) {
        Base = value;
    }

    /**
    * Gets the exponent term of the power function.
    */
    private double Exponent;
    public double getExponent() {
        return Exponent;
    }

    public void setExponent(double value) {
        Exponent = value;
    }

//    /**
//    * Accepts a term visitor.
//    *
//    *  @param visitor The term visitor to accept.
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
            return String.format("constPower( %s, %f )", getBase(), getExponent());
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
        setBase(getBase().aggregateConstants());
        if (getBase() instanceof Constant)
        {
            return TermBuilder.Constant(Math.pow((getBase() instanceof Constant ? (Constant)getBase() : (Constant)null)
                .getValue(),
                getExponent()));
        }
        else if (getBase() instanceof Zero)
        {
            if (getExponent() >= 0)
            {
                return getBase();
            }
            else
            {
                throw new IllegalArgumentException("Argument 'divisor' is 0");
            } 
        }
        else if (getBase() instanceof ConstPower)
        {
            setExponent(getExponent() * (getBase() instanceof ConstPower ? (ConstPower)getBase() : (ConstPower)null).getExponent());
            setBase((getBase() instanceof ConstPower ? (ConstPower)getBase() : (ConstPower)null).getBase());
            return this;
        }
        else
        {
            return this;
        }   
    }

    public Term derivative(Variable v) throws Exception {
        return Term.multiply(TermBuilder.Constant(this.getExponent()), Term.multiply(new ConstPower(this.getBase(),(this
            .getExponent() - 1)), this.getBase().derivative(v)));
    }

}


