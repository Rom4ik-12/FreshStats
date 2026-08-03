package com.freshstats.util;

import java.lang.reflect.Method;

public class NetworkUtils {
    private static Method sendPacketMethod = null;

    public static void sendPacket(Object networkHandler, Object packet) {
        if (networkHandler == null || packet == null) return;
        try {
            if (sendPacketMethod == null) {
                for (Method m : networkHandler.getClass().getMethods()) {
                    if (m.getParameterCount() == 1 && m.getReturnType() == void.class) {
                        Class<?> paramType = m.getParameterTypes()[0];
                        if (paramType.isAssignableFrom(packet.getClass())) {
                            sendPacketMethod = m;
                            sendPacketMethod.setAccessible(true);
                            break;
                        }
                    }
                }
            }
            if (sendPacketMethod != null) {
                sendPacketMethod.invoke(networkHandler, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
