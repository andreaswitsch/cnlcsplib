package AutoDiff.Compiled;

import AutoDiff.ITermVisitor;
import AutoDiff.Term;
import AutoDiff.Zero;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by awitsch on 12.11.16.
 */
public class Compiler implements ITermVisitor<Integer>
{
  // int --> the index of the compiled element in the tape
  private List<TapeElement> tape = new ArrayList<TapeElement>();

  private HashMap<Term, Integer> indexOf = new HashMap<Term, Integer>();

  public Compiler(AutoDiff.Variable[] variables, List<TapeElement> tape) throws Exception
  {
    this.tape = tape;
    indexOf = new HashMap<>();
    for (int i = 0; i < variables.length; i++) {
      //Replacement for Linq code -- HS
      indexOf.put(variables[i], i);
      tape.add(new Variable());
    }
  }

  public void compile(Term term) throws Exception
  {
    term.accept(this);
  }

  //Func<CompileResult>
  private Integer compile(Term term, TapeElement compiled) throws Exception
  {
    Integer index = indexOf.get(term);

    if (index == null) {
      TapeElement compileResult = compiled;
      tape.add(compileResult);

      index = tape.size() - 1;
      indexOf.put(term, index);
    }

    return index;
  }


  public Integer visit(AutoDiff.Constant constant) throws Exception
  {
    Constant var = new Constant(constant.getValue());
    var.Inputs = new AutoDiff.Compiled.InputEdge[0];
    return compile(constant, var);
  }

  public Integer visit(Zero zero) throws Exception
  {
    Constant var = new AutoDiff.Compiled.Constant(0);
    var.Inputs = new AutoDiff.Compiled.InputEdge[0];
    return compile(zero, var);
  }

  public Integer visit(AutoDiff.ConstPower intPower) throws Exception
  {
    AutoDiff.Compiled.ConstPower var = new AutoDiff.Compiled.ConstPower();
    int baseIndex = intPower.getBase().accept(this);

    var.Base = baseIndex;
    var.Exponent = intPower.getExponent();
    var.Inputs = new AutoDiff.Compiled.InputEdge[]
        { new AutoDiff.Compiled.InputEdge() };
    var.Inputs[0].Index = baseIndex;

    return compile(intPower, var);
  }

  public Integer visit(AutoDiff.TermPower power) throws Exception
  {

    int baseIndex = power.getBase().accept(this);
    int expIndex = power.getExponent().accept(this);
    final TermPower var = new TermPower();

    var.Base = baseIndex;
    var.Exponent = expIndex;
    var.Inputs = new AutoDiff.Compiled.InputEdge[]
        {
            new AutoDiff.Compiled.InputEdge(),
            new AutoDiff.Compiled.InputEdge()
        };
    var.Inputs[0].Index = baseIndex;
    var.Inputs[1].Index = expIndex;
    return compile(power, var);
  }

  public Integer visit(AutoDiff.Product product) throws Exception
  {
    int leftIndex = product.getLeft().accept(this);
    int rightIndex = product.getRight().accept(this);
    final Product var = new Product();

    var.Left = leftIndex;
    var.Right = rightIndex;
    var.Inputs = new AutoDiff.Compiled.InputEdge[]
        {
            new AutoDiff.Compiled.InputEdge(),
            new AutoDiff.Compiled.InputEdge(),
        };
    var.Inputs[0].Index = leftIndex;
    var.Inputs[1].Index = rightIndex;

    return compile(product, var);
  }

  public Integer visit(AutoDiff.Sum sum) throws Exception
  {
    int[] indices = new int[sum.getTerms().length];
    AutoDiff.Compiled.InputEdge[] inputs = new AutoDiff.Compiled.InputEdge[indices.length];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = sum.getTerms()[i].accept(this);
      inputs[i] = new AutoDiff.Compiled.InputEdge();
      inputs[i].Index = indices[i];
    }

    final Sum var = new Sum();
    var.Terms = indices;
    var.Inputs = inputs;

