
package edu.calpoly.spritely;

import java.util.Objects;

/**
 * Represents a width and height value.  May be used as a Map key.
 */

public final class Size {

    public final int width;
    public final int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int hashCode() {
        return (31 + width * 31 + height) * 31;
        // What java.util.Arrays ends up doing, without all the autoboxing
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Size) {
            Size so = (Size) other;
            return so.width == width && so.height == height;
        } else {
            return false;
        }
    }
}
