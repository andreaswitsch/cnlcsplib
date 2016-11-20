//#define GSOLVER_LOG
//#define ALWAYS_CHECK_THRESHOLD
#define AGGREGATE_CONSTANTS
using System;
using AutoDiff;
using AD=AutoDiff;
using System.IO;
using System.Text;
using Castor;
using System.Collections.Generic;

namespace Alica.Reasoner
{
public class GSolver  {
		
	protected class RpropResult : IComparable<RpropResult> {
		public double[] initialValue;
		public double[] finalValue;
		public double initialUtil;
		public double finalUtil;
		public bool aborted;
		public int CompareTo (RpropResult other) {
				
				return (this.finalUtil > other.finalUtil)?-1:1;
		}
		public double DistanceTraveled() {
			double ret = 0;
			for(int i=0; i<initialValue.Length; i++) {
					ret+= (initialValue[i]-finalValue[i])*(initialValue[i]-finalValue[i]);
			}
			return Math.Sqrt(ret);
		}
		public double DistanceTraveledNormed(double[] ranges) {
			double ret = 0;
			for(int i=0; i<initialValue.Length; i++) {
					ret+= (initialValue[i]-finalValue[i])*(initialValue[i]-finalValue[i])/(ranges[i]*ranges[i]);
			}
			return Math.Sqrt(ret);
		}		
	}
	public long Runs {get; private set;}
	public long FEvals {get; private set;}
	public long MaxFEvals {get; set;}
	double initialStepSize = 0.005;
	public double RPropConvergenceStepSize {get; private set;}
	
	internal double utilitySignificanceThreshold = 1E-22;
	Random rand;	
	int dim;
	double[,] limits;
	double[] ranges;
	double[] rpropStepWidth;
	double[] rpropStepConvergenceThreshold;
	double utilityThreshold;
	ulong maxSolveTime;
	//double[][] seeds;
	AD.ICompiledTerm term;
		
	StreamWriter sw;
		
	List<RpropResult> rResults;
	protected static int fcounter =0;	
	//Configuration:
	protected bool seedWithUtilOptimum;
			
	public GSolver () {
		AD.Term.SetAnd(AD.Term.AndType.and);
		AD.Term.SetOr(AD.Term.OrType.max);
		
		this.rand = new Random();
		this.rResults = new List<RpropResult>();
			
		this.seedWithUtilOptimum = true;
		this.MaxFEvals = SystemConfig.LocalInstance["Alica"].GetInt("Alica","CSPSolving","MaxFunctionEvaluations");
		this.maxSolveTime = ((ulong)SystemConfig.LocalInstance["Alica"].GetInt("Alica","CSPSolving","MaxSolveTime"))*1000000;
		RPropConvergenceStepSize = 1E-2;
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
			
			return Solve(equation,args,limits,null,Double.MaxValue, out util);
	}
	public bool SolveSimple(AD.Term equation, AD.Variable[] args, double[,] limits) {
			
			return SolveSimple(equation,args,limits,null);
	}
		                                                                                           
