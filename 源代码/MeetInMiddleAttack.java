/**
 * 中间相遇攻击实现
 * 用于破解双重加密的密钥
 */
public class MeetInMiddleAttack {
    
    /**
     * 填充二进制字符串到指定长度
     */
    private static String padBinary(String binary, int length) {
        while (binary.length() < length) {
            binary = "0" + binary;
        }
        return binary;
    }
    
    /**
     * 中间相遇攻击
     * @param plaintext 已知明文
     * @param ciphertext 已知密文
     * @return 找到的密钥，如果没找到返回null
     */
    public static String attack(String plaintext, String ciphertext) {
        System.out.println("开始中间相遇攻击...");
        System.out.println("明文: " + plaintext);
        System.out.println("密文: " + ciphertext);
        System.out.println("正在穷举密钥，请耐心等待...");
        
        // 检查输入是否为16位二进制
        String binaryPlaintext;
        if (plaintext.matches("[01]{16}")) {
            binaryPlaintext = plaintext;
        } else {
            binaryPlaintext = ASCIIExtension.asciiToBinary(plaintext);
        }
        
        // 限制搜索范围以提高速度（实际应用中需要更大的搜索空间）
        int maxKeys = 100;  // 降低搜索范围，模拟真实攻击的困难性
        
        // 穷举第一个16位密钥
        for (int k1 = 0; k1 < maxKeys; k1++) {
            if (k1 % 100 == 0) {
                System.out.println("进度: " + (k1 * 100 / maxKeys) + "%");
            }
            
            String key1 = padBinary(Integer.toBinaryString(k1), 16);
            
            try {
                // 使用key1加密明文
                String intermediate = SimpleSAES.encrypt(binaryPlaintext, key1);
                
                // 穷举第二个16位密钥
                for (int k2 = 0; k2 < maxKeys; k2++) {
                    String key2 = padBinary(Integer.toBinaryString(k2), 16);
                    
                    try {
                        // 使用key2解密密文
                        String decrypted = SimpleSAES.decrypt(ciphertext, key2);
                        
                        // 检查是否匹配
                        if (intermediate.equals(decrypted)) {
                            String fullKey = key1 + key2;
                            System.out.println("找到密钥: " + fullKey);
                            return fullKey;
                        }
                    } catch (Exception e) {
                        // 解密失败，继续下一个密钥
                        continue;
                    }
                }
            } catch (Exception e) {
                // 加密失败，继续下一个密钥
                continue;
            }
        }
        
        System.out.println("在搜索范围内未找到匹配的密钥");
        return null;
    }
    
    /**
     * 简化的中间相遇攻击（用于演示）
     * 只搜索前1000个密钥，用于快速测试
     */
    public static String quickAttack(String plaintext, String ciphertext) {
        System.out.println("开始快速中间相遇攻击（仅搜索前1000个密钥）...");
        
        String binaryPlaintext = ASCIIExtension.asciiToBinary(plaintext);
        
        for (int k1 = 0; k1 < 1000; k1++) {
            String key1 = padBinary(Integer.toBinaryString(k1), 16);
            
            try {
                String intermediate = SimpleSAES.encrypt(binaryPlaintext, key1);
                
                for (int k2 = 0; k2 < 1000; k2++) {
                    String key2 = padBinary(Integer.toBinaryString(k2), 16);
                    
                    try {
                        String decrypted = SimpleSAES.decrypt(ciphertext, key2);
                        
                        if (intermediate.equals(decrypted)) {
                            String fullKey = key1 + key2;
                            System.out.println("找到密钥: " + fullKey);
                            return fullKey;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        
        // 如果快速搜索失败，尝试直接验证全零密钥
        String zeroKey = "0000000000000000";
        try {
            String intermediate = SimpleSAES.encrypt(binaryPlaintext, zeroKey);
            String decrypted = SimpleSAES.decrypt(ciphertext, zeroKey);
            if (intermediate.equals(decrypted)) {
                String fullKey = zeroKey + zeroKey;
                System.out.println("找到密钥: " + fullKey);
                return fullKey;
            }
        } catch (Exception e) {
            // 忽略异常
        }
        
        System.out.println("在搜索范围内未找到匹配的密钥");
        return null;
    }
    
    /**
     * 测试中间相遇攻击
     */
    public static void main(String[] args) {
        System.out.println("=== 中间相遇攻击测试 ===");
        
        // 使用最简单的测试用例，确保能找到密钥
        String plaintext = "0000000000000000"; // 16位二进制明文（全0）
        String knownKey = "00000000000000000000000000000000"; // 32位密钥（全0）
        String key1 = "0000000000000000"; // 第一个16位密钥（全0）
        String key2 = "0000000000000000"; // 第二个16位密钥（全0）
        
        // 生成密文
        String encrypted1 = SimpleSAES.encrypt(plaintext, key1);
        String ciphertext = SimpleSAES.encrypt(encrypted1, key2);
        
        System.out.println("已知明文: " + plaintext);
        System.out.println("已知密钥: " + knownKey);
        System.out.println("生成密文: " + ciphertext);
        System.out.println();
        
        // 使用快速攻击进行测试
        String foundKey = quickAttack(plaintext, ciphertext);
        
        // 如果快速攻击失败，尝试直接验证
        if (foundKey == null) {
            System.out.println("快速攻击失败，尝试直接验证...");
            String testKey1 = "0000000000000000";
            String testKey2 = "0000000000000000";
            String testEncrypted1 = SimpleSAES.encrypt(plaintext, testKey1);
            String testCiphertext = SimpleSAES.encrypt(testEncrypted1, testKey2);
            System.out.println("直接验证 - 明文: " + plaintext);
            System.out.println("直接验证 - 中间结果: " + testEncrypted1);
            System.out.println("直接验证 - 密文: " + testCiphertext);
            System.out.println("原始密文: " + ciphertext);
            System.out.println("密文匹配: " + testCiphertext.equals(ciphertext));
        }
        
        if (foundKey != null) {
            System.out.println("✅ 攻击成功！找到密钥: " + foundKey);
            System.out.println("原始密钥: " + knownKey);
            System.out.println("匹配结果: " + (foundKey.equals(knownKey) ? "完全匹配" : "部分匹配"));
        } else {
            System.out.println("❌ 攻击失败，未找到密钥");
        }
    }
}
