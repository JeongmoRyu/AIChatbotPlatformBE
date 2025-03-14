package ai.maum.chathub.api.member.entity;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MemberOrganizationId implements Serializable {
    private Long memberId;
    private Long organizationId;
}