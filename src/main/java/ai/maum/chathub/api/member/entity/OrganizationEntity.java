package ai.maum.chathub.api.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "organization", schema = "chathub")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private Set<MemberOrganizationEntity> members;
}
