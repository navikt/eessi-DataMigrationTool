package eu.ec.dgempl.eessi.rina.model.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.AbstractPersistableWithSid;

@Entity
@Table(name = "temp_attachment")
@NamedQuery(name = "TempAttachment.findAll", query = "SELECT ta FROM TempAttachment ta")
public class TempAttachment extends AbstractPersistableWithSid<TempAttachment> {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "TEMP_ATTACHMENT_SID_GENERATOR", sequenceName = "TEMP_ATTACHMENT_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TEMP_ATTACHMENT_SID_GENERATOR")
    private Long sid;

    @NaturalId
    private String id;

    private String name;

    private String filename;

    private String pathname;

    @Column(name = "created_by")
    private String createdBy;

    private String json;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_case_sid")
    private RinaCase rinaCase;

    @Override
    public Long getSid() {
        return sid;
    }

    @Override
    public void setSid(Long sid) {
        this.sid = sid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String fileName) {
        this.filename = fileName;
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public RinaCase getRinaCase() {
        return rinaCase;
    }

    public void setRinaCase(RinaCase rinaCase) {
        this.rinaCase = rinaCase;
    }
}
