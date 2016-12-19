//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.CNSAT;

import Reasoner.CNSMTGSolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.mutable.MutableObject;

//#define CNSatDebug
public class CNSat
{
  protected List<Clause> clauses = new ArrayList<Clause>();

  public List<Clause> getClauses() throws Exception
  {
    return clauses;
  }

  public void setClauses(List<Clause> value) throws Exception
  {
    clauses = value;
  }

  protected List<Clause> satClauses = new ArrayList<Clause>();

  public List<Clause> getSATClauses() throws Exception
  {
    return satClauses;
  }

  public void setSATClauses(List<Clause> value) throws Exception
  {
    satClauses = value;
  }

  protected List<Clause> tClauses = new ArrayList<Clause>();

  public List<Clause> getTClauses() throws Exception
  {
    return tClauses;
  }

  public void setTClauses(List<Clause> value) throws Exception
  {
    tClauses = value;
  }

  protected List<Clause> iClauses = new ArrayList<Clause>();

  public List<Clause> getIClauses() throws Exception
  {
    return iClauses;
  }

  public void setIClauses(List<Clause> value) throws Exception
  {
    iClauses = value;
  }

  protected List<Var> variables = new ArrayList<Var>();

  public List<Var> getVariables() throws Exception
  {
    return variables;
  }

  public void setVariables(List<Var> value) throws Exception
  {
    variables = value;
  }

  protected List<Var> decisions = new ArrayList<Var>();

  public List<Var> getDecisions() throws Exception
  {
    return decisions;
  }

  public void setDecisions(List<Var> value) throws Exception
  {
    decisions = value;
  }

  protected List<DecisionLevel> decisionLevel = new ArrayList<DecisionLevel>();

  public List<DecisionLevel> getDecisionLevel() throws Exception
  {
    return decisionLevel;
  }

  public void setDecisionLevel(List<DecisionLevel> value) throws Exception
  {
    decisionLevel = value;
  }

  private boolean __UseIntervalProp;

  public boolean getUseIntervalProp()
  {
    return __UseIntervalProp;
  }

  public void setUseIntervalProp(boolean value)
  {
    __UseIntervalProp = value;
  }

  public Random getRand() throws Exception
  {
    return this.r;
  }

  private int conflictCount = 0;

  private int decisionCount = 0;

  private int learnedCount = 0;

  private int learntNum = 0;

  private int restartCount = 0;

  private DecisionLevel decisionLevelNull;

  public Random r = new Random();

  private boolean recentBacktrack = false;

  private int unitDecissions = 0;

  public int getUnitDecissions()
  {
    return unitDecissions;
  }

  public void setUnitDecissions(int value)
  {
    unitDecissions = value;
  }

  private CNSMTGSolver cNSMTGSolver;

  public CNSMTGSolver getCNSMTGSolver()
  {
    return cNSMTGSolver;
  }

  public void setCNSMTGSolver(CNSMTGSolver value)
  {
    cNSMTGSolver = value;
  }

  public CNSat() throws Exception
  {
    setUseIntervalProp(true);
    this.setCNSMTGSolver(null);
    decisionLevelNull = new DecisionLevel(0);
    setUnitDecissions(0);
    r = new Random();
  }

  public Var newVar() throws Exception
  {
    Var T = new Var(variables.size());
    variables.add(T);
    return T;
  }

