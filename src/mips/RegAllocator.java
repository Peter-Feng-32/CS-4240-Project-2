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
            mipsSub.instructions = new ArrayList<>();
            // Number of variables that need to be stored on the stacks
            int numVariables = 0;
            HashMap<String, Integer> virRegToNum = new HashMap<>();
            // Count the number of virtual registers
            for (MIPSInstruction instruction : subroutine.instructions)
            {
                for (MIPSOperand operand : instruction.operands)
                {
                    if (operand instanceof MIPSRegisterOperand && ((MIPSRegisterOperand) operand).virtual && !virRegToNum.containsKey(operand.getName()))
                    {
                        virRegToNum.put(operand.getName(), numVariables++);
                    }
                }
            }
            // We need to make space on the stack for all the spilled registers just before we the first instruction that uses a virtual register to ensure we don't mess up the calling convention
            boolean seenFirstVirtualReg = false;
            for (MIPSInstruction instruction : subroutine.instructions)
            {
                MIPSInstruction allocatedInstruction = new MIPSInstruction();
                allocatedInstruction.opCode = instruction.opCode;
                allocatedInstruction.operands = new MIPSOperand[instruction.operands.length];
                int operandIndex = 0;
                for (MIPSOperand operand : instruction.operands)
                {
                    // Convert virtual registers into physical ones
                    // Load value from stack into a physical register then store it back into the stack
                    // Calling convention stuff shouldn't involve virtual registers so this shouldn't break anything?
                    if (operand instanceof MIPSRegisterOperand && ((MIPSRegisterOperand) operand).virtual)
                    {
                        if (!seenFirstVirtualReg)
                        {
                            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.ADDI, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$sp", false), new MIPSRegisterOperand(-1, "$sp", false), new MIPSConstantOperand(String.valueOf(-4 * numVariables), -4 * numVariables)}));
                            seenFirstVirtualReg = true;
                        }
                        String virtualRegName = operand.getName();

                        // Load value from spill location on stack into a temp reg
                        // This also happens when arguments get loaded into virtual registers
                        mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + operandIndex, false), new MIPSConstantOperand("" + 4 * virRegToNum.get(virtualRegName), 4 * virRegToNum.get(virtualRegName)), new MIPSRegisterOperand(-1, "$sp", false)}));

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
                // Need to go through operands in reverse order in case the destination variable is also an operand
                for (int i = instruction.operands.length - 1; i > -1; i--)
                {
                    MIPSOperand operand = instruction.operands[i];
                    if (operand instanceof MIPSRegisterOperand && ((MIPSRegisterOperand) operand).virtual)
                    {
                        String virtualRegName = operand.getName();
                        mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + i, false), new MIPSConstantOperand("" + 4 * virRegToNum.get(virtualRegName), 4 * virRegToNum.get(virtualRegName)), new MIPSRegisterOperand(-1, "$sp", false)}));
                    }
                }
            }
            allocatedProgram.subroutines.add(mipsSub);
        }
        return allocatedProgram;
    }
}
