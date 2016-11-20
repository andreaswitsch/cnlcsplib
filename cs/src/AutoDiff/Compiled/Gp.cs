using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UFGP;
using DotNetMatrix;

namespace AutoDiff.Compiled
{
	class Gp : TapeElement
    {
        public int[] Terms;
		public int dc;
		public GaussianProcess Gpr { get; set; }
		

        public override void Accept(ITapeVisitor visitor)
        {
            visitor.Visit(this);
        }
	}
}