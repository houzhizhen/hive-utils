package com.baidu.hive.util.ast;

import java.util.List;
import java.util.function.Consumer;

import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.parse.ASTNode;

public class ASTWalker {

    public static void walk(ASTNode node, Consumer<ASTNode> consumer) {
        consumer.accept(node);
        List<? extends Node> children = node.getChildren();
        if (children != null) {
            for (Node sub : children) {
                walk((ASTNode)sub, consumer);
            }
        }

    }
}
