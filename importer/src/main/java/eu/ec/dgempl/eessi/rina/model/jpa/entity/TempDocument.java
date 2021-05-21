package eu.ec.dgempl.eessi.rina.model.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.NaturalId;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentDirection;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.AbstractPersistableWithSid;

@Entity
@Table(name = "temp_document")
@NamedQuery(name = "TempDocument.findAll", query = "SELECT td FROM TempDocument td")
public class TempDocument extends AbstractPersistableWithSid<TempDocument> {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "TEMP_DOCUMENT_SID_GENERATOR", sequenceName = "TEMP_DOCUMENT_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TEMP_DOCUMENT_SID_GENERATOR")
    private Long sid;

    @NotBlank
    @NaturalId
    private String id;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EDocumentStatus status;

    @Enumerated(EnumType.STRING)
    private EDocumentDirection direction;

    private String type;

    @Column(name = "is_starter")
    private Boolean isStarter;

    @Column(name = "parent_document_id")
    private String parentDocumentId;

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

    public EDocumentStatus getStatus() {
        return status;
    }

    public void setStatus(EDocumentStatus status) {
        this.status = status;
    }

    public EDocumentDirection getDirection() {
        return direction;
    }

    public void setDirection(EDocumentDirection direction) {
        this.direction = direction;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getStarter() {
        return isStarter;
    }

    public void setStarter(Boolean starter) {
        isStarter = starter;
    }

    public String getParentDocumentId() {
        return parentDocumentId;
    }

    public void setParentDocumentId(String parentDocumentId) {
        this.parentDocumentId = parentDocumentId;
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
