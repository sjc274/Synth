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

*


