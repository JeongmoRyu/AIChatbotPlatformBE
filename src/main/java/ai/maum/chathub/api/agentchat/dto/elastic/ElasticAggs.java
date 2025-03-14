package ai.maum.chathub.api.agentchat.dto.elastic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElasticAggs {

    private IntCount int_count;
    @Getter
    @Setter
    public class IntCount {
        private Terms terms;
        public IntCount() {

        }
        @Getter
        @Setter
        public class Terms {
            private String field;
            public Terms() {

            }
        }
    }
}
