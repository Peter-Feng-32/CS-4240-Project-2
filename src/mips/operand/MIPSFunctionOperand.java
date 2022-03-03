package mips.operand;

import mips.MIPSInstruction;
import mips.datatype.MIPSType;

public class MIPSFunctionOperand extends MIPSOperand {

    public MIPSType type;

    public MIPSFunctionOperand(MIPSType type, String name) {
        super(name);
        this.type = type;
    }

    public String getValueString() {
        return name;
    }
}
