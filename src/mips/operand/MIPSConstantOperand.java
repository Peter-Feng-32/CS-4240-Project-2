package mips.operand;

import mips.MIPSInstruction;
import mips.datatype.MIPSType;

public class MIPSConstantOperand extends MIPSOperand {

    public MIPSType type;

    public MIPSConstantOperand(MIPSType type, String value, MIPSInstruction parent) {
        super(value, parent);
        this.type = type;
    }

    public String getValueString() {
        return value;
    }
}
