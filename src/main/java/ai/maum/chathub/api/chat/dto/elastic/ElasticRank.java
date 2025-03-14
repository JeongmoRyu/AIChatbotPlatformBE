package ai.maum.chathub.api.chat.dto.elastic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElasticRank {
    private ElasticRankRrf rrf;
    @Getter
    @Setter
    public class ElasticRankRrf {
        private int window_size;
        private int rank_constant;
        public ElasticRankRrf() {

        }
    }
}
