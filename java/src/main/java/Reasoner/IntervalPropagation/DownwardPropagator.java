//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.IntervalPropagation;

import AutoDiff.Abs;
import AutoDiff.And;
import AutoDiff.Atan2;
import AutoDiff.ConstPower;
import AutoDiff.Constant;
import AutoDiff.ConstraintUtility;
import AutoDiff.Cos;
import AutoDiff.Exp;
import AutoDiff.ITermVisitor;
import AutoDiff.LTConstraint;
import AutoDiff.LTEConstraint;
import AutoDiff.LinSigmoid;
import AutoDiff.Log;
import AutoDiff.Max;
import AutoDiff.Min;
import AutoDiff.Negation;
import AutoDiff.Or;
import AutoDiff.Product;
import AutoDiff.Reification;
import AutoDiff.Sigmoid;
import AutoDiff.Sin;
import AutoDiff.Sum;
import AutoDiff.Term;
import AutoDiff.TermPower;
import AutoDiff.Zero;
import org.apache.commons.lang3.NotImplementedException;

//#define DEBUG_DP
//using Alica.Reasoner;
//using Al=Alica;
public class DownwardPropagator implements ITermVisitor<Boolean>
{
    public TermList Changed;
    //internal Queue<Term> Changed;
    private void addChanged(Term t) throws Exception {
        if (!Changed.contains(t))
            Changed.enqueue(t);
         
    }

    public DownwardPropagator() throws Exception {
    }

    public Boolean visit(Constant constant) throws Exception {
        return false;
    }

    public Boolean visit(Zero zero) throws Exception {
        return false;
    }

    public Boolean visit(ConstPower intPower) throws Exception {
        if (intPower.Max == Double.POSITIVE_INFINITY || intPower.Min == Double.NEGATIVE_INFINITY)
            return false;
         
        double a = Math.pow(intPower.Min, 1 / intPower.getExponent());
        double b = Math.pow(intPower.Max, 1 / intPower.getExponent());
        Boolean isRational = intPower.getExponent() != Math.round(intPower.getExponent());
        if (isRational)
        {
            if (updateInterval(intPower.getBase(), Math.max(0, Math.min(a, b)), Math.max(a, Math.max(-a, Math.max(b, -b)))))
            {
                addChanged(intPower.getBase());
                return true;
            }
             
        }
        else
        {
            double min;
            double max;
            if (intPower.getExponent() >= 0)
            {
                if (intPower.getBase().Max <= 0)
                {
                    max = Math.max(-Math.abs(a), -Math.abs(b));
                }
                else
                    max = Math.max(a, Math.max(-a, Math.max(b, -b))); 
                if (intPower.getBase().Min >= 0)
                {
                    min = Math.min(Math.abs(a), Math.abs(b));
                }
                else
                    min = Math.min(a, Math.min(-a, Math.min(b, -b))); 
            }
            else
            {
                //this case can be improved
                max = Math.max(a, Math.max(-a, Math.max(b, -b)));
                min = Math.min(a, Math.min(-a, Math.min(b, -b)));
            } 
            if (updateInterval(intPower.getBase(), min, max))
            {
                addChanged(intPower.getBase());
                return true;
            }
             
        } 
        return false;
    }

    public Boolean visit(TermPower tp) throws Exception {
        throw new NotImplementedException("Propagation for TermPower not implemented");
    }


