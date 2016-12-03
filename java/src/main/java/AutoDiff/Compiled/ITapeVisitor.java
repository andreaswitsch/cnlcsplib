//
// Translated by CS2J (http://www.cs2j.com): 12.11.2016 09:40:32
//

package AutoDiff.Compiled;

public interface ITapeVisitor
{
    void visit(Constant elem) throws Exception ;

    void visit(Exp elem) throws Exception ;

    void visit(Log elem) throws Exception ;

    void visit(ConstPower elem) throws Exception ;

    void visit(TermPower elem) throws Exception ;

    void visit(Product elem) throws Exception ;

    void visit(Sum elem) throws Exception ;

    void visit(Variable var) throws Exception ;

    void visit(Min elem) throws Exception ;

    void visit(Max elem) throws Exception ;

    void visit(And elem) throws Exception ;

    void visit(Or elem) throws Exception ;

    void visit(Sigmoid elem) throws Exception ;

    void visit(LinSigmoid elem) throws Exception ;

    void visit(LTConstraint elem) throws Exception ;

    void visit(LTEConstraint elem) throws Exception ;

    void visit(ConstraintUtility elem) throws Exception ;

    void visit(Sin elem) throws Exception ;

    void visit(Cos elem) throws Exception ;

    void visit(Abs elem) throws Exception ;

    void visit(Atan2 elem) throws Exception ;

    void visit(Reification elem) throws Exception ;

}


