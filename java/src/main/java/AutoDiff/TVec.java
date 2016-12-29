//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:34
//

package AutoDiff;

//using CarpeNoctem.Containers;
//using System.Diagnostics.Contracts;
/**
* A column vector made of terms.
*/
public class TVec   
{
    private Term[] terms = new Term[0];
    /**
    * Constructs a new instance of the 
    *  {@link #TVec}
    *  class given vector components.
    * 
    *  @param terms The vector component terms
    */
    public TVec(Term... terms) throws Exception {
        /*Contract.Requires(terms != null);
                    Contract.Requires(Contract.ForAll(terms, term => term != null));
                    Contract.Requires(terms.Any());
        			 */
        this.terms = terms;
    }

    /* Contract.Requires(terms != null);
                Contract.Requires(Contract.ForAll(terms, term => term != null));
                Contract.Requires(terms.Length > 0);
                */
    /**
    * Constructs a new instance of the 
    *  {@link #TVec}
    *  class using another vector's components.
    * 
    *  @param first A vector containing the first vector components to use.
    *  @param rest More vector components to add in addition to the components in 
    *  {@code first}
    */
    public TVec(TVec first, Term... rest) throws Exception {
        terms = new Term[first.getTerms().length+rest.length];
        for (int i = 0; i < first.getTerms().length; i++) {
            terms[i] = first.getTerms()[i];
        }
        for (int i = 0; i < rest.length; i++) {
            terms[i+first.getTerms().length] = rest[i];
        }
    }

    /*Contract.Requires(first != null);
                Contract.Requires(Contract.ForAll(rest, term => term != null));
                */
//    private TVec(Term[] left, Term[] right, Func<Term, Term, Term> elemOp) throws Exception {
//        //Contract.Assume(left.Length == right.Length);
//        terms = new Term[left.length];
//        for (int i = 0;i < terms.length;++i)
//            terms[i] = elemOp(left[i], right[i]);
//    }
//
//    private TVec(Term[] input, Func<Term, Term> elemOp) throws Exception {
//        terms = new Term[input.length];
//        for (int i = 0;i < input.length;++i)
//            terms[i] = elemOp(input[i]);
//    }

    /**
    * Gets a vector component given its zero-based index.
    * 
    *  @param index The vector's component index.
    *  @return The vector component.
    */
    public Term get_idx(int index) throws Exception {
        return terms[index];
    }

    /*  Contract.Requires(index >= 0 && index < Dimension);
                    Contract.Ensures(Contract.Result<Term>() != null);
                    */
//    /**
//    * Gets a term representing the squared norm of this vector.
//    */
//    public Term getNormSquared() throws Exception {
//        //Contract.Ensures(Contract.Result<Term>() != null);
//        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ powers = terms.Select(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(x) => {
//            return TermBuilder.Power(x, 2);
//        }" */);
//        return TermBuilder.Sum(powers);
//    }
//
//    /**
//    * Gets a term representing the a new vector with length = 1 and the direction of this vector.
//    */
//    public TVec getNormalize() throws Exception {
//        Term a = getNormSquared();
//        a = TermBuilder.Power(a, 0.5);
//        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ b = terms.Select(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(x) => {
//            return (x / a);
//        }" */);
//        return new TVec(b);
//    }

    /**
    * Gets the dimensions of this vector
    */
    public int getDimension() throws Exception {
        return terms.length;
    }

    //Contract.Ensures(Contract.Result<int>() > 0);
    /**
    * Gets the first vector component
    */
    public Term getX() throws Exception {
        return this.get_idx(0);
    }

    //Contract.Ensures(Contract.Result<Term>() != null);
    /**
    * Gets the second vector component.
    */
    public Term getY() throws Exception {
        return this.get_idx(1);
    }

    //Contract.Requires(Dimension >= 2);
    //Contract.Ensures(Contract.Result<Term>() != null);
    /**
    * Gets the third vector component
    */
    public Term getZ() throws Exception {
        return this.get_idx(2);
    }

    //Contract.Requires(Dimension >= 3);
    //Contract.Ensures(Contract.Result<Term>() != null);
    /**
    * Gets an array of all vector components.
    * 
    *  @return An array of all vector components. Users are free to modify this array. It doesn't point to any
    * internal structures.
    */
    public Term[] getTerms() throws Exception {
        return (Term[])terms.clone();
    }

    /*Contract.Ensures(Contract.Result<Term[]>() != null);
                Contract.Ensures(Contract.Result<Term[]>().Length > 0);
                Contract.Ensures(Contract.ForAll(Contract.Result<Term[]>(), term => term != null));
    			 */
    /*
    		public static implicit operator TVec(Point2D value)
            {
                return new TVec(value.X,value.Y);
            }
    		*/
    /**
    * Constructs a sum of two term vectors.
    * 
    *  @param left The first vector in the sum
    *  @param right The second vector in the sum
    *  @return A vector representing the sum of 
    *  {@code left}
    *  and 
    *  {@code right}
    */

