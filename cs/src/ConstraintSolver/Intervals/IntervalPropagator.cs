using System;
using AutoDiff;
using Alica.Reasoner.CNSAT;
using System.Collections.Generic;
namespace Alica.Reasoner.IntervalPropagation
{
	public class IntervalPropagator// :ITermVisitor<bool>
	{
		private ResetIntervals ri;
		private RecursivePropagate rp;		
		private double[,] globalRanges;
		private int dim;
		private AutoDiff.Variable[] vars;
		private CNSAT.CNSat solver;
		//private SetParents sp;
		internal static int updates;
		internal static int visits;
		
		public IntervalPropagator()
		{
			this.ri = new ResetIntervals();
			this.rp = new RecursivePropagate();
			//this.sp = new SetParents();
			
		}
		public void PrintStats() {
			Console.WriteLine("IP Steps: {0}/{1}",updates,visits);
		}
		public void SetGlobalRanges(AutoDiff.Variable[] vars, double[,] ranges, CNSAT.CNSat solver) {			
			for(int i=0; i<vars.Length; i++) {
				vars[i].GlobalMin = ranges[i,0];
				vars[i].GlobalMax = ranges[i,1];
			}
			this.globalRanges = ranges;
			this.vars = vars;
			this.dim = vars.Length;
			this.solver = solver;
			updates = 0;
			visits = 0;
		}
		
		public bool Propagate(List<Var> decisions, out double[,] completeRanges, out List<Var> offenders) {
			offenders = null;
			completeRanges = new double[dim,2];
			Buffer.BlockCopy(globalRanges,0,completeRanges,0,sizeof(double)*2*dim);
			for(int i=decisions.Count-1; i>=0; i--) {
				double[,] curRanges;
				if(decisions[i].Assignment == CNSAT.Assignment.True) {
					if(decisions[i].PositiveRanges == null) {
						if(!PropagateSingle(decisions[i],true)) {
							offenders = new List<Var>();
							offenders.Add(decisions[i]); 
							return false;
						}						
					}
					curRanges = decisions[i].PositiveRanges;					
				}
				else {
					if(decisions[i].NegativeRanges == null) {
						if(!PropagateSingle(decisions[i],false)) {
							offenders = new List<Var>();
							offenders.Add(decisions[i]);
							return false;
						}
					}
					curRanges = decisions[i].NegativeRanges;
				}
				for(int j= dim-1; j >=0; j--) {
					//Console.WriteLine("{0} B4: [{1}..{2}]",j,completeRanges[j,0],completeRanges[j,1]);
					
					completeRanges[j,0] = Math.Max(completeRanges[j,0],curRanges[j,0]);
					completeRanges[j,1] = Math.Min(completeRanges[j,1],curRanges[j,1]);	
					//Console.WriteLine("{0} AF: [{1}..{2}]",j,completeRanges[j,0],completeRanges[j,1]);
					if (completeRanges[j,0] > completeRanges[j,1]) { //ranges collapsed, build offenders
						offenders = new List<Var>();
						for(int k = decisions.Count -1; k>=i; k--) {
							offenders.Add(decisions[k]);
						}
						return false;						
					}
				}
				
			}
			return true;
		}
		public bool PrePropagate(List<Var> vars) {			
			for(int i=vars.Count-1; i>=0; --i) {
				if (vars[i].Assignment != CNSAT.Assignment.False && !PropagateSingle(vars[i],true)) {
					//Console.WriteLine(vars[i]+" true not doable");
					if(!solver.PreAddIUnitClause(vars[i],CNSAT.Assignment.False)) return false;
				}
				if (vars[i].Assignment != CNSAT.Assignment.True && !PropagateSingle(vars[i],false)) {
					//Console.WriteLine(vars[i]+" false not doable");
					if(!solver.PreAddIUnitClause(vars[i],CNSAT.Assignment.True)) return false;
				}				
			}
			return true;
		}
		private bool PropagateSingle(Var v, bool sign) {
			
			for(int i=dim-1; i>=0; i--) {			
				vars[i].Max = vars[i].GlobalMax;
				vars[i].Min = vars[i].GlobalMin;				
			}
			
			if (sign) {
				if(!Propagate(v.Term)) return false;
			} else {
				if(!Propagate(v.Term.Negate())) return false;
			}
			double rangeSize = 0;
			double[,] range = new double[dim,2];
			for(int i=dim-1; i>=0;--i) {
				range[i,0] = vars[i].Min;
				range[i,1] = vars[i].Max;
				double d = vars[i].Max-vars[i].Min;
				rangeSize += d*d;
			}			
			if(sign) {
				v.PositiveRanges = range;
				v.PositiveRangeSize = rangeSize;
			} else {
				v.NegativeRanges = range;
				v.NegativeRangeSize = rangeSize;
			}
			return true;
		}
		
		
		public bool Propagate(Term term) {
			term.Accept(this.ri);
			//term.Accept(this.sp);
			term.Min = 1;
			term.Max = 1;
			updates = 0;
			try {
				this.rp.Propagate(term);
			} catch(UnsolveableException) {
#if RecPropDEBUG
				Console.WriteLine("Unsolvable: "+ue);
				Console.WriteLine("{0} update steps",updates);
#endif
				return false;
			}
#if RecPropDEBUG
			Console.WriteLine("{0} update steps",updates);
#endif			
			return true;
		}
		/*
		public bool Visit(Constant c) {
			return false;
		}
		public bool Visit(Zero z) {
			return false;
		}
		public bool Visit(Variable v) {
			bool changed = false;
			foreach(Term t in v.Parents) {
				if(t.Accept(this.up)) {
					changed |= t.Accept(this);
					
				}
			}
			
		}*/
		
	}
	
}

