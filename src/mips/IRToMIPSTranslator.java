package mips;
import mips.operand.*;
import mips.datatype.*;
import mips.MIPSInstruction.OpCode.*;
import java.util.*;
import ir.*;
import ir.IRInstruction.OpCode.*;
import ir.datatype.IRArrayType;
import ir.datatype.IRIntType;
import ir.datatype.IRType;
import ir.operand.*;

// read in function list
// iterate through function list
// - convert each instruction to MIPS individually
// Q? - add calling convention before and after function calls? - This should be done in register allocation, after we know num of registers to store?

public class IRToMIPSTranslator {
    public static MIPSProgram translate(IRProgram irp) {
        MIPSProgram mipsProgram = new MIPSProgram();
        for (IRFunction func : irp.functions) {
            MIPSSubroutine mipsSub = new MIPSSubroutine();

            mipsSub.name = func.name;
            //Q? Are there other mips types (arrays?) i think we can just keep everything as words, but not sure
            mipsSub.returnType = MIPSWordType.get();
            //dynamically assign variables new registers
            HashMap<String, Integer> regMap = new HashMap<>();
            //start assigning registers for values - will need to load these from stack in regAlloc
            int regCount = 1;
            mipsSub.parameters = new ArrayList<MIPSRegisterOperand>();
            for (IRVariableOperand ivo : func.parameters) {
                mipsSub.parameters.add(new MIPSRegisterOperand(regCount, ivo.getName(), null));
                regMap.put(ivo.getName(), regCount);
                regCount++;
            }
            mipsSub.variables = new ArrayList<MIPSRegisterOperand>();
            for (IRVariableOperand ivo : func.variables) {
                mipsSub.variables.add(new MIPSRegisterOperand(regCount, ivo.getName(), null));
                regMap.put(ivo.getName(), regCount);
                regCount++;
            }
            mipsSub.instructions = new ArrayList<MIPSInstruction>();
            for (IRInstruction iri : func.instructions) {
                mipsSub.instructions.add(translateInstruction(iri, regCount, regMap));
                regCount++;
                // note no guarantee register numbers are continuous bc incremented on every instruction
            }
            mipsProgram.subroutines.add(mipsSub);
        }
        return mipsProgram;
    }
    private static MIPSInstruction translateInstruction(IRInstruction iri, int regCount, HashMap<String, Integer> regMap) {
        MIPSInstruction mipsI = new MIPSInstruction();
        mipsI.operands = new MIPSOperand[iri.operands.length];
        for (int i = 0; i < iri.operands.length; i++) {
            IROperand ivo = iri.operands[i];
            if (ivo instanceof IRConstantOperand) {
                mipsI.operands[i] = new MIPSConstantOperand(MIPSWordType.get(), ((IRConstantOperand) ivo).getValueString(), null);
            } else if (ivo instanceof IRFunctionOperand) {
                mipsI.operands[i] = new MIPSFunctionOperand(MIPSWordType.get(), ((IRFunctionOperand) ivo).getName(), null);
            } else if (ivo instanceof IRLabelOperand) {
                mipsI.operands[i] = new MIPSLabelOperand(MIPSWordType.get(), ((IRLabelOperand) ivo).getName(), null);
            } else {
                String s = ((IRVariableOperand) ivo).getName();
                mipsI.operands[i] = new MIPSRegisterOperand(regMap.get(s), s, null);
            }
        }
        // Q? are there any duplicate enum problems between MIPSEnum and IREnum. doubt it, but unsure
        switch(iri.opCode) {
             case ASSIGN:
                mipsI.opCode = MIPSInstruction.OpCode.LI;
                newDestReg(mipsI, regCount, regMap);
                //Q? todo: implement array assign
                break;
             case ADD:
                if (mipsI.operands[2] instanceof MIPSConstantOperand || mipsI.operands[1] instanceof MIPSConstantOperand) {
                    mipsI.opCode = MIPSInstruction.OpCode.ADDI;
                } else {
                    mipsI.opCode = MIPSInstruction.OpCode.ADD;
                }
                newDestReg(mipsI, regCount, regMap);
                break;
             case SUB:
                 if (mipsI.operands[2] instanceof MIPSConstantOperand || mipsI.operands[1] instanceof MIPSConstantOperand) {
                     mipsI.opCode = MIPSInstruction.OpCode.SUBI;
                 } else {
                     mipsI.opCode = MIPSInstruction.OpCode.SUB;
                 }
                 newDestReg(mipsI, regCount, regMap);
                 break;
             case MULT:
                mipsI.opCode = MIPSInstruction.OpCode.MULT;
                newDestReg(mipsI, regCount, regMap);
                break;
             case DIV:
                mipsI.opCode = MIPSInstruction.OpCode.DIV;
                newDestReg(mipsI, regCount, regMap);
                break;
             case AND:
                if (mipsI.operands[2] instanceof MIPSConstantOperand || mipsI.operands[1] instanceof MIPSConstantOperand) {
                    mipsI.opCode = MIPSInstruction.OpCode.ANDI;
                } else {
                    mipsI.opCode = MIPSInstruction.OpCode.AND;
                }
                newDestReg(mipsI, regCount, regMap);
                break;
             case OR:
                if (mipsI.operands[2] instanceof MIPSConstantOperand || mipsI.operands[1] instanceof MIPSConstantOperand) {
                    mipsI.opCode = MIPSInstruction.OpCode.ORI;
                } else {
                    mipsI.opCode = MIPSInstruction.OpCode.OR;
                }
                newDestReg(mipsI, regCount, regMap);
                break;
             case BREQ:
                mipsI.opCode = MIPSInstruction.OpCode.BEQ;
                break;
             case BRNEQ:
                mipsI.opCode = MIPSInstruction.OpCode.BNE;
                break;
             case BRLT:
                mipsI.opCode = MIPSInstruction.OpCode.BLT;
                break;
             case BRGT:
                mipsI.opCode = MIPSInstruction.OpCode.BGT;
                break;
             case BRLEQ:
                mipsI.opCode = MIPSInstruction.OpCode.BLE;
                break;
             case BRGEQ:
                mipsI.opCode = MIPSInstruction.OpCode.BGE;
                break;
             case RETURN:
                mipsI.opCode = MIPSInstruction.OpCode.JR;
                break;
             case CALL:
                mipsI.opCode = MIPSInstruction.OpCode.JAL;
                break;
             case CALLR:
                mipsI.opCode = MIPSInstruction.OpCode.JAL;
                break;
             case ARRAY_STORE:
                mipsI.opCode = MIPSInstruction.OpCode.SW;
                break;
             case ARRAY_LOAD:
                mipsI.opCode = MIPSInstruction.OpCode.LW;
                break;
             case LABEL:
                mipsI.opCode = MIPSInstruction.OpCode.LABEL;
                break;
        }
        // assign with two operands -> LI
        // assign with three operands -> loop of sw
        // ADD with constant -> ADDI
        // ADD with two vars -> ADD
        // SUB with constant -> SUBI
        // SUB with two vars -> SUB
        // MULT -> MULT
        // DIV -> DIV
        // AND with constant -> ANDI
        // AND with two vars -> AND
        // OR with constant -> ORI
        // OR with two vars -> OR
        // BRANCHES directly correspond
        // return -> jr $ra
        // call -> jal
        // callr -> jal
        // ARRAY_STORE -> sw
        // ARRAY_LOAD -> lw
        // LABEL -> LABEL
        return mipsI;
    }
    private static void newDestReg(MIPSInstruction m, int rc, HashMap<String, Integer> rm) {
        ((MIPSRegisterOperand) m.operands[0]).regNum = rc;
        rm.put(((MIPSRegisterOperand) m.operands[0]).getValueString(), rc);
    }
}
