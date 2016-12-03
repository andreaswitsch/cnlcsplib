//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:47
//

package Reasoner.IntervalPropagation;


//using Alica.Reasoner;
//using Al=Alica;
public class SetParents  extends ITermVisitor<boolean> 
{
    public SetParents() throws Exception {
    }

    public boolean visit(Constant constant) throws Exception {
        return false;
    }

    //return UpdateInterval(constant,constant.Value,constant.Value);
    public boolean visit(Zero zero) throws Exception {
        return false;
    }

    //return UpdateInterval(zero,0,0);
    public boolean visit(ConstPower intPower) throws Exception {
        intPower.Base.Parents.Add(intPower);
        return false;
    }

    //intPower.Base.Accept(this);
    public boolean visit(TermPower tp) throws Exception {
        tp.Base.Parents.Add(tp);
        tp.Exponent.Parents.Add(tp);
        return false;
    }

    public boolean visit(Gp gp) throws Exception {
        throw new NotImplementedException();
        return false;
    }

    public boolean visit(Product product) throws Exception {
        product.Left.Parents.Add(product);
        product.Right.Parents.Add(product);
        return false;
    }

    //product.Left.Accept(this);
    //product.Right.Accept(this);
    public boolean visit(Sigmoid sigmoid) throws Exception {
        sigmoid.Arg.Parents.Add(sigmoid);
        sigmoid.Mid.Parents.Add(sigmoid);
        return false;
    }

    //sigmoid.Arg.Accept(this);
    //sigmoid.Mid.Accept(this);
    public boolean visit(LinSigmoid sigmoid) throws Exception {
        sigmoid.Arg.Parents.Add(sigmoid);
        return false;
    }

    //sigmoid.Arg.Accept(this);
    //sigmoid.Mid.Accept(this);
    public boolean visit(LTConstraint constraint) throws Exception {
        constraint.Left.Parents.Add(constraint);
        constraint.Right.Parents.Add(constraint);
        return false;
    }

    //constraint.Left.Accept(this);
    //constraint.Right.Accept(this);
    public boolean visit(LTEConstraint constraint) throws Exception {
        constraint.Left.Parents.Add(constraint);
        constraint.Right.Parents.Add(constraint);
        return false;
    }

    //constraint.Left.Accept(this);
    //constraint.Right.Accept(this);
    public boolean visit(Min min) throws Exception {
        min.Left.Parents.Add(min);
        min.Right.Parents.Add(min);
        return false;
    }

    //min.Left.Accept(this);
    //min.Right.Accept(this);
    public boolean visit(Reification reif) throws Exception {
        reif.Condition.Parents.Add(reif);
        return false;
    }

    //reif.Condition.Accept(this);
    public boolean visit(Max max) throws Exception {
        max.Left.Parents.Add(max);
        max.Right.Parents.Add(max);
        return false;
    }

    //max.Left.Accept(this);
    //max.Right.Accept(this);
    public boolean visit(And and) throws Exception {
        and.Left.Parents.Add(and);
        and.Right.Parents.Add(and);
        return false;
    }

    //and.Left.Accept(this);
    //and.Right.Accept(this);
    public boolean visit(Or or) throws Exception {
        or.Left.Parents.Add(or);
        or.Right.Parents.Add(or);
        return false;
    }

    //or.Left.Accept(this);
    //or.Right.Accept(this);
    public boolean visit(ConstraintUtility cu) throws Exception {
        cu.Constraint.Parents.Add(cu);
        cu.Utility.Parents.Add(cu);
        return false;
    }

    //cu.Constraint.Accept(this);
    //cu.Utility.Accept(this);
    public boolean visit(Sum sum) throws Exception {
        for (Object __dummyForeachVar0 : sum.Terms)
        {
            Term t = (Term)__dummyForeachVar0;
            t.Parents.Add(sum);
        }
        return false;
    }

    //t.Accept(this);
    public boolean visit(AutoDiff.Variable variable) throws Exception {
        return false;
    }

    public boolean visit(Log log) throws Exception {
        log.Arg.Parents.Add(log);
        return false;
    }

    //log.Arg.Accept(this);
    public boolean visit(Sin sin) throws Exception {
        sin.Arg.Parents.Add(sin);
        return false;
    }

    //sin.Arg.Accept(this);
    public boolean visit(Cos cos) throws Exception {
        cos.Arg.Parents.Add(cos);
        return false;
    }

    //cos.Arg.Accept(this);
    public boolean visit(Abs abs) throws Exception {
        abs.Arg.Parents.Add(abs);
        return false;
    }

    //abs.Arg.Accept(this);
    public boolean visit(Exp exp) throws Exception {
        exp.Arg.Parents.Add(exp);
        return false;
    }

    //exp.Arg.Accept(this);
    public boolean visit(Atan2 atan2) throws Exception {
        atan2.Left.Parents.Add(atan2);
        atan2.Right.Parents.Add(atan2);
        return false;
    }

}


//atan2.Left.Accept(this);
//atan2.Right.Accept(this);