package ru.regiuss.vk.group.mailing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id", nullable = false)
    private Integer id;

    @Column(name = "file_patch", length = 255, nullable = false)
    private String filePatch;

    @Column(name = "is_document", nullable = false)
    private boolean document;
}
