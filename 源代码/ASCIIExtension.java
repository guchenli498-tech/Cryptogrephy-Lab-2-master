/**
 * ASCII字符串扩展功能
 * 支持ASCII字符串的S-AES加解密
 */
public class ASCIIExtension {
    
    /**
     * 将ASCII字符串转换为二进制字符串
     */
    public static String asciiToBinary(String ascii) {
        StringBuilder result = new StringBuilder();
        for (char c : ascii.toCharArray()) {
            String binary = Integer.toBinaryString(c);
            // 确保每个字符都是8位
            while (binary.length() < 8) {
                binary = "0" + binary;
            }
            result.append(binary);
        }
        return result.toString();
    }
    
    /**
     * 将二进制字符串转换为ASCII字符串
     */
    public static String binaryToAscii(String binary) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 8) {
            if (i + 8 <= binary.length()) {
                String byteStr = binary.substring(i, i + 8);
                int ascii = Integer.parseInt(byteStr, 2);
                if (ascii >= 32 && ascii <= 126) { // 只处理可打印ASCII字符
                    result.append((char) ascii);
                }
            }
        }
        return result.toString();
    }
    
    /**
     * 将ASCII字符串分组为16位块进行加密
     */
    public static String encryptAscii(String asciiText, String key) {
        String binary = asciiToBinary(asciiText);
        StringBuilder result = new StringBuilder();
        
        // 按16位分组
        for (int i = 0; i < binary.length(); i += 16) {
            String block = binary.substring(i, Math.min(i + 16, binary.length()));
            // 如果不足16位，用0填充
            while (block.length() < 16) {
                block += "0";
            }
            
            String encrypted = SimpleSAES.encrypt(block, key);
            result.append(encrypted);
        }
        
        return result.toString();
    }
    
    /**
     * 将加密的二进制字符串解密为ASCII字符串
     */
    public static String decryptAscii(String encryptedBinary, String key) {
        StringBuilder result = new StringBuilder();
        
        // 按16位分组解密
        for (int i = 0; i < encryptedBinary.length(); i += 16) {
            String block = encryptedBinary.substring(i, Math.min(i + 16, encryptedBinary.length()));
            if (block.length() == 16) {
                String decrypted = SimpleSAES.decrypt(block, key);
                result.append(decrypted);
            }
        }
        
        return binaryToAscii(result.toString());
    }
    
    /**
     * 测试ASCII扩展功能
     */
    public static void main(String[] args) {
        System.out.println("=== ASCII字符串扩展测试 ===");
        
        String asciiText = "Hi";
        String key = "1100110011001100";
        
        System.out.println("原始ASCII文本: " + asciiText);
        System.out.println("密钥: " + key);
        
        // 转换为二进制
        String binary = asciiToBinary(asciiText);
        System.out.println("转换为二进制: " + binary);
        
        // 加密
        String encrypted = encryptAscii(asciiText, key);
        System.out.println("加密结果: " + encrypted);
        
        // 解密
        String decrypted = decryptAscii(encrypted, key);
        System.out.println("解密结果: " + decrypted);
        
        // 验证
        boolean success = asciiText.equals(decrypted);
        System.out.println("测试结果: " + (success ? "成功" : "失败"));
        
        if (success) {
            System.out.println("✅ ASCII扩展功能实现正确！");
        } else {
            System.out.println("❌ ASCII扩展功能实现有误！");
        }
    }
}
