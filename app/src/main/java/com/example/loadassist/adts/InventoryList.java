package com.example.loadassist.adts;
import androidx.annotation.NonNull;

import java.util.Iterator;
import com.example.loadassist.interfaces.ListInterface;

public class InventoryList <E> extends ListInterface<E> implements Iterable<E> {

        protected E[] list;         // generic array, underlying data structure

        protected int numElements;  // 0 at ArrayBasedList object instantiation

        // set by find method:
        protected boolean found;  // true if element found, otherwise false
        protected int location;   // indicates location of element when found is true

        protected final int DEFAULT_CAPACITY = 10;

        protected int initial_capacity;

        public InventoryList() {
            list = (E[]) new Object[DEFAULT_CAPACITY];
            initial_capacity = DEFAULT_CAPACITY;
        }

        public InventoryList(int capacity) {

            list = (E[]) new Object[capacity];
            initial_capacity = capacity;
        }




        protected void enlarge() {
            E[] newBiggerArray = (E[]) new
                    Object[list.length + initial_capacity];
            // Object[numElements + initial_capacity];
            if (numElements >= 0) System.arraycopy(list, 0, newBiggerArray, 0, numElements);
            list = newBiggerArray;
        }

        @Override
        public void add(E element) {
            if (numElements == list.length) {
                enlarge();
            }
            list[numElements] = element;
            numElements++;
        }
        
        protected void find(E target)
        // helper method
        // search for the first item on the list such that item.equals(target) is true
        // if a match is found set instance variables:
        //    found to true
        //    location to the array index for item
        // if no match, found must be false
        {
            found = false;
            location = 0;

            while (location < numElements) {
                if (list[location].equals(target)) {
                    found = true;
                    return;
                }
                else {
                    location++;
                }
            }
        }

        /**/
        @Override
        // quick & dirty remove
        public boolean remove (E element) {

            find(element);  // sets found, and when found is true, sets location
            if (found) {
                // replace element to be removed with last element:
                list[location] = list[numElements - 1];
                // clear out element that had been the last element:
                list[numElements - 1] = null;
                numElements--;
            }
            return found;
        }
        /**/

        @Override
        public int size() {
            return numElements;
        }

        @Override
        public boolean isEmpty() {
            return numElements == 0;
        }

        @Override
        public boolean contains (E element) {
            find(element);
            return found;
        }

        @Override
        public E get(E element) {
            find(element);
            if (found) {
                return list[location];
            }
            else {
                return null;
            }
        }

        @Override
        public E get(int index) {
            if (index < 0 || index > numElements - 1) {
                return null;
            }
            else {
                return list[index];
            }
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder listStr = new StringBuilder("---------------\n");
            for (int i = 0; i < numElements; i++) {
                listStr.append(list[i]).append("\n");
            }
            return listStr.toString();
        }

        @NonNull
        @Override
        public Iterator<E> iterator() {
            return new IteratorForThisClass();
        }

        private class IteratorForThisClass implements Iterator<E> {

            int current = 0;

            public boolean hasNext() {
                // return current < size();
                return current < numElements;
            }

            public E next() {
			/* /
			E temp = list[current];
			current++;
			return temp;
			/**/
                return list[current++];
            }
        }
}
