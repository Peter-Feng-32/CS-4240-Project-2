package mips.cfg;

import mips.MIPSInstruction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BasicBlock {
    public List<MIPSInstruction> instructions;
    public List<BasicBlock> predecessors;
    public List<BasicBlock> successors;
    public HashSet<String> liveIn;
    public HashSet<String> liveOut;
    public HashSet<String> varKill;
    public HashSet<String> UEVar;
    public void addInstruction(MIPSInstruction instruction)
    {
        instructions.add(instruction);
    }
    public BasicBlock()
    {
        this.instructions = new ArrayList<>();
        this.predecessors = new ArrayList<>();
        this.successors = new ArrayList<>();
        this.liveIn = new HashSet<>();
        this.liveOut = new HashSet<>();
        this.varKill = new HashSet<>();
        this.UEVar = new HashSet<>();
    }
}
