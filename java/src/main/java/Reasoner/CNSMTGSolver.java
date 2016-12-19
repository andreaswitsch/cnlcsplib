package Reasoner;

import AutoDiff.And;
import AutoDiff.Constant;
import AutoDiff.ConstraintUtility;
import AutoDiff.ICompiledTerm;
import AutoDiff.Term;
import AutoDiff.TermUtils;
import AutoDiff.Tuple;
import AutoDiff.Variable;
import Reasoner.CNSAT.Assignment;
import Reasoner.CNSAT.CNSat;
import Reasoner.CNSAT.Clause;
import Reasoner.CNSAT.FormulaTransform;
import Reasoner.CNSAT.Lit;
import Reasoner.CNSAT.Var;
import Reasoner.IntervalPropagation.IntervalPropagator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

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
    Double[][] limits;
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
    private long maxFEvals;
    public long getMaxFEvals() {
        return maxFEvals;
    }
    protected CNSat ss;
    protected FormulaTransform ft;
    protected IntervalPropagator ip;

    public long maxSolveTime;
    public void setMaxFEvals(long value) {
        maxFEvals = value;
    }

    private boolean useIntervalProp;
    public boolean getUseIntervalProp() {
        return useIntervalProp;
    }

    public void setUseIntervalProp(boolean value) {
        useIntervalProp = value;
    }

    private boolean optimize;
    public boolean getOptimize() {
        return optimize;
    }

    public void setOptimize(boolean value) {
        optimize = value;
    }

    public CNSMTGSolver() throws Exception {
        Term.setAnd(Term.AndType.and);
        Term.setOr(Term.OrType.max);
        this.rand = new Random();
        this.rResults = new LinkedList<>();
        //this.seedWithUtilOptimum = true;
        this.ft = new FormulaTransform();
        this.ip = new IntervalPropagator();
        instance = this;
        this.setMaxFEvals(100000000);
        this.maxSolveTime = 25; //25ms
        //RPropConvergenceStepSize = 1E-2;
        setRPropConvergenceStepSize(0);
        setUseIntervalProp(true);
        setOptimize(false);
    }




    public Double[] solve(Term equation, Variable[] args, Double[][] limits, MutableDouble util) throws
        Exception {
        return solve(equation, args, limits, null, util);

    }

    public Double[] solve(Term equation, Variable[] args, Double[][] limits, Double[][] seeds, MutableDouble util)
        throws Exception {
        lastSeed = null;
        probeCount = 0;
        successProbeCount = 0;
        intervalCount = 0;
        successIntervalCount = 0;
        fevalsCount = 0;
        runCount = 0;
        long begin = System.currentTimeMillis();
        //this.FEvals = 0;
        util.setValue(0);
        //ft.Reset();
        rResults.clear();
        currentArgs = args;
        this.dim = args.length;
        this.limits = limits;
        this.ranges = new double[dim];
        this.rpropStepWidth = new double[dim];
        this.rpropStepConvergenceThreshold = new double[dim];
        equation = equation.aggregateConstants();
        ConstraintUtility cu = (ConstraintUtility)equation;
        boolean utilIsConstant = (cu.getUtility() instanceof Constant);
        boolean constraintIsConstant = (cu.getConstraint() instanceof Constant);
        if (constraintIsConstant)
        {
            if (((Constant)cu.getConstraint()).getValue() < 0.25)
            {
                util.setValue(((Constant)cu.getConstraint()).getValue());
                Double[] ret = new Double[dim];
                for (int i = 0;i < dim;i++)
                {
                    ret[i] = (this.limits[i][1] + this.limits[i][0]) / 2.0;
                }
                return ret;
            }

        }

        //this.ranges[i]/2.0+this.limits[i,0];
        ss = new CNSat();
        ss.setUseIntervalProp(this.getUseIntervalProp());
        //Console.WriteLine(cu.Constraint);
        LinkedList<Clause> cnf = ft.transformToCNF(cu.getConstraint(), ss);
        /*
              int litc=1;
              List<Term> terms = new List<Term>(ft.Atoms);

              currentAtoms = new Dictionary<int, Term>();

              foreach(Term t in terms) {
                foreach(Literal l in ft.References[t]) {
                  l.Id = litc;
                }
                currentAtoms.Add(litc,t);
                litc++;
              }*/
        if (getUseIntervalProp())
        {
            ip.setGlobalRanges(args, limits, ss);
        }

        for (Clause c : cnf)
        {
            if (!c.getIsTautologic())
            {
                if (c.getLiterals().size() == 0)
                {
                    util.setValue(Double.MIN_VALUE);
                    Double[] ret = new Double[dim];
                    for (int i = 0;i < dim;i++)
                    {
                        ret[i] = (this.limits[i][1] + this.limits[i][0]) / 2.0;
                    }
                    return ret;
                }

                //Post Clause Here
                //Console.Write("\nAdding Clause with "+c.Literals.Count+" Literals\n");
                //c.Print();
                ss.addBasicClause(c);
            }

        }
        ss.setCNSMTGSolver(this);
        ss.init();
        //PRE-Propagation:
        if (getUseIntervalProp())
        {
            if (!ip.prePropagate(ss.getVariables()))
            {
                System.out.println("Unsatisfiable (unit propagation)");
                return null;
            }
        }

        //END-PrePropagation
        boolean solutionFound = false;
        ss.setUnitDecissions(ss.getDecisions().size());
        do
        {
            if (!solutionFound)
            {
                ss.emptySATClause();
                ss.emptyTClause();
                ss.backTrack(ss.getUnitDecissions());
            }

            solutionFound = ss.solve();
            if (getOptimize())
                r1 = rPropOptimizeFeasible(ss.getDecisions(), ((ConstraintUtility)equation).getUtility(), args,
                    r1.finalValue, false);

            if (!solutionFound && r1.finalUtil > 0)
                r1.finalUtil = -1;

            util.setValue(r1.finalUtil);
            if (!getOptimize() && solutionFound)
                return r1.finalValue;
            else if (getOptimize())
            {
                //optimization case
                rResults.add(r1);
                Clause c = new Clause();
                for (Var v : ss.getDecisions())
                {
                    c.add(new Lit(v,v.getAssignment() == Assignment.True ? Assignment.False : Assignment.True));
                }
                //ss.addBasicClause(c);
                ss.addIClause(c);
                ss.backTrack(ss.getDecisions().get(ss.getDecisions().size() - 1).getDecisionLevel());
                solutionFound = false;
            }

        }
        while (!solutionFound && this.begin + this.maxSolveTime > System.currentTimeMillis());
        /*&& this.runCount < this.MaxFEvals*/
        //Console.WriteLine("Probes: {0}/{1}\tIntervals: {2}/{3}",successProbeCount,probeCount,successIntervalCount,intervalCount);
//        ip.printStats();
        //Console.WriteLine("Rprop Runs: {0}\t FEvals {1}\t Solutions Found: {2}",this.runCount,this.fevalsCount, rResults.Count);
        //return best result
        if (rResults.size() > 0)
        {
            for (RpropResult rp : rResults)
            {
                if (rp.finalUtil > util.getValue())
                {
                    util.setValue(rp.finalUtil);
                    r1 = rp;
                }

            }
            return r1.finalValue;
        }

        System.out.println("Unsatisfiable");
        return null;
    }

    //if(util>this.utilitySignificanceThreshold) return r1.finalValue;
    public boolean intervalPropagate(List<Var> decisions, MutableObject<Double[][]> curRanges) throws Exception {
        intervalCount++;
        MutableObject<List<Var>> offending = new MutableObject<>();
        if (!ip.propagate(decisions, curRanges, offending))
        {
            if (offending != null)
            {
                Clause learnt = new Clause();
                for (Var v : offending.getValue())
                {
                    learnt.add(new Lit(v,v.getAssignment() == Assignment.True ? Assignment.False : Assignment.True));
                }
                //if(learnt.Literals.Count>0) {
                ss.addIClause(learnt);
            }

            return false;
        }

        //}
        //Console.WriteLine("Propagation succeeded");
        this.limits = curRanges.getValue();
        successIntervalCount++;
        return true;
    }
    public long begin;

    public boolean probeForSolution(List<Var> decisions, Double[] solution) throws Exception {
        probeCount++;
        solution = null;
        for (int i = 0;i < dim;i++)
        {
            this.ranges[i] = (this.limits[i][1] - this.limits[i][0]);
        }
        for (int i = 0;i < decisions.size();i++)
        {
            decisions.get(i).setCurTerm(decisions.get(i).getAssignment() == Assignment.True ? decisions.get(i)
            .getPositiveTerm() : decisions.get(i).getNegativeTerm());
        }
        r1 = rPropFindFeasible(decisions, lastSeed);
        if (r1.finalUtil < 0.5)
            r1 = rPropFindFeasible(decisions, null);

        if (r1.finalUtil < 0.5)
        {
            Clause learnt = new Clause();
            for (Var v : decisions)
            {
                learnt.add(new Lit(v,v.getAssignment() == Assignment.True ? Assignment.False : Assignment.True));
            }
            ss.addTClause(learnt);
            return false;
        }
        lastSeed = r1.finalValue;
        solution = r1.finalValue;
        successProbeCount++;
        return true;
    }

    protected RpropResult rPropFindFeasible(List<Var> constraints, Double[] seed) throws Exception {
        runCount++;
        initialStepSize();
        Double[] curGradient;
        RpropResult ret = new RpropResult();
        if (seed != null)
        {
            curGradient = initialPointFromSeed(constraints, ret, seed);
        }
        else
        {
            curGradient = initialPoint(constraints, ret);
        }
        double curUtil = ret.initialUtil;
        if (curUtil > 0.5)
        {
            return ret;
        }

        Double[] formerGradient = new Double[dim];
        Double[] curValue = new Double[dim];
        System.arraycopy(ret.initialValue, 0, curValue, 0, dim);
        formerGradient = curGradient;
        int itcounter = 0;
        int badcounter = 0;
        int maxIter = 60;
        int maxBad = 30;
        double minStep = 1E-11;
        while (itcounter++ < maxIter && badcounter < maxBad)
        {
            for (int i = 0;i < dim;i++)
            {
                if (curGradient[i] * formerGradient[i] > 0)
                    rpropStepWidth[i] *= 1.3;
                else if (curGradient[i] * formerGradient[i] < 0)
                    rpropStepWidth[i] *= 0.5;

                rpropStepWidth[i] = Math.max(minStep, rpropStepWidth[i]);
                if (curGradient[i] > 0)
                    curValue[i] += rpropStepWidth[i];
                else if (curGradient[i] < 0)
                    curValue[i] -= rpropStepWidth[i];

                if (curValue[i] > limits[i][1])
                    curValue[i] = limits[i][1];
                else if (curValue[i] < limits[i][0])
                    curValue[i] = limits[i][0];

            }
            this.fevalsCount++;
            formerGradient = curGradient;
            MutableObject<Double[]> tmp = new MutableObject<>(curGradient);
            MutableDouble utilRet = new MutableDouble(curUtil);
            differentiate(constraints, curValue, tmp, utilRet);
            curGradient = tmp.getValue();
            curUtil = utilRet.getValue();

            boolean allZero = true;
            for (int i = 0;i < dim;i++)
            {
                if (Double.isNaN(curGradient[i]))
                {
                    ret.aborted = true;
                    return ret;
                }

                allZero &= (curGradient[i] == 0);
            }
            if (curUtil > ret.finalUtil)
            {
                badcounter = 0;
                //Math.Max(0,badcounter-1);
                ret.finalUtil = curUtil;
                System.arraycopy(curValue,0, ret.finalValue,0 , dim);
                //ret.finalValue = curValue;
                if (curUtil > 0.75)
                    return ret;

            }
            else
            {
                badcounter++;
            }
            if (allZero)
            {
                ret.aborted = false;
                return ret;
            }

        }
        ret.aborted = false;
        return ret;
    }

    protected RpropResult rPropOptimizeFeasible(List<Var> constraints, Term ut, Variable[] args, Double[] seed,
        boolean precise) throws Exception {
        //Compiled Term zusammenbauen
        Term constr = Term.True;
        for (Var v : constraints)
        {
            if (v.getAssignment() == Assignment.True)
                constr = new And(constr, v.getTerm());
            else
                constr = new And(constr, v.getTerm().negate());
        }
        ConstraintUtility cu = new ConstraintUtility(constr, ut);
        ICompiledTerm term = TermUtils.compile(cu, args);
        Tuple<Double[], Double> tup = new Tuple<Double[], Double>();
        //fertig zusammengebaut
        runCount++;
        initialStepSize();
        Double[] curGradient;
        RpropResult ret = new RpropResult();
        //			curGradient = InitialPoint(constraints, ret);
        if (seed != null)
        {
            curGradient = initialPointFromSeed(constraints, ret, seed);
        }
        else
        {
            curGradient = initialPoint(constraints, ret);
        }
        double curUtil = ret.initialUtil;
        Double[] formerGradient = new Double[dim];
        double[] curValue = new double[dim];
        //Tuple<double[],double> tup;
        System.arraycopy(ret.initialValue,0, curValue,0 , dim);
        formerGradient = curGradient;
        int itcounter = 0;
        int badcounter = 0;
        int maxIter = 60;
        int maxBad = 30;
        double minStep = 1E-11;
        if (precise)
        {
            maxIter = 120;
            //110
            maxBad = 60;
            //60
            minStep = 1E-15;
        }

        //15
        int convergendDims = 0;
        while (itcounter++ < maxIter && badcounter < maxBad)
        {
            convergendDims = 0;
            for (int i = 0;i < dim;i++)
            {
                if (curGradient[i] * formerGradient[i] > 0)
                    rpropStepWidth[i] *= 1.3;
                else if (curGradient[i] * formerGradient[i] < 0)
                    rpropStepWidth[i] *= 0.5;

                rpropStepWidth[i] = Math.max(minStep, rpropStepWidth[i]);
                //rpropStepWidth[i] = Math.Max(0.000001,rpropStepWidth[i]);
                if (curGradient[i] > 0)
                    curValue[i] += rpropStepWidth[i];
                else if (curGradient[i] < 0)
                    curValue[i] -= rpropStepWidth[i];

                if (curValue[i] > limits[i][1])
                    curValue[i] = limits[i][1];
                else if (curValue[i] < limits[i][0])
                    curValue[i] = limits[i][0];

                //Console.Write("{0}\t",curValue[i]);
                if (rpropStepWidth[i] < rpropStepConvergenceThreshold[i])
                {
                    ++convergendDims;
                }

            }
            //Abort if all dimensions are converged
            if (!precise && convergendDims >= dim)
            {
                return ret;
            }

            this.fevalsCount++;
            formerGradient = curGradient;
            tup = term.differentiate(curValue);
            boolean allZero = true;
            for (int i = 0;i < dim;i++)
            {
                if (Double.isNaN(tup.getItem1()[i]))
                {
                    ret.aborted = false;
                    return ret;
                }

                //true; //HACK!
                allZero &= (tup.getItem1()[i] == 0);
            }
            curUtil = tup.getItem2();
            formerGradient = curGradient;
            curGradient = tup.getItem1();
            //Console.WriteLine("CurUtil: {0} Final {1}",curUtil,ret.finalUtil);
            if (curUtil > ret.finalUtil)
            {
                badcounter = 0;
                //Math.Max(0,badcounter-1);
                ret.finalUtil = curUtil;
                System.arraycopy(curValue,0, ret.finalValue,0 , dim);
                //ret.finalValue = curValue;
                if (curUtil > 0.75)
                    return ret;

            }
            else
            {
                badcounter++;
            }
            if (allZero)
            {
                ret.aborted = false;
                return ret;
            }

        }
        ret.aborted = false;
        return ret;
    }

    protected void differentiate(List<Var> constraints, Double[] val, MutableObject<Double[]> gradient,
        MutableDouble util)
        throws Exception {
        Tuple<Double[], Double> t1 = constraints.get(0).getCurTerm().differentiate(ArrayUtils.toPrimitive(val));
        gradient.setValue(t1.getItem1());
        util.setValue(t1.getItem2());
        for (int i = 1;i < constraints.size();i++)
        {
            Tuple<Double[], Double> tup = constraints.get(i).getCurTerm().differentiate(ArrayUtils.toPrimitive(val));
            if (tup.getItem2() <= 0)
            {
                if (util.getValue() > 0)
                    util.setValue(tup.getItem2());
                else
                    util.setValue(util.getValue() + tup.getItem2());
                for (int j = 0;j < dim;j++)
                {
                    gradient.getValue()[j] += tup.getItem1()[j];
                }
            }

        }
    }

    //return new Tuple<double[], double>(gradient,util);
    protected Double[] initialPointFromSeed(List<Var> constraints, RpropResult res, Double[] seed) throws Exception {
        Tuple<Double[], Double> tup = new Tuple<Double[], Double>();
        boolean found = true;
        res.initialValue = new Double[dim];
        res.finalValue = new Double[dim];
        Double[] gradient;
        do
        {
            gradient = new Double[dim];
            found = true;
            res.initialUtil = 1;
            for (int i = 0;i < dim;i++)
            {
                if (Double.isNaN(seed[i]))
                {
                    res.initialValue[i] = rand.nextDouble() * ranges[i] + limits[i][0];
                }
                else
                {
                    res.initialValue[i] = Math.min(Math.max(seed[i], limits[i][0]), limits[i][1]);
                }
            }
            //why this?
            this.fevalsCount++;
            for (int i = 0;i < constraints.size();i++)
            {
                if (constraints.get(i).getAssignment() == Assignment.True)
                {
                    if (constraints.get(i).getPositiveTerm() == null)
                        constraints.get(i).setPositiveTerm(TermUtils.compile(constraints.get(i).getTerm(), this
                            .currentArgs));

                    constraints.get(i).setCurTerm(constraints.get(i).getPositiveTerm());
                }
                else
                {
                    if (constraints.get(i).getNegativeTerm() == null)
                        constraints.get(i).setNegativeTerm(TermUtils.compile(constraints.get(i).getTerm().negate(), this.currentArgs));

                    constraints.get(i).setCurTerm(constraints.get(i).getNegativeTerm());
                }
                tup = constraints.get(i).getCurTerm().differentiate(ArrayUtils.toPrimitive(res.initialValue));
                for (int j = 0;j < dim;j++)
                {
                    if (Double.isNaN(tup.getItem1()[j]))
                    {
                        found = false;
                        break;
                    }

                    gradient[j] += tup.getItem1()[j];
                }
                if (!found)
                    break;

                if (tup.getItem2() <= 0.0)
                {
                    if (res.initialUtil > 0.0)
                        res.initialUtil = tup.getItem2();
                    else
                        res.initialUtil += tup.getItem2();
                }

            }
        }
        while (!found);
        //tup = term.Differentiate(res.initialValue);
        res.finalUtil = res.initialUtil;
        System.arraycopy(res.initialValue,0, res.finalValue,0 , dim);
        return gradient;
    }

    protected Double[] initialPoint(List<Var> constraints, RpropResult res) throws Exception {
        Tuple<Double[], Double> tup = new Tuple<Double[], Double>();
        boolean found = true;
        res.initialValue = new Double[dim];
        res.finalValue = new Double[dim];
        Double[] gradient;
        do
        {
            gradient = new Double[dim];
            found = true;
            res.initialUtil = 1;
            for (int i = 0;i < dim;i++)
            {
                gradient[i] = new Double(0);
                res.initialValue[i] = rand.nextDouble() * ranges[i] + limits[i][0];
            }
            this.fevalsCount++;
            for (int i = 0;i < constraints.size();i++)
            {
                if (constraints.get(i).getAssignment() == Assignment.True)
                {
                    if (constraints.get(i).getPositiveTerm() == null)
                        constraints.get(i).setPositiveTerm(TermUtils.compile(constraints.get(i).getTerm(), this
                            .currentArgs));

                    constraints.get(i).setCurTerm(constraints.get(i).getPositiveTerm());
                }
                else
                {
                    if (constraints.get(i).getNegativeTerm() == null)
                        constraints.get(i).setNegativeTerm(TermUtils.compile(constraints.get(i).getTerm().negate(), this
                            .currentArgs));

                    constraints.get(i).setCurTerm(constraints.get(i).getNegativeTerm());
                }
                tup = constraints.get(i).getCurTerm().differentiate(ArrayUtils.toPrimitive(res.initialValue));
                for (int j = 0;j < dim;j++)
                {
                    if (Double.isNaN(tup.getItem1()[j]))
                    {
                        found = false;
                        break;
                    }

                    gradient[j] += tup.getItem1()[j];
                }
                if (!found)
                    break;

                if (tup.getItem2() <= 0.0)
                {
                    if (res.initialUtil > 0.0)
                        res.initialUtil = tup.getItem2();
                    else
                        res.initialUtil += tup.getItem2();
                }

            }
        }
        while (!found);
        //tup = term.Differentiate(res.initialValue);
        res.finalUtil = res.initialUtil;
        System.arraycopy(res.initialValue,0, res.finalValue,0 , dim);
        return gradient;
    }

    protected void initialStepSize() throws Exception {
        for (int i = 0;i < this.dim;i++)
        {
            this.rpropStepWidth[i] = initialStepSize * ranges[i];
            this.rpropStepConvergenceThreshold[i] = rpropStepWidth[i] * this.getRPropConvergenceStepSize();
        }
    }

    public boolean currentCacheConsistent() throws Exception {
        if (this.lastSeed == null)
            return false;

        for (int i = dim - 1;i >= 0;--i)
        {
            if (this.lastSeed[i] < this.limits[i][0])
                return false;

            if (this.lastSeed[i] > this.limits[i][1])
                return false;

        }
        return true;
    }

}


