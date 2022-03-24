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

    static int tempLabelCounter = 0;

    public static MIPSProgram translate(IRProgram irp) throws Exception {
        MIPSProgram mipsProgram = new MIPSProgram();
        int regCount = 0;
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
            HashMap<String, Boolean> isParameter = new HashMap<>();

//            System.out.println(func.name);

            //Assign a virtual register for every parameter.
            mipsSub.parameters = new ArrayList<MIPSRegisterOperand>();
            for (IRVariableOperand ivo : func.parameters) {
                mipsSub.parameters.add(new MIPSRegisterOperand(regCount, ivo.getName(), true));
                isParameter.put(ivo.getName(), true);
//                System.out.println(ivo.getName() + " " + regCount);
                varToRegMap.put(ivo.getName(), regCount);
                regCount++;
            }

            //todo: get rid of overlap between parameter and variable so we can rework the code to pass pointers for arrays.

            //Add a virtual register for every variable
            for(IRVariableOperand ivo: func.variables) {
                if(!isParameter.getOrDefault(ivo.getName(), false)){
//                    System.out.println(ivo.getName() + " " + regCount);
                    varToRegMap.put(ivo.getName(), regCount);
                    regCount++;
                }

            }

            // Initialize Subroutine with a label
            MIPSInstruction lbl = new MIPSInstruction();
            MIPSOperand t_arr[] = new MIPSOperand[1];
            t_arr[0] = new MIPSLabelOperand(mipsSub.name);
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
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "$fp", false), new MIPSRegisterOperand(-1, "$sp", false), new MIPSConstantOperand("0", 0)}));
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

            //Create an array to frame pointer offset table for every array.
            int fpRunningOffset = -36; //offset by $ra and $s0 - $s7

            for(IRVariableOperand ivo: func.variables) {
                if(ivo.type instanceof IRArrayType && !isParameter.getOrDefault(ivo.getName(), false)) {
                    int arraySize = ((IRArrayType) ivo.type).getSize();
                    fpRunningOffset -= (arraySize * 4);
                    //Allocate space in stack
                    mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                            new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp("" + (-4*arraySize))
                    }));
                    mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADD,
                            new MIPSOperand[]{new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$virFPOffset", true)}));
                    arrayToFPOffsetMap.put(ivo.getName(), fpRunningOffset);
                }
            }
            // Moved to after array creation so all stack pointer manipulation is done before virtual registers are introduced.
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
                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                        new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp("" + paramFPOffset)
                }));
                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[] {
                        new MIPSRegisterOperand(-1, "$virParamBase", true), new MIPSRegisterOperand(-1, "$virFPOffset", true), new MIPSRegisterOperand(-1, "$fp", false)
                }));
                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW,
                        new MIPSOperand[]{new MIPSRegisterOperand(vRegNum, "$vir" + vRegNum, true), new MIPSConstantOperand("" + 0, 0), new MIPSRegisterOperand(-1, "$virParamBase", true)}));
            }

            //Do Instruction Translation
            for (IRInstruction iri : func.instructions) {
                ArrayList<MIPSInstruction> newInstructions = translateInstruction(iri, varToRegMap, arrayToFPOffsetMap, isParameter, func.name);
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
            //3) Restore $sp ($sp = $fp + 4) (ADDI $sp, $fp, 4)
            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI,
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$fp", false), new MIPSConstantOperand("" + 4, 4)}));
            //4) Restore caller's $fp
            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW,
                    new MIPSOperand[]{new MIPSRegisterOperand(-1, "$fp", false), new MIPSConstantOperand("" + 0, 0), new MIPSRegisterOperand(-1, "$fp", false)}));
            //5) Return
            if (func.name.equals("main"))
            {
                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                        new MIPSRegisterOperand(-1, "$v0", false), newConstantOp("10")
                }));
                //Syscall
                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SYSCALL, new MIPSOperand[]{
                }));
            }
            else
            {
                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.JR,
                        new MIPSOperand[]{new MIPSRegisterOperand(-1, "$ra", false)}));
            }
            //Done
            mipsProgram.subroutines.add(mipsSub);
        }

        return mipsProgram;
    }
    private static ArrayList<MIPSInstruction> translateInstruction(IRInstruction iri, HashMap<String, Integer> varToRegMap, HashMap<String, Integer> arrayToFPOffsetMap, HashMap<String, Boolean> isParameter, String funcName) throws Exception {
        ArrayList<MIPSInstruction> newInstructions = new ArrayList<>();

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

        //Currently assuming that we use LI for stuff bigger than 16 bits.  Will be answered in piazza question.
        //https://piazza.com/class/ky0ihqbe9716ox?cid=126

        switch(iri.opCode) {
             case ASSIGN:
                 //Assign Constant - Assign a c -> LI A, C
                 if(iri.operands.length == 2 && iri.operands[1] instanceof IRConstantOperand){
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     newInstructions.add(mipsI);
                 }
                 //Assign Register Value - Assign a b -> ADDI A, B, 0
                 else if (iri.operands.length == 2 && iri.operands[1] instanceof IRVariableOperand) {
                     //Turn "Assign a b" where b is a register into ADDI a b 0.
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[1]).getName()), newConstantOp("0")
                     });
                     newInstructions.add(mipsI);
                 }
                 //Array Assign - Assign x, size, value -> Loop size times, assigning value to all array element in x
                 //Get array pointer by assigning array address to a virtual register?
                 //Use the same $vir_array for all array ops to increase efficiency for code?
                 else if (iri.operands.length == 3 && iri.operands[1] instanceof IRConstantOperand) {
                     int arraySize = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString());

                     //Load array pointer into virtual register
                     //If the array is a local variable use fp offset table
                     //ADDI $virArrayBase $fp, fpOffset
                     //or if the array is a parameter
                     //use the assigned virtual register
                     //ADDI $virArrayBase, $vir, 0
                     if(!isParameter.getOrDefault(((IRVariableOperand)iri.operands[0]).getName(), false)){
                         int fpOffset = arrayToFPOffsetMap.get(((IRVariableOperand)iri.operands[0]).getName());
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp("" + fpOffset)
                         }));
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newNonVirRegOp("$fp"), new MIPSRegisterOperand(-1, "$virFPOffset", true)
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     } else {
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp("0")
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     }

                     //If we have a constant value, we have to put it in a register
                     if (iri.operands[2] instanceof IRConstantOperand) {
                         MIPSInstruction mipsILoadValue = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                             //LI $virArrayValue, constant
                             new MIPSRegisterOperand(-1, "$virArrayValue", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                         });
                         newInstructions.add(mipsILoadValue);
                         for(int i = 0; i < arraySize; i++){
                             //SW virArrayValue, offset, virArrayBase
                            MIPSInstruction mipsIArrayAssign = new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[] {
                                new MIPSRegisterOperand(-1, "$virArrayValue", true), newConstantOp("" + 4 * i), new MIPSRegisterOperand(-1, "$virArrayBase", true)
                            });
                            newInstructions.add(mipsIArrayAssign);
                         }
                     } else { //Otherwise, if we are storing a register's value, we can just use it
                         for(int i = 0; i < arraySize; i++){
                             //SW value, offset, virArrayBase
                             MIPSInstruction mipsIArrayAssign = new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[] {
                                     newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[2]).getName()), newConstantOp("" + 4 * i), new MIPSRegisterOperand(-1, "$virArrayBase", true)
                             });
                             newInstructions.add(mipsIArrayAssign);
                         }
                     }
                 }
                 else if (iri.operands[1] instanceof IRVariableOperand) {

                     if(!isParameter.getOrDefault(((IRVariableOperand)iri.operands[0]).getName(), false)){
                         int fpOffset = arrayToFPOffsetMap.get(((IRVariableOperand)iri.operands[0]).getName());
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp("" + fpOffset)
                         }));
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newNonVirRegOp("$fp"), new MIPSRegisterOperand(-1, "$virFPOffset", true)
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     } else {
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp("0")
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     }

                     //If we have a constant value, we have to put it in a register
                     if (iri.operands[2] instanceof IRConstantOperand) {
                         MIPSInstruction mipsILoadValue = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                                 //LI $virArrayValue, constant
                                 new MIPSRegisterOperand(-1, "$virArrayValue", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                         });
                         newInstructions.add(mipsILoadValue);
                         //Load the array size into a temp variable, and decrease that temp variable until it reaches 0 in a loop of assigns.
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virArrayAssignTempCounter", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[1]).getName()), newConstantOp("0")
                         }));
                         //Start a running offset for the array.
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virArrayAssignRunningOffset", true), newConstantOp("" + 0)
                         }));
                         //To loop an indefinite amount of times setting the array, create a new unique label
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LABEL, new MIPSOperand[]{new MIPSLabelOperand("" +"ARRAY_ASSIGN_START_" + (tempLabelCounter) )}));

                         //Branch to end if temp is equal to zero.
                         MIPSInstruction mipsIBranchTempCreator = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp("0")
                         });
                         newInstructions.add(mipsIBranchTempCreator);
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.BEQ, new MIPSOperand[]{
                                 new MIPSLabelOperand("" + "ARRAY_ASSIGN_END_" + (tempLabelCounter)), new MIPSRegisterOperand(-1, "$virArrayAssignTempCounter", true), new MIPSRegisterOperand(-1, "$virBranchTemp", true)
                         }));

                         //Calculate base+offset
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBasePlusOffset", true), new MIPSRegisterOperand(-1, "$virArrayBase", true), new MIPSRegisterOperand(-1, "$virArrayAssignRunningOffset", true)
                         }));

                         //SW virArrayValue, 0, virArrayBase+offset
                         MIPSInstruction mipsIArrayAssign = new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virArrayValue", true), newConstantOp("" + 0), new MIPSRegisterOperand(-1, "$virArrayBasePlusOffset", true)
                         });
                         newInstructions.add(mipsIArrayAssign);



                         //Subtract 1 from the temp counter
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayAssignTempCounter", true), new MIPSRegisterOperand(-1, "$virArrayAssignTempCounter", true), newConstantOp(""+ -1)
                         }));
                         //Add 4 to the running offset.
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayAssignRunningOffset", true), new MIPSRegisterOperand(-1, "$virArrayAssignRunningOffset", true), newConstantOp(""+ 4)
                         }));
                         //Jump back to start of loop.
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.J, new MIPSOperand[]{
                             new MIPSLabelOperand("" +"ARRAY_ASSIGN_START_" + (tempLabelCounter))
                         }));

                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LABEL, new MIPSOperand[]{new MIPSLabelOperand("" + "ARRAY_ASSIGN_END_" + (tempLabelCounter) )}));



                     } else { //Otherwise, if we are storing a register's value, we can just use it

                         //Load the array size into a temp variable, and decrease that temp variable until it reaches 0 in a loop of assigns.
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virArrayAssignTempCounter", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[1]).getName()), newConstantOp("0")
                         }));
                         //Start a running offset for the array.
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virArrayAssignRunningOffset", true), newConstantOp("" + 0)
                         }));
                         //To loop an indefinite amount of times setting the array, create a new unique label
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LABEL, new MIPSOperand[]{new MIPSLabelOperand("" +"ARRAY_ASSIGN_START_" + (tempLabelCounter) )}));

                         //Branch to end if temp is equal to zero.
                         MIPSInstruction mipsIBranchTempCreator = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp("0")
                         });
                         newInstructions.add(mipsIBranchTempCreator);
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.BEQ, new MIPSOperand[]{
                                 new MIPSLabelOperand("" + "ARRAY_ASSIGN_END_" + (tempLabelCounter)), new MIPSRegisterOperand(-1, "$virArrayAssignTempCounter", true), new MIPSRegisterOperand(-1, "$virBranchTemp", true)
                         }));

                         //Calculate base+offset
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBasePlusOffset", true), new MIPSRegisterOperand(-1, "$virArrayBase", true), new MIPSRegisterOperand(-1, "$virArrayAssignRunningOffset", true)
                         }));

                         //SW virArrayValue, 0, virArrayBase+Offset
                         MIPSInstruction mipsIArrayAssign = new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[] {
                                 newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[2]).getName()), newConstantOp("" + 0), new MIPSRegisterOperand(-1, "$virArrayBasePlusOffset", true)
                         });
                         newInstructions.add(mipsIArrayAssign);



                         //Subtract 1 from the temp counter
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayAssignTempCounter", true), new MIPSRegisterOperand(-1, "$virArrayAssignTempCounter", true), newConstantOp(""+ -1)
                         }));
                         //Add 4 to the running offset.
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayAssignRunningOffset", true), new MIPSRegisterOperand(-1, "$virArrayAssignRunningOffset", true), newConstantOp(""+ 4)
                         }));
                         //Jump back to start of loop.
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.J, new MIPSOperand[]{
                                 new MIPSLabelOperand("" +"ARRAY_ASSIGN_START_" + (tempLabelCounter))
                         }));

                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LABEL, new MIPSOperand[]{new MIPSLabelOperand("" + "ARRAY_ASSIGN_END_" + (tempLabelCounter) )}));


                     }

                     tempLabelCounter++;
                 }
                 else {
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

                 // Case a == variable, b == variable
                 //ADD t, a, b
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsI);
                 }
                 //Case a == variable, b == constant
                 // LI temp, b; ADD t, a, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == variable
                 // LI temp, a; ADD t, temp, b
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == constant
                 //LI, t, a+b
                 else {
                     int toStore = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) + Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newConstantOp("" + toStore)
                     });
                     newInstructions.add(mipsILI);
                 }
                 break;
             case SUB:
                 /* todo:
                  * SUB t, a, b
                  * Case a == variable, b == variable -> SUB t, a, b
                  * Case a == variable, b == constant -> LI temp, b; SUB, t, a, temp
                  * Case a == constant, b == variable -> LI temp, a; SUB t, temp, b
                  * Case a == constant, b == constant -> LI t, a-b
                  * */

                 // Case a == variable, b == variable
                 //SUB t, a, b
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                    MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.SUB, new MIPSOperand[]{
                            newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                    });
                    newInstructions.add(mipsI);
                 }

                 //Case a == variable, b == constant
                 // LI temp, b; SUB t, a, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.SUB, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == variable
                 // LI temp, a; SUB t, temp, b
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.SUB, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == constant
                 //LI, t, a-b
                 else {
                     int toStore = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) - Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp("" + toStore)
                     });
                     newInstructions.add(mipsILI);
                 }

                 break;
             case MULT:
                 /* todo:
                  * MULT t, a, b
                  * Case a == variable, b == variable -> MUL t, a, b
                  * Case a == variable, b == constant -> LI temp, b; MUL, t, a, temp
                  * Case a == constant, b == variable -> LI temp, a; MUL t, temp, b
                  * Case a == constant, b == constant -> LI t, a*b
                  * */

                 // Case a == variable, b == variable
                 //MUL t, a, b
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.MUL, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsI);
                 }

                 //Case a == variable, b == constant
                 // LI temp, b; MUL t, a, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.MUL, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == variable
                 // LI temp, a; MUL t, temp, b
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.MUL, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == constant
                 //LI, t, a*b
                 else {
                     int toStore = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) * Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp("" + toStore)
                     });
                     newInstructions.add(mipsILI);
                 }
                 break;
             case DIV:
                 /* todo:
                  * DIV t, a, b
                  * Case a == variable, b == variable -> DIV t, a, b
                  * Case a == variable, b == constant -> LI temp, b; DIV, t, a, temp
                  * Case a == constant, b == variable -> LI temp, a; DIV t, temp, b
                  * Case a == constant, b == constant -> LI t, a/b
                  * */

                 // Case a == variable, b == variable
                 //DIV t, a, b
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.DIV, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsI);
                 }

                 //Case a == variable, b == constant
                 // LI temp, b; DIV, t, a, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.DIV, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == variable
                 // LI temp, a; DIV t, temp, b
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.DIV, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == constant
                 //LI t, a/b
                 else {
                     int toStore = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) / Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp("" + toStore)
                     });
                     newInstructions.add(mipsILI);
                 }
                 break;
             case AND:
                 /* todo:
                  * AND t, a, b
                  * Case a == variable, b == variable -> AND t, a, b
                  * Case a == variable, b == constant -> LI temp, b; AND, t, a, temp
                  * Case a == constant, b == variable -> LI temp, a; AND t, temp, b
                  * Case a == constant, b == constant -> LI t, a&b
                  * */

                 // Case a == variable, b == variable
                 //AND t, a, b
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.AND, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsI);
                 }

                 //Case a == variable, b == constant
                 // LI temp, b; AND, t, a, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.AND, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == variable
                 // LI temp, a; AND t, temp, b
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.AND, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == constant
                 //LI t, a&b
                 else {
                     int toStore = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) & Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp("" + toStore)
                     });
                     newInstructions.add(mipsILI);
                 }
                 break;
             case OR:
                 /* todo:
                  * OR t, a, b
                  * Case a == variable, b == variable -> OR t, a, b
                  * Case a == variable, b == constant -> LI temp, b; OR, t, a, temp
                  * Case a == constant, b == variable -> LI temp, a; OR t, temp, b
                  * Case a == constant, b == constant -> LI t, a|b
                  * */

                 // Case a == variable, b == variable
                 //OR t, a, b
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.OR, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsI);
                 }

                 //Case a == variable, b == constant
                 // LI temp, b; OR, t, a, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.OR, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == variable
                 // LI temp, a; OR t, temp, b
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArithTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIArith = new MIPSInstruction(MIPSInstruction.OpCode.OR, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), new MIPSRegisterOperand(-1, "$virArithTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIArith);
                 }
                 //Case a == constant, b == constant
                 //LI t, a|b
                 else {
                     int toStore = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) | Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp("" + toStore)
                     });
                     newInstructions.add(mipsILI);
                 }
                 break;
             case BREQ:
                 /* todo:
                  * BREQ, label, y, z
                  * Case y == variable, z == variable -> BEQ, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BEQ label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BEQ label, temp, z
                    Case y == immediate, z == immediate -> if(y == z) B label
                  * */

                 // Case y == variable, z == variable -> BEQ, label, y, z
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.BEQ, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsI);
                 }

                 //Case y == variable, z == immediate -> LI temp, z; BEQ label, y, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BEQ, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virBranchTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == variable -> LI temp, y; BEQ label, temp, z
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BEQ, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName()), new MIPSRegisterOperand(-1, "$virBranchTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == immediate -> if(y == z) B label
                 else {
                     boolean toBranch = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) == Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     if(toBranch) {
                         MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.J, new MIPSOperand[]{
                                 new MIPSLabelOperand(((IRLabelOperand) iri.operands[0]).getName() + "_" + funcName)
                         });
                         newInstructions.add(mipsIBranch);
                     }
                 }
                 break;

             case BRNEQ:
                 /* todo:
                  * BRNEQ, label, y, z
                  * Case y == variable, z == variable -> BNE, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BNE label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BNE label, temp, z
                    Case y == immediate, z == immediate -> if(y == z) B label
                  * */
                 // Case y == variable, z == variable -> BNE, label, y, z
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.BNE, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsI);
                 }

                 //Case y == variable, z == immediate -> LI temp, z; BNE label, y, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BNE, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virBranchTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == variable -> LI temp, y; BNE label, temp, z
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BNE, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), new MIPSRegisterOperand(-1, "$virBranchTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == immediate -> if(y != z) B label
                 else {
                     boolean toBranch = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) != Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     if(toBranch) {
                         MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.J, new MIPSOperand[]{
                                 new MIPSLabelOperand(((IRLabelOperand) iri.operands[0]).getName() + "_" + funcName)
                         });
                         newInstructions.add(mipsIBranch);
                     }
                 }
                 break;
             case BRLT:
                 /* todo:
                  * BRLT, label, y, z
                  * Case y == variable, z == variable -> BLT, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BLT label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BLT label, temp, z
                    Case y == immediate, z == immediate -> if(y < z) B label
                  * */
                 // Case y == variable, z == variable -> BLT, label, y, z
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.BLT, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsI);
                 }

                 //Case y == variable, z == immediate -> LI temp, z; BLT label, y, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BLT, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virBranchTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == variable -> LI temp, y; BLT label, temp, z
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BLT, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), new MIPSRegisterOperand(-1, "$virBranchTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == immediate -> if(y < z) B label
                 else {
                     boolean toBranch = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) < Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     if(toBranch) {
                         MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.J, new MIPSOperand[]{
                                 new MIPSLabelOperand(((IRLabelOperand) iri.operands[0]).getName() + "_" + funcName)
                         });
                         newInstructions.add(mipsIBranch);
                     }
                 }
                 break;
             case BRGT:
                 /* todo:
                  * BRGT, label, y, z
                  * Case y == variable, z == variable -> BGT, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BGT label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BGT label, temp, z
                    Case y == immediate, z == immediate -> if(y > z) B label
                  * */
                 // Case y == variable, z == variable -> BGT, label, y, z
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.BGT, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsI);
                 }

                 //Case y == variable, z == immediate -> LI temp, z; BGT label, y, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BGT, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virBranchTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == variable -> LI temp, y; BGT label, temp, z
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BGT, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), new MIPSRegisterOperand(-1, "$virBranchTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == immediate -> if(y > z) B label
                 else {
                     boolean toBranch = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) > Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     if(toBranch) {
                         MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.J, new MIPSOperand[]{
                                 new MIPSLabelOperand(((IRLabelOperand) iri.operands[0]).getName() + "_" + funcName)
                         });
                         newInstructions.add(mipsIBranch);
                     }
                 }
                 break;
             case BRLEQ:
                 /* todo:
                  * BRLEQ, label, y, z
                  * Case y == variable, z == variable -> BLE, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BLE label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BLE label, temp, z
                    Case y == immediate, z == immediate -> if(y <= z) B label
                  * */
                 // Case y == variable, z == variable -> BLE, label, y, z
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.BLE, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsI);
                 }

                 //Case y == variable, z == immediate -> LI temp, z; BLE label, y, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BLE, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virBranchTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == variable -> LI temp, y; BLE label, temp, z
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BLE, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), new MIPSRegisterOperand(-1, "$virBranchTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == immediate -> if(y <= z) B label
                 else {
                     boolean toBranch = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) <= Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     if(toBranch) {
                         MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.J, new MIPSOperand[]{
                                 new MIPSLabelOperand(((IRLabelOperand) iri.operands[0]).getName() + "_" + funcName)
                         });
                         newInstructions.add(mipsIBranch);
                     }
                 }
                 break;
             case BRGEQ:
                 /* todo:
                  * BRGEQ, label, y, z
                  * Case y == variable, z == variable -> BGE, label, y, z
                  * Case y == variable, z == immediate -> LI temp, z; BGE label, y, temp
                  * Case y == immediate, z == variable -> LI temp, y; BGE label, temp, z
                    Case y == immediate, z == immediate -> if(y >= z) B label
                  * */

                 // Case y == variable, z == variable -> BGE, label, y, z
                 if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.BGE, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsI);
                 }

                 //Case y == variable, z == immediate -> LI temp, z; BGE label, y, temp
                 else if(iri.operands[1] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[2]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BGE, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), new MIPSRegisterOperand(-1, "$virBranchTemp", true)
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == variable -> LI temp, y; BGE label, temp, z
                 else if(iri.operands[1] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     MIPSInstruction mipsILI = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virBranchTemp", true), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                     });
                     MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.BGE, new MIPSOperand[]{
                             new MIPSLabelOperand(((IRLabelOperand)iri.operands[0]).getName() + "_" + funcName), new MIPSRegisterOperand(-1, "$virBranchTemp", true), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2]).getName())
                     });
                     newInstructions.add(mipsILI);
                     newInstructions.add(mipsIBranch);
                 }
                 //Case y == immediate, z == immediate -> if(y >= z) B label
                 else {
                     boolean toBranch = Integer.parseInt(((IRConstantOperand)iri.operands[1]).getValueString()) >= Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString());
                     if(toBranch) {
                         MIPSInstruction mipsIBranch = new MIPSInstruction(MIPSInstruction.OpCode.J, new MIPSOperand[]{
                                 new MIPSLabelOperand(((IRLabelOperand) iri.operands[0]).getName() + "_" + funcName)
                         });
                         newInstructions.add(mipsIBranch);
                     }
                 }
                 break;

             case GOTO:
                 /* todo:
                  * GOTO label -> B, label
                  * */
                 MIPSInstruction mipsIGoto = new MIPSInstruction(MIPSInstruction.OpCode.J, new MIPSOperand[]{
                         new MIPSLabelOperand(((IRLabelOperand) iri.operands[0]).getName() + "_" + funcName)
                 });
                 newInstructions.add(mipsIGoto);
                 break;
             case RETURN:
                 /*
                 todo: Implement once we figure out how the stack is structured.
                  */
                 /*
                 Return: op, x
                    Teardown:
                    1) Pop $s0 - $s7
                    2) Load $ra
                    3) Restore $sp ($sp = $fp + 4)
                    4) Restore caller's $fp
                    5) Save return value in $v0
                    6) Return

                  */

                 //1) Load $s0 - $s7
                 for(int i = 0; i <= 7; i++) {
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW,
                             new MIPSOperand[]{new MIPSRegisterOperand(-1, "$s" + i, false), new MIPSConstantOperand("" + (-8 + -4 * i), (-8 + -4 * i)), new MIPSRegisterOperand(-1, "$fp", false)}));
                 }
                 //2) Load $ra (LW $ra, -4, $fp)
                 newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW,
                         new MIPSOperand[]{new MIPSRegisterOperand(-1, "$ra", false), new MIPSConstantOperand("" + -4, -4), new MIPSRegisterOperand(-1, "$fp", false)}));
                 //3) Restore $sp ($sp = $fp + 4)
                 newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI,
                         new MIPSOperand[]{new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$fp", false), new MIPSConstantOperand("" + 4, 4)}));
                 //4) Restore caller's $fp
                 newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW,
                         new MIPSOperand[]{new MIPSRegisterOperand(-1, "$fp", false), new MIPSConstantOperand("" + 0, 0), new MIPSRegisterOperand(-1, "$fp", false)}));
                 //5) Save return value in $v0
                 if(iri.operands[0] instanceof IRConstantOperand) {
                     //LI $v0, constant
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                            new MIPSRegisterOperand(-1, "$v0", false), newConstantOp(((IRConstantOperand)iri.operands[0]).getValueString())
                     }));
                 } else {
                     //ADDI, $v0, $arg, 0
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$v0", false), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[0]).getName()), newConstantOp("0")
                     }));
                 }
                 //6) Return
                 newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.JR,
                         new MIPSOperand[]{new MIPSRegisterOperand(-1, "$ra", false)}));
                 break;

             case CALL:
                 /*
                 todo: Implement once we figure out how the stack is structured.
                  */
                 //Translate intrinsic functions from IR
                 //Case analysis.

                 //CALL, dest, arg0, ...

                 //Possible built in functions: puti, putc
                 //Note: with these, we have to use the $a registers.

                //Handle cases where argument is a constant vs cases where argument is a variable.

                 //Push arguments and call function
                 //Handle differently depending on if an argument is a constant or a variable
                 String callFuncName = ((IRFunctionOperand)iri.operands[0]).getName();
                 if(callFuncName.equals("puti")) { //System call print_int, code 1, argument in $a0
                     //LI $v0 1
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$v0", false), newConstantOp("1")
                     }));
                     if(iri.operands[1] instanceof IRConstantOperand) {
                         //LI $a0 constant
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$a0", false), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                         }));
                     } else {
                         //ADDI $a0, $arg, 0
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$a0", false), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newConstantOp("0")
                         }));
                     }
                     //Syscall
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SYSCALL, new MIPSOperand[]{
                     }));
                 }
                 else if (callFuncName.equals("putc")) { //System call print_char, code 11, argument in $a0.
                     //LI $v0 1
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$v0", false), newConstantOp("11")
                     }));
                     if(iri.operands[1] instanceof IRConstantOperand) {
                         //LI $a0 constant
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$a0", false), newConstantOp(((IRConstantOperand) iri.operands[1]).getValueString())
                         }));
                     } else {
                         //ADDI $a0, $arg, 0
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$a0", false), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1]).getName()), newConstantOp("0")
                         }));
                     }
                     //Syscall
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SYSCALL, new MIPSOperand[]{
                     }));
                 }
                 else {

                     // Moved calling convention here since it doesn't need to happen for syscalls

                     /*
                     When calling a function:
                     1) Save $a0 - $a3
                     2) Save $t0 - $t9

                     //Note: We can treat $a0 - $a3 as helper registers because by our convention we won't use them except for syscalls
                     3) Push arguments in order(ie. the earliest parameter will be "lowest" on the stack (have the highest address).
                     4) Call function
                     5) Pop Arguments
                     6) If Callr Load $v0 into the return value

                     7) Load $t0 - $t9
                     8) Load $a0 - $a3
                     9) Pop from stack
                    */

                     //1, 2

                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a0", false), newConstantOp("-4"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a1", false), newConstantOp("-8"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a2", false), newConstantOp("-12"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a3", false), newConstantOp("-16"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t0", false), newConstantOp("-20"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t1", false), newConstantOp("-24"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t2", false), newConstantOp("-28"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t3", false), newConstantOp("-32"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t4", false), newConstantOp("-36"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t5", false), newConstantOp("-40"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t6", false), newConstantOp("-44"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t7", false), newConstantOp("-48"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t8", false), newConstantOp("-52"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t9", false), newConstantOp("-56"), new MIPSRegisterOperand(-1, "$sp", false)}));


                     //Any other function.  Push args, call function, then restore $sp.
                    int numArguments = iri.operands.length - 1;
                    //Push Args
                     for(int i = 0; i < numArguments; i++) {
                         if(iri.operands[1 + i] instanceof IRConstantOperand) {
                             //Constant arg: LI $a0 constant; SW $a0, spOffset, $sp
                             //LI $a0 constant;
                             newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                                     new MIPSRegisterOperand(-1, "$a0", false), newConstantOp(((IRConstantOperand) iri.operands[1 + i]).getValueString())
                             }));
                             //SW $a0, spOffset, $sp
                             newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                                     new MIPSRegisterOperand(-1, "$a0", false), newConstantOp("" + (-56-4 * (i + 1))), new MIPSRegisterOperand(-1, "$sp", false)
                             }));
                         } else if(iri.operands[1 + i] instanceof IRVariableOperand && !(((IRVariableOperand) iri.operands[1 + i]).type instanceof IRArrayType)){
                             //Non-Array Variable arg: ADDI $a0, $arg, 0; SW $a0, 0, $sp
                             newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                     new MIPSRegisterOperand(-1, "$a0", false), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[1+i]).getName()), newConstantOp("0")
                             }));
                             //SW $a0, spOffset, $sp
                             newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                                     new MIPSRegisterOperand(-1, "$a0", false), newConstantOp("" + (-56-4 * (i + 1))), new MIPSRegisterOperand(-1, "$sp", false)
                             }));
                         }else {
                             //Array variable arg:
                             // If Local array variable
                             // ADDI $a0, $fp, arrayToFPOffsetMap[arg]; SW $a0, spOffset, $sp
                             // If parameter
                             // ADDI $a0, $vir, 0; SW $a0, spOffset, $sp
                             if(!isParameter.getOrDefault(((IRVariableOperand)iri.operands[1 + i]).getName(), false)){
                                 int fpOffset = arrayToFPOffsetMap.get(((IRVariableOperand)iri.operands[1 + i]).getName());
                                 newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                         new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp(""+fpOffset)
                                 }));
                                 MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                         new MIPSRegisterOperand(-1, "$a0", true), newNonVirRegOp("$fp"), new MIPSRegisterOperand(-1, "$virFPOffset", true)
                                 });
                                 newInstructions.add(mipsILoadArrayBase);
                             } else {
                                 //Parameter
                                 newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                         new MIPSRegisterOperand(-1, "$a0", false), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[1 + i]).getName()), newConstantOp("0")
                                 }));
                             }
                             //SW $a0, spOffset, $sp
                             newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                                     new MIPSRegisterOperand(-1, "$a0", false), newConstantOp("" + (-56-4 * (i + 1))), new MIPSRegisterOperand(-1, "$sp", false)
                             }));
                         }
                     }
                    // Moved stack pointer manipulation here to  help with register allocation
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), newConstantOp(""+ (-56 -4 * (numArguments))), }));

                    //Call Function
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.JAL, new MIPSOperand[]{
                             new MIPSLabelOperand(callFuncName)
                     }));
                     //Restore $sp
                     //ADDI $sp, $sp, numberOfArguments * 4;
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), newConstantOp("" + numArguments * 4)
                     }));
                     //7,8,9

                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), newConstantOp("56"), }));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a0", false), newConstantOp("-4"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a1", false), newConstantOp("-8"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a2", false), newConstantOp("-12"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a3", false), newConstantOp("-16"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t0", false), newConstantOp("-20"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t1", false), newConstantOp("-24"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t2", false), newConstantOp("-28"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t3", false), newConstantOp("-32"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t4", false), newConstantOp("-36"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t5", false), newConstantOp("-40"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t6", false), newConstantOp("-44"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t7", false), newConstantOp("-48"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t8", false), newConstantOp("-52"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t9", false), newConstantOp("-56"), new MIPSRegisterOperand(-1, "$sp", false)}));

                 }

                 break;
             case CALLR:
/*
                 todo: Implement once we figure out how the stack is structured.
                  */
                 //Translate intrinsic functions from IR
                 //Case analysis.

                 //CALLR, x, func, arg0, ...
                 //Possible built in functions: geti, getc

                 //Handle cases where argument is a constant vs cases where argument is a variable.

                 //Push arguments and call function
                 //Handle differently depending on if an argument is a constant or a variable
                 String callrFuncName = ((IRFunctionOperand)iri.operands[1]).getName();
                 if(callrFuncName.equals("geti")) { //System call read_int, code 5
                     //LI $v0 5
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$v0", false), newConstantOp("5")
                     }));
                     //Syscall
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SYSCALL, new MIPSOperand[]{
                     }));
                     //Save return value from $v0 to destination
                     //ADDI, $dest, $v0, 0
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), new MIPSRegisterOperand(-1, "$v0", false), newConstantOp("0")
                     }));
                 }
                 else if(callrFuncName.equals("getc")) { //System call read_char, code 12
                     //LI $v0 12
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$v0", false), newConstantOp("12")
                     }));
                     //Syscall
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SYSCALL, new MIPSOperand[]{
                     }));
                     //Save return value from $v0 to destination
                     //ADDI, $dest, $v0, 0
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), new MIPSRegisterOperand(-1, "$v0", false), newConstantOp("0")
                     }));
                 }
                 else {
                     // Moved calling convention here since not needed for syscalls
                      /*
                     When calling a function:
                     1) Save $a0 - $a3
                     2) Save $t0 - $t9

                     //Note: We can treat $a0 - $a3 as helper registers because by our convention we won't use them except for syscalls
                     3) Push arguments in order(ie. the earliest parameter will be "lowest" on the stack (have the highest address).
                     4) Call function
                     5) Pop Arguments
                     6) If Callr Load $v0 into the return value

                     7) Load $t0 - $t9
                     8) Load $a0 - $a3
                     9) Pop from stack
                    */

                     //1, 2
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a0", false), newConstantOp("-4"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a1", false), newConstantOp("-8"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a2", false), newConstantOp("-12"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a3", false), newConstantOp("-16"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t0", false), newConstantOp("-20"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t1", false), newConstantOp("-24"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t2", false), newConstantOp("-28"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t3", false), newConstantOp("-32"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t4", false), newConstantOp("-36"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t5", false), newConstantOp("-40"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t6", false), newConstantOp("-44"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t7", false), newConstantOp("-48"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t8", false), newConstantOp("-52"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t9", false), newConstantOp("-56"), new MIPSRegisterOperand(-1, "$sp", false)}));


                     //Any other function.  Push args, call function, then restore $sp.
                     int numArguments = iri.operands.length - 2;
                     //Push Args
                     for(int i = 0; i < numArguments; i++) {
                         if(iri.operands[2 + i] instanceof IRConstantOperand) {
                             //Constant arg: LI $a0 constant; SW $a0, spOffset, $sp
                             //LI $a0 constant;
                             newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                                     new MIPSRegisterOperand(-1, "$a0", false), newConstantOp(((IRConstantOperand) iri.operands[2 + i]).getValueString())
                             }));
                             //SW $a0, spOffset, $sp
                             newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                                     new MIPSRegisterOperand(-1, "$a0", false), newConstantOp("" + (-56-4 * (i + 1))), new MIPSRegisterOperand(-1, "$sp", false)
                             }));
                         } else if(iri.operands[2 + i] instanceof IRVariableOperand && !(((IRVariableOperand) iri.operands[2 + i]).type instanceof IRArrayType)){
                             //Non-Array Variable arg: ADDI $a0, $arg, 0; SW $a0, 0, $sp
                             newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                     new MIPSRegisterOperand(-1, "$a0", false), newVirRegOp(varToRegMap, ((IRVariableOperand) iri.operands[2+i]).getName()), newConstantOp("0")
                             }));
                             //SW $a0, spOffset, $sp
                             newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                                     new MIPSRegisterOperand(-1, "$a0", false), newConstantOp("" + (-56-4 * (i + 1))), new MIPSRegisterOperand(-1, "$sp", false)
                             }));
                         }else {
                             // If Local array variable
                             // ADDI $a0, $fp, arrayToFPOffsetMap[arg]; SW $a0, spOffset, $sp
                             // If parameter
                             // ADDI $a0, $vir, 0; SW $a0, spOffset, $sp
                             if(!isParameter.getOrDefault(((IRVariableOperand)iri.operands[2 + i]).getName(), false)){
                                 int fpOffset = arrayToFPOffsetMap.get(((IRVariableOperand)iri.operands[2 + i]).getName());
                                 newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                         new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp(""+fpOffset)
                                 }));
                                 MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                         new MIPSRegisterOperand(-1, "$a0", true), newNonVirRegOp("$fp"), new MIPSRegisterOperand(-1, "$virFPOffset", true)
                                 });
                                 newInstructions.add(mipsILoadArrayBase);
                             } else {
                                 //Parameter
                                 newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                         new MIPSRegisterOperand(-1, "$a0", false), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[2 + i]).getName()), newConstantOp("0")
                                 }));
                             }
                             //SW $a0, spOffset, $sp
                             newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                                     new MIPSRegisterOperand(-1, "$a0", false), newConstantOp("" + (-56-4 * (i + 1))), new MIPSRegisterOperand(-1, "$sp", false)
                             }));
                         }
                     }
                     // Moved to help with register allocation
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), newConstantOp("" + (-56 -4 * (numArguments))), }));

                     //Call Function
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.JAL, new MIPSOperand[]{
                             new MIPSLabelOperand(callrFuncName)
                     }));
                     //Restore $sp
                     //ADDI $sp, $sp, numberOfArguments * 4;
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), newConstantOp("" + numArguments * 4)
                     }));

                     //7,8,9
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), newConstantOp("56"), }));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a0", false), newConstantOp("-4"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a1", false), newConstantOp("-8"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a2", false), newConstantOp("-12"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$a3", false), newConstantOp("-16"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t0", false), newConstantOp("-20"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t1", false), newConstantOp("-24"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t2", false), newConstantOp("-28"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t3", false), newConstantOp("-32"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t4", false), newConstantOp("-36"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t5", false), newConstantOp("-40"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t6", false), newConstantOp("-44"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t7", false), newConstantOp("-48"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t8", false), newConstantOp("-52"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$t9", false), newConstantOp("-56"), new MIPSRegisterOperand(-1, "$sp", false)}));
                     // Moved to help with register allocation
                     //Save return value from $v0 to destination
                     //ADDI, $dest, $v0, 0
                     newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), new MIPSRegisterOperand(-1, "$v0", false), newConstantOp("0")
                     }));
                 }

                 break;
             case ARRAY_STORE:
                 /*
                 todo:
                 ARRAY_STORE, x, array_name, offset
                 In SW, x has to be a register, offset must be an integer value, and the base must be a register.
                 CASE x == immediate, offset == immediate -> LI virArrayValue, x;  SW, virArrayValue, offset * 4, (virArrayBase)
                 CASE x == immediate, offset == variable -> LI virArrayValue, x; SLL, virOffset, offset, 2;  ADD, virOffset, virOffset, fpOffset;  LW, virArrayValue, 0(virOffset)
                 CASE x == variable, offset == immediate -> SW, x, offset * 4(array_name)
                 CASE x == variable, offset == variable -> SLL, temp1, offset, 2;  ADD, temp2, temp1, array_name;  SW, x, 0(temp2)
                  */

                 //CASE x == immediate, offset == immediate -> LI virArrayValue, x;  SW, virArrayValue, offset * 4, (virArrayBase)

                 if(iri.operands[0] instanceof IRConstantOperand && iri.operands[2] instanceof IRConstantOperand)
                 {
                     // If Local array variable
                     // ADDI $a0, $fp, arrayToFPOffsetMap[arg]
                     // If parameter
                     // ADDI $a0, $vir, 0
                     if(!isParameter.getOrDefault(((IRVariableOperand)iri.operands[1]).getName(), false)){
                         int fpOffset = arrayToFPOffsetMap.get(((IRVariableOperand)iri.operands[1]).getName());
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp(""+fpOffset)
                         }));
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newNonVirRegOp("$fp"), new MIPSRegisterOperand(-1, "$virFPOffset", true)
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     } else {
                         //Parameter
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[1]).getName()), newConstantOp("0")
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     }
                     int arrayValue = Integer.parseInt(((IRConstantOperand)iri.operands[0]).getValueString());
                     MIPSInstruction mipsILoadArrayValue = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$virArrayValue", true), newConstantOp("" + arrayValue)
                     });
                     newInstructions.add(mipsILoadArrayValue);
                     int offset = Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString()) * 4;
                     MIPSInstruction mipsISW = new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$virArrayValue", true), newConstantOp("" + offset), new MIPSRegisterOperand(-1, "$virArrayBase", true)
                     });
                     newInstructions.add(mipsISW);
                 }
                 //CASE x == immediate, offset == variable -> LI virArrayValue, x; SLL, virOffset, offset, 2;  ADD, virOffset, virOffset, fpOffset;  LW, virArrayValue, 0(virOffset)

                 else if (iri.operands[0] instanceof IRConstantOperand && iri.operands[2] instanceof IRVariableOperand) {
                     int arrayValue = Integer.parseInt(((IRConstantOperand)iri.operands[0]).getValueString());
                     MIPSInstruction mipsILoadArrayValue = new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[]{
                             new MIPSRegisterOperand(-1, "$virArrayValue", true), newConstantOp("" + arrayValue)
                     });
                     newInstructions.add(mipsILoadArrayValue);

                     // If Local array variable
                     // ADDI $a0, $fp, arrayToFPOffsetMap[arg]
                     // If parameter
                     // ADDI $a0, $vir, 0
                     if(!isParameter.getOrDefault(((IRVariableOperand)iri.operands[1]).getName(), false)){
                         int fpOffset = arrayToFPOffsetMap.get(((IRVariableOperand)iri.operands[1]).getName());
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp(""+fpOffset)
                         }));
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newNonVirRegOp("$fp"), new MIPSRegisterOperand(-1, "$virFPOffset", true)
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     } else {
                         //Parameter
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[1]).getName()), newConstantOp("0")
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     }

                     MIPSInstruction mipsSLL = new MIPSInstruction(MIPSInstruction.OpCode.SLL, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArrayLocation", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[2]).getName()), newConstantOp("2")
                     });
                     MIPSInstruction mipsADD = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArrayLocation", true), new MIPSRegisterOperand(-1, "$virArrayLocation", true), new MIPSRegisterOperand(-1, "$virArrayBase", true)
                     });
                     MIPSInstruction mipsLW = new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArrayValue", true), newConstantOp("" + 0), new MIPSRegisterOperand(-1, "$virArrayLocation", true)
                     });
                     newInstructions.add(mipsSLL);
                     newInstructions.add(mipsADD);
                     newInstructions.add(mipsLW);
                 }
                 //CASE x == variable, offset == immediate -> SW, x, offset * 4, (fpOffset)
                 else if(iri.operands[0] instanceof IRVariableOperand && iri.operands[2] instanceof IRConstantOperand) {
                     // If Local array variable
                     // ADDI $a0, $fp, arrayToFPOffsetMap[arg]
                     // If parameter
                     // ADDI $a0, $vir, 0
                     if(!isParameter.getOrDefault(((IRVariableOperand)iri.operands[1]).getName(), false)){
                         int fpOffset = arrayToFPOffsetMap.get(((IRVariableOperand)iri.operands[1]).getName());
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp(""+fpOffset)
                         }));
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newNonVirRegOp("$fp"), new MIPSRegisterOperand(-1, "$virFPOffset", true)
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     } else {
                         //Parameter
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[1]).getName()), newConstantOp("0")
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     }

                     int offset = Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString()) * 4;
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[] {
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp("" + offset), new MIPSRegisterOperand(-1, "$virArrayBase", true)
                     });
                     newInstructions.add(mipsI);
                 }
                 //CASE x == variable, offset == variable -> SLL, virOffset, offset, 2;  ADD, virOffset, virOffset, fpOffset;  SW, x, 0(virOffset)
                 else{
                     // If Local array variable
                     // ADDI $a0, $fp, arrayToFPOffsetMap[arg]
                     // If parameter
                     // ADDI $a0, $vir, 0
                     if(!isParameter.getOrDefault(((IRVariableOperand)iri.operands[1]).getName(), false)){
                         int fpOffset = arrayToFPOffsetMap.get(((IRVariableOperand)iri.operands[1]).getName());
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp(""+fpOffset)
                         }));
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newNonVirRegOp("$fp"), new MIPSRegisterOperand(-1, "$virFPOffset", true)
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     } else {
                         //Parameter
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[1]).getName()), newConstantOp("0")
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     }

                     MIPSInstruction mipsSLL = new MIPSInstruction(MIPSInstruction.OpCode.SLL, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArrayLocation", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[2]).getName()), newConstantOp("2")
                     });
                     MIPSInstruction mipsADD = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArrayLocation", true), new MIPSRegisterOperand(-1, "$virArrayLocation", true), new MIPSRegisterOperand(-1, "$virArrayBase", true)
                     });
                     MIPSInstruction mipsSW = new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[] {
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp("" + 0), new MIPSRegisterOperand(-1, "$virArrayLocation", true)
                     });
                     newInstructions.add(mipsSLL);
                     newInstructions.add(mipsADD);
                     newInstructions.add(mipsSW);
                 }

                 break;
             case ARRAY_LOAD:
                 /*
                 todo:
                 ARRAY_LOAD, x, array_name, offset
                 In LW, x has to be a register, offset must be an integer value, and the base must be a register.

                 CASE x == variable, offset == immediate -> LW, x, offset * 4, (fpOffset)
                 CASE x == variable, offset == variable -> SLL, virOffset, offset, 2;  ADD, virOffset, virOffset, fpOffset;  LW, x, 0(virOffset)
                  */


                 //CASE x == variable, offset == immediate -> LW, x, offset * 4, (array_name)
                 if(iri.operands[2] instanceof IRConstantOperand) {
                     // If Local array variable
                     // ADDI $a0, $fp, arrayToFPOffsetMap[arg]
                     // If parameter
                     // ADDI $a0, $vir, 0
                     if(!isParameter.getOrDefault(((IRVariableOperand)iri.operands[1]).getName(), false)){
                         int fpOffset = arrayToFPOffsetMap.get(((IRVariableOperand)iri.operands[1]).getName());
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp(""+fpOffset)
                         }));
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newNonVirRegOp("$fp"), new MIPSRegisterOperand(-1, "$virFPOffset", true)
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     } else {
                         //Parameter
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[1]).getName()), newConstantOp("0")
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     }

                     int offset = Integer.parseInt(((IRConstantOperand)iri.operands[2]).getValueString()) * 4;
                     MIPSInstruction mipsI = new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[] {
                         newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp("" + offset), new MIPSRegisterOperand(-1, "$virArrayBase", true)
                     });
                     newInstructions.add(mipsI);
                 }
                 //CASE x == variable, offset == variable -> SLL, virArrayLocation, offset, 2;  ADD, virArrayLocation, virArrayLocation, fpOffset;  LW, x, 0(virArrayLocation)
                 else {
                     // If Local array variable
                     // ADDI $a0, $fp, arrayToFPOffsetMap[arg]
                     // If parameter
                     // ADDI $a0, $vir, 0
                     if(!isParameter.getOrDefault(((IRVariableOperand)iri.operands[1]).getName(), false)){
                         int fpOffset = arrayToFPOffsetMap.get(((IRVariableOperand)iri.operands[1]).getName());
                         newInstructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LI, new MIPSOperand[] {
                                 new MIPSRegisterOperand(-1, "$virFPOffset", true), newConstantOp("" + fpOffset)
                         }));
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newNonVirRegOp("$fp"), new MIPSRegisterOperand(-1, "$virFPOffset", true)
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     } else {
                         //Parameter
                         MIPSInstruction mipsILoadArrayBase = new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{
                                 new MIPSRegisterOperand(-1, "$virArrayBase", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[1]).getName()), newConstantOp("0")
                         });
                         newInstructions.add(mipsILoadArrayBase);
                     }

                     MIPSInstruction mipsSLL = new MIPSInstruction(MIPSInstruction.OpCode.SLL, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArrayLocation", true), newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[2]).getName()), newConstantOp("2")
                     });
                     MIPSInstruction mipsADD = new MIPSInstruction(MIPSInstruction.OpCode.ADD, new MIPSOperand[] {
                             new MIPSRegisterOperand(-1, "$virArrayLocation", true), new MIPSRegisterOperand(-1, "$virArrayLocation", true), new MIPSRegisterOperand(-1, "$virArrayBase", true)
                     });
                     MIPSInstruction mipsLW = new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[] {
                             newVirRegOp(varToRegMap, ((IRVariableOperand)iri.operands[0]).getName()), newConstantOp("" + 0), new MIPSRegisterOperand(-1, "$virArrayLocation", true)
                     });
                     newInstructions.add(mipsSLL);
                     newInstructions.add(mipsADD);
                     newInstructions.add(mipsLW);
                 }

                 break;
             case LABEL:
                 // Making sure label names don't conflict
                 MIPSInstruction mipsILabel = new MIPSInstruction(MIPSInstruction.OpCode.LABEL, new MIPSOperand[]{
                         new MIPSLabelOperand(((IRLabelOperand) iri.operands[0]).getName() + "_" + funcName)
                 });
                 newInstructions.add(mipsILabel);
                 break;
        }
        return newInstructions;
    }

    private static MIPSRegisterOperand newVirRegOp(HashMap<String, Integer> rm, String name) {
        return new MIPSRegisterOperand(rm.get(name), "$vir" + rm.get(name), true);
    }
    //creates a new constant operand
    private static MIPSConstantOperand newConstantOp(String value) {
        return new MIPSConstantOperand(value, Integer.parseInt(value));
    }
    //creates a new register operand
    private static MIPSRegisterOperand newNonVirRegOp(String name){
        return new MIPSRegisterOperand(-1, name, false);
    }
    private static boolean canStore16Bits(int val) {
        return val > -32768 && val < 32767;
    }
}