    public Boolean visit(Product product) throws Exception {
        /*
        			 * a*b = c
        			 * ==> a = c/b
        			 * */
        if (product.getLeft() == product.getRight())
        {
            double a = Math.sqrt(product.Min);
            double b = Math.sqrt(product.Max);
            double min;
            double max;
            if (product.getLeft().Max <= 0)
            {
                max = Math.max(-a, -b);
            }
            else
                max = Math.max(a, b); 
            if (product.getLeft().Min >= 0)
            {
                min = Math.min(a, b);
            }
            else
                min = Math.min(-a, -b); 
            if (updateInterval(product.getLeft(), min, max))
            {
                addChanged(product.getLeft());
                return true;
            }
             
        }
        else
        {
            Boolean c = false, d = false;
            if (product.getRight().Min * product.getRight().Max > 0)
            {
                //Left:
                double aa = product.Min / product.getRight().Min;
                double ab = product.Min / product.getRight().Max;
                double ba = product.Max / product.getRight().Min;
                double bb = product.Max / product.getRight().Max;
                double min = Math.min(aa, Math.min(ab, Math.min(ba, bb)));
                double max = Math.max(aa, Math.max(ab, Math.max(ba, bb)));
                c = updateInterval(product.getLeft(), min, max);
                if (c)
                    addChanged(product.getLeft());
                 
            }
             
            if (product.getLeft().Min * product.getLeft().Max > 0)
            {
                //Right:
                double aa = product.Min / product.getLeft().Min;
                double ab = product.Min / product.getLeft().Max;
                double ba = product.Max / product.getLeft().Min;
                double bb = product.Max / product.getLeft().Max;
                double min = Math.min(aa, Math.min(ab, Math.min(ba, bb)));
                double max = Math.max(aa, Math.max(ab, Math.max(ba, bb)));
                d = updateInterval(product.getRight(), min, max);
                if (d)
                    addChanged(product.getRight());
                 
            }
             
            return c || d;
        } 
        return false;
    }

    public Boolean visit(Sigmoid sigmoid) throws Exception {
        throw new NotImplementedException("Sigmoidal propagation not implemented");
    }

    public Boolean visit(LinSigmoid sigmoid) throws Exception {
        throw new NotImplementedException("LinSigmoid propagation not implemented");
    }

    public Boolean visit(LTConstraint constraint) throws Exception {
        Boolean changed = false;
        if (constraint.Min > 0)
        {
            if (updateInterval(constraint.getRight(), constraint.getLeft().Min, Double.POSITIVE_INFINITY))
            {
                addChanged(constraint.getRight());
                changed = true;
            }
             
            if (updateInterval(constraint.getLeft(), Double.NEGATIVE_INFINITY, constraint.getRight().Max))
            {
                addChanged(constraint.getLeft());
                changed = true;
            }
             
        }
        else if (constraint.Max <= 0)
        {
            if (updateInterval(constraint.getRight(), Double.NEGATIVE_INFINITY, constraint.getLeft().Max))
            {
                addChanged(constraint.getRight());
                changed = true;
            }
             
            if (updateInterval(constraint.getLeft(), constraint.getRight().Min, Double.POSITIVE_INFINITY))
            {
                addChanged(constraint.getLeft());
                changed = true;
            }
             
        }
          
        return changed;
    }

    public Boolean visit(LTEConstraint constraint) throws Exception {
        Boolean changed = false;
        if (constraint.Min > 0)
        {
            if (updateInterval(constraint.getRight(), constraint.getLeft().Min, Double.POSITIVE_INFINITY))
            {
                addChanged(constraint.getRight());
                changed = true;
            }
             
            if (updateInterval(constraint.getLeft(), Double.NEGATIVE_INFINITY, constraint.getRight().Max))
            {
                addChanged(constraint.getLeft());
                changed = true;
            }
             
        }
        else if (constraint.Max <= 0)
        {
            if (updateInterval(constraint.getRight(), Double.NEGATIVE_INFINITY, constraint.getLeft().Max))
            {
                addChanged(constraint.getRight());
                changed = true;
            }
             
            if (updateInterval(constraint.getLeft(), constraint.getRight().Min, Double.POSITIVE_INFINITY))
            {
                addChanged(constraint.getLeft());
                changed = true;
            }
             
        }
          
        return changed;
    }

    public Boolean visit(Min min) throws Exception {
        Boolean c1 = updateInterval(min.getLeft(), min.Min, Double.POSITIVE_INFINITY);
        Boolean c2 = updateInterval(min.getRight(), min.Min, Double.POSITIVE_INFINITY);
        if (c1)
            addChanged(min.getLeft());
         
        if (c2)
            addChanged(min.getRight());
         
        return c1 || c2;
    }

