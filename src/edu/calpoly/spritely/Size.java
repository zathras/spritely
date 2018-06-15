/*
 * Copyright © 2018, Bill Foote, Cal Poly, San Luis Obispo, CA
 * 
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files (the “Software”), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.calpoly.spritely;

import java.util.Objects;

/**
 * Represents a width and height value.  May be used as a Map key.
 *
 *      @author         Bill Foote, http://jovial.com
 */

public final class Size {

    /**
     * The width
     */
    public final int width;

    /**
     * The height
     */
    public final int height;

    /**
     * Create a new, immutable Size object
     *
     * @param width     The width
     * @param height    The height
     */
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
