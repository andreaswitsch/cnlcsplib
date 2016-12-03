//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:32
//

package AutoDiff.Compiled;

abstract public class TapeElement
{
    public double Value;
    public double Adjoint;
    public InputEdge[] Inputs = new InputEdge[0];
    public abstract void accept(ITapeVisitor visitor) throws Exception ;

}


