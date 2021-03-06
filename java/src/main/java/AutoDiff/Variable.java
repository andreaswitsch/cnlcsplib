//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:34
//

package AutoDiff;

/**
* Represents a variable term. Variable terms are substituted for real values during evaluation and
* differentiation.
*/
public class Variable  extends Term 
{
//    /**
//    * Accepts a term visitor
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

    /**
    * Additions by Carpe Noctem:
    */
    public double GlobalMin = Double.NEGATIVE_INFINITY;
    public double GlobalMax = Double.POSITIVE_INFINITY;

    static int id = 0;
    String name="Var";
    int ownID;
    public Variable() throws Exception {
        ownID = id++;
    }

    public String toString() {
        try
        {
            int hash = ownID;
            return (hash < 0 ? String.format("%s_%d",name, -hash) : String.format("%s%d", name, hash));
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

    //return (hash<0 ? string.Format ("Var_{0}[{1}..{2}]",-hash,Min,Max) : string.Format ("Var{0}[{1}..{2}]",hash,Min,Max));
    public Term aggregateConstants() throws Exception {
        return this;
    }

    public Term derivative(Variable v) throws Exception {
        if (this == v)
            return TermBuilder.Constant(1);
        else
            return TermBuilder.Constant(0);
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public static void resetIdCounter() {
        id = 0;
    }
}


