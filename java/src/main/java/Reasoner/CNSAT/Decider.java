//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.CNSAT;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Decider   
{
    public Decider() throws Exception {
    }

    public static Var decideRangeBased(List<Var> variables, CNSat solver) throws Exception {
        List<Lit> choices = new LinkedList<>();
        int vars = variables.size();
        for (int i = 0;i < vars;i++)
        {
            if (variables.get(i).getAssignment() != Assignment.Unassigned)
                continue;
             
            boolean hT = false;
            boolean hF = false;
            for (int j = 0;j < variables.get(i).getWatchList().size();j++)
            {
                if (!variables.get(i).getWatchList().get(j).getClause().getSatisfied())
                {
                    if (variables.get(i).getWatchList().get(j).getLit().getSign() == Assignment.True)
                    {
                        if (!hT)
                            choices.add(variables.get(i).getWatchList().get(j).getLit());
                         
                        hT = true;
                    }
                    else
                    {
                        if (!hF)
                            choices.add(variables.get(i).getWatchList().get(j).getLit());
                         
                        hF = true;
                    } 
                }
                 
                if (hT && hF)
                    break;
                 
            }
        }
        if (choices.size() == 0)
            return null;
         
        choices.sort(new Comparator<Lit>()
        {
            @Override
            public int compare(final Lit a, final Lit b)
            {
                double sa;
                double sb;
                if (a.getSign() == Assignment.True)
                    sa = a.getVar().getPositiveRangeSize();
                else
                    sa = a.getVar().getNegativeRangeSize();
                if (b.getSign() == Assignment.True)
                    sb = b.getVar().getPositiveRangeSize();
                else
                    sb = b.getVar().getNegativeRangeSize();
                if (sa < sb)
                    return -1;

                if (sa > sb)
                    return 1;

                return 0;
            }
        });

        Var v;
        Assignment ass = Assignment.False;
        //Uniform:
        //int idx = solver.Rand.Next(choices.Count);
        //smallest range:
        //int idx = 0;
        //largest range:
        int idx = choices.size() - 1;

        v = choices.get(idx).getVar();
        ass = choices.get(idx).getSign();
        DecisionLevel d = new DecisionLevel(solver.getDecisions().size());
        solver.getDecisionLevel().add(d);
        v.setAssignment(ass);
        solver.getDecisions().add(v);
        v.setReason(null);
        v.setDecisionLevel(d);
        return v;
    }

    public static Var decideActivityBased(List<Var> variables, CNSat solver) throws Exception {
        int vars = variables.size();
        Random r = solver.getRand();
        int init = r.nextInt(vars);
        Var v = null, next = null;
        int maxActivity = 0;
        //Search Lit with highest Activity
        if (solver.getCNSMTGSolver() == null)
        {
            for (int i = 0;i < vars;i++)
            {
                int p = (init + i) % vars;
                if (p < 0 || p >= vars)
                    System.out.println("p = " + p);
                 
                v = variables.get(p);
                if (v.getAssignment() == Assignment.Unassigned)
                {
                    if (maxActivity <= v.getActivity())
                    {
                        maxActivity = v.getActivity();
                        next = v;
                    }
                     
                }
                 
            }
            //Decide it
            if (next != null)
            {
                DecisionLevel d = new DecisionLevel(solver.getDecisions().size());
                solver.getDecisionLevel().add(d);
                double rel = next.getPositiveAppearance() + next.getNegativeAppearance();
                if (rel != 0)
                    rel = ((double)next.getPositiveAppearance()) / rel;
                else
                    rel = 0.5; 
                next.setAssignment((r.nextDouble() < rel) ? Assignment.True : Assignment.False);
                solver.getDecisions().add(next);
                next.setReason(null);
                next.setDecisionLevel(d);
                return next;
            }
             
        }
        else
        {
            init = r.nextInt(solver.getClauses().size());
            for (int i = 0;i < solver.getClauses().size();i++)
            {
                Clause c = solver.getClauses().get((i + init) % solver.getClauses().size());
                if (!c.getSatisfied() && c.getLiterals().size() > 1)
                {
                    if (!c.watcher[0].getLit().satisfied())
                    {
                        next = c.watcher[0].getLit().getVar();
                        DecisionLevel d = new DecisionLevel(solver.getDecisions().size());
                        solver.getDecisionLevel().add(d);
                        next.setAssignment(c.watcher[0].getLit().getSign());
                        solver.getDecisions().add(next);
                        next.setReason(null);
                        next.setDecisionLevel(d);
                        return next;
                    }
                    else if (!c.watcher[1].getLit().satisfied())
                    {
                        DecisionLevel d = new DecisionLevel(solver.getDecisions().size());
                        solver.getDecisionLevel().add(d);
                        next = c.watcher[1].getLit().getVar();
                        next.setAssignment(c.watcher[1].getLit().getSign());
                        solver.getDecisions().add(next);
                        next.setReason(null);
                        next.setDecisionLevel(d);
                        return next;
                    }
                    else
                        System.out.println("This shoud Never Happen!!");
                }
                 
            }
        } 
        return null;
    }

    public static Var decideVariableCountBased(List<Var> variables, CNSat solver) throws Exception {
        int vars = variables.size();
        Lit l = null;
        int maxCount = Integer.MAX_VALUE;
        int minCount = -1;
        for (int i = 0;i < vars;i++)
        {
            if (variables.get(i).getAssignment() != Assignment.Unassigned)
                continue;
             
            for (int j = 0;j < variables.get(i).getWatchList().size();j++)
            {
                if (!variables.get(i).getWatchList().get(j).getClause().getSatisfied())
                {
                    /*if(maxCount>variables.get(i).WatchList.get(j).Lit.VariableCount) {
                    							l = variables.get(i).WatchList.get(j).Lit;
                    							maxCount = l.VariableCount;
                    						}*/
                    if (minCount < variables.get(i).getWatchList().get(j).getLit().getVariableCount())
                    {
                        l = variables.get(i).getWatchList().get(j).getLit();
                        minCount = l.getVariableCount();
                    }
                     
                }
                 
            }
        }
        if (l == null)
            return null;
         
        Assignment ass = Assignment.False;
        Var v = l.getVar();
        ass = l.getSign();
        DecisionLevel d = new DecisionLevel(solver.getDecisions().size());
        solver.getDecisionLevel().add(d);
        v.setAssignment(ass);
        solver.getDecisions().add(v);
        v.setReason(null);
        v.setDecisionLevel(d);
        return v;
    }

}


