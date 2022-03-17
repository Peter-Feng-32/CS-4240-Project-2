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
        IRProgram program = irReader.parseIRFile(args[1]);
        MIPSProgram mips = IRToMIPSTranslator.translate(program);
        MIPSProgram alloc;
        if(args[0].equals("intra"))
        {
             alloc = RegAllocator.IntraBlockAlloc(mips);
        }
        else if (args[0].equals("naive")){
            alloc = RegAllocator.NaiveAlloc(mips);
        }
        else
        {
            throw new Exception("Invalid allocator");
        }
        MIPSPrinter.print(args[2], alloc);

        //todo: Check if register allocation makes error with prime numbers from 6-24 go away.
        // This error is currently caused because our virtual registers don't start off by being initialized, but this shouldn't be a problem once we use physical registers after register allocation because they will actually have values.
        // I'm writing this here to make sure we don't forget to check it after register allocation.
    }
}
