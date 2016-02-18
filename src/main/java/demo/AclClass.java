package demo;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the acl_class database table.
 */
@Entity
@Table(name = "acl_class")
public class AclClass implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.AUTO) private String id;

	@Column(name = "class") private String class_;

	// bi-directional many-to-one association to AclObjectIdentity
	@OneToMany(mappedBy = "aclClass") private List<AclObjectIdentity> objectIdentities;

	public AclClass() {}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClass_() {
		return this.class_;
	}

	public void setClass_(String class_) {
		this.class_ = class_;
	}

	public List<AclObjectIdentity> getObjectIdentities() {
		return objectIdentities;
	}

	public void setObjectIdentities(List<AclObjectIdentity> objectIdentities) {
		this.objectIdentities = objectIdentities;
	}

	public AclObjectIdentity addAclObjectIdentity(AclObjectIdentity aclObjectIdentity) {
		getObjectIdentities().add(aclObjectIdentity);
		aclObjectIdentity.setAclClass(this);

		return aclObjectIdentity;
	}

	public AclObjectIdentity removeAclObjectIdentity(AclObjectIdentity aclObjectIdentity) {
		getObjectIdentities().remove(aclObjectIdentity);
		aclObjectIdentity.setAclClass(null);

		return aclObjectIdentity;
	}

}
