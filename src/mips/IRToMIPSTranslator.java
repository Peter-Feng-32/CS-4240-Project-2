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

            /*Add instructions to perform calling convention
            Preserved registers: $s0 - $s7, $sp, $ra - saved by function called
            Unpreserved registers: $t0 - $t9, $a0 - $a3, $v0 - $v1 - saved by caller
            When entering a function:
            1) Save previous frame pointer $fp
            2) Set current frame pointer $fp to point to the top of the stack
            3) Save $ra
            4) Save $s0 - $s7


            When calling a function:
            1) Save $a0 - $a3
            2) Save $t0 - $t9
            3) Save $v0
            4) Push arguments in order(ie. the earliest parameter will be "lowest" on the stack (have the highest address).
            5) Call function

            6) If Callr Load $v0 into a temporary register
            7) Load $v0
            8) Load $t0 - $t9
            9) Load $a0 - $a3
            10) Pop from stack


            Teardown:
            1) Pop $s0 - $s7
            2) Load $ra
            3) Restore $sp ($sp = $fp + 4)
            4) Restore caller's $fp
            5) Return


            */

            /*
            todo:
            Procedure:
            Enter function - Done
            Assign a virtual register for every parameter - Done
            Assign a virtual register for every variables - Done
            Initialize subroutine with a label - Done
            Add instructions to perform calling convention procedure when entering the function - Done
            Add instructions to load parameters into their virtual registers - Done
            Create an array to frame pointer offset table for every array - Done
                In instruction translation of array ops, load the address of the array into the virtual register for the array variable by adding the offset to the frame pointer
                Then add the offset and perform the operation
            Add instructions to perform teardown
            Do Instruction Translation
             */

            MIPSSubroutine mipsSub = new MIPSSubroutine();
            mipsSub.name = func.name;
            mipsSub.returnType = (func.returnType == null) ? null : MIPSWordType.get();
            mipsSub.instructions = new ArrayList<MIPSInstruction>();

            HashMap<String, Integer> varToRegMap = new HashMap<>();
            HashMap<String, Integer> arrayToFPOffsetMap = new HashMap<>();
            int regCount = 0;

            //Assign a virtual register for every parameter.
            mipsSub.parameters = new ArrayList<MIPSRegisterOperand>();
            for (IRVariableOperand ivo : func.parameters) {
                mipsSub.parameters.add(new MIPSRegisterOperand(regCount, ivo.getName(), true));
                varToRegMap.put(ivo.getName(), regCount);
                regCount++;
            }

            //Add a virtual register for every variable
            for(IRVariableOperand ivo: func.variables) {
                varToRegMap.put(ivo.getName(), regCount);
                regCount++;
            }

            // Initialize Subroutine with a label
            MIPSInstruction lbl = new MIPSInstruction();
            MIPSOperand t_arr[] = new MIPSOperand[1];
            t_arr[0] = new MIPSLabelOperand(MIPSWordType.get(), mipsSub.name);
            lbl.operands = t_arr;
            lbl.opCode = MIPSInstruction.OpCode.LABEL;
            mipsSub.instructions.add(lbl);

            //Note: to store something, push $sp by -4 and then store the word at the new $sp.

            /*
            Add instructions to perform calling convention
            Preserved registers: $s0 - $s7, $sp, $ra - saved by function called
            Unpreserved registers: $t0 - $t9, $a0 - $a3, $v0 - $v1 - saved by caller
            When entering a function:
            1) Save previous frame pointer $fp
            2) Set current frame pointer $fp to point to the top of the stack
            3) Save $ra
            4) Save $s0 - $s7

            */

            //Add instructions to perform calling convention
            //1) Save previous frame pointer $fp
            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW,
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "$fp", false), new MIPSConstantOperand("-4", -4), new MIPSRegisterOperand(-1, "$sp", false)}));
            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI,
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), new MIPSConstantOperand("-4", -4)}));
            //2) Set current frame pointer $fp to point to the top of the stack
            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI,
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "fp", false), new MIPSRegisterOperand(-1, "$sp", false), new MIPSConstantOperand("0", 0)}));
            //3) Save $ra
            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW,
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "$ra", false), new MIPSConstantOperand("-4", -4), new MIPSRegisterOperand(-1, "$sp", false)}));
            //4) Save $s0 - $s7
            for(int i = 0; i <= 7; i++) {
                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW,
                        new MIPSOperand[]{new MIPSRegisterOperand(-1, "$s" + i, false), new MIPSConstantOperand("" + (-8 + -4 * i), (-8 + -4 * i)), new MIPSRegisterOperand(-1, "$sp", false)}));
            }
            //Set stack pointer to correct position
            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI,
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), new MIPSConstantOperand("-36", -36)}));


            //Add instructions to load parameters into virtual registers.
            for (int i = 0; i < func.parameters.size(); i++) {
                //Get correct virtual register number
                IRVariableOperand ivo = func.parameters.get(i);
                int vRegNum = varToRegMap.get(ivo.getName());
                int K = func.parameters.size();
                //Add instruction to load
                //K total parameters
                //LW $vi, 4 + (K - 1 - i) * 4, $fp
                int paramFPOffset = 4 + (K-1 - i) * 4;
                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW,
                        new MIPSOperand[]{new MIPSRegisterOperand(vRegNum, "$vir" + vRegNum, true), new MIPSConstantOperand("" + paramFPOffset, paramFPOffset), new MIPSRegisterOperand(-1, "$fp", false)}));
            }

            //Create an array to frame pointer offset table for every array.
            int fpRunningOffset = -36; //offset by $ra and $s0 - $s7

            for(IRVariableOperand ivo: func.variables) {
                if(ivo.type instanceof IRArrayType) {
                    int arraySize = ((IRArrayType) ivo.type).getSize();
                    fpRunningOffset -= (arraySize * 4);
                    //Allocate space in stack
                    mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI,
                            new MIPSOperand[]{new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), new MIPSConstantOperand("" + (-4*arraySize), (-4*arraySize))}));
                    arrayToFPOffsetMap.put(ivo.getName(), fpRunningOffset);
                }
            }

            //Do Instruction Translation
            for (IRInstruction iri : func.instructions) {
                ArrayList<MIPSInstruction> newInstructions = translateInstruction(iri, varToRegMap);
                for(MIPSInstruction mipsi : newInstructions) {
                    mipsSub.instructions.add(mipsi);
                }
            }

            //Add instructions to perform Teardown
            //1) Load $s0 - $s7
            for(int i = 0; i <= 7; i++) {
                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW,
                        new MIPSOperand[]{new MIPSRegisterOperand(-1, "$s" + i, false), new MIPSConstantOperand("" + (-8 + -4 * i), (-8 + -4 * i)), new MIPSRegisterOperand(-1, "$fp", false)}));
            }
            //2) Load $ra (LW $ra, -4, $fp)
            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW,
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "$ra", false), new MIPSConstantOperand("" + -4, -4), new MIPSRegisterOperand(-1, "$fp", false)}));
            //3) Restore $sp ($sp = $fp + 4)
            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW,
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "$sp", false), new MIPSConstantOperand("" + 4, 4), new MIPSRegisterOperand(-1, "$fp", false)}));
            //4) Restore caller's $fp
            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW,
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "$fp", false), new MIPSConstantOperand("" + 0, 0), new MIPSRegisterOperand(-1, "$fp", false)}));
            //5) Return
            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.JR,
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "$ra", false)}));

            //Done
            mipsProgram.subroutines.add(mipsSub);
        }
        return mipsProgram;
    }
    private static ArrayList<MIPSInstruction> translateInstruction(IRInstruction iri, HashMap<String, Integer> regMap) throws Exception {
        ArrayList<MIPSInstruction> newInstructions = new ArrayList<>();
        MIPSInstruction mipsI = new MIPSInstruction();

        //All of the below stuff will be removed and replaced with specific code within the switch statement per case.

        /**
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
        **/

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
                 String name = ((IRVariableOperand) iri.operands[0]).getName();
                 // Case Both Constant
                 if (iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRConstantOperand) {
                     //opcode
                     mipsI.opCode = MIPSInstruction.OpCode.LI;
                     //numOperands
                     mipsI.operands = new MIPSOperand[2];
                     //Declare Constant Operand
                     String addVal = "" + (Integer.parseInt(iri.operands[1].toString()) + Integer.parseInt(iri.operands[2].toString()));
                     dclrConstantOp(mipsI, addVal, 1);
                     //Dest Register
                     newDestReg(mipsI, regCount, regMap, name);
                 }
                 // Supercase One Constant
                 else if (iri.operands[2] instanceof IRConstantOperand || iri.operands[1] instanceof IRConstantOperand) {
                     // constant is always second
                     if (iri.operands[1] instanceof IRConstantOperand) {
                         IROperand temp = iri.operands[1];
                         iri.operands[1] = iri.operands[2];
                         iri.operands[2] = temp;
                     }
                     int b = Integer.parseInt(iri.operands[2].toString());
                     // case b doesn't fit in 16 bits (LUI, ORI, ADD)
                     if (b >>> 16 != 0) {
                         //opcode
                         mipsI.opCode = MIPSInstruction.OpCode.LUI;
                         //numOperands
                         mipsI.operands = new MIPSOperand[2];
                         //Declare Constant Operand
                         String val = "" + (b >>> 16);
                         dclrConstantOp(mipsI, val, 1);
                         //Dest Register
                         newDestReg(mipsI, regCount, regMap, name);
                         //add inst
                         newInstructions.add(mipsI);
                         // Move to second new Instruction
                         mipsI = new MIPSInstruction();
                         //opcode
                         mipsI.opCode = MIPSInstruction.OpCode.ORI;
                         //numOperands
                         mipsI.operands = new MIPSOperand[3];
                         //Declare 1st Register Operand
                         dclrRegOp(mipsI, "", 1, regMap.get(name)); //the name (2nd arg in call) doesn't matter, right? I just ignore it here
                         //Declare 2nd Constant Operand
                         val = "" + (b & ((1 << 16) - 1));
                         dclrConstantOp(mipsI, val, 2);
                         //Dest Register
                         newDestReg(mipsI, regCount, regMap, name);
                         //add inst
                         newInstructions.add(mipsI);
                         // Move to actual add instruction
                         mipsI = new MIPSInstruction();
                         //opcode
                         mipsI.opCode = MIPSInstruction.OpCode.ADD;
                         //numOperands
                         mipsI.operands = new MIPSOperand[3];
                         //Declare 1st Register Operand
                         dclrRegOp(mipsI, "", 1, regMap.get(name));
                         //Declare 2nd Register Operand
                         String a = iri.operands[1].toString();
                         dclrRegOp(mipsI, "", 2, regMap.get(a));
                         //Dest Register
                         newDestReg(mipsI, regCount, regMap, name);
                     }
                     // case b fits in 16 bits
                     else {
                         //opcode
                         mipsI.opCode = MIPSInstruction.OpCode.ADDI;
                         //numOperands
                         mipsI.operands = new MIPSOperand[3];
                         //Declare 1st Register Operand
                         dclrRegOp(mipsI, "", 1, regMap.get(iri.operands[1].toString()));
                         //Declare 2nd Constant Operand
                         dclrConstantOp(mipsI, iri.operands[2].toString(), 2);
                         //Dest Register
                         newDestReg(mipsI, regCount, regMap, name);
                     }
                 }
                 // case both operands are variables (add)
                 else {
                     mipsI.opCode = MIPSInstruction.OpCode.ADD;
                     mipsI.operands = new MIPSOperand[3];
                     dclrRegOp(mipsI, "", 1, regMap.get(iri.operands[1].toString()));
                     dclrRegOp(mipsI, "", 2, regMap.get(iri.operands[2].toString()));
                     newDestReg(mipsI, regCount, regMap, name);
                 }
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

                 // Case a == variable, b == variable
                 mipsI.opCode = MIPSInstruction.OpCode.SUB;
                 mipsI.operands = new MIPSOperand[3];
                 dclrRegOp(mipsI, "", 1, regMap)

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
        return newInstructions;
    }
    //creates a new destination register operand
    private static void newDestReg(MIPSInstruction m, WrapInt rc, HashMap<String, Integer> rm, String name) {
        m.operands[0] = new MIPSRegisterOperand(rc.val, name, null, true);
        rm.put(name, rc.val);
        rc.val++;
    }
    //creates a new constant operand
    private static void dclrConstantOp(MIPSInstruction m, String value, int opNum) {
        m.operands[opNum] = new MIPSConstantOperand(MIPSWordType.get(), value, m);
    }
    //creates a new register operand
    private static void dclrRegOp(MIPSInstruction m, String value, int opNum, int regNum) {
        m.operands[opNum] = new MIPSRegisterOperand(regNum, value, m, true);
    }
}
