/**
 * 三重加密实现
 * 使用48位密钥进行三重S-AES加密
 */
public class TripleEncryption {
    
    /**
     * 三重加密
     * 实现 E(K3, D(K2, E(K1, P)))
     * @param plaintext 明文（ASCII字符串）
     * @param key 48位密钥
     * @return 加密结果
     */
    public static String encrypt(String plaintext, String key) {
        if (key.length() != 48) {
            throw new IllegalArgumentException("密钥必须是48位！");
        }
        
        // 将48位密钥分为三个16位密钥
        String key1 = key.substring(0, 16);
        String key2 = key.substring(16, 32);
        String key3 = key.substring(32, 48);
        
        // 将ASCII文本转换为二进制
        String binary = ASCIIExtension.asciiToBinary(plaintext);
        StringBuilder result = new StringBuilder();
        
        // 按16位分组进行三重加密
        for (int i = 0; i < binary.length(); i += 16) {
            String block = binary.substring(i, Math.min(i + 16, binary.length()));
            // 如果不足16位，用0填充
            while (block.length() < 16) {
                block += "0";
            }
            
            // 第一次加密：E(K1, P)
            String encrypted1 = SimpleSAES.encrypt(block, key1);
            // 第二次解密：D(K2, E(K1, P))
            String decrypted2 = SimpleSAES.decrypt(encrypted1, key2);
            // 第三次加密：E(K3, D(K2, E(K1, P)))
            String encrypted3 = SimpleSAES.encrypt(decrypted2, key3);
            
            result.append(encrypted3);
        }
        
        return result.toString();
    }
    
    /**
     * 三重解密
     * 实现 D(K1, E(K2, D(K3, C)))
     * @param ciphertext 密文（二进制字符串）
     * @param key 48位密钥
     * @return 解密结果
     */
    public static String decrypt(String ciphertext, String key) {
        if (key.length() != 48) {
            throw new IllegalArgumentException("密钥必须是48位！");
        }
        
        // 将48位密钥分为三个16位密钥
        String key1 = key.substring(0, 16);
        String key2 = key.substring(16, 32);
        String key3 = key.substring(32, 48);
        
        StringBuilder result = new StringBuilder();
        
        // 按16位分组进行三重解密
        for (int i = 0; i < ciphertext.length(); i += 16) {
            String block = ciphertext.substring(i, Math.min(i + 16, ciphertext.length()));
            if (block.length() == 16) {
                // 第一次解密：D(K3, C)
                String decrypted1 = SimpleSAES.decrypt(block, key3);
                // 第二次加密：E(K2, D(K3, C))
                String encrypted2 = SimpleSAES.encrypt(decrypted1, key2);
                // 第三次解密：D(K1, E(K2, D(K3, C)))
                String decrypted3 = SimpleSAES.decrypt(encrypted2, key1);
                
                result.append(decrypted3);
            }
        }
        
        return ASCIIExtension.binaryToAscii(result.toString());
    }
    
    /**
     * 测试三重加密功能
     */
    public static void main(String[] args) {
        System.out.println("=== 三重加密测试 ===");
        
        String plaintext = "World";
        String key = "110011001100110011001100110011001100110011001100"; // 48位密钥
        
        System.out.println("明文: " + plaintext);
        System.out.println("密钥: " + key);
        
        try {
            // 加密
            String ciphertext = encrypt(plaintext, key);
            System.out.println("密文: " + ciphertext);
            
            // 解密
            String decrypted = decrypt(ciphertext, key);
            System.out.println("解密: " + decrypted);
            
            // 验证
            boolean success = plaintext.equals(decrypted);
            System.out.println("测试结果: " + (success ? "成功" : "失败"));
            
            if (success) {
                System.out.println("✅ 三重加密功能实现正确！");
            } else {
                System.out.println("❌ 三重加密功能实现有误！");
            }
            
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
