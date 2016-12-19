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
import AutoDiff.Variable;
import AutoDiff.Zero;

//using Alica.Reasoner;
//using Al=Alica;
public class RecursivePropagate implements ITermVisitor
{
    //internal Queue<Term> Changed;
    public TermList Changed;
    DownwardPropagator dp = new DownwardPropagator();
    UpwardPropagator up = new UpwardPropagator();
    SetParents sp = new SetParents();
    public RecursivePropagate() throws Exception {
        this.Changed = new TermList();
        dp.Changed = this.Changed;
        up.Changed = this.Changed;
    }

    public void propagate(Term t) throws Exception {
        this.Changed.clear();
        t.accept(this);
        Term cur = this.Changed.First;
        while (cur != null)
        {
            cur.accept(this.sp);
            cur = cur.Next;
        }
        cur = this.Changed.dequeue();
        while (cur != null)
        {
            cur.accept(this.dp);
            cur.accept(this.up);
            cur = this.Changed.dequeue();
        }
    }

    private void addToQueue(Term t) throws Exception {
        if (!this.Changed.contains(t))
            this.Changed.enqueue(t);
         
    }

    public Object visit(Constant constant) throws Exception {
        return null;
    }

    //	return false;
    public Object visit(Zero zero) throws Exception {
        return null;
    }

    //	return false;
    public Object visit(ConstPower intPower) throws Exception {
        addToQueue(intPower);
        intPower.getBase().accept(this);
        return null;
    }

    //return true;
    public Object visit(TermPower intPower) throws Exception {
        addToQueue(intPower);
        intPower.getBase().accept(this);
        intPower.getExponent().accept(this);
        return null;
    }


    //return true;
    public Object visit(Product product) throws Exception {
        addToQueue(product);
        product.getLeft().accept(this);
        product.getRight().accept(this);
        return null;
    }

    //return true;
    public Object visit(Sigmoid sigmoid) throws Exception {
        addToQueue(sigmoid);
        sigmoid.getArg().accept(this);
        sigmoid.getMid().accept(this);
        return null;
    }

    //return true;
    public Object visit(LinSigmoid sigmoid) throws Exception {
        addToQueue(sigmoid);
        sigmoid.getArg().accept(this);
        return null;
    }

    //return true;
    public Object visit(LTConstraint constraint) throws Exception {
        addToQueue(constraint);
        constraint.getLeft().accept(this);
        constraint.getRight().accept(this);
        return null;
    }

    //return true;
    public Object visit(LTEConstraint constraint) throws Exception {
        addToQueue(constraint);
        constraint.getLeft().accept(this);
        constraint.getRight().accept(this);
        return null;
    }

    //return true;
    public Object visit(Min min) throws Exception {
        addToQueue(min);
        min.getLeft().accept(this);
        min.getRight().accept(this);
        return null;
    }

    //return true;
    public Object visit(Max max) throws Exception {
        addToQueue(max);
        max.getLeft().accept(this);
        max.getRight().accept(this);
        return null;
    }

    //return true;
    public Object visit(And and) throws Exception {
        addToQueue(and);
        and.getLeft().accept(this);
        and.getRight().accept(this);
        return null;
    }

    //return true;
    public Object visit(Or or) throws Exception {
        addToQueue(or);
        or.getLeft().accept(this);
        or.getRight().accept(this);
        return null;
    }

    //return true;
    public Object visit(ConstraintUtility cu) throws Exception {
        addToQueue(cu);
        cu.getConstraint().accept(this);
        cu.getUtility().accept(this);
        return null;
    }

    //return true;
    public Object visit(Sum sum) throws Exception {
        addToQueue(sum);
        for (Term t : sum.getTerms())
        {
            t.accept(this);
        }
        return null;
    }

    //return true;
    public Object visit(Variable variable) throws Exception {
        addToQueue(variable);
        return null;
    }

    //return true;
    public Object visit(Reification reif) throws Exception {
        addToQueue(reif);
        reif.getCondition().accept(this);
        return null;
    }

    @Override
    public Object visit(final Negation r) throws Exception
    {
        return null;
    }

    //return true;
    public Object visit(Log log) throws Exception {
        addToQueue(log);
        log.getArg().accept(this);
        return null;
    }

    //return true;
    public Object visit(Sin sin) throws Exception {
        addToQueue(sin);
        sin.getArg().accept(this);
        return null;
    }

    //return true;
    public Object visit(Cos cos) throws Exception {
        addToQueue(cos);
        cos.getArg().accept(this);
        return null;
    }

    //return true;
    public Object visit(Abs abs) throws Exception {
        addToQueue(abs);
        abs.getArg().accept(this);
        return null;
    }

    //return true;
    public Object visit(Exp exp) throws Exception {
        addToQueue(exp);
        exp.getArg().accept(this);
        return null;
    }

    //return true;
    public Object visit(Atan2 atan2) throws Exception {
        addToQueue(atan2);
        atan2.getLeft().accept(this);
        atan2.getRight().accept(this);
        return null;
    }

}


//return true;