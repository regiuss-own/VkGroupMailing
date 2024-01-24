package space.regiuss.vk.mailing.wrapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailItemWrapper<T> extends ImageItemWrapper<T> {

    private String email;

    public EmailItemWrapper(T item) {
        super(item);
    }

    public EmailItemWrapper(T item, String email) {
        super(item);
        this.email = email;
    }
}
