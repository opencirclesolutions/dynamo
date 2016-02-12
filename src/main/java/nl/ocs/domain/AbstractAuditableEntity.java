package nl.ocs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import nl.ocs.domain.model.VisibilityType;
import nl.ocs.domain.model.annotation.Attribute;

/**
 * Base class for entities that store auditing information
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key
 */
@MappedSuperclass
public abstract class AbstractAuditableEntity<ID> extends AbstractEntity<ID> {

	private static final long serialVersionUID = 3347137920794563022L;

	@Attribute(readOnly = true)
	@Column(name = "Created_by")
	private String createdBy;

	@Attribute(readOnly = true)
	@Column(name = "Created_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdOn;

	@Attribute(readOnly = true, showInTable = VisibilityType.HIDE)
	@Column(name = "Changed_by")
	private String changedBy;

	@Attribute(readOnly = true, showInTable = VisibilityType.HIDE)
	@Column(name = "Changed_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Date changedOn;

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	public Date getChangedOn() {
		return changedOn;
	}

	public void setChangedOn(Date changedOn) {
		this.changedOn = changedOn;
	}

}