  public boolean addBasicClause(Clause c) throws Exception
  {
    if (c.getLiterals().size() == 0)
      return false;

    if (c.getLiterals().size() > 1) {
      Watcher w1 = new Watcher(c.getLiterals().get(0), c);
      Watcher w2 = new Watcher(c.getLiterals().get(1), c);
      c.watcher[0] = w1;
      c.watcher[1] = w2;
      clauses.add(c);
    } else {
            /*foreach(Lit l in c.Literals) {
                      if(l.Sign == Assignment.True) l.Var.Activity++;
            					else l.Var.NegActivity++;
            				}*/
      if (c.getLiterals().get(0).getVar().getAssignment() != Assignment.Unassigned && c.getLiterals().get(0)
          .getVar().getAssignment() != c.getLiterals().get(0).getSign())
        return false;

      c.getLiterals().get(0).getVar().setAssignment(c.getLiterals().get(0).getSign());
      c.getLiterals().get(0).getVar().setDecisionLevel(this.decisionLevelNull);
      c.getLiterals().get(0).getVar().setReason(null);
      //TODO check this!!!
      decisions.add(c.getLiterals().get(0).getVar());
      c.getLiterals().get(0).getVar().setLocked(true);
      clauses.add(c);
    }
    return true;
  }

  public void emptySATClause() throws Exception
  {
    List<Clause> list = this.getSATClauses();
    emptyClauseList(list);
  }

  public void emptyTClause() throws Exception
  {
    List<Clause> list = this.getTClauses();
    emptyClauseList(list);
  }

  public void resetVariables() throws Exception
  {
    for (Var v : variables) {
      v.reset();
    }
  }

  protected void emptyClauseList(List<Clause> list) throws Exception
  {
    for (Clause c : list) {
      c.watcher[0].getLit().getVar().getWatchList().remove(c.watcher[0]);
      c.watcher[0].getLit().setVariableCount(c.watcher[0].getLit().getVariableCount() - 1);
      c.watcher[1].getLit().getVar().getWatchList().remove(c.watcher[1]);
      c.watcher[1].getLit().setVariableCount(c.watcher[1].getLit().getVariableCount() - 1);
    }
    list.clear();
  }


  public boolean addSATClause(Clause c) throws Exception
  {
    if (c.getLiterals().size() == 1) {
      //TODO Can be removed when Locked is removed
      if (c.getLiterals().get(0).getVar().getLocked() && c.getLiterals().get(0).getSign() != c.getLiterals()
          .get(0).getVar().getAssignment()) {
        return false;
      }

      /**
       * ////////
       * ////////
       * //////// AAAAAAAAAAAAAAAAm Ende von DL 0 muss das sein! oO
       * ////////
       * ////////
       */
            /*if(decisionLevelNull.Level-1 > decisions.Count) decisions.Add(c.Literals[0].Var);
            				else
            					decisions.Insert(decisionLevelNull.Level-1, c.Literals[0].Var);				*/
            /*if(DecisionLevel.Count>1)decisions.Insert(DecisionLevel[1].Level-1, c.Literals[0].Var);
            				else
            					decisions.Add(c.Literals[0].Var);*/
      decisions.add(decisionLevelNull.getLevel(), c.getLiterals().get(0).getVar());
      //decisions.Insert(0, c.Literals[0].Var);
      c.getLiterals().get(0).getVar().setDecisionLevel(this.decisionLevel.get(0));
      c.getLiterals().get(0).getVar().setAssignment(c.getLiterals().get(0).getSign());
      c.getLiterals().get(0).getVar().setReason(null);
      c.getLiterals().get(0).getVar().setLocked(true);
      for (DecisionLevel l : this.decisionLevel) {
        l.setLevel(l.getLevel() + 1);
      }
      return true;
    }

    Watcher w1 = new Watcher(c.getLiterals().get(0), c);
    Watcher w2 = new Watcher(c.getLiterals().get(c.getLiterals().size() - 1), c);
    c.watcher[0] = w1;
    c.watcher[1] = w2;
    satClauses.add(c);
    return true;
  }

