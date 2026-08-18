package com.cyyaw.admin.entity.utils;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 树对象
 */
@Data
public class TreeResponseEntity<T> {

    /**
     * 森林
     */
    private List<Node<T>> root = new ArrayList<>();

    /**
     * 添加数据
     */
    public void add(Node<T> node) {
        if (!root.isEmpty()) {
            String pid = node.getPid();
            if (StrUtil.isBlank(pid)) {
                root.add(node);
            } else {
                // 添加到节点
                boolean h = false;
                for (Node<T> tree : root) {
                    boolean b = this.addTreeToForest(tree, node);
                    if (b) {
                        h = true;
                        break;
                    }
                }
                if (!h) {
                    root.add(node);
                }
            }
            // 整理森林
            for (int i = 0; i < root.size(); ) {
                boolean h = false;
                Node<T> tNode = root.get(i);
                String treePid = tNode.getPid();
                if (StrUtil.isNotBlank(treePid)) {
                    for (int j = 0; j < root.size(); j++) {
                        if (j != i) {
                            Node<T> tryParent = root.get(j);
                            boolean b = this.addTreeToForest(tryParent, tNode);
                            if (b) {
                                h = true;
                                break;
                            }
                        }
                    }
                    if (h) {
                        root.remove(i);
                    } else {
                        i++;
                    }
                } else {
                    i++;
                }
            }
        } else {
            root.add(node);
        }
    }


    private boolean addTreeToForest(Node<T> tree, Node<T> node) {
        String treeId = tree.getId();
        String pid = node.getPid();
        List<Node<T>> children = tree.getChildren();
        if (treeId.equals(pid)) {
            children.add(node);
            return true;
        } else {
            for (Node<T> childNode : children) {
                boolean b = addTreeToForest(childNode, node);
                if (b) {
                    return true;
                }
            }
        }
        return false;
    }


    @Data
    public static class Node<S> {

        /**
         * 节点ID
         */
        private String id;

        /**
         * 父级ID
         */
        private String pid;

        /**
         * 名称
         */
        private String title;

        /**
         * 子节点
         */
        private List<Node<S>> children = new ArrayList<>();

        /**
         * 数据
         */
        private S data;

    }
}
