package my.list;

import contention.abstractions.AbstractCompositionalIntSet;


public class MyLinkedList extends AbstractCompositionalIntSet {
    private Node head;
    private int size = 0;

    // Add an item to the set (only if it doesn't already exist)
    @Override
    public boolean addInt(int value) {
        if (containsInt(value)) {
            return false;
        }
        if (head == null || head.value > value) {
            head = new Node(value, head);
            size++;
            return true;
        }
        Node current = head;
        while (current.next != null && current.next.value < value) {
            current = current.next;
        }
        current.next = new Node(value, current.next);
        size++;
        return true;
    }

    // Remove an item from the set
    @Override
    public boolean removeInt(int value) {
        if (head == null) {
            return false;
        }
        if (head.value == value) {
            head = head.next;
            size--;
            return true;
        }
        Node current = head;
        while (current.next != null) {
            if (current.next.value == value) {
                current.next = current.next.next;
                size--;
                return true;
            }
            if (current.next.value > value) {
                break;
            }
            current = current.next;
        }
        return false;
    }

    // Check if the set contains a value
    @Override
    public boolean containsInt(int value) {
        Node current = head;
        while (current != null && current.value < value) {
            current = current.next;
        }
        return current != null && current.value == value;
    }

    // Print all items in the set
    public void printSet() {
        Node current = head;
        while (current != null) {
            System.out.print(current.value + " -> ");
            current = current.next;
        }
        System.out.println("null");
    }

    // Node class
    private static class Node {
        int value;
        Node next;

        Node(int value, Node next) {
            this.value = value;
            this.next = next;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        head = null;
        size = 0;
    }

//    public static void main(String[] args) {
//        MyLinkedList set = new MyLinkedList();
//        System.out.println(set.addInt(1)); // Output: true
//        System.out.println(set.addInt(2)); // Output: true
//        System.out.println(set.addInt(3)); // Output: true
//        System.out.println(set.addInt(2)); // Output: false (already exists)
//        set.printSet(); // Output: 1 -> 2 -> 3 -> null
//        System.out.println(set.removeInt(2)); // Output: true
//        System.out.println(set.removeInt(4)); // Output: false (not in set)
//        set.printSet(); // Output: 1 -> 3 -> null
//        System.out.println("Size: " + set.size()); // Output: Size: 2
//        set.clear();
//        set.printSet(); // Output: null
//        System.out.println("Size: " + set.size()); // Output: Size: 0
//    }
}
