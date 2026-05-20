package com.myproject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class VideoClipLinkList {
    private class Node {
        Node(Object o){
            this.o = o;
        }
        Object o;
        Node next;
    }

    private Node first;
    private Node last; // 火車尾的指標
    private int size = 0; //長度

    public void add(Object elem){
        var node = new Node(elem);
        if(first == null) {
            first = node;
            last = node; // 第一個節點同時也是最後一個節點
        }
        else {
            last.next = node;
            last = node; // 把火車尾更新為最新節點
        }
        size++; // 長度加 1
    }

    public int size(){
        return this.size;
    }

	public void saveToFile(String outputPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            Node current = first;
            while (current != null) {
                // current.o.toString() 會自動呼叫 ClipData 的格式化字串輸出
                writer.write(current.o.toString()); 
                writer.newLine(); // 換行
                current = current.next; // 移到下一節車廂
            }
        } catch (IOException e) {
            System.err.println("寫入檔案時發生錯誤: " + e.getMessage());
        }
    }

    public Object get(int index) {
        checkSize(index);
        return findElemOf(index);
    }

    private void checkSize(int index) throws IndexOutOfBoundsException {
        if(index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                "Index: %d, Size: %d".formatted(index, size) );
        }
    }

    private Object findElemOf(int index) {
        var count = 0;
        var current = first;
        while(count < index) {
            current = current.next;
            count++;
        }
        return current.o;
    }
}