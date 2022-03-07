package mips;

import mips.datatype.MIPSWordType;
import mips.operand.MIPSConstantOperand;
import mips.operand.MIPSOperand;
import mips.operand.MIPSRegisterOperand;

import java.util.ArrayList;
import java.util.HashMap;

public class RegAllocator {
    public static MIPSProgram NaiveAlloc(MIPSProgram virp) throws Exception
    {
        MIPSProgram allocatedProgram = new MIPSProgram();

        for (MIPSSubroutine subroutine : virp.subroutines)
        {
            MIPSSubroutine mipsSub = new MIPSSubroutine();
            mipsSub.name = subroutine.name;
            mipsSub.returnType = (subroutine.returnType == null) ? null : MIPSWordType.get();
            mipsSub.instructions = new ArrayList<MIPSInstruction>();
            // Store mapping of virtual registers to spill locations
            HashMap<Integer, Integer> regToOffsetMap = new HashMap<>();
            // Number of variables that need to be stored on the stacks
            int numVariables = 0;
            for (MIPSInstruction instruction : subroutine.instructions)
            {
                MIPSInstruction allocatedInstruction = new MIPSInstruction();
                allocatedInstruction.opCode = instruction.opCode;
                int operandIndex = 0;
                for (MIPSOperand operand : instruction.operands)
                {
                    // Convert virtual registers into physical ones
                    // Load value from stack into a physical register then store it back into the stack
                    // Calling convention stuff shouldn't involve virtual registers so this shouldn't break anything?
                    if (operand instanceof MIPSRegisterOperand && ((MIPSRegisterOperand) operand).virtual)
                    {
                        int virtualRegNum = ((MIPSRegisterOperand) operand).regNum;
                        // If virtual reg number isn't in the map add it and make room on the stack for the value
                        if (!regToOffsetMap.containsKey(virtualRegNum))
                        {
                            regToOffsetMap.put(virtualRegNum, ++numVariables);
                            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), new MIPSConstantOperand(String.valueOf(-4), -4)}));
                        }
                        // Load value from spill location on stack into a temp reg
                        // This also happens when arguments get loaded into virtual registers
                        mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + operandIndex, false), new MIPSConstantOperand("" + (numVariables - regToOffsetMap.get(virtualRegNum)), (numVariables - regToOffsetMap.get(virtualRegNum))), new MIPSRegisterOperand(-1, "$sp", false)}));
                        allocatedInstruction.operands[operandIndex] = new MIPSRegisterOperand(-1, "$t" + operandIndex, false);
                    }
                    // Keep non virtual register operands the same
                    else
                    {
                        allocatedInstruction.operands[operandIndex] = instruction.operands[operandIndex];
                    }
                    operandIndex++;
                }
                // Add the new instruction that uses physical registers
                mipsSub.instructions.add(allocatedInstruction);
                // Store values in the physical registers back into spill locations on the stack
                operandIndex = 0;
                for (MIPSOperand operand : instruction.operands)
                {
                    if (operand instanceof MIPSRegisterOperand && ((MIPSRegisterOperand) operand).virtual)
                    {
                        int virtualRegNum = ((MIPSRegisterOperand) operand).regNum;
                        mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + operandIndex, false), new MIPSConstantOperand("" + (numVariables - regToOffsetMap.get(virtualRegNum)), (numVariables - regToOffsetMap.get(virtualRegNum))), new MIPSRegisterOperand(-1, "$sp", false)}));
                    }
                    operandIndex++;
                }
            }
            allocatedProgram.subroutines.add(mipsSub);
        }
        return allocatedProgram;
    }
}
