//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:47
//

package Reasoner.IntervalPropagation;


//using Alica.Reasoner;
//using Al=Alica;
public class ResetIntervals  extends ITermVisitor<boolean> 
{
    public ResetIntervals() throws Exception {
    }

    public boolean visit(Constant constant) throws Exception {
        constant.Parents.Clear();
        UpdateInterval(constant, constant.Value, constant.Value);
        return true;
    }

    public boolean visit(Zero zero) throws Exception {
        zero.Parents.Clear();
        UpdateInterval(zero, 0, 0);
        return true;
    }

    public boolean visit(ConstPower intPower) throws Exception {
        intPower.Parents.Clear();
        intPower.Base.Accept(this);
        if (intPower.Exponent == 0)
        {
            UpdateInterval(intPower, 0, 0);
            return true;
        }
         
        double e = Math.Round(intPower.Exponent);
        if (intPower.Exponent == e && ((int)e) % 2 == 0)
        {
            UpdateInterval(intPower, 0, Double.PositiveInfinity);
            return true;
        }
         
        UpdateInterval(intPower, Double.NegativeInfinity, Double.PositiveInfinity);
        return false;
    }

    public boolean visit(TermPower tp) throws Exception {
        tp.Parents.Clear();
        tp.Base.Accept(this);
        tp.Exponent.Accept(this);
        UpdateInterval(tp, Double.NegativeInfinity, Double.PositiveInfinity);
        return false;
    }

    public boolean visit(Gp gp) throws Exception {
        throw new NotImplementedException();
        return false;
    }

    public boolean visit(Product product) throws Exception {
        product.Parents.Clear();
        UpdateInterval(product, Double.NegativeInfinity, Double.PositiveInfinity);
        product.Left.Accept(this);
        product.Right.Accept(this);
        return false;
    }

    public boolean visit(Sigmoid sigmoid) throws Exception {
        sigmoid.Parents.Clear();
        sigmoid.Arg.Accept(this);
        sigmoid.Mid.Accept(this);
        UpdateInterval(sigmoid, 0, 1);
        return true;
    }

    public boolean visit(LinSigmoid sigmoid) throws Exception {
        sigmoid.Parents.Clear();
        sigmoid.Arg.Accept(this);
        UpdateInterval(sigmoid, 0, 1);
        return true;
    }

    public boolean visit(LTConstraint constraint) throws Exception {
        constraint.Parents.Clear();
        constraint.Left.Accept(this);
        constraint.Right.Accept(this);
        UpdateInterval(constraint, Double.NegativeInfinity, 1);
        return true;
    }

    public boolean visit(LTEConstraint constraint) throws Exception {
        constraint.Parents.Clear();
        constraint.Left.Accept(this);
        constraint.Right.Accept(this);
        UpdateInterval(constraint, Double.NegativeInfinity, 1);
        return true;
    }

    public boolean visit(Min min) throws Exception {
        min.Parents.Clear();
        min.Left.Accept(this);
        min.Right.Accept(this);
        UpdateInterval(min, Double.NegativeInfinity, Double.PositiveInfinity);
        return true;
    }

    public boolean visit(Reification reif) throws Exception {
        reif.Parents.Clear();
        reif.Condition.Accept(this);
        UpdateInterval(reif, reif.MinVal, reif.MaxVal);
        return true;
    }

    public boolean visit(Max max) throws Exception {
        max.Parents.Clear();
        max.Left.Accept(this);
        max.Right.Accept(this);
        UpdateInterval(max, Double.NegativeInfinity, Double.PositiveInfinity);
        return true;
    }

    public boolean visit(And and) throws Exception {
        and.Parents.Clear();
        and.Left.Accept(this);
        and.Right.Accept(this);
        UpdateInterval(and, Double.NegativeInfinity, 1);
        return true;
    }

    //UpdateInterval(and,1,1); //enforce the purely conjunctive problem
    public boolean visit(Or or) throws Exception {
        or.Parents.Clear();
        or.Left.Accept(this);
        or.Right.Accept(this);
        UpdateInterval(or, Double.NegativeInfinity, 1);
        return true;
    }

    public boolean visit(ConstraintUtility cu) throws Exception {
        cu.Parents.Clear();
        cu.Constraint.Accept(this);
        cu.Utility.Accept(this);
        UpdateInterval(cu, 1, Double.PositiveInfinity);
        return true;
    }

    public boolean visit(Sum sum) throws Exception {
        sum.Parents.Clear();
        for (Object __dummyForeachVar0 : sum.Terms)
        {
            Term t = (Term)__dummyForeachVar0;
            t.Accept(this);
        }
        UpdateInterval(sum, Double.NegativeInfinity, Double.PositiveInfinity);
        return true;
    }

    public boolean visit(AutoDiff.Variable variable) throws Exception {
        variable.Parents.Clear();
        UpdateInterval(variable, variable.GlobalMin, variable.GlobalMax);
        return true;
    }

    public boolean visit(Log log) throws Exception {
        log.Parents.Clear();
        log.Arg.Accept(this);
        UpdateInterval(log, Double.NegativeInfinity, Double.PositiveInfinity);
        return true;
    }

    public boolean visit(Sin sin) throws Exception {
        sin.Parents.Clear();
        sin.Arg.Accept(this);
        UpdateInterval(sin, -1, 1);
        return true;
    }

    public boolean visit(Cos cos) throws Exception {
        cos.Parents.Clear();
        cos.Arg.Accept(this);
        UpdateInterval(cos, -1, 1);
        return true;
    }

    public boolean visit(Abs abs) throws Exception {
        abs.Parents.Clear();
        abs.Arg.Accept(this);
        UpdateInterval(abs, 0, Double.PositiveInfinity);
        return true;
    }

    public boolean visit(Exp exp) throws Exception {
        exp.Parents.Clear();
        exp.Arg.Accept(this);
        UpdateInterval(exp, 0, Double.PositiveInfinity);
        return true;
    }

    public boolean visit(Atan2 atan2) throws Exception {
        atan2.Parents.Clear();
        atan2.Left.Accept(this);
        atan2.Right.Accept(this);
        UpdateInterval(atan2, -Math.PI, Math.PI);
        return true;
    }

    private void updateInterval(Term t, double min, double max) throws Exception {
        t.Min = min;
        t.Max = max;
        return ;
    }

}


