//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.IntervalPropagation;

import Alica.Reasoner.CNSAT.CNSat;
import Alica.Reasoner.CNSAT.Var;
import Alica.Reasoner.IntervalPropagation.RecursivePropagate;
import Alica.Reasoner.IntervalPropagation.ResetIntervals;
import Alica.Reasoner.IntervalPropagation.UnsolveableException;
import CS2JNet.JavaSupport.language.RefSupport;

public class IntervalPropagator   
{
    // :ITermVisitor<bool>
    private ResetIntervals ri;
    private RecursivePropagate rp;
    private double[][] globalRanges = new double[][]();
    private int dim = new int();
    private AutoDiff.Variable[] vars = new AutoDiff.Variable[]();
    private CNSat solver;
    //private SetParents sp;
    public static int updates = new int();
    public static int visits = new int();
    public IntervalPropagator() throws Exception {
        this.ri = new ResetIntervals();
        this.rp = new RecursivePropagate();
    }

    //this.sp = new SetParents();
    public void printStats() throws Exception {
        Console.WriteLine("IP Steps: {0}/{1}", updates, visits);
    }

    public void setGlobalRanges(AutoDiff.Variable[] vars, double[][] ranges, CNSat solver) throws Exception {
        for (int i = 0;i < vars.Length;i++)
        {
            vars[i].GlobalMin = ranges[i, 0];
            vars[i].GlobalMax = ranges[i, 1];
        }
        this.globalRanges = ranges;
        this.vars = vars;
        this.dim = vars.Length;
        this.solver = solver;
        updates = 0;
        visits = 0;
    }

    public boolean propagate(List<Var> decisions, RefSupport<double[][]> completeRanges, RefSupport<List<Var>> offenders) throws Exception {
        offenders.setValue(null);
        completeRanges.setValue(new double[dim, 2]);
        Buffer.BlockCopy(globalRanges, 0, completeRanges.getValue(), 0, * 2 * dim);
        for (int i = decisions.Count - 1;i >= 0;i--)
        {
            double[][] curRanges = new double[][]();
            if (decisions[i].Assignment == Alica.Reasoner.CNSAT.Assignment.True)
            {
                if (decisions[i].PositiveRanges == null)
                {
                    if (!PropagateSingle(decisions[i], true))
                    {
                        offenders.setValue(new List<Var>());
                        offenders.getValue().Add(decisions[i]);
                        return false;
                    }
                     
                }
                 
                curRanges = decisions[i].PositiveRanges;
            }
            else
            {
                if (decisions[i].NegativeRanges == null)
                {
                    if (!PropagateSingle(decisions[i], false))
                    {
                        offenders.setValue(new List<Var>());
                        offenders.getValue().Add(decisions[i]);
                        return false;
                    }
                     
                }
                 
                curRanges = decisions[i].NegativeRanges;
            } 
            for (int j = dim - 1;j >= 0;j--)
            {
                //Console.WriteLine("{0} B4: [{1}..{2}]",j,completeRanges[j,0],completeRanges[j,1]);
                completeRanges.getValue()[j, 0] = Math.Max(completeRanges.getValue()[j, 0], curRanges[j, 0]);
                completeRanges.getValue()[j, 1] = Math.Min(completeRanges.getValue()[j, 1], curRanges[j, 1]);
                //Console.WriteLine("{0} AF: [{1}..{2}]",j,completeRanges[j,0],completeRanges[j,1]);
                if (completeRanges.getValue()[j, 0] > completeRanges.getValue()[j, 1])
                {
                    //ranges collapsed, build offenders
                    offenders.setValue(new List<Var>());
                    for (int k = decisions.Count - 1;k >= i;k--)
                    {
                        offenders.getValue().Add(decisions[k]);
                    }
                    return false;
                }
                 
            }
        }
        return true;
    }

    public boolean prePropagate(List<Var> vars) throws Exception {
        for (int i = vars.Count - 1;i >= 0;--i)
        {
            if (vars[i].Assignment != Alica.Reasoner.CNSAT.Assignment.False && !PropagateSingle(vars[i], true))
            {
                //Console.WriteLine(vars[i]+" true not doable");
                if (!solver.PreAddIUnitClause(vars[i], Alica.Reasoner.CNSAT.Assignment.False))
                    return false;
                 
            }
             
            if (vars[i].Assignment != Alica.Reasoner.CNSAT.Assignment.True && !PropagateSingle(vars[i], false))
            {
                //Console.WriteLine(vars[i]+" false not doable");
                if (!solver.PreAddIUnitClause(vars[i], Alica.Reasoner.CNSAT.Assignment.True))
                    return false;
                 
            }
             
        }
        return true;
    }

    private boolean propagateSingle(Var v, boolean sign) throws Exception {
        for (int i = dim - 1;i >= 0;i--)
        {
            vars[i].Max = vars[i].GlobalMax;
            vars[i].Min = vars[i].GlobalMin;
        }
        if (sign)
        {
            if (!propagate(v.getTerm()))
                return false;
             
        }
        else
        {
            if (!Propagate(v.getTerm().Negate()))
                return false;
             
        } 
        double rangeSize = 0;
        double[][] range = new double[dim, 2];
        for (int i = dim - 1;i >= 0;--i)
        {
            range[i, 0] = vars[i].Min;
            range[i, 1] = vars[i].Max;
            double d = vars[i].Max - vars[i].Min;
            rangeSize += d * d;
        }
        if (sign)
        {
            v.setPositiveRanges(range);
            v.setPositiveRangeSize(rangeSize);
        }
        else
        {
            v.setNegativeRanges(range);
            v.setNegativeRangeSize(rangeSize);
        } 
        return true;
    }

    public boolean propagate(Term term) throws Exception {
        term.Accept(this.ri);
        //term.Accept(this.sp);
        term.Min = 1;
        term.Max = 1;
        updates = 0;
        try
        {
            this.rp.propagate(term);
        }
        catch (UnsolveableException __dummyCatchVar0)
        {
            return false;
        }

        return true;
    }

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