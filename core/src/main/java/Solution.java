
import java.util.HashMap;

class Solution {
    public static void main(String[] args) {
        int result = new Solution().respace(new String[]{
                "aaysaayayaasyya", "yyas", "yayysaaayasasssy", "yaasassssssayaassyaayaayaasssasysssaaayysaaasaysyaasaaaaaasayaayayysasaaaa", "aya", "sya", "ysasasy", "syaaaa", "aaaas", "ysa", "a", "aasyaaassyaayaayaasyayaa", "ssaayayyssyaayyysyayaasaaa", "aya", "aaasaay", "aaaa", "ayyyayssaasasysaasaaayassasysaaayaassyysyaysaayyasayaaysyyaasasasaayyasasyaaaasysasy", "aaasa", "ysayssyasyyaaasyaaaayaaaaaaaaassaaa", "aasayaaaayssayyaayaaaaayaaays", "s"
        }, "asasayaayaassayyayyyyssyaassasaysaaysaayaaaaysyaaaa");

        System.out.println(result);
    }

    public int respace(String[] dictionary, String sentence) {
        // 将字典变成字符串和其长度的映射
        HashMap<String, Integer> map = toMap(dictionary);
        int[] dp = new int[sentence.length() + 1];
        for (int i = 0; i < sentence.length(); i++) {
            // 算出每次匹配最长的字典长度
            int longestMatch = longestMatch(map, sentence.substring(0, i + 1));
            dp[i + 1] = Math.max(dp[i], longestMatch == -1 ? dp[i] : dp[i + 1 - longestMatch] + longestMatch);
        }
        return sentence.length() - dp[sentence.length()];
    }

    private HashMap<String, Integer> toMap(String[] dictionary) {
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < dictionary.length; i++) {
            map.put(dictionary[i], dictionary[i].length());
        }
        return map;
    }

    private int longestMatch(HashMap<String, Integer> dic, String str) {
        int maxLen = -1;
        for (int i = str.length() - 1; i >= 0; i--) {
            Integer len = dic.get(str.substring(i));
            if (len != null) {
                maxLen = Math.max(len, maxLen);
            }
        }
        return maxLen;
    }
}
