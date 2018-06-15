
package edu.calpoly.spritely;

import java.awt.event.KeyEvent;
/**
 * A functional interface for receiving a notification when a key is typed.
 *
 * @see SpriteWindow#setKeyTypedHandler(KeyTypedHandler)
 */

@FunctionalInterface
public interface KeyTypedHandler {

    void keyTyped(char ch);

}
