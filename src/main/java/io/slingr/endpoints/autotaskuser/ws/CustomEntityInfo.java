package io.slingr.endpoints.autotaskuser.ws;

import java.util.Date;
import java.util.List;

public class CustomEntityInfo {
    private String account;
    private EntityType entityType;
    private Date lastPolling = new Date();
    private List<EntityFieldInfo> userDefinedFields;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Date getLastPolling() {
        return lastPolling;
    }

    public void setLastPolling(Date lastPolling) {
        this.lastPolling = lastPolling;
    }

    public List<EntityFieldInfo> getUserDefinedFields() {
        return userDefinedFields;
    }

    public void setUserDefinedFields(List<EntityFieldInfo> userDefinedFields) {
        this.userDefinedFields = userDefinedFields;
    }
}
