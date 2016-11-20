//#define GSOLVER_LOG
#define DO_PREPROPAGATION
using System;
using AutoDiff;
using AD=AutoDiff;
using System.IO;
using System.Text;
using Castor;
using System.Collections.Generic;
using Alica.Reasoner.IntervalPropagation;

namespace Alica.Reasoner
{
public class CNSMTGSolver  {
		
	protected class RpropResult : IComparable<RpropResult> {
		public double[] initialValue;
		public double[] finalValue;
		public double initialUtil;
		public double finalUtil;
		public bool aborted;
		public int CompareTo (RpropResult other) {				
				return (this.finalUtil > other.finalUtil)?-1:1;
		}
		
	}
	public int Runs {get { return this.runCount;}}
	public int FEvals {get {return this.fevalsCount;}}
	double initialStepSize = 0.005;
	double utilitySignificanceThreshold = 1E-22;
	Random rand;	
	int dim;
	double[,] limits;
	double[] ranges;
	double[] rpropStepWidth;
	double[] rpropStepConvergenceThreshold;
	public double RPropConvergenceStepSize {get; private set;}
	//AD.ICompiledTerm term;
	AD.Variable[] currentArgs;	
	//Dictionary<int,AD.Term> currentAtoms;
	
		
	StreamWriter sw;
		
	static CNSMTGSolver instance=null;
		
	List<RpropResult> rResults;
	protected static int fcounter =0;	
	//Configuration:
	protected bool seedWithUtilOptimum;
	protected CNSAT.CNSat ss;
		
	protected CNSAT.FormulaTransform ft;
	protected IntervalPropagator ip;
	protected double[] lastSeed;
		
	protected RpropResult r1=null;
		
	protected int probeCount = 0;
	protected int successProbeCount =0;
	protected int intervalCount = 0;
	protected int successIntervalCount =0;
	protected int fevalsCount = 0;
	protected int runCount = 0;
	public ulong maxSolveTime;
	public ulong begin;
	public long MaxFEvals {get; set;}
	public bool UseIntervalProp{get; set;}
	public bool Optimize{get;set;}
		
	public CNSMTGSolver () {
		AD.Term.SetAnd(AD.Term.AndType.and);
		AD.Term.SetOr(AD.Term.OrType.max);
		
		this.rand = new Random();
		this.rResults = new List<RpropResult>();
			
		//this.seedWithUtilOptimum = true;
		
		this.ft = new CNSAT.FormulaTransform();
			
		this.ip = new IntervalPropagator();
			
		instance = this;
		this.MaxFEvals = SystemConfig.LocalInstance["Alica"].GetInt("Alica","CSPSolving","MaxFunctionEvaluations");
		this.maxSolveTime = ((ulong)SystemConfig.LocalInstance["Alica"].GetInt("Alica","CSPSolving","MaxSolveTime"))*1000000;
		//RPropConvergenceStepSize = 1E-2;
		RPropConvergenceStepSize = 0;
		UseIntervalProp = true;
		Optimize = false;
	}
	protected void InitLog() {
			
			string logFile = "/tmp/test"+(fcounter++)+".dbg";
			FileStream file = new FileStream(logFile, FileMode.Create);
			this.sw = new StreamWriter(file);				
			sw.AutoFlush = true;
	}
	protected void Log(double util, double[] val) {
			sw.Write(util);
			sw.Write("\t");
			for(int i=0; i <dim; i++) {
				sw.Write(val[i]); sw.Write("\t");
			}
			sw.WriteLine();
			
	}
	protected void LogStep() {
			sw.WriteLine();
			sw.WriteLine();
	}
	protected void CloseLog() {
			if (sw != null) {
				sw.Close();
				sw=null;
			}
	}
	
	public double[] Solve(AD.Term equation, AD.Variable[] args, double[,] limits, out double util) {
			return Solve(equation, args, limits, null, out util);
	}
		                                                                                           
