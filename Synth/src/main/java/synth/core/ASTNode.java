package synth.core;

import synth.cfg.Symbol;
import java.util.ArrayList;

import java.util.List;

public class ASTNode {
    private final Symbol symbol;    
    private final List<ASTNode> children;

    public ASTNode(Symbol symbol, List<ASTNode> children) {
        this.symbol = symbol;
        this.children = children;
    }

    public ASTNode(ASTNode node) {
        this.symbol = node.getSymbol();
        this.children = new ArrayList<>();
        for(ASTNode child : node.getChildren()){
            this.children.add(child);
        }
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public ASTNode getChild(int index) {
        return children.get(index);
    }

    public void setChild(ASTNode child, int index){
        this.children.set(index, child);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(symbol);
        String separator = "";
        if (!children.isEmpty()) {
            builder.append("(");
            for (ASTNode child : children) {
                builder.append(separator);
                separator = ", ";
                builder.append(child);
            }
            builder.append(")");
        }
        return builder.toString();
    }
}
