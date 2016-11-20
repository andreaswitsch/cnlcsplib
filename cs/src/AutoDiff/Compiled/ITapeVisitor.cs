using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff.Compiled
{
    interface ITapeVisitor
    {
        void Visit(Constant elem);
        void Visit(Exp elem);
        void Visit(Log elem);
        void Visit(ConstPower elem);
        void Visit(TermPower elem);
        void Visit(Product elem);
        void Visit(Sum elem);
		void Visit(Gp elem);
        void Visit(Variable var);
		void Visit(Min elem);
        void Visit(Max elem);
        void Visit(And elem);
        void Visit(Or elem);            
        void Visit(Sigmoid elem);
		void Visit(LinSigmoid elem);
        void Visit(LTConstraint elem);
        void Visit(LTEConstraint elem);
        void Visit(ConstraintUtility elem);
        void Visit(Sin elem);
        void Visit(Cos elem);
        void Visit(Abs elem);
        void Visit(Atan2 elem);
        void Visit(Reification elem);        
    }
}
