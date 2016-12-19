package Reasoner;

import AutoDiff.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableDouble;

public class GSolver
{


    private long runs;
    public long getRuns() {
        return runs;
    }

    public void setRuns(long value) {
        runs = value;
    }

    private long fEvals;
    public long getFEvals() {
        return fEvals;
    }

    public void setFEvals(long value) {
        fEvals = value;
    }

    private long maxFEvals;
    public long getMaxFEvals() {
        return maxFEvals;
    }

    public void setMaxFEvals(long value) {
        maxFEvals = value;
    }

    double initialStepSize = 0.005;
    private double rPropConvergenceStepSize;
    public double getRPropConvergenceStepSize() {
        return rPropConvergenceStepSize;
    }

    public void setRPropConvergenceStepSize(double value) {
        rPropConvergenceStepSize = value;
    }

    public double utilitySignificanceThreshold = 1E-22;
    Random rand = new Random();
    int dim;
    Double[][] limits;
    Double[] ranges;
    Double[] rpropStepWidth;
    Double[] rpropStepConvergenceThreshold;
    double utilityThreshold;
    long maxSolveTime;
    
    AutoDiff.ICompiledTerm term;
    List<RpropResult> rResults = new ArrayList<RpropResult>();
    protected static int fcounter = 0;
    //Configuration:
    protected boolean seedWithUtilOptimum;
    public GSolver() throws Exception {
        AutoDiff.Term.setAnd(AutoDiff.Term.AndType.and);
        AutoDiff.Term.setOr(AutoDiff.Term.OrType.max);
        this.rand = new Random();
        this.rResults = new ArrayList<RpropResult>();
        this.seedWithUtilOptimum = true;
        this.setMaxFEvals(100000000);
        this.maxSolveTime = 25; //25ms
        setRPropConvergenceStepSize(1E-2);
    }



    public Double[] solve(AutoDiff.Term equation, AutoDiff.Variable[] args, Double[][] limits, MutableDouble util)
        throws Exception {
        return solve(equation, args, limits, null, Double.MAX_VALUE, util);
    }

    public Double[] solve(AutoDiff.Term equation, AutoDiff.Variable[] args, Double[][] limits, Double[][] seeds,
        double sufficientUtility, MutableDouble util) throws Exception {
        this.setFEvals(0);
        this.setRuns(0);
        util.setValue(0.0);
        this.utilityThreshold = sufficientUtility;
        rResults.clear();
        long begin = System.currentTimeMillis();
        this.dim = args.length;
        this.limits = limits;
        this.ranges = new Double[dim];
        for (int i = 0;i < dim;i++)
        {
            this.ranges[i] = (this.limits[i][1] - this.limits[i][0]);
        }
        equation = equation.aggregateConstants();
        term = AutoDiff.TermUtils.compile(equation, args);
        AutoDiff.ConstraintUtility cu = (AutoDiff.ConstraintUtility)equation;
        boolean utilIsConstant = (cu.getUtility() instanceof AutoDiff.Constant);
        if (utilIsConstant)
            this.utilityThreshold = 0.75;
         
        boolean constraintIsConstant = (cu.getConstraint() instanceof AutoDiff.Constant);
        if (constraintIsConstant)
        {
            if (((AutoDiff.Constant)cu.getConstraint()).getValue() < 0.25)
            {
                util.setValue(((AutoDiff.Constant)cu.getConstraint()).getValue());
                Double[] ret = new Double[dim];
                for (int i = 0;i < dim;i++)
                {
                    ret[i] = this.ranges[i] / 2.0 + this.limits[i][0];
                }
                return ret;
            }
             
        }
         
        //Optimize given seeds
        this.rpropStepWidth = new Double[dim];
        this.rpropStepConvergenceThreshold = new Double[dim];
        if (seeds != null)
        {
            this.setRuns(this.getRuns() + 1);
            //Run with prefered cached seed
            RpropResult rpfirst = rPropLoop(seeds[0], true);
            if (rpfirst.finalUtil > this.utilityThreshold)
            {
                util.setValue(rpfirst.finalUtil);
                return rpfirst.finalValue;
            }
             
            rResults.add(rpfirst);
            for (int i = 1;i < seeds.length;i++)
            {
                //run with seeds of all other agends
                if (begin + this.maxSolveTime < System.currentTimeMillis() || this.getFEvals() > this.getMaxFEvals())
                {
                    break;
                }
                 
                //do not check any further seeds
                this.setRuns(this.getRuns() + 1);
                RpropResult rp = rPropLoop(seeds[i], false);
                if (rp.finalUtil > this.utilityThreshold)
                {
                    util.setValue(rp.finalUtil);
                    return rp.finalValue;
                }
                 
                rResults.add(rp);
            }
        }
         
        //Here: Ignore all constraints search optimum
        if (begin + this.maxSolveTime > System.currentTimeMillis() && this.getFEvals() < this.getMaxFEvals())
        {
            //if time allows, do an unconstrained run
            if (!constraintIsConstant && !utilIsConstant && seedWithUtilOptimum)
            {
                AutoDiff.ICompiledTerm curProb = term;
                term = AutoDiff.TermUtils.compile(((AutoDiff.ConstraintUtility)equation).getUtility(), args);
                this.setRuns(this.getRuns() + 1);
                Double[] utilitySeed = rPropLoop(null).finalValue;
                term = curProb;
                //Take result and search with constraints
                RpropResult ru = rPropLoop(utilitySeed, false);
                rResults.add(ru);
            }
             
        }
         
        do
        {
            //Do runs until termination criteria, running out of time, or too many function evaluations
            this.setRuns(this.getRuns() + 1);
            RpropResult rp = rPropLoop(null,false);
            if (rp.finalUtil > this.utilityThreshold)
            {
                util.setValue(rp.finalUtil);
                return rp.finalValue;
            }
             
            rResults.add(rp);
        }
        while (begin + this.maxSolveTime > System.currentTimeMillis() && this.getFEvals() < this.getMaxFEvals());
        //return best result
        int resIdx = 0;
        RpropResult res = rResults.get(0);
        for (int i = 1;i < rResults.size();i++)
        {
            if (Double.NaN == res.finalUtil || rResults.get(i).finalUtil > res.finalUtil)
            {
                if (resIdx == 0 && seeds != null && !(Double.NaN == res.finalUtil))
                {
                    if (rResults.get(i).finalUtil - res.finalUtil > utilitySignificanceThreshold && rResults.get(i)
                    .finalUtil > 0.75)
                    {
                        res = rResults.get(i);
                        resIdx = i;
                    }
                     
                }
                else
                {
                    res = rResults.get(i);
                    resIdx = i;
                } 
            }
             
        }

        util.setValue(res.finalUtil);
        return res.finalValue;
    }



