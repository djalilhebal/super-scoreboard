# JOverlay

Java Swing library to create overlays for DirectX 9 games.

## How it should work

Swing side:
- Whenever the view (JLayer? JFrame? Container?) changes, paint it to a `BufferedImage`.
- Export the `BufferedImage` (as PNG? PMP?) to a shared memory.

Native side:
- In the `EndScene` hook, it reads the image and displays it.
- It forwards input events to Swing.

## Name

- **JOverlay** similar to goverlay, except we use J for Java.

- **Serge** as in Chrono Cross (you know, parallel dimensions and all).

- **Com(p(onent))Cross** we use Swing **components** to define and paint components, but we display them in a different parallel/**cross** process.


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


## See

- [Reading/Loading an Image (The Java™ Tutorials > 2D Graphics > Working with Images)](https://docs.oracle.com/javase/tutorial/2d/images/loadimage.html)
    * "Image I/O has built-in support for GIF, PNG, JPEG, BMP, and WBMP."

- [D3DXIMAGE_FILEFORMAT enumeration (D3dx9tex.h) - Win32 apps | Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/direct3d9/d3dximage-fileformat)
    * "Describes the supported image file formats."
    * Lists BMP, PNG, JPG (JPEG standard), etc.
