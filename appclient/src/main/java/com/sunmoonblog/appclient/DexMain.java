package com.sunmoonblog.appclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DexMain {

    public static byte[] getDex() {
        InputStream is = DexMain.class.getResourceAsStream("/classes.dex");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            is.close();
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }


    public static void main(String[] args) throws IOException {

        System.out.println(getDex());

    }
}
