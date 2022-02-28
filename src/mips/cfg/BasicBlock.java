package mips.cfg;

import mips.MIPSInstruction;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    public List<MIPSInstruction> instructions;
    public List<BasicBlock> predecessors;
    public List<BasicBlock> successors;
    public void addInstruction(MIPSInstruction instruction)
    {
        instructions.add(instruction);
    }
    public BasicBlock()
    {
        this.instructions = new ArrayList<>();
        this.predecessors = new ArrayList<>();
        this.successors = new ArrayList<>();
    }
}
