//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:34
//

package AutoDiff;

/**
* Represents a sigmoidal function.
*/
public class Sigmoid  extends Term 
{
    /**
    * Constructs a new instance of the 
    *  {@link #Sigmoid}
    *  type.
    * 
    *  @param arg The argument of the sigmoid
    *  @param mid The midpoint of the sigmoid, where its value equals .5
    *  @param steepness The steepness of the sigmoid
    */
    public Sigmoid(Term arg, Term mid, double steepness) throws Exception {
        setArg(arg);
        setMid(mid);
        setSteepness(steepness);
    }

    public Sigmoid(Term arg, Term mid) throws Exception {
        setArg(arg);
        setMid(mid);
        setSteepness(1);
    }

    /**
    * Gets the Argument of the sigmoid.
    */
    private Term __Arg;
    public Term getArg() {
        return __Arg;
    }

    public void setArg(Term value) {
        __Arg = value;
    }

    /**
    * Gets the Midpoint of the sigmoid.
    */
    private Term __Mid;
    public Term getMid() {
        return __Mid;
    }

    public void setMid(Term value) {
        __Mid = value;
    }

    /**
    * Gets the sigmoid's steepness.
    */
    private double __Steepness;
    public double getSteepness() {
        return __Steepness;
    }

    public void setSteepness(double value) {
        __Steepness = value;
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
            return String.format("sigmoid( %s, %s, %f )", getArg(), getMid(), getSteepness());
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
        setArg(getArg().aggregateConstants());
        setMid(getMid().aggregateConstants());
        if (getArg() instanceof Constant && getMid() instanceof Constant)
        {
            double e = Math.exp(getSteepness() * (-(getArg() instanceof Constant ? (Constant)getArg() : (Constant)
                null).getValue() + (getMid() instanceof Constant ? (Constant)getMid() : (Constant)null).getValue()));
            if (Double.POSITIVE_INFINITY == e)
            {
                return TermBuilder.Constant(Epsilon);
            }
            else
            {
                //Console.WriteLine("FUCKUP {0}",e);
                e = 1.0 / (1.0 + e);
            } 
            if (e < Epsilon)
            {
                return TermBuilder.Constant(Epsilon);
            }
            else
            {
                return TermBuilder.Constant(e);
            } 
        }
        else
        {
            return this;
        } 
    }

    public Term derivative(Variable v) throws Exception {
        Term t = multiply(multiply(TermBuilder.Constant(getSteepness()),
            (substract(getArg().derivative(v), getMid().derivative(v)))),
            new Exp(multiply(TermBuilder.Constant(getSteepness()), substract(getMid(), getArg()))));
        return divide(t, new ConstPower(add(new Exp(multiply(TermBuilder.Constant(getSteepness()), getArg
            ())), new Exp(multiply(TermBuilder.Constant(getSteepness()), this.getMid()))), 2));
    }

}