	public double[] Solve(AD.Term equation, AD.Variable[] args, double[,] limits, double[][] seeds,double sufficientUtility, out double util) {						
			this.FEvals = 0;
			this.Runs = 0;
			util = 0;
			this.utilityThreshold = sufficientUtility;
#if (GSOLVER_LOG)			
			InitLog();
#endif			
			rResults.Clear();
			ulong begin = RosCS.RosSharp.Now();		
			
			this.dim = args.Length;
			this.limits = limits;
			this.ranges = new double[dim];
			for(int i=0; i<dim; i++) {
				this.ranges[i] = (this.limits[i,1]-this.limits[i,0]);
			}
#if AGGREGATE_CONSTANTS			
			equation = equation.AggregateConstants();
#endif			
			term = AD.TermUtils.Compile(equation,args);
			AD.ConstraintUtility cu = (AD.ConstraintUtility) equation;
			bool utilIsConstant =(cu.Utility is AD.Constant);
			if (utilIsConstant) this.utilityThreshold = 0.75;
			bool constraintIsConstant = (cu.Constraint is AD.Constant);
			if (constraintIsConstant) {
				if(((AD.Constant)cu.Constraint).Value < 0.25) {
					util = ((AD.Constant)cu.Constraint).Value;
					double[] ret = new double[dim];
					for(int i=0; i<dim; i++) {
						ret[i] = this.ranges[i]/2.0+this.limits[i,0];
					}
					return ret;
				}
			}
			
			//Optimize given seeds			
			this.rpropStepWidth = new double[dim];
			this.rpropStepConvergenceThreshold = new double[dim];
			if (seeds != null) {
				this.Runs++;
				//Run with prefered cached seed
				RpropResult rpfirst = RPropLoop(seeds[0],true);				
				if (rpfirst.finalUtil > this.utilityThreshold) {
						util = rpfirst.finalUtil;
						//Console.WriteLine("FEvals: {0}",this.FEvals);
						return rpfirst.finalValue;
				}
				rResults.Add(rpfirst);
				//run with seeds of all other agends
				for(int i=1; i<seeds.Length; i++) {
					if (begin + this.maxSolveTime < RosCS.RosSharp.Now() || this.FEvals > this.MaxFEvals) {
						break; //do not check any further seeds
					}					
					this.Runs++;
					RpropResult rp = RPropLoop(seeds[i],false);
					if (rp.finalUtil > this.utilityThreshold) {
						util = rp.finalUtil;
						//Console.WriteLine("FEvals: {0}",this.FEvals);
						return rp.finalValue;
					}
					rResults.Add(rp);	
				}
			}
			
			//Here: Ignore all constraints search optimum
			if (begin + this.maxSolveTime > RosCS.RosSharp.Now() && this.FEvals < this.MaxFEvals) {
				//if time allows, do an unconstrained run
				if(!constraintIsConstant && !utilIsConstant && seedWithUtilOptimum) {
					
					AD.ICompiledTerm curProb = term;
					term = AD.TermUtils.Compile(((AD.ConstraintUtility)equation).Utility,args);
					this.Runs++;
					double[] utilitySeed = RPropLoop(null).finalValue;	
					term = curProb;
					//Take result and search with constraints
					RpropResult ru = RPropLoop(utilitySeed,false);
					/*if (ru.finalUtil > this.utilityThreshold) {
						util = ru.finalUtil;
						return ru.finalValue;
					}*/
					rResults.Add(ru);					
				}
			}
			
			do { //Do runs until termination criteria, running out of time, or too many function evaluations
				this.Runs++;
				RpropResult rp = RPropLoop(null,false);
				if(rp.finalUtil > this.utilityThreshold) {
					util = rp.finalUtil;
					//Console.WriteLine("FEvals: {0}",this.FEvals);
					return rp.finalValue;
				}
				rResults.Add(rp);	
			} while(begin + this.maxSolveTime > RosCS.RosSharp.Now() && this.FEvals < this.MaxFEvals);
			
			//return best result
			int resIdx = 0;
			RpropResult res = rResults[0];
			for(int i=1; i< rResults.Count; i++) {
				if (Double.IsNaN(res.finalUtil) || rResults[i].finalUtil > res.finalUtil) {
					if (resIdx == 0 && seeds!=null && !Double.IsNaN(res.finalUtil)) {
						if (rResults[i].finalUtil - res.finalUtil > utilitySignificanceThreshold && rResults[i].finalUtil > 0.75) {
							res = rResults[i];
							resIdx = i;
						}
					} else {
						res = rResults[i];
						resIdx = i;
					}
				}
			}
//Console.WriteLine("ResultIndex: {0} Delta {1}",resIdx,res.finalUtil-rResults[0].finalUtil);
			
#if (GSOLVER_LOG)			
			CloseLog();
#endif			
			
//			Console.Write("Found: ");
//			for(int i=0; i<dim; i++) {
//				Console.Write("{0}\t",res.finalValue[i]);
//			}
//			Console.WriteLine();
//
			//Console.WriteLine("Runs: {0} FEvals: {1}",this.Runs,this.FEvals);	
			
			util = res.finalUtil;
			return res.finalValue;
	
	}
	public bool SolveSimple(AD.Term equation, AD.Variable[] args, double[,] limits, double[][] seeds) {
			
			rResults.Clear();
						
			
			this.dim = args.Length;
			this.limits = limits;
			this.ranges = new double[dim];
			for(int i=0; i<dim; i++) {
				this.ranges[i] = (this.limits[i,1]-this.limits[i,0]);
			}
			equation = equation.AggregateConstants();
			
			term = AD.TermUtils.Compile(equation,args);
			
						
			this.rpropStepWidth = new double[dim];
			this.rpropStepConvergenceThreshold = new double[dim];
			if (seeds != null) {
				for(int i=0; i<seeds.Length; i++) {
					RpropResult r = RPropLoopSimple(seeds[i]);
					rResults.Add(r);
					if (r.finalUtil > 0.75) return true;
					
				}
			}
			int runs = 2*dim - (seeds==null?0:seeds.Length);
			for(int i=0; i<runs; i++) {
				RpropResult r = RPropLoopSimple(null);
				rResults.Add(r);	
				if (r.finalUtil > 0.75) return true;
				
			}
			int adit = 0;
			while(!EvalResults() && adit++ < 20) {
				RpropResult r = RPropLoopSimple(null);
				rResults.Add(r);
				if(r.finalUtil > 0.75) return true;
				
			}
			if (adit > 20) {
				Console.WriteLine("Failed to satisfy heuristic!");
			}
			
			return false;
	
	}
	public double[] SolveTest(AD.Term equation, AD.Variable[] args, double[,] limits) {
#if (GSOLVER_LOG)			
			InitLog();
#endif			
			
			rResults.Clear();
			double[] res = null;						
			
			this.dim = args.Length;
			this.limits = limits;
			this.ranges = new double[dim];
			for(int i=0; i<dim; i++) {
				this.ranges[i] = (this.limits[i,1]-this.limits[i,0]);
			}
			
			
			term = AD.TermUtils.Compile(equation,args);
			
						
			this.rpropStepWidth = new double[dim];
			this.rpropStepConvergenceThreshold = new double[dim];
			this.Runs = 0;
			this.FEvals = 0;
			//int runs = 1000000;
			while(true) {
				this.Runs++;
				//RpropResult r = RPropLoopSimple(null);
				RpropResult r = RPropLoop(null,false);
				//Console.WriteLine("Run: {0} Util: {1}",i,r.finalUtil);
				/*for(int k=0; k<dim; k++) {
					Console.Write("{0}\t",r.finalValue[k]);
				}
				Console.WriteLine();*/
				if (r.finalUtil > 0.75) {
					res = r.finalValue;
					break;
				}
				
			}
			
			#if (GSOLVER_LOG)			
			CloseLog();
			#endif			

			return res;
	
		}
		
