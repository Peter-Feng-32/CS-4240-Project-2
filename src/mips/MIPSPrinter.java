package mips;
import mips.operand.*;
import mips.datatype.*;
import mips.MIPSInstruction.OpCode.*;
import java.util.*;
import java.io.*;

public class MIPSPrinter {
    public static void print(String filename, MIPSProgram mp) throws IOException {
        ArrayList<String> al = new ArrayList<>();
        for (MIPSSubroutine subr : mp.subroutines) {
            for (MIPSInstruction mi : subr.instructions) {
                String name = mi.opCode.toString();
                String inst = "";
                if (name.equals(MIPSInstruction.OpCode.LABEL.toString())) {
                    inst = ((MIPSLabelOperand) mi.operands[0]).getValueString() + ":";
                } else if (name.equals(MIPSInstruction.OpCode.SYSCALL.toString())) {
                    //q? Not sure what to do here, nothing?
                } else {
                    inst = name + " ";
                    for (MIPSOperand mo : mi.operands) {
                        name += mo.getName() + " ,";
                    }
                    inst = inst.substring(0, inst.length() - 2);
                }
                al.add(inst);
            }
        }
        File file = new File(filename);
        file.createNewFile();
        FileWriter wr = new FileWriter(file);
        for (String s : al) {
            wr.write(s);
        }
        wr.close();
    }
}
