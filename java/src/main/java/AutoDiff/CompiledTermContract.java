//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

public class CompiledTermContract   implements ICompiledTerm
{
    public double evaluate(double... arg) throws Exception {
        if (arg == null)
            throw new Exception("arg == null");
         
        if (arg.length != getVariables().length)
            throw new Exception("Length mismatch");
         
        return 0/* [UNSUPPORTED] default expressions are not yet supported "default double" */;
    }

    public Tuple<Double[],Double> differentiate(double... arg) throws Exception {
        if (arg == null)
            throw new Exception("arg == null");
         
        if (arg.length != getVariables().length)
            throw new Exception("Length mismatch");
         
        return null;
    }

    //Contract.Ensures(Contract.Result<Tuple<double[], double>>() != null);
    //Contract.Ensures(Contract.Result<Tuple<double[], double>>().Item1.Length == arg.Length);
    public Variable[] getVariables() throws Exception {
        return null;
    }

}


