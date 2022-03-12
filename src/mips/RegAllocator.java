package mips;

import mips.cfg.BasicBlock;
import mips.cfg.CFG;
import mips.datatype.MIPSWordType;
import mips.operand.MIPSConstantOperand;
import mips.operand.MIPSOperand;
import mips.operand.MIPSRegisterOperand;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

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

            // Compute live sets for each basic block
            Queue<BasicBlock> workList = new ArrayDeque<>();
            // Add basic blocks to work list and initialize kill and uevar
            for (BasicBlock bb : cfg.basicBlocks)
            {
                workList.add(bb);
                for (MIPSInstruction instruction : bb.instructions)
                {
                    for (int i = instruction.operands.length - 1; i >= 0 ; i--)
                    {
                        if (instruction.operands[i] instanceof MIPSRegisterOperand && ((MIPSRegisterOperand) instruction.operands[i]).virtual)
                        {
                            String varName = instruction.operands[i].getName();
                            if (!isDef(instruction.opCode, i) && !bb.varKill.contains(varName))
                            {
                                bb.UEVar.add(varName);
                            }
                            if (isDef(instruction.opCode, i))
                            {
                                bb.varKill.add(varName);
                            }
                        }
                    }
                }
            }
            while (!workList.isEmpty())
            {
                BasicBlock block = workList.poll();
                for (BasicBlock succ : block.successors)
                {
                    block.liveOut.addAll(succ.liveIn);
                }
                HashSet<String> temp = new HashSet<>(block.liveOut);
                temp.removeAll(block.varKill);
                HashSet<String> newIn = new HashSet<>(block.UEVar);
                newIn.addAll(temp);
                if (!newIn.equals(block.liveIn))
                {
                    for (BasicBlock pred : block.predecessors)
                    {
                        workList.add(pred);
                    }
                }
                block.liveIn = newIn;
            }

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
                // Mapping from variables to temp registers
                HashMap<String, Integer> varToReg = new HashMap<>();
                // Count uses of each variable
                HashMap<String, Integer> useCounts = new HashMap<>();
                for (int i = 0; i < bb.instructions.size(); i++) {
                    for (int j = bb.instructions.get(i).operands.length - 1; j >= 0; j--) {
                        if (bb.instructions.get(i).operands[j] instanceof MIPSRegisterOperand && ((MIPSRegisterOperand) bb.instructions.get(i).operands[j]).virtual) {
                            String varName = bb.instructions.get(i).operands[j].getName();
                            if (!isDef(bb.instructions.get(i).opCode, j))
                            {
                                if (useCounts.containsKey(varName))
                                {
                                    useCounts.put(varName, useCounts.get(varName) + 1);
                                }
                                else
                                {
                                    useCounts.put(varName, 1);
                                }
                            }
                        }
                    }
                }
                // Find the 10 most used variables and map them to temp registers
                if (useCounts.size() > 0) {
                    String shortestVar = (String) useCounts.keySet().toArray()[0];
                    for (String name : useCounts.keySet()) {
                        if (varToReg.size() < 10) {
                            if (useCounts.get(name) < useCounts.get(shortestVar)) {
                                shortestVar = name;
                            }
                            varToReg.put(name, varToReg.size());
                        } else {
                            if (useCounts.get(name) > useCounts.get(shortestVar)) {
                                int reg = varToReg.get(shortestVar);
                                varToReg.remove(shortestVar);
                                varToReg.put(name, reg);
                                shortestVar = (String) varToReg.keySet().toArray()[0];
                                for (String shortNames : varToReg.keySet()) {
                                    if (useCounts.get(shortNames) < useCounts.get(shortestVar)) {
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

                    // Load variables from stack into registers
                    if (!loadedRegs && instruction.opCode != MIPSInstruction.OpCode.LABEL)
                    {
                        // Load values from stack into reg once per basic block
                        for (String name : varToReg.keySet())
                        {
                            if (bb.liveIn.contains(name))
                            {
                                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + varToReg.get(name), false), new MIPSConstantOperand("" + 4 * virRegToNum.get(name), 4 * virRegToNum.get(name)), new MIPSRegisterOperand(-1, "$sp", false)}));
                            }
//                            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + varToReg.get(name), false), new MIPSConstantOperand("" + 4 * virRegToNum.get(name), 4 * virRegToNum.get(name)), new MIPSRegisterOperand(-1, "$sp", false)}));

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
                                // No need to load the variable if its being written to
                                if (!isDef(instruction.opCode, operandIndex)) {
                                    mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$s" + operandIndex, false), new MIPSConstantOperand("" + 4 * virRegToNum.get(virtualRegName), 4 * virRegToNum.get(virtualRegName)), new MIPSRegisterOperand(-1, "$sp", false)}));
                                }
                                allocatedInstruction.operands[operandIndex] = new MIPSRegisterOperand(-1, "$s" + operandIndex, false);
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
                            if (isDef(instruction.opCode, i)) {
                                mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$s" + i, false), new MIPSConstantOperand("" + 4 * virRegToNum.get(virtualRegName), 4 * virRegToNum.get(virtualRegName)), new MIPSRegisterOperand(-1, "$sp", false)}));
                            }
                        }
                    }
                }
                // End each basic block by storing the register values back onto the stack
                if (isBranchOrJump(bb.instructions.get(bb.instructions.size() - 1)))
                {
                    for (String name : varToReg.keySet())
                    {
                        if (bb.liveOut.contains(name))
                        {
                            mipsSub.instructions.add(mipsSub.instructions.size() - 1, new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + varToReg.get(name), false), new MIPSConstantOperand("" + 4 * virRegToNum.get(name), 4 * virRegToNum.get(name)), new MIPSRegisterOperand(-1, "$sp", false)}));

                        }
//                        mipsSub.instructions.add(mipsSub.instructions.size() - 1, new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + varToReg.get(name), false), new MIPSConstantOperand("" + 4 * virRegToNum.get(name), 4 * virRegToNum.get(name)), new MIPSRegisterOperand(-1, "$sp", false)}));
                    }
                }
                else
                {
                    for (String name : varToReg.keySet())
                    {
                        if (bb.liveOut.contains(name))
                        {
                            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + varToReg.get(name), false), new MIPSConstantOperand("" + 4 * virRegToNum.get(name), 4 * virRegToNum.get(name)), new MIPSRegisterOperand(-1, "$sp", false)}));
                        }
//                        mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + varToReg.get(name), false), new MIPSConstantOperand("" + 4 * virRegToNum.get(name), 4 * virRegToNum.get(name)), new MIPSRegisterOperand(-1, "$sp", false)}));
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
                        // No need to load the variable if its being written to
                        if (!isDef(instruction.opCode, operandIndex)) {
                            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.LW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + operandIndex, false), new MIPSConstantOperand("" + 4 * virRegToNum.get(virtualRegName), 4 * virRegToNum.get(virtualRegName)), new MIPSRegisterOperand(-1, "$sp", false)}));
                        }
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
                        // Only need to write back the defs
                        if (isDef(instruction.opCode, i)) {
                            mipsSub.instructions.add(new MIPSInstruction(MIPSInstruction.OpCode.SW, new MIPSOperand[]{new MIPSRegisterOperand(-1, "$t" + i, false), new MIPSConstantOperand("" + 4 * virRegToNum.get(virtualRegName), 4 * virRegToNum.get(virtualRegName)), new MIPSRegisterOperand(-1, "$sp", false)}));

                        }
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
