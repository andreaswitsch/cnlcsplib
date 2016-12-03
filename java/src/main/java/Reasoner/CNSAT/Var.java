//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.CNSAT;

import AutoDiff.ICompiledTerm;
import AutoDiff.Term;
import java.util.LinkedList;
import java.util.List;

public class Var   
{
    public Var(int index) throws Exception
    {
        this(index, true);
    }

    public Var(int index, boolean prefSign) throws Exception {
        this.setIndex(index);
        this.setAssignment(getAssignment().Unassigned);
        setLocked(false);
        setPreferedSign(prefSign);
        setActivity(0);
    }

    protected List<Watcher> watchList = new LinkedList<Watcher>();
    public List<Watcher> getWatchList() throws Exception {
        return watchList;
    }

    public void setWatchList(List<Watcher> value) throws Exception {
        watchList = value;
    }

    private Clause reason;
    private int __Index;
    public void setIndex(int value) {
        __Index = value;
    }

    public int getIndex() {
        return __Index;
    }

    private int __Activity;
    public void setActivity(int value) {
        __Activity = value;
    }

    public int getActivity() {
        return __Activity;
    }

    private int __NegActivity;
    public void setNegActivity(int value) {
        __NegActivity = value;
    }

    public int getNegActivity() {
        return __NegActivity;
    }

    private boolean __Locked;
    public void setLocked(boolean value) {
        __Locked = value;
    }

    public boolean getLocked() {
        return __Locked;
    }

    private Assignment __Assignment;
    public void setAssignment(Assignment value) {
        __Assignment = value;
    }

    public Assignment getAssignment() {
        return __Assignment;
    }

    private boolean __Seen;
    public void setSeen(boolean value) {
        __Seen = value;
    }

    public boolean getSeen() {
        return __Seen;
    }

    private boolean __PreferedSign;
    public boolean getPreferedSign() {
        return __PreferedSign;
    }

    public void setPreferedSign(boolean value) {
        __PreferedSign = value;
    }

    public Clause getReason() throws Exception {
        return this.reason;
    }

    public void setReason(Clause value) throws Exception {
        if (value != null && value != this.reason)
        {
            for (Object __dummyForeachVar0 : value.getLiterals())
            {
                Lit l = (Lit)__dummyForeachVar0;
                if (l.getVar().getAssignment() == getAssignment().Unassigned)
                {
                    System.out.println(this + "   " + l.getVar());
                    value.print();
                    throw new Exception("!!!!!");
                }
                 
            }
        }
         
        this.reason = value;
    }

    public void reset() throws Exception {
        if (getLocked())
            return ;
         
        this.setReason(null);
        this.setSeen(false);
        this.setAssignment(getAssignment().Unassigned);
        setActivity(0);
    }

    private DecisionLevel __DecisionLevel;
    public DecisionLevel getDecisionLevel() {
        return __DecisionLevel;
    }

    public void setDecisionLevel(DecisionLevel value) {
        __DecisionLevel = value;
    }

    private int __PositiveAppearance;
    public int getPositiveAppearance() {
        return __PositiveAppearance;
    }

    public void setPositiveAppearance(int value) {
        __PositiveAppearance = value;
    }

    private int __NegativeAppearance;
    public int getNegativeAppearance() {
        return __NegativeAppearance;
    }

    public void setNegativeAppearance(int value) {
        __NegativeAppearance = value;
    }

    private Term __Term;
    public Term getTerm() {
        return __Term;
    }

    public void setTerm(Term value) {
        __Term = value;
    }

    private Double[][] __PositiveRanges;
    public Double[][] getPositiveRanges() {
        return __PositiveRanges;
    }

    public void setPositiveRanges(Double[][] value) {
        __PositiveRanges = value;
    }

    private Double[][] __NegativeRanges;
    public Double[][] getNegativeRanges() {
        return __NegativeRanges;
    }

    public void setNegativeRanges(Double[][] value) {
        __NegativeRanges = value;
    }

    private double __PositiveRangeSize;
    public double getPositiveRangeSize() {
        return __PositiveRangeSize;
    }

    public void setPositiveRangeSize(double value) {
        __PositiveRangeSize = value;
    }

    private double __NegativeRangeSize;
    public double getNegativeRangeSize() {
        return __NegativeRangeSize;
    }

    public void setNegativeRangeSize(double value) {
        __NegativeRangeSize = value;
    }

    private ICompiledTerm __PositiveTerm;
    public ICompiledTerm getPositiveTerm() {
        return __PositiveTerm;
    }

    public void setPositiveTerm(ICompiledTerm value) {
        __PositiveTerm = value;
    }

    private ICompiledTerm __NegativeTerm;
    public ICompiledTerm getNegativeTerm() {
        return __NegativeTerm;
    }

    public void setNegativeTerm(ICompiledTerm value) {
        __NegativeTerm = value;
    }

    private ICompiledTerm __CurTerm;
    public ICompiledTerm getCurTerm() {
        return __CurTerm;
    }

    public void setCurTerm(ICompiledTerm value) {
        __CurTerm = value;
    }

    public void print() throws Exception {
        if (getAssignment() == getAssignment().False)
            System.out.println("-");
        else if (getAssignment() == getAssignment().Unassigned)
            System.out.println("o");
        else
            System.out.println("+");
        System.out.println(getIndex());
    }

    public String toString() {
        try
        {
            return (getAssignment() == getAssignment().False ? "-" : (getAssignment() == getAssignment().True ? "+" : "o")) + this.getIndex();
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

}


//	return string.Format("[Var: WatchList={0}, Index={1}, Activity={2}, NegActivity={3}, Locked={4}, Assignment={5}, Seen={6}, PreferedSign={7}, Reason={8}, DecisionLevel={9}, Term={10}, PositiveRanges={11}, NegativeRanges={12}]", WatchList, Index, Activity, NegActivity, Locked, Assignment, Seen, PreferedSign, Reason, DecisionLevel, Term, PositiveRanges, NegativeRanges);