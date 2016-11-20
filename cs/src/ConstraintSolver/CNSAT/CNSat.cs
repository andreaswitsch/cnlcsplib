//#define CNSatDebug
using System;
using System.Collections.Generic;

namespace Alica.Reasoner.CNSAT
{
	public class CNSat
	{
		protected List<Clause> clauses = new List<Clause>();
		public List<Clause> Clauses {
			get {return clauses;}
			set {clauses = value;}
		}		
		protected List<Clause> satClauses = new List<Clause>();
		public List<Clause> SATClauses {
			get {return satClauses;}
			set {satClauses = value;}
		}
		
		protected List<Clause> tClauses = new List<Clause>();
		public List<Clause> TClauses {
			get {return tClauses;}
			set {tClauses = value;}
		}
		
		protected List<Clause> iClauses = new List<Clause>();
		public List<Clause> IClauses {
			get {return iClauses;}
			set {iClauses = value;}
		}
		
		protected List<Var> variables = new List<Var>();
		public List<Var> Variables {
			get {return variables;}
			set {variables = value;}
		}
		
		protected List<Var> decisions = new List<Var>();
		public List<Var> Decisions {
			get {return decisions;}
			set {decisions = value;}
		}
		
		protected List<DecisionLevel> decisionLevel = new List<DecisionLevel>();
		public List<DecisionLevel> DecisionLevel {
			get {return decisionLevel;}
			set {decisionLevel = value;}
		}
		
		public bool UseIntervalProp{get; set;}
		
		public Random Rand { get { return this.r; }}
		private int conflictCount=0;
		private int decisionCount=0;
		private int learnedCount=0;
		private int learntNum;
		private int restartCount = 0;
		private DecisionLevel decisionLevelNull;
		public Random r;
		private bool recentBacktrack = false;
		
		public int UnitDecissions{get; set;}
		
		public CNSMTGSolver CNSMTGSolver{get; set;}
		
		public CNSat ()
		{
			UseIntervalProp = true;
			this.CNSMTGSolver = null;
			decisionLevelNull = new DecisionLevel(0);
			UnitDecissions = 0;
			r = new Random();
		}
		
		public Var newVar() {
			Var T = new Var(variables.Count);
			variables.Add(T);
			return T;
		}
		
		public bool addBasicClause(Clause c) {
			if(c.Literals.Count==0) return false;
			
			if(c.Literals.Count>1) {
				Watcher w1 = new Watcher(c.Literals[0], c);
				Watcher w2 = new Watcher(c.Literals[1], c);
				c.watcher[0] = w1;
				c.watcher[1] = w2;
				clauses.Add(c);
				/*foreach(Lit l in c.Literals) {
					if(l.Sign == Assignment.True) l.Var.Activity++;
					else l.Var.NegActivity++;
				}*/
			} else {
				if(c.Literals[0].Var.Assignment != Assignment.Unassigned 
					&& c.Literals[0].Var.Assignment != c.Literals[0].Sign) return false;
				
				c.Literals[0].Var.Assignment = c.Literals[0].Sign;
				c.Literals[0].Var.DecisionLevel = this.decisionLevelNull;
				c.Literals[0].Var.Reason = null;
				
				//TODO check this!!!
				decisions.Add(c.Literals[0].Var);
				c.Literals[0].Var.Locked = true;
				clauses.Add(c);
			}
			
			return true;
		}
		
		public void EmptySATClause() {
			List<Clause> list = this.SATClauses;
			EmptyClauseList(list);			
		}
		
		public void EmptyTClause() {
			List<Clause> list = this.TClauses;
			EmptyClauseList(list);			
		}
		
		public void ResetVariables() {
			foreach(Var v in variables) {
				v.Reset();
			}
		}
		
