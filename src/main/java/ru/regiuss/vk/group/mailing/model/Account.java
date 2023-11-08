package ru.regiuss.vk.group.mailing.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class Account {
    @Id
    @Column(name = "account_id", nullable = false)
    private Integer id;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "icon", nullable = false, length = 200)
    private String icon;

    public Account(Integer id, String token, String name, String icon) {
        this.id = id;
        this.token = token;
        this.name = name;
        this.icon = icon;
    }
}