  public boolean addTClause(Clause c) throws Exception
  {
    if (c.getLiterals().size() == 1) {
      if (c.getLiterals().get(0).getVar().getLocked() && c.getLiterals().get(0).getSign() != c.getLiterals().get(0)
          .getVar().getAssignment())
        return false;

      //TODO Check: Do we need this?
      backTrack(this.decisionLevel.get(0));
      decisions.add(decisionLevelNull.getLevel(), c.getLiterals().get(0).getVar());
      //decisions.Insert(0, c.Literals[0].Var);
      c.getLiterals().get(0).getVar().setDecisionLevel(this.decisionLevel.get(0));
      c.getLiterals().get(0).getVar().setAssignment(c.getLiterals().get(0).getSign());
      c.getLiterals().get(0).getVar().setReason(null);
      for (DecisionLevel l : this.decisionLevel) {
        //Do we have to Lock T-Clauses?????
        //c.Literals[0].Var.Locked = true;
        l.setLevel(l.getLevel() + 1);
      }
      return true;
    }

    Watcher w1 = new Watcher(c.getLiterals().get(c.getLiterals().size() - 2), c);
    Watcher w2 = new Watcher(c.getLiterals().get(c.getLiterals().size() - 1), c);
    c.watcher[0] = w1;
    c.watcher[1] = w2;
    getTClauses().add(c);
    return true;
  }

  public boolean preAddIUnitClause(Var v, Assignment ass) throws Exception
  {
    if ((v.getAssignment() != Assignment.Unassigned) && (v.getAssignment() != ass)) {
      return false;
    }

    //problem is unsolveable
    decisions.add(0, v);
    v.setDecisionLevel(this.decisionLevel.get(0));
    v.setAssignment(ass);
    v.setReason(null);
    v.setLocked(true);
    this.decisionLevel.get(0).setLevel(this.decisionLevel.get(0).getLevel()+1);
    setUnitDecissions(getUnitDecissions() + 1);
    return true;
  }

  public boolean addIClause(Clause c) throws Exception
  {
    if (c.getLiterals().size() == 1) {
      //TODO brauchen wir hier noch einen check, ob da schon was gesetzt ist?
      //if (c.Literals[0].Var.Locked && c.Literals[0].Sign != c.Literals[0].Var.Assignment)
      //	return false;
      //backTrackAndRevoke(c.Literals[0].Var.DecisionLevel);
      backTrack(this.decisionLevel.get(0));
      decisions.add(0, c.getLiterals().get(0).getVar());
      c.getLiterals().get(0).getVar().setDecisionLevel(this.decisionLevel.get(0));
      c.getLiterals().get(0).getVar().setAssignment(c.getLiterals().get(0).getSign());
      c.getLiterals().get(0).getVar().setReason(null);
      c.getLiterals().get(0).getVar().setLocked(true);
      for (DecisionLevel l : this.decisionLevel) {
        l.setLevel(l.getLevel() + 1);
      }
      setUnitDecissions(getUnitDecissions() + 1);
      return true;
    }

    //TODO this does somehow not work! oO
    //Watcher w1 = new Watcher(c.Literals[c.Literals.Count-2], c);
    Watcher w1 = new Watcher(c.getLiterals().get(0), c);
    Watcher w2 = new Watcher(c.getLiterals().get(c.getLiterals().size() - 1), c);
    c.watcher[0] = w1;
    c.watcher[1] = w2;
    getIClauses().add(c);
    return true;
  }

  /**
   * Initialise some stuff, so information can be posted by interval propagation
   */
  public void init() throws Exception
  {
    //Initial decisionlevel, see addClause
    setUnitDecissions(decisions.size());
    this.decisionLevel.clear();
    this.decisionLevelNull.setLevel(decisions.size());
    this.decisionLevel.add(this.decisionLevelNull);
    for (Clause cl : clauses) {
      //int unitClauseCount = 0;
      if (cl.getLiterals().size() == 1)
        //unitClauseCount++;
        cl.setSatisfied(true);

    }
    recentBacktrack = false;
  }

