package AutoDiff;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by awitsch on 18.11.16.
 */
public class AutoDiffTests
{

  @Test
  public void onVariableDiffTest() throws Exception
  {
    Variable x = new Variable();
    Variable[] vars = {x};
    Term expression = x;

    ICompiledTerm comp = TermUtils.compile(expression, vars);
    expression.toString();
    comp.toString();

    Assert.assertEquals(2, TermUtils.evaluate(expression, vars, new double[]{2}), 0.001);
    Assert.assertEquals(1, TermUtils.differentiate(expression, vars, new double[]{2})[0], 0.001);

    Tuple<Double[], Double> res = comp.differentiate(4);
    Assert.assertEquals(4, res.getItem2(), 0.001);
    Assert.assertEquals(1, res.getItem1()[0], 0.001);
  }

  @Test
  public void baseDiffTest() throws Exception
  {
    Variable x = new Variable();
    Variable[] vars = {x};
    Term expression = x.multiply(TermBuilder.Constant(12.5));

    ICompiledTerm comp = TermUtils.compile(expression, vars);
    Assert.assertEquals(25, TermUtils.evaluate(expression, vars, new double[]{2}), 0.001);
    Assert.assertEquals(12.5, TermUtils.differentiate(expression, vars, new double[]{2})[0], 0.001);

    expression.toString();
    comp.toString();

    Tuple<Double[], Double> res = comp.differentiate(4);
    Assert.assertEquals(50, res.getItem2(), 0.001);
    Assert.assertEquals(12.5, res.getItem1()[0], 0.001);
  }


  @Test
  public void twoVariableTest() throws Exception
  {
    Variable x = new Variable();
    Variable y = new Variable();
    Variable[] vars = {x, y};
    Term expression = x.multiply(y);

    ICompiledTerm comp = TermUtils.compile(expression, vars);
    Assert.assertEquals(8, TermUtils.evaluate(expression, vars, new double[]{2,4}), 0.001);
    Assert.assertEquals(4, TermUtils.differentiate(expression, vars, new double[]{2,4})[0], 0.001);

    expression.toString();
    comp.toString();

    Tuple<Double[], Double> res = comp.differentiate(5,10);
    Assert.assertEquals(50, res.getItem2(), 0.001);
    Assert.assertEquals(10, res.getItem1()[0], 0.001);
    Assert.assertEquals(5, res.getItem1()[1], 0.001);
  }

  @Test
  public void powerTest() throws Exception
  {
    Variable x = new Variable();
    Variable[] vars = {x};
    Term expression = TermBuilder.Power(x, TermBuilder.Constant(2)).multiply(TermBuilder.Constant(2));

    expression.toString();

    Assert.assertEquals(8, TermUtils.evaluate(expression, vars, new double[]{2}), 0.001);
    Assert.assertEquals(8, TermUtils.differentiate(expression, vars, new double[]{2})[0], 0.001);
  }

  @Test
  public void DiffQuadraticTwoVars() throws Exception
  {
    Variable x = new Variable();
    Variable y = new Variable();

    // f(x, y) = 2x² - 3y²
    Term twoXSquared = TermBuilder.Constant(2).multiply(TermBuilder.Power(x, 2));
    Term threeYSquared = TermBuilder.Constant(3).multiply(TermBuilder.Power(y, 2));
    Term func = twoXSquared.substract(threeYSquared);
    Double[] ret = TermUtils.differentiate(func, new Variable[]{x,y}, new double[]{2,3});

    func.toString();

    // df(x, y) = (4x, -6y)
    Assert.assertEquals(8, ret[0], 0.0001);
    Assert.assertEquals(-18, ret[1], 0.0001);
  }

