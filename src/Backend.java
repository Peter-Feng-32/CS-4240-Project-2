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


    }
}