  public boolean solve() throws Exception
  {
    int restartNum = 100;
    learntNum = 700;
    restartCount = 0;
    MutableObject<Double[][]> curRanges = new MutableObject<>();
    Double[] solution = null;
    DecisionLevel evaluatedDL = null;
    //check: is already undecisdable?
    Clause c;
    while (true) {
      c = null;
      while ((c = propagate()) != null) {
        //resolve all conflicts
        if (decisionLevel.size() == 1) {
          return false;
        }

        if (conflictCount % 50 == 0 && getCNSMTGSolver() != null && getCNSMTGSolver().begin + getCNSMTGSolver()
            .maxSolveTime < System.currentTimeMillis())
          return false;

        if (!resolveConflict(c)) {
          return false;
        }

      }
      if (getCNSMTGSolver() != null) {
        //check for conflict of Theoremprover
        if(getUseIntervalProp() && !getCNSMTGSolver().intervalPropagate(decisions, curRanges)) {
          continue;
        } else {
          /*
          //TODO: Heuristic Decision whether or not to query the T-solver
          //comes in here
          //double satRatio = ((double)satClauseCount) / clauses.Count;
          //double varRatio = ((double)decisions.Count) / variables.Count;
          //if (recentBacktrack || varRatio < r.NextDouble()) { //|| satRatio > r.NextDouble()) {
          //if (recentBacktrack || satRatio > r.NextDouble()) {
          //if (decisionCount % 10 == 0) {
          //	recentBacktrack = false;
          //if(solution==null || !SolutionInsideRange(solution, curRanges)) {
          //if(!VarAssignmentInsideRange(Decisions[Decisions.Count-1],curRanges)) {
          //if(evaluatedDL==null || !AssignmentInsideRange(evaluatedDL, curRanges)) {
            if (!CNSMTGSolver.ProbeForSolution(decisions, out solution)) {
              continue;
            }
          //	evaluatedDL = DecisionLevel[DecisionLevel.Count-1];
          //}
          //}
          */
          if (!getCNSMTGSolver().probeForSolution(decisions, solution)) {
            continue;
          }
          int satClauseCount=0;
          for(int i = clauses.size()-1; i>= 0; --i) {
            if (clauses.get(i).getSatisfied()) {
              satClauseCount++;
            }
          }
          if(satClauseCount>=clauses.size()) {
            return true;
          }
        }
      }

      Var next;
      //Make a decission:
      //Var next = Decider.DecideRangeBased(variables,this);
      if (getCNSMTGSolver() != null)
        next = Decider.decideVariableCountBased(variables, this);
      else
        next = Decider.decideActivityBased(variables, this);
      //Var next = decideRangeBased();
      //Var next = decide();
      if (next == null) {
        // if no unassigned vars
        System.out.println("ConflictCount: " + conflictCount + " DecisionCount " + decisionCount + " LC " + this
            .learnedCount);
        return true;
      }

      ++decisionCount;
      //if(decisionCount%10000==0) PrintStatistics();
      if (decisionCount % 25 == 0 && getCNSMTGSolver() != null && getCNSMTGSolver().begin + getCNSMTGSolver()
          .maxSolveTime < System.currentTimeMillis())
        return false;

      //Forget unused clauses
      if (decisionCount % 1000 == 0) {
        reduceDB(learntNum);
        for (Var v : variables) {
          v.setActivity(v.getActivity() / 4);
        }
      }

      if (false && decisionCount % restartNum == 0) {
        //perform restart
        restartNum *= 2;
        learntNum += learntNum / 10;
        restartCount++;
        for (int j = (decisionLevel.get(1).getLevel()); j < decisions.size(); j++) {
          decisions.get(j).setAssignment(Assignment.Unassigned);
          decisions.get(j).setReason(null);
          for (Watcher wa : decisions.get(j).getWatchList()) {
            //decisions[j].Seen = false;
            wa.getClause().setSatisfied(false);
          }

        }
        decisions.subList(decisionLevel.get(1).getLevel(), decisionLevel.get(1).getLevel()+decisions.size() -
            (decisionLevel.get(1).getLevel())).clear();
        decisionLevel.subList(1, 1+decisionLevel.size() - 1).clear();
      }

    }
  }

