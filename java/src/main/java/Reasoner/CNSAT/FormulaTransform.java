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
import AutoDiff.Or;
import AutoDiff.Term;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;


public class FormulaTransform   
{
    private HashMap<Term, Var> __Atoms = new HashMap<Term, Var>();
    public HashMap<Term, Var> getAtoms() {
        return __Atoms;
    }

    public void setAtoms(HashMap<Term, Var> value) {
        __Atoms = value;
    }

    private int __AtomOccurrence;
    public int getAtomOccurrence() {
        return __AtomOccurrence;
    }

    public void setAtomOccurrence(int value) {
        __AtomOccurrence = value;
    }

    public FormulaTransform() throws Exception {
        this.setAtoms(new HashMap<Term, Var>());
        this.setAtomOccurrence(0);
    }

    private CNSat solver;
    public void reset() throws Exception {
        this.setAtoms(new HashMap<Term, Var>());
        this.setAtomOccurrence(0);
    }

    public LinkedList<Clause> transformToCNF(Term formula, CNSat solver) throws Exception {
        this.solver = solver;
        reset();
        LinkedList<Reasoner.CNSAT.Clause> clauses = new LinkedList<Reasoner.CNSAT.Clause>();
        Reasoner.CNSAT.Clause initial = new Reasoner.CNSAT.Clause();
        initial.getLiterals().add(new Lit(formula, Reasoner.CNSAT.Assignment.Unassigned,true));
        clauses.addFirst(initial);
        doTransform(clauses);
        return clauses;
    }


    //TODO check this the logic of java iterators is dump square
    protected void doTransform(LinkedList<Reasoner.CNSAT.Clause> clauses) throws Exception {
        Clause curClause = null;
        Lit curLit = null;
        int j=0;
        ListIterator<Clause> clauseNode = clauses.listIterator();
        //TODO check this!
        Clause value=null;
        //risk
        if(clauseNode.hasNext())
            value = clauseNode.next();
        while(clauseNode.hasNext()) {
            if (!value.getIsFinished()) {
                boolean finished = true;

                for(j=0; j < value.getLiterals().size(); j++) {
                    if(value.getLiterals().get(j).getIsTemporary()) {
                        finished = false;
                        curClause = value;
                        curLit = curClause.getLiterals().get(j);
                        break;
                    }
                }
                if(!finished) {
                    //break clause on lit:
                    //risk
                    ListIterator<Clause> prevNode = clauses.listIterator(clauseNode.nextIndex()-1);
                    //LinkedListNode<Clause> prevNode = clauseNode.Previous;
                    //clauses.remove(clauseNode);
                    clauseNode.remove();

                    curClause.getLiterals().remove(j);
                    Clause nc1 = new Clause();
                    Clause nc2 = new Clause();
                    performStep(curClause,curLit, nc1, nc2);
                    if (nc1!=null) clauses.addLast(nc1);
                    if (nc2!=null) clauses.addLast(nc2);
                    if(prevNode == null) {
                        clauseNode = clauses.listIterator();
                    }
                    //risk
                    else {
                        //clauseNode = prevNode.next;
                        clauseNode = prevNode;
                        value = clauseNode.next();
                    }

                } else {
                    value.setIsFinished(true);
                    //risk
                    //clauseNode = clauseNode.Next;
                    value = clauseNode.next();
                }
            } else {
                //risk
                //clauseNode = clauseNode.Next;
                value = clauseNode.next();
            }
        }
    }