		protected void EmptyClauseList(List<Clause> list) {
			foreach(Clause c in list) {
				c.watcher[0].Lit.Var.WatchList.Remove(c.watcher[0]);
				c.watcher[0].Lit.VariableCount--;
				c.watcher[1].Lit.Var.WatchList.Remove(c.watcher[1]);
				c.watcher[1].Lit.VariableCount--;
			}
			list.Clear();
		}
		
		/* Maybe we want theese ...
		public void addTClause(Clause c) {
			clauses.Add(c);
		}*/
		
		public bool addSATClause(Clause c) {
			if(c.Literals.Count==1) {
				//TODO Can be removed when Locked is removed
				if (c.Literals[0].Var.Locked && c.Literals[0].Sign != c.Literals[0].Var.Assignment) {
					return false;
				}

				
				///////////
				///////////
				/////////// AAAAAAAAAAAAAAAAm Ende von DL 0 muss das sein! oO
				///////////
				///////////
				/*if(decisionLevelNull.Level-1 > decisions.Count) decisions.Add(c.Literals[0].Var);
				else 
					decisions.Insert(decisionLevelNull.Level-1, c.Literals[0].Var);				*/
				/*if(DecisionLevel.Count>1)decisions.Insert(DecisionLevel[1].Level-1, c.Literals[0].Var);
				else 
					decisions.Add(c.Literals[0].Var);*/
				decisions.Insert(decisionLevelNull.Level, c.Literals[0].Var);
				//decisions.Insert(0, c.Literals[0].Var);
				c.Literals[0].Var.DecisionLevel = this.decisionLevel[0];
				c.Literals[0].Var.Assignment = c.Literals[0].Sign;
				c.Literals[0].Var.Reason = null;
				c.Literals[0].Var.Locked = true;
				foreach(DecisionLevel l in this.decisionLevel) {
					l.Level++;
				}
				return true;
			}
			Watcher w1 = new Watcher(c.Literals[0], c);
			Watcher w2 = new Watcher(c.Literals[c.Literals.Count-1], c);
			c.watcher[0] = w1;
			c.watcher[1] = w2;
			satClauses.Add(c);
			
			return true;
		}
		
		
		public bool addTClause(Clause c) {
			if(c.Literals.Count==1) {
				if (c.Literals[0].Var.Locked && c.Literals[0].Sign != c.Literals[0].Var.Assignment)
					return false;
				
				//TODO Check: Do we need this?
				backTrack(this.decisionLevel[0]);

				decisions.Insert(decisionLevelNull.Level, c.Literals[0].Var);
				//decisions.Insert(0, c.Literals[0].Var);
				c.Literals[0].Var.DecisionLevel = this.decisionLevel[0];
				c.Literals[0].Var.Assignment = c.Literals[0].Sign;
				c.Literals[0].Var.Reason = null;
				//Do we have to Lock T-Clauses?????
				//c.Literals[0].Var.Locked = true;
				foreach(DecisionLevel l in this.decisionLevel) {
					l.Level++;
				}
				return true;
			}
			Watcher w1 = new Watcher(c.Literals[c.Literals.Count-2], c);
			Watcher w2 = new Watcher(c.Literals[c.Literals.Count-1], c);
			c.watcher[0] = w1;
			c.watcher[1] = w2;
			TClauses.Add(c);
			
			return true;
		}
		public bool PreAddIUnitClause(Var v, Assignment ass) {			
			if ((v.Assignment != Assignment.Unassigned) && (v.Assignment != ass)) {				
				return false; //problem is unsolveable
			}			
			decisions.Insert(0, v);
			v.DecisionLevel = this.decisionLevel[0];
			v.Assignment = ass;
			v.Reason = null;
			v.Locked = true;
			this.decisionLevel[0].Level++;	
			UnitDecissions++;
			return true;
		}
		
