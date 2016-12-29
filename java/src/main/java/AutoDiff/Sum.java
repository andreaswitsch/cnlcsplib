//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:34
//

package AutoDiff;

import java.util.ArrayList;
import java.util.List;

/**
* Represents a sum of at least two terms.
*/
public class Sum  extends Term 
{
    /**
    * Constructs an instance of the 
    *  {@link #Sum}
    *  class.
    * 
    *  @param first The first term in the sum
    *  @param second The second term in the sum
    *  @param rest The rest of the terms in the sum.
    */
    public Sum(Term first, Term second, Term... rest) throws Exception {
        Term[] allTerms = new Term[2+rest.length];
        allTerms[0] = first;
        allTerms[1] = second;

        for (int i = 0; i < rest.length; i++) {
            allTerms[i+2] = rest[i];
        }

        setTerms(allTerms);
    }

    public Sum(Term[] terms) throws Exception {
        setTerms(terms);
    }

    /**
    * Gets the terms of this sum.
    */
    private Term[] Terms;
    public Term[] getTerms() {
        return Terms;
    }

    public void setTerms(Term[] value) {
        Terms = value;
    }

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
            String ret = String.format("( %s", getTerms()[0].toString());
            for (int i = 1;i < getTerms().length;i++)
            {
                ret += String.format(" + %s", getTerms()[i].toString());
            }
            return ret += " )";
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
        Term curSummand;
        boolean foundConst = false;
        double sum = 0;
        List<Term> nonConstTerms = new ArrayList<Term>();
        for (int i = 0;i < getTerms().length;i++)
        {
            curSummand = getTerms()[i].aggregateConstants();
            if (curSummand instanceof Constant)
            {
                sum += (curSummand instanceof Constant ? (Constant)curSummand : (Constant)null).getValue();
                foundConst = true;
            }
            else
            {
                if (!(curSummand instanceof Zero))
                    nonConstTerms.add(curSummand);
                 
            } 
        }
        if (nonConstTerms.size() == 0)
        {
            return TermBuilder.Constant(sum);
        }
        else if (!foundConst && nonConstTerms.size() == 1)
        {
            return nonConstTerms.get(0);
        }
          
        if (foundConst)
        {
            nonConstTerms.add(TermBuilder.Constant(sum));
        }

        Term[] terms = new Term[nonConstTerms.size()];
        terms = nonConstTerms.toArray(terms);
        setTerms(terms);
        return this;
    }

    public Term derivative(Variable v) throws Exception {
        List<Term> t = new ArrayList<>();
        for (int i = 0;i < getTerms().length;i++)
        {
            t.add(getTerms()[i].derivative(v));
        }
        return new Sum((Term[]) t.toArray());
    }

}


