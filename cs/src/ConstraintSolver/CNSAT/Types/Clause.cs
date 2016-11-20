using System;
using System.Collections.Generic;

namespace Alica.Reasoner.CNSAT
{
	public class Clause : IComparable<Clause>
	{
		protected List<Lit> literals = new List<Lit>();
		
		
		
		public void AddChecked(Lit l) {
			bool found = false;
			if (l.IsTemporary) {
				for(int i=0; i<this.Literals.Count; i++) {
					if (this.Literals[i].IsTemporary && l.Atom == this.Literals[i].Atom) {
						found = true;
						break;
					}					
				}
				if (!found) {
					Literals.Add(l);
				} //else { 	}
				return;
			}			
			for(int i=0; i<this.Literals.Count; i++) {
				if (l.Atom == this.Literals[i].Atom) {
					found = true;
				    if (l.Sign != this.Literals[i].Sign) {
						this.IsTautologic = true;
						this.Literals.Clear();
					}
					//else {}
					break;
				}					
			}
			if (!found) {
				Literals.Add(l);
			}
			return;

		}
		internal bool IsTautologic {
			get;
			private set;
		}
		internal bool IsFinished {
			get;
			set;
		}
		
		public Clause Clone() {
			Clause clone = new Clause();
			clone.Literals.AddRange(this.literals);
			clone.IsFinished = this.IsFinished;
			clone.IsTautologic = this.IsTautologic;
			return clone;
		}
		
		
		public Clause ()
		{
			this.Activity = 0;
			this.Satisfied = false;
			this.IsFinished = false;
			this.IsTautologic = false;
		
		}
		
		public void Add(Lit l) {
			literals.Add(l);
		}
		
		public int avgActivity() {
			int ret=0;
			foreach(Lit l in literals) {
				ret+=l.Var.Activity;
			}
			return ret/literals.Count;
		}
		
		public int CompareTo(Clause other)
    	{
			
        	int a = this.Activity;//avgActivity();
			int b = other.Activity; //avgActivity();
			if(a<b) return 1;
			else if(a>b) return -1;
			else return 0;
    	}
		
		public bool checkSatisfied() {
			foreach(Lit l in literals) {
				if(l.Var.Assignment==l.Sign) {
					//this.Satisfied = true;
					return true;
				}
			}
			//this.Satisfied = false;
			return false;
		}
		
		public List<Lit> Literals {
			get {return literals;}
			set {literals = value;}
		}
		public bool Satisfied {set; get;}
		public Watcher[] watcher = new Watcher[2];
		public Var LastModVar {set; get;}
		public int Activity {set; get;}
		
		public void Print() {
			foreach(Lit l in Literals) {
				if(l.Sign==Assignment.False) Console.Write("-");
				if(l.Sign==Assignment.True) Console.Write("+");
				if(l.Sign==Assignment.Unassigned) Console.Write("#");
				Console.Write(l.Var.Index);
				if((watcher[0] != null && watcher[0].Lit == l) || (watcher[1] != null && watcher[1].Lit == l)) Console.Write("w");
				Console.Write(" ");
			}
			Console.WriteLine();
		}
	}
}