		public bool addIClause(Clause c) {
			if(c.Literals.Count==1) {
				//TODO brauchen wir hier noch einen check, ob da schon was gesetzt ist?
				//if (c.Literals[0].Var.Locked && c.Literals[0].Sign != c.Literals[0].Var.Assignment)
				//	return false;
				//backTrackAndRevoke(c.Literals[0].Var.DecisionLevel);
				backTrack(this.decisionLevel[0]);
					
				decisions.Insert(0, c.Literals[0].Var);
				c.Literals[0].Var.DecisionLevel = this.decisionLevel[0];
				c.Literals[0].Var.Assignment = c.Literals[0].Sign;
				c.Literals[0].Var.Reason = null;
				c.Literals[0].Var.Locked = true;
				foreach(DecisionLevel l in this.decisionLevel) {
					l.Level++;
				}
				UnitDecissions++;
				return true;
			}
			//TODO this does somehow not work! oO
			//Watcher w1 = new Watcher(c.Literals[c.Literals.Count-2], c);
			Watcher w1 = new Watcher(c.Literals[0], c);
			Watcher w2 = new Watcher(c.Literals[c.Literals.Count-1], c);
			c.watcher[0] = w1;
			c.watcher[1] = w2;
			IClauses.Add(c);
			
			return true;
		}
		
		/// <summary>
		/// Initialise some stuff, so information can be posted by interval propagation
		/// </summary>
		public void Init() {
			
			//Initial decisionlevel, see addClause
			UnitDecissions=decisions.Count;
			this.decisionLevel.Clear();
			this.decisionLevelNull.Level = decisions.Count;
			this.decisionLevel.Add(this.decisionLevelNull);
			
			//int unitClauseCount = 0;
			foreach(Clause cl in clauses) {
				if(cl.Literals.Count==1) //unitClauseCount++;
					cl.Satisfied=true;
			}
			recentBacktrack = false;
		}
		
		public bool solve() {			
			int restartNum=100;
			learntNum=700;
			restartCount = 0;
			double[,] curRanges=null;
			double[] solution=null;
			DecisionLevel evaluatedDL = null;
			//check: is already undecisdable?
			
			Clause c;
			while(true) {
				c = null;
				while((c = propagate()) != null) { //resolve all conflicts
					if (decisionLevel.Count == 1) {
						return false;
					}
					if(conflictCount%50==0 && CNSMTGSolver!=null && CNSMTGSolver.begin + CNSMTGSolver.maxSolveTime < RosCS.RosSharp.Now()) return false;
					if(!resolveConflict(c)) {
						return false;
					}
				}
				
				if(CNSMTGSolver!=null) {
					//check for conflict of Theoremprover
					if(UseIntervalProp && !CNSMTGSolver.IntervalPropagate(decisions, out curRanges)) {
						continue;
					} else {
						/*
						//TODO: Heuristic Decision whether or not to query the T-solver
						//comes in here
						//double satRatio = ((double)satClauseCount) / clauses.Count;
						//double varRatio = ((double)decisions.Count) / variables.Count;
						//if (recentBacktrack || varRatio < r.NextDouble()) { //|| satRatio > r.NextDouble()) {
						//if (recentBacktrack || satRatio > r.NextDouble()) {
						//if (decisionCount % 10 == 0) {
						//	recentBacktrack = false;
						//if(solution==null || !SolutionInsideRange(solution, curRanges)) {
						//if(!VarAssignmentInsideRange(Decisions[Decisions.Count-1],curRanges)) {
						//if(evaluatedDL==null || !AssignmentInsideRange(evaluatedDL, curRanges)) {
							if (!CNSMTGSolver.ProbeForSolution(decisions, out solution)) {
								continue;
							}
						//	evaluatedDL = DecisionLevel[DecisionLevel.Count-1];
						//}
						//}
						*/
						if (!CNSMTGSolver.ProbeForSolution(decisions, out solution)) {
								continue;
						}				
						int satClauseCount=0;
						for(int i = clauses.Count-1; i>= 0; --i) {
							if (clauses[i].Satisfied) {
								satClauseCount++;
							}
						}
						if(satClauseCount>=clauses.Count) {
							return true;
						}
					}
				}
				Var next;
				//Make a decission:
				//Var next = Decider.DecideRangeBased(variables,this);
				if(CNSMTGSolver!=null) next = Decider.DecideVariableCountBased(variables,this);
				else next = Decider.DecideActivityBased(variables,this);
				//Var next = decideRangeBased();
				//Var next = decide();
				if(next==null) { // if no unassigned vars
					Console.WriteLine("ConflictCount: "+conflictCount+" DecisionCount " +decisionCount+" LC "+this.learnedCount);
					return true;
				}
#if (CNSatDebug)	
				Console.Write("Decision: ");
				next.Print();
				Console.WriteLine();
#endif
				++decisionCount;
				//if(decisionCount%10000==0) PrintStatistics();
				if(decisionCount%25==0 && CNSMTGSolver != null && CNSMTGSolver.begin + CNSMTGSolver.maxSolveTime < RosCS.RosSharp.Now()) return false;
				//Forget unused clauses
				if(decisionCount%1000==0) {
					reduceDB(learntNum);
					foreach(Var v in variables) {
						v.Activity /= 4;
					}
				}
				
				
				if(false && decisionCount%restartNum==0) {
					//perform restart
					restartNum*=2;
					learntNum+=learntNum/10;
					restartCount++;
					for(int j = (decisionLevel[1].Level); j<decisions.Count; j++) {
						decisions[j].Assignment = Assignment.Unassigned;
						decisions[j].Reason = null;
						//decisions[j].Seen = false;
						foreach(Watcher wa in decisions[j].WatchList) {
							wa.Clause.Satisfied = false;
						}
					}
					decisions.RemoveRange(decisionLevel[1].Level, decisions.Count - (decisionLevel[1].Level));
					
					decisionLevel.RemoveRange(1, decisionLevel.Count-1);		
				}
			}
		}
		