    public Boolean visit(Max max) throws Exception {
        if (max.Min > 0)
        {
            Boolean c = false;
            if (max.getLeft().Max <= 0)
            {
                Boolean c1 = updateInterval(max.getRight(), 1, 1);
                if (c1)
                    addChanged(max.getRight());
                 
                c |= c1;
            }
             
            if (max.getRight().Max <= 0)
            {
                Boolean c2 = updateInterval(max.getLeft(), 1, 1);
                if (c2)
                    addChanged(max.getLeft());
                 
                c |= c2;
            }
             
            return c;
        }
         
        return false;
    }

    public Boolean visit(And and) throws Exception {
        Boolean changed = false;
        if (and.Min > 0)
        {
            if (updateInterval(and.getLeft(), 1, 1))
            {
                addChanged(and.getLeft());
                changed = true;
            }
             
            if (updateInterval(and.getRight(), 1, 1))
            {
                addChanged(and.getRight());
                changed = true;
            }
             
        }
         
        return changed;
    }

    public Boolean visit(Or or) throws Exception {
        throw new NotImplementedException("Or operator progation not implemented (max is used)");
    }

    //return false;
    public Boolean visit(ConstraintUtility cu) throws Exception {
        Boolean c = false;
        if (cu.Min >= 1)
        {
            if (updateInterval(cu.getConstraint(), 1, 1))
            {
                addChanged(cu.getConstraint());
                c = true;
            }
             
            if (updateInterval(cu.getUtility(), 1, cu.Max))
            {
                addChanged(cu.getUtility());
                c = true;
            }
             
        }
         
        return c;
    }

    public Boolean visit(Sum sum) throws Exception {
        //a+b= c
        // a= b-c
        //a:
        Boolean changed = false;
        Boolean anychange = false;
        do
        {
            changed = false;
            for (int i = 0;i < sum.getTerms().length;++i)
            {
                double minother = 0;
                double maxother = 0;
                for (int j = 0;j < sum.getTerms().length;++j)
                {
                    if (i == j)
                        continue;
                     
                    minother += sum.getTerms()[j].Min;
                    maxother += sum.getTerms()[j].Max;
                }
                /*Console.WriteLine("-______S({0} {1})",sum.Min,sum.Max);
                					Console.WriteLine("-______O({0} {1})",maxother,minother);
                					Console.WriteLine("-______>DW {0} to {1} {2} I am {3}",t,sum.Min-maxother,sum.Max-minother,sum);
                					*/
                if (updateInterval(sum.getTerms()[i], sum.Min - maxother, sum.Max - minother))
                {
                    addChanged(sum.getTerms()[i]);
                    changed = true;
                    anychange = true;
                }
                 
            }
        }
        while (changed);
        return anychange;
    }

    public Boolean visit(AutoDiff.Variable variable) throws Exception {
        return false;
    }

    public Boolean visit(Reification reif) throws Exception {
        Boolean c = false;
        if (reif.Max < reif.getMaxVal())
        {
            c = updateInterval(reif, reif.getMinVal(), reif.getMinVal());
            if (c)
                addChanged(reif);
             
            if (updateInterval(reif.getCondition(), Double.NEGATIVE_INFINITY, 0))
            {
                addChanged(reif.getCondition());
                c = true;
            }
             
        }
        else if (reif.Min > reif.getMinVal())
        {
            c = updateInterval(reif, reif.getMaxVal(), reif.getMaxVal());
            if (c)
                addChanged(reif);
             
            if (updateInterval(reif.getCondition(), 1, 1))
            {
                addChanged(reif.getCondition());
                c = true;
            }
             
        }
          
        return c;
    }

    @Override
    public Boolean visit(final Negation r) throws Exception
    {
        throw new NotImplementedException("Negation");
    }

    public Boolean visit(Log log) throws Exception {
        double a = Math.exp(log.Min);
        double b = Math.exp(log.Max);
        if (updateInterval(log.getArg(), a, b))
        {
            addChanged(log.getArg());
            return true;
        }
         
        return false;
    }

