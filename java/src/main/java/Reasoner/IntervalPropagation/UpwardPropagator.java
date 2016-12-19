//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:47
//

package Reasoner.IntervalPropagation;


import AutoDiff.ITermVisitor;
import AutoDiff.Product;
import AutoDiff.Term;
import org.apache.commons.lang3.NotImplementedException;
import AutoDiff.*;

public class UpwardPropagator implements ITermVisitor<Boolean>
{
    public TermList Changed;

    private void addChanged(Term t) throws Exception {
        for (Term s : t.Parents)
        {
            if (!Changed.contains(s))
                Changed.enqueue(s);
             
        }
        //Changed.MoveToEnd(s);
        if (!Changed.contains(t))
            Changed.enqueue(t);
         
    }

    public UpwardPropagator() throws Exception {
    }

    public Boolean visit(Constant constant) throws Exception {
        return false;
    }

    public Boolean visit(Zero zero) throws Exception {
        return false;
    }

    public Boolean visit(ConstPower intPower) throws Exception {
        boolean includesZero = intPower.getBase().Min * intPower.getBase().Max <= 0;
        if (intPower.getExponent() > 0)
        {
            double a = Math.pow(intPower.getBase().Max, intPower.getExponent());
            double b = Math.pow(intPower.getBase().Min, intPower.getExponent());
            if (includesZero)
            {
                if (updateInterval(intPower, Math.min(0, Math.min(a, b)), Math.max(0, Math.max(a, b))))
                {
                    addChanged(intPower);
                    return true;
                }
                 
            }
            else
            {
                if (updateInterval(intPower, Math.min(a, b), Math.max(a, b)))
                {
                    addChanged(intPower);
                    return true;
                }
                 
            } 
        }
        else if (!includesZero)
        {
            double a = Math.pow(intPower.getBase().Max, intPower.getExponent());
            double b = Math.pow(intPower.getBase().Min, intPower.getExponent());
            if (updateInterval(intPower, Math.min(a, b), Math.max(a, b)))
            {
                addChanged(intPower);
                return true;
            }
             
        }
          
        return false;
    }

    //else +- Infinity is possible
    public Boolean visit(TermPower tp) throws Exception {
        throw new NotImplementedException("Propagation for TermPower not implemented");
    }


    public Boolean visit(Product product) throws Exception {
        double aa = product.getLeft().Min * product.getRight().Min;
        double bb = product.getLeft().Max * product.getRight().Max;
        double max;
        double min;
        if (product.getLeft() == product.getRight())
        {
            min = Math.min(aa, bb);
            max = Math.max(aa, bb);
            if (product.getLeft().Min * product.getLeft().Max <= 0)
                min = 0;
             
        }
        else
        {
            double ab = product.getLeft().Min * product.getRight().Max;
            double ba = product.getLeft().Max * product.getRight().Min;
            max = Math.max(aa, Math.max(ab, Math.max(ba, bb)));
            min = Math.min(aa, Math.min(ab, Math.min(ba, bb)));
        } 
        if (updateInterval(product, min, max))
        {
            addChanged(product);
            return true;
        }
         
        return false;
    }

    public Boolean visit(LinSigmoid sigmoid) throws Exception {
        throw new NotImplementedException("LinSigmoid for TermPower not implemented");
    }

    public Boolean visit(LTConstraint constraint) throws Exception {
        if (constraint.getLeft().Max < constraint.getRight().Min)
        {
            if (updateInterval(constraint, 1, 1))
            {
                addChanged(constraint);
                return true;
            }
             
        }
        else if (constraint.getLeft().Min >= constraint.getRight().Max)
        {
            //Console.WriteLine("LT UP negated: {0} {1}",constraint.getLeft().Min ,constraint.getRight().Max);
            if (updateInterval(constraint, Double.NEGATIVE_INFINITY, 0))
            {
                addChanged(constraint);
                return true;
            }
             
        }
          
        return false;
    }