		public void PrintStatistics() {
			Console.WriteLine("DC: {0}\tCC: {1}\tAD: {2}\tLC: {3}/{4}\t IC: {7}\tTC: {8}\tRestarts: {5}\t0 Level: {6}", 
					decisionCount, conflictCount, decisions.Count, satClauses.Count, learntNum, restartCount, decisionLevel[0].Level,iClauses.Count,tClauses.Count);
		}
		
		protected bool SolutionInsideRange(double[] solution, double[,] range) {
			for(int i=0; i<solution.Length; i++) {
				double val = solution[i];
				if(val < range[i,0] || val > range[i,1]) return false;
			}
			return true;
		}
		
		protected bool VarAssignmentInsideRange(Var v, double[,] range) {
			double[,] litrange = null;
			if(v.Assignment == Assignment.True) litrange=v.PositiveRanges;
			else litrange = v.NegativeRanges;
			for(int i=0; i<litrange.Length; i++) {
				double min = litrange[i,0];
				double max = litrange[i,1];
				if(min < range[i,0] || max > range[i,1]) return false;
			}
			return true;
		}
		
		protected bool AssignmentInsideRange(DecisionLevel dl, double[,] range) {
			for(int i=dl.Level; i<decisions.Count; i++) {
				if(!VarAssignmentInsideRange(decisions[i], range)) return false;
			}
			return true;
		}
		
