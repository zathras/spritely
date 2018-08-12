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


/**
 * Spritely - simple, grid-based sprite animation with a text mode.
 * <br>
 * <br>
 * <center>
 * <img src="doc-files/pink_flying_toaster_morgaine1976-300px.png" alt="*">
 * </center>
 * <p>
 * This package provides a framework for showing an animated grid of 
 * equal-size tiles, which is a limited form of sprite animation.  
 * It was written for educational purposes in an introductory OO
 * class.  It has a few unique features:
 * <ul>
 * <li>It strives to be as simple as possible, so as to not overload
 *     students with language features, unfamiliar idioms, or complex
 *     code to read through.
 * 
 * <li>It does <em>not</em> force the student to do all their work via
 *     callbacks, as is typical for GUIs.  The student is presented with
 *     a single-threaded model, where they ask the frame to display each
 *     animation frame.
 * 
 * <li>It allows programs to be run in text mode, using character-based
 *     graphics.  This allows students to do their development on a
 *     remote server via ssh, so they don't have to do system
 *     administration on their personal computer, like installing
 *     a Java environment, setting the CLASSPATH, or installing an X server.
 * 
 * <li>It restricts the user to equal-sized tiles.  This is more or less
 *     necessary to enable character mode.
 * 
 * <li>It handles the needed logic to maintain a given frame rate.  It
 *     handles the case where a program is suspended and woken back up,
 *     by moving the notion of time forward.
 *
 * <li>It features simple callbacks for mouse clicks and key typed events,
 *     and it simulates these events in text mode.
 * </ul>
 * The main entry point for this package is the class 
 * <a href="SpriteWindow.html"><code>SpriteWindow</code></a>.  For
 * non-tile-based drawing, the alernate entry point
 * <a href="GraphicsWindw.html"><code>GraphicsWindow</code></a> is
 * available. Here's an illustration of the main classes:
 * <p>
 * <img src="doc-files/uml.svg" width="100%" alt="*">
 * <p>
 * An application starts by creating an instance of SpriteWindow.
 * Each time it's ready to show a frame of animation, it calls
 * <code>waitForNextFrame()</code> to wait until it's time to show
 * the frame.  This method returns an <code>AnimationFrame</code>,
 * which the program fills with tiles.  When it's done, it calls
 * <code>showNextFrame()</code> on the <code>SpriteWindow</code> to
 * display it.
 * <p>
 * Some implementation notes are available
 * <a href="doc-files/implementation-notes.html">here</a>.
 * <p>
 * See also 
 * <a href="https://spritely.jovial.com/" target="_top">https://spritely.jovial.com/</a>,
 * and <a href="doc-files/LICENSE.txt">the license</a>.
 *
 *
 *      @author         Bill Foote, http://jovial.com
 *
 */
package edu.calpoly.spritely;
