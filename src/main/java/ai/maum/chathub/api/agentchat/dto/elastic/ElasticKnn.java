package ai.maum.chathub.api.agentchat.dto.elastic;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ElasticKnn {
    private String field;
    private List<Double> query_vector;
    private int k;
    private int num_candidates;
}
