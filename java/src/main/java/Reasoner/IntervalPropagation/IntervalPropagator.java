//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.IntervalPropagation;

import AutoDiff.Term;
import Reasoner.CNSAT.Assignment;
import Reasoner.CNSAT.CNSat;
import Reasoner.CNSAT.Var;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.mutable.MutableObject;

public class IntervalPropagator   
{
    // :ITermVisitor<bool>
    private ResetIntervals ri;
    private RecursivePropagate rp;
    private Double[][] globalRanges;
    private int dim;
    private AutoDiff.Variable[] vars;
    private CNSat solver;
    //private SetParents sp;
    public static int updates;
    public static int visits;
    public IntervalPropagator() throws Exception {
        this.ri = new ResetIntervals();
        this.rp = new RecursivePropagate();
    }

    //this.sp = new SetParents();
    public void printStats() throws Exception {
        System.out.println("IP Steps: "+updates+"/"+visits);
    }

    public void setGlobalRanges(AutoDiff.Variable[] vars, Double[][] ranges, CNSat solver) throws Exception {
        for (int i = 0;i < vars.length;i++)
        {
            vars[i].GlobalMin = ranges[i][0];
            vars[i].GlobalMax = ranges[i][1];
        }
        this.globalRanges = ranges;
        this.vars = vars;
        this.dim = vars.length;
        this.solver = solver;
        updates = 0;
        visits = 0;
    }

    public boolean propagate(List<Var> decisions, MutableObject<Double[][]> completeRanges, MutableObject<List<Var>>
        offenders)
        throws Exception {
        offenders.setValue(null);
        completeRanges.setValue(new Double[dim][2]);
        for (int i = 0; i < dim; i++) {
            System.arraycopy(globalRanges[i], 0, completeRanges.getValue()[i], 0, completeRanges.getValue()[i].length);
        }
//        System.arraycopy(globalRanges,0, completeRanges.getValue(),0 , 2 * dim);
        for (int i = decisions.size() - 1;i >= 0;i--)
        {
            Double[][] curRanges;
            if (decisions.get(i).getAssignment() == Assignment.True)
            {
                if (decisions.get(i).getPositiveRanges() == null)
                {
                    if (!propagateSingle(decisions.get(i), true))
                    {
                        offenders.setValue(new LinkedList<Var>());
                        offenders.getValue().add(decisions.get(i));
                        return false;
                    }
                     
                }
                 
                curRanges = decisions.get(i).getPositiveRanges();
            }
            else
            {
                if (decisions.get(i).getNegativeRanges() == null)
                {
                    if (!propagateSingle(decisions.get(i), false))
                    {
                        offenders.setValue(new LinkedList<Var>());
                        offenders.getValue().add(decisions.get(i));
                        return false;
                    }
                     
                }
                 
                curRanges = decisions.get(i).getNegativeRanges();
            } 
            for (int j = dim - 1;j >= 0;j--)
            {
                //Console.WriteLine("{0} B4: [{1}..{2}]",j,completeRanges[j,0],completeRanges[j,1]);
                completeRanges.getValue()[j][0] = Math.max(completeRanges.getValue()[j][0], curRanges[j][0]);
                completeRanges.getValue()[j][1] = Math.min(completeRanges.getValue()[j][1], curRanges[j][1]);
                //Console.WriteLine("{0} AF: [{1}..{2}]",j,completeRanges[j,0],completeRanges[j,1]);
                if (completeRanges.getValue()[j][0] > completeRanges.getValue()[j][1])
                {
                    //ranges collapsed, build offenders
                    offenders.setValue(new LinkedList<>());
                    for (int k = decisions.size() - 1;k >= i;k--)
                    {
                        offenders.getValue().add(decisions.get(k));
                    }
                    return false;
                }
                 
            }
        }
        return true;
    }

    public boolean prePropagate(List<Var> vars) throws Exception {
        for (int i = vars.size() - 1;i >= 0;--i)
        {
            if (vars.get(i).getAssignment() != Assignment.False && !propagateSingle(vars.get(i), true))
            {
                //Console.WriteLine(vars.get(i)+" true not doable");
                if (!solver.preAddIUnitClause(vars.get(i), Assignment.False))
                    return false;
                 
            }
             
            if (vars.get(i).getAssignment() != Assignment.True && !propagateSingle(vars.get(i), false))
            {
                //Console.WriteLine(vars.get(i)+" false not doable");
                if (!solver.preAddIUnitClause(vars.get(i), Assignment.True))
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
            if (!propagate(v.getTerm().negate()))
                return false;
             
        } 
        double rangeSize = 0;
        Double[][] range = new Double[dim][2];
        for (int i = dim - 1;i >= 0;--i)
        {
            range[i][0] = vars[i].Min;
            range[i][1] = vars[i].Max;
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
        term.accept(this.ri);
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