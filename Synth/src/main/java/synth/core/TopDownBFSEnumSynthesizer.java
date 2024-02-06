package synth.core;

import synth.cfg.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collector;
import java.lang.Thread;



public class TopDownBFSEnumSynthesizer implements ISynthesizer {

    /**
     * Synthesize a program f(x, y, z) based on a context-free grammar and examples
     *
     * @param cfg      the context-free grammar
     * @param examples a list of examples
     * @return the program or null to indicate synthesis failure
     */
    @Override
    public Program synthesize(CFG cfg, List<Example> examples) {
        NonTerminal returnSymbol = new NonTerminal("E");
        List<ASTNode> worklist = new ArrayList<ASTNode>();
        ASTNode astnode = new ASTNode(returnSymbol, new ArrayList<>());
        worklist.add(astnode);

        while(!worklist.isEmpty()){

            ASTNode ast = worklist.getFirst();
            worklist.removeFirst();

            System.out.println("The first element of the current worklist is: ");
            System.out.println(ast);
            System.out.println("\n");

            if(complete(ast) && satisfy(ast, examples)){
                return new Program(ast);
            }else{
                
                worklist.addAll(expand(ast, cfg));
                worklist.removeIf(this::prunning);
                sort(worklist);
                System.out.println("The worklist is: \n");
                System.out.println(worklist);
                // try {
                //     // Sleep for 2 seconds (2000 milliseconds)
                //     Thread.sleep(2000);
                // } catch (InterruptedException e) {
                //     // Handle the exception if needed
                //     e.printStackTrace();
                // }
            }
        }

        return null;
    }

    // Take a root ASTNode and recursively get the number of its all non-terminal children.
    public int numberOfNonTerminals(ASTNode astnode){
        int number = 0;
        if(astnode.getSymbol().isNonTerminal()){
            return 1;
        }else{
            for(ASTNode child : astnode.getChildren()){
                number += numberOfNonTerminals(child);
            }
            return number;
        }
    }

    // Check if there is any Ite as child
    public boolean IteAsChildren(ASTNode astnode){
        if(IteAmount(astnode) > 0){
            return true;
        }else{
            return false;
        }
    }

    public int IteAmount(ASTNode astnode){
        int amount = 0;
        for(ASTNode child : astnode.getChildren()){
            if(child.getSymbol().getName() == "Ite"){
                amount++;
            }
            if(!child.getChildren().isEmpty()){
                amount += IteAmount(child);
            }
        }
        return amount;
    }

   
    public int numX(ASTNode astnode){
        int num = 0;
        for(ASTNode child : astnode.getChildren()){
            if(child.getSymbol().getName() == "x"){
                num++;
            }else{
                num += numX(child);
            }
        }
        return num;
    }

    public int numY(ASTNode astnode){
        int num = 0;
        for(ASTNode child : astnode.getChildren()){
            if(child.getSymbol().getName() == "y"){
                num++;
            }else{
                num += numY(child);
            }
        }
        return num;
    }

    public int numZ(ASTNode astnode){
        int num = 0;
        for(ASTNode child : astnode.getChildren()){
            if(child.getSymbol().getName() == "z"){
                num++;
            }else{
                num += numZ(child);
            }
        }
        return num;
    }

    // Check if the astnode has all x, y and z variables in it
    public boolean hasAllVariables(ASTNode astnode){
        int numX = numX(astnode);
        int numY = numY(astnode);
        int numZ = numZ(astnode);
        if(numX <= 0 || numY <= 0 || numZ <= 0){
            return false;
        }else{
            return true;
        }
    }

    

    // Prunning for eligible ASTNodes following the rules:
    // 1. Ite is not a child in any level of this astnode.
    // 2. x, y, z, 1, 2, 3 are not seen as valid program
    // 3. complete nodes'size must be more than 3 (> 3)
    // 4. complete nodes must have x, y and z all three variables in it.
    public boolean prunning(ASTNode astnode){
        if(!hasAllVariables(astnode) && complete(astnode)){
            return true;
        }
        if(IteAsChildren(astnode)){
            return true;
        }
        switch (astnode.getSymbol().getName()) {
            case "x":
            case "y":    
            case "z":
            case "1":
            case "2":
            case "3":
                return true;
            default:
                break;
        }
        switch(astnode.getSymbol().getName()){
            case "Ite":
            if(numberOfNonTerminals(astnode) > 6){
                return true;
            }else{
                return false;
            }
            
            default:
            if(numberOfNonTerminals(astnode) > 4){
                return true;
            }else{
                return false;
            }
            
        }
    }

    public int sizeOfAST(ASTNode astnode){
        if(complete(astnode)){ // completed astnode to the front
            return 0;
        }
        int size = 1;
        for(ASTNode child : astnode.getChildren()){
            if(child.getSymbol().isNonTerminal()){
                size += 2;
            }else{
                size += sizeOfAST(child);
            }
        }
        return size;
    }



    // Merge Sort ASTNodes with least Non Terminals to the front.
    public void sort(List<ASTNode> worklist){
        Comparator<ASTNode> comparator = Comparator
            .comparingInt(this::sizeOfAST);
        Collections.sort(worklist, comparator);
    }

