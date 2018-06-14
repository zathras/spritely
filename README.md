# spritely
Sprite Animation Library
![Toaster](docs/pink_flying_toaster_morgaine1976-300px.png "Toaster")

Spritely is a small sprite animation library designed for educational
purposes in an introductory OO class.  It has a few unique features:

  *  It's strives to be as simple as possible, so as to not overload
     students with language features, unfamiliar idioms, or complex
     code to read through.

  *  It does <em>not</em> force the student to do all their work via
     callbacks, as is typical for GUIs.  The student is presented with
     a single-threaded model, where they ask the frame to display each
     animation frame.

  *  It allows programs to be run in text mode, using character-based
     graphics.  It uses the ANSI escape sequence to move the cursor
     to the home position in order to achieve an animation effect in
     this mode.

  *  It restricts the user to equal-sized tiles.  (This is more or less
     necessary to enable character mode).

  *  It handles the needed logic to maintain a given frame rate.  It
     handles the case where a program is suspended and woken back up,
     by moving the notion of time forward.
