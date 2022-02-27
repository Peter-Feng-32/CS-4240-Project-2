package mips.operand;

import mips.MIPSInstruction;
import mips.datatype.MIPSType;

public class MIPSRegisterOperand extends MIPSOperand {

    public int regNum;

    public MIPSRegisterOperand(int regNum, String value, MIPSInstruction parent) {
        super(value, parent);
        this.regNum = regNum;
    }

    public String getValueString() {
        return value;
    }
}
