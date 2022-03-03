package mips.operand;

import mips.MIPSInstruction;
import mips.datatype.MIPSType;

public class MIPSLabelOperand extends MIPSOperand {



    public MIPSLabelOperand(String name) {
        super(name);
    }

    public String getValueString() {
        return name;
    }
}