  public void printStatistics() throws Exception
  {
    System.out.format("DC: %s\tCC: %d\tAD: %d\tLC: %d/%d\t IC: %d\tTC: %d\tRestarts: %d\t0 Level: %d",
        decisionCount, conflictCount, decisions.size(), satClauses.size(), learntNum, restartCount, decisionLevel.get(0)
            .getLevel(), iClauses.size(), tClauses.size());
  }

  protected boolean solutionInsideRange(Double[] solution, Double[][] range) throws Exception
  {
    for (int i = 0; i < solution.length; i++) {
      double val = solution[i];
      if (val < range[i][0] ||val > range[i][1])
      return false;

    }
    return true;
  }

  protected boolean varAssignmentInsideRange(Var v, Double[][] range) throws Exception
  {
    Double[][] litrange = null;
    if (v.getAssignment() == Assignment.True)
      litrange = v.getPositiveRanges();
    else
      litrange = v.getNegativeRanges();
    for (int i = 0; i < litrange.length; i++) {
      double min = litrange[i][0];
      double max = litrange[i][1];
      if (min < range[i][0] ||max > range[i][1])
      return false;

    }
    return true;
  }

  protected boolean assignmentInsideRange(DecisionLevel dl, Double[][] range) throws Exception
  {
    for (int i = dl.getLevel(); i < decisions.size(); i++) {
      if (!varAssignmentInsideRange(decisions.get(i), range))
        return false;

    }
    return true;
  }

  public void reduceDB(int num) throws Exception
  {
    if (satClauses.size() < num)
      return;

    Collections.sort(satClauses);
    for (int i = num; i < satClauses.size(); i++) {
      satClauses.get(i).watcher[0].getLit().getVar().getWatchList().remove(satClauses.get(i).watcher[0]);
      satClauses.get(i).watcher[1].getLit().getVar().getWatchList().remove(satClauses.get(i).watcher[1]);
    }
    //Lit l = satClauses[i].Literals[satClauses[i].Literals.Count-1];
    //if (l.Var.Reason == satClauses[i]) l.Var.Reason = null;
    satClauses.subList(num, satClauses.size()).clear();
    for (int i = 0; i < satClauses.size(); i++) {
      satClauses.get(i).setActivity(satClauses.get(i).getActivity()/4);
    }
  }

  public Clause propagate() throws Exception
  {
    int lLevel = 0;
    if (decisionLevel.size() > 1)
      lLevel = decisionLevel.get(decisionLevel.size() - 1).getLevel();

    for (int i = lLevel; i < decisions.size(); i++) {
      List<Watcher> watchList = decisions.get(i).getWatchList();
      for (int j = 0; j < watchList.size(); j++) {
        Watcher w = watchList.get(j);
        if (w.getClause().getSatisfied())
          continue;

        if (w.getLit().satisfied()) {
          w.getClause().setSatisfied(true);
          continue;
        }

        //TODO Do we need this?
        if (w.getLit().getVar().getAssignment() == Assignment.Unassigned)
          continue;

        //This can be optimized !?
        Clause c = w.getClause();
        //Search for new Watch
        int oWId = (c.watcher[0] == w) ? 1 : 0;
        if (c.watcher[oWId].getLit().satisfied()) {
          //TODO: Do we need this?
          w.getClause().setSatisfied(true);
          continue;
        }

        boolean found = false;
        for (Object __dummyForeachVar8 : c.getLiterals()) {
          Lit l = (Lit) __dummyForeachVar8;
          if (c.watcher[oWId].getLit().getVar() != l.getVar() && (l.getVar().getAssignment() == Assignment.Unassigned || l
              .satisfied())) {
            w.getLit().getVar().getWatchList().remove(w);
            j--;
            w.setLit(l);
            l.getVar().getWatchList().add(w);
            found = true;
            if (l.satisfied())
              w.getClause().setSatisfied(true);

            break;
          }

        }
        if (!found) {
          c.setActivity(c.getActivity() + 1);
          //TODO Handle Watcher here ... do not return -> faster
          Watcher w2 = c.watcher[oWId];
          if (w2.getLit().getVar().getAssignment() == Assignment.Unassigned) {
            w2.getLit().getVar().setAssignment(w2.getLit().getSign());
            w2.getClause().setSatisfied(true);
            w2.getLit().getVar().setDecisionLevel(decisionLevel.get(decisionLevel.size() - 1));
            decisions.add(w2.getLit().getVar());
            w2.getLit().getVar().setReason(c);
            for (Watcher wi : w2.getLit().getVar().getWatchList()) {
              wi.getClause().setLastModVar(w2.getLit().getVar());
            }
          } else
            return c;
        }

      }
    }
    return null;
  }

