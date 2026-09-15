package io.github.mortuusars.exposure.util;

public class ByteArrayUtils {
    public static byte[][] splitToParts(byte[] input, int partSize) {
        int parts = (int)Math.ceil(input.length / (double)partSize);
        byte[][] output = new byte[parts][];

        for(int part = 0; part < parts; part++) {
            int start = part * partSize;
            int length = Math.min(input.length - start, partSize);

            byte[] bytes = new byte[length];
            System.arraycopy(input, start, bytes, 0, length);
            output[part] = bytes;
        }

        return output;
    }
}
