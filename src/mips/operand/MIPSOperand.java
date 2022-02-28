package mips.operand;

import mips.MIPSInstruction;

public abstract class MIPSOperand {

    protected String value;

    protected MIPSInstruction parent;

    public MIPSOperand(String value, MIPSInstruction parent) {
        this.value = value;
        this.parent = parent;
    }

    public MIPSInstruction getParent() {
        return parent;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
