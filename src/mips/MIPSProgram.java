package mips;

import java.util.ArrayList;
import java.util.List;

public class MIPSProgram {

    public List<MIPSSubroutine> subroutines;

    public MIPSProgram() {
        subroutines = new ArrayList<>();
    }

    public MIPSProgram(List<MIPSSubroutine> subroutines) {
        this.subroutines = subroutines;
    }

}
