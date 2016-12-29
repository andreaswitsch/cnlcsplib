//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:33
//

package AutoDiff;

import org.apache.commons.lang3.ArrayUtils;

public class ParametricCompiledTerm   implements IParametricCompiledTerm
{
    private final ICompiledTerm compiledTerm;
    public ParametricCompiledTerm(Term term, Variable[] variables, Variable[] parameters) throws Exception {
        compiledTerm = TermUtils.compile(term, (Variable[]) ArrayUtils.addAll(variables, parameters));
        setVariables(variables);
        setParameters(parameters);
    }

    public double evaluate(double[] arg, double[] parameters) throws Exception {
        double[] combinedArg = ArrayUtils.addAll(arg, parameters);
        return compiledTerm.evaluate(combinedArg);
    }

    public Tuple<Double[],Double> differentiate(double[] arg, double[] parameters) throws Exception {
        double[] combinedArg = ArrayUtils.addAll(arg, parameters);
        Tuple<Double[], Double> diffResult = compiledTerm.differentiate(combinedArg);
        Double[] partialGradient = new Double[arg.length];
        System.arraycopy(diffResult.getItem1(), 0, partialGradient, 0, partialGradient.length);
        return Tuple.create(partialGradient, diffResult.getItem2());
    }

    private Variable[] Variables = new Variable[0];
    public Variable[] getVariables() {
        return Variables;
    }

    public void setVariables(Variable[] value) {
        Variables = value;
    }

    private Variable[] Parameters = new Variable[0];
    public Variable[] getParameters() {
        return Parameters;
    }

    public void setParameters(Variable[] value) {
        Parameters = value;
    }

}


