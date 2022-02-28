package mips.operand;

import mips.MIPSInstruction;
import mips.datatype.MIPSType;

public class MIPSRegisterOperand extends MIPSOperand {


    public int regNum;
    public boolean virtual;

    public MIPSRegisterOperand(int regNum, String value, MIPSInstruction parent, Boolean virtual) {
        super(value, parent);
        this.regNum = regNum;
        this.virtual = virtual;
    }

    public String getValueString() {
        return value;
    }
}