    public boolean complete(ASTNode astnode){
        if(astnode.getSymbol().isNonTerminal()){
            return false;
        }

        for(ASTNode ast : astnode.getChildren()){
            if(!complete(ast)){
                return false;
            }
        }
        return true;
    }

    public boolean satisfy(ASTNode astnode, List<Example> examples){
        if(!complete(astnode)){
            return false;
        }else{
            System.out.println("/// EVALUATING THE PROGRAM: ");
            Program program = new Program(astnode);
            System.out.println(program);
            System.out.println("\n");
            for(Example example : examples){
                System.out.println("EXAMPLE: ");
                System.out.println(example);
                System.out.println("\n");
                System.out.println("THE RESULT OF PROGRAM IS: ");
                System.out.println(Interpreter.evaluate(program, example.getInput()));
                if(Interpreter.evaluate(program, example.getInput()) != example.getOutput()){
                    return false;
                }
            }
            return true;
        }

    }


    // Return a List of ASTNodes that is expanded from a Non Terminal symbol (B or E)
    public List<ASTNode> NonTerminalExpand(ASTNode astnode, CFG cfg){
        if(astnode.getSymbol().isTerminal()){
            return new ArrayList<ASTNode>();
        }
        List<ASTNode> addList = new ArrayList<ASTNode>();
        NonTerminal retSymbol = new NonTerminal(astnode.getSymbol().getName());
        List<Production> productions = new ArrayList<Production>();
        productions = cfg.getProductions(retSymbol);
        for(Production production : productions){
            List<ASTNode> children = new ArrayList<ASTNode>();
            for(Symbol arg : production.getArgumentSymbols()){
                ASTNode child = new ASTNode(arg, new ArrayList<ASTNode>());
                children.add(child);
            }
            ASTNode ast = new ASTNode(production.getOperator(), children);
            addList.add(ast);
        }

        return addList;
    }

    // Takes a List of List of ASTNodes and return the cartesian product of all the lists of ASTNodes RECURSIVELY
    // For example: ((x, y, z), (x, y))  --- > ((x, x), (x, y), (y, x), (y, y), (z, x), (z, y))
    List<List<ASTNode>> cartesianProduct(List<List<ASTNode>> lists){
        List<List<ASTNode>> resultLists = new ArrayList<List<ASTNode>>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<ASTNode>());
            return resultLists;
        } else {
            List<ASTNode> firstList = lists.get(0);
            List<List<ASTNode>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            for (ASTNode condition : firstList) {
                for (List<ASTNode> remainingList : remainingLists) {
                    ArrayList<ASTNode> resultList = new ArrayList<ASTNode>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }


    public boolean hasNonTerminalChild(ASTNode astnode){
        // return false if the node is leaf
        if(astnode.getChildren().isEmpty()){
            return false;
        }

        // loop through all childs of the astnode
        for(ASTNode child : astnode.getChildren()){
            if(child.getSymbol().isTerminal()){
                return false;
            }
        }

        return true;
    }

    // Expand all children of the current ASTNode, and return the list of all expanding results
    public List<ASTNode> expand(ASTNode astnode, CFG cfg){
        List<ASTNode> addlist = new ArrayList<ASTNode>();
        if(complete(astnode)){
            System.out.println("The addlist of astnode ");
            System.out.println(astnode);
            System.out.println("is: \n");
            System.out.println(addlist);
            return addlist;
        }
        List<List<ASTNode>> expansions = new ArrayList<List<ASTNode>>();
        int index = 0;
        if(astnode.getSymbol().isNonTerminal()){
            return NonTerminalExpand(astnode, cfg);
        }else if(!hasNonTerminalChild(astnode)){
            for(ASTNode child : astnode.getChildren()){
                expansions.add(expand(child, cfg));
                index++;
            }
            List<List<ASTNode>> listsOfChildren = cartesianProduct(expansions);
            for(List<ASTNode> children : listsOfChildren){
                ASTNode node = new ASTNode(astnode.getSymbol(), children);
                addlist.add(node);
            }
        }else{
            for(ASTNode child : astnode.getChildren()){
                expansions.add(NonTerminalExpand(child, cfg));
                index++;
            }
            List<List<ASTNode>> listsOfChildren = cartesianProduct(expansions);
            for(List<ASTNode> children : listsOfChildren){
                ASTNode node = new ASTNode(astnode.getSymbol(), children);
                addlist.add(node);
            }
        }



        System.out.println("The addlist of astnode ");
        System.out.println(astnode);
        System.out.println("is: \n");
        System.out.println(addlist);

        // if(astnode.getSymbol().getName() == "Add"){
        //     try {
        //             // Sleep for 2 seconds (2000 milliseconds)
        //             Thread.sleep(2000);
        //         } catch (InterruptedException e) {
        //             // Handle the exception if needed
        //             e.printStackTrace();
        //         }
        // }
        
        return addlist;
    }




    // Expanding Function. Expand the first level of the AST with non terminal symbol.
    // For example:
    //          T                  T                      T                           T
    //         / \      --- >     / \                    / \                         / \
    //        N   N              T   T                  T   T          --- >        T   T
    //                                                 / \ / \                     / \ / \
    //                                                N  N N  N                   T  T T  T
    //
    // The argument astnode in this function is always the root of the tree
}

