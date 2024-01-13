package space.regiuss.vk.mailing.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "pages")
@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class Page {
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

    private transient boolean canMessage;

    @Column(name = "page_type")
    @Enumerated(EnumType.STRING)
    private PageType type;

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
