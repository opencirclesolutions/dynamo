package ${package}.application.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.AttributeOrder;

@Entity
@AttributeOrder(attributeNames = { "id", "stringAttribute", "dateAttribute" })
public class MyEntity extends AbstractEntity<Integer> {

    private static final long serialVersionUID = -6342590031746525068L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Attribute(searchable = true)
    private String stringAttribute;

    @Attribute(dateType = AttributeDateType.DATE)
    private Date dateAttribute;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStringAttribute() {
        return stringAttribute;
    }

    public void setStringAttribute(String stringAttribute) {
        this.stringAttribute = stringAttribute;
    }

    public Date getDateAttribute() {
        return dateAttribute;
    }

    public void setDateAttribute(Date dateAttribute) {
        this.dateAttribute = dateAttribute;
    }
}
