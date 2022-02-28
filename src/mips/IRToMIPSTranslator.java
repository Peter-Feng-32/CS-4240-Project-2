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
    public static MIPSProgram translate(IRProgram irp) throws Exception {
        MIPSProgram mipsProgram = new MIPSProgram();
        for (IRFunction func : irp.functions) {
            MIPSSubroutine mipsSub = new MIPSSubroutine();

            mipsSub.name = func.name;
            //Map array to offset from frame pointer
            //Keep running frame pointer offset
            //

            //Q? Are there other mips types (arrays?) i think we can just keep everything as words, but not sure


            mipsSub.returnType = MIPSWordType.get();
            //dynamically assign variables new registers
            HashMap<String, Integer> regMap = new HashMap<>();
            //start assigning registers for values - will need to load these from stack in regAlloc
            int regCount = 1;
            mipsSub.parameters = new ArrayList<MIPSRegisterOperand>();
            for (IRVariableOperand ivo : func.parameters) {
                mipsSub.parameters.add(new MIPSRegisterOperand(regCount, ivo.getName(), null, true));
                regMap.put(ivo.getName(), regCount);
                regCount++;
            }
            mipsSub.variables = new ArrayList<MIPSRegisterOperand>();
            for (IRVariableOperand ivo : func.variables) {
                //todo: Handle arrays that are declared here by allocating space on the stack.

            }


            mipsSub.instructions = new ArrayList<MIPSInstruction>();
            //todo: Initialize Subroutine with a label?
            //Calling convention for callee goes here...


            for (IRInstruction iri : func.instructions) {
                ArrayList<MIPSInstruction> newInstructions = translateInstruction(iri, regCount, regMap);
                for(MIPSInstruction mipsi : newInstructions) {
                    mipsSub.instructions.add(mipsi);
                }
                regCount++;

                // note no guarantee register numbers are continuous bc incremented on every instruction
            }
            mipsProgram.subroutines.add(mipsSub);
        }
        return mipsProgram;
    }
    private static ArrayList<MIPSInstruction> translateInstruction(IRInstruction iri, int regCount, HashMap<String, Integer> regMap) throws Exception {
        ArrayList<MIPSInstruction> newInstructions = new ArrayList<>();
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
                mipsI.operands[i] = new MIPSRegisterOperand(regMap.get(s), s, null, true);
            }
        }
        //todo: question: What if we need multiple temp registers to implement an instruction?
        //todo: restructure so register count is assigned when registers are actually allocated, not just 1 per IR instruction.  Java pass regCount by reference using wrapper.


        /*
        When something is a variable, we directly assign a temp register to it or read from a mapped temp register depending on whether we are reading or writing.
         */


        switch(iri.opCode) {
             case ASSIGN:
                 //Assign Constant

                 if(iri.operands.length == 2 && iri.operands[1] instanceof IRConstantOperand){
                     mipsI.opCode = MIPSInstruction.OpCode.LI;
                     newDestReg(mipsI, regCount, regMap);
                     newInstructions.add(mipsI);
                 }
                //Assign Register Value

                 else if (iri.operands.length == 2 && iri.operands[1] instanceof IRVariableOperand) {
                     //Turn "Assign a b" where b is a register into ADDI a b 0.
                     MIPSOperand[] newMIPSOps = new MIPSOperand[3];
                     newMIPSOps[0] = mipsI.operands[0];
                     newMIPSOps[1] = mipsI.operands[1];
                     newMIPSOps[2] = new MIPSConstantOperand(MIPSWordType.get(), "0", null);
                     mipsI.opCode = MIPSInstruction.OpCode.ADDI;
                     newDestReg(mipsI, regCount, regMap);
                     newInstructions.add(mipsI);
                 }
                //Q? todo: implement array assign

                 //Array Assign - Leave until calling convention lecture?
                 else if (iri.operands.length == 3) {
                     newDestReg(mipsI, regCount, regMap);
                     newInstructions.add(mipsI);

                 } else {
                     throw new Exception("TIGER-IR ASSIGN has more than 3 operands!");
                 }

                break;
             case ADD:
                 /* todo:
                 * ADD t, a, b
                 * Case a == variable, b == variable -> ADD t, a, b
                 * Case a == variable, b == constant, b fits immediate value size(16 bits) -> ADDI, t, a, b
                 * Case a == variable, b == constant, b doesn't fit immediate value size -> LUI temp, b>>16, (ORI b << 16) >> 16; ADD, t, a, b
                 * Case a == constant, b == constant -> LI t, a+b
                 * */
                 if (mipsI.operands[2] instanceof MIPSConstantOperand || mipsI.operands[1] instanceof MIPSConstantOperand) {
                     mipsI.opCode = MIPSInstruction.OpCode.ADDI;
                 } else {
                     mipsI.opCode = MIPSInstruction.OpCode.ADD;
                 }
                 newDestReg(mipsI, regCount, regMap);
                 newInstructions.add(mipsI);

                 break;
             case SUB:
                 /* todo:
                  * SUB t, a, b
                  * Case a == variable, b == variable -> SUB t, a, b
                  * Case a == variable, b == constant, b fits immediate value size(16 bits) -> SUBI, t, a, b
                  * Case a == variable, b == constant, b doesn't fit immediate value size -> LUI temp, b>>16, (ORI b << 16) >> 16; SUB, t, a, b
                  * Case a == constant, b == constant -> LI t, a-b
                  * */


                 if (mipsI.operands[2] instanceof MIPSConstantOperand || mipsI.operands[1] instanceof MIPSConstantOperand) {
                     mipsI.opCode = MIPSInstruction.OpCode.SUBI;
                 } else {
                     mipsI.opCode = MIPSInstruction.OpCode.SUB;
                 }
                 newDestReg(mipsI, regCount, regMap);
                 newInstructions.add(mipsI);

                 break;
             case MULT:
                 //todo: implement once figured out what to do with overflow
                 // TBD

                 mipsI.opCode = MIPSInstruction.OpCode.MULT;
                 newDestReg(mipsI, regCount, regMap);
                 newInstructions.add(mipsI);

                 break;
             case DIV:
                 /* todo:
                  * DIV t, a, b
                  * Case a == variable, b == variable -> DIV t, a, b
                  * Case a == variable, b == constant, b fits immediate value size(16 bits) -> DIVI, t, a, b
                  * Case a == variable, b == constant, b doesn't fit immediate value size -> LUI temp, b>>16, (ORI b << 16) >> 16; DIV, t, a, b
                  * Case a == constant, b == constant -> LI t, a/b
                  * */

                 mipsI.opCode = MIPSInstruction.OpCode.DIV;
                 newDestReg(mipsI, regCount, regMap);
                 newInstructions.add(mipsI);

                 break;
             case AND:
                 /* todo:
                  * AND t, a, b
                  * Case a == variable, b == variable -> AND t, a, b
                  * Case a == variable, b == constant, b fits immediate value size(16 bits) -> ANDI, t, a, b
                  * Case a == variable, b == constant, b doesn't fit immediate value size -> LUI temp, b>>16, (ORI b << 16) >> 16; AND, t, a, b
                  * Case a == constant, b == constant -> LI t, a&b
                  * */

                 if (mipsI.operands[2] instanceof MIPSConstantOperand || mipsI.operands[1] instanceof MIPSConstantOperand) {
                     mipsI.opCode = MIPSInstruction.OpCode.ANDI;
                 } else {
                     mipsI.opCode = MIPSInstruction.OpCode.AND;
                 }
                 newDestReg(mipsI, regCount, regMap);
                 newInstructions.add(mipsI);

                 break;
             case OR:

                 /* todo:
                  * OR t, a, b
                  * Case a == variable, b == variable -> OR t, a, b
                  * Case a == variable, b == constant, b fits immediate value size(16 bits) -> ORI, t, a, b
                  * Case a == variable, b == constant, b doesn't fit immediate value size -> LUI temp, b>>16, (ORI b << 16) >> 16; OR, t, a, b
                  * Case a == constant, b == constant -> LI t, a|b
                  * */

                 if (mipsI.operands[2] instanceof MIPSConstantOperand || mipsI.operands[1] instanceof MIPSConstantOperand) {
                     mipsI.opCode = MIPSInstruction.OpCode.ORI;
                 } else {
                     mipsI.opCode = MIPSInstruction.OpCode.OR;
                 }
                 newDestReg(mipsI, regCount, regMap);
                 newInstructions.add(mipsI);

                 break;
             case BREQ:
                 /* todo:
                  * BREQ, label, y, z
                  * Case y == variable, z == variable -> BEQ, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BEQ label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BEQ label, temp, z
                    Case y == immediate, z == immediate -> LI temp1, y; LI temp2, z; BEQ label, temp1, temp2;
                  * */

                 mipsI.opCode = MIPSInstruction.OpCode.BEQ;
                 newInstructions.add(mipsI);

                 break;
             case BRNEQ:
                 /* todo:
                  * BRNEQ, label, y, z
                  * Case y == variable, z == variable -> BNE, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BNE label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BNE label, temp, z
                    Case y == immediate, z == immediate -> LI temp1, y; LI temp2, z; BNE label, temp1, temp2;
                  * */
                 mipsI.opCode = MIPSInstruction.OpCode.BNE;
                 newInstructions.add(mipsI);

                 break;
             case BRLT:
                 /* todo:
                  * BRLT, label, y, z
                  * Case y == variable, z == variable -> BLT, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BLT label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BLT label, temp, z
                    Case y == immediate, z == immediate -> LI temp1, y; LI temp2, z; BLT label, temp1, temp2;
                  * */
                 mipsI.opCode = MIPSInstruction.OpCode.BLT;
                 newInstructions.add(mipsI);

                 break;
             case BRGT:
                 /* todo:
                  * BRGT, label, y, z
                  * Case y == variable, z == variable -> BGT, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BGT label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BGT label, temp, z
                    Case y == immediate, z == immediate -> LI temp1, y; LI temp2, z; BGT label, temp1, temp2;
                  * */
                 mipsI.opCode = MIPSInstruction.OpCode.BGT;
                 newInstructions.add(mipsI);
                 break;
             case BRLEQ:
                 /* todo:
                  * BRLEQ, label, y, z
                  * Case y == variable, z == variable -> BLE, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BLE label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BLE label, temp, z
                    Case y == immediate, z == immediate -> LI temp1, y; LI temp2, z; BLE label, temp1, temp2;
                  * */
                 mipsI.opCode = MIPSInstruction.OpCode.BLE;
                 newInstructions.add(mipsI);
                 break;
             case BRGEQ:
                 /* todo:
                  * BRGEQ, label, y, z
                  * Case y == variable, z == variable -> BGE, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BGE label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BGE label, temp, z
                    Case y == immediate, z == immediate -> LI temp1, y; LI temp2, z; BGE label, temp1, temp2;
                  * */
                 mipsI.opCode = MIPSInstruction.OpCode.BGE;
                 newInstructions.add(mipsI);
                 break;

             case GOTO:
                 /* todo:
                  * GOTO label -> B, label
                  * */
                mipsI.opCode = MIPSInstruction.OpCode.BGE;
                break;
             case RETURN:
                 /*
                 todo: Implement once we figure out how the stack is structured.
                  */
                 mipsI.opCode = MIPSInstruction.OpCode.JR;
                 newInstructions.add(mipsI);

                 break;
             case CALL:
                 /*
                 todo: Implement once we figure out how the stack is structured.
                  */
                 //Translate intrinsic functions from IR
                 //Case analysis.
                 mipsI.opCode = MIPSInstruction.OpCode.JAL;
                 newInstructions.add(mipsI);

                 break;
             case CALLR:
                 /*
                 todo: Implement once we figure out how the stack is structured.
                  */
                 //Translate intrinsic functions from IR
                 //Case analysis.

                 mipsI.opCode = MIPSInstruction.OpCode.JAL;
                 newInstructions.add(mipsI);
                 break;
             case ARRAY_STORE:
                 /*
                 todo:
                 ARRAY_STORE, x, array_name, offset
                 In SW, x has to be a register, offset must be an integer value, and the base must be a register.
                 CASE x == immediate, offset == immediate -> LI temp, x;  SW, temp, offset * 4(array_name)
                 CASE x == immediate, offset == variable -> LI temp1, x; SLL, temp2, offset, 2;  ADD, temp3, temp2, array_name;  SW, temp1, 0(temp3)
                 CASE x == variable, offset == immediate -> SW, x, offset * 4(array_name)
                 CASE x == variable, offset == variable -> SLL, temp1, offset, 2;  ADD, temp2, temp1, array_name;  SW, x, 0(temp2)
                  */
                 mipsI.opCode = MIPSInstruction.OpCode.SW;
                 newInstructions.add(mipsI);

                 break;
             case ARRAY_LOAD:
                 /*
                 todo:
                 ARRAY_LOAD, x, array_name, offset
                 In LW, x has to be a register, offset must be an integer value, and the base must be a register.
                 CASE x == immediate, offset == immediate -> LI temp, x;  LW, temp, offset * 4(array_name)
                 CASE x == immediate, offset == variable -> LI temp1, x; SLL, temp2, offset, 2;  ADD, temp3, temp2, array_name;  LW, temp1, 0(temp3)
                 CASE x == variable, offset == immediate -> LW, x, offset * 4(array_name)
                 CASE x == variable, offset == variable -> SLL, temp1, offset, 2;  ADD, temp2, temp1, array_name;  LW, x, 0(temp2)
                  */


                mipsI.opCode = MIPSInstruction.OpCode.LW;
                newInstructions.add(mipsI);

                 break;
             case LABEL:
                 //Directly corresponds.  Label -> Label should be fine.
                 mipsI.opCode = MIPSInstruction.OpCode.LABEL;
                 newInstructions.add(mipsI);
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
        // GOTO -> B
        // return -> jr $ra
        // call -> jal
        // callr -> jal
        // ARRAY_STORE -> sw
        // ARRAY_LOAD -> lw
        // LABEL -> LABEL
        return newInstructions;
    }
    private static void newDestReg(MIPSInstruction m, int rc, HashMap<String, Integer> rm) {
        ((MIPSRegisterOperand) m.operands[0]).regNum = rc;
        rm.put(((MIPSRegisterOperand) m.operands[0]).getValueString(), rc);
    }
}
