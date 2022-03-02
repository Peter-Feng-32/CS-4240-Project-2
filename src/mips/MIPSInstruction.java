package mips;

import mips.operand.MIPSOperand;

public class MIPSInstruction {

    public enum OpCode {
        LI, LUI,
        ADD, ADDI, SUB, SUBI, MULT, MULO, DIV, AND, ANDI, OR, ORI,
        B,
        BEQ, BNE, BLT, BGT, BLE, BGE,
        JR,
        JAL,
        LW, SW,
        LABEL;
        //Add more instructions as necessary when they come up during writing the translator.

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public OpCode opCode;

    public MIPSOperand[] operands;


    public MIPSInstruction() {}

    public MIPSInstruction(OpCode opCode, MIPSOperand[] operands) {
        this.opCode = opCode;
        this.operands = operands;

    }

}
