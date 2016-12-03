//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:46
//

package Reasoner.CNSAT;


public class DecisionLevel   
{
    private int __Level;
    public int getLevel() {
        return __Level;
    }

    public void setLevel(int value) {
        __Level = value;
    }

    //public double[] Seed {get; set;}
    public DecisionLevel(int level) throws Exception {
        this.setLevel(level);
    }

}