    public Boolean visit(Sin sin) throws Exception {
        if (sin.Min == -1.0 && sin.Max == 1.0)
            return false;
         
        double cdist = sin.getArg().Max - sin.getArg().Min;
        if (cdist >= Math.PI)
            return false;
         
        //Console.WriteLine("Sine Prop Sine interval: [{0}, {1}]",sin.Min,sin.Max);
        //Console.WriteLine("getArg() interval: [{0}, {1}]",sin.getArg().Min,sin.getArg().Max);
        double a = Math.asin(sin.Min);
        double b = Math.asin(sin.Max);
        //-pi/2..pi/2
        double t;
        if (a > b)
        {
            t = b;
            b = a;
            a = t;
        }
         
        //now a<= b;
        double c = Math.PI - b;
        double d = Math.PI - a;
        double n1 = Math.ceil((sin.getArg().Min - a) / (2 * Math.PI));
        //double n1a = Math.Floor((sin.getArg().Max - a) / (2*Math.PI));
        double n2 = Math.floor((sin.getArg().Max - b) / (2 * Math.PI));
        //double n2a = Math.Ceiling((sin.getArg().Min - b)   /   (2*Math.PI));
        double n3 = Math.ceil((sin.getArg().Min - c) / (2 * Math.PI));
        //double n3a = Math.Floor((sin.getArg().Max - c) / (2*Math.PI));
        double n4 = Math.floor((sin.getArg().Max - d) / (2 * Math.PI));
        //double n4a = Math.Ceiling((sin.getArg().Min - d)   /   (2*Math.PI));
        //Console.WriteLine("N: {0} {1} {2} {3}",n1,n2,n3,n4);
        //Console.WriteLine("P: {0} {1} {2} {3}",n1*2*Math.PI+a,n2*2*Math.PI+b,n3*2*Math.PI+c,n4*2*Math.PI+d);
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double n1a = n1 * 2 * Math.PI + a;
        double n2b = n2 * 2 * Math.PI + b;
        Boolean faulty = true;
        if (n1a <= sin.getArg().Max && n2b >= sin.getArg().Min)
        {
            //interval 1 completely enclosed
            min = Math.min(min, n1a);
            max = Math.max(max, n2b);
            faulty = false;
        }
        else
        {
        } 
        //no bound is inside as adding interval is smaller than pi
        double n3c = n3 * 2 * Math.PI + c;
        double n4d = n4 * 2 * Math.PI + d;
        if (n3c <= sin.getArg().Max && n4d >= sin.getArg().Min)
        {
            //interval 2 completely enclosed
            min = Math.min(min, n3c);
            max = Math.max(max, n4d);
            faulty = false;
        }
        else
        {
        } 
        //no bound is inside as adding interval is smaller than pi
        if (faulty)
        {
            throw new UnsolveableException();
        }
         
        //return false; //updateInterval(sin.getArg(),sin.getArg().Max,sin.getArg().Min); //no solution possible
        /*if (n1 == n2) { //bound within interval
        				min = Math.min(min,n1*2*Math.PI+a);
        				max = Math.max(max,n2*2*Math.PI+b);
        			} else {
        				if (n1 > n2) { //lower bound cut
        					
        					min = Math.min(min,sin.getArg().Min);
        					max = Math.max(max,n2*2*Math.PI+b);
        					
        					double k = 
        					
        				}
        			}
        			
        			//if (n1 == n2 && n3 == n4) { //bind to rectangle:
        				double min = Math.min(n1*2*Math.PI+a,n3*2*Math.PI+c);
        				double max = Math.max(n2*2*Math.PI+b,n4*2*Math.PI+d);
        			*/
        //}
        if (min == Double.MAX_VALUE)
            min = Double.NEGATIVE_INFINITY;
         
        if (max == -Double.MAX_VALUE)
            max = Double.POSITIVE_INFINITY;
         
        if (updateInterval(sin.getArg(), min, max))
        {
            addChanged(sin.getArg());
            return true;
        }
         
        return false;
    }