		public double[] SolveTest(AD.Term equation, AD.Variable[] args, double[,] limits,int maxRuns, out bool found) {
#if (GSOLVER_LOG)			
			InitLog();
#endif			
			found = false;
			rResults.Clear();
			double[] res = null;						
			
			this.dim = args.Length;
			this.limits = limits;
			this.ranges = new double[dim];
			for(int i=0; i<dim; i++) {
				this.ranges[i] = (this.limits[i,1]-this.limits[i,0]);
			}
			
			
			term = AD.TermUtils.Compile(equation,args);
			
						
			this.rpropStepWidth = new double[dim];
			this.rpropStepConvergenceThreshold = new double[dim];
			this.Runs = 0;
			this.FEvals = 0;
			
			
			while(this.Runs < maxRuns) {
				
				this.Runs++;
				//RpropResult r = RPropLoopSimple(null);
				RpropResult r = RPropLoop(null,false);
				//Console.WriteLine("Run: {0} Util: {1}",i,r.finalUtil);
				/*for(int k=0; k<dim; k++) {
					Console.Write("{0}\t",r.finalValue[k]);
				}
				Console.WriteLine();*/
				if (r.finalUtil > 0.75) {
					res = r.finalValue;
					found = true;
					break;
				}
				
			}
			
			#if (GSOLVER_LOG)			
			CloseLog();
			#endif			
			
			return res;
	
		}		
		