  @Test
  public void DiffMeanSquaredError() throws Exception
  {
    Variable x = new Variable();
    Variable y = new Variable();

    // f(x, y) = 0.5*((x - 5)² + (x + 2)²)
    Term left= TermBuilder.Power(x.substract(TermBuilder.Constant(5)), 2);
    Term right=TermBuilder.Power(y.add(TermBuilder.Constant(2)), 2);
    Term func = TermBuilder.Constant(0.5).multiply(left.add(right));

    Double[] grad1 = TermUtils.differentiate(func, new Variable[]{x,y}, new double[]{5,-2});
    Double[] grad2 = TermUtils.differentiate(func, new Variable[]{x,y}, new double[]{3,1});

    func.toString();

    // df(x, y) = [x-5, x+2]
    Assert.assertEquals(0, grad1[0], 0.0001);
    Assert.assertEquals(0, grad1[1], 0.0001);

    Assert.assertEquals(-2, grad2[0], 0.0001);
    Assert.assertEquals(3, grad2[1], 0.0001);
  }



  @Test
  public void DiffRational() throws Exception
  {
    Variable x = new Variable();
    Variable y = new Variable();

    // f(x, y) = (x² - xy + y²) / (x + y)
    Term nom =(TermBuilder.Power(x, 2).substract(x.multiply(y))).add(TermBuilder.Power(y, 2));
    Term denom = x.add(y);
    Term func = nom.divide(denom);

    func.toString();

    // df(1,4) = [-0.92, 0.88]
    // df(-6,4) = [-11, -26]
    Double[] grad1 = TermUtils.differentiate(func, new Variable[]{x,y}, new double[]{1,4});
    Double[] grad2 = TermUtils.differentiate(func, new Variable[]{x,y}, new double[]{-6,4});


    Assert.assertEquals(-0.92, grad1[0], 0.0001);
    Assert.assertEquals(0.88, grad1[1], 0.0001);

    Assert.assertEquals(-11, grad2[0], 0.0001);
    Assert.assertEquals(-26, grad2[1], 0.0001);
  }

  @Test
  public void DiffPolynomial() throws Exception
  {
    Variable x = new Variable();
    Variable y = new Variable();
    Variable z = new Variable();

    // f(x,y,z) = 2(x-y)² + 5xy - 3y²
    Term left = TermBuilder.Constant(2).multiply(TermBuilder.Power(x.substract(y), 2));
    Term middle = TermBuilder.Constant(5).multiply(x).multiply(y);
    Term right = TermBuilder.Constant(3).multiply(TermBuilder.Power(y, 2));
    Term func = left.add(middle).substract(right);
    ICompiledTerm diff = TermUtils.compile(func, new Variable[] { x, y, z });

    func.toString();
    diff.toString();

    Tuple<Double[], Double> res = diff.differentiate(1, 2, -3);


    Assert.assertEquals(6, res.getItem1()[0], 0.0001);
    Assert.assertEquals(-3, res.getItem1()[1], 0.0001);
    Assert.assertEquals(0, res.getItem1()[2], 0.0001);
    Assert.assertEquals(0, res.getItem2(), 0.0001);
  }


  @Test
  public void DiffExp() throws Exception
  {
    Variable x = new Variable();

    Term func = TermBuilder.Exp(x);

    Double[] grad1 = TermUtils.differentiate(func, new Variable[]{x}, new double[]{1});
    Double[] grad2 = TermUtils.differentiate(func, new Variable[]{x}, new double[]{-2});

    func.toString();

    Assert.assertEquals(Math.exp(1), grad1[0], 0.0001);
    Assert.assertEquals(Math.exp(-2), grad2[0], 0.0001);
  }

  @Test
  public void DiffTermPower() throws Exception
  {
    Variable x = new Variable();
    Variable y = new Variable();

    Term func = TermBuilder.Power(x, y);

    Double[] grad1 = TermUtils.differentiate(func, new Variable[]{x,y}, new double[]{2, 3});

    func.toString();

    Assert.assertEquals(12, grad1[0], 0.0001);
    Assert.assertEquals(8*Math.log(2), grad1[1], 0.0001);
  }

  @Test
  public void DiffTermPowerSingle() throws Exception
  {
    Variable x = new Variable();

    Term func = TermBuilder.Power(x, x);

    Double[] grad1 = TermUtils.differentiate(func, new Variable[]{x}, new double[]{2.5});

    func.toString();

    Assert.assertEquals(Math.pow(2.5, 2.5)*(Math.log(2.5)+1), grad1[0], 0.0001);
  }

}
