package space.regiuss.vk.mailing.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import space.regiuss.vk.mailing.NameProvider;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "message_kits")
@Getter
@Setter
@NoArgsConstructor
public class MessageKit implements NameProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_kit_id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "message_kit_id")
    private List<Message> messages;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageKit that = (MessageKit) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
