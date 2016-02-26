package demo;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the acl_object_identity database table.
 */
@Entity
@Table(name = "acl_object_identity")
@NamedQuery(name = "AclObjectIdentity.findAll", query = "SELECT a FROM AclObjectIdentity a")
public class AclObjectIdentity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.AUTO) private String id;

	@Column(name = "entries_inheriting") private byte entriesInheriting;

	@Column(name = "object_id_identity") private BigInteger objectIdIdentity;

	// bi-directional many-to-one association to AclEntry
	@OneToMany(mappedBy = "objectIdentity") private List<AclEntry> aclEntries;

	// bi-directional many-to-one association to AclClass
	@ManyToOne @JoinColumn(name = "object_id_class") private AclClass aclClass;

	// bi-directional many-to-one association to AclSid
	@ManyToOne @JoinColumn(name = "owner_sid") private AclSid ownerSid;

	// bi-directional many-to-one association to AclObjectIdentity
	@ManyToOne @JoinColumn(name = "parent_object") private AclObjectIdentity parentObject;

	// bi-directional many-to-one association to AclObjectIdentity
	@OneToMany(mappedBy = "parentObject") private List<AclObjectIdentity> childObjects;

	public AclObjectIdentity() {}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte getEntriesInheriting() {
		return this.entriesInheriting;
	}

	public void setEntriesInheriting(byte entriesInheriting) {
		this.entriesInheriting = entriesInheriting;
	}

	public BigInteger getObjectIdIdentity() {
		return this.objectIdIdentity;
	}

	public void setObjectIdIdentity(BigInteger objectIdIdentity) {
		this.objectIdIdentity = objectIdIdentity;
	}

	public List<AclEntry> getAclEntries() {
		return this.aclEntries;
	}

	public void setAclEntries(List<AclEntry> aclEntries) {
		this.aclEntries = aclEntries;
	}

	public AclEntry addAclEntry(AclEntry aclEntry) {
		getAclEntries().add(aclEntry);
		aclEntry.setObjectIdentity(this);

		return aclEntry;
	}

	public AclEntry removeAclEntry(AclEntry aclEntry) {
		getAclEntries().remove(aclEntry);
		aclEntry.setObjectIdentity(null);

		return aclEntry;
	}

	public AclClass getAclClass() {
		return this.aclClass;
	}

	public void setAclClass(AclClass aclClass) {
		this.aclClass = aclClass;
	}

	public AclSid getOwnerSid() {
		return ownerSid;
	}

	public void setOwnerSid(AclSid ownerSid) {
		this.ownerSid = ownerSid;
	}

	public AclObjectIdentity getParentObject() {
		return parentObject;
	}

	public void setParentObject(AclObjectIdentity parentObject) {
		this.parentObject = parentObject;
	}

	public List<AclObjectIdentity> getChildObjects() {
		return childObjects;
	}

	public void setChildObjects(List<AclObjectIdentity> childObjects) {
		this.childObjects = childObjects;
	}

	public AclObjectIdentity addAclObjectIdentity(AclObjectIdentity objectIdentity) {
		getChildObjects().add(objectIdentity);
		objectIdentity.setParentObject(this);

		return objectIdentity;
	}

	public AclObjectIdentity removeAclObjectIdentity(AclObjectIdentity aclObjectIdentity) {
		getChildObjects().remove(aclObjectIdentity);
		aclObjectIdentity.setParentObject(null);

		return aclObjectIdentity;
	}

}
