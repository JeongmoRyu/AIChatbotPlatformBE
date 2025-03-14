package ai.maum.chathub.api.skins.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class MemberResult {
    private String memberInfo;
    private String consultInfo;
    private String measureInfo;
    private String geneInfo;

    public MemberResult(String memberInfo, String consultInfo, String measureInfo, String geneInfo) {
        this.memberInfo = memberInfo;
        this.measureInfo = measureInfo;
        this.geneInfo = geneInfo;
        this.consultInfo = consultInfo;
    }

    public MemberResult() {
    }

    public MemberResult(HashMap<String,String> infoMap) {
        this.memberInfo = infoMap.get("member_info")==null?"":infoMap.get("member_info");
        this.consultInfo = infoMap.get("consult_info")==null?"":infoMap.get("consult_info");
        this.measureInfo = infoMap.get("measure_info")==null?"":infoMap.get("measure_info");
        this.geneInfo = infoMap.get("gene_info")==null?"":infoMap.get("gene_info");
    }
}