		protected double[] InitialPointFromSeed(RpropResult res, double[] seed) {
			Tuple<double[],double> tup;
			
			res.initialValue = new double[dim];
			res.finalValue = new double[dim];		
			for(int i=0; i<dim; i++) {
				if (Double.IsNaN(seed[i])) {
					res.initialValue[i] = rand.NextDouble()*ranges[i]+limits[i,0];
				} else {
					res.initialValue[i] = Math.Min(Math.Max(seed[i],limits[i,0]),limits[i,1]);
				}
			}
			tup = term.Differentiate(res.initialValue);
			res.initialUtil = tup.Item2;
			res.finalUtil = tup.Item2;
			
			Buffer.BlockCopy(res.initialValue,0,res.finalValue,0,sizeof(double)*dim);
			
			return tup.Item1;
		}
		
		
		
		protected double[] InitialPoint(RpropResult res) {
			Tuple<double[],double> tup;
			bool found = true;
			res.initialValue = new double[dim];
			res.finalValue = new double[dim];
			do {
				for(int i=0; i<dim; i++) {
					//double range = limits[i,1]-limits[i,0];					
					res.initialValue[i] = rand.NextDouble()*ranges[i]+limits[i,0];
				}
				this.FEvals++;
				tup = term.Differentiate(res.initialValue);
				for(int i=0; i<dim; i++) {
					if (Double.IsNaN(tup.Item1[i])) {
						//Console.WriteLine("NaN in Gradient, retrying");
						found = false;
						break;
					} else found = true;
				}
				
			} while(!found);
			res.initialUtil = tup.Item2;
			res.finalUtil = tup.Item2;
			
			Buffer.BlockCopy(res.initialValue,0,res.finalValue,0,sizeof(double)*dim);
			
			return tup.Item1;
		}
		protected RpropResult RPropLoop(double[] seed) {
			return RPropLoop(seed,false);
		}
		
		protected RpropResult RPropLoop(double[] seed, bool precise) {
	//Console.WriteLine("RpropLoop");
			InitialStepSize();
	
			double[] curGradient;
			
			RpropResult ret = new RpropResult();
	
			if(seed != null) {
				curGradient = InitialPointFromSeed(ret,seed);
			}
			else {			
				curGradient = InitialPoint(ret);
			}
			double curUtil = ret.initialUtil;
					
			double[] formerGradient = new double[dim];
			double[] curValue = new double[dim];
			
			Tuple<double[],double> tup;
											
			Buffer.BlockCopy(ret.initialValue,0,curValue,0,sizeof(double)*dim);
			
			
			formerGradient = curGradient;
			//Buffer.BlockCopy(curGradient,0,formerGradient,0,sizeof(double)*dim);
			
			
			int itcounter = 0;
			int badcounter = 0;
			
		
			/*Console.WriteLine("Initial Sol:");
			for(int i=0; i<dim;i++) {
				Console.Write("{0} ",curValue[i]);
			}
			Console.WriteLine();
			Console.WriteLine("Initial Util: {0}",curUtil);
			 */
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
				//Console.WriteLine("Iteration {0}",itcounter);
				//Console.WriteLine("Val: ");
				/*if (curUtil < 0.5 && rand.NextDouble()< 0.05) { //JUMP!
					//Console.WriteLine("JUMPING!");
					for (int i=0; i<dim; i++) {
						curValue[i] += curGradient[i];
						if (curValue[i] > limits[i,1]) curValue[i] = limits[i,1];
						else if (curValue[i] < limits[i,0]) curValue[i] = limits[i,0];
					}
					InitialStepSize();					
				} else {*/
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
						if (curUtil > ret.finalUtil) {
							ret.finalUtil = curUtil;
							Buffer.BlockCopy(curValue,0,ret.finalValue,0,sizeof(double)*dim);
						}
					
						return ret;
					}
				//}
				//Console.WriteLine();
				this.FEvals++;
				tup = term.Differentiate(curValue);
				bool allZero = true;
				//Console.WriteLine("Grad: ");
				for(int i=0; i < dim; i++) {
					if (Double.IsNaN(tup.Item1[i])) {
						//Console.Error.WriteLine("NaN in gradient, aborting!");
						ret.aborted=true;
#if (GSOLVER_LOG)									
						LogStep();
#endif						
						return ret;					
					}
					allZero &= (tup.Item1[i]==0);
					//Console.Write("{0}\t",tup.Item1[i]);
				}
				//Console.WriteLine();
				curUtil = tup.Item2;
				formerGradient = curGradient;
				curGradient = tup.Item1;
#if (GSOLVER_LOG)			
				Log(curUtil,curValue);
#endif				
				//Console.WriteLine("CurUtil: {0} Final {1}",curUtil,ret.finalUtil);
				
