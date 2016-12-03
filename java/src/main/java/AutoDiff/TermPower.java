//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:34
//

package AutoDiff;

public class TermPower  extends Term
{
    public TermPower(Term baseTerm, Term exponent) throws Exception {
        setBase(baseTerm);
        setExponent(exponent);
    }

    /**
    * Gets the base term of the power function
    */
    private Term __Base;
    public Term getBase() {
        return __Base;
    }

    public void setBase(Term value) {
        __Base = value;
    }

    /**
    * Gets the exponent term of the power function.
    */
    private Term __Exponent;
    public Term getExponent() {
        return __Exponent;
    }

    public void setExponent(Term value) {
        __Exponent = value;
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
            return String.format("termPower( {0}, {1} )", getBase(), getExponent());
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
        setBase(getBase().aggregateConstants());
        setExponent(getExponent().aggregateConstants());
        if (getExponent() instanceof Zero)
        {
            return TermBuilder.Constant(1);
        }
         
        if (getBase() instanceof Constant && getExponent() instanceof Constant)
        {
            return TermBuilder.Constant(Math.pow((getBase() instanceof Constant ? (Constant)getBase() : (Constant)null)
                    .getValue(),
                (getExponent() instanceof Constant ? (Constant)getExponent() : (Constant)null).getValue()));
        }
        else if (getBase() instanceof Zero)
        {
            return getBase();
        }
        else if (getBase() instanceof TermPower)
        {
            setExponent(multiply(getExponent(), (getBase() instanceof TermPower ? (TermPower)getBase() :
                (TermPower)null).getExponent()));
            setBase((getBase() instanceof TermPower ? (TermPower)getBase() : (TermPower)null).getBase());
            return this;
        }
        else
        {
            return this;
        }   
    }

    public Term derivative(Variable v) throws Exception {
        return new TermPower(this.getBase(), multiply(substract(this.getExponent(), TermBuilder.Constant(1)),
            add(multiply(this.getExponent(), this.getBase().derivative(v)), multiply(multiply(this
                    .getBase(), (new Log(this.getBase()))), this.getExponent().derivative(v)))));
    }

}


