package my.list;

import contention.abstractions.AbstractCompositionalIntSet;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MyListWithHandOverHandMyOptimizedVolatileSpinLocking extends AbstractCompositionalIntSet {
    private Node head;
    private final AtomicInteger size = new AtomicInteger(0);

    private final ReentrantLock headLock = new ReentrantLock();

    @Override
    public boolean addInt(int value) {
        headLock.lock();
        if (head == null) {
            head = new Node(value, null);
            size.incrementAndGet();
            headLock.unlock();
            return true;
        }

        Node prev = null;
        Node current = head;

        current.lock();
        headLock.unlock();
        try {
            while (current != null && current.value < value) {
                if (prev != null) {
                    prev.unlock();
                }
                prev = current;
                current = current.next;
                if (current != null) {
                    current.lock();
                }
            }

            if (current != null && current.value == value) {
                return false;
            }

            Node newNode = new Node(value, current);
            if (prev != null) {
                prev.next = newNode;
            } else {
                head = newNode;
            }
            size.incrementAndGet();
            return true;
        } finally {
            if (current != null) {
                current.unlock();
            }
            if (prev != null) {
                prev.unlock();
            }
        }
    }

    @Override
    public boolean removeInt(int value) {
        if (head == null) {
            return false;
        }

        Node prev = null;
        Node current = head;

        headLock.lock();
        current.lock();
        headLock.unlock();
        try {
            while (current != null && current.value < value) {
                if (prev != null) {
                    prev.unlock();
                }
                prev = current;
                current = current.next;
                if (current != null) {
                    current.lock();
                }
            }

            if (current == null || current.value != value) {
                return false;
            }

            if (prev != null) {
                prev.next = current.next;
            } else {
                head = current.next;
            }
            size.decrementAndGet();
            return true;
        } finally {
            if (current != null) {
                current.unlock();
            }
            if (prev != null) {
                prev.unlock();
            }
        }
    }

    @Override
    public boolean containsInt(int value) {
        if (head == null) {
            return false;
        }

        Node current = head;
        while (current != null && current.value < value) {
            current = current.next;
        }

        return current != null && current.value == value;
    }

    public void printSet() {
        Node current = head;
        while (current != null) {
            System.out.print(current.value + " -> ");
            current = current.next;
        }
        System.out.println("null");
    }

    private static class Node {
        int value;
        Node next;
        private volatile boolean lock = false;

        Node(int value, Node next) {
            this.value = value;
            this.next = next;
        }

        void lock() {
            while (lock) {
                // Thread.yield();
            }
            lock = true;
        }

        void unlock() {
            lock = false;
        }
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public void clear() {
        synchronized (this) {
            head = null;
            size.set(0);
        }
    }
}
