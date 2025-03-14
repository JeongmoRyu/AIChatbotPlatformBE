package ai.maum.chathub.api.execinfo;

import ai.maum.chathub.api.common.BaseField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="exec_info")
public class ExecInfoEntity extends BaseField {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String result;
    private String api;
    // FIXME mongodb to maria
    //private Object params;
    private long time;
    private String ip;
}
