package space.regiuss.vk.mailing.wrapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DescriptionItemWrapper<T> extends ImageItemWrapper<T> {

    private String description;

    public DescriptionItemWrapper(T item) {
        super(item);
    }

    public DescriptionItemWrapper(T item, String description) {
        super(item);
        this.description = description;
    }
}
