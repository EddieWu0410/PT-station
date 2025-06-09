package tracker;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Map;
import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

/**
 * Enhanced InfoHash calculator that matches qBittorrent's calculation
 * 
 * The key issues with infoHash calculation are:
 * 1. Bencode libraries may encode data differently
 * 2. The original torrent's info dictionary bytes should be preserved
 * 3. Re-encoding might change the binary representation
 */
public class EnhancedInfoHashCalculator {
    
    /**
     * Calculate infoHash by extracting the original info dictionary bytes
     * from the torrent file, rather than re-encoding the parsed data
     */
    public static String calculateInfoHash(File torrentFile) throws Exception {
        byte[] torrentData = Files.readAllBytes(torrentFile.toPath());
        
        // Find the info dictionary in the raw torrent data
        // Look for the pattern "4:info" which indicates the start of the info dictionary
        int infoStart = findInfoDictionary(torrentData);
        if (infoStart == -1) {
            throw new Exception("Could not find info dictionary in torrent file");
        }
        
        // Extract the info dictionary bytes directly from the original torrent
        byte[] infoBytes = extractInfoBytes(torrentData, infoStart);
        
        // Calculate SHA1 hash
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha1.digest(infoBytes);
        
        // Convert to hex string
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        
        return sb.toString();
    }
    
    /**
     * Find the position of "4:info" in the torrent data
     */
    private static int findInfoDictionary(byte[] data) {
        byte[] pattern = "4:info".getBytes();
        
        for (int i = 0; i <= data.length - pattern.length; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Extract the info dictionary bytes from the original torrent data
     */
    private static byte[] extractInfoBytes(byte[] data, int infoStart) throws Exception {
        // Skip "4:info" to get to the actual dictionary content
        int dictStart = infoStart + 6; // "4:info".length()
        
        if (dictStart >= data.length || data[dictStart] != 'd') {
            throw new Exception("Invalid info dictionary format");
        }
        
        // Find the matching 'e' that closes the info dictionary
        int dictEnd = findMatchingEnd(data, dictStart);
        if (dictEnd == -1) {
            throw new Exception("Could not find end of info dictionary");
        }
        
        // Extract the info dictionary bytes (including 'd' and 'e')
        int length = dictEnd - dictStart + 1;
        byte[] infoBytes = new byte[length];
        System.arraycopy(data, dictStart, infoBytes, 0, length);
        
        return infoBytes;
    }
    
    /**
     * Find the matching 'e' for a dictionary that starts with 'd'
     */
    private static int findMatchingEnd(byte[] data, int start) {
        if (start >= data.length || data[start] != 'd') {
            return -1;
        }
        
        int depth = 0;
        for (int i = start; i < data.length; i++) {
            if (data[i] == 'd' || data[i] == 'l') {
                depth++;
            } else if (data[i] == 'e') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * For comparison: calculate using the re-encoding method (your original approach)
     */
    public static String calculateInfoHashByReencoding(File torrentFile) throws Exception {
        byte[] torrentData = Files.readAllBytes(torrentFile.toPath());
        Bencode bencode = new Bencode();
        
        @SuppressWarnings("unchecked")
        Map<String,Object> meta = bencode.decode(torrentData, Type.DICTIONARY);
        @SuppressWarnings("unchecked")
        Map<String,Object> info = (Map<String,Object>) meta.get("info");
        
        if (info == null) {
            throw new Exception("No info dictionary found");
        }
        
        // Re-encode the info dictionary
        byte[] infoBytes = bencode.encode(info);
        
        // Calculate SHA1
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha1.digest(infoBytes);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        
        return sb.toString();
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java EnhancedInfoHashCalculator <torrent-file>");
            return;
        }
        
        File torrentFile = new File(args[0]);
        if (!torrentFile.exists()) {
            System.out.println("Torrent file not found: " + args[0]);
            return;
        }
        
        try {
            System.out.println("=== InfoHash Calculation Comparison ===");
            System.out.println("File: " + torrentFile.getName());
            
            String directHash = calculateInfoHash(torrentFile);
            System.out.println("Direct extraction method: " + directHash);
            System.out.println("Direct (uppercase):       " + directHash.toUpperCase());
            
            String reencodingHash = calculateInfoHashByReencoding(torrentFile);
            System.out.println("Re-encoding method:       " + reencodingHash);
            System.out.println("Re-encoding (uppercase):  " + reencodingHash.toUpperCase());
            
            if (directHash.equals(reencodingHash)) {
                System.out.println("✓ Both methods produce the same result");
            } else {
                System.out.println("✗ Methods produce different results!");
                System.out.println("This suggests the Bencode re-encoding is changing the data");
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
