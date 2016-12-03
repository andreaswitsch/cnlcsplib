package Reasoner;

import Reasoner.CNSAT.CNSat;
import Reasoner.CNSAT.FormulaTransform;
import Reasoner.CNSAT.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CNSMTGSolver
{
    public int getRuns() throws Exception {
        return this.runCount;
    }

    public int getFEvals() throws Exception {
        return this.fevalsCount;
    }

    double initialStepSize = 0.005;
    double utilitySignificanceThreshold = 1E-22;
    Random rand = new Random();
    int dim;
    double[][] limits;
    double[] ranges;
    double[] rpropStepWidth;
    double[] rpropStepConvergenceThreshold;
    private double __RPropConvergenceStepSize;
    public double getRPropConvergenceStepSize() {
        return __RPropConvergenceStepSize;
    }

    public void setRPropConvergenceStepSize(double value) {
        __RPropConvergenceStepSize = value;
    }

    AutoDiff.Variable[] currentArgs;

    static CNSMTGSolver instance = null;
    List<RpropResult> rResults = new ArrayList<RpropResult>();
    protected static int fcounter = 0;
    //Configuration:
    protected boolean seedWithUtilOptimum;
    protected Double[] lastSeed;
    protected RpropResult r1 = null;
    protected int probeCount = 0;
    protected int successProbeCount = 0;
    protected int intervalCount = 0;
    protected int successIntervalCount = 0;
    protected int fevalsCount = 0;
    protected int runCount = 0;
    private long __MaxFEvals;
    public long getMaxFEvals() {
        return __MaxFEvals;
    }
    protected CNSat ss;
    protected FormulaTransform ft;
    //protected IntervalPropagator ip;

    public long maxSolveTime;
    public long begin;
    //    public void setMaxFEvals(long value) {
//        __MaxFEvals = value;
//    }
//
//    private boolean __UseIntervalProp = new boolean();
//    public boolean getUseIntervalProp() {
//        return __UseIntervalProp;
//    }
//
//    public void setUseIntervalProp(boolean value) {
//        __UseIntervalProp = value;
//    }
//
//    private boolean __Optimize = new boolean();
//    public boolean getOptimize() {
//        return __Optimize;
//    }
//
//    public void setOptimize(boolean value) {
//        __Optimize = value;
//    }
//
//    public CNSMTGSolver() throws Exception {
//        Term.SetAnd(Term.AndType.and);
//        Term.SetOr(Term.OrType.max);
//        this.rand = new Random();
//        this.rResults = new List<RpropResult>();
//        //this.seedWithUtilOptimum = true;
//        this.ft = new FormulaTransform();
//        this.ip = new IntervalPropagator();
//        instance = this;
//        this.setMaxFEvals(SystemConfig.LocalInstance["Alica"].GetInt("Alica", "CSPSolving", "MaxFunctionEvaluations"));
//        this.maxSolveTime = ((ulong)SystemConfig.LocalInstance["Alica"].GetInt("Alica", "CSPSolving", "MaxSolveTime")) * 1000000;
//        //RPropConvergenceStepSize = 1E-2;
//        setRPropConvergenceStepSize(0);
//        setUseIntervalProp(true);
//        setOptimize(false);
//    }
//
//    protected void initLog() throws Exception {
//        String logFile = "/tmp/test" + (fcounter++) + ".dbg";
//        FileStream file = new FileStream(logFile, FileMode.Create);
//        this.sw = new StreamWriter(file);
//        sw.AutoFlush = true;
//    }
//
//    protected void log(double util, double[] val) throws Exception {
//        sw.Write(util);
//        sw.Write("\t");
//        for (int i = 0;i < dim;i++)
//        {
//            sw.Write(val[i]);
//            sw.Write("\t");
//        }
//        sw.WriteLine();
//    }
//
//    protected void logStep() throws Exception {
//        sw.WriteLine();
//        sw.WriteLine();
//    }
//
//    protected void closeLog() throws Exception {
//        if (sw != null)
//        {
//            sw.Close();
//            sw = null;
//        }
//
//    }
//
//    public double[] solve(Term equation, Variable[] args, double[][] limits, RefSupport<double> util) throws Exception {
//        RefSupport<double> refVar___0 = new RefSupport<double>();
//        resVar___0 = Solve(equation, args, limits, null, refVar___0);
//        util.setValue(refVar___0.getValue());
//        return resVar___0;
//    }
//
//    public double[] solve(Term equation, Variable[] args, double[][] limits, double[][] seeds, RefSupport<double> util) throws Exception {
//        lastSeed = null;
//        probeCount = 0;
//        successProbeCount = 0;
//        intervalCount = 0;
//        successIntervalCount = 0;
//        fevalsCount = 0;
//        runCount = 0;
//        this.begin = RosCS.RosSharp.Now();
//        //this.FEvals = 0;
//        util.setValue(0);
//        //ft.Reset();
//        rResults.Clear();
//        currentArgs = args;
//        this.dim = args.Length;
//        this.limits = limits;
//        this.ranges = new double[dim];
//        this.rpropStepWidth = new double[dim];
//        this.rpropStepConvergenceThreshold = new double[dim];
//        equation = equation.AggregateConstants();
//        ConstraintUtility cu = (ConstraintUtility)equation;
//        boolean utilIsConstant = (cu.Utility instanceof Constant);
//        boolean constraintIsConstant = (cu.Constraint instanceof Constant);
//        if (constraintIsConstant)
//        {
//            if (((Constant)cu.Constraint).Value < 0.25)
//            {
//                util.setValue(((Constant)cu.Constraint).Value);
//                double[] ret = new double[dim];
//                for (int i = 0;i < dim;i++)
//                {
//                    ret[i] = (this.limits[i, 1] + this.limits[i, 0]) / 2.0;
//                }
//                return ret;
//            }
//
//        }
//
//        //this.ranges[i]/2.0+this.limits[i,0];
//        ss = new CNSat();
//        ss.setUseIntervalProp(this.getUseIntervalProp());
//        //Console.WriteLine(cu.Constraint);
//        LinkedList<Clause> cnf = ft.TransformToCNF(cu.Constraint, ss);
//        /*Console.WriteLine("Atoms: {0}, Occurrence: {1}",ft.Atoms.Count,ft.AtomOccurrence);
//        			Console.WriteLine("Clauses: {0}",cnf.Count);
//        			foreach(Term atom in ft.Atoms.Keys) {
//        				Console.WriteLine("-------");
//        				Console.WriteLine(atom);
//        				Console.WriteLine("-------");
//        			}*/
//        /*
//        			int litc=1;
//        			List<Term> terms = new List<Term>(ft.Atoms);
//
//        			currentAtoms = new Dictionary<int, Term>();
//
//        			foreach(Term t in terms) {
//        				foreach(Literal l in ft.References[t]) {
//        					l.Id = litc;
//        				}
//        				currentAtoms.Add(litc,t);
//        				litc++;
//        			}*/
//        if (getUseIntervalProp())
//        {
//            ip.SetGlobalRanges(args, limits, ss);
//        }
//
//        for (Object __dummyForeachVar0 : cnf)
//        {
//            Clause c = (Clause)__dummyForeachVar0;
//            if (!c.getIsTautologic())
//            {
//                if (c.getLiterals().Count == 0)
//                {
//                    util.setValue(Double.MinValue);
//                    double[] ret = new double[dim];
//                    for (int i = 0;i < dim;i++)
//                    {
//                        ret[i] = (this.limits[i, 1] + this.limits[i, 0]) / 2.0;
//                    }
//                    return ret;
//                }
//
//                //Post Clause Here
//                //Console.Write("\nAdding Clause with "+c.Literals.Count+" Literals\n");
//                //c.Print();
//                ss.addBasicClause(c);
//            }
//
//        }
//        ss.setCNSMTGSolver(this);
//        ss.init();
//        //PRE-Propagation:
//        if (getUseIntervalProp())
//        {
//            if (!ip.prePropagate(ss.getVariables()))
//            {
//                Console.WriteLine("Unsatisfiable (unit propagation)");
//                return null;
//            }
//
//        }
//
//        //END-PrePropagation
//        //Console.WriteLine("Variable Count: " + ss.Variables.Count);
//        boolean solutionFound = false;
//        ss.setUnitDecissions(ss.getDecisions().Count);
//        do
//        {
//            if (!solutionFound)
//            {
//                ss.emptySATClause();
//                ss.emptyTClause();
//                ss.backTrack(ss.getUnitDecissions());
//            }
//
//            solutionFound = ss.solve();
//            if (getOptimize())
//                r1 = RPropOptimizeFeasible(ss.getDecisions(), ((ConstraintUtility)equation).Utility, args, r1.finalValue, false);
//
//            if (!solutionFound && r1.finalUtil > 0)
//                r1.finalUtil = -1;
//
//            util.setValue(r1.finalUtil);
//            if (!getOptimize() && solutionFound)
//                return r1.finalValue;
//            else if (getOptimize())
//            {
//                //optimization case
//                rResults.Add(r1);
//                Clause c = new Alica.Clause();
//                for (Object __dummyForeachVar1 : ss.getDecisions())
//                {
//                    Var v = (Var)__dummyForeachVar1;
//                    c.add(new Lit(v,v.getAssignment() == Assignment.True ? Assignment.False : Assignment.True));
//                }
//                //ss.addBasicClause(c);
//                ss.addIClause(c);
//                ss.backTrack(ss.getDecisions()[ss.getDecisions().Count - 1].DecisionLevel);
//                solutionFound = false;
//            }
//
//        }
//        while (!solutionFound && this.begin + this.maxSolveTime > RosCS.RosSharp.Now());
//        /*&& this.runCount < this.MaxFEvals*/
//        //Console.WriteLine("Probes: {0}/{1}\tIntervals: {2}/{3}",successProbeCount,probeCount,successIntervalCount,intervalCount);
//        //ip.PrintStats();
//        //Console.WriteLine("Rprop Runs: {0}\t FEvals {1}\t Solutions Found: {2}",this.runCount,this.fevalsCount, rResults.Count);
//        //return best result
//        if (rResults.Count > 0)
//        {
//            for (Object __dummyForeachVar2 : rResults)
//            {
//                RpropResult rp = (RpropResult)__dummyForeachVar2;
//                if (rp.finalUtil > util.getValue())
//                {
//                    util.setValue(rp.finalUtil);
//                    r1 = rp;
//                }
//
//            }
//            return r1.finalValue;
//        }
//
//        Console.WriteLine("Unsatisfiable");
//        return null;
//    }
//
//    //if(util>this.utilitySignificanceThreshold) return r1.finalValue;
   public boolean intervalPropagate(List<Var> decisions, Double[][] curRanges) throws Exception {
//        intervalCount++;
//        /*Console.Write("SAT proposed({0}): ", decisions.Count);
//        			foreach(CNSAT.Var v in decisions) {
//        				v.Print();
//        				Console.Write(" ");
//        			}
//        			Console.WriteLine();
//        			*/
//        //double[,] curRanges=null;
//        List<Var> offending = null;
//        RefSupport<double[][]> refVar___1 = new RefSupport<double[][]>();
//        RefSupport<List<Var>> refVar___2 = new RefSupport<List<Var>>();
//        boolean boolVar___1 = !ip.Propagate(decisions, refVar___1, refVar___2);
//        curRanges.setValue(refVar___1.getValue());
//        offending = refVar___2.getValue();
//        if (boolVar___1)
//        {
//            /*Console.WriteLine("Propagation FAILED offenders are:");
//            				foreach(CNSAT.Var v in offending) {
//            					//Console.WriteLine(v + "\t"+(v.Assignment==CNSAT.Assignment.True?v.Term:v.Term.Negate()));
//            					Console.Write(v + " ");
//            				}
//            				Console.WriteLine();*/
//            if (offending != null)
//            {
//                Alica.Clause learnt = new Alica.Clause();
//                for (Object __dummyForeachVar4 : offending)
//                {
//                    Var v = (Var)__dummyForeachVar4;
//                    learnt.add(new Lit(v,v.getAssignment() == Assignment.True ? Assignment.False : Assignment.True));
//                }
//                //if(learnt.Literals.Count>0) {
//                ss.addIClause(learnt);
//            }
//
//            return false;
//        }
//
//        //}
//        //Console.WriteLine("Propagation succeeded");
//        this.limits = curRanges.getValue();
//        successIntervalCount++;
        return true;
    }
//
    public boolean probeForSolution(List<Var> decisions, Double[] solution) throws Exception {
//        probeCount++;
//        /*Console.Write("SAT proposed({0}): ", decisions.Count);
//        			foreach(CNSAT.Var v in decisions) {
//        				v.Print();
//        				Console.Write(" ");
//        			}
//        			Console.WriteLine();*/
//        solution.setValue(null);
//        for (int i = 0;i < dim;i++)
//        {
//            //	Console.WriteLine("[{0}..{1}]",this.limits[i,0],this.limits[i,1]);
//            this.ranges[i] = (this.limits[i, 1] - this.limits[i, 0]);
//        }
//        for (int i = 0;i < decisions.Count;i++)
//        {
//            decisions[i].CurTerm = (decisions[i].Assignment == Assignment.True ? decisions[i].PositiveTerm : decisions[i].NegativeTerm);
//        }
//        //int idx = Math.Max(0,ss.DecisionLevel.Count-1);
//        //r1 = RPropFindFeasible(decisions, ss.DecisionLevel[idx].Seed);
//        r1 = RPropFindFeasible(decisions, lastSeed);
//        if (r1.finalUtil < 0.5)
//            r1 = RPropFindFeasible(decisions, null);
//
//        if (r1.finalUtil < 0.5)
//        {
//            //Probe was not successfull -> assignment not valid
//            //Console.Write(".");
//            //Console.WriteLine("Could not find point for {0}",constraint);
//            Alica.Clause learnt = new Alica.Clause();
//            for (Object __dummyForeachVar5 : decisions)
//            {
//                Var v = (Var)__dummyForeachVar5;
//                learnt.add(new Lit(v,v.getAssignment() == Assignment.True ? Assignment.False : Assignment.True));
//            }
//            ss.addTClause(learnt);
//            return false;
//        }
//
//        //ss.DecisionLevel[ss.DecisionLevel.Count-1].Seed = r1.finalValue;
//        lastSeed = r1.finalValue;
//        solution.setValue(r1.finalValue);
//        successProbeCount++;
        return true;
    }
//
//    protected RpropResult rPropFindFeasible(List<Var> constraints, double[] seed) throws Exception {
//        runCount++;
//        initialStepSize();
//        double[] curGradient = new double[]();
//        RpropResult ret = new RpropResult();
//        //			curGradient = InitialPoint(constraints, ret);
//        if (seed != null)
//        {
//            curGradient = InitialPointFromSeed(constraints, ret, seed);
//        }
//        else
//        {
//            curGradient = InitialPoint(constraints, ret);
//        }
//        double curUtil = ret.initialUtil;
//        if (curUtil > 0.5)
//        {
//            return ret;
//        }
//
//        double[] formerGradient = new double[dim];
//        double[] curValue = new double[dim];
//        //Tuple<double[],double> tup;
//        Buffer.BlockCopy(ret.initialValue, 0, curValue, 0, * dim);
//        formerGradient = curGradient;
//        int itcounter = 0;
//        int badcounter = 0;
//        int maxIter = 60;
//        int maxBad = 30;
//        double minStep = 1E-11;
//        while (itcounter++ < maxIter && badcounter < maxBad)
//        {
//            for (int i = 0;i < dim;i++)
//            {
//                if (curGradient[i] * formerGradient[i] > 0)
//                    rpropStepWidth[i] *= 1.3;
//                else if (curGradient[i] * formerGradient[i] < 0)
//                    rpropStepWidth[i] *= 0.5;
//
//                rpropStepWidth[i] = Math.Max(minStep, rpropStepWidth[i]);
//                if (curGradient[i] > 0)
//                    curValue[i] += rpropStepWidth[i];
//                else if (curGradient[i] < 0)
//                    curValue[i] -= rpropStepWidth[i];
//
//                if (curValue[i] > limits[i, 1])
//                    curValue[i] = limits[i, 1];
//                else if (curValue[i] < limits[i, 0])
//                    curValue[i] = limits[i, 0];
//
//            }
//            this.fevalsCount++;
//            formerGradient = curGradient;
//            RefSupport<double[]> refVar___3 = new RefSupport<double[]>();
//            RefSupport<double> refVar___4 = new RefSupport<double>();
//            Differentiate(constraints, curValue, refVar___3, refVar___4);
//            curGradient = refVar___3.getValue();
//            curUtil = refVar___4.getValue();
//            boolean allZero = true;
//            for (int i = 0;i < dim;i++)
//            {
//                if (Double.IsNaN(curGradient[i]))
//                {
//                    //Console.Error.WriteLine("NaN in gradient, aborting!");
//                    ret.aborted = true;
//                    return ret;
//                }
//
//                allZero &= (curGradient[i] == 0);
//            }
//            //Console.WriteLine("CurUtil: {0} Final {1}",curUtil,ret.finalUtil);
//            if (curUtil > ret.finalUtil)
//            {
//                badcounter = 0;
//                //Math.Max(0,badcounter-1);
//                ret.finalUtil = curUtil;
//                Buffer.BlockCopy(curValue, 0, ret.finalValue, 0, * dim);
//                //ret.finalValue = curValue;
//                if (curUtil > 0.75)
//                    return ret;
//
//            }
//            else
//            {
//                badcounter++;
//            }
//            if (allZero)
//            {
//                ret.aborted = false;
//                return ret;
//            }
//
//        }
//        ret.aborted = false;
//        return ret;
//    }
//
//    protected RpropResult rPropOptimizeFeasible(List<Var> constraints, Term ut, Variable[] args, double[] seed, boolean precise) throws Exception {
//        //Compiled Term zusammenbauen
//        Term constr = Term.True;
//        for (Object __dummyForeachVar6 : constraints)
//        {
//            Var v = (Var)__dummyForeachVar6;
//            if (v.getAssignment() == Alica.Assignment.True)
//                constr &= v.getTerm();
//            else
//                constr &= ConstraintBuilder.Not(v.getTerm());
//        }
//        ConstraintUtility cu = new ConstraintUtility(constr, ut);
//        ICompiledTerm term = TermUtils.Compile(cu, args);
//        Tuple<double[], double> tup = new Tuple<double[], double>();
//        //fertig zusammengebaut
//        runCount++;
//        initialStepSize();
//        double[] curGradient = new double[]();
//        RpropResult ret = new RpropResult();
//        //			curGradient = InitialPoint(constraints, ret);
//        if (seed != null)
//        {
//            curGradient = InitialPointFromSeed(constraints, ret, seed);
//        }
//        else
//        {
//            curGradient = InitialPoint(constraints, ret);
//        }
//        double curUtil = ret.initialUtil;
//        double[] formerGradient = new double[dim];
//        double[] curValue = new double[dim];
//        //Tuple<double[],double> tup;
//        Buffer.BlockCopy(ret.initialValue, 0, curValue, 0, * dim);
//        formerGradient = curGradient;
//        int itcounter = 0;
//        int badcounter = 0;
//        int maxIter = 60;
//        int maxBad = 30;
//        double minStep = 1E-11;
//        if (precise)
//        {
//            maxIter = 120;
//            //110
//            maxBad = 60;
//            //60
//            minStep = 1E-15;
//        }
//
//        //15
//        int convergendDims = 0;
//        while (itcounter++ < maxIter && badcounter < maxBad)
//        {
//            convergendDims = 0;
//            for (int i = 0;i < dim;i++)
//            {
//                if (curGradient[i] * formerGradient[i] > 0)
//                    rpropStepWidth[i] *= 1.3;
//                else if (curGradient[i] * formerGradient[i] < 0)
//                    rpropStepWidth[i] *= 0.5;
//
//                rpropStepWidth[i] = Math.Max(minStep, rpropStepWidth[i]);
//                //rpropStepWidth[i] = Math.Max(0.000001,rpropStepWidth[i]);
//                if (curGradient[i] > 0)
//                    curValue[i] += rpropStepWidth[i];
//                else if (curGradient[i] < 0)
//                    curValue[i] -= rpropStepWidth[i];
//
//                if (curValue[i] > limits[i, 1])
//                    curValue[i] = limits[i, 1];
//                else if (curValue[i] < limits[i, 0])
//                    curValue[i] = limits[i, 0];
//
//                //Console.Write("{0}\t",curValue[i]);
//                if (rpropStepWidth[i] < rpropStepConvergenceThreshold[i])
//                {
//                    ++convergendDims;
//                }
//
//            }
//            //Abort if all dimensions are converged
//            if (!precise && convergendDims >= dim)
//            {
//                return ret;
//            }
//
//            this.fevalsCount++;
//            formerGradient = curGradient;
//            tup = term.Differentiate(curValue);
//            boolean allZero = true;
//            for (int i = 0;i < dim;i++)
//            {
//                if (Double.IsNaN(tup.Item1[i]))
//                {
//                    ret.aborted = false;
//                    return ret;
//                }
//
//                //true; //HACK!
//                allZero &= (tup.Item1[i] == 0);
//            }
//            curUtil = tup.Item2;
//            formerGradient = curGradient;
//            curGradient = tup.Item1;
//            //Console.WriteLine("CurUtil: {0} Final {1}",curUtil,ret.finalUtil);
//            if (curUtil > ret.finalUtil)
//            {
//                badcounter = 0;
//                //Math.Max(0,badcounter-1);
//                ret.finalUtil = curUtil;
//                Buffer.BlockCopy(curValue, 0, ret.finalValue, 0, * dim);
//                //ret.finalValue = curValue;
//                if (curUtil > 0.75)
//                    return ret;
//
//            }
//            else
//            {
//                badcounter++;
//            }
//            if (allZero)
//            {
//                ret.aborted = false;
//                return ret;
//            }
//
//        }
//        ret.aborted = false;
//        return ret;
//    }
//
//    protected void differentiate(List<Var> constraints, double[] val, RefSupport<double[]> gradient, RefSupport<double> util) throws Exception {
//        Tuple<double[], double> t1 = constraints[0].CurTerm.Differentiate(val);
//        gradient.setValue(t1.Item1);
//        util.setValue(t1.Item2);
//        for (int i = 1;i < constraints.Count;i++)
//        {
//            Tuple<double[], double> tup = constraints[i].CurTerm.Differentiate(val);
//            if (tup.Item2 <= 0)
//            {
//                if (util.getValue() > 0)
//                    util.setValue(tup.Item2);
//                else
//                    util.setValue(util.getValue() + tup.Item2);
//                for (int j = 0;j < dim;j++)
//                {
//                    gradient.getValue()[j] += tup.Item1[j];
//                }
//            }
//
//        }
//    }
//
//    //return new Tuple<double[], double>(gradient,util);
//    protected double[] initialPointFromSeed(List<Var> constraints, RpropResult res, double[] seed) throws Exception {
//        Tuple<double[], double> tup = new Tuple<double[], double>();
//        boolean found = true;
//        res.initialValue = new double[dim];
//        res.finalValue = new double[dim];
//        double[] gradient = new double[]();
//        do
//        {
//            gradient = new double[dim];
//            found = true;
//            res.initialUtil = 1;
//            for (int i = 0;i < dim;i++)
//            {
//                if (Double.IsNaN(seed[i]))
//                {
//                    res.initialValue[i] = rand.NextDouble() * ranges[i] + limits[i, 0];
//                }
//                else
//                {
//                    res.initialValue[i] = Math.Min(Math.Max(seed[i], limits[i, 0]), limits[i, 1]);
//                }
//            }
//            //why this?
//            this.fevalsCount++;
//            for (int i = 0;i < constraints.Count;i++)
//            {
//                if (constraints[i].Assignment == Alica.Assignment.True)
//                {
//                    if (constraints[i].PositiveTerm == null)
//                        constraints[i].PositiveTerm = TermUtils.Compile(constraints[i].Term, this.currentArgs);
//
//                    constraints[i].CurTerm = constraints[i].PositiveTerm;
//                }
//                else
//                {
//                    if (constraints[i].NegativeTerm == null)
//                        constraints[i].NegativeTerm = TermUtils.Compile(constraints[i].Term.Negate(), this.currentArgs);
//
//                    constraints[i].CurTerm = constraints[i].NegativeTerm;
//                }
//                tup = constraints[i].CurTerm.Differentiate(res.initialValue);
//                for (int j = 0;j < dim;j++)
//                {
//                    if (Double.IsNaN(tup.Item1[j]))
//                    {
//                        found = false;
//                        break;
//                    }
//
//                    gradient[j] += tup.Item1[j];
//                }
//                if (!found)
//                    break;
//
//                if (tup.Item2 <= 0.0)
//                {
//                    if (res.initialUtil > 0.0)
//                        res.initialUtil = tup.Item2;
//                    else
//                        res.initialUtil += tup.Item2;
//                }
//
//            }
//        }
//        while (!found);
//        //tup = term.Differentiate(res.initialValue);
//        res.finalUtil = res.initialUtil;
//        Buffer.BlockCopy(res.initialValue, 0, res.finalValue, 0, * dim);
//        return gradient;
//    }
//
//    protected double[] initialPoint(List<Var> constraints, RpropResult res) throws Exception {
//        Tuple<double[], double> tup = new Tuple<double[], double>();
//        boolean found = true;
//        res.initialValue = new double[dim];
//        res.finalValue = new double[dim];
//        double[] gradient = new double[]();
//        do
//        {
//            gradient = new double[dim];
//            found = true;
//            res.initialUtil = 1;
//            for (int i = 0;i < dim;i++)
//            {
//                res.initialValue[i] = rand.NextDouble() * ranges[i] + limits[i, 0];
//            }
//            this.fevalsCount++;
//            for (int i = 0;i < constraints.Count;i++)
//            {
//                if (constraints[i].Assignment == Alica.Assignment.True)
//                {
//                    if (constraints[i].PositiveTerm == null)
//                        constraints[i].PositiveTerm = TermUtils.Compile(constraints[i].Term, this.currentArgs);
//
//                    constraints[i].CurTerm = constraints[i].PositiveTerm;
//                }
//                else
//                {
//                    if (constraints[i].NegativeTerm == null)
//                        constraints[i].NegativeTerm = TermUtils.Compile(constraints[i].Term.Negate(), this.currentArgs);
//
//                    constraints[i].CurTerm = constraints[i].NegativeTerm;
//                }
//                tup = constraints[i].CurTerm.Differentiate(res.initialValue);
//                for (int j = 0;j < dim;j++)
//                {
//                    if (Double.IsNaN(tup.Item1[j]))
//                    {
//                        found = false;
//                        break;
//                    }
//
//                    gradient[j] += tup.Item1[j];
//                }
//                if (!found)
//                    break;
//
//                if (tup.Item2 <= 0.0)
//                {
//                    if (res.initialUtil > 0.0)
//                        res.initialUtil = tup.Item2;
//                    else
//                        res.initialUtil += tup.Item2;
//                }
//
//            }
//        }
//        while (!found);
//        //tup = term.Differentiate(res.initialValue);
//        res.finalUtil = res.initialUtil;
//        Buffer.BlockCopy(res.initialValue, 0, res.finalValue, 0, * dim);
//        return gradient;
//    }
//
//    protected void initialStepSize() throws Exception {
//        for (int i = 0;i < this.dim;i++)
//        {
//            this.rpropStepWidth[i] = initialStepSize * ranges[i];
//            this.rpropStepConvergenceThreshold[i] = rpropStepWidth[i] * this.getRPropConvergenceStepSize();
//        }
//    }
//
//    public boolean currentCacheConsistent() throws Exception {
//        if (this.lastSeed == null)
//            return false;
//
//        for (int i = dim - 1;i >= 0;--i)
//        {
//            if (this.lastSeed[i] < this.limits[i, 0])
//                return false;
//
//            if (this.lastSeed[i] > this.limits[i, 1])
//                return false;
//
//        }
//        return true;
//    }

}


