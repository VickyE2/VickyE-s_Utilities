/* Licensed under Apache-2.0 2024. */
package org.vicky

fun <T> List<T>.getNextOrFirst(currentItem: T): T {
    val currentIndex = this.indexOf(currentItem)

    // Handle the case where the item isn't even in the list
    if (currentIndex == -1) throw IllegalArgumentException("Item not found in list")

    // Add 1 to the index, then wrap around using modulo (%)
    val nextIndex = (currentIndex + 1) % this.size
    return this[nextIndex]
}