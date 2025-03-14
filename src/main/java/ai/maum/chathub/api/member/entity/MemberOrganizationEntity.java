package ai.maum.chathub.api.member.entity;

import com.slack.api.methods.response.admin.analytics.AdminAnalyticsGetFileResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "member_organization", schema = "chathub")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberOrganizationEntity {
    @EmbeddedId
    private MemberOrganizationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", referencedColumnName = "user_key")
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("organizationId")
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private OrganizationEntity organization;
}