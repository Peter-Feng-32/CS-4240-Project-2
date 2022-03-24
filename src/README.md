# Tiger-IR Helper Code & Interpreter

## Build

- Linux/macOS
Prereq: JDK 14
```bash
mkdir build
find src -name "*.java" > sources.txt
javac -d build @sources.txt
```

- Windows CMD
```cmd
mkdir build
dir /A-D /B /S src\*.java > sources.txt
javac.exe -d build @sources.txt
```

## Run the Demo

```bash
java -cp ./build Demo example/example.ir out.ir
```

## Run the Interpreter

```bash
java -cp ./build IRInterpreter example/example.ir
```

The program will read input from the console and print output to the console.

If you have the input stored in a file, use this command to feed it to the program:

```bash
java -cp ./build IRInterpreter example/example.ir < example/example.in
```

And you can redirect the output to a file with this command:

```bash
java -cp ./build IRInterpreter example/example.ir < example/example.in > example.out
```


1000000000
1000000001
2000000001
2000000002
2000000002
-1
-2
2
1800000000
1600000000
1600000000
33000
1
1
8
34816
34816
363003
1000328186
1000328186
0
1
