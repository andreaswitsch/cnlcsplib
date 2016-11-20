//#define FORMULATRANS_DEBUG
//#define USE_EXTENDED_EQUALITY
using System;
using System.Collections.Generic;
using AutoDiff;

namespace Alica.Reasoner.CNSAT
{
	
	
	public class FormulaTransform
	{
		public Dictionary<Term,Var> Atoms {
			get;
			private set;
		}
		public int AtomOccurrence {
			get; 
			private set;
		}
#if USE_EXTENDED_EQUALITY		
		private TermEquality te;
#endif		
		public FormulaTransform()
		{
			this.Atoms = new Dictionary<Term,Var>();
#if USE_EXTENDED_EQUALITY			
			this.te = new TermEquality();
#endif			
			this.AtomOccurrence = 0;
			
		}
		private CNSat solver;
		public void Reset() {
			this.Atoms.Clear();
			this.AtomOccurrence = 0;
			
		}
		public LinkedList<Clause> TransformToCNF(Term formula, CNSat solver) {
			this.solver = solver;
			Reset();
			LinkedList<Clause> clauses = new LinkedList<Clause>();
			Clause initial = new Clause();
			initial.Literals.Add(new Lit(formula, Assignment.Unassigned, true));
			
			clauses.AddFirst(initial);
			
			DoTransform(clauses);
#if FORMULATRANS_DEBUG			
			Console.WriteLine("Clauses: {0} Lits: {1} Vars: {2}",clauses.Count,this.AtomOccurrence,solver.Variables.Count);
#endif			
			
			return clauses;
		}
		protected void DoTransform(LinkedList<Clause> clauses) {			
			Clause curClause = null;
			Lit curLit = null;
			int j=0;
			LinkedListNode<Clause> clauseNode = clauses.First;			 
			while(clauseNode != null) {
				if (!clauseNode.Value.IsFinished) {
					bool finished = true;
				
					for(j=0; j < clauseNode.Value.Literals.Count; j++) {
						if(clauseNode.Value.Literals[j].IsTemporary) {
							finished = false;
							curClause = clauseNode.Value;
							curLit = curClause.Literals[j];
							break;
						}						
					}	
					if(!finished) {
						//break clause on lit:
						LinkedListNode<Clause> prevNode = clauseNode.Previous;
						clauses.Remove(clauseNode);
						
						curClause.Literals.RemoveAt(j);
						Clause nc1, nc2;
						PerformStep(curClause,curLit, out nc1, out nc2);						
						if (nc1!=null) clauses.AddLast(nc1);
						if (nc2!=null) clauses.AddLast(nc2);
						if(prevNode == null) {
							clauseNode = clauses.First;
						}
						else clauseNode = prevNode.Next;											
						
					} else {
						clauseNode.Value.IsFinished = true;
						clauseNode = clauseNode.Next;
					}
				} else {
					clauseNode = clauseNode.Next;
				}
			}
			/*
			for(i=0; i<clauses.Count;i++) {
				if (clauses[i].IsFinished) {
					continue;
				}
				bool finished = true;
				for(j=0; j < clauses[i].Literals.Count; j++) {
					if(clauses[i].Literals[j].IsTemporary) {
						finished = false;
						curClause = clauses[i];
						curLit = curClause.Literals[j];
						break;
					}						
				}	
				if(!finished) {
					//break clause on lit:
					clauses.RemoveAt(i);
					curClause.Literals.RemoveAt(j);
					clauses.AddRange(PerformStep(curClause,curLit));
					i--;	
				
				} else {
					clauses[i].IsFinished = true;
				}
			}
			*/
			/*foreach(Clause c in clauses) {
				if(!c.IsFinished) throw new Exception("Not Finished");
			}*/
		}
		
