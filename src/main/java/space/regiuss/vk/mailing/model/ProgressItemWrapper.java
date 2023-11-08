package space.regiuss.vk.mailing.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgressItemWrapper<T> extends ImageItemWrapper<T> {

    protected int progress = -1;
    protected int total = -1;

    public ProgressItemWrapper(T item) {
        super(item);
    }
}
