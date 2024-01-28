package space.regiuss.vk.mailing.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "page_blacklist")
@Getter
@Setter
@NoArgsConstructor
public class PageBlacklist {

    @EmbeddedId
    private PageId id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Page page;

}
