package my.list;

import contention.abstractions.AbstractCompositionalIntSet;
import java.util.concurrent.locks.ReentrantLock;

public class MyListWithMutex extends AbstractCompositionalIntSet {
    private Node head;
    private int size = 0;
    private final ReentrantLock lock = new ReentrantLock();

    // Add an item to the set (only if it doesn't already exist)
    @Override
    public boolean addInt(int value) {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    // Remove an item from the set
    @Override
    public boolean removeInt(int value) {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    // Check if the set contains a value
    @Override
    public boolean containsInt(int value) {
        lock.lock();
        try {
            Node current = head;
            while (current != null && current.value < value) {
                current = current.next;
            }
            return current != null && current.value == value;
        } finally {
            lock.unlock();
        }
    }

    // Print all items in the set
    public void printSet() {
        lock.lock();
        try {
            Node current = head;
            while (current != null) {
                System.out.print(current.value + " -> ");
                current = current.next;
            }
            System.out.println("null");
        } finally {
            lock.unlock();
        }
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
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            head = null;
            size = 0;
        } finally {
            lock.unlock();
        }
    }
}