		protected void PerformStep(Clause c, Lit lit, out Clause newClause1, out Clause newClause2) {
			//List<Clause> ret = new List<Clause>();
			Term formula = lit.Atom;
			if (formula is Max) {
				Max m = (Max)formula;
				Lit l = new Lit(m.Left,Assignment.Unassigned,true);
				Lit r = new Lit(m.Right,Assignment.Unassigned,true);
				c.AddChecked(l);
				c.AddChecked(r);
				newClause1 = c;
				newClause2 = null;
				return;
			}
			if (formula is And) {
				And m = (And)formula;
				Lit l = new Lit(m.Left,Assignment.Unassigned,true);
				Lit r = new Lit(m.Right,Assignment.Unassigned,true);
				Clause c2 = c.Clone();
				c.AddChecked(l);
				c2.AddChecked(r);
				newClause1 = c;
				newClause2 = c2;
				return;				
			}
			if (formula is Or) {
				Or m = (Or)formula;
				Lit l = new Lit(m.Left,Assignment.Unassigned,true);
				Lit r = new Lit(m.Right,Assignment.Unassigned,true);
				c.AddChecked(l);
				c.AddChecked(r);
				newClause1 = c;
				newClause2 = null;
				return;
			}			
			if (formula is Min) {
				Min m = (Min)formula;
				Lit l = new Lit(m.Left,Assignment.Unassigned,true);
				Lit r = new Lit(m.Right,Assignment.Unassigned,true);
				Clause c2 = c.Clone();
				c.AddChecked(l);
				c2.AddChecked(r);
				newClause1 = c;
				newClause2 = c2;
				return;				
			}
			if (formula is LTConstraint) {				
				lit.IsTemporary = false;
				lit.ComputeVariableCount();
				lit.Sign = Assignment.True;
				this.AtomOccurrence++;
				Var v=null;			
#if USE_EXTENDED_EQUALITY
				if(this.TryGetVar(lit.Atom,out v)) {
#else					
				if(this.Atoms.TryGetValue(lit.Atom,out v)) {
#endif
					lit.Var = v;
				}
				else {
					lit.Var = solver.newVar();
					lit.Var.Term = lit.Atom;
					this.Atoms.Add(lit.Atom,lit.Var);
				}
				c.AddChecked(lit);
				newClause1 = c;
				newClause2 = null;
				return;	
			}
			if (formula is LTEConstraint) {				
				lit.IsTemporary = false;
				lit.ComputeVariableCount();
				lit.Sign = Assignment.False;
				this.AtomOccurrence++;
				Term p = ((LTEConstraint)formula).Negate();
				lit.Atom = p;
				Var v = null;
#if USE_EXTENDED_EQUALITY					
				if (this.TryGetVar(p,out v)) {
#else						
				if (this.Atoms.TryGetValue(p,out v)) {
#endif
					lit.Var = v;					
				}
				else {
					lit.Var = solver.newVar();
					lit.Var.Term = p;
					this.Atoms.Add(p,lit.Var);
				}				
				c.AddChecked(lit);
				newClause1 = c;
				newClause2 = null;
				return;					
			}
			if (formula is Constant) {
				if (((Constant)formula).Value <= 0.0) {
					newClause1 = c;
				} else newClause1 = null;
				newClause2 = null;
				return;
			}
			Console.Error.WriteLine("U C: {0}",formula);
			throw new Exception("Unknown constraint in transformation: "+formula);
			

			
		}
#if USE_EXTENDED_EQUALITY					
		private bool TryGetVar(Term t, out Var v) {
			v = null;
			foreach(KeyValuePair<Term, Var> kvp in this.Atoms) {
				if (te.EqualTerms(kvp.Key,t)) {
					v = kvp.Value;
					return true;
				}
			}
			return false;
			
		}
#endif				
		
		/*public List<Clause> TransformToCNF(Term formula) {
//Console.WriteLine("Transforming: {0}",formula);
			List<Clause> ret = new List<Clause>();
			if(formula is Max) {
				Max m = (Max)formula;
				Term l = TransformToCNF(m.Left);
				Term r = TransformToCNF(m.Right);
				if (l is And && r is And) {
					And ml = (And)l;
					And mr = (And)r;
					ret.AddRange(TransformToCNF(ml.Left | mr.Left));
					ret.AddRange(TransformToCNF(ml.Left | mr.Right));
					ret.AddRange(TransformToCNF(ml.Right | mr.Left));
					ret.AddRange(TransformToCNF(ml.Right | mr.Right));					
				}
				if (l is And) {
					And ml = (And)l;
					return TransformToCNF(ml.Left | r) & TransformToCNF(ml.Right | r);
				}
				if (r is And) {
					And mr = (And)r;
					return TransformToCNF(mr.Left | l) & TransformToCNF(mr.Right | l);
				}
				if (l is Min && r is Min) {
					Min ml = (Min)l;
					Min mr = (Min)r;
					return TransformToCNF(ml.Left | mr.Left) & TransformToCNF(ml.Left | mr.Right) & TransformToCNF(ml.Right | mr.Left) & TransformToCNF(ml.Right | mr.Right);
				}
				if (l is Min) {
					Min ml = (Min)l;
					return TransformToCNF(ml.Left | r) & TransformToCNF(ml.Right | r);
				}
				if (r is Min) {
					Min mr = (Min)r;
					return TransformToCNF(mr.Left | l) & TransformToCNF(mr.Right | l);
				}
				return (l | r);
			}
			else if (formula is Or) {
				Or m = (Or)formula;
				Term l = TransformToCNF(m.Left);
				Term r = TransformToCNF(m.Right);
				if (l is And && r is And) {
					And ml = (And)l;
					And mr = (And)r;
					return TransformToCNF(ml.Left | mr.Left) & TransformToCNF(ml.Left | mr.Right) & TransformToCNF(ml.Right | mr.Left) & TransformToCNF(ml.Right | mr.Right);
				}
				if (l is And) {
					And ml = (And)l;
					return TransformToCNF(ml.Left | r) & TransformToCNF(ml.Right | r);
				}
				if (r is And) {
					And mr = (And)r;
					return TransformToCNF(mr.Left | l) & TransformToCNF(mr.Right | l);
				}
				if (l is Min && r is Min) {
					Min ml = (Min)l;
					Min mr = (Min)r;
					return TransformToCNF(ml.Left | mr.Left) & TransformToCNF(ml.Left | mr.Right) & TransformToCNF(ml.Right | mr.Left) & TransformToCNF(ml.Right | mr.Right);
				}
				if (l is Min) {
					Min ml = (Min)l;
					return TransformToCNF(ml.Left | r) & TransformToCNF(ml.Right | r);
				}
				if (r is Min) {
					Min mr = (Min)r;
					return TransformToCNF(mr.Left | l) & TransformToCNF(mr.Right | l);
				}
				return (l | r);
			}
			else if (formula is And) {
				And a = (And)formula;
				return TransformToCNF(a.Left) & TransformToCNF(a.Right);
			}
			else if (formula is Min) {
				Min a = (Min)formula;
				return TransformToCNF(a.Left) & TransformToCNF(a.Right);				
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
	}
}