	public double[] Solve(AD.Term equation, AD.Variable[] args, double[,] limits, double[][] seeds, out double util) {		
			lastSeed=null;
			probeCount = 0;
			successProbeCount = 0;
			intervalCount = 0;
			successIntervalCount = 0;
			fevalsCount = 0;
			runCount = 0;
			this.begin = RosCS.RosSharp.Now();		
			
			//this.FEvals = 0;
			util = 0;
#if (GSOLVER_LOG)			
			InitLog();
#endif		
			//ft.Reset();
			rResults.Clear();
			
			currentArgs = args;
			
			this.dim = args.Length;
			this.limits = limits;
			this.ranges = new double[dim];
			this.rpropStepWidth = new double[dim];
			this.rpropStepConvergenceThreshold = new double[dim];
			
			
			equation = equation.AggregateConstants();
			
			AD.ConstraintUtility cu = (AD.ConstraintUtility) equation;
			bool utilIsConstant =(cu.Utility is AD.Constant);
			bool constraintIsConstant = (cu.Constraint is AD.Constant);
			if (constraintIsConstant) {
				if(((AD.Constant)cu.Constraint).Value < 0.25) {
					util = ((AD.Constant)cu.Constraint).Value;
					double[] ret = new double[dim];
					for(int i=0; i<dim; i++) {
						ret[i] = (this.limits[i,1]+this.limits[i,0])/2.0;
							//this.ranges[i]/2.0+this.limits[i,0];
					}
					return ret;
				}
			}
			ss = new CNSAT.CNSat();
			
			ss.UseIntervalProp = this.UseIntervalProp;
			//Console.WriteLine(cu.Constraint);
			LinkedList<CNSAT.Clause> cnf = ft.TransformToCNF(cu.Constraint,ss);
			
			/*Console.WriteLine("Atoms: {0}, Occurrence: {1}",ft.Atoms.Count,ft.AtomOccurrence);
			Console.WriteLine("Clauses: {0}",cnf.Count);
			foreach(AD.Term atom in ft.Atoms.Keys) {
				Console.WriteLine("-------");	
				Console.WriteLine(atom);	
				Console.WriteLine("-------");
			}*/
			/*
			int litc=1;
			List<AD.Term> terms = new List<AD.Term>(ft.Atoms);
			
			currentAtoms = new Dictionary<int, Term>();
			
			foreach(AD.Term t in terms) {
				foreach(Literal l in ft.References[t]) {
					l.Id = litc;
				}
				currentAtoms.Add(litc,t);
				litc++;
			}*/
			
			if(UseIntervalProp) {
				ip.SetGlobalRanges(args, limits,ss);
			}
			
			
			foreach(CNSAT.Clause c in cnf) {
				if(!c.IsTautologic) {
					if (c.Literals.Count == 0) {
						util = Double.MinValue;
						double[] ret = new double[dim];
						for(int i=0; i<dim; i++) {
							ret[i] = (this.limits[i,1]+this.limits[i,0])/2.0;
						}
						return ret;
					}
					//Post Clause Here
					//Console.Write("\nAdding Clause with "+c.Literals.Count+" Literals\n");
					//c.Print();
				
					ss.addBasicClause(c);
				}
			}
			ss.CNSMTGSolver = this;
			
			ss.Init();
			//PRE-Propagation:
			if(UseIntervalProp) {
#if DO_PREPROPAGATION			
				if(!ip.PrePropagate(ss.Variables)) {
					Console.WriteLine("Unsatisfiable (unit propagation)");
					return null;
				}
#endif		
			}
			//END-PrePropagation
			//Console.WriteLine("Variable Count: " + ss.Variables.Count);
			
			bool solutionFound = false;
			
			ss.UnitDecissions = ss.Decisions.Count;
			
			do {
				if(!solutionFound) {
					ss.EmptySATClause();
					ss.EmptyTClause();
					ss.backTrack(ss.UnitDecissions);
				}
				solutionFound = ss.solve();
				if(Optimize) r1 = RPropOptimizeFeasible(ss.Decisions, ((AD.ConstraintUtility)equation).Utility, args, r1.finalValue, false);
				if(!solutionFound && r1.finalUtil > 0) r1.finalUtil=-1;
				util = r1.finalUtil;
				if(!Optimize && solutionFound) return r1.finalValue;
				else if(Optimize) {
					//optimization case
					rResults.Add(r1);
					CNSAT.Clause c = new Alica.Reasoner.CNSAT.Clause();
					foreach(CNSAT.Var v in ss.Decisions) {
						c.Add(new CNSAT.Lit(v, v.Assignment==CNSAT.Assignment.True ? CNSAT.Assignment.False : CNSAT.Assignment.True));
					}
					//ss.addBasicClause(c);
					ss.addIClause(c);
					ss.backTrack(ss.Decisions[ss.Decisions.Count-1].DecisionLevel);
					solutionFound = false;
				}
			} while (!solutionFound && this.begin + this.maxSolveTime > RosCS.RosSharp.Now() /*&& this.runCount < this.MaxFEvals*/);
			
			//Console.WriteLine("Probes: {0}/{1}\tIntervals: {2}/{3}",successProbeCount,probeCount,successIntervalCount,intervalCount);
			//ip.PrintStats();
			//Console.WriteLine("Rprop Runs: {0}\t FEvals {1}\t Solutions Found: {2}",this.runCount,this.fevalsCount, rResults.Count);
			
			//return best result
			if(rResults.Count>0) {
				foreach(RpropResult rp in rResults) {
					if(rp.finalUtil>util) {
						util = rp.finalUtil;
						r1 = rp;
					}
				}
				return r1.finalValue;
			}
			Console.WriteLine("Unsatisfiable");

			return null;
			
		}
		//Stripped down method for simpler testing
		public double[] SolveTest(AD.Term equation, AD.Variable[] args, double[,] limits) {		
			lastSeed = null;
			probeCount = 0;
			successProbeCount = 0;
			intervalCount = 0;
			successIntervalCount = 0;
			fevalsCount = 0;
			runCount = 0;
			
			//this.FEvals = 0;
			double util = 0;
			this.begin = RosCS.RosSharp.Now();		
#if (GSOLVER_LOG)			
			InitLog();
#endif		
			//ft.Reset();
			rResults.Clear();
			
			currentArgs = args;
			
			this.dim = args.Length;
			this.limits = limits;
			this.ranges = new double[dim];
			this.rpropStepWidth = new double[dim];
			this.rpropStepConvergenceThreshold = new double[dim];
			
			
			equation = equation.AggregateConstants();
			
			
			
			
			ss = new CNSAT.CNSat();
			
			ss.UseIntervalProp = this.UseIntervalProp;
			//Console.WriteLine(cu.Constraint);
			LinkedList<CNSAT.Clause> cnf = ft.TransformToCNF(equation,ss);
			
			/*Console.WriteLine("Atoms: {0}, Occurrence: {1}",ft.Atoms.Count,ft.AtomOccurrence);
			Console.WriteLine("Clauses: {0}",cnf.Count);
			foreach(AD.Term atom in ft.Atoms.Keys) {
				Console.WriteLine("-------");	
				Console.WriteLine(atom);	
				Console.WriteLine("-------");
			}*/
			/*
			int litc=1;
			List<AD.Term> terms = new List<AD.Term>(ft.Atoms);
			
			currentAtoms = new Dictionary<int, Term>();
			
			foreach(AD.Term t in terms) {
				foreach(Literal l in ft.References[t]) {
					l.Id = litc;
				}
				currentAtoms.Add(litc,t);
				litc++;
			}*/
			
			
			ip.SetGlobalRanges(args, limits,ss);
			
			
			foreach(CNSAT.Clause c in cnf) {
				if(!c.IsTautologic) {
					if (c.Literals.Count == 0) {
						util = Double.MinValue;
						double[] ret = new double[dim];
						for(int i=0; i<dim; i++) {
							ret[i] = (this.limits[i,1]+this.limits[i,0])/2.0;
						}
						return ret;
					}
					//Post Clause Here
					//Console.Write("\nAdding Clause with "+c.Literals.Count+" Literals\n");
					//c.Print();
				
					ss.addBasicClause(c);
				}
			}
			ss.CNSMTGSolver = this;
			
			ss.Init();
			//PRE-Propagation:
#if DO_PREPROPAGATION			
			if(!ip.PrePropagate(ss.Variables)) {
				Console.WriteLine("Unsatisfiable (unit propagation)");
				//return null;
			}
#endif		
			//END-PrePropagation
			//Console.WriteLine("Variable Count: " + ss.Variables.Count);
			
			bool solutionFound = false;
			
			
			solutionFound = ss.solve();
			//Console.WriteLine("Solution Found!!!!!");
				
			if(!solutionFound && r1.finalUtil > 0) r1.finalUtil=-1;
			util = r1.finalUtil;
			//if(util>this.utilitySignificanceThreshold) return r1.finalValue;
			return r1.finalValue;
				
			
			
			
			
		}
		public bool IntervalPropagate(List<CNSAT.Var> decisions, out double[,] curRanges) {
			intervalCount++;
			/*Console.Write("SAT proposed({0}): ", decisions.Count);
			foreach(CNSAT.Var v in decisions) {
				v.Print();
				Console.Write(" ");
			}
			Console.WriteLine();
			*/
			//double[,] curRanges=null;
			List<CNSAT.Var> offending=null;
			if(!ip.Propagate(decisions,out curRanges,out offending)) {
				/*Console.WriteLine("Propagation FAILED offenders are:");
				foreach(CNSAT.Var v in offending) {
					//Console.WriteLine(v + "\t"+(v.Assignment==CNSAT.Assignment.True?v.Term:v.Term.Negate()));
					Console.Write(v + " ");
				}
				Console.WriteLine();*/
				
				if(offending!=null) {
					CNSAT.Clause learnt = new CNSAT.Clause();
					foreach(CNSAT.Var v in offending) {
						learnt.Add(new CNSAT.Lit(v, v.Assignment==CNSAT.Assignment.True ? CNSAT.Assignment.False : CNSAT.Assignment.True));
					}
					//if(learnt.Literals.Count>0) {
					ss.addIClause(learnt);
					//}
				}
				return false;
				
			}
			//Console.WriteLine("Propagation succeeded");			
			this.limits = curRanges;
			successIntervalCount++;
			return true;
		}
		
