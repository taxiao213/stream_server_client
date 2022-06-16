// IStream.aidl
package vl.vision.app;

// Declare any non-default types here with import statements

interface IStreamService {
    void sendStream(inout byte[] bytes);

    void register();

    void unregister();
}
