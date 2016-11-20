//#define GSOLVER_LOG
using System;
using AutoDiff;
using AD=AutoDiff;
using System.IO;
using System.Text;
using Castor;
using System.Collections.Generic;

namespace Alica.Reasoner
{
public class QGSolver  {
		
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
	double initialStepSize = 0.005;
	double utilitySignificanceThreshold = 1E-22;
	Random rand;	
	int dim;
	double[,] limits;
	double[] ranges;
	double[] rpropStepWidth;
	//double[][] seeds;
	AD.ICompiledTerm term;
		
	StreamWriter sw;
		
	List<RpropResult> rResults;
	protected static int fcounter =0;	
	//Configuration:
	protected bool seedWithUtilOptimum;
			
	public QGSolver () {
		
		this.rand = new Random();
		this.rResults = new List<RpropResult>();
			
		this.seedWithUtilOptimum = true;
	}
	protected void InitLog() {
			
			string logFile = "/tmp/test"+(fcounter++)+".dbg";
			FileStream file = new FileStream(logFile, FileMode.Create);
			this.sw = new StreamWriter(file);				
			sw.AutoFlush = true;
	}
	protected void Log(double util, double[] val,int type) {
			sw.Write(util);
			sw.Write("\t");
			sw.Write(type);
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
			
			return Solve(equation,args,limits,null, out util);
	}
	public bool SolveSimple(AD.Term equation, AD.Variable[] args, double[,] limits) {
			
			return SolveSimple(equation,args,limits,null);
	}
		                                                                                           
