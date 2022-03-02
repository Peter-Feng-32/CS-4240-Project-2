package mips.operand;

import mips.MIPSInstruction;
import mips.datatype.MIPSType;

public class MIPSRegisterOperand extends MIPSOperand {


    public int regNum;
    public boolean virtual;

    //If virtual, then name will be name of the virtual register and regNum will be the virtual register number
    //If non-virtual then name will be the name of the real register and regNum is irrelevant
    public MIPSRegisterOperand(int regNum, String name, Boolean virtual) {
        super(name);
        this.regNum = regNum;
        this.virtual = virtual;
    }

    public String getName() {
        return name;
    }
    public int getRegNum() {
        return regNum;
    }
    public boolean isVirtual() {
        return isVirtual();
    }
}
