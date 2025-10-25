/**
 * CBC工作模式实现
 * 支持密码分组链模式及错误传播分析
 */
public class CBCMode {
    
    /**
     * CBC模式加密
     * @param plaintext 明文
     * @param key 16位密钥
     * @return 加密结果（包含IV和密文）
     */
    public static String encrypt(String plaintext, String key) {
        // 生成随机IV（16位二进制）
        java.util.Random random = new java.util.Random();
        int ivInt = random.nextInt(65536);
        String iv = Integer.toBinaryString(ivInt);
        while (iv.length() < 16) {
            iv = "0" + iv;
        }
        
        // 将明文转换为二进制
        String binary = ASCIIExtension.asciiToBinary(plaintext);
        StringBuilder result = new StringBuilder();
        result.append("IV: ").append(iv).append("\n");
        
        String previousCipher = iv;
        
        // 按16位分组进行CBC加密
        for (int i = 0; i < binary.length(); i += 16) {
            String block = binary.substring(i, Math.min(i + 16, binary.length()));
            // 如果不足16位，用0填充
            while (block.length() < 16) {
                block += "0";
            }
            
            // 与前一个密文块异或
            String xorResult = xorBinary(block, previousCipher);
            
            // 使用S-AES加密
            String encrypted = SimpleSAES.encrypt(xorResult, key);
            result.append("块").append((i/16 + 1)).append(": ").append(encrypted).append("\n");
            
            previousCipher = encrypted;
        }
        
        return result.toString();
    }
    
    /**
     * CBC模式解密
     * @param encryptedData 加密数据（包含IV和密文）
     * @param key 16位密钥
     * @return 解密结果
     */
    public static String decrypt(String encryptedData, String key) {
        // 处理单行格式：IV: xxx 块1: xxx 块2: xxx ...
        if (!encryptedData.contains("\n")) {
            // 单行格式，需要解析
            String[] parts = encryptedData.split(" ");
            String iv = "";
            java.util.List<String> blocks = new java.util.ArrayList<>();
            
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (part.equals("IV:")) {
                    // IV: 后面跟着的是IV值
                    if (i + 1 < parts.length) {
                        iv = parts[i + 1];
                    }
                } else if (part.startsWith("块") && part.endsWith(":")) {
                    // 块1: 后面跟着的是块值
                    if (i + 1 < parts.length) {
                        blocks.add(parts[i + 1]);
                    }
                }
            }
            
            
            StringBuilder result = new StringBuilder();
            String previousCipher = iv;
            
            // 解密每个密文块
            for (String cipherBlock : blocks) {
                // 使用S-AES解密
                String decrypted = SimpleSAES.decrypt(cipherBlock, key);
                
                // 与前一个密文块异或
                String plainBlock = xorBinary(decrypted, previousCipher);
                result.append(plainBlock);
                
                previousCipher = cipherBlock;
            }
            
            return ASCIIExtension.binaryToAscii(result.toString());
        } else {
            // 多行格式
            String[] lines = encryptedData.split("\n");
            String iv = lines[0].substring(4); // 去掉"IV: "前缀
            
            StringBuilder result = new StringBuilder();
            String previousCipher = iv;
            
            // 解密每个密文块
            for (int i = 1; i < lines.length; i++) {
                String cipherBlock = lines[i].substring(lines[i].indexOf(": ") + 2);
                
                // 使用S-AES解密
                String decrypted = SimpleSAES.decrypt(cipherBlock, key);
                
                // 与前一个密文块异或
                String plainBlock = xorBinary(decrypted, previousCipher);
                result.append(plainBlock);
                
                previousCipher = cipherBlock;
            }
            
            return ASCIIExtension.binaryToAscii(result.toString());
        }
    }
    
    /**
     * CBC模式错误传播测试
     * @param originalCipher 原始密文
     * @param tamperedCipher 篡改后的密文
     * @param key 16位密钥
     * @return 测试结果
     */
    public static String errorPropagationTest(String originalCipher, String tamperedCipher, String key) {
        StringBuilder result = new StringBuilder();
        result.append("=== CBC错误传播测试 ===\n");
        
        // 解密原始密文
        String originalPlain = decrypt(originalCipher, key);
        result.append("原始密文解密结果: ").append(originalPlain).append("\n");
        
        // 解密篡改后的密文
        String tamperedPlain = decrypt(tamperedCipher, key);
        result.append("篡改密文解密结果: ").append(tamperedPlain).append("\n");
        
        // 分析错误传播
        result.append("错误传播分析:\n");
        result.append("- 原始明文: ").append(originalPlain).append("\n");
        result.append("- 篡改后明文: ").append(tamperedPlain).append("\n");
        result.append("- 错误传播: ").append(originalPlain.equals(tamperedPlain) ? "无影响" : "影响后续所有块").append("\n");
        
        return result.toString();
    }
    
    /**
     * 二进制字符串异或运算
     */
    private static String xorBinary(String a, String b) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            result.append(a.charAt(i) == b.charAt(i) ? '0' : '1');
        }
        return result.toString();
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        String plaintext = "Hello World";
        String key = "1100110011001100";
        
        System.out.println("=== CBC工作模式测试 ===");
        System.out.println("明文: " + plaintext);
        System.out.println("密钥: " + key);
        System.out.println();
        
        String encrypted = encrypt(plaintext, key);
        System.out.println("加密结果:");
        System.out.println(encrypted);
        
        String decrypted = decrypt(encrypted, key);
        System.out.println("解密结果: " + decrypted);
        System.out.println("测试结果: " + (plaintext.equals(decrypted) ? "成功" : "失败"));
        
        if (plaintext.equals(decrypted)) {
            System.out.println("✅ CBC工作模式实现正确！");
        } else {
            System.out.println("❌ CBC工作模式实现有误！");
        }
    }
}