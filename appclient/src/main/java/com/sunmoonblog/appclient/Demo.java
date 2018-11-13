package com.sunmoonblog.appclient;

import android.support.annotation.NonNull;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.tools.ir.client.InstantRunClient;
import com.android.tools.ir.client.UpdateMode;
import com.android.tools.ir.runtime.ApplicationPatch;
import com.android.tools.ir.runtime.Paths;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Demo {
    // you can call AndroidDebugBridge.init() and terminate() only once
    // createBridge() and disconnectBridge() can be called as many times as you want

    private List<IDevice> list = new ArrayList<IDevice>();

    public void init() {
        AndroidDebugBridge.init(false);
    }

    public void finish() {
        AndroidDebugBridge.terminate();
    }

    public void usingWaitLoop() throws Exception {
        AndroidDebugBridge adb = AndroidDebugBridge.createBridge();

        try {
            int trials = 10;
            while (trials > 0) {
                Thread.sleep(50);
                if (adb.isConnected()) {
                    break;
                }
                trials--;
            }

            if (!adb.isConnected()) {
                System.out.println("Couldn't connect to ADB server");
                return;
            }

            trials = 10;
            while (trials > 0) {
                Thread.sleep(50);
                if (adb.hasInitialDeviceList()) {
                    break;
                }
                trials--;
            }

            if (!adb.hasInitialDeviceList()) {
                System.out.println("Couldn't list connected devices");
                return;
            }

            for (IDevice device : adb.getDevices()) {
//                System.out.println("- " + device.getSerialNumber());
                list.add(device);

//                if (device.getSerialNumber().equals("192.168.56.101:5555")) {
//                    InstantRunClient c = new InstantRunClient("com.sunmoonblog.instantrun_demo", new ILogger() {
//                        @Override
//                        public void error(Throwable t, String msgFormat, Object... args) {
//
//                        }
//
//                        @Override
//                        public void warning(String msgFormat, Object... args) {
//
//                        }
//
//                        @Override
//                        public void info(String msgFormat, Object... args) {
//
//                        }
//
//                        @Override
//                        public void verbose(String msgFormat, Object... args) {
//
//                        }
//                    }, 0);
//                    c.showToast(device, "叫爸爸");
//                }
            }
        } finally {
//            AndroidDebugBridge.disconnectBridge();
        }
    }

    public void usingDeviceChangeListener() throws Exception {
        AndroidDebugBridge.addDeviceChangeListener(new AndroidDebugBridge.IDeviceChangeListener() {
            // this gets invoked on another thread, but you probably shouldn't count on it
            public void deviceConnected(IDevice device) {
                System.out.println("* " + device.getSerialNumber());
            }

            public void deviceDisconnected(IDevice device) {
            }

            public void deviceChanged(IDevice device, int changeMask) {
            }
        });

        AndroidDebugBridge adb = AndroidDebugBridge.createBridge();

        Thread.sleep(1000);
        if (!adb.isConnected()) {
            System.out.println("Couldn't connect to ADB server");
        }

        AndroidDebugBridge.disconnectBridge();
    }

    public static void main(String[] args) throws Exception {
        Demo demo = new Demo();

        demo.init();

        // I think this is the way to go for non-interactive or short-running applications
        System.out.println("Demo using wait loop to ensure connection to ADB server and then enumerate devices synchronously");
        demo.usingWaitLoop();

        // this looks like the right way for interactive or long-running applications
//        System.out.println("Demo using DeviceChangeListener to get information about devices asynchronously");
//        demo.usingDeviceChangeListener();


        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        IDevice target = chooseDevice(demo, reader);

        while (true) {
            System.out.println("\n==== 操作列表 ====");
            System.out.println("==== [1]/[S] send message ====");
            System.out.println("==== [2]/[R] restart activity ====");
            System.out.println("==== [3]/[E] exit ====");
            System.out.println("==== [4]/[P] put patches ====");

            System.out.print("请选择操作: ");
            String action = reader.readLine().trim();
            if ("1".equals(action) || "s".equals(action) || "S".equals(action)) {
                sendMessage(reader, target, 0);
            } else if ("2".equals(action) || "r".equals(action) || "R".equals(action)) {
                restartActivity(reader, target);
            } else if ("3".equals(action) || "e".equals(action) || "E".equals(action)) {
                break;
            } else if ("4".equals(action) || "p".equals(action) || "P".equals(action)) {
                putPatches(reader, target);
            }
        }

        demo.finish();
    }

    private static void putPatches(BufferedReader reader, IDevice target) throws IOException {
        long token;
        while (true) {
            try {
                System.out.print("请输入app token(见build-info.xml文件): ");
                token = Long.parseLong(reader.readLine().trim());
                System.out.println("已输入app token: " + token);
                break;
            } catch (Exception e) {
                // NO OP
                e.printStackTrace();
            }
        }

        InstantRunClient client = new InstantRunClient("com.sunmoonblog.instantrun_demo", new DummyLogger(), token);
        List<ApplicationPatch> changes = new ArrayList<ApplicationPatch>();

        // InstantRunClient.FileTransfer transfer = InstantRunClient.FileTransfer.createHotswapPatch()
        byte[] data = DexMain.getDex();
        ApplicationPatch patch = new ApplicationPatch(Paths.RELOAD_DEX_FILE_NAME, data);
        changes.add(patch);

        client.pushPatches(target, Long.toString(System.currentTimeMillis()), changes, UpdateMode.HOT_SWAP, false, true);

    }

    @NonNull
    private static IDevice chooseDevice(Demo demo, BufferedReader reader) throws IOException {
        IDevice target = null;
        while (target == null) {
            System.out.println("\n==== 设备列表 ====");
            for (int i = 0; i < demo.list.size(); i++) {
                System.out.println("==== [" + (i + 1) + "] " + demo.list.get(i) + " ====");
            }
            System.out.println("========\n");


            System.out.print("请选择设备: ");
            String tmp = reader.readLine().trim();
            try {
                int pos = Integer.parseInt(tmp) - 1;
                if (pos >= 0 && pos < demo.list.size()) {
                    target = demo.list.get(pos);
                }
            } catch (Exception e) {
                // NO OP
            }
        }
        System.out.println("已选择设备: " + target);
        return target;
    }

    private static void restartActivity(BufferedReader reader, IDevice target) throws IOException {
        long token;
        while (true) {
            try {
                System.out.print("请输入app token(见build-info.xml文件): ");
                token = Long.parseLong(reader.readLine().trim());
                System.out.println("已输入app token: " + token);
                break;
            } catch (Exception e) {
                // NO OP
                e.printStackTrace();
            }
        }
        InstantRunClient client = new InstantRunClient("com.sunmoonblog.instantrun_demo", new DummyLogger(), token);
        client.showToast(target, "看仔细啦我要重启Activity啦");
        client.restartActivity(target);
    }

    private static void sendMessage(BufferedReader reader, IDevice target, int token) throws IOException {
        String message;
        InstantRunClient client = new InstantRunClient("com.sunmoonblog.instantrun_demo", new DummyLogger(), token);
        while (true) {
            System.out.print("Send message: ");
            message = reader.readLine().trim();
            if (message.isEmpty()) {
                continue;
            }
            if ("bye".equals(message)) {
                break;
            }
            client.showToast(target, message);
        }
        System.out.println("=== BYE ===");
    }
}