		public void reduceDB(int num) {
			if(satClauses.Count<num) return;
			satClauses.Sort();
			for(int i=num; i<satClauses.Count; i++) {
				satClauses[i].watcher[0].Lit.Var.WatchList.Remove(satClauses[i].watcher[0]);
				satClauses[i].watcher[1].Lit.Var.WatchList.Remove(satClauses[i].watcher[1]);
				//Lit l = satClauses[i].Literals[satClauses[i].Literals.Count-1];
				//if (l.Var.Reason == satClauses[i]) l.Var.Reason = null;
			}			
			
			satClauses.RemoveRange(num, satClauses.Count-num);
			for(int i=0; i<satClauses.Count; i++) {
				satClauses[i].Activity /= 4;
			}
		}

		
		public Clause propagate() {
			int lLevel = 0;
			if(decisionLevel.Count>1) lLevel = decisionLevel[decisionLevel.Count-1].Level;
			
			for(int i=lLevel; i<decisions.Count; i++) {
				List<Watcher> watchList = decisions[i].WatchList;
				
				for(int j=0; j<watchList.Count; j++) {
					Watcher w = watchList[j];
#if (CNSatDebug)	
					decisions[i].Print(); 
					Console.Write(" -> "); 
					w.Clause.Print();
#endif
					if(w.Clause.Satisfied) continue;
					if(w.Lit.Satisfied()) {
						w.Clause.Satisfied = true;
						continue;
					}
					//TODO Do we need this?
					if(w.Lit.Var.Assignment==Assignment.Unassigned) continue;
					
					//This can be optimized !?
					Clause c = w.Clause;
					
					//Search for new Watch
					int oWId = (c.watcher[0] == w) ? 1 : 0;
					if(c.watcher[oWId].Lit.Satisfied()) {
						//TODO: Do we need this?
						w.Clause.Satisfied = true;
						continue;
					}
					bool found = false;
					foreach(Lit l in c.Literals) {
						if(c.watcher[oWId].Lit.Var != l.Var && (l.Var.Assignment==Assignment.Unassigned || l.Satisfied())) {
							w.Lit.Var.WatchList.Remove(w);
							j--;
							w.Lit = l;
							l.Var.WatchList.Add(w);
							found = true;
							if(l.Satisfied()) w.Clause.Satisfied = true;
							break;
						}
					}
					if(!found) {
						c.Activity++;
						//TODO Handle Watcher here ... do not return -> faster
						Watcher w2 = c.watcher[oWId]; 
						if(w2.Lit.Var.Assignment == Assignment.Unassigned) {
							w2.Lit.Var.Assignment = w2.Lit.Sign;
							w2.Clause.Satisfied = true;
							w2.Lit.Var.DecisionLevel = decisionLevel[decisionLevel.Count-1];
							decisions.Add(w2.Lit.Var);
							w2.Lit.Var.Reason = c;
														
							foreach (Watcher wi in w2.Lit.Var.WatchList)
								wi.Clause.LastModVar = w2.Lit.Var;

						}
						else return c;
					}
				}
			}
			
			return null;
		}
		
