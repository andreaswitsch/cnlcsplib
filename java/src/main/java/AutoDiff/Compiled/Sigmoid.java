//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:32
//

package AutoDiff.Compiled;

public class Sigmoid  extends TapeElement
{
    public int Arg;
    public int Mid;
    public double Steepness;
    public void accept(ITapeVisitor visitor) throws Exception {
        visitor.visit(this);
    }

}


