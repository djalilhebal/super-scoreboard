# JOverlay

JOverlay (aka **ComCross**) is a
Java Swing library to create overlays for DirectX 9 games.


## Using

```java
import me.kaito.comcross.ComCross;

var view = createView(); // e.g. JWindow
var cc = new ComCross();
cc.setSource(view);
cc.setTarget("LeagueClient.exe");
cc.start();

// That's all.
// Treat the view as you would treat any normal Swing app.
```


## Connecting the dots

Let's put everything relevant on the table.

- League Client (LCU) uses a microservices arch even though they all live on the same computer and communicate only with one another.

- VNC uses a server-client arch.
The server performs actions and sends data (mainly the screen capture) to the client.
The client displays the screen and allows the user to interact with it. The client sends the users' commands (mainly input events) to the server.

- Overlays work similarly. **Actually, they can only work this way.** \
The actual logic (business logic and rendering) is located in a different process, maybe it's even written in a diff framework like Electron, CEF, or Dear ImGui.
    * The screen is displayed inside the fullscreen app (an independent process).
    * Then, it captures input events and forwards them to the main process.
    * Whenever the original view (say, Chromium) repaints, we take the output (a frame/image) and send it to the hooked process, which paints the view in-game.

AFAIK, that's how `gelectron` works. \
In one term: _IPC_.

We can do it.


## Name

- **JOverlay** similar to goverlay/gelectron, except we use J for Java.

- **Serge** as in Chrono Cross (you know, parallel dimensions and all).

- **Com(p(onent))Cross** we use Swing **components** to define and paint stuff, but we display them in a different parallel/**cross** process.

- **ComCross** as mentioned in "connecting the dots":
    * **Com**:
        - **Component**, as in AWT/Swing `Component`s
        - **Computing** as in VNC
        - **Communication** as in IPC
    * **Cross**:
        - It works across different processes (IPC).
        - Also, Chrono Cross, parallel dimensions


## How it should work

Swing side:
- Whenever the view (JLayer? JFrame? Container?) changes, paint it to a `BufferedImage`.
- Export the `BufferedImage` (as PNG? PMP?) to a shared memory.

Native side:
- In the `EndScene` hook, it reads the image and displays it.
- It forwards input events to Swing.


## Pseudocode

```java
/**
 * Notes:
 * - It would be amazing if we could use C#/.NET's `Mutex` class,
 * which is a wrapper around an OS level locking mechanism.
 */
public class Serge {

    private static int CONSUMED_FLAG = 0;
    private static int NOT_CONSUMED_FLAG = 1;

    // RandomAccessFile
    sharedMem = [consumedFlag, ...imageData]

    class FrameProducer {
        // Was the last frame consumed?
        public boolean isConsumed() {
            return sharedMem.get(0) == CONSUMED_FLAG;
        }
        
        public void generate() {
            if (!isConsumed()) {
                return;
            }
            
            // - Create a BufferedImage
            // - Convert it to a PNG(?) ByteArray prob using ByteArrayOutputStream
            // - Write the image data to the shared mem file
            setConsumed(NOT_CONSUMED_FLAG);
        }
    }

    // will be in C/C++
    class FrameConsumer {
        public void setConsumed(byte b) {
            return sharedMem.put(0, b);
        }
        
        /**
         * `D3DXCreateTextureFromFileInMemory`
         */
        void getFrame() {
            if (!isConsumed()) {
                imageData = seekAndStartReading(1);
                image = png(imageData)
                setConsumed(CONSUMED_FLAG);
            }
            draw(image);
        }
    }

}
```


## Image format

We have a few requirements:
- Supported by all systems we are interested in (Java and DirectX 9).
- Supports transparency\*. \
  (\* Transparency, translucency, opacity, alpha channel? Idk, same diff.)
- Super fast to encode and decode.

Options:
- **BMP**
    * :green_square: Supports transparency.
    * :green_square: Uncompressed.
- **PNG**.
    * :green_square: Supports transparency.
    * :yellow_square: Compressed.
- **PPM** seems cool
    * :red_square: Does not support transparency.
    * :green_square: Uncompressed.

### See

- [Reading/Loading an Image (The Java™ Tutorials > 2D Graphics > Working with Images)](https://docs.oracle.com/javase/tutorial/2d/images/loadimage.html)
    * "Image I/O has built-in support for GIF, PNG, JPEG, BMP, and WBMP."

- [D3DXIMAGE_FILEFORMAT enumeration (D3dx9tex.h) - Win32 apps | Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/direct3d9/d3dximage-fileformat)
    * "Describes the supported image file formats."
    * Lists BMP, PNG, JPG (JPEG standard), etc.

---

END.
