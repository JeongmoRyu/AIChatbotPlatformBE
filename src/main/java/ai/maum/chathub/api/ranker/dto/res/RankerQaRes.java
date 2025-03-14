package ai.maum.chathub.api.ranker.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RankerQaRes {
    private Long rowNumber;
    private Long id;
    private String question;
    private String answer;
    private Integer docId;
    private String chunk;
//    public RankerQaRes(Long rowNumber, Long id, String question, String answer, Integer docId, String chunk) {
//        this.rowNumber = rowNumber;
//        this.id = id;
//        this.question = question;
//        this.answer = answer;
//        this.docId = docId;
//        this.chunk = chunk;
//    }
}
