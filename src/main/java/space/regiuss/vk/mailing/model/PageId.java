package space.regiuss.vk.mailing.model;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class PageId implements Serializable {
    private Integer pageId;
    private PageType pageType;
}
