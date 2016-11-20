using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AutoDiff.Compiled
{
	class Sigmoid : TapeElement
    {
        public int Arg;
        public int Mid;
		public double Steepness;

        public override void Accept(ITapeVisitor visitor)
        {
            visitor.Visit(this);
        }
	}
}