    protected Double[] initialPointFromSeed(RpropResult res, Double[] seed) throws Exception {
        Tuple<Double[], Double> tup = new Tuple<Double[], Double>();
        res.initialValue = new Double[dim];
        res.finalValue = new Double[dim];
        for (int i = 0;i < dim;i++)
        {
            if (Double.NaN==seed[i])
            {
                res.initialValue[i] = rand.nextDouble() * ranges[i] + limits[i][0];
            }
            else
            {
                res.initialValue[i] = Math.min(Math.max(seed[i], limits[i][0]), limits[i][1]);
            } 
        }
        //TODO Double double conversion is expensive here! and maybe somewhere else too!
        tup = term.differentiate(ArrayUtils.toPrimitive(res.initialValue));
        res.initialUtil = tup.getItem2();
        res.finalUtil = tup.getItem2();

        System.arraycopy(res.initialValue,0, res.finalValue,0 , dim);
        return tup.getItem1();
    }

    protected Double[] initialPoint(RpropResult res) throws Exception {
        Tuple<Double[], Double> tup = new Tuple<Double[], Double>();
        boolean found = true;
        res.initialValue = new Double[dim];
        res.finalValue = new Double[dim];
        do
        {
            for (int i = 0;i < dim;i++)
            {
                res.initialValue[i] = rand.nextDouble() * ranges[i] + limits[i][0];
            }
            this.setFEvals(this.getFEvals() + 1);
            //TODO this is again expensive
            tup = term.differentiate(ArrayUtils.toPrimitive(res.initialValue));
            for (int i = 0;i < dim;i++)
            {
                if (Double.NaN ==  tup.getItem1()[i])
                {
                    found = false;
                    break;
                }
                else
                    found = true; 
            }
        }
        while (!found);
        res.initialUtil = tup.getItem2();
        res.finalUtil = tup.getItem2();
        System.arraycopy(res.initialValue,0, res.finalValue,0 , dim);
        return tup.getItem1();
    }

    protected RpropResult rPropLoop(Double[] seed) throws Exception {
        return rPropLoop(seed, false);
    }

    protected RpropResult rPropLoop(Double[] seed, boolean precise) throws Exception {
        initialStepSize();
        Double[] curGradient;
        RpropResult ret = new RpropResult();
        if (seed != null)
        {
            curGradient = initialPointFromSeed(ret, seed);
        }
        else
        {
            curGradient = initialPoint(ret);
        } 
        double curUtil = ret.initialUtil;
        Double[] formerGradient = new Double[dim];
        Double[] curValue = new Double[dim];
        Tuple<Double[], Double> tup = new Tuple<Double[], Double>();
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
            maxBad = 60;
            minStep = 1E-15;
        }
         
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
                if (curGradient[i] > 0)
                    curValue[i] += rpropStepWidth[i];
                else if (curGradient[i] < 0)
                    curValue[i] -= rpropStepWidth[i];
                  
                if (curValue[i] > limits[i][1])
                    curValue[i] = limits[i][1];
                else if (curValue[i] < limits[i][0])
                    curValue[i] = limits[i][0];
                  
                if (rpropStepWidth[i] < rpropStepConvergenceThreshold[i])
                {
                    ++convergendDims;
                }
                 
            }
            //Abort if all dimensions are converged
            if (!precise && convergendDims >= dim)
            {
                if (curUtil > ret.finalUtil)
                {
                    ret.finalUtil = curUtil;
                    System.arraycopy(curValue,0, ret.finalValue,0 , dim);
                }
                 
                return ret;
            }
             
            this.setFEvals(this.getFEvals() + 1);
            //TODO this is again expensive
            tup = term.differentiate(ArrayUtils.toPrimitive(curValue));
            boolean allZero = true;
            for (int i = 0;i < dim;i++)
            {
                if (Double.NaN == tup.getItem1()[i])
                {
                    ret.aborted = true;
                    return ret;
                }
                 
                allZero &= (tup.getItem1()[i] == 0);
            }

            curUtil = tup.getItem2();
            formerGradient = curGradient;
            curGradient = tup.getItem1();

            if (curUtil > ret.finalUtil)
            {
                badcounter = 0;

                ret.finalUtil = curUtil;
                System.arraycopy(curValue,0, ret.finalValue,0 , dim);
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

    protected void initialStepSize() throws Exception {
        for (int i = 0;i < this.dim;i++)
        {
            //double range = this.limits[i,1]-this.limits[i,0];
            this.rpropStepWidth[i] = initialStepSize * ranges[i];
            this.rpropStepConvergenceThreshold[i] = rpropStepWidth[i] * this.getRPropConvergenceStepSize();
        }
    }
}
