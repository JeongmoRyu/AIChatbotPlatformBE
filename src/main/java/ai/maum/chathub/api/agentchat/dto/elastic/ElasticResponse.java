package ai.maum.chathub.api.agentchat.dto.elastic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticResponse {
    private int took;
    private boolean timed_out;
    private Shards _shards;
    private Hits hits;
    private Aggregations aggregations;

    public ElasticResponse() {
    }

    public ElasticResponse(String jsonString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
        objectMapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, false);
        ElasticResponse elasticResponse = objectMapper.readValue(jsonString, ElasticResponse.class);
        this.took = elasticResponse.took;
        this.timed_out = elasticResponse.timed_out;
        this._shards = elasticResponse._shards;
        this.hits = elasticResponse.hits;
        this.aggregations = elasticResponse.aggregations;
    }

    public void initializeVectors() {
        if(hits != null && hits.hits != null) {
            for(Hits.Hit hit : hits.hits) {
                if(hit._source != null) {
                    hit._source.vector = new ArrayList<>();
                }
            }
        }
    }
    // Existing getters and setters...

    // Method to extract all 'text' fields from hits
    public List<String> extractAllTexts() {
        List<String> texts = new ArrayList<>();
        if (hits != null && hits.hits != null) {
            for (Hits.Hit hit : hits.hits) {
                if (hit._source != null && hit._source.text != null) {
                    texts.add(hit._source.text);
                }
            }

        }
        return texts;
    }

    @Getter
    @Setter
    public static class Shards {
        private int total;
        private int successful;
        private int skipped;
        private int failed;

        // getters and setters
    }

    @Getter
    @Setter
    public static class Hits {
        private Total total;
        private Object max_score;
        private List<Hit> hits;

        // Existing getters and setters...

        @Getter
        @Setter
        public static class Total {
            private int value;
            private String relation;

            // getters and setters
        }

        @Getter
        @Setter
        public static class Hit {
            private String _index;
            private String _id;
            private Object _score;
            private int _rank;
            private List<String> _ignored;
            private Source _source;

            // Existing getters and setters...

            @Getter
            @Setter
            public static class Source {
                private String text;
                private Metadata metadata;
                private List<Double> vector;

                // Existing getters and setters...
                public void setVector(List<Double> vector) {
                    this.vector = vector;
                }

                @Getter
                @Setter
                public static class Metadata {
                    private String source;
                    private Integer page;
                    private String uuid;

                    // getters and setters
                }
            }
        }
    }

    @Getter
    @Setter
    public static class Aggregations {
        private IntCount int_count;

        // Existing getters and setters...

        @Getter
        @Setter
        public static class IntCount {
            private int doc_count_error_upper_bound;
            private int sum_other_doc_count;
            private List<Object> buckets;

            // getters and setters
        }
    }
}