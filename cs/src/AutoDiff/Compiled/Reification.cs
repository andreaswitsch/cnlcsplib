using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff.Compiled
{
	class Reification : TapeElement
    {
        public double Min;
        public double Max;
		public int Condition;
		public int NegatedCondition;

        public override void Accept(ITapeVisitor visitor)
        {
            visitor.Visit(this);
        }
	}
}
