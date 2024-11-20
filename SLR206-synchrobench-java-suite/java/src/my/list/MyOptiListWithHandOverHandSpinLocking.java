package my.list;

import contention.abstractions.AbstractCompositionalIntSet;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MyOptiListWithHandOverHandSpinLocking extends AbstractCompositionalIntSet {
    private Node head;
    private final AtomicInteger size = new AtomicInteger(0);

    @Override
    public boolean addInt(int value) {
        if (head == null) {
            synchronized (this) {
                if (head == null) {
                    head = new Node(value, null, new SpinLock());
                    size.incrementAndGet();
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

	    SpinLock headLock = prev != null ? null : new SpinLock();
            Node newNode = new Node(value, current, headLock);
            if (prev != null) {
                prev.next = newNode;
            } else {
		head.demote();
                head = newNode;
            }
            size.incrementAndGet();
	    newNode.unlock();
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
                return false;
            }

            if (prev != null) {
                prev.next = current.next;
            } else {
                head = current.next;
		head.promote();
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

    private static class SpinLock {
        private final AtomicBoolean lock = new AtomicBoolean(false);

        SpinLock() { }

        void lock() {
            while (!lock.compareAndSet(false, true)) {
                // Thread.yield();
            }
        }

        void unlock() {
            lock.set(false);
        }
    }

    private static class Node {
        int value;
        Node next;
    	private SpinLock headLock = null;
        private volatile boolean lock = false;

        Node(int value, Node next, SpinLock headlock) {
            this.value = value;
            this.next = next;
	        this.headLock = headLock;
        }

	void promote() {
            headLock = new SpinLock();
	}

	void demote() { headLock = null; }

        void lock() {
            if (headLock != null) { headLock.lock(); }

            while (lock) {
                // Thread.yield();
            }

            lock = true;
        }

        void unlock() {
            lock = false;
            if (headLock != null) { headLock.unlock(); }
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
