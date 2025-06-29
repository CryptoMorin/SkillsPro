package org.skills.utils;

import java.lang.reflect.Array;

public final class ArrayUtils {
    /**
     * Removes the element at the specified position from the specified array.
     * All subsequent elements are shifted to the left (subtracts one from
     * their indices).
     * <p>
     * This method returns a new array with the same elements of the input
     * array except the element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     * </p>
     * <p>
     * If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     * </p>
     *
     * @param array the array to remove the element from, may not be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the element
     * at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 2.1
     */
    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static Object remove(final Object array, final int index) {
        final int length = Array.getLength(array);
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }
        final Object result = Array.newInstance(array.getClass().getComponentType(), length - 1);
        System.arraycopy(array, 0, result, 0, index);
        if (index < length - 1) {
            System.arraycopy(array, index + 1, result, index, length - index - 1);
        }
        return result;
    }
}
