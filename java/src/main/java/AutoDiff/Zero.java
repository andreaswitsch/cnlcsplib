//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:34
//

package AutoDiff;

/**
* A constant zero term. Similar to 
*  {@link #Constant}
*  but represents only the value 0.
*/
public class Zero  extends Term 
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

    public String toString() {
        try
        {
            return "0";
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
        return this;
    }

    public Term derivative(Variable v) throws Exception {
        return this;
    }

    public boolean equals(Object obj) {
        try
        {
            if (obj instanceof Zero)
                return true;
             
            return false;
        }
        catch (RuntimeException dummyCatchVar1)
        {
            throw dummyCatchVar1;
        }
        catch (Exception dummyCatchVar1)
        {
            throw new RuntimeException(dummyCatchVar1);
        }
    
    }

}


