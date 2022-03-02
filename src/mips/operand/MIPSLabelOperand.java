package mips.operand;

import mips.MIPSInstruction;
import mips.datatype.MIPSType;

public class MIPSLabelOperand extends MIPSOperand {

    public MIPSType type;

    public MIPSLabelOperand(MIPSType type, String name) {
        super(name);
        this.type = type;
    }

    public String getValueString() {
        return name;
    }
}
