//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:34
//

package AutoDiff;

import AutoDiff.Compiled.EvalVisitor;
import AutoDiff.Compiled.ForwardSweepVisitor;
import AutoDiff.Compiled.InputEdge;
import AutoDiff.Compiled.TapeElement;
import java.util.ArrayList;
import java.util.List;


//using System.Diagnostics.Contracts;/**
/* Compiles the terms tree to a more efficient form for differentiation.
*/
public class CompiledDifferentiator implements ICompiledTerm
{
    private TapeElement[] tape = new TapeElement[0];
    /**
    * Initializes a new instance of the 
    *  {@link #CompiledDifferentiator}
    *  class.
    * 
    *  @param function The function.
    *  @param variables The variables.
    */
    public CompiledDifferentiator(Term function, Variable[] variables) throws Exception {
        if (function instanceof Variable)
            function = new ConstPower(function, 1);
         
        List<TapeElement> tapeList = new ArrayList<TapeElement>();
        (new AutoDiff.Compiled.Compiler(variables, tapeList)).compile(function);
        tape = new TapeElement[tapeList.size()];
        tapeList.toArray(tape);
        setDimension(variables.length);
        setVariables(variables);
    }

    private int __Dimension;
    public int getDimension() {
        return __Dimension;
    }

    public void setDimension(int value) {
        __Dimension = value;
    }

    public double evaluate(double[] arg) throws Exception {
        /*
                    Contract.Requires(arg != null);
                    Contract.Requires(arg.Length == Dimension);
                    */
        evaluateTape(arg);
        return tape[tape.length-1].Value;
    }

    public Tuple<Double[],Double> differentiate(double[] arg) throws Exception {
        /*
                    Contract.Requires(arg != null);
                    Contract.Requires(arg.Length == Dimension);
        			*/
        forwardSweep(arg);
        reverseSweep();
        //Replacement for Linq code -- HS
        Double[] gradient = new Double[getDimension()];
        for (int i = 0;i < getDimension();i++)
        {
            gradient[i] = tape[i].Adjoint;
        }
        Double value = tape[tape.length - 1].Value;
        return Tuple.create(gradient, value);
    }

    //var gradient = tape.Take(Dimension).Select(elem => elem.Adjoint).ToArray();
    //var value = tape.Last().Value;
    private void reverseSweep() throws Exception {
        //Removed Linq code -- HS
        tape[tape.length - 1].Adjoint = 1;
        for (int i = 0;i < tape.length - 1;++i)
            //tape.Last().Adjoint = 1;
            // initialize adjoints
            tape[i].Adjoint = 0;
        for (int i = tape.length - 1;i >= getDimension();--i)
        {
            // accumulate adjoints
            InputEdge[] inputs = tape[i].Inputs;
            double adjoint = tape[i].Adjoint;
            for (int j = 0;j < inputs.length;++j)
                tape[inputs[j].Index].Adjoint += adjoint * inputs[j].Weight;
        }
    }

    private void forwardSweep(double[] arg) throws Exception {
        for (int i = 0;i < getDimension();++i)
            tape[i].Value = arg[i];
        ForwardSweepVisitor forwardDiffVisitor = new ForwardSweepVisitor(tape);
        for (int i = getDimension();i < tape.length;++i)
            tape[i].accept(forwardDiffVisitor);
    }

    private void evaluateTape(double[] arg) throws Exception {
        for (int i = 0;i < getDimension();++i)
            tape[i].Value = arg[i];
        EvalVisitor evalVisitor = new EvalVisitor(tape);
        for (int i = getDimension();i < tape.length;++i)
            tape[i].accept(evalVisitor);
    }

    private double valueOf(int index) throws Exception {
        return tape[index].Value;
    }

    private Variable[] __Variables = new Variable[0];
    public Variable[] getVariables() {
        return __Variables;
    }

    public void setVariables(Variable[] value) {
        __Variables = value;
    }

}


