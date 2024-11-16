package my.list;

import contention.abstractions.AbstractCompositionalIntSet;

import java.util.concurrent.locks.ReentrantLock;

public class MyListWithHandOverHandLocking extends AbstractCompositionalIntSet {
    private Node head;
    private int size = 0;

    @Override
    public boolean addInt(int value) {
        if (head == null) {
            synchronized (this) {
                if (head == null) {
                    head = new Node(value, null);
                    size++;
                    return true;
                }
            }
        }

        Node prev = null;
        Node current = head;

        current.lock();
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
            synchronized (this) {
                size++;
            }
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

        current.lock();
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
                return false; // Value not found
            }

            if (prev != null) {
                prev.next = current.next;
            } else {
                head = current.next;
            }
            synchronized (this) {
                size--;
            }
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
        current.lock();
        try {
            while (current != null && current.value < value) {
                Node next = current.next;
                if (next != null) {
                    next.lock();
                }
                current.unlock();
                current = next;
            }

            return current != null && current.value == value;
        } finally {
            if (current != null) {
                current.unlock();
            }
        }
    }

    public void printSet() {
        Node current = head;
        while (current != null) {
            current.lock();
            try {
                System.out.print(current.value + " -> ");
                Node next = current.next;
                if (next != null) {
                    next.lock();
                }
                current.unlock();
                current = next;
            } finally {
                if (current != null) {
                    current.unlock();
                }
            }
        }
        System.out.println("null");
    }

    private static class Node {
        int value;
        Node next;
        private final ReentrantLock lock = new ReentrantLock();

        Node(int value, Node next) {
            this.value = value;
            this.next = next;
        }

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            return size;
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            head = null;
            size = 0;
        }
    }
}
