package ai.maum.chathub.api.chat.dto.elastic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElasticQuery {
    private Term term;
    @Getter
    @Setter
    public class Term {
        private String text;
        public Term() {

        }
    }
}