  public boolean resolveConflict(Clause c) throws Exception
  {
    ++conflictCount;
    //Learn Clause from conflict here
    Clause confl = c;
    Clause learnt = new Clause();
    int index = decisions.size() - 1;
    int pathC = 0;
    Var p = null;
    do {
      //Find all Literals until First Unique Implication Point(UIP)
      Clause cl = confl;
      for (int j = 0; j < cl.getLiterals().size(); j++) {
        //Inspect conflict reason clause Literals
        Lit q = cl.getLiterals().get(j);
        //ignore UIP
        if (q.getVar() == p) {
          continue;
        }

        //ignore sawnvariables and decissionlevel 0
        if (!q.getVar().getSeen() && q.getVar().getDecisionLevel() != decisionLevel.get(0)) {
          q.getVar().setSeen(true);
          //if q has been decided in curent level: increase iterations; else add literal to learnt clause
          if (q.getVar().getDecisionLevel().getLevel() >= (decisionLevel.get(decisionLevel.size() - 1).getLevel())) {
            pathC++;
          } else {
            learnt.add(q);
          }
        }

      }
      while (!decisions.get(index--).getSeen())
        ;
      // Select next clause to look at:
      //do { if(index<0) {Console.Write("BLA"); return false;} }
      p = decisions.get(index + 1);
      confl = p.getReason();
      p.setSeen(false);
      pathC--;
    }
    while (pathC > 0);
    //Add UIP
    Lit t = new Lit(p, (p.getAssignment() == Assignment.False) ? Assignment.True : Assignment.False);
    learnt.add(t);
    //Store Seen Variables for later reset
    List<Lit> SeenList = new ArrayList<Lit>(learnt.getLiterals());
    for (int m = 0; m < learnt.getLiterals().size() - 1; ++m) {
      //simplify learnt clause
      //Here is still an error!!!!!!!
      Lit l = learnt.getLiterals().get(m);
      //Ignore Literals without reason
      if (l.getVar().getReason() == null) {
        continue;
      } else {
        //Check whether reason for current literal is already in learnt -> remove l
        Clause re = l.getVar().getReason();
        boolean found = false;
        for (Lit rel : re.getLiterals()) {
          if (!rel.getVar().getSeen() && (rel.getVar().getDecisionLevel() != decisionLevel.get(0))) {
            found = true;
            break;
          }

        }
        if (!found) {
          learnt.getLiterals().remove(m--);
        }

      }
    }
    for (Lit l : SeenList) {
      //Reset Seen
      l.getVar().setSeen(false);
    }
    //End Learn Clause
    //Find backtracklevel:
    DecisionLevel db;
    int i = 1, maxLitIndex = 0;
    Lit changeLit;
    if (learnt.getLiterals().size() == 1)
      db = this.decisionLevel.get(0);
    else {
      db = learnt.getLiterals().get(0).getVar().getDecisionLevel();
      maxLitIndex = 0;
      for (i = 1; i < learnt.getLiterals().size() - 1; ++i) {
        //Search newest decission, which affects a literal in learnt.
        Lit l = learnt.getLiterals().get(i);
        if (db.getLevel() < l.getVar().getDecisionLevel().getLevel()) {
          db = l.getVar().getDecisionLevel();
          maxLitIndex = i;
        }

      }
      changeLit = learnt.getLiterals().get(0);
      learnt.getLiterals().set(0 , learnt.getLiterals().get(maxLitIndex));
      learnt.getLiterals().set(maxLitIndex, changeLit);
    }
    //Backtrack to db
    backTrack(db);
    //Add learnt clause: Unit Clauses have to be satisfied otherwise: -> UNSAT
    boolean solvable = this.addSATClause(learnt);
    if (!solvable) {
      // TODO can be removed once bugfree
      System.out.println("Error on insert learned clause");
      return false;
    }

        /*if(db==DecisionLevel[0] && learnt.Literals.Count>1) {
        				Console.WriteLine("Reached decision level 0");
        				return false;
        			}*/
    if (learnt.getLiterals().size() == 1) {
      //decisions[0].Assignment = learnt.Literals[0].Sign;
      learnt.getLiterals().get(0).getVar().setAssignment(learnt.getLiterals().get(0).getSign());
      learnt.getLiterals().get(0).getVar().setReason(null);
    }

    //Switch assignment of UIP to satisfy learnt
    if (learnt.getLiterals().size() > 1) {
      //DecisionLevel d = new DecisionLevel(decisions.Count);
      //decisionLevel.Add(d);
      //Set Learnt as Reason for UIP
      Lit l = learnt.getLiterals().get(learnt.getLiterals().size() - 1);
      l.getVar().setAssignment(l.getSign());
      learnt.setSatisfied(true);
      l.getVar().setDecisionLevel(this.decisionLevel.get(this.decisionLevel.size() - 1));
      l.getVar().setReason(learnt);
      decisions.add(l.getVar());
    }

    return true;
  }

