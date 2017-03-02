package helper;

import AutoDiff.Term;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import wtp.rangesParser.RangesLexer;
import wtp.rangesParser.RangesParser;

/**
 * Created by awitsch on 26.11.16.
 */
public class RangesFactory
{
  protected static Dictionary<String, Variable> varDictionary;

  public static Double[][] createTermFromString(String rangesExpression, List<Variable> vars) throws Exception
  {
    varDictionary = new Hashtable<>();
    CharStream inputCharStream = new ANTLRInputStream(new StringReader(rangesExpression));
    TokenSource tokenSource = new RangesLexer(inputCharStream);
    TokenStream inputTokenStream = new CommonTokenStream(tokenSource);
    RangesParser parser = new RangesParser(inputTokenStream);
    RangesParser.ProgContext tree = parser.prog();

    return parse(tree, vars);
  }

  protected static Double[][] parse(ParseTree tree, List<Variable> vars) throws Exception
  {
    Double[][] ret = new Double[vars.size()][2];
    boolean[] varFound = new boolean[vars.size()];
    for (int i = 0; i < varFound.length; i++) {
      varFound[i] = false;
    }

    List<Term> params = new ArrayList<>();
    for (ParseTree child : ((RangesParser.ProgContext) tree).children) {
      boolean lowerBound=true;
      int idx=-1;
      for (ParseTree node : ((RangesParser.RangeContext) child).children) {

        if (!StringUtils.equals(node.getText(), "[") && !StringUtils.equals(node.getText(), "]")
            && !StringUtils.equals(node.getText(), ",") && !StringUtils.equals(node.getText(), ":")) {
          if(!NumberUtils.isNumber(node.getText())) {
            for (int i = 0; i < vars.size(); i++) {
              if(StringUtils.equals(vars.get(i).getName(), node.getText())) {
                idx = i;
                varFound[i] = true;
                break;
              }
            }
            if(idx < 0) {
              throw new RuntimeException("Variable "+node.getText()+" has not been found in the problem " +
                  "definition");
            }
          } else if(lowerBound && NumberUtils.isNumber(node.getText())) {
            ret[idx][0] = Double.parseDouble(node.getText());
            lowerBound = false;
          } else if(NumberUtils.isNumber(node.getText())) {
            ret[idx][1] = Double.parseDouble(node.getText());
          }
        }
      }
    }
    for (int i = 0; i < varFound.length; i++) {
      if(!varFound[i]) {
        throw new RuntimeException("No interval for variable "+vars.get(i).getName()+" defined");
      }
    }
    return ret;
  }
}
