//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.CNSAT;

import AutoDiff.And;
import AutoDiff.Constant;
import AutoDiff.LTConstraint;
import AutoDiff.LTEConstraint;
import AutoDiff.Max;
import AutoDiff.Min;
import AutoDiff.Negation;
import AutoDiff.Or;
import AutoDiff.Term;
import AutoDiff.Variable;
import com.sun.org.apache.xpath.internal.operations.Neg;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.lang3.mutable.MutableObject;


public class FormulaTransform
{
    private HashMap<Term, Var> __Atoms = new HashMap<Term, Var>();

    public HashMap<Term, Var> getAtoms()
    {
        return __Atoms;
    }

    public void setAtoms(HashMap<Term, Var> value)
    {
        __Atoms = value;
    }

    private int __AtomOccurrence;

    public int getAtomOccurrence()
    {
        return __AtomOccurrence;
    }

    public void setAtomOccurrence(int value)
    {
        __AtomOccurrence = value;
    }

    public FormulaTransform() throws Exception
    {
        this.setAtoms(new HashMap<Term, Var>());
        this.setAtomOccurrence(0);
    }

    private CNSat solver;

    public void reset() throws Exception
    {
        this.setAtoms(new HashMap<Term, Var>());
        this.setAtomOccurrence(0);
    }

    public LinkedList<Clause> transformToCNF(Term formula, CNSat solver) throws Exception
    {
        this.solver = solver;
        reset();
        LinkedList<Reasoner.CNSAT.Clause> clauses = new LinkedList<Reasoner.CNSAT.Clause>();
        Reasoner.CNSAT.Clause initial = new Reasoner.CNSAT.Clause();
        initial.getLiterals().add(new Lit(formula, Reasoner.CNSAT.Assignment.Unassigned, true));
        clauses.addFirst(initial);
        doTransform(clauses);
        return clauses;
    }


    //TODO check this the logic of java iterators is dump square
    protected void doTransform(LinkedList<Reasoner.CNSAT.Clause> clauses) throws Exception
    {
        Clause curClause = null;
        Lit curLit = null;
        int j = 0;
        int clauseNode = 0;
        //TODO check this!
        Clause value = null;
        //risk
        while (clauses.size()>clauseNode) {
            value = clauses.get(clauseNode);
            if (!value.getIsFinished()) {
                boolean finished = true;

                for (j = 0; j < value.getLiterals().size(); j++) {
                    if (value.getLiterals().get(j).getIsTemporary()) {
                        finished = false;
                        curClause = value;
                        curLit = curClause.getLiterals().get(j);
                        break;
                    }
                }
                if (!finished) {
                    //break clause on lit:
                    //risk
                    int prevNode = clauseNode - 1;
                    //LinkedListNode<Clause> prevNode = clauseNode.Previous;
                    //clauses.remove(clauseNode);
                    clauses.remove(clauseNode);

                    curClause.getLiterals().remove(j);
                    MutableObject<Clause> nc1 = new MutableObject<Clause>();
                    MutableObject<Clause> nc2 = new MutableObject<Clause>();

                    performStep(curClause, curLit, nc1, nc2);
                    if (nc1.getValue() != null)
                        clauses.addLast(nc1.getValue());
                    if (nc2.getValue() != null)
                        clauses.addLast(nc2.getValue());
                    if (prevNode < 0) {
                        clauseNode = 0;
                    }
                    //risk
                    else {
                        //clauseNode = prevNode.next;
                        clauseNode = prevNode+1;
                    }

                } else {
                    value.setIsFinished(true);
                    //risk
                    //clauseNode = clauseNode.Next;
                    clauseNode++;
                }
            } else {
                //risk
                //clauseNode = clauseNode.Next;
                clauseNode++;
            }
        }
    }