		public bool ProbeForSolution(List<CNSAT.Var> decisions, out double[] solution) {
			probeCount++;
			/*Console.Write("SAT proposed({0}): ", decisions.Count);
			foreach(CNSAT.Var v in decisions) {
				v.Print();
				Console.Write(" ");
			}
			Console.WriteLine();*/
			solution=null;
			for(int i=0; i<dim; i++) {
			//	Console.WriteLine("[{0}..{1}]",this.limits[i,0],this.limits[i,1]);
				this.ranges[i] = (this.limits[i,1]-this.limits[i,0]);
			}
		
			for(int i=0; i<decisions.Count; i++) {
				decisions[i].CurTerm = (decisions[i].Assignment==CNSAT.Assignment.True?decisions[i].PositiveTerm:decisions[i].NegativeTerm);
			}
			//int idx = Math.Max(0,ss.DecisionLevel.Count-1);
			//r1 = RPropFindFeasible(decisions, ss.DecisionLevel[idx].Seed);
			r1 = RPropFindFeasible(decisions, lastSeed);
			if (r1.finalUtil < 0.5) r1 = RPropFindFeasible(decisions, null);
			if(r1.finalUtil < 0.5) {
				//Probe was not successfull -> assignment not valid
				//Console.Write(".");				
				//Console.WriteLine("Could not find point for {0}",constraint);				
				CNSAT.Clause learnt = new CNSAT.Clause();
				foreach(CNSAT.Var v in decisions) {
					learnt.Add(new CNSAT.Lit(v,v.Assignment==CNSAT.Assignment.True ? CNSAT.Assignment.False : CNSAT.Assignment.True));
				}
				ss.addTClause(learnt); 
				
				return false;
			}
			//ss.DecisionLevel[ss.DecisionLevel.Count-1].Seed = r1.finalValue;
			lastSeed = r1.finalValue;
			solution = r1.finalValue;
			successProbeCount++;
			return true;
		}
		
