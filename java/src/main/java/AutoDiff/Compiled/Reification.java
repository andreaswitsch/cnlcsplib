//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:32
//

package AutoDiff.Compiled;

public class Reification  extends TapeElement
{
    public double Min;
    public double Max;
    public int Condition;
    public int NegatedCondition;
    public void accept(ITapeVisitor visitor) throws Exception {
        visitor.visit(this);
    }

}


