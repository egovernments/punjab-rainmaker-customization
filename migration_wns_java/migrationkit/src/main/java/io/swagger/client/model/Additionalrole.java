/*
 * eGov Property Registry System.
 * APIs for Property Registry module. This provide APIs for create new property, update existing property, search existing property. 
 *
 * OpenAPI spec version: 1.0.0
 * Contact: contact@egovernments.org
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * User role carries the tenant related role information for the user. A user can have multiple roles per tenant based on the need of the tenant. A user may also have multiple roles for multiple tenants.
 */
public class Additionalrole {
  @JsonProperty("tenantId")
  private String tenantId = null;

  @JsonProperty("roles")
  private List<Primaryrole> roles = new ArrayList<Primaryrole>();

  public Additionalrole tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

   /**
   * tenantid for the tenant
   * @return tenantId
  **/
  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Additionalrole roles(List<Primaryrole> roles) {
    this.roles = roles;
    return this;
  }

  public Additionalrole addRolesItem(Primaryrole rolesItem) {
    this.roles.add(rolesItem);
    return this;
  }

   /**
   * Roles assigned for a particular tenant - array of role codes/names
   * @return roles
  **/
  public List<Primaryrole> getRoles() {
    return roles;
  }

  public void setRoles(List<Primaryrole> roles) {
    this.roles = roles;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Additionalrole additionalrole = (Additionalrole) o;
    return Objects.equals(this.tenantId, additionalrole.tenantId) &&
        Objects.equals(this.roles, additionalrole.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantId, roles);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Additionalrole {\n");
    
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}