// from https://www.programmersought.com/article/4084449403/

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimHash {
    private String text;
    private final BigInteger simHash; // character produced hash value
    private final int hashBits = 64; // the number of hashes after the word segmentation


    public SimHash(String tokens) {
        this.text = tokens;
        this.simHash = this.simHash();
    }


    // Clear html tags
    private String cleanHtmlTagsAndNormalize(String content) {
        // If the input is HTML, the following will filter out all the HTML tags.
        content = Jsoup.clean(content, Whitelist.none()).toLowerCase();
        String[] strings = {" ", "\n", "\r", "\t", "\\r", "\\n", "\\t", "&nbsp;"};
        for (String s : strings) {
            content = content.replaceAll(s, "");
        }
        return content;
    }

    // This is a hash calculation of the entire string
    private BigInteger simHash() {
        text = cleanHtmlTagsAndNormalize(text);
        List<Term> tokens = StandardTokenizer.segment(this.text); // Segmentation of strings
        int[] v = new int[this.hashBits];
        //Some special treatment of the word segmentation
        // For example:add weight according to part of speech, filter out punctuation, filter overclocking vocabulary, etc.
        Map<String, Integer> weightOfNature = new HashMap<>(); // weight of part of speech
        weightOfNature.put("n", 2); //The weight given to the noun is 2;
        Map<String, String> stopNatures = new HashMap<>();//Deactivated part of speech such as some punctuation;
        stopNatures.put("w", ""); //
        int overCount = 5; //Set the bounds of the overclocked vocabulary;
        Map<String, Integer> wordCount = new HashMap<>();
        for (Term term : tokens) {
            String word = term.word; //word segmentation string
            String nature = term.nature.toString(); // word segmentation attribute;
            // Filter overclocking words
            if (wordCount.containsKey(word)) {
                int count = wordCount.get(word);
                if (count > overCount) {
                    continue;
                }
                wordCount.put(word, count + 1);
            } else {
                wordCount.put(word, 1);
            }
            // Filter stop words
            if (stopNatures.containsKey(nature)) {
                continue;
            }
            // 2. Divide each participle hash into a fixed-length sequence. For example, an integer of 64bit.
            BigInteger t = this.hash(word);
            for (int i = 0; i < this.hashBits; i++) {
                BigInteger bitmask = new BigInteger("1").shiftLeft(i);
                // 3. Create an array of integers of length 64 (assuming you want to generate a 64-bit digital fingerprint, or other numbers).
                // Judge the sequence after each participle hash. If it is 1000...1, then the first and last digits of the array are incremented by 1.
                // The middle 62 points are decremented by one, that is, every 1 plus 1 is counted as 0 minus 1. Until all the word breakers are counted.
                int weight = 1; //add weight
                if (weightOfNature.containsKey(nature)) {
                    weight = weightOfNature.get(nature);
                }
                if (t.and(bitmask).signum() != 0) {
                    // Here is the vector sum that computes all the features of the entire document
                    v[i] += weight;
                } else {
                    v[i] -= weight;
                }
            }
        }
        BigInteger fingerprint = new BigInteger("0");
        for (int i = 0; i < this.hashBits; i++) {
            if (v[i] >= 0) {
                fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
            }
        }
        return fingerprint;
    }


    // Perform a hash calculation on a single participle;
    private BigInteger hash(String source) {
        if (source == null || source.length() == 0) {
            return new BigInteger("0");
        } else {
            /*
             * When the length of the source is too short, the hash algorithm will be invalidated,
             * so it is necessary to compensate for too short words.
             */
            while (source.length() < 3) {
                source = source + source.charAt(0);
            }
            char[] sourceArray = source.toCharArray();
            BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
            BigInteger m = new BigInteger("1000003");
            BigInteger mask = new BigInteger("2").pow(this.hashBits).subtract(new BigInteger("1"));
            for (char item : sourceArray) {
                BigInteger temp = BigInteger.valueOf(item);
                x = x.multiply(m).xor(temp).and(mask);
            }
            x = x.xor(new BigInteger(String.valueOf(source.length())));
            if (x.equals(new BigInteger("-1"))) {
                x = new BigInteger("-2");
            }
            return x;
        }
    }

    // Calculate the Hamming distance, the smaller the Hamming distance, the more similar;
    private int hammingDistance(SimHash other) {
        BigInteger m = new BigInteger("1").shiftLeft(this.hashBits).subtract(
                new BigInteger("1"));
        BigInteger x = this.simHash.xor(other.simHash).and(m);
        int tot = 0;
        while (x.signum() != 0) {
            tot += 1;
            x = x.and(x.subtract(new BigInteger("1")));
        }
        return tot;
    }

    public double getSemblance(SimHash s2) {
        double i = this.hammingDistance(s2);
        return 1 - i / this.hashBits;
    }

    public static double getSemblance(BigInteger simHash1, BigInteger simHash2) {

        return 1;
    }

}

