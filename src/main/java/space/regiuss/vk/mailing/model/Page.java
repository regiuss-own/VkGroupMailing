package space.regiuss.vk.mailing.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "pages")
@Getter
@Setter
@NoArgsConstructor
public class Page {

    @EmbeddedId
    private PageId id;

    @Column(name = "name")
    private String name;

    @Column(name = "subscribers", nullable = false)
    private int subscribers;

    @Column(name = "icon", length = 500, nullable = false)
    private String icon;

    @OneToOne()
    @PrimaryKeyJoinColumn
    private PageBlacklist blackList;

    private transient boolean canMessage;

    public String getLink() {
        String baseUrl = "https://vk.com/";
        switch (id.getPageType()) {
            case USER:
                baseUrl += "id";
                break;
            case GROUP:
                baseUrl += "club";
        }
        return baseUrl + id.getPageId();
    }

}