		public bool resolveConflict(Clause c) {
			
			++conflictCount;
			
			//Learn Clause from conflict here
			Clause confl = c;
			Clause learnt = new Clause();
		    int index   = decisions.Count - 1;
			int pathC = 0;
		    Var p=null;
#if (CNSatDebug)	
				Console.WriteLine("\nxxxxxxxxxxxxxxxxxxx");
				Console.WriteLine("\nAlready Learned");
				foreach(Clause a in satClauses) {
					a.Print();
				}
				
				Console.WriteLine("\nAssignment");
				this.PrintAssignments();
				
				Console.WriteLine("\nConflict");
				confl.Print();
				
				Console.WriteLine("\nReason");
				if (confl.LastModVar != null && confl.LastModVar.Reason != null)
					confl.LastModVar.Reason.Print();
				else
					Console.WriteLine("null");
				
				Console.WriteLine("-------------------\nLearning");
#endif
		    //Find all Literals until First Unique Implication Point(UIP)
			do {
#if (CNSatDebug)	
				if(p!=null) {Console.Write("Var ");p.Print();Console.Write(" -> ");}
				confl.Print();
				Console.Write("Trying ");
#endif
				
				Clause cl = confl;
				//Inspect conflict reason clause Literals
		        for (int j = 0; j < cl.Literals.Count; j++) {
		            Lit q = cl.Literals[j];
					//ignore UIP
					if (q.Var == p) {
#if (CNSatDebug)	
						q.Var.Print();
						Console.Write(" n(sub) ");
#endif
						continue;
					}
					//ignore sawnvariables and decissionlevel 0
		            if (!q.Var.Seen && q.Var.DecisionLevel!=decisionLevel[0]) {						
						q.Var.Seen = true;
						//if q has been decided in curent level: increase iterations; else add literal to learnt clause
		                if (q.Var.DecisionLevel.Level >= (decisionLevel[decisionLevel.Count-1].Level)) {
		                    pathC++;
#if (CNSatDebug)	
							q.Var.Print();
							Console.Write(" n(curlvl) ");
#endif
						}else{
#if (CNSatDebug)	
							q.Var.Print();
							Console.Write(" add ");
#endif
		                    learnt.Add(q);
						}
		            }
		        }
		        
		        // Select next clause to look at:
				//do { if(index<0) {Console.Write("BLA"); return false;} }
				while (!decisions[index--].Seen);
				
		        p     = decisions[index+1];
		        confl = p.Reason;
		        p.Seen = false;
		        pathC--;
#if (CNSatDebug)	
				Console.WriteLine();
#endif
		    } while (pathC > 0);
#if (CNSatDebug)	
			Console.WriteLine("-------------------");
#endif
			//Add UIP
			Lit t = new Lit(p, (p.Assignment==Assignment.False) ? Assignment.True : Assignment.False);
			learnt.Add(t);
			
			//Store Seen Variables for later reset
			List<Lit> SeenList = new List<Lit>(learnt.Literals);
			//simplify learnt clause
			//Here is still an error!!!!!!!
			for (int m=0; m < learnt.Literals.Count-1; ++m){
				Lit l = learnt.Literals[m];
				//Ignore Literals without reason
				if (l.Var.Reason == null) {
					continue;
				} else {
					//Check whether reason for current literal is already in learnt -> remove l
					Clause re = l.Var.Reason;
					bool found = false;

					foreach (Lit rel in re.Literals) {
						if (!rel.Var.Seen && (rel.Var.DecisionLevel!=decisionLevel[0])) {
							found = true;
							break;
						}
					}
				
					if (!found) {
						learnt.Literals.RemoveAt(m--);
					}
				}
			}
			//Reset Seen
			foreach(Lit l in SeenList) {
				l.Var.Seen = false;
			}
			
#if (CNSatDebug)	
			Console.WriteLine("\nLearned ");
			learnt.Print();
		  //This Stuff checks whether learnt is already in learntClauses -> throws exception
		/*	bool blub=false;
			for(int r=0; r<satClauses.Count; r++) {
				bool nof=true;
				if(satClauses[r].Literals.Count != learnt.Literals.Count) continue;
				foreach(Lit l in learnt.Literals) {		
					bool foundLit = false;
					foreach(Lit m in satClauses[r].Literals) {
						if(l.Var == (m.Var) && l.Sign == m.Sign) {
							foundLit = true;
							break;
						}
					}
					nof &= foundLit;
					
					if (false == nof) break;
				}
				if(nof) blub=true;
			}
			if(blub) throw new Exception("jbjkjdasklfjklasdkgklsaglajgkl");*/
#endif
			//End Learn Clause
			
			//Find backtracklevel: 
			DecisionLevel db;
			int i=1, maxLitIndex;
			Lit changeLit;
			
			if (learnt.Literals.Count == 1)
				db = this.decisionLevel[0];
			else {
				db = learnt.Literals[0].Var.DecisionLevel;
				maxLitIndex = 0;
				
				//Search newest decission, which affects a literal in learnt.
				for (i = 1; i < learnt.Literals.Count-1; ++i){
					Lit l = learnt.Literals[i];
					
					if (db.Level < l.Var.DecisionLevel.Level) {
						db = l.Var.DecisionLevel;
						maxLitIndex = i;
					}
				}
			
				changeLit = learnt.Literals[0];
				learnt.Literals[0] = learnt.Literals[maxLitIndex];
				learnt.Literals[maxLitIndex] = changeLit;
			}

			#if (CNSatDebug)	
			Console.WriteLine("Backtracking from " + this.decisionLevel[this.decisionLevel.Count-1].Level + " to " + db.Level);
			#endif
			
			//Backtrack to db
			backTrack(db);
			
			//Add learnt clause: Unit Clauses have to be satisfied otherwise: -> UNSAT
			bool solvable = this.addSATClause(learnt);
			if(!solvable) {// TODO can be removed once bugfree
				Console.WriteLine("Error on insert learned clause");
				return false;
			}
		
			/*if(db==DecisionLevel[0] && learnt.Literals.Count>1) {
				Console.WriteLine("Reached decision level 0");
				return false;
			}*/
			

			
			
			if (learnt.Literals.Count == 1) {
				//decisions[0].Assignment = learnt.Literals[0].Sign;	
				learnt.Literals[0].Var.Assignment = learnt.Literals[0].Sign;
				learnt.Literals[0].Var.Reason = null;
			}
			
			
			
			//Switch assignment of UIP to satisfy learnt
			if(learnt.Literals.Count>1) {
				//DecisionLevel d = new DecisionLevel(decisions.Count);
				//decisionLevel.Add(d);
			
				//Set Learnt as Reason for UIP 
				Lit l = learnt.Literals[learnt.Literals.Count-1];
				l.Var.Assignment = l.Sign;
				learnt.Satisfied = true;
				l.Var.DecisionLevel = this.decisionLevel[this.decisionLevel.Count-1];
				l.Var.Reason = learnt;
				decisions.Add(l.Var);
			}
			
#if (CNSatDebug)	
			this.PrintAssignments();
			Console.ReadLine();
#endif
			return true;
		}
		
		
		public void backTrack(DecisionLevel db) {
			//TODO make this more efficient (linked list?)
			recentBacktrack = true;
			int ndbidx = decisionLevel.IndexOf(db)+1;
			if (ndbidx >= decisionLevel.Count) return;
			db = decisionLevel[ndbidx];
			for(int j = db.Level; j<decisions.Count; j++) {
				decisions[j].Assignment = Assignment.Unassigned;
				decisions[j].Reason = null;
				//this is expensive
				foreach(Watcher wa in decisions[j].WatchList) {
					wa.Clause.Satisfied = wa.Clause.watcher[0].Lit.Satisfied() || wa.Clause.watcher[1].Lit.Satisfied();
					//wa.Clause.Satisfied = false; //this should take other watcher into account
				}
			}
			decisions.RemoveRange(db.Level, decisions.Count - db.Level);
			
			//int i = decisionLevel.IndexOf(db);
			int i = ndbidx;
			i=Math.Max(1,i);
			decisionLevel.RemoveRange(i, decisionLevel.Count-i);		
		}
		
		//This moves all decissions to decission level 0!
		public void backTrack(int decission) {
			recentBacktrack = true;
			if(decission<0) return;
			if(decisionLevel.Count < 2) decisionLevel.Add(new DecisionLevel(decission));
			
			//if(decission-1<decisions.Count) decisions[decission-1].Print();
			//	Console.WriteLine();
			decisionLevel[1].Level = decission;
			
			for(int j = (decisionLevel[1].Level); j<decisions.Count; j++) {
				decisions[j].Assignment = Assignment.Unassigned;
				decisions[j].Reason = null;
				decisions[j].Locked = false;
				foreach(Watcher wa in decisions[j].WatchList) {
					wa.Clause.Satisfied = false;
				}
			}
			if(decisionLevel[1].Level<decisions.Count) decisions.RemoveRange(decisionLevel[1].Level, decisions.Count - (decisionLevel[1].Level));
			
			decisionLevel.RemoveRange(1, decisionLevel.Count-1);	
			decisionLevelNull.Level = decisions.Count;
		}
		
	
		void PrintAssignments() {
			foreach(Var v in Variables) {
				v.Print();
				Console.Write(" ");
			}
			Console.WriteLine();
		}
	}
}




