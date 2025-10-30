/**
 * S-AES算法实现
 * 基于官方文档的完整实现
 */
public class SimpleSAES {
    
    // S盒（标准S-AES S盒）
    private static final int[][] S_BOX = {
        {9, 4, 10, 11},
        {13, 1, 8, 5},
        {6, 2, 0, 3},
        {12, 14, 15, 7}
    };
    
    // 逆S盒（标准S-AES逆S盒）
    private static final int[][] INV_S_BOX = {
        {10, 5, 9, 11},
        {1, 7, 8, 15},
        {6, 0, 2, 3},
        {12, 4, 13, 14}
    };
    
    // 轮常数
    private static final String RCON1 = "10000000";
    private static final String RCON2 = "00110000";
    
    // 扩展密钥
    private static String w2, w3, w4, w5;
    
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
     * 填充二进制字符串到指定长度
     */
    private static String padBinary(String binary, int length) {
        while (binary.length() < length) {
            binary = "0" + binary;
        }
        return binary;
    }
    
    /**
     * 半字节替换
     */
    private static String subBytes(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i += 4) {
            String nibble = input.substring(i, i + 4);
            int row = Integer.parseInt(nibble.substring(0, 2), 2);
            int col = Integer.parseInt(nibble.substring(2, 4), 2);
            int value = S_BOX[row][col];
            String binary = padBinary(Integer.toBinaryString(value), 4);
            result.append(binary);
        }
        return result.toString();
    }
    
    /**
     * 逆半字节替换
     */
    private static String invSubBytes(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i += 4) {
            String nibble = input.substring(i, i + 4);
            int row = Integer.parseInt(nibble.substring(0, 2), 2);
            int col = Integer.parseInt(nibble.substring(2, 4), 2);
            int value = INV_S_BOX[row][col];
            String binary = padBinary(Integer.toBinaryString(value), 4);
            result.append(binary);
        }
        return result.toString();
    }
    
    /**
     * 行移位
     */
    private static String shiftRow(String input) {
        String firstRow = input.substring(0, 8);
        String secondRow = input.substring(8, 16);
        String shiftedSecondRow = secondRow.substring(4) + secondRow.substring(0, 4);
        return firstRow + shiftedSecondRow;
    }
    
    /**
     * GF(2^4)有限域乘法表
     */
    private static int[][] gfMultTable = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
        {0, 2, 4, 6, 8, 10, 12, 14, 3, 1, 7, 5, 11, 9, 15, 13},
        {0, 3, 6, 5, 12, 15, 10, 9, 11, 8, 13, 14, 7, 4, 1, 2},
        {0, 4, 8, 12, 3, 7, 11, 15, 6, 2, 14, 10, 5, 1, 13, 9},
        {0, 5, 10, 15, 7, 2, 13, 8, 14, 11, 4, 1, 9, 12, 3, 6},
        {0, 6, 12, 10, 11, 13, 7, 1, 5, 3, 9, 15, 14, 8, 2, 4},
        {0, 7, 14, 9, 15, 8, 1, 6, 13, 10, 3, 4, 2, 5, 12, 11},
        {0, 8, 3, 11, 6, 14, 5, 13, 12, 4, 15, 7, 10, 2, 9, 1},
        {0, 9, 1, 8, 2, 11, 3, 10, 4, 13, 5, 12, 6, 15, 7, 14},
        {0, 10, 7, 13, 14, 4, 9, 3, 15, 5, 8, 2, 1, 11, 6, 12},
        {0, 11, 5, 14, 10, 1, 15, 4, 7, 12, 2, 9, 13, 6, 8, 3},
        {0, 12, 11, 7, 5, 9, 14, 2, 10, 6, 1, 13, 15, 3, 4, 8},
        {0, 13, 9, 4, 1, 12, 8, 5, 2, 15, 11, 6, 3, 14, 10, 7},
        {0, 14, 15, 1, 13, 3, 2, 12, 9, 7, 6, 8, 4, 10, 11, 5},
        {0, 15, 13, 2, 9, 6, 4, 11, 1, 14, 12, 3, 8, 7, 5, 10}
    };
    
    /**
     * GF(2^4)有限域乘法
     */
    private static int gfMultiply(int a, int b) {
        if (a < 0 || a > 15 || b < 0 || b > 15) return 0;
        return gfMultTable[a][b];
    }
    
    /**
     * 列混淆 (列主序实现)
     */
    private static String mixColumns(String input) {
        // 列主序：s00, s10, s01, s11
        int s00 = Integer.parseInt(input.substring(0, 4), 2);
        int s10 = Integer.parseInt(input.substring(4, 8), 2);
        int s01 = Integer.parseInt(input.substring(8, 12), 2);
        int s11 = Integer.parseInt(input.substring(12, 16), 2);

        int new_s00 = s00 ^ gfMultiply(4, s10);
        int new_s10 = gfMultiply(4, s00) ^ s10;
        int new_s01 = s01 ^ gfMultiply(4, s11);
        int new_s11 = gfMultiply(4, s01) ^ s11;

        String result = padBinary(Integer.toBinaryString(new_s00), 4);
        result += padBinary(Integer.toBinaryString(new_s10), 4);
        result += padBinary(Integer.toBinaryString(new_s01), 4);
        result += padBinary(Integer.toBinaryString(new_s11), 4);
        return result;
    }
    
    /**
     * 逆列混淆 (列主序实现)
     */
    private static String invMixColumns(String input) {
        int s00 = Integer.parseInt(input.substring(0, 4), 2);
        int s10 = Integer.parseInt(input.substring(4, 8), 2);
        int s01 = Integer.parseInt(input.substring(8, 12), 2);
        int s11 = Integer.parseInt(input.substring(12, 16), 2);

        int s00_new = gfMultiply(9, s00) ^ gfMultiply(2, s10);
        int s10_new = gfMultiply(2, s00) ^ gfMultiply(9, s10);
        int s01_new = gfMultiply(9, s01) ^ gfMultiply(2, s11);
        int s11_new = gfMultiply(2, s01) ^ gfMultiply(9, s11);

        String result = padBinary(Integer.toBinaryString(s00_new), 4);
        result += padBinary(Integer.toBinaryString(s10_new), 4);
        result += padBinary(Integer.toBinaryString(s01_new), 4);
        result += padBinary(Integer.toBinaryString(s11_new), 4);
        return result;
    }
    
    /**
     * 半字节循环移位
     */
    private static String rotNib(String input) {
        if (input.length() == 8) {
            return input.substring(4) + input.substring(0, 4);
        }
        return input;
    }
    
    /**
     * 半字节替换（用于密钥扩展）
     */
    private static String subNib(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i += 4) {
            String nibble = input.substring(i, i + 4);
            int row = Integer.parseInt(nibble.substring(0, 2), 2);
            int col = Integer.parseInt(nibble.substring(2, 4), 2);
            int value = S_BOX[row][col];
            String binary = padBinary(Integer.toBinaryString(value), 4);
            result.append(binary);
        }
        return result.toString();
    }
    
    /**
     * 密钥扩展
     */
    private static void keyExpansion(String key) {
        String w0 = key.substring(0, 8);
        String w1 = key.substring(8, 16);
        
        String rotNibW1 = rotNib(w1);
        String subNibRotW1 = subNib(rotNibW1);
        w2 = xorBinary(xorBinary(w0, RCON1), subNibRotW1);
        
        w3 = xorBinary(w2, w1);
        
        String rotNibW3 = rotNib(w3);
        String subNibRotW3 = subNib(rotNibW3);
        w4 = xorBinary(xorBinary(w2, RCON2), subNibRotW3);
        
        w5 = xorBinary(w4, w3);
    }
    
    /**
     * 加密
     */
    public static String encrypt(String plaintext, String key) {
        keyExpansion(key);
        
        // 第0轮：密钥加
        String state = xorBinary(plaintext, key);
        
        // 第1轮：完整轮
        state = subBytes(state);
        state = shiftRow(state);
        state = mixColumns(state);
        state = xorBinary(state, w2 + w3);
        
        // 第2轮：无列混淆
        state = subBytes(state);
        state = shiftRow(state);
        state = xorBinary(state, w4 + w5);
        
        return state;
    }
    
    /**
     * 解密
     */
    public static String decrypt(String ciphertext, String key) {
        keyExpansion(key);
        
        // 第0轮：密钥加
        String state = xorBinary(ciphertext, w4 + w5);
        
        // 第1轮：逆行移位 + 逆半字节替换
        state = shiftRow(state);
        state = invSubBytes(state);
        
        // 第2轮：逆列混淆 + 逆行移位 + 逆半字节替换
        state = xorBinary(state, w2 + w3);
        state = invMixColumns(state);
        state = shiftRow(state);
        state = invSubBytes(state);
        
        // 第3轮：密钥加
        state = xorBinary(state, key);
        
        return state;
    }
    
    /**
     * 测试S-AES功能
     */
    public static void main(String[] args) {
        System.out.println("=== S-AES测试 ===");
        
        String plaintext = "1010110101010101";
        String key = "1100110011001100";
        
        System.out.println("明文: " + plaintext);
        System.out.println("密钥: " + key);
        
        String ciphertext = encrypt(plaintext, key);
        System.out.println("密文: " + ciphertext);
        
        String decrypted = decrypt(ciphertext, key);
        System.out.println("解密: " + decrypted);
        
        boolean success = plaintext.equals(decrypted);
        System.out.println("测试结果: " + (success ? "成功" : "失败"));
    }
}