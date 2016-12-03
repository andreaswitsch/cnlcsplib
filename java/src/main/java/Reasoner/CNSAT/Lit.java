//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.CNSAT;

import AutoDiff.ITermVisitor;
import AutoDiff.*;

public class Lit implements ITermVisitor
{
    public Lit(Var v, Assignment ass) throws Exception {
        setVar(v);
        this.setSign(ass);
        if (ass == Assignment.True)
            getVar().setPositiveAppearance(getVar().getPositiveAppearance() + 1);
        else
            getVar().setNegActivity(getVar().getNegActivity() + 1); 
        this.setIsTemporary(false);
        setVariableCount(-1);
    }

    public boolean satisfied() throws Exception {
        return getSign() == getVar().getAssignment();
    }

    public boolean conflicted() throws Exception {
        return getVar().getAssignment() != getSign() && getVar().getAssignment() != Assignment.Unassigned;
    }

    private Assignment __Sign = Assignment.False;
    public void setSign(Assignment value) {
        __Sign = value;
    }

    public Assignment getSign() {
        return __Sign;
    }

    private Var __Var;
    public Var getVar() {
        return __Var;
    }

    public void setVar(Var value) {
        __Var = value;
    }

    private int __VariableCount;
    public int getVariableCount() {
        return __VariableCount;
    }

    public void setVariableCount(int value) {
        __VariableCount = value;
    }

    public void computeVariableCount() throws Exception {
        setVariableCount(0);
        getAtom().accept(this);
    }

    private boolean __IsTemporary;
    public boolean getIsTemporary() {
        return __IsTemporary;
    }

    public void setIsTemporary(boolean value) {
        __IsTemporary = value;
    }

    public Lit(Term t, Assignment ass, boolean temp) throws Exception {
        this.setIsTemporary(temp);
        this.setSign(ass);
        this.setAtom(t);
        setVariableCount(-1);
    }

    private Term __Atom;
    public Term getAtom() {
        return __Atom;
    }

    public void setAtom(Term value) {
        __Atom = value;
    }

    public Object visit(Constant constant) throws Exception {
        return null;
    }

    public Object visit(Zero zero) throws Exception {
        return null;
    }

    public Object visit(ConstPower intPower) throws Exception {
        intPower.getBase().accept(this);
        return null;
    }

    public Object visit(TermPower intPower) throws Exception {
        intPower.getBase().accept(this);
        intPower.getExponent().accept(this);
        return null;
    }

    public Object visit(Product product) throws Exception {
        product.getLeft().accept(this);
        product.getRight().accept(this);
        return null;
    }

    public Object visit(Sigmoid sigmoid) throws Exception {
        sigmoid.getArg().accept(this);
        sigmoid.getMid().accept(this);
        return null;
    }

    public Object visit(LinSigmoid sigmoid) throws Exception {
        sigmoid.getArg().accept(this);
        return null;
    }

    public Object visit(LTConstraint constraint) throws Exception {
        constraint.getLeft().accept(this);
        constraint.getRight().accept(this);
        return null;
    }

    public Object visit(LTEConstraint constraint) throws Exception {
        constraint.getLeft().accept(this);
        constraint.getRight().accept(this);
        return null;
    }

    public Object visit(Min min) throws Exception {
        min.getLeft().accept(this);
        min.getRight().accept(this);
        return null;
    }

    public Object visit(Max max) throws Exception {
        max.getLeft().accept(this);
        max.getRight().accept(this);
        return null;
    }

    public Object visit(And and) throws Exception {
        and.getLeft().accept(this);
        and.getRight().accept(this);
        return null;
    }

    public Object visit(Or or) throws Exception {
        or.getLeft().accept(this);
        or.getRight().accept(this);
        return null;
    }

    public Object visit(ConstraintUtility cu) throws Exception {
        cu.getConstraint().accept(this);
        cu.getUtility().accept(this);
        return null;
    }

    public Object visit(Sum sum) throws Exception {
        for (Object __dummyForeachVar0 : sum.getTerms())
        {
            Term t = (Term)__dummyForeachVar0;
            t.accept(this);
        }
        return null;
    }
    public Object visit(Variable variable) throws Exception {
        setVariableCount(getVariableCount() + 1);
        return null;
    }

    public Object visit(Reification reif) throws Exception {
        reif.getCondition().accept(this);
        return null;
    }

    public Object visit(Log log) throws Exception {
        log.getArg().accept(this);
        return null;
    }

    public Object visit(Sin sin) throws Exception {
        sin.getArg().accept(this);
        return null;
    }

    public Object visit(Cos cos) throws Exception {
        cos.getArg().accept(this);
        return null;
    }

    public Object visit(Abs abs) throws Exception {
        abs.getArg().accept(this);
        return null;
    }

    public Object visit(Exp exp) throws Exception {
        exp.getArg().accept(this);
        return null;
    }

    public Object visit(Atan2 atan2) throws Exception {
        atan2.getLeft().accept(this);
        atan2.getRight().accept(this);
        return null;
    }

}


