//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.CNSAT;

public class Watcher
{
    private Clause __Clause;
    public Clause getClause() {
        return __Clause;
    }

    public void setClause(Clause value) {
        __Clause = value;
    }

    private Lit __Lit;
    public Lit getLit() {
        return __Lit;
    }

    public void setLit(Lit value) {
        __Lit = value;
    }

    public Watcher(Lit l, Clause parent) throws Exception {
        this.setClause(parent);
        this.setLit(l);
        getLit().getVar().getWatchList().add(this);
    }

}


