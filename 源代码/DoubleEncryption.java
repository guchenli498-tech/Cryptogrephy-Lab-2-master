/**
 * 双重加密实现
 * 使用32位密钥进行双重S-AES加密
 */
public class DoubleEncryption {
    
    /**
     * 双重加密
     * @param plaintext 明文（ASCII字符串）
     * @param key 32位密钥
     * @return 加密结果
     */
    public static String encrypt(String plaintext, String key) {
        if (key.length() != 32) {
            throw new IllegalArgumentException("密钥必须是32位！");
        }
        
        // 将32位密钥分为两个16位密钥
        String key1 = key.substring(0, 16);
        String key2 = key.substring(16, 32);
        
        // 将ASCII文本转换为二进制
        String binary = ASCIIExtension.asciiToBinary(plaintext);
        StringBuilder result = new StringBuilder();
        
        // 按16位分组进行双重加密
        for (int i = 0; i < binary.length(); i += 16) {
            String block = binary.substring(i, Math.min(i + 16, binary.length()));
            // 如果不足16位，用0填充
            while (block.length() < 16) {
                block += "0";
            }
            
            // 第一次加密
            String encrypted1 = SimpleSAES.encrypt(block, key1);
            // 第二次加密
            String encrypted2 = SimpleSAES.encrypt(encrypted1, key2);
            
            result.append(encrypted2);
        }
        
        return result.toString();
    }
    
    /**
     * 双重解密
     * @param ciphertext 密文（二进制字符串）
     * @param key 32位密钥
     * @return 解密结果
     */
    public static String decrypt(String ciphertext, String key) {
        if (key.length() != 32) {
            throw new IllegalArgumentException("密钥必须是32位！");
        }
        
        // 将32位密钥分为两个16位密钥
        String key1 = key.substring(0, 16);
        String key2 = key.substring(16, 32);
        
        StringBuilder result = new StringBuilder();
        
        // 按16位分组进行双重解密
        for (int i = 0; i < ciphertext.length(); i += 16) {
            String block = ciphertext.substring(i, Math.min(i + 16, ciphertext.length()));
            if (block.length() == 16) {
                // 第一次解密
                String decrypted1 = SimpleSAES.decrypt(block, key2);
                // 第二次解密
                String decrypted2 = SimpleSAES.decrypt(decrypted1, key1);
                
                result.append(decrypted2);
            }
        }
        
        return ASCIIExtension.binaryToAscii(result.toString());
    }
    
    /**
     * 测试双重加密功能
     */
    public static void main(String[] args) {
        System.out.println("=== 双重加密测试 ===");
        
        String plaintext = "Hello";
        String key = "11001100110011001100110011001100"; // 32位密钥
        
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
                System.out.println("✅ 双重加密功能实现正确！");
            } else {
                System.out.println("❌ 双重加密功能实现有误！");
            }
            
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
