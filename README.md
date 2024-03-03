# Enumerative Program Synthesizer - Based on TopDown Algorithm

# Introduction ✏️
* CFG: context-free grammer. The simple interpretion of complex grammmer. e.g. E := 1 | 2 | 3 | x
* AST: abstract syntax tree. A tree structure obtained by parsing the program.
* TopDown Search Algorithm:
```
TopDownSearch((T, N, P, s), E):
  worklist := { s }
  while (worklist is not empty)
  AST := worklist.remove()
  if (AST is complete) if (AST satisfies E) return AST
  else worklist.addAll(expand(AST, (T, N, P, s)))
```
* Examples: these are input-output examples, like x=1, y=2, z=3 -> 3

# Search Prioritization 🤖
##### This is the biggest challange of the Program Synthesis. The search space of one example could be infinite.
Here are the prioritizations we used in this project:

## pruning:
The idea of pruning is to filter the worklist every time after expanding with some constraints.
* It must have all three variables x, y, z in a complete program.
* The ASTNode with Ite cannot be in the child node.
* No single variables as a program, such as x.

# Execution
In the Synth directory, you can execute the program using the following command:

`$ java -cp lib:target/synth-1.0.jar synth.Main <path-to-examples-file>`

# Evaluation 🛠️
The example we use here is:

x=1, y=2, z=3 -> 6

x=3, y=2, z=2 -> 7

x=2, y=3, z=4 -> 9

The result is:

<img width="697" alt="Screenshot 2024-03-03 at 2 58 59 PM" src="https://github.com/sjc274/Synth/assets/113268694/f0a62d12-4477-45f1-8327-2936dd067b12">

# Concern 🤔
- Using more advanced evaluation system to evaluate the program sketch.
- Another issue of the synthesizer is the big amount of duplicate programs produced by
the cartesian product.
