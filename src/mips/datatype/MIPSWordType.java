package mips.datatype;

public class MIPSWordType extends MIPSType {

    private static MIPSWordType instance;

    private MIPSWordType() {}

    public static MIPSWordType get() {
        if (instance == null) {
            instance = new MIPSWordType();
        }
        return instance;
    }

    @Override
    public String toString() {
        return "word";
    }

}
