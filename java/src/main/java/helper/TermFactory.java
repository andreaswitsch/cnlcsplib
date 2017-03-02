package helper;

import AutoDiff.Abs;
import AutoDiff.And;
import AutoDiff.Atan2;
import AutoDiff.Constant;
import AutoDiff.Exp;
import AutoDiff.LTConstraint;
import AutoDiff.LTEConstraint;
import AutoDiff.Max;
import AutoDiff.Min;
import AutoDiff.Negation;
import AutoDiff.Or;
import AutoDiff.Term;
import AutoDiff.TermBuilder;
import AutoDiff.Variable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import wtp.parser.ExprLexer;
import wtp.parser.ExprParser;

/**
 * Created by awitsch on 26.11.16.
 */
public class TermFactory
{
  protected static Dictionary<String, Variable> varDictionary;

  public static Term createTermFromString(String aritmeticExpression, List<Variable> vars) throws Exception
  {
    Variable.resetIdCounter();
    varDictionary = new Hashtable<>();
    CharStream inputCharStream = new ANTLRInputStream(new StringReader(aritmeticExpression));
    TokenSource tokenSource = new ExprLexer(inputCharStream);
    TokenStream inputTokenStream = new CommonTokenStream(tokenSource);
    ExprParser parser = new ExprParser(inputTokenStream);
    ExprParser.ExprContext tree = parser.expr();

    Term expression = parse(tree, vars);
    return expression;
  }

  protected static Term parse(ParseTree tree, List<AutoDiff.Variable> vars) throws Exception
  {
    Term thisTerm=null;
    ParseTree thisType=null;
    List<Term> params = new ArrayList<>();
    for (ParseTree child : ((ExprParser.ExprContext) tree).children) {
      if(child instanceof ExprParser.ExprContext) {
        params.add(parse(child, vars));
      } else if(child instanceof TerminalNode) {
        if(!StringUtils.equals(child.getText(),"(") && !StringUtils.equals(child.getText(),")")
            && !StringUtils.equals(child.getText(),","))
          thisType = child;
      } else {
        System.out.println("OMG!");
      }
    }
    if(thisType==null) {
      return params.get(0);
    }
    return termFactory(thisType, params, vars);
  }

  protected static Term termFactory(ParseTree thisType, List<Term> params, List<AutoDiff.Variable> vars) throws Exception
  {
    if(StringUtils.equals(thisType.getText(), "<")) {
      return new LTConstraint(params.get(0), params.get(1), 0.01);
    } else if(StringUtils.equals(thisType.getText(), "<=")) {
      return new LTEConstraint(params.get(0), params.get(1), 0.01);
    } else if(StringUtils.equals(thisType.getText(), ">=")) {
      return new LTEConstraint(params.get(1), params.get(0), 0.01);
    } else if(StringUtils.equals(thisType.getText(), ">")) {
      return new LTConstraint(params.get(1), params.get(0), 0.01);
    } else if(StringUtils.equals(thisType.getText(), "and") || StringUtils.equals(thisType.getText(), "&&")) {
      return new And(params.get(1), params.get(0));
    } else if(StringUtils.equals(thisType.getText(), "or") || StringUtils.equals(thisType.getText(), "||")) {
      return new Or(params.get(1), params.get(0));
    } else if(StringUtils.equals(thisType.getText(), "^")) {
      return TermBuilder.Power(params.get(0), params.get(1));
    } else if(StringUtils.equals(thisType.getText(), "*")) {
      return params.get(0).multiply(params.get(1));
    } else if(StringUtils.equals(thisType.getText(), "+")) {
      return params.get(0).add(params.get(1));
    } else if(StringUtils.equals(thisType.getText(), "-")) {
      if(params.size()>=2) {
        return params.get(0).substract(params.get(1));
      } else if (params.size()==1) {
        return (new Constant(-1)).multiply(params.get(0));
      }
    } else if(StringUtils.equals(thisType.getText(), "/")) {
      return params.get(0).divide(params.get(1));
    } else if(StringUtils.equals(thisType.getText(), "sqrt")) {
      return TermBuilder.Power(params.get(0), 0.5);
    } else if(StringUtils.equals(thisType.getText(), "cos")) {
      return TermBuilder.Cos(params.get(0));
    } else if(StringUtils.equals(thisType.getText(), "sin")) {
      return TermBuilder.Sin(params.get(0));
    } else if(StringUtils.equals(thisType.getText(), "log")) {
      return TermBuilder.Log(params.get(0));
    } else if(StringUtils.equals(thisType.getText(), "abs")) {
      return new Abs(params.get(0));
    } else if(StringUtils.equals(thisType.getText(), "atan2")) {
      return new Atan2(params.get(0), params.get(1));
    } else if(StringUtils.equals(thisType.getText(), "min")) {
      return new Min(params.get(0), params.get(1));
    } else if(StringUtils.equals(thisType.getText(), "max")) {
      return new Max(params.get(0), params.get(1));
    } else if(StringUtils.equals(thisType.getText(), "exp")) {
      return new Exp(params.get(0));
    } else if(StringUtils.equals(thisType.getText(), "~")) {
      return new Negation(params.get(0));
    }

    if(thisType instanceof TerminalNode) {
      if(NumberUtils.isNumber(thisType.getText())) {
        return TermBuilder.Constant(Double.parseDouble(thisType.getText()));
      } else {
        Variable v = varDictionary.get(thisType.getText());
        if(v == null) {
          v = new Variable();
          v.setName(thisType.getText());
          varDictionary.put(thisType.getText(), v);
          vars.add(v);
        }
        return v;
      }
    }
    return null;
  }


}
