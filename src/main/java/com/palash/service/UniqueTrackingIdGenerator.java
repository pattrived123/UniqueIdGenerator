package com.palash.service;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

public class UniqueTrackingIdGenerator {

	//Constants defining the structure of ObjectID components.
    private static final int MACHINE_IDENTIFIER;
    private static final short PROCESS_IDENTIFIER;
    private static final AtomicInteger COUNTER = new AtomicInteger(new SecureRandom().nextInt());

    // Static block to initialize the machine and process identifiers.
    static {
        try {
            MACHINE_IDENTIFIER = createMachineIdentifier();
            PROCESS_IDENTIFIER = createProcessIdentifier();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a 3-byte machine identifier.
     * Tries to use the MAC address to ensure uniqueness across machines in distributed environment.
     * If MAC address cannot be retrieved, falls back to a random number.
     * @return A 3-byte machine identifier.
     * @throws Exception if there is an error accessing the MAC address.
     */
    private static int createMachineIdentifier() throws Exception {
        int machinePiece;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                }
            }
            machinePiece = sb.toString().hashCode();
        } catch (Throwable t) {
            machinePiece = (new SecureRandom().nextInt());
        }
        //Use only 3 bytes of the hash code to fit in the 3-byte machine identifier field.
        return machinePiece & 0x00FFFFFF;
    }

    /**
     * Generates a 2-byte process identifier.
     * Uses the process ID if available; otherwise, it generates a random number.
     * Ensures uniqueness among processes on the same machine.
     * @return A 2-byte process identifier.
     */
    private static short createProcessIdentifier() {
        int processId;
        try {
        	// Uses the hash code of the runtime process name, usually including the process ID.
            processId = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().hashCode();
        } catch (Throwable t) {
            processId = new SecureRandom().nextInt();
        }
        return (short) (processId & 0xFFFF); // Use only 2 bytes
    }

    /**
     * Generates a unique 12-byte ID similar to MongoDB's ObjectId.
     * Combines a 4-byte timestamp, 3-byte machine identifier,
     * 2-byte process identifier, and 3-byte counter to ensure uniqueness.
     * @return A hexadecimal string representation of the generated ID.
     */
    public static String generateId() {
        byte[] id = new byte[12];
        
        // 4-byte timestamp: number of seconds since the Unix epoch.
        int timestamp = (int) (Instant.now().getEpochSecond());
        id[0] = (byte) (timestamp >> 24);
        id[1] = (byte) (timestamp >> 16);
        id[2] = (byte) (timestamp >> 8);
        id[3] = (byte) (timestamp);

        // 3-byte machine identifier.
        id[4] = (byte) (MACHINE_IDENTIFIER >> 16);
        id[5] = (byte) (MACHINE_IDENTIFIER >> 8);
        id[6] = (byte) (MACHINE_IDENTIFIER);

        // 2-byte process identifier.
        id[7] = (byte) (PROCESS_IDENTIFIER >> 8);
        id[8] = (byte) (PROCESS_IDENTIFIER);

        // 3-byte counter: ensures uniqueness for IDs generated in the same second.
        int counter = COUNTER.getAndIncrement();
        id[9] = (byte) (counter >> 16);
        id[10] = (byte) (counter >> 8);
        id[11] = (byte) (counter);

        // Convert the 12-byte ID to a hexadecimal string representation.
        return toHexString(id);
    }

    /**
     * Converts a byte array to a hexadecimal string.
     * @param bytes The byte array to convert.
     * @return Hexadecimal string representation of the byte array.
     */
    private static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
}
