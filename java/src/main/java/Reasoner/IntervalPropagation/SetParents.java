//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:47
//

package Reasoner.IntervalPropagation;

import AutoDiff.*;


//using Alica.Reasoner;
//using Al=Alica;
public class SetParents implements ITermVisitor<Boolean>
{
    public SetParents() throws Exception {
    }

    public Boolean visit(Constant constant) throws Exception {
        return false;
    }

    //return UpdateInterval(constant,constant.Value,constant.Value);
    public Boolean visit(Zero zero) throws Exception {
        return false;
    }

    //return UpdateInterval(zero,0,0);
    public Boolean visit(ConstPower intPower) throws Exception {
        intPower.getBase().Parents.add(intPower);
        return false;
    }

    //intPower.getBase().Accept(this);
    public Boolean visit(TermPower tp) throws Exception {
        tp.getBase().Parents.add(tp);
        tp.getExponent().Parents.add(tp);
        return false;
    }


    public Boolean visit(Product product) throws Exception {
        product.getLeft().Parents.add(product);
        product.getRight().Parents.add(product);
        return false;
    }

    //product.getLeft().Accept(this);
    //product.getRight().Accept(this);
    public Boolean visit(Sigmoid sigmoid) throws Exception {
        sigmoid.getArg().Parents.add(sigmoid);
        sigmoid.getMid().Parents.add(sigmoid);
        return false;
    }

    //sigmoid.getArg().Accept(this);
    //sigmoid.Mid.Accept(this);
    public Boolean visit(LinSigmoid sigmoid) throws Exception {
        sigmoid.getArg().Parents.add(sigmoid);
        return false;
    }

    //sigmoid.getArg().Accept(this);
    //sigmoid.Mid.Accept(this);
    public Boolean visit(LTConstraint constraint) throws Exception {
        constraint.getLeft().Parents.add(constraint);
        constraint.getRight().Parents.add(constraint);
        return false;
    }

    //constraint.getLeft().Accept(this);
    //constraint.getRight().Accept(this);
    public Boolean visit(LTEConstraint constraint) throws Exception {
        constraint.getLeft().Parents.add(constraint);
        constraint.getRight().Parents.add(constraint);
        return false;
    }

    //constraint.getLeft().Accept(this);
    //constraint.getRight().Accept(this);
    public Boolean visit(Min min) throws Exception {
        min.getLeft().Parents.add(min);
        min.getRight().Parents.add(min);
        return false;
    }

    //min.getLeft().Accept(this);
    //min.getRight().Accept(this);
    public Boolean visit(Reification reif) throws Exception {
        reif.getCondition().Parents.add(reif);
        return false;
    }

    @Override
    public Boolean visit(final Negation r) throws Exception
    {
        r.getArg().Parents.add(r);
        return false;
    }

    //reif.Condition.Accept(this);
    public Boolean visit(Max max) throws Exception {
        max.getLeft().Parents.add(max);
        max.getRight().Parents.add(max);
        return false;
    }

    //max.getLeft().Accept(this);
    //max.getRight().Accept(this);
    public Boolean visit(And and) throws Exception {
        and.getLeft().Parents.add(and);
        and.getRight().Parents.add(and);
        return false;
    }

    //and.getLeft().Accept(this);
    //and.getRight().Accept(this);
    public Boolean visit(Or or) throws Exception {
        or.getLeft().Parents.add(or);
        or.getRight().Parents.add(or);
        return false;
    }

    //or.getLeft().Accept(this);
    //or.getRight().Accept(this);
    public Boolean visit(ConstraintUtility cu) throws Exception {
        cu.getConstraint().Parents.add(cu);
        cu.getUtility().Parents.add(cu);
        return false;
    }

    //cu.Constraint.Accept(this);
    //cu.Utility.Accept(this);
    public Boolean visit(Sum sum) throws Exception {
        for (Term t : sum.getTerms())
        {
            t.Parents.add(sum);
        }
        return false;
    }

    //t.Accept(this);
    public Boolean visit(AutoDiff.Variable variable) throws Exception {
        return false;
    }

    public Boolean visit(Log log) throws Exception {
        log.getArg().Parents.add(log);
        return false;
    }

    //log.getArg().Accept(this);
    public Boolean visit(Sin sin) throws Exception {
        sin.getArg().Parents.add(sin);
        return false;
    }

    //sin.getArg().Accept(this);
    public Boolean visit(Cos cos) throws Exception {
        cos.getArg().Parents.add(cos);
        return false;
    }

    //cos.getArg().Accept(this);
    public Boolean visit(Abs abs) throws Exception {
        abs.getArg().Parents.add(abs);
        return false;
    }

    //abs.getArg().Accept(this);
    public Boolean visit(Exp exp) throws Exception {
        exp.getArg().Parents.add(exp);
        return false;
    }

    //exp.getArg().Accept(this);
    public Boolean visit(Atan2 atan2) throws Exception {
        atan2.getLeft().Parents.add(atan2);
        atan2.getRight().Parents.add(atan2);
        return false;
    }

}


//atan2.getLeft().Accept(this);
//atan2.getRight().Accept(this);