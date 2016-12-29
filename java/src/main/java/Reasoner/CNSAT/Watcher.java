//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.CNSAT;

public class Watcher
{
    private Clause Clause;
    public Clause getClause() {
        return Clause;
    }

    public void setClause(Clause value) {
        Clause = value;
    }

    private Lit Lit;
    public Lit getLit() {
        return Lit;
    }

    public void setLit(Lit value) {
        Lit = value;
    }

    public Watcher(Lit l, Clause parent) throws Exception {
        this.setClause(parent);
        this.setLit(l);
        getLit().getVar().getWatchList().add(this);
    }

}


