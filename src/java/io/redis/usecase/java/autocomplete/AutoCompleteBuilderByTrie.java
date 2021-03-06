package io.redis.usecase.java.autocomplete;

import io.redis.usecase.java.JedisHelper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.util.MurmurHash;

public class AutoCompleteBuilderByTrie {
    private final Jedis jedis;
    private static final int SEED_MURMURHASH = 0x1234ABCD;

    public AutoCompleteBuilderByTrie(JedisHelper jedisHelper) {
        jedis = jedisHelper.getConnection();
    }

    /**
     * Add a phrase to autocomplete dic.
     * @param phrase
     * @param score
     */
    public void addPhrase(String phrase, int score) {
        Pipeline pipeline = jedis.pipelined();

        String phraseId = Long.toString(MurmurHash.hash64A(phrase.getBytes(), SEED_MURMURHASH));

        KoreanSoundExtractor koreanSoundExtractor = new KoreanSoundExtractor();
        String result = koreanSoundExtractor.getSoundExtractedString(phrase);

        StringBuilder builder = new StringBuilder(result.length());
        for (char element : result.toCharArray()) {
            builder.append(element);
            pipeline.zadd(builder.toString(), score, phraseId);
        }

        // set phrase to redis by string data type
        pipeline.set(phraseId, phrase);

        pipeline.sync();
    }
}
