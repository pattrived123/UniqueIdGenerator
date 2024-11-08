package com.palash;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.palash.service.UniqueTrackingIdGenerator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UniqueTrackingIdGeneratorTests {
	
	private Set<String> generatedIds;

    @BeforeEach
    public void setUp() {
        generatedIds = new HashSet<>();
    }

    @Test
    public void testGenerateIdUniquenessAndFormat() {
        int numberOfIds = 1000; // Generate 1000 IDs to test uniqueness and format.
        Set<String> generatedIds = new HashSet<>();

        for (int i = 0; i < numberOfIds; i++) {
            String id = UniqueTrackingIdGenerator.generateId();

            // Check uniqueness by adding each ID to the set and ensuring no duplicates.
            Assertions.assertTrue(generatedIds.add(id), "Duplicate ID found: " + id);

            // Check that the ID has the correct length of 24 characters.
            Assertions.assertEquals(24, id.length(), "ID length should be 24 characters.");

            // Check that the ID contains only valid hexadecimal characters.
            Assertions.assertTrue(id.matches("[0-9A-Fa-f]+"), "ID should only contain hexadecimal characters.");
        }
    }
    
    @Test
    public void testGenerateIdUniqueness() {
        int numberOfIds = 1000;
        for (int i = 0; i < numberOfIds; i++) {
            String id = UniqueTrackingIdGenerator.generateId();
            Assertions.assertTrue(generatedIds.add(id), "Duplicate ID found: " + id);
        }
    }

    @Test
    public void testGenerateIdLength() {
        String id = UniqueTrackingIdGenerator.generateId();
        Assertions.assertEquals(24, id.length(), "ID length should be 24 characters.");
    }

    @Test
    public void testGenerateIdHexadecimalFormat() {
        String id = UniqueTrackingIdGenerator.generateId();
        Assertions.assertTrue(id.matches("[0-9A-Fa-f]+"), "ID should only contain hexadecimal characters.");
    }

    @Test
    public void testGenerateIdChronologicalOrder() {
        String id1 = UniqueTrackingIdGenerator.generateId();
        String id2 = UniqueTrackingIdGenerator.generateId();
        
        // Convert timestamps from the generated IDs to long for comparison.
        long timestamp1 = Long.parseLong(id1.substring(0, 8), 16);
        long timestamp2 = Long.parseLong(id2.substring(0, 8), 16);

        // Assert that the second ID's timestamp is greater than or equal to the first.
        Assertions.assertTrue(timestamp2 >= timestamp1, "IDs should be generated in chronological order.");
    }

    @Test
    public void testGenerateIdThreadSafety() throws InterruptedException {
        int numberOfThreads = 50;
        int idsPerThread = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        
        // Use a concurrent set to store generated IDs from multiple threads.
        Set<String> concurrentIds = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < idsPerThread; j++) {
                    String id = UniqueTrackingIdGenerator.generateId();
                    concurrentIds.add(id);
                }
            });
        }

        // Shutdown and await termination of all threads.
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Verify that all generated IDs are unique across threads.
        Assertions.assertEquals(numberOfThreads * idsPerThread, concurrentIds.size(),
                "Generated IDs should be unique across threads.");
    }
}