		protected RpropResult RPropFindFeasible(List<CNSAT.Var> constraints, double[] seed) {
			runCount++;
			InitialStepSize();	
			double[] curGradient;			
			RpropResult ret = new RpropResult();
//			curGradient = InitialPoint(constraints, ret);
	
			if(seed != null) {
				curGradient = InitialPointFromSeed(constraints, ret, seed);
			}
			else {
				curGradient = InitialPoint(constraints, ret);
			}
			
			double curUtil = ret.initialUtil;
			if (curUtil > 0.5) {
				return ret;
			}
					
			double[] formerGradient = new double[dim];
			double[] curValue = new double[dim];
			
			//Tuple<double[],double> tup;
											
			Buffer.BlockCopy(ret.initialValue,0,curValue,0,sizeof(double)*dim);			
			formerGradient = curGradient;	
			
			int itcounter = 0;
			int badcounter = 0;
				
#if (GSOLVER_LOG)			
			Log(curUtil,curValue);
#endif
			
			int maxIter = 60;
			int maxBad = 30;
			double minStep = 1E-11;
			
			while(itcounter++ < maxIter && badcounter < maxBad) {				
				for(int i=0; i<dim; i++) {
					if (curGradient[i] * formerGradient[i] > 0) rpropStepWidth[i] *= 1.3;
					else if (curGradient[i] * formerGradient[i] < 0) rpropStepWidth[i] *= 0.5;
					rpropStepWidth[i] = Math.Max(minStep,rpropStepWidth[i]);					
					if (curGradient[i] > 0) curValue[i] += rpropStepWidth[i];
					else if (curGradient[i] < 0) curValue[i] -= rpropStepWidth[i];
						
					if (curValue[i] > limits[i,1]) curValue[i] = limits[i,1];
					else if (curValue[i] < limits[i,0]) curValue[i] = limits[i,0];						
				}
				this.fevalsCount++;
				formerGradient = curGradient;
				Differentiate(constraints,curValue,out curGradient,out curUtil);
			
				bool allZero = true;
				for(int i=0; i < dim; i++) {
					if (Double.IsNaN(curGradient[i])) {
						//Console.Error.WriteLine("NaN in gradient, aborting!");
						ret.aborted=true;
#if (GSOLVER_LOG)									
						LogStep();
#endif						
						return ret;					
					}
					allZero &= (curGradient[i]==0);
				}

			
#if (GSOLVER_LOG)			
				Log(curUtil,curValue);
#endif				
				//Console.WriteLine("CurUtil: {0} Final {1}",curUtil,ret.finalUtil);
				
				if (curUtil > ret.finalUtil) {
					badcounter = 0;//Math.Max(0,badcounter-1);
					
					
					ret.finalUtil = curUtil;
					Buffer.BlockCopy(curValue,0,ret.finalValue,0,sizeof(double)*dim);
					//ret.finalValue = curValue;
					if (curUtil > 0.75) return ret;
					
				} else {
						
						badcounter++;
				}
				if (allZero) {
					
					ret.aborted = false;
#if (GSOLVER_LOG)									
					LogStep();
#endif			
					return ret;
				}

			}
#if (GSOLVER_LOG)									
			LogStep();
#endif			
			ret.aborted = false;
			return ret;
			
			
		}
		
