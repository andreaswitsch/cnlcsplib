//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:47
//

package Reasoner.IntervalPropagation;


import Alica.Reasoner.IntervalPropagation.DownwardPropagator;
import Alica.Reasoner.IntervalPropagation.IntervalPropagator;
import Alica.Reasoner.IntervalPropagation.TermList;
import Alica.Reasoner.IntervalPropagation.UnsolveableException;
import AutoDiff.Gp;
import AutoDiff.Product;
import AutoDiff.Term;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

//#define DEBUG_UP
//using Alica.Reasoner;
//using Al=Alica;
public class UpwardPropagator  extends ITermVisitor<boolean> 
{
    public TermList Changed;
    //internal Queue<Term> Changed;
    /*private void AddAllChanged(List<Term> l) {
    			foreach(Term t in l) {
    				if(!Changed.Contains(t)) Changed.Enqueue(t);
    			}
    		}*/
    private void addChanged(Term t) throws Exception {
        for (Object __dummyForeachVar0 : t.Parents)
        {
            /*foreach(Term s in t.Parents) {
            				Changed.Enqueue(s);
            			}
            			Changed.Enqueue(t);*/
            Term s = (Term)__dummyForeachVar0;
            //Changed.Enqueue(s);
            if (!Changed.contains(s))
                Changed.enqueue(s);
             
        }
        //Changed.MoveToEnd(s);
        if (!Changed.contains(t))
            Changed.enqueue(t);
         
    }

    //Changed.Enqueue(t);
    public UpwardPropagator() throws Exception {
    }

    private DownwardPropagator __DP;
    public DownwardPropagator getDP() {
        return __DP;
    }

    public void setDP(DownwardPropagator value) {
        __DP = value;
    }

    public boolean visit(Constant constant) throws Exception {
        return false;
    }

    public boolean visit(Zero zero) throws Exception {
        return false;
    }

    public boolean visit(ConstPower intPower) throws Exception {
        boolean includesZero = intPower.Base.Min * intPower.Base.Max <= 0;
        if (intPower.Exponent > 0)
        {
            double a = Math.Pow(intPower.Base.Max, intPower.Exponent);
            double b = Math.Pow(intPower.Base.Min, intPower.Exponent);
            if (includesZero)
            {
                if (UpdateInterval(intPower, Math.Min(0, Math.Min(a, b)), Math.Max(0, Math.Max(a, b))))
                {
                    //if(UpdateInterval(intPower,Math.Min(0,Math.Pow(intPower.Base.Min,intPower.Exponent)),Math.Max(0,Math.Pow(intPower.Base.Max,intPower.Exponent)))) {
                    AddChanged(intPower);
                    return true;
                }
                 
            }
            else
            {
                if (UpdateInterval(intPower, Math.Min(a, b), Math.Max(a, b)))
                {
                    //if(UpdateInterval(intPower,Math.Pow(intPower.Base.Min,intPower.Exponent),Math.Pow(intPower.Base.Max,intPower.Exponent))) {
                    AddChanged(intPower);
                    return true;
                }
                 
            } 
        }
        else if (!includesZero)
        {
            double a = Math.Pow(intPower.Base.Max, intPower.Exponent);
            double b = Math.Pow(intPower.Base.Min, intPower.Exponent);
            //Console.WriteLine("Cur: {0} [{1} : {2}]",intPower,intPower.Min,intPower.Max);
            //Console.WriteLine("Base: [{0} : {1}]",intPower.Base.Min,intPower.Base.Max);
            if (UpdateInterval(intPower, Math.Min(a, b), Math.Max(a, b)))
            {
                //Console.WriteLine("From UW intpower {0}",intPower.Exponent);
                //if(UpdateInterval(intPower,Math.Pow(intPower.Base.Max,intPower.Exponent),Math.Pow(intPower.Base.Min,intPower.Exponent))) {
                AddChanged(intPower);
                return true;
            }
             
        }
          
        return false;
    }

    //else +- Infinity is possible
    public boolean visit(TermPower tp) throws Exception {
        throw new NotImplementedException("Propagation for TemPower not implemented");
    }

    public boolean visit(Gp gp) throws Exception {
        throw new NotImplementedException("Propagation for TemPower not implemented");
    }

    public boolean visit(Product product) throws Exception {
        double aa = product.Left.Min * product.Right.Min;
        double bb = product.Left.Max * product.Right.Max;
        double max;
        double min;
        if (product.Left == product.Right)
        {
            min = Math.Min(aa, bb);
            max = Math.Max(aa, bb);
            if (product.Left.Min * product.Left.Max <= 0)
                min = 0;
             
        }
        else
        {
            double ab = product.Left.Min * product.Right.Max;
            double ba = product.Left.Max * product.Right.Min;
            max = Math.Max(aa, Math.Max(ab, Math.Max(ba, bb)));
            min = Math.Min(aa, Math.Min(ab, Math.Min(ba, bb)));
        } 
        if (UpdateInterval(product, min, max))
        {
            AddChanged(product);
            return true;
        }
         
        return false;
    }