    protected void performStep(Reasoner.CNSAT.Clause c, Lit lit, Clause newClause1, Clause newClause2) throws Exception {
        //List<Clause> ret = new List<Clause>();
        Term formula = lit.getAtom();
        if (formula instanceof Max)
        {
            Max m = (Max)formula;
            Lit l = new Lit(m.getLeft(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Lit r = new Lit(m.getRight(), Reasoner.CNSAT.Assignment.Unassigned, true);
            c.addChecked(l);
            c.addChecked(r);
            newClause1 = c;
            newClause2 = null;
            return ;
        }
         
        if (formula instanceof And)
        {
            And m = (And)formula;
            Lit l = new Lit(m.getLeft(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Lit r = new Lit(m.getRight(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Reasoner.CNSAT.Clause c2 = c.clone();
            c.addChecked(l);
            c2.addChecked(r);
            newClause1 = c;
            newClause2 = c2;
            return ;
        }
         
        if (formula instanceof Or)
        {
            Or m = (Or)formula;
            Lit l = new Lit(m.getLeft(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Lit r = new Lit(m.getRight(), Reasoner.CNSAT.Assignment.Unassigned, true);
            c.addChecked(l);
            c.addChecked(r);
            newClause1 = c;
            newClause2 = null;
            return ;
        }
         
        if (formula instanceof Min)
        {
            Min m = (Min)formula;
            Lit l = new Lit(m.getLeft(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Lit r = new Lit(m.getRight(), Reasoner.CNSAT.Assignment.Unassigned, true);
            Reasoner.CNSAT.Clause c2 = c.clone();
            c.addChecked(l);
            c2.addChecked(r);
            newClause1 = c;
            newClause2 = c2;
            return ;
        }
         
        if (formula instanceof LTConstraint)
        {
            lit.setIsTemporary(false);
            lit.computeVariableCount();
            lit.setSign(Assignment.True);
            this.setAtomOccurrence(this.getAtomOccurrence() + 1);
            Var v=this.getAtoms().get(lit.getAtom());
            if(v!=null) {
                lit.setVar(v);
            }
            else {
                lit.setVar(solver.newVar());
                lit.getVar().setTerm(lit.getAtom());
                this.getAtoms().put(lit.getAtom(),lit.getVar());
            }
            c.addChecked(lit);
            newClause1 = c;
            newClause2 = null;
            return;
        }
         
        if (formula instanceof LTEConstraint)
        {
            lit.setIsTemporary(false);
            lit.computeVariableCount();
            lit.setSign(Reasoner.CNSAT.Assignment.False);
            this.setAtomOccurrence(this.getAtomOccurrence() + 1);
            Term p = ((LTEConstraint)formula).negate();
            lit.setAtom(p);
            Var v = this.getAtoms().get(p);

            if (v!=null) {

                lit.setVar(v);
            }
            else {
                lit.setVar(solver.newVar());
                lit.getVar().setTerm(p);
                this.getAtoms().put(p,lit.getVar());
            }
            c.addChecked(lit);
            newClause1 = c;
            newClause2 = null;
            return;
            }
         
        if (formula instanceof Constant)
        {
            if (((Constant)formula).getValue() <= 0.0)
            {
                newClause1 = c;
            }
            else
                newClause1 = null;
            newClause2 = null;
            return ;
        }
         
        System.out.println("U C: " + formula);
        throw new Exception("Unknown constraint in transformation: " + formula);
    }

}


/*public List<Clause> TransformToCNF(Term formula) {
//Console.WriteLine("Transforming: {0}",formula);
			List<Clause> ret = new List<Clause>();
			if(formula is Max) {
				Max m = (Max)formula;
				Term l = TransformToCNF(m.getLeft());
				Term r = TransformToCNF(m.getRight());
				if (l is And && r is And) {
					And ml = (And)l;
					And mr = (And)r;
					ret.AddRange(TransformToCNF(ml.getLeft() | mr.getLeft()));
					ret.AddRange(TransformToCNF(ml.getLeft() | mr.getRight()));
					ret.AddRange(TransformToCNF(ml.getRight() | mr.getLeft()));
					ret.AddRange(TransformToCNF(ml.getRight() | mr.getRight()));					
				}
				if (l is And) {
					And ml = (And)l;
					return TransformToCNF(ml.getLeft() | r) & TransformToCNF(ml.getRight() | r);
				}
				if (r is And) {
					And mr = (And)r;
					return TransformToCNF(mr.getLeft() | l) & TransformToCNF(mr.getRight() | l);
				}
				if (l is Min && r is Min) {
					Min ml = (Min)l;
					Min mr = (Min)r;
					return TransformToCNF(ml.getLeft() | mr.getLeft()) & TransformToCNF(ml.getLeft() | mr.getRight()) & TransformToCNF(ml.getRight() | mr.getLeft()) & TransformToCNF(ml.getRight() | mr.getRight());
				}
				if (l is Min) {
					Min ml = (Min)l;
					return TransformToCNF(ml.getLeft() | r) & TransformToCNF(ml.getRight() | r);
				}
				if (r is Min) {
					Min mr = (Min)r;
					return TransformToCNF(mr.getLeft() | l) & TransformToCNF(mr.getRight() | l);
				}
				return (l | r);
			}
			else if (formula is Or) {
				Or m = (Or)formula;
				Term l = TransformToCNF(m.getLeft());
				Term r = TransformToCNF(m.getRight());
				if (l is And && r is And) {
					And ml = (And)l;
					And mr = (And)r;
					return TransformToCNF(ml.getLeft() | mr.getLeft()) & TransformToCNF(ml.getLeft() | mr.getRight()) & TransformToCNF(ml.getRight() | mr.getLeft()) & TransformToCNF(ml.getRight() | mr.getRight());
				}
				if (l is And) {
					And ml = (And)l;
					return TransformToCNF(ml.getLeft() | r) & TransformToCNF(ml.getRight() | r);
				}
				if (r is And) {
					And mr = (And)r;
					return TransformToCNF(mr.getLeft() | l) & TransformToCNF(mr.getRight() | l);
				}
				if (l is Min && r is Min) {
					Min ml = (Min)l;
					Min mr = (Min)r;
					return TransformToCNF(ml.getLeft() | mr.getLeft()) & TransformToCNF(ml.getLeft() | mr.getRight()) & TransformToCNF(ml.getRight() | mr.getLeft()) & TransformToCNF(ml.getRight() | mr.getRight());
				}
				if (l is Min) {
					Min ml = (Min)l;
					return TransformToCNF(ml.getLeft() | r) & TransformToCNF(ml.getRight() | r);
				}
				if (r is Min) {
					Min mr = (Min)r;
					return TransformToCNF(mr.getLeft() | l) & TransformToCNF(mr.getRight() | l);
				}
				return (l | r);
			}
			else if (formula is And) {
				And a = (And)formula;
				return TransformToCNF(a.getLeft()) & TransformToCNF(a.getRight());
			}
			else if (formula is Min) {
				Min a = (Min)formula;
				return TransformToCNF(a.getLeft()) & TransformToCNF(a.getRight());				
			}
			else {
				if (formula is LTConstraint) {
					if(!Atoms.Contains(formula)) {
						Atoms.Add(formula);
					}
				}
				else if (formula is LTEConstraint) {
					Term p = ((LTEConstraint)formula).Negate();
					if(!Atoms.Contains(p)) {
						Atoms.Add(p);
					}
				}
				else {
					return formula; // True and False Constants
					//throw new Exception("Unexpected Constraint in Formula Transformation: "+formula);
				}
				this.AtomOccurrence++;
				return formula;
			}
			
		}*/