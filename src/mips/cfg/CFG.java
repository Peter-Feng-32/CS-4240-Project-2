package mips.cfg;

import mips.MIPSInstruction;
import mips.MIPSSubroutine;

import java.util.ArrayList;
import java.util.List;

public class CFG {
    public BasicBlock root;

    public static CFG MakeCFG(MIPSSubroutine func)
    {
        CFG cfg = new CFG();
        List<MIPSInstruction> instructions = func.instructions;
        List<Integer> leaders = leaderIndices(instructions);
        List<BasicBlock> basicBlocks = new ArrayList<>();
        for (int i = 0; i < leaders.size(); i++)
        {
            basicBlocks.add(new BasicBlock());
            BasicBlock bb = basicBlocks.get(i);
           bb.addInstruction(instructions.get(leaders.get(i)));
           int index = leaders.get(i) + 1;
           while (!leaders.contains(index)) {
               bb.addInstruction(instructions.get(index));
               index++;
           }
        }
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            // Not sure if there is a goto instruction. If there is this changes a bit
            BasicBlock bb = basicBlocks.get(i);
            MIPSInstruction lastInstruction = bb.instructions.get(bb.instructions.size() - 1);
            if (isBranch(lastInstruction))
            {
                String label = lastInstruction.operands[2].getName(); // assuming the target label is the third operand
                for (BasicBlock block : basicBlocks)
                {
                    if (block.instructions.get(0).opCode == MIPSInstruction.OpCode.LABEL && block.instructions.get(0).operands[0].getName().equals(label))
                    {
                        addEdge(bb, block);
                        break;
                    }
                }
            }
            if (i < leaders.size() - 1)
            {
                addEdge(bb, basicBlocks.get(i + 1));
            }
        }
        cfg.root = basicBlocks.get(0);
        return cfg;
    }
    private static void addEdge(BasicBlock pred, BasicBlock succ)
    {
        pred.successors.add(succ);
        succ.predecessors.add(pred);
    }
    private static boolean isBranch(MIPSInstruction instruction)
    {
        switch (instruction.opCode)
        {
            case BEQ:
            case BNE:
            case BLT:
            case BGE:
            case BGT:
            case BLE:
                return true;
        }
        return false;
    }
    private static List<Integer> leaderIndices(List<MIPSInstruction> instructions)
    {
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {
            if (instructions.get(i).opCode == MIPSInstruction.OpCode.LABEL || i == 0 || isBranch(instructions.get(i - 1)))
            {
                indices.add(i);
            }
        }
        return indices;
    }
}