    /*Contract.Requires(left != null);
                Contract.Requires(right != null);
                Contract.Requires(left.Dimension == right.Dimension);
                Contract.Ensures(Contract.Result<TVec>().Dimension == left.Dimension);
    			 */
    /**
    * Constructs a difference of two term vectors,
    * 
    *  @param left The first vector in the difference
    *  @param right The second vector in the difference.
    *  @return A vector representing the difference of 
    *  {@code left}
    *  and 
    *  {@code right}
    */

    /*Contract.Requires(left != null);
                Contract.Requires(right != null);
                Contract.Requires(left.Dimension == right.Dimension);
                Contract.Ensures(Contract.Result<TVec>().Dimension == left.Dimension);
    			 */
    /**
    * Inverts a vector
    * 
    *  @param vector The vector to invert
    *  @return A vector repsesenting the inverse of 
    *  {@code vector}
    */

    /*Contract.Requires(vector != null);
                Contract.Ensures(Contract.Result<TVec>().Dimension == vector.Dimension);
    			 */
    /**
    * Multiplies a vector by a scalar
    * 
    *  @param vector The vector
    *  @param scalar The scalar
    *  @return A product of the vector 
    *  {@code vector}
    *  and the scalar 
    *  {@code scalar}
    * .
    */

    /*Contract.Requires(vector != null);
                Contract.Requires(scalar != null);
                Contract.Ensures(Contract.Result<TVec>().Dimension == vector.Dimension);
    			 */
    /**
    * Multiplies a vector by a scalar
    * 
    *  @param vector The vector
    *  @param scalar The scalar
    *  @return A product of the vector 
    *  {@code vector}
    *  and the scalar 
    *  {@code scalar}
    * .
    */

    /*Contract.Requires(vector != null);
                Contract.Requires(scalar != null);
                Contract.Ensures(Contract.Result<TVec>().Dimension == vector.Dimension);
    			 */
    /**
    * Constructs a term representing the inner product of two vectors.
    * 
    *  @param left The first vector of the inner product
    *  @param right The second vector of the inner product
    *  @return A term representing the inner product of 
    *  {@code left}
    *  and 
    *  {@code right}
    * .
    */

    /*Contract.Requires(left != null);
                Contract.Requires(right != null);
                Contract.Requires(left.Dimension == right.Dimension);
                Contract.Ensures(Contract.Result<Term>() != null);
    			 */
//    /**
//    * Constructs a term representing the inner product of two vectors.
//    *
//    *  @param left The first vector of the inner product
//    *  @param right The second vector of the inner product
//    *  @return A term representing the inner product of
//    *  {@code left}
//    *  and
//    *  {@code right}
//    * .
//    */
//    public static Term innerProduct(TVec left, TVec right) throws Exception {
//        /*Contract.Requires(left != null);
//                    Contract.Requires(right != null);
//                    Contract.Requires(left.Dimension == right.Dimension);
//                    Contract.Ensures(Contract.Result<Term>() != null);
//        			 */
//        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ products;
//        return TermBuilder.Sum(products);
//    }

    /**
    * Constructs a 3D cross-product vector given two 3D vectors.
    * 
    *  @param left The left cross-product term
    *  @param right The right cross product term
    *  @return A vector representing the cross product of 
    *  {@code left}
    *  and 
    *  {@code right}
    */
    public static TVec crossProduct(TVec left, TVec right) throws Exception {
        return new TVec(left.getY().multiply(right.getZ()).substract(left.getZ().multiply(right.getY())),left.getZ()
            .multiply(right.getX()).substract(left.getX().multiply(right.getZ())),left.getX().multiply(right.getY())
                .substract(left.getY().multiply(right.getX())));
    }

    /*  Contract.Requires(left != null);
                Contract.Requires(right != null);
                Contract.Requires(left.Dimension == 3);
                Contract.Requires(right.Dimension == 3);
                Contract.Ensures(Contract.Result<TVec>().Dimension == 3);
    			 */
    public String toString() {
        try
        {
            Term[] terms = getTerms();
            String termString = "";
            if (terms != null && terms.length > 0)
            {
                termString = terms[0].toString();
                for (int i = 1;i < terms.length;i++)
                {
                    termString += String.format(", %s", terms[i].toString());
                }
            }
             
            return String.format("TVec: Dimension=%d, [%s]", getDimension(), termString);
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

}


