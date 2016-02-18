package demo;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the acl_sid database table.
 */
@Entity
@Table(name = "acl_sid")
@NamedQuery(name = "AclSid.findAll", query = "SELECT a FROM AclSid a")
public class AclSid implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.AUTO) private String id;

	private byte principal;

	private String sid;

	// bi-directional many-to-one association to AclEntry
	@OneToMany(mappedBy = "sid") private List<AclEntry> aclEntries;

	// bi-directional many-to-one association to AclObjectIdentity
	@OneToMany(mappedBy = "ownerSid") private List<AclObjectIdentity> objectIdentities;

	public AclSid() {}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte getPrincipal() {
		return this.principal;
	}

	public void setPrincipal(byte principal) {
		this.principal = principal;
	}

	public String getSid() {
		return this.sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public List<AclEntry> getAclEntries() {
		return this.aclEntries;
	}

	public void setAclEntries(List<AclEntry> aclEntries) {
		this.aclEntries = aclEntries;
	}

	public AclEntry addAclEntry(AclEntry aclEntry) {
		getAclEntries().add(aclEntry);
		aclEntry.setSid(this);

		return aclEntry;
	}

	public AclEntry removeAclEntry(AclEntry aclEntry) {
		getAclEntries().remove(aclEntry);
		aclEntry.setSid(null);

		return aclEntry;
	}

	public List<AclObjectIdentity> getObjectIdentities() {
		return objectIdentities;
	}

	public void setObjectIdentities(List<AclObjectIdentity> objectIdentities) {
		this.objectIdentities = objectIdentities;
	}

	public AclObjectIdentity addAclObjectIdentity(AclObjectIdentity objectIdentity) {
		getObjectIdentities().add(objectIdentity);
		objectIdentity.setOwnerSid(this);

		return objectIdentity;
	}

	public AclObjectIdentity removeAclObjectIdentity(AclObjectIdentity objectIdentity) {
		getObjectIdentities().remove(objectIdentity);
		objectIdentity.setOwnerSid(null);

		return objectIdentity;
	}

}