    public Boolean visit(LTEConstraint constraint) throws Exception {
        if (constraint.getLeft().Max <= constraint.getRight().Min)
        {
            if (updateInterval(constraint, 1, 1))
            {
                addChanged(constraint);
                return true;
            }
             
        }
        else if (constraint.getLeft().Min > constraint.getRight().Max)
        {
            if (updateInterval(constraint, Double.NEGATIVE_INFINITY, 0))
            {
                addChanged(constraint);
                return true;
            }
             
        }
          
        return false;
    }

    public Boolean visit(Min min) throws Exception {
        if (updateInterval(min, Math.min(min.getLeft().Min, min.getRight().Min), Math.max(min.getLeft().Max, min.getRight().Max)))
        {
            addChanged(min);
            return true;
        }
         
        return false;
    }

    public Boolean visit(Max max) throws Exception {
        if (updateInterval(max, Math.min(max.getLeft().Min, max.getRight().Min), Math.max(max.getLeft().Max, max.getRight().Max)))
        {
            addChanged(max);
            return true;
        }
         
        return false;
    }

    public Boolean visit(And and) throws Exception {
        if (and.getLeft().Min > 0 && and.getRight().Min > 0)
        {
            if (updateInterval(and, 1, 1))
            {
                addChanged(and);
                return true;
            }
             
        }
        else if (and.getLeft().Max <= 0 || and.getRight().Max <= 0)
        {
            if (updateInterval(and, Double.NEGATIVE_INFINITY, 0))
            {
                addChanged(and);
                return true;
            }
             
        }
          
        return false;
    }

    public Boolean visit(Or or) throws Exception {
        if (or.getLeft().Min > 0 || or.getRight().Min > 0)
        {
            if (updateInterval(or, 1, 1))
            {
                addChanged(or);
                return true;
            }
             
        }
        else if (or.getLeft().Max <= 0 && or.getRight().Max <= 0)
        {
            if (updateInterval(or, Double.NEGATIVE_INFINITY, 0))
            {
                addChanged(or);
                return true;
            }
             
        }
          
        return false;
    }

    public Boolean visit(ConstraintUtility cu) throws Exception {
        if (cu.getConstraint().Max < 1)
        {
            if (updateInterval(cu, Double.NEGATIVE_INFINITY, cu.getConstraint().Max))
            {
                addChanged(cu);
                return true;
            }
             
        }
         
        if (updateInterval(cu, Double.NEGATIVE_INFINITY, cu.getUtility().Max))
        {
            addChanged(cu);
            return true;
        }
         
        return false;
    }

    public Boolean visit(Reification reif) throws Exception {
        if (reif.getCondition().Min > 0)
        {
            if (updateInterval(reif, reif.getMaxVal(), reif.getMaxVal()))
            {
                addChanged(reif);
                return true;
            }
             
        }
        else if (reif.getCondition().Max < 0)
        {
            if (updateInterval(reif, reif.getMinVal(), reif.getMinVal()))
            {
                addChanged(reif);
                return true;
            }
             
        }
          
        return false;
    }

    @Override
    public Boolean visit(final Negation r) throws Exception
    {
        return null;
    }

    public Boolean visit(Sum sum) throws Exception {
        double min = 0;
        double max = 0;
        for (int i = sum.getTerms().length - 1;i >= 0;--i)
        {
            min += sum.getTerms()[i].Min;
            max += sum.getTerms()[i].Max;
        }
        if (updateInterval(sum, min, max))
        {
            addChanged(sum);
            return true;
        }
         
        return false;
    }

    public Boolean visit(AutoDiff.Variable variable) throws Exception {
        return true;
    }

    public Boolean visit(Log log) throws Exception {
        if (updateInterval(log, Math.log(log.getArg().Min), Math.log(log.getArg().Max)))
        {
            addChanged(log);
            return true;
        }
         
        return false;
    }

