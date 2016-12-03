//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.IntervalPropagation;

import Alica.Reasoner.IntervalPropagation.DownwardPropagator;
import Alica.Reasoner.IntervalPropagation.SetParents;
import Alica.Reasoner.IntervalPropagation.TermList;
import Alica.Reasoner.IntervalPropagation.UpwardPropagator;

//using Alica.Reasoner;
//using Al=Alica;
public class RecursivePropagate  extends ITermVisitor 
{
    //internal Queue<Term> Changed;
    public TermList Changed;
    DownwardPropagator dp = new DownwardPropagator();
    UpwardPropagator up = new UpwardPropagator();
    SetParents sp = new SetParents();
    public RecursivePropagate() throws Exception {
        //this.Changed = new Queue<Term>();
        this.Changed = new TermList();
        dp.Changed = this.Changed;
        up.Changed = this.Changed;
    }

    public void propagate(Term t) throws Exception {
        //for(int i=0; i<2; i++) {
        this.Changed.clear();
        t.Accept(this);
        /*
        			foreach(Term q in this.Changed) {
        				q.Accept(this.sp);
        			}
        			while(this.Changed.Count > 0) {
        				Term cur = this.Changed.Dequeue();
        				cur.Accept(this.dp);
        				cur.Accept(this.up);				
        			}*/
        Term cur = this.Changed.First;
        while (cur != null)
        {
            cur.Accept(this.sp);
            cur = cur.Next;
        }
        cur = this.Changed.dequeue();
        while (cur != null)
        {
            cur.Accept(this.dp);
            cur.Accept(this.up);
            cur = this.Changed.dequeue();
        }
    }

    //}
    /*cur = this.Changed.First;
    			while(cur!=null) {				
    				cur.Accept(this.dp);				
    				//Term next = cur.Next;
    				
    				if(cur.Accept(this.up)) {
    					/*Term prev = cur.Prev;
    					this.Changed.MoveToEnd(cur);					
    					if (prev == null) cur = this.Changed.First;
    					else cur = prev.Next;*/
    //cur = cur.Next;
    //	}
    //	cur = cur.Next;
    //}
    //*/
    private void addToQueue(Term t) throws Exception {
        if (!this.Changed.contains(t))
            this.Changed.enqueue(t);
         
    }

    //this.Changed.Enqueue(t);
    //this.Changed.MoveToEnd(t);
    public void visit(Constant constant) throws Exception {
    }

    //	return false;
    public void visit(Zero zero) throws Exception {
    }

    //	return false;
    public void visit(ConstPower intPower) throws Exception {
        AddToQueue(intPower);
        intPower.Base.Accept(this);
    }

    //return true;
    public void visit(TermPower intPower) throws Exception {
        AddToQueue(intPower);
        intPower.Base.Accept(this);
        intPower.Exponent.Accept(this);
    }

    //return true;
    public void visit(Gp gp) throws Exception {
        throw new NotImplementedException();
    }

    //return true;
    public void visit(Product product) throws Exception {
        AddToQueue(product);
        product.Left.Accept(this);
        product.Right.Accept(this);
    }

    //return true;
    public void visit(Sigmoid sigmoid) throws Exception {
        AddToQueue(sigmoid);
        sigmoid.Arg.Accept(this);
        sigmoid.Mid.Accept(this);
    }

    //return true;
    public void visit(LinSigmoid sigmoid) throws Exception {
        AddToQueue(sigmoid);
        sigmoid.Arg.Accept(this);
    }

    //return true;
    public void visit(LTConstraint constraint) throws Exception {
        AddToQueue(constraint);
        constraint.Left.Accept(this);
        constraint.Right.Accept(this);
    }

    //return true;
    public void visit(LTEConstraint constraint) throws Exception {
        AddToQueue(constraint);
        constraint.Left.Accept(this);
        constraint.Right.Accept(this);
    }

    //return true;
    public void visit(Min min) throws Exception {
        AddToQueue(min);
        min.Left.Accept(this);
        min.Right.Accept(this);
    }

    //return true;
    public void visit(Max max) throws Exception {
        AddToQueue(max);
        max.Left.Accept(this);
        max.Right.Accept(this);
    }

    //return true;
    public void visit(And and) throws Exception {
        AddToQueue(and);
        and.Left.Accept(this);
        and.Right.Accept(this);
    }

    //return true;
    public void visit(Or or) throws Exception {
        AddToQueue(or);
        or.Left.Accept(this);
        or.Right.Accept(this);
    }

    //return true;
    public void visit(ConstraintUtility cu) throws Exception {
        AddToQueue(cu);
        cu.Constraint.Accept(this);
        cu.Utility.Accept(this);
    }

    //return true;
    public void visit(Sum sum) throws Exception {
        AddToQueue(sum);
        for (Object __dummyForeachVar0 : sum.Terms)
        {
            Term t = (Term)__dummyForeachVar0;
            t.Accept(this);
        }
    }

    //return true;
    public void visit(AutoDiff.Variable variable) throws Exception {
        AddToQueue(variable);
    }

    //return true;
    public void visit(Reification reif) throws Exception {
        AddToQueue(reif);
        reif.Condition.Accept(this);
    }

    //return true;
    public void visit(Log log) throws Exception {
        AddToQueue(log);
        log.Arg.Accept(this);
    }

    //return true;
    public void visit(Sin sin) throws Exception {
        AddToQueue(sin);
        sin.Arg.Accept(this);
    }

    //return true;
    public void visit(Cos cos) throws Exception {
        AddToQueue(cos);
        cos.Arg.Accept(this);
    }

    //return true;
    public void visit(Abs abs) throws Exception {
        AddToQueue(abs);
        abs.Arg.Accept(this);
    }

    //return true;
    public void visit(Exp exp) throws Exception {
        AddToQueue(exp);
        exp.Arg.Accept(this);
    }

    //return true;
    public void visit(Atan2 atan2) throws Exception {
        AddToQueue(atan2);
        atan2.Left.Accept(this);
        atan2.Right.Accept(this);
    }

}


//return true;