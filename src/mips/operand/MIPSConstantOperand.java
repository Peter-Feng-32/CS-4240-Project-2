package mips.operand;

import mips.MIPSInstruction;
import mips.datatype.MIPSType;

public class MIPSConstantOperand extends MIPSOperand {

    private int value;

    public MIPSConstantOperand(String name, int value) {
        super(name);
        this.value = value;
    }

    public int getValueString() {
        return value;
    }
}
