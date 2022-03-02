package mips.operand;

import mips.MIPSInstruction;

public abstract class MIPSOperand {

    protected String name;

    protected MIPSInstruction parent;

    public MIPSOperand(String name) {
        this.name = name;
    }

    public MIPSInstruction getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
