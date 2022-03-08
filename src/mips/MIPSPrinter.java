package mips;
import mips.operand.*;
import mips.datatype.*;
import mips.MIPSInstruction.OpCode.*;
import java.util.*;
import java.io.*;

public class MIPSPrinter {
    public static void print(String filename, MIPSProgram mp) throws IOException {
        ArrayList<String> al = new ArrayList<>();
        // Need to make sure main is the first function
        if(!mp.subroutines.get(0).name.equals("main"))
        {
            int main = 0;
            for (int i = 0; i < mp.subroutines.size(); i++) {
                if (mp.subroutines.get(i).name.equals("main"))
                {
                    main = i;
                    break;
                }
            }
            Collections.swap(mp.subroutines, 0, main);
        }
        // I think this has to be at the start of programs
        al.add(".text\n");
        for (MIPSSubroutine subr : mp.subroutines) {
            for (MIPSInstruction mi : subr.instructions) {
                String name = mi.opCode.toString();
                String inst = "";
                if (name.equals(MIPSInstruction.OpCode.LABEL.toString())) {
                    inst = ((MIPSLabelOperand) mi.operands[0]).getValueString() + ":";
                } else if (name.equals(MIPSInstruction.OpCode.SYSCALL.toString())) {
                    inst = "syscall";
                } else {
                    name += " ";
                    if(mi.opCode == MIPSInstruction.OpCode.SW || mi.opCode == MIPSInstruction.OpCode.LW)
                    {
                        inst = name + mi.operands[0] + ", " + mi.operands[1] + "(" + mi.operands[2] + ")";
                    }
                    // IRtoMIPSTranslator puts the operands for branches in the wrong order. Swapping them into the right order here since it's easier.
                    else if(mi.opCode == MIPSInstruction.OpCode.BEQ || mi.opCode == MIPSInstruction.OpCode.BNE || mi.opCode == MIPSInstruction.OpCode.BLT || mi.opCode == MIPSInstruction.OpCode.BGT || mi.opCode == MIPSInstruction.OpCode.BLE || mi.opCode == MIPSInstruction.OpCode.BGE)
                    {
                        inst = name + mi.operands[1] + ", " + mi.operands[2] + ", " + mi.operands[0];
                    }
                    else
                    {
                        for (MIPSOperand mo : mi.operands) {
                            name += mo.getName() + " ,";
                        }
                        name = name.substring(0, name.length() - 1);
                        inst = name;
                    }

                }

                al.add(inst + " \n");
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
