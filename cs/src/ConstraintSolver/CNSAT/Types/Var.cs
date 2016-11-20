using System;
using System.Collections.Generic;
using AutoDiff;
namespace Alica.Reasoner.CNSAT
{
	public class Var
	{
		public Var (int index, bool prefSign=true)
		{
			this.Index = index;
			this.Assignment = Assignment.Unassigned;
			Locked = false;
			PreferedSign = prefSign;
			Activity = 0;
		}
		
		protected List<Watcher> watchList = new List<Watcher>();
		public List<Watcher> WatchList {
			get {return watchList;}
			set {watchList = value;}
		}
		private Clause reason;
		public int Index {set; get;}
		public int Activity {set; get;}
		public int NegActivity {set; get;}
		public bool Locked {set; get;}		
		public Assignment Assignment {set; get;}
		public bool Seen {set; get;}
		public bool PreferedSign { get; set; }
		public Clause Reason { get { return this.reason;} 
			set {
				if(value!=null && value != this.reason) {
					foreach(Lit l in value.Literals){
						if (l.Var.Assignment == Assignment.Unassigned) {
							Console.WriteLine(this +"   "+l.Var);
							value.Print();
							throw new Exception("!!!!!");
						}
					}
				}
				this.reason = value;
			}
		}
		
		public void Reset(){
			if(Locked) return;
			this.Reason = null;
			this.Seen = false;
			
			this.Assignment = Assignment.Unassigned;
			
			Activity = 0;
		}
		
		public DecisionLevel DecisionLevel { get; set; }
		
		public int PositiveAppearance { get; set; }
		public int NegativeAppearance { get; set; }
		
		public Term Term {get; set;}
		public double[,] PositiveRanges {get; set;}
		public double[,] NegativeRanges {get; set;}
		
		public double PositiveRangeSize {get; set;}
		public double NegativeRangeSize {get; set;}
		
		public ICompiledTerm PositiveTerm {get; set;}
		public ICompiledTerm NegativeTerm {get; set;}
		public ICompiledTerm CurTerm {get; set;}
		public void Print() {
			if(Assignment == Assignment.False) Console.Write("-");
			else if(Assignment == Assignment.Unassigned) Console.Write("o");
			else Console.Write("+");
			Console.Write(Convert.ToString(Index));
		}
		public override string ToString()
		{
			return (Assignment==Assignment.False?"-":(Assignment==Assignment.True?"+":"o"))+this.Index;
		//	return string.Format("[Var: WatchList={0}, Index={1}, Activity={2}, NegActivity={3}, Locked={4}, Assignment={5}, Seen={6}, PreferedSign={7}, Reason={8}, DecisionLevel={9}, Term={10}, PositiveRanges={11}, NegativeRanges={12}]", WatchList, Index, Activity, NegActivity, Locked, Assignment, Seen, PreferedSign, Reason, DecisionLevel, Term, PositiveRanges, NegativeRanges);
		}
	}
}

