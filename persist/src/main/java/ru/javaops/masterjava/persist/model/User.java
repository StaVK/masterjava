package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class User extends BaseEntity {
    @Column("full_name")
    private @NonNull String fullName;
    private @NonNull String email;
    private @NonNull UserFlag flag;
    private String code;

    public User(Integer id, String fullName, String email, UserFlag flag, String code) {
        this(fullName, email, flag, code);
        this.id=id;
    }
    public User(String fullName, String email, UserFlag flag, String code){
        this.fullName=fullName;
        this.email=email;
        this.flag=flag;
        this.code=code;
    }
}