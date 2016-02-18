package demo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The persistent class for the acl_entry database table.
 */
@Entity
@Table(name = "acl_entry")
@NamedQuery(name = "AclEntry.findAll", query = "SELECT a FROM AclEntry a")
public class AclEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.AUTO) private String id;

	@Column(name = "ace_order") private int order;

	@Column(name = "audit_failure") private byte auditFailure;

	@Column(name = "audit_success") private byte auditSuccess;

	private byte granting;

	private int mask;

	// bi-directional many-to-one association to AclSid
	@ManyToOne @JoinColumn(name = "sid") private AclSid sid;

	// bi-directional many-to-one association to AclObjectIdentity
	@ManyToOne @JoinColumn(name = "acl_object_identity") private AclObjectIdentity objectIdentity;

	public AclEntry() {}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public byte getAuditFailure() {
		return this.auditFailure;
	}

	public void setAuditFailure(byte auditFailure) {
		this.auditFailure = auditFailure;
	}

	public byte getAuditSuccess() {
		return this.auditSuccess;
	}

	public void setAuditSuccess(byte auditSuccess) {
		this.auditSuccess = auditSuccess;
	}

	public byte getGranting() {
		return this.granting;
	}

	public void setGranting(byte granting) {
		this.granting = granting;
	}

	public int getMask() {
		return mask;
	}

	public void setMask(int mask) {
		this.mask = mask;
	}

	public AclSid getSid() {
		return sid;
	}

	public void setSid(AclSid sid) {
		this.sid = sid;
	}

	public AclObjectIdentity getObjectIdentity() {
		return objectIdentity;
	}

	public void setObjectIdentity(AclObjectIdentity objectIdentity) {
		this.objectIdentity = objectIdentity;
	}

}
