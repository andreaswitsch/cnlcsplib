//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.CNSAT;

import java.util.LinkedList;
import java.util.List;

public class Clause implements Comparable<Clause>
{
    protected List<Lit> literals = new LinkedList<>();
    public void addChecked(Lit l) throws Exception {
        boolean found = false;
        if (l.getIsTemporary())
        {
            for (int i = 0;i < this.getLiterals().size();i++)
            {
                if (this.getLiterals().get(i).getIsTemporary() && l.getAtom() == this.getLiterals().get(i).getAtom())
                {
                    found = true;
                    break;
                }
                 
            }
            if (!found)
            {
                getLiterals().add(l);
            }
             
            return ;
        }
         
        for (int i = 0;i < this.getLiterals().size();i++)
        {
            //else { 	}
            if (l.getAtom() == this.getLiterals().get(i).getAtom())
            {
                found = true;
                if (l.getSign() != this.getLiterals().get(i).getSign())
                {
                    this.setIsTautologic(true);
                    this.getLiterals().clear();
                }
                 
                break;
            }
             
        }
        //else {}
        if (!found)
        {
            getLiterals().add(l);
        }
         
        return;
    }

    private boolean __IsTautologic;
    public boolean getIsTautologic() {
        return __IsTautologic;
    }

    public void setIsTautologic(boolean value) {
        __IsTautologic = value;
    }

    private boolean __IsFinished;
    public boolean getIsFinished() {
        return __IsFinished;
    }

    public void setIsFinished(boolean value) {
        __IsFinished = value;
    }

    public Clause clone() {
        try
        {
            Clause clone = new Clause();
            clone.getLiterals().addAll(this.literals);
            clone.setIsFinished(this.getIsFinished());
            clone.setIsTautologic(this.getIsTautologic());
            return clone;
        }
        catch (RuntimeException __dummyCatchVar0)
        {
            throw __dummyCatchVar0;
        }
        catch (Exception __dummyCatchVar0)
        {
            throw new RuntimeException(__dummyCatchVar0);
        }
    
    }

    public Clause() throws Exception {
        this.setActivity(0);
        this.setSatisfied(false);
        this.setIsFinished(false);
        this.setIsTautologic(false);
    }

    public void add(Lit l) throws Exception {
        literals.add(l);
    }

    public int avgActivity() throws Exception {
        int ret = 0;
        for (Object __dummyForeachVar0 : literals)
        {
            Lit l = (Lit)__dummyForeachVar0;
            ret += l.getVar().getActivity();
        }
        return ret / literals.size();
    }

    public int compareTo(Clause other) {
        int a = this.getActivity();
        int b = other.getActivity();
        if (a < b)
            return 1;
        else if (a > b)
            return -1;
        else
            return 0;  
    }

    public boolean checkSatisfied() throws Exception {
        for (Object __dummyForeachVar1 : literals)
        {
            Lit l = (Lit)__dummyForeachVar1;
            if (l.getVar().getAssignment() == l.getSign())
            {
                return true;
            }
             
        }
        return false;
    }


    public List<Lit> getLiterals() throws Exception {
        return literals;
    }

    public void setLiterals(List<Lit> value) throws Exception {
        literals = value;
    }

    private boolean __Satisfied;
    public void setSatisfied(boolean value) {
        __Satisfied = value;
    }

    public boolean getSatisfied() {
        return __Satisfied;
    }

    public Watcher[] watcher = new Watcher[2];
    private Var __LastModVar;
    public void setLastModVar(Var value) {
        __LastModVar = value;
    }

    public Var getLastModVar() {
        return __LastModVar;
    }

    private int __Activity;
    public void setActivity(int value) {
        __Activity = value;
    }

    public int getActivity() {
        return __Activity;
    }

    public void print() throws Exception {
        for (Object __dummyForeachVar2 : getLiterals())
        {
            Lit l = (Lit)__dummyForeachVar2;
            if (l.getSign() == Assignment.False)
                System.out.print("-");
             
            if (l.getSign() == Assignment.True)
                System.out.print("+");
             
            if (l.getSign() == Assignment.Unassigned)
                System.out.print("#");

            System.out.print(l.getVar().getIndex());
            if ((watcher[0] != null && watcher[0].getLit() == l) || (watcher[1] != null && watcher[1].getLit() == l))
                System.out.print("w");

            System.out.print(" ");
        }
        System.out.println();
    }

}


