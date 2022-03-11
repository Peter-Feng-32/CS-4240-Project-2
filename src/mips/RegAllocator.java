package mips;

import mips.cfg.BasicBlock;
import mips.cfg.CFG;
import mips.datatype.MIPSWordType;
import mips.operand.MIPSConstantOperand;
import mips.operand.MIPSOperand;
import mips.operand.MIPSRegisterOperand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegAllocator {
    public static MIPSProgram IntraBlockAlloc(MIPSProgram virp)
    {
        MIPSProgram allocatedProgram = new MIPSProgram();

        for (MIPSSubroutine subroutine : virp.subroutines) {

            MIPSSubroutine mipsSub = new MIPSSubroutine();
            mipsSub.name = subroutine.name;
            mipsSub.returnType = (subroutine.returnType == null) ? null : MIPSWordType.get();
            mipsSub.instructions = new ArrayList<>();

            CFG cfg = CFG.MakeCFG(subroutine);

            // Number of variables that need to be stored on the stack
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
            for (BasicBlock bb : cfg.basicBlocks)
            {
                HashMap<String, List<Boolean>> liveRanges = new HashMap<>();
                // Initialize live range map with all variables used in this basic block
                for (int i = 0; i < bb.instructions.size(); i++)
                {
                    for (MIPSOperand operand : bb.instructions.get(i).operands)
                    {
                        if (operand instanceof MIPSRegisterOperand && ((MIPSRegisterOperand) operand).virtual && !liveRanges.containsKey(operand.getName()))
                        {
                            liveRanges.put(operand.getName(), new ArrayList<>());
                        }
                    }
                }
                // Mapping from variables to temp registers
                HashMap<String, Integer> varToReg = new HashMap<>();
                if (liveRanges.size() > 0) {
                    // Calculate the live ranges
                    for (int i = 0; i < bb.instructions.size(); i++) {
                        for (int j = bb.instructions.get(i).operands.length - 1; j >= 0; j--) {
                            if (bb.instructions.get(i).operands[j] instanceof MIPSRegisterOperand && ((MIPSRegisterOperand) bb.instructions.get(i).operands[j]).virtual) {
                                String varName = bb.instructions.get(i).operands[j].getName();
                                if (isDef(bb.instructions.get(i).opCode, j)) {
                                    while (liveRanges.get(varName).size() < i + 1) {
                                        liveRanges.get(varName).add(false);
                                    }
                                } else {
                                    while (liveRanges.get(varName).size() < i + 1) {
                                        liveRanges.get(varName).add(true);
                                    }
                                }
                            }
                        }
                    }
                    // Calculate how long each variable is live
                    HashMap<String, Integer> liveTimes = new HashMap<>();
                    for (String name : liveRanges.keySet()) {
                        liveTimes.put(name, 0);
                        for (boolean val : liveRanges.get(name)) {
                            if (val) {
                                liveTimes.put(name, liveTimes.get(name) + 1);
                            }
                        }
                    }
                    // Find the 10 longest living variables and map them to temp registers
                    String shortestVar = (String) liveTimes.keySet().toArray()[0];
                    for (String name : liveTimes.keySet()) {
                        if (varToReg.size() < 10) {
                            if (liveTimes.get(name) < liveTimes.get(shortestVar)) {
                                shortestVar = name;
                            }
                            varToReg.put(name, varToReg.size());
                        } else {
                            if (liveTimes.get(name) > liveTimes.get(shortestVar))
                            {
                                int reg = varToReg.get(shortestVar);
                                varToReg.remove(shortestVar);
                                varToReg.put(name, reg);
                                shortestVar = (String) varToReg.keySet().toArray()[0];
                                for (String shortNames : varToReg.keySet())
                                {
                                    if (liveTimes.get(shortNames) < liveTimes.get(shortestVar)) {
                                        shortestVar = shortNames;
                                    }
                                }
                            }
                        }
                    }
                }
                boolean loadedRegs = false;
                // Iterate over instructions and do the allocations
                for (MIPSInstruction instruction : bb.instructions)
                {
                    MIPSInstruction allocatedInstruction = new MIPSInstruction();
                    allocatedInstruction.opCode = instruction.opCode;
                    allocatedInstruction.operands = new MIPSOperand[instruction.operands.length];

                    // Load variables from stack into registers\
                    // Must load them after the label if the basic block starts with a label
                    if (!loadedRegs && instruction.opCode != MIPSInstruction.OpCode.LABEL)
                    {
                        // Load values from stack into reg once per basic block
                        for (String name : varToReg.keySet())
                        {
                            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + varToReg.get(name), false), new MIPSConstantOperand("" + 4 * virRegToNum.get(name), 4 * virRegToNum.get(name)), new MIPSRegisterOperand(-1, "$sp", false)}));
                        }
                        loadedRegs = true;
                    }

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
                            if(varToReg.containsKey(virtualRegName))
                            {
                                allocatedInstruction.operands[operandIndex] = new MIPSRegisterOperand(-1, "$t" + varToReg.get(virtualRegName), false);
                            }
                            else {
                                // Load value from spill location on stack into a temp reg
                                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$s" + operandIndex, false), new MIPSConstantOperand("" + 4 * virRegToNum.get(virtualRegName), 4 * virRegToNum.get(virtualRegName)), new MIPSRegisterOperand(-1, "$sp", false)}));

                                allocatedInstruction.operands[operandIndex] = new MIPSRegisterOperand(-1, "$t" + operandIndex, false);
                            }
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
                        if (operand instanceof MIPSRegisterOperand && ((MIPSRegisterOperand) operand).virtual && !varToReg.containsKey(operand.getName()))
                        {
                            String virtualRegName = operand.getName();
                            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + i, false), new MIPSConstantOperand("" + 4 * virRegToNum.get(virtualRegName), 4 * virRegToNum.get(virtualRegName)), new MIPSRegisterOperand(-1, "$sp", false)}));
                        }
                    }
                }
                // End each basic block by storing the register values back onto the stack
                if (isBranchOrJump(bb.instructions.get(bb.instructions.size() - 1)))
                {
                    for (String name : varToReg.keySet())
                    {
                        mipsSub.instructions.add(mipsSub.instructions.size() - 1, new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + varToReg.get(name), false), new MIPSConstantOperand("" + 4 * virRegToNum.get(name), 4 * virRegToNum.get(name)), new MIPSRegisterOperand(-1, "$sp", false)}));
                    }
                }
                else
                {
                    for (String name : varToReg.keySet())
                    {
                        mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + varToReg.get(name), false), new MIPSConstantOperand("" + 4 * virRegToNum.get(name), 4 * virRegToNum.get(name)), new MIPSRegisterOperand(-1, "$sp", false)}));
                    }
                }
            }
            allocatedProgram.subroutines.add(mipsSub);
        }
        return allocatedProgram;
    }
    public static MIPSProgram NaiveAlloc(MIPSProgram virp)
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
    private static boolean isDef(MIPSInstruction.OpCode op, int operandIndex)
    {
        if (
            (op == MIPSInstruction.OpCode.LI ||
            op == MIPSInstruction.OpCode.ADD ||
            op == MIPSInstruction.OpCode.ADDI ||
            op == MIPSInstruction.OpCode.SUB ||
            op == MIPSInstruction.OpCode.DIV ||
            op == MIPSInstruction.OpCode.AND ||
            op == MIPSInstruction.OpCode.OR ||
            op == MIPSInstruction.OpCode.DIVU ||
            op == MIPSInstruction.OpCode.MUL ||
            op == MIPSInstruction.OpCode.SLL ||
            op == MIPSInstruction.OpCode.LW) &&
            operandIndex == 0
        )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    private static boolean isBranchOrJump(MIPSInstruction instruction)
    {
        switch (instruction.opCode)
        {
            case BEQ:
            case BNE:
            case BLT:
            case BGE:
            case BGT:
            case BLE:
            case J:
                return true;
        }
        return false;
    }
}
