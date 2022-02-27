package mips;

import mips.datatype.MIPSType;
// import mips.operand.MIPSVariableOperand;
import mips.operand.MIPSRegisterOperand;

import java.util.List;

public class MIPSSubroutine {

    public String name;

    public MIPSType returnType;

    //I think it might be a good idea to convert parameters and variables into a list of registers?  Or a mapping of variables to registers?
    //Like MIPSRegisterOperand?
    public List<MIPSRegisterOperand> parameters;

    public List<MIPSRegisterOperand> variables;

    public List<MIPSInstruction> instructions;

    public MIPSSubroutine(){}

    public MIPSSubroutine(String name, MIPSType returnType,
                      List<MIPSRegisterOperand> parameters, List<MIPSRegisterOperand> variables,
                      List<MIPSInstruction> instructions) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.variables = variables;
        this.instructions = instructions;
    }
}
