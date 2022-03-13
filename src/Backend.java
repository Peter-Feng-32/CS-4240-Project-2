import ir.*;
import ir.datatype.IRArrayType;
import ir.datatype.IRIntType;
import ir.datatype.IRType;
import ir.operand.IRConstantOperand;
import ir.operand.IROperand;
import ir.operand.IRVariableOperand;
import mips.*;

import java.io.PrintStream;
import java.util.*;

public class Backend {
    public static void main(String[] args) throws Exception {
        // Parse the IR file
        IRReader irReader = new IRReader();
        IRProgram program = irReader.parseIRFile(args[0]);
        MIPSProgram mips = IRToMIPSTranslator.translate(program);
        MIPSProgram alloc = RegAllocator.IntraBlockAlloc(mips);
        String filename = "spimTEST.s";
        MIPSPrinter.print(filename, alloc);

        //todo: Check if register allocation makes error with prime numbers from 6-24 go away.
        // This error is currently caused because our virtual registers don't start off by being initialized, but this shouldn't be a problem once we use physical registers after register allocation because they will actually have values.
        // I'm writing this here to make sure we don't forget to check it after register allocation.
    }
}
