package space.regiuss.vk.mailing.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupTaskResult {
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
    private int checkedGroupsCount;
    private int validGroupsCount;
    private List<Page> pages;
}