	public double[] Solve(AD.Term equation, AD.Variable[] args, double[,] limits, double[][] seeds, out double util) {
			//this.FEvals = 0;
			util = 0;
#if (GSOLVER_LOG)			
			InitLog();
#endif			
			rResults.Clear();
			
			
			
			this.dim = args.Length;
			this.limits = limits;
			this.ranges = new double[dim];
			for(int i=0; i<dim; i++) {
				this.ranges[i] = (this.limits[i,1]-this.limits[i,0]);
			}
			equation = equation.AggregateConstants();
			term = AD.TermUtils.Compile(equation,args);
			AD.ConstraintUtility cu = (AD.ConstraintUtility) equation;
			bool utilIsConstant =(cu.Utility is AD.Constant);
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
			
						
			this.rpropStepWidth = new double[dim];
			this.samplePoint = new double[dim];
			if (seeds != null) {
				RpropResult rpfirst = RPropLoop(seeds[0],true,false);
				if (utilIsConstant && rpfirst.finalUtil > 0.75) {
						util = rpfirst.finalUtil;
						//Console.WriteLine("FEvals: {0}",this.FEvals);
						return rpfirst.finalValue;
				}
				rResults.Add(rpfirst);
				for(int i=1; i<seeds.Length; i++) {
					RpropResult rp = RPropLoop(seeds[i],false,false);
					if (utilIsConstant && rp.finalUtil > 0.75) {
						util = rp.finalUtil;
						//Console.WriteLine("FEvals: {0}",this.FEvals);
						return rp.finalValue;
					}
					rResults.Add(rp);	
				}
			}
			if(!constraintIsConstant && !utilIsConstant && seedWithUtilOptimum) {
				
				AD.ICompiledTerm curProb = term;
				term = AD.TermUtils.Compile(((AD.ConstraintUtility)equation).Utility,args);
				double[] utilitySeed = RPropLoop(null).finalValue;	
/*Console.WriteLine("Unconstraint Seed:");
Console.Write("S: ");
foreach(double d in utilitySeed) Console.Write("{0} ",d);
Console.WriteLine();
*/				
				term = curProb;
				rResults.Add(RPropLoop(utilitySeed,false,false));
			}
			
			int runs = Math.Max(3,1*dim - (seeds==null?0:seeds.Length));
			//runs = 10;

			RpropResult rpQS = RPropLoop(null,false,true);
			if(utilIsConstant && rpQS.finalUtil > 0.75) {
				util = rpQS.finalUtil;
				return rpQS.finalValue;
			}
			rResults.Add(rpQS);	

			for(int i=1; i<runs; i++) {				
				RpropResult rp = RPropLoop(null,false,false);
				if(utilIsConstant && rp.finalUtil > 0.75) {
					util = rp.finalUtil;
					//Console.WriteLine("FEvals: {0}",this.FEvals);
					return rp.finalValue;
				}
				rResults.Add(rp);	
			}
			
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
			//Console.WriteLine("FEvals: {0}",this.FEvals);	
			
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
			this.samplePoint = new double[dim];
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
			this.samplePoint = new double[dim];
			this.Runs = 1;
			//this.Runs = 0;
			this.FEvals = 0;
			//int runs = 1000000;
			RpropResult rfirst = RPropLoop(null,false,true);
			if (rfirst.finalUtil > 0.75) {
					res = rfirst.finalValue;					
			}
			else while(true) {
				this.Runs++;
				//RpropResult r = RPropLoopSimple(null);
				RpropResult r = RPropLoop(null,false,false);
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
			return RPropLoop(seed,false,false);
		}
		protected double[] samplePoint;
		protected RpropResult RPropLoop(double[] seed, bool precise, bool useQEstimate) {
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
			double[] otherSamplePoint = new double[dim];
			
			Tuple<double[],double> tup = null;
											
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
			Log(curUtil,curValue,1);
#endif
			
			int maxIter = 60;
			int maxBad = 30;
			double minStep = 1E-11;
			if(precise) {
				maxIter = 110;
				maxBad = 60;
				minStep = 1E-15;				
			}
			
			while(itcounter++ < maxIter && badcounter < maxBad) {
				if(useQEstimate && tup!=null) {					
					for(int i=0; i<dim;i++) {					
						otherSamplePoint[i] = curValue[i]+Math.Sign(curGradient[i])*Math.Max(1,rpropStepWidth[i]/3.0);
					}
					double oval = term.Evaluate(otherSamplePoint);
				
					getNewSamplePoint(oval,tup.Item2,curValue,otherSamplePoint,tup.Item1);
					Tuple<double[],double> tup2 = term.Differentiate(samplePoint);
					this.FEvals +=2;
					if (tup2.Item2 > tup.Item2) {
						//useQEstimate = false;
						//Console.WriteLine("y");
						curUtil = tup2.Item2;
						formerGradient = curGradient;
						curGradient = tup2.Item1;
						bool allZero1 = true;
						for(int i=0; i<dim; i++) {
							allZero1 &= curGradient[i] == 0;
							curValue[i] = samplePoint[i];
							if (curValue[i] > limits[i,1]) curValue[i] = limits[i,1];
							else if (curValue[i] < limits[i,0]) curValue[i] = limits[i,0];					
						}
						#if (GSOLVER_LOG)									
							Log(curUtil,curValue,2);
						#endif	
						if (curUtil > ret.finalUtil) {
							badcounter = 0;//Math.Max(0,badcounter-1);
					
							ret.finalUtil = curUtil;
							Buffer.BlockCopy(curValue,0,ret.finalValue,0,sizeof(double)*dim);					
						}
						if (allZero1) {
							ret.aborted = false;
							#if (GSOLVER_LOG)									
								LogStep();
							#endif			
							return ret;
						}
					}
				}
				
				
				for(int i=0; i<dim; i++) {
					if (curGradient[i] * formerGradient[i] > 0) rpropStepWidth[i] *= 1.3;
					else if (curGradient[i] * formerGradient[i] < 0) rpropStepWidth[i] *= 0.5;
					rpropStepWidth[i] = Math.Max(minStep,rpropStepWidth[i]);
					//rpropStepWidth[i] = Math.Max(0.000001,rpropStepWidth[i]);
					if (curGradient[i] > 0) curValue[i] += rpropStepWidth[i];
					else if (curGradient[i] < 0) curValue[i] -= rpropStepWidth[i];
					
					if (curValue[i] > limits[i,1]) curValue[i] = limits[i,1];
					else if (curValue[i] < limits[i,0]) curValue[i] = limits[i,0];
				}
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
				Log(curUtil,curValue,1);
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
		protected void getNewSamplePoint(double fq,double fp,double[] p,double[] q,double[] gp) {			
			for(int i=0; i<dim; i++) {
				double a = fq - fp + gp[i]*(p[i]-q[i]);
				a /= q[i]*q[i]-2*p[i]*q[i]+4*p[i]*p[i];
				double b = gp[i]-2*a*p[i];
				samplePoint[i] = - b/2*a;
				//samplePoint[i] = (-b/2*a + p[i])/2.0;
			}			
		}
		/*protected void getNewSamplePoint(double fq,double fp,double[] p,double[] q,double[] gp) {			
			for(int i=0; i<dim; i++) {
				double a = fq - fp + gp[i]*(p[i]-q[i]);
				a /= q[i]*q[i]-2*p[i]*q[i]+4*p[i]*p[i];
				double b = gp[i]-2*a*p[i];
				double pc = b/a;
				pc= -pc/2;
				double qc = (fp-gp[i]*p[i]+4*a*p[i]*p[i])/a;
				double s = pc*pc;
				if (s > qc) {
					s = Math.Sqrt(s-qc);
					samplePoint[i] = pc;
					if (pc > p[i]) samplePoint[i] -= s;
					else samplePoint[i] += s;
					samplePoint[i] = samplePoint[i] + (samplePoint[i]-p[i])*1E-8;
				}
				else samplePoint[i] = - b/2*a;

			}			
		}*/
		                                                                  
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

