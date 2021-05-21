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

import org.hibernate.annotations.NaturalId;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EActionStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.AbstractPersistableWithSid;

@Entity
@Table(name = "temp_action")
@NamedQuery(name = "TempAction.findAll", query = "SELECT ta FROM TempAction ta")
public class TempAction extends AbstractPersistableWithSid<TempAction> {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "TEMP_ACTION_SID_GENERATOR", sequenceName = "TEMP_ACTION_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TEMP_ACTION_SID_GENERATOR")
    private Long sid;

    @NaturalId
    private String id;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EActionStatus status;

    @Column(name = "name")
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_id")
    private String documentId;

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

    public EActionStatus getStatus() {
        return status;
    }

    public void setStatus(EActionStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getParentDocumentId() {
        return parentDocumentId;
    }

    public void setParentDocumentId(String parentDocumentId) {
        this.parentDocumentId = parentDocumentId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
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