    protected void performStep(Reasoner.CNSAT.Clause c, Lit lit, MutableObject<Clause> newClause1, MutableObject<Clause> newClause2)
        throws
        Exception
    {
        //List<Clause> ret = new List<Clause>();
        Term formula = lit.getAtom();
        if (formula instanceof Max) {
            Max m = (Max) formula;
            Lit l = new Lit(m.getLeft(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Lit r = new Lit(m.getRight(), Reasoner.CNSAT.Assignment.Unassigned, true);
            c.addChecked(l);
            c.addChecked(r);
            newClause1.setValue(c);
            newClause2.setValue(null);
            return;
        }

        if (formula instanceof And) {
            And m = (And) formula;
            Lit l = new Lit(m.getLeft(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Lit r = new Lit(m.getRight(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Reasoner.CNSAT.Clause c2 = c.clone();
            c.addChecked(l);
            c2.addChecked(r);
            newClause1.setValue(c);
            newClause2.setValue(c2);
            return;
        }

        if (formula instanceof Or) {
            Or m = (Or) formula;
            Lit l = new Lit(m.getLeft(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Lit r = new Lit(m.getRight(), Reasoner.CNSAT.Assignment.Unassigned, true);
            c.addChecked(l);
            c.addChecked(r);
            newClause1.setValue(c);
            newClause2.setValue(null);
            return;
        }

        if (formula instanceof Min) {
            Min m = (Min) formula;
            Lit l = new Lit(m.getLeft(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Lit r = new Lit(m.getRight(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Reasoner.CNSAT.Clause c2 = c.clone();
            c.addChecked(l);
            c2.addChecked(r);
            newClause1.setValue(c);
            newClause2.setValue(c2);
            return;
        }

        if (formula instanceof LTConstraint) {
            lit.setIsTemporary(false);
            lit.computeVariableCount();
            lit.setSign(Assignment.True);
            this.setAtomOccurrence(this.getAtomOccurrence() + 1);
            Var v = this.getAtoms().get(lit.getAtom());
            if (v != null) {
                lit.setVar(v);
            } else {
                lit.setVar(solver.newVar());
                lit.getVar().setTerm(lit.getAtom());
                this.getAtoms().put(lit.getAtom(), lit.getVar());
            }
            c.addChecked(lit);
            newClause1.setValue(c);
            newClause2.setValue(null);
            return;
        }

        if (formula instanceof LTEConstraint) {
            lit.setIsTemporary(false);
            lit.computeVariableCount();
            lit.setSign(Assignment.False);
            this.setAtomOccurrence(this.getAtomOccurrence() + 1);
            Term p = ((LTEConstraint) formula).negate();
            lit.setAtom(p);
            Var v = this.getAtoms().get(p);

            if (v != null) {

                lit.setVar(v);
            } else {
                lit.setVar(solver.newVar());
                lit.getVar().setTerm(p);
                this.getAtoms().put(p, lit.getVar());
            }
            c.addChecked(lit);
            newClause1.setValue(c);
            newClause2.setValue(null);
            return;
        }

        if (formula instanceof Variable) {
            lit.setIsTemporary(false);
            lit.computeVariableCount();
            if(lit.getSign()!=Assignment.False) lit.setSign(Assignment.True);
            this.setAtomOccurrence(this.getAtomOccurrence() + 1);
            Var v = this.getAtoms().get(lit.getAtom());
            if (v != null) {
                lit.setVar(v);
            } else {
                lit.setVar(solver.newVar());
                lit.getVar().setTerm(lit.getAtom());
                this.getAtoms().put(lit.getAtom(), lit.getVar());
            }
            c.addChecked(lit);
            newClause1.setValue(c);
            newClause2.setValue(null);
            return;
        }

        if (formula instanceof Negation && ((Negation)formula).getArg() instanceof Variable) {
            Negation m = (Negation) formula;
            Lit l = new Lit(m.getArg(), Assignment.False, true);
            c.addChecked(l);
            newClause1.setValue(c);
            newClause2.setValue(null);
            return;
        }

        if (formula instanceof Constant) {
            if (((Constant) formula).getValue() <= 0.0) {
                newClause1.setValue(c);
            } else {
                newClause1.setValue(null);
            }
            newClause2.setValue(null);
            return;
        }

        System.out.println("U C: " + formula);
        throw new Exception("Unknown constraint in transformation: " + formula);
    }


//    public List<Clause> TransformToCNF(Term formula)
//    {
//        List<Clause> ret = new ArrayList<Clause>();
//        if (formula instanceof Max){
//        Max m = (Max) formula;
//        Term l = TransformToCNF(m.getLeft());
//        Term r = TransformToCNF(m.getRight());
//        if (l instanceof And && r instanceof And){
//            And ml = (And) l;
//            And mr = (And) r;
//            ret.AddRange(TransformToCNF(ml.getLeft() | mr.getLeft()));
//            ret.AddRange(TransformToCNF(ml.getLeft() | mr.getRight()));
//            ret.AddRange(TransformToCNF(ml.getRight() | mr.getLeft()));
//            ret.AddRange(TransformToCNF(ml.getRight() | mr.getRight()));
//        }
//        if (l instanceof And){
//            And ml = (And) l;
//            return TransformToCNF(ml.getLeft() | r) & TransformToCNF(ml.getRight() | r);
//        }
//        if (r instanceof And){
//            And mr = (And) r;
//            return TransformToCNF(mr.getLeft() | l) & TransformToCNF(mr.getRight() | l);
//        }
//        if (l instanceof Min && r instanceof Min){
//            Min ml = (Min) l;
//            Min mr = (Min) r;
//            return TransformToCNF(ml.getLeft() | mr.getLeft()) & TransformToCNF(ml.getLeft() | mr.getRight()) & TransformToCNF(ml.getRight() | mr.getLeft()) & TransformToCNF(ml.getRight() | mr.getRight());
//        }
//        if (l instanceof Min){
//            Min ml = (Min) l;
//            return TransformToCNF(ml.getLeft() | r) & TransformToCNF(ml.getRight() | r);
//        }
//        if (r instanceof Min){
//            Min mr = (Min) r;
//            return TransformToCNF(mr.getLeft() | l) & TransformToCNF(mr.getRight() | l);
//        }
//        return (l | r);
//        }
//        else if (formula instanceof Or) {
//            Or m = (Or) formula;
//            Term l = TransformToCNF(m.getLeft());
//            Term r = TransformToCNF(m.getRight());
//            if (l instanceof And && r instanceof And){
//                And ml = (And) l;
//                And mr = (And) r;
//                return TransformToCNF(ml.getLeft() | mr.getLeft()) & TransformToCNF(ml.getLeft() | mr.getRight()) & TransformToCNF(ml.getRight() | mr.getLeft()) & TransformToCNF(ml.getRight() | mr.getRight());
//            }
//            if (l instanceof And){
//                And ml = (And) l;
//                return TransformToCNF(ml.getLeft() | r) & TransformToCNF(ml.getRight() | r);
//            }
//            if (r instanceof And){
//                And mr = (And) r;
//                return TransformToCNF(mr.getLeft() | l) & TransformToCNF(mr.getRight() | l);
//            }
//            if (l instanceof Min && r instanceof Min){
//                Min ml = (Min) l;
//                Min mr = (Min) r;
//                return TransformToCNF(ml.getLeft() | mr.getLeft()) & TransformToCNF(ml.getLeft() | mr.getRight()) & TransformToCNF(ml.getRight() | mr.getLeft()) & TransformToCNF(ml.getRight() | mr.getRight());
//            }
//            if (l instanceof Min){
//                Min ml = (Min) l;
//                return TransformToCNF(ml.getLeft() | r) & TransformToCNF(ml.getRight() | r);
//            }
//            if (r instanceof Min){
//                Min mr = (Min) r;
//                return TransformToCNF(mr.getLeft() | l) & TransformToCNF(mr.getRight() | l);
//            }
//            return (l | r);
//        }
//        else if (formula instanceof And){
//            And a = (And) formula;
//            return TransformToCNF(a.getLeft()) & TransformToCNF(a.getRight());
//        }
//        else if (formula instanceof Min){
//            Min a = (Min) formula;
//            return TransformToCNF(a.getLeft()) & TransformToCNF(a.getRight());
//        }
//        else {
//        if (formula instanceof LTConstraint){
//            if (!Atoms.Contains(formula)) {
//                Atoms.Add(formula);
//            }
//        }
//        else if (formula instanceof LTEConstraint){
//            Term p = ((LTEConstraint) formula).Negate();
//            if (!Atoms.Contains(p)) {
//                Atoms.Add(p);
//            }
//        }
//        else{
//            return formula; // True and False Constants
//            //throw new Exception("Unexpected Constraint in Formula Transformation: "+formula);
//        }
//        this.AtomOccurrence++;
//        return formula;
//        }
//    }
}