    public Boolean visit(Sin sin) throws Exception {
        double size = sin.getArg().Max - sin.getArg().Min;
        boolean c = false;
        if (size <= 2 * Math.PI)
        {
            double a = Math.sin(sin.getArg().Max);
            double b = Math.sin(sin.getArg().Min);
            double halfPI = Math.PI / 2;
            double x = Math.ceil((sin.getArg().Min - halfPI) / Math.PI);
            double y = Math.floor((sin.getArg().Max - halfPI) / Math.PI);
            if (x == y)
            {
                //single extrema
                if (((int)x) % 2 == 0)
                {
                    //maxima
                    c = updateInterval(sin, Math.min(a, b), 1);
                }
                else
                {
                    //minima
                    c = updateInterval(sin, -1, Math.max(a, b));
                } 
            }
            else if (x > y)
            {
                //no extrema
                c = updateInterval(sin, Math.min(a, b), Math.max(a, b));
            }
              
        }
         
        //multiple extrema, don't update
        if (c)
            addChanged(sin);
         
        return c;
    }

    public Boolean visit(Cos cos) throws Exception {
        double size = cos.getArg().Max - cos.getArg().Min;
        boolean c = false;
        if (size <= 2 * Math.PI)
        {
            double a = Math.cos(cos.getArg().Max);
            double b = Math.cos(cos.getArg().Min);
            double x = Math.ceil(cos.getArg().Min / Math.PI);
            double y = Math.floor(cos.getArg().Max / Math.PI);
            if (x == y)
            {
                //single extrema
                if (((int)x) % 2 == 0)
                {
                    //maxima
                    c = updateInterval(cos, Math.min(a, b), 1);
                }
                else
                {
                    //minima
                    c = updateInterval(cos, -1, Math.max(a, b));
                } 
            }
            else if (x > y)
            {
                //no extrema
                c = updateInterval(cos, Math.min(a, b), Math.max(a, b));
            }
              
        }
         
        //multiple extrema, don't update
        if (c)
            addChanged(cos);
         
        return c;
    }

    public Boolean visit(Abs abs) throws Exception {
        boolean containsZero = abs.getArg().Min * abs.getArg().Max <= 0;
        boolean c = false;
        if (containsZero)
            c = updateInterval(abs, 0, Math.max(Math.abs(abs.getArg().Min), Math.abs(abs.getArg().Max)));
        else
            c = updateInterval(abs, Math.min(Math.abs(abs.getArg().Min), Math.abs(abs.getArg().Max)), Math.max(Math.abs(abs.getArg().Min), Math.abs(abs.getArg().Max))); 
        if (c)
            addChanged(abs);
         
        return c;
    }

    public Boolean visit(Exp exp) throws Exception {
        if (updateInterval(exp, Math.exp(exp.getArg().Min), Math.exp(exp.getArg().Max)))
        {
            addChanged(exp);
            return true;
        }
         
        return false;
    }

    @Override
    public Boolean visit(final Sigmoid sigmoid) throws Exception
    {
        throw new NotImplementedException("Sigmoid Propagation not implemeted");
    }

    public Boolean visit(Atan2 atan2) throws Exception {
        throw new NotImplementedException("atan2 Propagation not implemeted");
    }

    protected void outputChange(Term t, double oldmin, double oldmax) throws Exception {
        //Console.WriteLine("UW: Interval of {0} is now [{1}, {2}]",t,t.Min,t.Max);
        double oldwidth = oldmax - oldmin;
        double newwidth = t.Max - t.Min;
        System.out.println("notImplemented");
//        if (t instanceof AutoDiff.Variable)
//            Console.WriteLine("UW shrinking [{0}..{1}] to [{2}..{3}] by {4} ({5}%)", oldmin, oldmax, t.Min, t.Max, oldwidth - newwidth, (oldwidth - newwidth) / oldwidth * 100);
         
    }

    protected Boolean updateInterval(Term t, double min, double max) throws Exception {
        boolean ret = t.Min < min || t.Max > max;
        if (!Double.isNaN(min))
            t.Min = Math.max(t.Min, min);
         
        if (!Double.isNaN(max))
            t.Max = Math.min(t.Max, max);
         
        if (ret)
            IntervalPropagator.updates++;
         
        IntervalPropagator.visits++;
        if (t.Min > t.Max)
            throw new UnsolveableException();
         
        return ret;
    }

}