				if (curUtil > ret.finalUtil) {
					badcounter = 0;//Math.Max(0,badcounter-1);
					
					//if (curUtil-ret.finalUtil < 0.00000000000001) {
						//Console.WriteLine("not better");
					//	badcounter++;
					//} else {
					//badcounter = 0;
					//}
				
					ret.finalUtil = curUtil;
					Buffer.BlockCopy(curValue,0,ret.finalValue,0,sizeof(double)*dim);
					//ret.finalValue = curValue;
#if (ALWAYS_CHECK_THRESHOLD)
					if(curUtil > utilityThreshold) return ret;
#endif					
					
				} else {
						//if (curUtil < ret.finalUtil || curUtil > 0) badcounter++;
						badcounter++;
				}
				if (allZero) {
					//Console.WriteLine("All Zero!");
					/*Console.WriteLine("Util {0}",curUtil);
					Console.Write("Vals: ");
					for(int i=0; i < dim; i++) {
						Console.Write("{0}\t",curValue[i]);
					}
					Console.WriteLine();*/
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
		
		protected RpropResult RPropLoopSimple(double[] seed) {
			
			InitialStepSize();
			double[] curGradient;
			
			RpropResult ret = new RpropResult();
			
			if(seed != null) {
				curGradient = InitialPointFromSeed(ret,seed);
			}
			else {			
				curGradient = InitialPoint(ret);
			}
			double curUtil = ret.initialUtil;
			
			if (ret.initialUtil > 0.75) {
				return ret;
			}
			
			double[] formerGradient = new double[dim];
			double[] curValue = new double[dim];
			
			Tuple<double[],double> tup;
											
			Buffer.BlockCopy(ret.initialValue,0,curValue,0,sizeof(double)*dim);
			
			
			formerGradient = curGradient;
			//Buffer.BlockCopy(curGradient,0,formerGradient,0,sizeof(double)*dim);
			
			
			int itcounter = 0;
			int badcounter = 0;
			
		
			//Log(curUtil,curValue);
			while(itcounter++ < 40 && badcounter < 6) {
				
				for(int i=0; i<dim; i++) {
					if (curGradient[i] * formerGradient[i] > 0) rpropStepWidth[i] *= 1.3;
					else if (curGradient[i] * formerGradient[i] < 0) rpropStepWidth[i] *= 0.5;
					rpropStepWidth[i] = Math.Max(0.0001,rpropStepWidth[i]);
					if (curGradient[i] > 0) curValue[i] += rpropStepWidth[i];
					else if (curGradient[i] < 0) curValue[i] -= rpropStepWidth[i];
						
					if (curValue[i] > limits[i,1]) curValue[i] = limits[i,1];
					else if (curValue[i] < limits[i,0]) curValue[i] = limits[i,0];
				
				}
				
				tup = term.Differentiate(curValue);
				bool allZero = true;
				
				for(int i=0; i < dim; i++) {
					if (Double.IsNaN(tup.Item1[i])) {
						Console.WriteLine("NaN in gradient, aborting!");
						ret.aborted=false;//true; //HACK!
						
						return ret;					
					}
					allZero &= (tup.Item1[i]==0);
					
				}
				
				curUtil = tup.Item2;
				formerGradient = curGradient;
				curGradient = tup.Item1;

				//Log(curUtil,curValue);
				
				//Console.WriteLine("CurUtil: {0} Final {1}",curUtil,ret.finalUtil);
				
				if (curUtil > ret.finalUtil) {
					
					badcounter = 0;
					
				
					ret.finalUtil = curUtil;
					Buffer.BlockCopy(curValue,0,ret.finalValue,0,sizeof(double)*dim);
					
					if (curUtil > 0.75) return ret;			
					
				} else {
						badcounter++;
				}
				if (allZero) {
					//Console.WriteLine("All Zero!");
					
					ret.aborted = false;
					
					return ret;
				}

			}
						
			ret.aborted = false;
			return ret;
		}
		
		protected void InitialStepSize() {
			for(int i=0; i<this.dim; i++) {
				//double range = this.limits[i,1]-this.limits[i,0];
				this.rpropStepWidth[i] = initialStepSize*ranges[i];	
				this.rpropStepConvergenceThreshold[i] = rpropStepWidth[i]*this.RPropConvergenceStepSize;
			}
		}
		protected bool EvalResults() {
			/* 
			 * TODO: Compare Average Distance between start and end point to
			 * Average distance between any two arbitrary points.
			 * 
			 * Latter one can be found in http://www.math.uni-muenster.de/reine/u/burgstal/d18.pdf :
			 * maxDist = sqrt(dim)
			 * avgDist <= 1 / sqrt(6) * sqrt((1+2*sqrt(1-3/(5*dim)))/3) * maxDist
			 * 
			 * aprox: (http://www.jstor.org/pss/1427094)
			 * 
			 * avgDist = sqrt(dim/3) * (1 - 1/(10*k) - 13/(280*k^2) - 101/ (2800*k^3) - 37533 / (1232000 k^4) * O(sqrt(k)) (?)
			 * */
			int count = this.rResults.Count;
			//Console.WriteLine("Eval");
			/*
			double maxDist = Math.Sqrt(dim);
			double avgDist =  1 / Math.Sqrt(6) * Math.Sqrt((1+2*Math.Sqrt(1-3/(5*dim)))/3) * maxDist;
			
			
			double allDist = 0;
			for(int i=0; i<count; i++) {
				if (rResults[i].aborted) {
					abortedCount++;
				} else {
					allDist += rResults[i].DistanceTraveledNormed(this.ranges);						
				}
			}

			double avgDistTraveled = allDist / (count-abortedCount);
			
			Console.WriteLine("Traveled: {0} Expected: {1}",avgDistTraveled,avgDist);
			*/
			//if (avgDistTraveled < 0.66* avgDist) return false;
			int abortedCount = 0;
			
			
			
			//double[] midValue = new double[dim];
			double[] midInitValue = new double[dim];
			
			//double[] valueDev = new double[dim];
			double[] valueInitDev = new double[dim];
			
			//double midUtil =0;
			for(int i=0; i<count; i++) {
				if (rResults[i].aborted) {
					abortedCount++;
				} else {
					//midUtil += rResults[i].finalUtil;
					for(int j=0; j<dim; j++) {
						//midValue[j] += rResults[i].finalValue[j];
						midInitValue[j] += rResults[i].initialValue[j];
					}
						
				}
			}
			if (count-abortedCount < dim) return false;
			for(int j=0; j<dim; j++) {
				//midValue[j] /= (count-abortedCount);
				midInitValue[j] /= (count-abortedCount);
			}
			for(int i=0; i<count; i++) {
				if (rResults[i].aborted) continue;
				for (int j=0; j<dim; j++) {
					
					//valueDev[j] += Math.Pow((rResults[i].finalValue[j]-midValue[j])/ranges[j],2);
					valueInitDev[j] += Math.Pow((rResults[i].initialValue[j]-midInitValue[j])/ranges[j],2);
				}				
			}
			for(int j=0; j<dim; j++) {
				
				
				//if (valueDev[j] > valueInitDev[j]) return false;
				//valueDev[j] /= (count-abortedCount);
				valueInitDev[j] /= (count-abortedCount);							
				//if (Math.Sqrt(valueInitDev[j]) < 0.22) return false;
				if (valueInitDev[j] < 0.0441) return false;
			}
			
			
			
			Console.WriteLine("Runs: {0} Aborted: {1}",count,abortedCount);
			
			/*Console.WriteLine("Final Std Deviation: ");
			for(int j=0; j<dim; j++) {
				Console.Write("{0}\t",Math.Sqrt(valueDev[j]));
			}
			Console.WriteLine();
			Console.WriteLine("Initial Std Deviation: ");
			for(int j=0; j<dim; j++) {
				Console.Write("{0}\t",Math.Sqrt(valueInitDev[j]));
			}
			Console.WriteLine();
			 */
			/*for(int j=0; j<dim; j++) {
				if (valueDev[j] > valueInitDev[j]) return false;
				
			}*/
			
			
			return true;
		}
	}
	

}

