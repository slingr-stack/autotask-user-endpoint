package io.slingr.endpoints.autotaskuser.ws;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import javax.xml.soap.SOAPException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public enum EntityType {
    ACCOUNT("Account", Entity.class, "LastActivityDate", "CreateDate"),
    ACCOUNT_ALERT("AccountAlert", Entity.class, null, null),
    ACCOUNT_LOCATION("AccountLocation", Entity.class, null, null),
    ACCOUNT_NOTE("AccountNote", Entity.class, "LastModifiedDate", null),
    ACCOUNT_PHYSICAL_LOCATION("AccountPhysicalLocation", Entity.class, null, null),
    ACCOUNT_TEAM("AccountTeam", Entity.class, null, null),
    ACCOUNT_TO_DO("AccountToDo", Entity.class, "LastModifiedDate", "CreateDateTime"),
    ACTION_TYPE("ActionType", Entity.class, null, null),
    ADDITIONAL_INVOICE_FIELD_VALUE("AdditionalInvoiceFieldValue", Entity.class, null, null),
    ALLOCATION_CODE("AllocationCode", Entity.class, null, null),
    APPOINTMENT("Appointment", Entity.class, null, "CreateDateTime"),
    ATTACHMENT_INFO("AttachmentInfo", Entity.class, null, null),
    BILLING_ITEM("BillingItem", Entity.class, null, null),
    BILLING_ITEM_APPROVAL_LEVEL("BillingItemApprovalLevel", Entity.class, null, null),
    BUSINESS_DIVISION("BusinessDivision", Entity.class, null, null),
    BUSINESS_DIVISION_SUBDIVISION("BusinessDivisionSubdivision", Entity.class, null, null),
    BUSINESS_DIVISION_SUBDIVISION_RESOURCE("BusinessDivisionSubdivisionResource", Entity.class, null, null),
    BUSINESS_LOCATION("BusinessLocation", Entity.class, null, null),
    CHANGE_REQUEST_LINK("ChangeRequestLink", Entity.class, null, null),
    CLASSIFICATION_ICON("ClassificationIcon", Entity.class, null, null),
    CLIENT_PORTAL_USER("ClientPortalUser", Entity.class, null, null),
    CONTACT("Contact", Entity.class, "LastActivityDate", "CreateDate"),
    CONTRACT("Contract", Entity.class, null, null),
    CONTRACT_BLOCK("ContractBlock", Entity.class, null, null),
    CONTRACT_COST("ContractCost", Entity.class, null, "CreateDate"),
    CONTRACT_EXCLUSION_ALLOCATION_CODE("ContractExclusionAllocationCode", Entity.class, null, null),
    CONTRACT_EXCLUSION_ROLE("ContractExclusionRole", Entity.class, null, null),
    CONTRACT_FACTOR("ContractFactor", Entity.class, null, null),
    CONTRACT_MILESTONE("ContractMilestone", Entity.class, null, "CreateDate"),
    CONTRACT_NOTE("ContractNote", Entity.class, "LastActivityDate", null),
    CONTRACT_RATE("ContractRate", Entity.class, null, null),
    CONTRACT_RETAINER("ContractRetainer", Entity.class, null, null),
    CONTRACT_ROLE_COST("ContractRoleCost", Entity.class, null, null),
    CONTRACT_SERVICE("ContractService", Entity.class, null, null),
    CONTRACT_SERVICE_ADJUSTMENT("ContractServiceAdjustment", Entity.class, null, null),
    CONTRACT_SERVICE_BUNDLE("ContractServiceBundle", Entity.class, null, null),
    CONTRACT_SERVICE_BUNDLE_ADJUSTMENT("ContractServiceBundleAdjustment", Entity.class, null, null),
    CONTRACT_SERVICE_BUNDLE_UNIT("ContractServiceBundleUnit", Entity.class, null, null),
    CONTRACT_SERVICE_UNIT("ContractServiceUnit", Entity.class, null, null),
    CONTRACT_TICKET_PURCHASE("ContractTicketPurchase", Entity.class, null, null),
    COUNTRY("Country", Entity.class, null, null),
    CURRENCY("Currency", Entity.class, null, null),
    DEPARTMENT("Department", Entity.class, null, null),
    EXPENSE_ITEM("ExpenseItem", Entity.class, null, null),
    EXPENSE_REPORT("ExpenseReport", Entity.class, null, null),
    HOLIDAY("Holiday", Entity.class, null, null),
    HOLIDAY_SET("HolidaySet", Entity.class, null, null),
    INSTALLED_PRODUCT("InstalledProduct", Entity.class, null, "CreateDate"),
    INSTALLED_PRODUCT_TYPE("InstalledProductType", Entity.class, null, null),
    INSTALLED_PRODUCT_TYPE_UDF_ASSOCIATION("InstalledProductTypeUdfAssociation", Entity.class, null, null),
    INTERNAL_LOCATION("InternalLocation", Entity.class, null, null),
    INVENTORY_ITEM("InventoryItem", Entity.class, null, null),
    INVENTORY_ITEM_SERIAL_NUMBER("InventoryItemSerialNumber", Entity.class, null, null),
    INVENTORY_LOCATION("InventoryLocation", Entity.class, null, null),
    INVENTORY_TRANSFER("InventoryTransfer", Entity.class, null, null),
    INVOICE("Invoice", Entity.class, null, "CreateDateTime"),
    INVOICE_TEMPLATE("InvoiceTemplate", Entity.class, null, null),
    NOTIFICATION_HISTORY("NotificationHistory", Entity.class, null, null),
    OPPORTUNITY("Opportunity", Entity.class, "LastActivity", "CreateDate"),
    PAYMENT_TERM("PaymentTerm", Entity.class, null, null),
    PHASE("Phase", Entity.class, "LastActivityDateTime", "CreateDate"),
    PRICE_LIST_MATERIAL_CODE("PriceListMaterialCode", Entity.class, null, null),
    PRICE_LIST_PRODUCT("PriceListProduct", Entity.class, null, null),
    PRICE_LIST_ROLE("PriceListRole", Entity.class, null, null),
    PRICE_LIST_SERVICE("PriceListService", Entity.class, null, null),
    PRICE_LIST_SERVICE_BUNDLE("PriceListServiceBundle", Entity.class, null, null),
    PRICE_LIST_WORK_TYPE_MODIFIER("PriceListWorkTypeModifier", Entity.class, null, null),
    PRODUCT("Product", Entity.class, null, null),
    PRODUCT_VENDOR("ProductVendor", Entity.class, null, null),
    PROJECT("Project", Entity.class, null, "CreateDateTime"),
    PROJECT_COST("ProjectCost", Entity.class, null, "CreateDate"),
    PROJECT_NOTE("ProjectNote", Entity.class, "LastActivityDate", null),
    PURCHASE_ORDER("PurchaseOrder", Entity.class, null, "CreateDateTime"),
    PURCHASE_ORDER_ITEM("PurchaseOrderItem", Entity.class, null, null),
    PURCHASE_ORDER_RECEIVE("PurchaseOrderReceive", Entity.class, null, null),
    QUOTE("Quote", Entity.class, null, "CreateDate"),
    QUOTE_ITEM("QuoteItem", Entity.class, null, null),
    QUOTE_LOCATION("QuoteLocation", Entity.class, null, null),
    QUOTE_TEMPLATE("QuoteTemplate", Entity.class, null, "CreateDate"),
    RESOURCE("Resource", Entity.class, null, null),
    RESOURCE_ROLE("ResourceRole", Entity.class, null, null),
    RESOURCE_ROLE_DEPARTMENT("ResourceRoleDepartment", Entity.class, null, null),
    RESOURCE_ROLE_QUEUE("ResourceRoleQueue", Entity.class, null, null),
    RESOURCE_SKILL("ResourceSkill", Entity.class, null, null),
    ROLE("Role", Entity.class, null, null),
    SALES_ORDER("SalesOrder", Entity.class, null, null),
    SERVICE("Service", Entity.class, "LastModifiedDate", "CreateDate"),
    SERVICE_BUNDLE("ServiceBundle", Entity.class, "LastModifiedDate", "CreateDate"),
    SERVICE_BUNDLE_SERVICE("ServiceBundleService", Entity.class, null, null),
    SERVICE_CALL("ServiceCall", Entity.class, "LastModifiedDateTime", "CreateDateTime"),
    SERVICE_CALL_TASK("ServiceCallTask", Entity.class, null, null),
    SERVICE_CALL_TASK_RESOURCE("ServiceCallTaskResource", Entity.class, null, null),
    SERVICE_CALL_TICKET("ServiceCallTicket", Entity.class, null, null),
    SERVICE_CALL_TICKET_RESOURCE("ServiceCallTicketResource", Entity.class, null, null),
    SHIPPING_TYPE("ShippingType", Entity.class, null, null),
    SKILL("Skill", Entity.class, null, null),
    SUBSCRIPTION("Subscription", Entity.class, null, null),
    SUBSCRIPTION_PERIOD("SubscriptionPeriod", Entity.class, null, null),
    TASK("Task", Entity.class, "LastActivityDateTime", "CreateDateTime"),
    TASK_NOTE("TaskNote", Entity.class, "LastActivityDate", null),
    TASK_PREDECESSOR("TaskPredecessor", Entity.class, null, null),
    TASK_SECONDARY_RESOURCE("TaskSecondaryResource", Entity.class, null, null),
    TAX("Tax", Entity.class, null, null),
    TAX_CATEGORY("TaxCategory", Entity.class, null, null),
    TAX_REGION("TaxRegion", Entity.class, null, null),
    TICKET("Ticket", Entity.class, "LastActivityDate", "CreateDate"),
    TICKET_ADDITIONAL_CONTACT("TicketAdditionalContact", Entity.class, null, null),
    TICKET_CATEGORY("TicketCategory", Entity.class, null, null),
    TICKET_CATEGORY_FIELD_DEFAULTS("TicketCategoryFieldDefaults", Entity.class, null, null),
    TICKET_CHANGE_REQUEST_APPROVAL("TicketChangeRequestApproval", Entity.class, null, null),
    TICKET_CHECKLIST_ITEM("TicketChecklistItem", Entity.class, null, null),
    TICKET_COST("TicketCost", Entity.class, null, "CreateDate"),
    TICKET_NOTE("TicketNote", Entity.class, "LastActivityDate", null),
    TICKET_SECONDARY_RESOURCE("TicketSecondaryResource", Entity.class, null, null),
    TIME_ENTRY("TimeEntry", Entity.class, "LastModifiedDateTime", "CreateDateTime"),
    USER_DEFINED_FIELD_DEFINITION("UserDefinedFieldDefinition", Entity.class, null, null),
    USER_DEFINED_FIELD_LIST_ITEM("UserDefinedFieldListItem", Entity.class, null, null),
    WORK_TYPE_MODIFIER("WorkTypeModifier", Entity.class, null, null);

    private static final Logger logger = Logger.getLogger(EntityType.class);

    private String name;
    private Class<? extends Entity> clazz;
    private String modifiedField;
    private String createField;
    // TODO in order to be able to scale we need to distribute customInfo because we might have many connected accounts
    private Map<String, CustomEntityInfo> customInfo = new HashMap<>();
    private Map<String, EntityInfo> infoPerZone = new HashMap<>();
    private AutotaskApi autotaskApi = null;
    private Map<String, Date> lastPollingDates = new HashMap<>();
    private Map<String, Long> lastPollingIds = new HashMap<>();

    EntityType(String name, Class<? extends Entity> clazz, String modifiedField, String createField) {
        this.name = name;
        this.clazz = clazz;
        this.modifiedField = modifiedField;
        this.createField = createField;
    }

    public String getName() {
        return name;
    }

    public String getModifiedField() {
        return modifiedField;
    }

    public String getCreateField() {
        return createField;
    }

    public Date getLastPolling(String configId) {
        if (StringUtils.isBlank(configId)) {
            throw new IllegalArgumentException("Config ID cannot be empty");
        }
        Date lastPollingDate = lastPollingDates.get(configId);
        if (lastPollingDate == null) {
            lastPollingDate = new Date();
            lastPollingDates.put(configId, lastPollingDate);
        }
        return lastPollingDate;
    }

    public Date getEffectiveLastPolling(String configId) {
        // create dates are truncated so we can't be precise so we go a day back to avoid missing a record
        // the polling algorithm should discard duplicates
        if (getModifiedField() == null && getCreateField() != null) {
            Date effectiveLastPolling = new Date(getLastPolling(configId).getTime() - 1000*60*60*24);
            return effectiveLastPolling;
        } else {
            return getLastPolling(configId);
        }
    }

    public void setLastPolling(String configId, Date lastPolling) {
        this.lastPollingDates.put(configId, lastPolling);
    }

    public Long getLastPollingId(String configId) {
        return lastPollingIds.get(configId);
    }

    public void setLastPollingId(String configId, Long lastPollingId) {
        lastPollingIds.put(configId, lastPollingId);
    }

    public synchronized EntityInfo getInfo(ApiCredentials credentials) {
        if (StringUtils.isBlank(credentials.getUsername()) || StringUtils.isBlank(credentials.getZoneUrl())) {
            throw new IllegalArgumentException("Credentials are invalid");
        }
        try {
            EntityInfo info = infoPerZone.get(credentials.getZoneUrl());
            if (info == null) {
                // this means we haven't fetched entity info for this zone and we will do it now for all entities,
                // not just this one
                loadEntitiesInfoForZone(credentials);
                info = infoPerZone.get(credentials.getZoneUrl());
            }
            if (info.getFields() == null || info.getFields().isEmpty()) {
                logger.info(String.format("Loading fields information for entity [%s] in zone [%s]", name, credentials.getZoneUrl()));
                List<EntityFieldInfo> allFields = new ArrayList<>();
                allFields.add(EntityFieldInfo.ID);
                allFields.addAll(autotaskApi.getFieldInfo(credentials, this));
                info.setFields(allFields);
                if (info.hasUserDefinedFields()) {
                    // now we try to fetch user defined fields and load them if we haven't done so
                    CustomEntityInfo customEntityInfo = customInfo.get(buildCustomInfoKey(credentials, this));
                    if (customEntityInfo == null) {
                        customEntityInfo = new CustomEntityInfo();
                        customEntityInfo.setAccount(credentials.getUsername());
                        customEntityInfo.setEntityType(this);
                        customEntityInfo.setUserDefinedFields(autotaskApi.getUDFInfo(credentials, this));
                        customInfo.put(buildCustomInfoKey(credentials, this), customEntityInfo);
                    }
                    // if there are user defined fields we will clone the original info and add user defined fields
                    // for this specific user
                    info = info.clone();
                    info.getFields().addAll(customEntityInfo.getUserDefinedFields());
                }
            } else {
                if (info.hasUserDefinedFields()) {
                    // now we try to fetch user defined fields and load them if we haven't done so
                    CustomEntityInfo customEntityInfo = customInfo.get(buildCustomInfoKey(credentials, this));
                    if (customEntityInfo != null) {
                        // if there are user defined fields we will clone the original info and add user defined fields
                        // for this specific user
                        info = info.clone();
                        info.getFields().addAll(customEntityInfo.getUserDefinedFields());
                    }
                }
            }
            return info;
        } catch (SOAPException e) {
            logger.error(String.format("Error initializing fields for entity [%s]", this), e);
        } catch (AutotaskException e) {
            logger.error(String.format("Error initializing fields for entity [%s]", this), e);
        }
        return null;
    }

    public void setInfo(ApiCredentials credentials, EntityInfo info) {
        infoPerZone.put(credentials.getZoneUrl(), info);
    }

    // this method fetches entity info directly from Autotask, skipping the cache, which is important
    // when the user asks for the entity info as permissions can change from user to user
    public EntityInfo getCleanInfo(ApiCredentials credentials) {
        if (StringUtils.isBlank(credentials.getUsername()) || StringUtils.isBlank(credentials.getZoneUrl())) {
            throw new IllegalArgumentException("Credentials are invalid");
        }
        List<EntityInfo> entityInfoList = null;
        try {
            entityInfoList = autotaskApi.getEntityInfo(credentials);
        } catch (SOAPException e) {
            logger.error(String.format("Error loading info for entity [%s] using account [%s]", this, credentials.getUsername()), e);
        } catch (AutotaskException e) {
            logger.error(String.format("Error loading info for entity [%s] using account [%s]", this, credentials.getUsername()), e);
        }
        for (EntityInfo entityInfo : entityInfoList) {
            if (entityInfo.getEntityName().equals(this.getName())) {
                return entityInfo;
            }
        }
        return null;
    }

    // same as above, this method skips cache to return correct permissions for given credentials
    public List<EntityFieldInfo> getCleanEntityFieldsInfo(ApiCredentials credentials) {
        List<EntityFieldInfo> allFields = new ArrayList<>();
        allFields.add(EntityFieldInfo.ID);
        try {
            allFields.addAll(autotaskApi.getFieldInfo(credentials, this));
            allFields.addAll(autotaskApi.getUDFInfo(credentials, this));
        } catch (SOAPException e) {
            logger.error(String.format("Error loading fields info for entity [%s] using account [%s]", this, credentials.getUsername()), e);
        } catch (AutotaskException e) {
            logger.error(String.format("Error loading fields info for entity [%s] using account [%s]", this, credentials.getUsername()), e);
        }
        return allFields;
    }

    private String buildCustomInfoKey(ApiCredentials credentials, EntityType entityType) {
        return credentials.getUsername() + "_" + entityType.getName();
    }

    private void loadEntitiesInfoForZone(ApiCredentials credentials) throws AutotaskException, SOAPException {
        logger.info(String.format("Loading entities information for zone [%s]", credentials.getZoneUrl()));
        List<EntityInfo> entityInfoList = autotaskApi.getEntityInfo(credentials);
        for (EntityInfo entityInfo : entityInfoList) {
            try {
                EntityType entityType = EntityType.getEntityTypeByName(entityInfo.getEntityName());
                if (entityType == null) {
                    logger.error(String.format("Entity type [%s] is not supported by the endpoint", entityInfo.getEntityName()));
                    continue;
                }
                entityType.setInfo(credentials, entityInfo);
            } catch (IllegalArgumentException iae) {
                logger.error(String.format("There is a problem load entity info for [%s]. Probably the endpoint is out of date.", entityInfo.getEntityName()), iae);
            }
        }
    }

    public void setAutotaskApi(AutotaskApi autotaskApi) {
        this.autotaskApi = autotaskApi;
    }

    public Entity newInstance(ApiCredentials credentials, Node xml) {
        try {
            return clazz.getConstructor(ApiCredentials.class, Node.class).newInstance(credentials, xml);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public Entity newInstance(ApiCredentials credentials) {
    	try {
    		Entity entity = clazz.getConstructor(ApiCredentials.class, EntityType.class).newInstance(credentials, this);
    		entity.type = this;
    		return entity;
    	} catch (InstantiationException e) {
    		throw new IllegalStateException(e);
    	} catch(IllegalAccessException e) {
    		throw new IllegalStateException(e);
    	} catch(InvocationTargetException e) {
    		throw new IllegalStateException(e);
    	} catch(NoSuchMethodException e) {
    		throw new IllegalStateException(e);
    	}
    }

    static public EntityType getEntityTypeByName(String name) {
        for (EntityType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("[%s] is not a valid entity type", name));
    }
}