    return compile(sum, var);
  }


  public Integer visit(AutoDiff.Variable variable) throws Exception
  {
    return indexOf.get(variable);
  }

  public Integer visit(AutoDiff.Log log) throws Exception
  {
    int argIndex = log.getArg().accept(this);
    final Log var = new Log();

    var.Arg = argIndex;
    var.Inputs = new AutoDiff.Compiled.InputEdge[]
        {
            new AutoDiff.Compiled.InputEdge()
        };
    var.Inputs[0].Index = argIndex;


    return compile(log, var);
  }

  public Integer visit(AutoDiff.Exp exp) throws Exception
  {
    int argIndex = exp.getArg().accept(this);
    final Exp var = new Exp();

    var.Arg = argIndex;
    var.Inputs = new AutoDiff.Compiled.InputEdge[]
        {
            new AutoDiff.Compiled.InputEdge()
        };
    var.Inputs[0].Index = argIndex;

    return compile(exp, var);
  }


  //Additions by Carpe Noctem:
  public Integer visit(AutoDiff.Min min) throws Exception
  {
    final int leftIndex = min.getLeft().accept(this);
    final int rightIndex = min.getRight().accept(this);
    final Min var = new AutoDiff.Compiled.Min();

    var.Left = leftIndex;
    var.Right = rightIndex;
    var.Inputs = new AutoDiff.Compiled.InputEdge[]
        {
            new AutoDiff.Compiled.InputEdge(leftIndex),
            new AutoDiff.Compiled.InputEdge(rightIndex)
        };

    return compile(min, var);
  }

  public Integer visit(AutoDiff.Max max) throws Exception
  {
    final int leftIndex = max.getLeft().accept(this);
    final int rightIndex = max.getRight().accept(this);
    final Max var = new AutoDiff.Compiled.Max();

    var.Left = leftIndex;
    var.Right = rightIndex;
    var.Inputs = new AutoDiff.Compiled.InputEdge[]
        {
            new AutoDiff.Compiled.InputEdge(leftIndex),
            new AutoDiff.Compiled.InputEdge(rightIndex)
        };

    return compile(max, var);
  }

  public Integer visit(AutoDiff.Reification dis) throws Exception
  {
    int conIndex = dis.getCondition().accept(this);
    int negConIndex = dis.getNegatedCondition().accept(this);
    final Reification var = new AutoDiff.Compiled.Reification();

    var.Min = dis.Min;
    var.Max = dis.Max;
    var.Condition = conIndex;
    var.NegatedCondition = negConIndex;
    var.Inputs = new AutoDiff.Compiled.InputEdge[]
        {
            new AutoDiff.Compiled.InputEdge(conIndex),
            new AutoDiff.Compiled.InputEdge(negConIndex)
        };

    return compile(dis, var);
  }

  public Integer visit(AutoDiff.And and) throws Exception
  {
    final And var = new AutoDiff.Compiled.And();
    int leftIndex = and.getLeft().accept(this);
    int rightIndex = and.getRight().accept(this);

    var.Left = leftIndex;
    var.Right = rightIndex;
    var.Inputs = new AutoDiff.Compiled.InputEdge[]
        {
            new AutoDiff.Compiled.InputEdge(leftIndex),
            new AutoDiff.Compiled.InputEdge(rightIndex)
        };

    return compile(and, var);
  }

  public Integer visit(AutoDiff.Or or) throws Exception
  {
    int rightIndex = or.getRight().accept(this);
    int leftIndex = or.getLeft().accept(this);
    final And var = new AutoDiff.Compiled.And();

    var.Left = leftIndex;
    var.Right = rightIndex;
    var.Inputs = new AutoDiff.Compiled.InputEdge[]
        {
            new AutoDiff.Compiled.InputEdge(leftIndex),
            new AutoDiff.Compiled.InputEdge(rightIndex)
        };

    return compile(or, var);
  }

  public Integer visit(AutoDiff.ConstraintUtility cu) throws Exception
  {
    int constraint = cu.getConstraint().accept(this);
    int util = cu.getUtility().accept(this);

    final ConstraintUtility var = new ConstraintUtility();

    var.Constraint = constraint;
    var.Utility = util;
    var.Inputs = new InputEdge[]
        {
            new InputEdge(constraint),
            new InputEdge(util)
        };

    return compile(cu, var);
  }

  public Integer visit(AutoDiff.Sigmoid sigmoid) throws Exception
  {
    int argIndex = sigmoid.getArg().accept(this);
    int midIndex = sigmoid.getMid().accept(this);

    final Sigmoid var = new Sigmoid();

    var.Arg = argIndex;
    var.Mid = midIndex;
    var.Steepness = sigmoid.getSteepness();
    var.Inputs = new InputEdge[]
        {
            new InputEdge(argIndex),
            new InputEdge(midIndex)
        };


    return compile(sigmoid, var);
  }

  public Integer visit(AutoDiff.LinSigmoid sigmoid) throws Exception
  {
    int argIndex = sigmoid.getArg().accept(this);
    final Sigmoid var = new Sigmoid();

    var.Arg = argIndex;
    var.Inputs = new InputEdge[]
        {
            new InputEdge(argIndex)
        };


    return compile(sigmoid, var);
  }

  public Integer visit(AutoDiff.LTConstraint constraint) throws Exception
  {
    int lIndex = constraint.getLeft().accept(this);
    int rIndex = constraint.getRight().accept(this);

    final LTConstraint var = new LTConstraint();

    var.Left = lIndex;
    var.Right = rIndex;
    var.Steepness = constraint.getSteepness();
    var.Inputs = new InputEdge[]
        {
            new InputEdge(lIndex),
            new InputEdge(rIndex)
        };


    return compile(constraint, var);
  }

  public Integer visit(AutoDiff.LTEConstraint constraint) throws Exception
  {
    int lIndex = constraint.getLeft().accept(this);
    int rIndex = constraint.getRight().accept(this);

    final LTEConstraint var = new LTEConstraint();

    var.Left = lIndex;
    var.Right = rIndex;
    var.Steepness = constraint.getSteepness();
    var.Inputs = new InputEdge[]
        {
            new InputEdge(lIndex),
            new InputEdge(rIndex)
        };

    return compile(constraint, var);
  }

  public Integer visit(AutoDiff.Sin sin) throws Exception
  {
    int argIndex = sin.getArg().accept(this);
    final Sin var = new Sin();

    var.Arg = argIndex;
    var.Inputs = new InputEdge[]
        {
            new InputEdge(argIndex),
        };

    return compile(sin, var);
  }

  public Integer visit(AutoDiff.Cos cos) throws Exception
  {
    int argIndex = cos.getArg().accept(this);
    final Cos var = new Cos();

    var.Arg = argIndex;
    var.Inputs = new InputEdge[]
        {
            new InputEdge(argIndex),
        };

    return compile(cos, var);
  }

  public Integer visit(AutoDiff.Abs abs) throws Exception
  {
    int argIndex = abs.getArg().accept(this);
    final Abs var = new Abs();

    var.Arg = argIndex;
    var.Inputs = new InputEdge[]
        {
            new InputEdge(argIndex)
        };

    return compile(abs, var);
  }

  public Integer visit(AutoDiff.Atan2 atan2) throws Exception
  {
    int lIndex = atan2.getLeft().accept(this);
    int rIndex = atan2.getRight().accept(this);

    final Atan2 var = new Atan2();

    var.Left = lIndex;
    var.Right = rIndex;
    var.Inputs = new InputEdge[]
        {
            new InputEdge(lIndex),
            new InputEdge(rIndex)
        };

    return compile(atan2, var);
  }


  public Integer visit(AutoDiff.Negation negation) throws Exception
  {
    int arg = negation.getArg().accept(this);

    final Negation var = new Negation();

    var.Arg = arg;
    var.Inputs = new InputEdge[]
        {
            new InputEdge(arg),
        };

    return compile(negation, var);
  }

}