		protected RpropResult RPropOptimizeFeasible(List<CNSAT.Var> constraints, AD.Term ut, AD.Variable[] args, double[] seed, bool precise) {
			
			//Compiled Term zusammenbauen
			AD.Term constr = AD.Term.True;
			foreach(CNSAT.Var v in constraints) {
				if(v.Assignment == Alica.Reasoner.CNSAT.Assignment.True) constr &= v.Term;
				else constr &= ConstraintBuilder.Not(v.Term);
				
			}
			AD.ConstraintUtility cu = new AD.ConstraintUtility(constr, ut);
			AD.ICompiledTerm term = AD.TermUtils.Compile(cu, args);
			Tuple<double[],double> tup;
			//fertig zusammengebaut
			
			
			
			runCount++;
			InitialStepSize();	
			double[] curGradient;			
			RpropResult ret = new RpropResult();
//			curGradient = InitialPoint(constraints, ret);
	
			if(seed != null) {
				curGradient = InitialPointFromSeed(constraints, ret, seed);
			}
			else {			
				curGradient = InitialPoint(constraints, ret);
			}
			double curUtil = ret.initialUtil;
						
			double[] formerGradient = new double[dim];
			double[] curValue = new double[dim];
			
			//Tuple<double[],double> tup;
											
			Buffer.BlockCopy(ret.initialValue,0,curValue,0,sizeof(double)*dim);			
			formerGradient = curGradient;	
			
			int itcounter = 0;
			int badcounter = 0;
				
#if (GSOLVER_LOG)			
			Log(curUtil,curValue);
#endif
			
			int maxIter = 60;
			int maxBad = 30;
			double minStep = 1E-11;
			if(precise) {
				maxIter = 120; //110
				maxBad = 60;   //60
				minStep = 1E-15;//15				
			}
			int convergendDims = 0;
			
			while(itcounter++ < maxIter && badcounter < maxBad) {	
				convergendDims = 0;
				for(int i=0; i<dim; i++) {
					if (curGradient[i] * formerGradient[i] > 0) rpropStepWidth[i] *= 1.3;
					else if (curGradient[i] * formerGradient[i] < 0) rpropStepWidth[i] *= 0.5;
					rpropStepWidth[i] = Math.Max(minStep,rpropStepWidth[i]);
					//rpropStepWidth[i] = Math.Max(0.000001,rpropStepWidth[i]);
					if (curGradient[i] > 0) curValue[i] += rpropStepWidth[i];
					else if (curGradient[i] < 0) curValue[i] -= rpropStepWidth[i];
					
					if (curValue[i] > limits[i,1]) curValue[i] = limits[i,1];
					else if (curValue[i] < limits[i,0]) curValue[i] = limits[i,0];
					//Console.Write("{0}\t",curValue[i]);
					if(rpropStepWidth[i] < rpropStepConvergenceThreshold[i]) {
						++convergendDims;
					}
				}
				//Abort if all dimensions are converged
				if(!precise && convergendDims>=dim) {
					return ret;
				}
				this.fevalsCount++;
				formerGradient = curGradient;
				tup = term.Differentiate(curValue);
			
				bool allZero = true;
				for(int i=0; i < dim; i++) {
					if (Double.IsNaN(tup.Item1[i])) {
						ret.aborted=false;//true; //HACK!
#if (GSOLVER_LOG)									
						LogStep();
#endif						
						return ret;					
					}
					allZero &= (tup.Item1[i]==0);
				}
				
				curUtil = tup.Item2;
				formerGradient = curGradient;
				curGradient = tup.Item1;
#if (GSOLVER_LOG)			
				Log(curUtil,curValue);
#endif				
				//Console.WriteLine("CurUtil: {0} Final {1}",curUtil,ret.finalUtil);
				
				if (curUtil > ret.finalUtil) {
					badcounter = 0;//Math.Max(0,badcounter-1);
					
					
					ret.finalUtil = curUtil;
					Buffer.BlockCopy(curValue,0,ret.finalValue,0,sizeof(double)*dim);
					//ret.finalValue = curValue;
					if (curUtil > 0.75) return ret;
					
				} else {
						
						badcounter++;
				}
				if (allZero) {
					
					ret.aborted = false;
#if (GSOLVER_LOG)									
					LogStep();
#endif			
					return ret;
				}

			}
#if (GSOLVER_LOG)									
			LogStep();
#endif			
			ret.aborted = false;
			return ret;
			
			
		}
		