    public Boolean visit(Cos cos) throws Exception {
        if (cos.Min == -1.0 && cos.Max == 1.0)
            return false;
         
        double cdist = cos.getArg().Max - cos.getArg().Min;
        if (cdist >= Math.PI)
            return false;
         
        //Console.WriteLine("Cos Prop Sine interval: [{0}, {1}]",cos.Min,cos.Max);
        //Console.WriteLine("getArg() interval: [{0}, {1}]",cos.getArg().Min,cos.getArg().Max);
        double a = Math.acos(cos.Min);
        double b = Math.acos(cos.Max);
        //0..pi
        double t;
        if (a > b)
        {
            t = b;
            b = a;
            a = t;
        }
         
        //now a<= b;
        double c = -b;
        double d = -a;
        double n1 = Math.ceil((cos.getArg().Min - a) / (2 * Math.PI));
        //double n1a = Math.Floor((sin.getArg().Max - a) / (2*Math.PI));
        double n2 = Math.floor((cos.getArg().Max - b) / (2 * Math.PI));
        //double n2a = Math.Ceiling((sin.getArg().Min - b)   /   (2*Math.PI));
        double n3 = Math.ceil((cos.getArg().Min - c) / (2 * Math.PI));
        //double n3a = Math.Floor((sin.getArg().Max - c) / (2*Math.PI));
        double n4 = Math.floor((cos.getArg().Max - d) / (2 * Math.PI));
        //double n4a = Math.Ceiling((sin.getArg().Min - d)   /   (2*Math.PI));
        //Console.WriteLine("N: {0} {1} {2} {3}",n1,n2,n3,n4);
        //Console.WriteLine("P: {0} {1} {2} {3}",n1*2*Math.PI+a,n2*2*Math.PI+b,n3*2*Math.PI+c,n4*2*Math.PI+d);
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double n1a = n1 * 2 * Math.PI + a;
        double n2b = n2 * 2 * Math.PI + b;
        Boolean faulty = true;
        if (n1a <= cos.getArg().Max && n2b >= cos.getArg().Min)
        {
            //interval 1 completely enclosed
            min = Math.min(min, n1a);
            max = Math.max(max, n2b);
            faulty = false;
        }
        else
        {
        } 
        //no bound is inside as adding interval is smaller than pi
        double n3c = n3 * 2 * Math.PI + c;
        double n4d = n4 * 2 * Math.PI + d;
        if (n3c <= cos.getArg().Max && n4d >= cos.getArg().Min)
        {
            //interval 2 completely enclosed
            min = Math.min(min, n3c);
            max = Math.max(max, n4d);
            faulty = false;
        }
        else
        {
        } 
        //no bound is inside as adding interval is smaller than pi
        if (faulty)
        {
            throw new UnsolveableException();
        }
         
        //return false;//return updateInterval(cos.getArg(),cos.getArg().Max,cos.getArg().Min); //no solution possible
        if (min == Double.MAX_VALUE)
            min = -Double.MAX_VALUE;
         
        if (max == -Double.MAX_VALUE)
            max = Double.MAX_VALUE;
         
        if (updateInterval(cos.getArg(), min, max))
        {
            addChanged(cos.getArg());
            return true;
        }
         
        return false;
    }

    public Boolean visit(Abs abs) throws Exception {
        if (updateInterval(abs.getArg(), -abs.Max, abs.Max))
        {
            addChanged(abs.getArg());
            return true;
        }
         
        return false;
    }

    public Boolean visit(Exp exp) throws Exception {
        double a = Math.log(exp.Min);
        double b = Math.log(exp.Max);
        if (updateInterval(exp.getArg(), a, b))
        {
            addChanged(exp.getArg());
            return true;
        }
         
        return false;
    }

    public Boolean visit(Atan2 atan2) throws Exception {
        throw new NotImplementedException("Atan2 propagation not implemented");
    }

    //return false;
    protected void outputChange(Term t, double oldmin, double oldmax) throws Exception {
        //Console.WriteLine("DW: Interval of {0} is now [{1}, {2}]",t,t.Min,t.Max);
        double oldwidth = oldmax - oldmin;
        double newwidth = t.Max - t.Min;
        System.out.println("not yet implemented");
//        if (t instanceof AutoDiff.Variable)
//            Console.WriteLine("DW shrinking [{0}..{1}] to [{2}..{3}] by {4} ({5}%)", oldmin, oldmax, t.Min, t.Max, oldwidth - newwidth, (oldwidth - newwidth) / oldwidth * 100);
         
    }

    protected Boolean updateInterval(Term t, double min, double max) throws Exception {
        Boolean ret = t.Min < min || t.Max > max;
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


