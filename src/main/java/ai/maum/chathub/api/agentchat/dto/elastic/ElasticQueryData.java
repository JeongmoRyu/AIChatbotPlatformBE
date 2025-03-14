package ai.maum.chathub.api.agentchat.dto.elastic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElasticQueryData {
    private ElasticQuery query;
    private ElasticKnn knn;
    private ElasticRank rank;
    int size;
    private ElasticAggs aggs;
}