    public boolean visit(Sigmoid sigmoid) throws Exception {
        throw new NotImplementedException("Sigmoidal propagation not implemented");
    }

    public boolean visit(LinSigmoid sigmoid) throws Exception {
        throw new NotImplementedException("Sigmoidal propagation not implemented");
    }

    public boolean visit(LTConstraint constraint) throws Exception {
        if (constraint.Left.Max < constraint.Right.Min)
        {
            if (UpdateInterval(constraint, 1, 1))
            {
                AddChanged(constraint);
                return true;
            }
             
        }
        else if (constraint.Left.Min >= constraint.Right.Max)
        {
            //Console.WriteLine("LT UP negated: {0} {1}",constraint.Left.Min ,constraint.Right.Max);
            if (UpdateInterval(constraint, Double.NegativeInfinity, 0))
            {
                AddChanged(constraint);
                return true;
            }
             
        }
          
        return false;
    }

    public boolean visit(LTEConstraint constraint) throws Exception {
        if (constraint.Left.Max <= constraint.Right.Min)
        {
            if (UpdateInterval(constraint, 1, 1))
            {
                AddChanged(constraint);
                return true;
            }
             
        }
        else if (constraint.Left.Min > constraint.Right.Max)
        {
            if (UpdateInterval(constraint, Double.NegativeInfinity, 0))
            {
                AddChanged(constraint);
                return true;
            }
             
        }
          
        return false;
    }

    public boolean visit(Min min) throws Exception {
        if (UpdateInterval(min, Math.Min(min.Left.Min, min.Right.Min), Math.Max(min.Left.Max, min.Right.Max)))
        {
            AddChanged(min);
            return true;
        }
         
        return false;
    }

    public boolean visit(Max max) throws Exception {
        if (UpdateInterval(max, Math.Min(max.Left.Min, max.Right.Min), Math.Max(max.Left.Max, max.Right.Max)))
        {
            AddChanged(max);
            return true;
        }
         
        return false;
    }

    public boolean visit(And and) throws Exception {
        if (and.Left.Min > 0 && and.Right.Min > 0)
        {
            if (UpdateInterval(and, 1, 1))
            {
                AddChanged(and);
                return true;
            }
             
        }
        else if (and.Left.Max <= 0 || and.Right.Max <= 0)
        {
            if (UpdateInterval(and, Double.NegativeInfinity, 0))
            {
                AddChanged(and);
                return true;
            }
             
        }
          
        return false;
    }

    public boolean visit(Or or) throws Exception {
        if (or.Left.Min > 0 || or.Right.Min > 0)
        {
            if (UpdateInterval(or, 1, 1))
            {
                AddChanged(or);
                return true;
            }
             
        }
        else if (or.Left.Max <= 0 && or.Right.Max <= 0)
        {
            if (UpdateInterval(or, Double.NegativeInfinity, 0))
            {
                AddChanged(or);
                return true;
            }
             
        }
          
        return false;
    }

    public boolean visit(ConstraintUtility cu) throws Exception {
        if (cu.Constraint.Max < 1)
        {
            if (UpdateInterval(cu, Double.NegativeInfinity, cu.Constraint.Max))
            {
                AddChanged(cu);
                return true;
            }
             
        }
         
        if (UpdateInterval(cu, Double.NegativeInfinity, cu.Utility.Max))
        {
            AddChanged(cu);
            return true;
        }
         
        return false;
    }

    public boolean visit(Reification reif) throws Exception {
        if (reif.Condition.Min > 0)
        {
            if (UpdateInterval(reif, reif.MaxVal, reif.MaxVal))
            {
                AddChanged(reif);
                return true;
            }
             
        }
        else if (reif.Condition.Max < 0)
        {
            if (UpdateInterval(reif, reif.MinVal, reif.MinVal))
            {
                AddChanged(reif);
                return true;
            }
             
        }
          
        return false;
    }

    public boolean visit(Sum sum) throws Exception {
        double min = 0;
        double max = 0;
        for (int i = sum.Terms.Count - 1;i >= 0;--i)
        {
            min += sum.Terms[i].Min;
            max += sum.Terms[i].Max;
        }
        if (UpdateInterval(sum, min, max))
        {
            AddChanged(sum);
            return true;
        }
         
        return false;
    }