  public void backTrack(DecisionLevel db) throws Exception
  {
    //TODO make this more efficient (linked list?)
    recentBacktrack = true;
    int ndbidx = decisionLevel.indexOf(db) + 1;
    if (ndbidx >= decisionLevel.size())
      return;

    db = decisionLevel.get(ndbidx);
    for (int j = db.getLevel(); j < decisions.size(); j++) {
      decisions.get(j).setAssignment(Assignment.Unassigned);
      decisions.get(j).setReason(null);
      for (Watcher wa : decisions.get(j).getWatchList()) {
        //this is expensive
        wa.getClause().setSatisfied(wa.getClause().watcher[0].getLit().satisfied() || wa.getClause().watcher[1].getLit()
            .satisfied());
      }
    }
    //wa.Clause.Satisfied = false; //this should take other watcher into account
    decisions.subList(db.getLevel(), decisions.size()).clear();
    //int i = decisionLevel.IndexOf(db);
    int i = ndbidx;
    i = Math.max(1, i);
    decisionLevel.subList(i, decisionLevel.size()).clear();
  }

  //This moves all decissions to decission level 0!
  public void backTrack(int decission) throws Exception
  {
    recentBacktrack = true;
    if (decission < 0)
      return;

    if (decisionLevel.size() < 2)
      decisionLevel.add(new DecisionLevel(decission));

    //if(decission-1<decisions.Count) decisions[decission-1].Print();
    //	Console.WriteLine();
    decisionLevel.get(1).setLevel(decission);
    for (int j = (decisionLevel.get(1).getLevel()); j < decisions.size(); j++) {
      decisions.get(j).setAssignment(Assignment.Unassigned);
      decisions.get(j).setReason(null);
      decisions.get(j).setLocked(false);
      for (Watcher wa : decisions.get(j).getWatchList()) {
        wa.getClause().setSatisfied(false);
      }
    }
    if (decisionLevel.get(1).getLevel() < decisions.size())
      decisions.subList(decisionLevel.get(1).getLevel(), decisions.size()).clear();

    decisionLevel.subList(1, decisionLevel.size()).clear();
    decisionLevelNull.setLevel(decisions.size());
  }

  void printAssignments() throws Exception
  {
    for (Var v : getVariables()) {
      v.print();
      System.out.print(" ");
    }
    System.out.println();
  }

}