		protected void Differentiate(List<CNSAT.Var> constraints,double[] val,out double[] gradient, out double util) {
			Tuple<double[],double> t1 = constraints[0].CurTerm.Differentiate(val);
			gradient = t1.Item1;
			util = t1.Item2;
			for(int i=1; i<constraints.Count; i++) {
				Tuple<double[],double> tup = constraints[i].CurTerm.Differentiate(val);
				if(tup.Item2 <=0) {
					if(util > 0) util=tup.Item2;
					else util += tup.Item2;
					for(int j=0; j<dim; j++) {
						gradient[j] += tup.Item1[j];
					}
				}
			}
			//return new Tuple<double[], double>(gradient,util);
		}

	
		
		
		
		
		protected double[] InitialPointFromSeed(List<CNSAT.Var> constraints, RpropResult res, double[] seed) {
			Tuple<double[],double> tup;
			bool found = true;
			res.initialValue = new double[dim];
			res.finalValue = new double[dim];
			double[] gradient;
			do {
				gradient = new double[dim];
				found = true;
				res.initialUtil = 1;
				for(int i=0; i<dim; i++) {
					if (Double.IsNaN(seed[i])) {
						res.initialValue[i] = rand.NextDouble()*ranges[i]+limits[i,0];
					} else {
						res.initialValue[i] = Math.Min(Math.Max(seed[i],limits[i,0]),limits[i,1]);
					}
				}
				//why this?
				this.fevalsCount++;
				for(int i=0; i<constraints.Count; i++) {
					if(constraints[i].Assignment == CNSAT.Assignment.True) {
						if (constraints[i].PositiveTerm == null) constraints[i].PositiveTerm = TermUtils.Compile(constraints[i].Term,this.currentArgs);
						constraints[i].CurTerm = constraints[i].PositiveTerm;
					} else {
						if(constraints[i].NegativeTerm == null) constraints[i].NegativeTerm = TermUtils.Compile(constraints[i].Term.Negate(),this.currentArgs);
						constraints[i].CurTerm = constraints[i].NegativeTerm;
					}
					tup = constraints[i].CurTerm.Differentiate(res.initialValue);
					for(int j=0; j<dim; j++) {
						if (Double.IsNaN(tup.Item1[j])) {						
							found = false;
							break;
						}
						gradient[j] += tup.Item1[j];
					}
					if(!found) break;
					if(tup.Item2 <= 0.0) {
						if (res.initialUtil > 0.0) res.initialUtil = tup.Item2;
						else res.initialUtil += tup.Item2;
					}
					
				}
				//tup = term.Differentiate(res.initialValue);				
				
			} while(!found);			
			res.finalUtil = res.initialUtil;
			
			Buffer.BlockCopy(res.initialValue,0,res.finalValue,0,sizeof(double)*dim);
			
			return gradient;
		}
		
		
		
		
		
