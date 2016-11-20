using System;
using System.Collections.Generic;

namespace Alica.Reasoner.CNSAT
{
	public class Decider
	{
		public Decider()
		{
		}
		public static Var DecideRangeBased(List<Var> variables, CNSat solver) {
			List<Lit> choices = new List<Lit>();
			int vars = variables.Count;
			for(int i=0; i<vars; i++) {				
				if (variables[i].Assignment != Assignment.Unassigned) continue;
				bool hT = false;
				bool hF = false;
				for(int j=0; j<variables[i].WatchList.Count; j++) {					
					if(!variables[i].WatchList[j].Clause.Satisfied) {						
						if (variables[i].WatchList[j].Lit.Sign == Assignment.True) {
							if(!hT) choices.Add(variables[i].WatchList[j].Lit);	
							hT = true;
							
						} else {
							if(!hF) choices.Add(variables[i].WatchList[j].Lit);
							hF = true;							
						}						
					}
					if (hT && hF) break;
				}
			}
			if(choices.Count == 0) return null;
			
			choices.Sort(LitRangeCompare);
			/*Console.WriteLine("======");
			foreach(Lit l in choices) {
				Console.WriteLine("{0}",(l.Sign == Assignment.True ? l.Var.PositiveRangeSize:l.Var.NegativeRangeSize));
			}
			Console.WriteLine("======");*/
			//Choices are now in ascending order!
			Var v;
			Assignment ass;
			
			//Uniform:
			//int idx = solver.Rand.Next(choices.Count);
			//smallest range:
			//int idx = 0;
			//largest range:
			int idx = choices.Count-1;
			//Prefer smaller:
			/*int idx = choices.Count-1;
			double r = solver.Rand.NextDouble();
			double q = 0;
			for(int i=0; i<choices.Count; i++) {
				q += 1.0/(i+2);
				if (q > r) {
					idx = i;
					break;
				}
			}*/
			//Prefer larger:
			/*int idx = 0;
			double r = solver.Rand.NextDouble();
			double q = 0;
			for(int i=0; i<choices.Count; i++) {
				q += 1.0/(i+2);
				if (q > r) {
					idx = choices.Count-i-1;
					break;
				}
			}*/
			//Pick middle:
			//int idx = choices.Count/2;
			
			v = choices[idx].Var;
			ass = choices[idx].Sign;
			
			DecisionLevel d = new DecisionLevel(solver.Decisions.Count);
			solver.DecisionLevel.Add(d);					
			v.Assignment = ass;
			solver.Decisions.Add(v);
			v.Reason=null;
			v.DecisionLevel = d;
			
			return v;
			
		}
		
		
		public static Var DecideActivityBased(List<Var> variables, CNSat solver) {
			int vars = variables.Count;
			Random r = solver.r;
			int init = r.Next(vars);
			Var v=null, next=null;
			int maxActivity=0;
			//Search Lit with highest Activity
			if(solver.CNSMTGSolver==null) {
				for(int i=0; i<vars; i++) {
					int p = (init+i)%vars;
					if(p<0||p>=vars) Console.WriteLine("p = " +p);
					v = variables[p];
					if(v.Assignment == Assignment.Unassigned) {
						if(maxActivity<=v.Activity) {
							maxActivity=v.Activity;
							next=v;
						}
					}
				}
				//Decide it
				if(next!=null) {
					DecisionLevel d = new DecisionLevel(solver.Decisions.Count);
					solver.DecisionLevel.Add(d);
					
					double rel = next.PositiveAppearance+next.NegativeAppearance;
					if(rel!=0) rel = ((double)next.PositiveAppearance)/rel;
					else rel = 0.5;
					
					next.Assignment = (r.NextDouble() < rel) ? Assignment.True : Assignment.False;
					solver.Decisions.Add(next);
					next.Reason=null;
					next.DecisionLevel = d;
					return next;
				}
			} else { 
				init = r.Next(solver.Clauses.Count);
				for(int i=0; i<solver.Clauses.Count; i++) {
					Clause c = solver.Clauses[(i+init)%solver.Clauses.Count];
					if(!c.Satisfied && c.Literals.Count>1) {
						if(!c.watcher[0].Lit.Satisfied()) {
							next = c.watcher[0].Lit.Var;
							DecisionLevel d = new DecisionLevel(solver.Decisions.Count);
							solver.DecisionLevel.Add(d);
							
							next.Assignment = c.watcher[0].Lit.Sign;
							solver.Decisions.Add(next);
							next.Reason=null;
							next.DecisionLevel = d;
							return next;
						}
						else if(!c.watcher[1].Lit.Satisfied()) {
							DecisionLevel d = new DecisionLevel(solver.Decisions.Count);
							solver.DecisionLevel.Add(d);
							next = c.watcher[1].Lit.Var;
							
							next.Assignment = c.watcher[1].Lit.Sign;
							solver.Decisions.Add(next);
							next.Reason=null;
							next.DecisionLevel = d;
							return next;
						}
						else Console.WriteLine("This shoud Never Happen!!");
					}
				}
			}
			
			return null;
		}
		
		public static Var DecideVariableCountBased(List<Var> variables, CNSat solver) {
			int vars = variables.Count;
			Lit l = null;
			int maxCount = Int32.MaxValue;
			int minCount = -1;
			
			for(int i=0; i<vars; i++) {				
				if (variables[i].Assignment != Assignment.Unassigned) continue;
				for(int j=0; j<variables[i].WatchList.Count; j++) {
					if(!variables[i].WatchList[j].Clause.Satisfied) {
						/*if(maxCount>variables[i].WatchList[j].Lit.VariableCount) {
							l = variables[i].WatchList[j].Lit;
							maxCount = l.VariableCount;
						}*/
						if(minCount<variables[i].WatchList[j].Lit.VariableCount) {
							l = variables[i].WatchList[j].Lit;
							minCount = l.VariableCount;
						}
					}
				}
			}
			if(l==null) return null;
			Assignment ass;
			Var v=l.Var;
					
			ass = l.Sign;
			
			DecisionLevel d = new DecisionLevel(solver.Decisions.Count);
			solver.DecisionLevel.Add(d);					
			v.Assignment = ass;
			solver.Decisions.Add(v);
			v.Reason=null;
			v.DecisionLevel = d;
			
			return v;
			
		}
		
		
		
		
		public static int LitRangeCompare(Lit a, Lit b) {
			double sa;
			double sb;
			if (a.Sign == Assignment.True) sa = a.Var.PositiveRangeSize;
			else sa = a.Var.NegativeRangeSize;
			if (b.Sign == Assignment.True) sb = b.Var.PositiveRangeSize;
			else sb = b.Var.NegativeRangeSize;
			if (sa < sb) return -1;
			if (sa > sb) return 1;
			return 0;			
		}
		
	}
}

