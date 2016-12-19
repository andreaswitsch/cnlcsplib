//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:47
//

package Reasoner.IntervalPropagation;
import AutoDiff.*;

//using Alica.Reasoner;
//using Al=Alica;
public class ResetIntervals implements ITermVisitor<Boolean>
{
    public ResetIntervals() throws Exception {
    }

    public Boolean visit(Constant constant) throws Exception {
        constant.Parents.clear();
        updateInterval(constant, constant.getValue(), constant.getValue());
        return true;
    }

    public Boolean visit(Zero zero) throws Exception {
        zero.Parents.clear();
        updateInterval(zero, 0, 0);
        return true;
    }

    public Boolean visit(ConstPower intPower) throws Exception {
        intPower.Parents.clear();
        intPower.getBase().accept(this);
        if (intPower.getExponent() == 0)
        {
            updateInterval(intPower, 0, 0);
            return true;
        }
         
        double e = Math.round(intPower.getExponent());
        if (intPower.getExponent() == e && ((int)e) % 2 == 0)
        {
            updateInterval(intPower, 0, Double.POSITIVE_INFINITY);
            return true;
        }
         
        updateInterval(intPower, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return false;
    }

    public Boolean visit(TermPower tp) throws Exception {
        tp.Parents.clear();
        tp.getBase().accept(this);
        tp.getExponent().accept(this);
        updateInterval(tp, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return false;
    }

    public Boolean visit(Product product) throws Exception {
        product.Parents.clear();
        updateInterval(product, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        product.getLeft().accept(this);
        product.getRight().accept(this);
        return false;
    }

    public Boolean visit(Sigmoid sigmoid) throws Exception {
        sigmoid.Parents.clear();
        sigmoid.getArg().accept(this);
        sigmoid.getMid().accept(this);
        updateInterval(sigmoid, 0, 1);
        return true;
    }

    public Boolean visit(LinSigmoid sigmoid) throws Exception {
        sigmoid.Parents.clear();
        sigmoid.getArg().accept(this);
        updateInterval(sigmoid, 0, 1);
        return true;
    }

    public Boolean visit(LTConstraint constraint) throws Exception {
        constraint.Parents.clear();
        constraint.getLeft().accept(this);
        constraint.getRight().accept(this);
        updateInterval(constraint, Double.NEGATIVE_INFINITY, 1);
        return true;
    }

    public Boolean visit(LTEConstraint constraint) throws Exception {
        constraint.Parents.clear();
        constraint.getLeft().accept(this);
        constraint.getRight().accept(this);
        updateInterval(constraint, Double.NEGATIVE_INFINITY, 1);
        return true;
    }

    public Boolean visit(Min min) throws Exception {
        min.Parents.clear();
        min.getLeft().accept(this);
        min.getRight().accept(this);
        updateInterval(min, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return true;
    }

    public Boolean visit(Reification reif) throws Exception {
        reif.Parents.clear();
        reif.getCondition().accept(this);
        updateInterval(reif, reif.getMinVal(), reif.getMaxVal());
        return true;
    }

    @Override
    public Boolean visit(final Negation r) throws Exception
    {
        return true;
    }

    public Boolean visit(Max max) throws Exception {
        max.Parents.clear();
        max.getLeft().accept(this);
        max.getRight().accept(this);
        updateInterval(max, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return true;
    }

    public Boolean visit(And and) throws Exception {
        and.Parents.clear();
        and.getLeft().accept(this);
        and.getRight().accept(this);
        updateInterval(and, Double.NEGATIVE_INFINITY, 1);
        return true;
    }

    //updateInterval(and,1,1); //enforce the purely conjunctive problem
    public Boolean visit(Or or) throws Exception {
        or.Parents.clear();
        or.getLeft().accept(this);
        or.getRight().accept(this);
        updateInterval(or, Double.NEGATIVE_INFINITY, 1);
        return true;
    }

    public Boolean visit(ConstraintUtility cu) throws Exception {
        cu.Parents.clear();
        cu.getConstraint().accept(this);
        cu.getUtility().accept(this);
        updateInterval(cu, 1, Double.POSITIVE_INFINITY);
        return true;
    }

    public Boolean visit(Sum sum) throws Exception {
        sum.Parents.clear();
        for (Object __dummyForeachVar0 : sum.getTerms())
        {
            Term t = (Term)__dummyForeachVar0;
            t.accept(this);
        }
        updateInterval(sum, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return true;
    }

    public Boolean visit(AutoDiff.Variable variable) throws Exception {
        variable.Parents.clear();
        updateInterval(variable, variable.GlobalMin, variable.GlobalMax);
        return true;
    }

    public Boolean visit(Log log) throws Exception {
        log.Parents.clear();
        log.getArg().accept(this);
        updateInterval(log, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return true;
    }

    public Boolean visit(Sin sin) throws Exception {
        sin.Parents.clear();
        sin.getArg().accept(this);
        updateInterval(sin, -1, 1);
        return true;
    }

    public Boolean visit(Cos cos) throws Exception {
        cos.Parents.clear();
        cos.getArg().accept(this);
        updateInterval(cos, -1, 1);
        return true;
    }

    public Boolean visit(Abs abs) throws Exception {
        abs.Parents.clear();
        abs.getArg().accept(this);
        updateInterval(abs, 0, Double.POSITIVE_INFINITY);
        return true;
    }

    public Boolean visit(Exp exp) throws Exception {
        exp.Parents.clear();
        exp.getArg().accept(this);
        updateInterval(exp, 0, Double.POSITIVE_INFINITY);
        return true;
    }

    public Boolean visit(Atan2 atan2) throws Exception {
        atan2.Parents.clear();
        atan2.getLeft().accept(this);
        atan2.getRight().accept(this);
        updateInterval(atan2, -Math.PI, Math.PI);
        return true;
    }

    private void updateInterval(Term t, double min, double max) throws Exception {
        t.Min = min;
        t.Max = max;
        return ;
    }

}


