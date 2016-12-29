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
    private int Index;
    public void setIndex(int value) {
        Index = value;
    }

    public int getIndex() {
        return Index;
    }

    private int Activity;
    public void setActivity(int value) {
        Activity = value;
    }

    public int getActivity() {
        return Activity;
    }

    private int NegActivity;
    public void setNegActivity(int value) {
        NegActivity = value;
    }

    public int getNegActivity() {
        return NegActivity;
    }

    private boolean Locked;
    public void setLocked(boolean value) {
        Locked = value;
    }

    public boolean getLocked() {
        return Locked;
    }

    private Assignment Assignment;
    public void setAssignment(Assignment value) {
        Assignment = value;
    }

    public Assignment getAssignment() {
        return Assignment;
    }

    private boolean Seen;
    public void setSeen(boolean value) {
        Seen = value;
    }

    public boolean getSeen() {
        return Seen;
    }

    private boolean PreferedSign;
    public boolean getPreferedSign() {
        return PreferedSign;
    }

    public void setPreferedSign(boolean value) {
        PreferedSign = value;
    }

    public Clause getReason() throws Exception {
        return this.reason;
    }

    public void setReason(Clause value) throws Exception {
        if (value != null && value != this.reason)
        {
            for (Lit l : value.getLiterals())
            {
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

    private DecisionLevel DecisionLevel;
    public DecisionLevel getDecisionLevel() {
        return DecisionLevel;
    }

    public void setDecisionLevel(DecisionLevel value) {
        DecisionLevel = value;
    }

    private int PositiveAppearance;
    public int getPositiveAppearance() {
        return PositiveAppearance;
    }

    public void setPositiveAppearance(int value) {
        PositiveAppearance = value;
    }

    private int NegativeAppearance;
    public int getNegativeAppearance() {
        return NegativeAppearance;
    }

    public void setNegativeAppearance(int value) {
        NegativeAppearance = value;
    }

    private Term Term;
    public Term getTerm() {
        return Term;
    }

    public void setTerm(Term value) {
        Term = value;
    }

    private Double[][] PositiveRanges;
    public Double[][] getPositiveRanges() {
        return PositiveRanges;
    }

    public void setPositiveRanges(Double[][] value) {
        PositiveRanges = value;
    }

    private Double[][] NegativeRanges;
    public Double[][] getNegativeRanges() {
        return NegativeRanges;
    }

    public void setNegativeRanges(Double[][] value) {
        NegativeRanges = value;
    }

    private double PositiveRangeSize;
    public double getPositiveRangeSize() {
        return PositiveRangeSize;
    }

    public void setPositiveRangeSize(double value) {
        PositiveRangeSize = value;
    }

    private double NegativeRangeSize;
    public double getNegativeRangeSize() {
        return NegativeRangeSize;
    }

    public void setNegativeRangeSize(double value) {
        NegativeRangeSize = value;
    }

    private ICompiledTerm PositiveTerm;
    public ICompiledTerm getPositiveTerm() {
        return PositiveTerm;
    }

    public void setPositiveTerm(ICompiledTerm value) {
        PositiveTerm = value;
    }

    private ICompiledTerm NegativeTerm;
    public ICompiledTerm getNegativeTerm() {
        return NegativeTerm;
    }

    public void setNegativeTerm(ICompiledTerm value) {
        NegativeTerm = value;
    }

    private ICompiledTerm CurTerm;
    public ICompiledTerm getCurTerm() {
        return CurTerm;
    }

    public void setCurTerm(ICompiledTerm value) {
        CurTerm = value;
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
        catch (RuntimeException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            throw new RuntimeException(exception);
        }
    }

    public String toOutputString() {
        try
        {
            return (getAssignment() == getAssignment().False ? "-" : (getAssignment() == getAssignment().True ? "+" :
                "o")) + this.getTerm().toString();
        }
        catch (RuntimeException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            throw new RuntimeException(exception);
        }
    }

}


//	return string.Format("[Var: WatchList={0}, Index={1}, Activity={2}, NegActivity={3}, Locked={4}, Assignment={5}, Seen={6}, PreferedSign={7}, Reason={8}, DecisionLevel={9}, Term={10}, PositiveRanges={11}, NegativeRanges={12}]", WatchList, Index, Activity, NegActivity, Locked, Assignment, Seen, PreferedSign, Reason, DecisionLevel, Term, PositiveRanges, NegativeRanges);