		protected double[] InitialPoint(List<CNSAT.Var> constraints,RpropResult res) {
			Tuple<double[],double> tup;
			bool found = true;
			res.initialValue = new double[dim];
			res.finalValue = new double[dim];
			double[] gradient;
			do {
				gradient = new double[dim];
				found = true;
				res.initialUtil = 1;
				for(int i=0; i<dim; i++) {
					res.initialValue[i] = rand.NextDouble()*ranges[i]+limits[i,0];					
				}
				this.fevalsCount++;
				for(int i=0; i<constraints.Count; i++) {
					if(constraints[i].Assignment == CNSAT.Assignment.True) {
						if (constraints[i].PositiveTerm == null) constraints[i].PositiveTerm = TermUtils.Compile(constraints[i].Term,this.currentArgs);
						constraints[i].CurTerm = constraints[i].PositiveTerm;
					} else {
						if(constraints[i].NegativeTerm == null) constraints[i].NegativeTerm = TermUtils.Compile(constraints[i].Term.Negate(),this.currentArgs);
						constraints[i].CurTerm = constraints[i].NegativeTerm;
					}
					tup = constraints[i].CurTerm.Differentiate(res.initialValue);
					for(int j=0; j<dim; j++) {
						if (Double.IsNaN(tup.Item1[j])) {						
							found = false;
							break;
						}
						gradient[j] += tup.Item1[j];
					}
					if(!found) break;
					if(tup.Item2 <= 0.0) {
						if (res.initialUtil > 0.0) res.initialUtil = tup.Item2;
						else res.initialUtil += tup.Item2;
					}
					
				}
				//tup = term.Differentiate(res.initialValue);				
				
			} while(!found);			
			res.finalUtil = res.initialUtil;
			
			Buffer.BlockCopy(res.initialValue,0,res.finalValue,0,sizeof(double)*dim);
			
			return gradient;
		}
		protected void InitialStepSize() {
			for(int i=0; i<this.dim; i++) {                         
				this.rpropStepWidth[i] = initialStepSize*ranges[i];
				this.rpropStepConvergenceThreshold[i] = rpropStepWidth[i]*this.RPropConvergenceStepSize;
			}
		}
		public bool CurrentCacheConsistent() {
			if (this.lastSeed == null) return false;
			for(int i = dim-1; i>=0; --i) {
				if(this.lastSeed[i] < this.limits[i,0]) return false;
				if(this.lastSeed[i] > this.limits[i,1]) return false;
			}
			return true;
		}

		
	}

}

