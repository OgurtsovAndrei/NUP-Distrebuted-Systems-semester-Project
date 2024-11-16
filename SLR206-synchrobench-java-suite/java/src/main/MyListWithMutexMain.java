package main;

import my.list.MyListWithMutex;

public class MyListWithMutexMain {
    public static void main(String[] args) {
        MyListWithMutex list = new MyListWithMutex();

        System.out.println("Adding values to the list:");
        list.addInt(5);
        list.addInt(3);
        list.addInt(8);
        list.addInt(1);
        list.printSet();

        System.out.println("\nRemoving value 3 from the list:");
        list.removeInt(3);
        list.printSet();

        System.out.println("\nChecking if the list contains value 8:");
        System.out.println(list.containsInt(8));

        System.out.println("\nChecking if the list contains value 3:");
        System.out.println(list.containsInt(3));

        System.out.println("\nClearing the list:");
        list.clear();
        list.printSet();
    }
}
