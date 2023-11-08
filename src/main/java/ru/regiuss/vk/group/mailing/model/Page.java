package ru.regiuss.vk.group.mailing.model;

import lombok.*;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;

@Entity
@Table(name = "pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Page implements Persistable<Integer> {
    @Id
    @NonNull
    @Column(name = "page_id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "subscribers", nullable = false)
    private int subscribers;

    @Column(name = "icon", nullable = false)
    private String icon;

    @Column(name = "page_type")
    @Enumerated(EnumType.STRING)
    private PageType type;

    private transient boolean isNew;

    @Override
    public boolean isNew() {
        return isNew;
    }

    public String getLink() {
        String baseUrl = "https://vk.com/";
        switch (type) {
            case USER:
                baseUrl += "id";
                break;
            case GROUP:
                baseUrl += "club";
        }
        return baseUrl + id;
    }
}
