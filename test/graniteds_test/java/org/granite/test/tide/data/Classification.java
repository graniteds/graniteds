package org.granite.test.tide.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;


@Entity
public class Classification extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="classification_hierarchy", 
            joinColumns={ @JoinColumn(name="parent_id") }, 
            inverseJoinColumns={ @JoinColumn(name="child_id") }
    )
    @OrderColumn(name="rank", nullable=false)
    private List<Classification> subClassifications = new ArrayList<Classification>();

    @ManyToMany(mappedBy="subClassifications", cascade=CascadeType.ALL)
    private Set<Classification> superClassifications = new HashSet<Classification>();

    @Column(name="classification_code", nullable=false)
    private String code;
    
    
    public Classification() {
    }
    
    public Classification(Long id, Long version, String uid) {
        super(id, version, uid);
    }
    
    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public List<Classification> getSubClassifications() {
        return this.subClassifications;
    }

    public void setSubClassifications(List<Classification> subClassifications) {
        this.subClassifications = subClassifications;
    }

    public Set<Classification> getSuperClassifications() {
        return this.superClassifications;
    }

    public void setSuperClassifications(Set<Classification> superClassifications) {
        this.superClassifications = superClassifications;
    }

}