    public boolean visit(AutoDiff.Variable variable) throws Exception {
        return true;
    }

    public boolean visit(Log log) throws Exception {
        if (UpdateInterval(log, Math.Log(log.Arg.Min), Math.Log(log.Arg.Max)))
        {
            AddChanged(log);
            return true;
        }
         
        return false;
    }

    public boolean visit(Sin sin) throws Exception {
        double size = sin.Arg.Max - sin.Arg.Min;
        boolean c = false;
        if (size <= 2 * Math.PI)
        {
            double a = Math.Sin(sin.Arg.Max);
            double b = Math.Sin(sin.Arg.Min);
            double halfPI = Math.PI / 2;
            double x = Math.Ceiling((sin.Arg.Min - halfPI) / Math.PI);
            double y = Math.Floor((sin.Arg.Max - halfPI) / Math.PI);
            if (x == y)
            {
                //single extrema
                if (((int)x) % 2 == 0)
                {
                    //maxima
                    c = UpdateInterval(sin, Math.Min(a, b), 1);
                }
                else
                {
                    //minima
                    c = UpdateInterval(sin, -1, Math.Max(a, b));
                } 
            }
            else if (x > y)
            {
                //no extrema
                c = UpdateInterval(sin, Math.Min(a, b), Math.Max(a, b));
            }
              
        }
         
        //multiple extrema, don't update
        if (c)
            AddChanged(sin);
         
        return c;
    }

    public boolean visit(Cos cos) throws Exception {
        double size = cos.Arg.Max - cos.Arg.Min;
        boolean c = false;
        if (size <= 2 * Math.PI)
        {
            double a = Math.Cos(cos.Arg.Max);
            double b = Math.Cos(cos.Arg.Min);
            double x = Math.Ceiling(cos.Arg.Min / Math.PI);
            double y = Math.Floor(cos.Arg.Max / Math.PI);
            if (x == y)
            {
                //single extrema
                if (((int)x) % 2 == 0)
                {
                    //maxima
                    c = UpdateInterval(cos, Math.Min(a, b), 1);
                }
                else
                {
                    //minima
                    c = UpdateInterval(cos, -1, Math.Max(a, b));
                } 
            }
            else if (x > y)
            {
                //no extrema
                c = UpdateInterval(cos, Math.Min(a, b), Math.Max(a, b));
            }
              
        }
         
        //multiple extrema, don't update
        if (c)
            AddChanged(cos);
         
        return c;
    }

    public boolean visit(Abs abs) throws Exception {
        boolean containsZero = abs.Arg.Min * abs.Arg.Max <= 0;
        boolean c = false;
        if (containsZero)
            c = UpdateInterval(abs, 0, Math.Max(Math.Abs(abs.Arg.Min), Math.Abs(abs.Arg.Max)));
        else
            c = UpdateInterval(abs, Math.Min(Math.Abs(abs.Arg.Min), Math.Abs(abs.Arg.Max)), Math.Max(Math.Abs(abs.Arg.Min), Math.Abs(abs.Arg.Max))); 
        if (c)
            AddChanged(abs);
         
        return c;
    }

    public boolean visit(Exp exp) throws Exception {
        if (UpdateInterval(exp, Math.Exp(exp.Arg.Min), Math.Exp(exp.Arg.Max)))
        {
            AddChanged(exp);
            return true;
        }
         
        return false;
    }

    public boolean visit(Atan2 atan2) throws Exception {
        throw new NotImplementedException("Atan2 prop not implemented!");
    }

    protected void outputChange(Term t, double oldmin, double oldmax) throws Exception {
        //Console.WriteLine("UW: Interval of {0} is now [{1}, {2}]",t,t.Min,t.Max);
        double oldwidth = oldmax - oldmin;
        double newwidth = t.Max - t.Min;
        if (t instanceof AutoDiff.Variable)
            Console.WriteLine("UW shrinking [{0}..{1}] to [{2}..{3}] by {4} ({5}%)", oldmin, oldmax, t.Min, t.Max, oldwidth - newwidth, (oldwidth - newwidth) / oldwidth * 100);
         
    }

    protected boolean updateInterval(Term t, double min, double max) throws Exception {
        boolean ret = t.Min < min || t.Max > max;
        if (!Double.IsNaN(min))
            t.Min = Math.Max(t.Min, min);
         
        if (!Double.IsNaN(max))
            t.Max = Math.Min(t.Max, max);
         
        if (ret)
            IntervalPropagator.updates++;
         
        IntervalPropagator.visits++;
        if (t.Min > t.Max)
            throw new UnsolveableException();
         
        return ret;
    }

}


