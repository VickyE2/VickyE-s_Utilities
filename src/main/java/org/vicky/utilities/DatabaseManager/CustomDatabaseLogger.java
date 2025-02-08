/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.DatabaseManager;

import static org.vicky.global.Global.configManager;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.*;
import org.hibernate.persister.entity.EntityPersister;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;

public class CustomDatabaseLogger
    implements PostInsertEventListener,
        PostUpdateEventListener,
        PostDeleteEventListener,
        LoadEventListener,
        RefreshEventListener,
        ReplicateEventListener,
        PostLoadEventListener,
        PersistEventListener,
        MergeEventListener {

  private ContextLogger logger = new ContextLogger(ContextLogger.ContextType.HIBERNATE, "LOG");

  @Override
  public void onPostInsert(PostInsertEvent event) {
    Object entity = event.getEntity();
    logChange("Inserted", entity, null);
  }

  @Override
  public void onPostUpdate(PostUpdateEvent event) {
    Object entity = event.getEntity();
    StringBuilder changes = new StringBuilder();

    // Detect changes made to fields
    EntityPersister persister = event.getPersister();
    Object[] oldState = event.getOldState();
    Object[] newState = event.getState();

    String[] propertyNames = persister.getPropertyNames();
    for (int i = 0; i < propertyNames.length; i++) {
      if (oldState != null && newState != null && !oldState[i].equals(newState[i])) {
        changes.append(
            String.format(
                "Field '%s' ~ Altered '%s' to '%s'. ", propertyNames[i], oldState[i], newState[i]));
      }
    }

    logChange("Altered", entity, changes.toString());
  }

  @Override
  public void onPostDelete(PostDeleteEvent event) {
    Object entity = event.getEntity();
    logChange("Removed", entity, null);
  }

  @Override
  public void onLoad(LoadEvent event, LoadType loadType) {
    Object entity = event.getResult();
    if (entity != null) {
      logChange("Loaded", entity, String.format("LoadType: %s", loadType.getName()));
    }
  }

  @Override
  public void onRefresh(RefreshEvent event) {
    Object entity = event.getObject();
    logChange("Refreshed", entity, null);
  }

  @Override
  public void onRefresh(RefreshEvent refreshEvent, RefreshContext refreshContext)
      throws HibernateException {
    Object entity = refreshEvent.getObject();
    logChange("Refreshed", entity, String.format("RefreshContext: %s", refreshContext));
  }

  @Override
  public void onReplicate(ReplicateEvent event) {
    Object entity = event.getObject();
    String replicationMode = event.getReplicationMode().toString();
    logChange("Replicated", entity, "ReplicationMode: " + replicationMode);
  }

  @Override
  public void onPostLoad(PostLoadEvent postLoadEvent) {
    Object entity = postLoadEvent.getEntity();
    logChange("PostLoadEvent", entity, null);
  }

  @Override
  public void onPersist(PersistEvent persistEvent) throws HibernateException {
    Object entity = persistEvent.getObject();
    logChange("Persist", entity, null);
  }

  @Override
  public void onPersist(PersistEvent persistEvent, PersistContext persistContext)
      throws HibernateException {
    Object entity = persistEvent.getObject();
    logChange("Persist", entity, String.format("PersistionContext: %s", persistContext));
  }

  @Override
  public void onMerge(MergeEvent mergeEvent) throws HibernateException {
    Object entity = mergeEvent.getEntity();
    logChange("Merge", entity, null);
  }

  @Override
  public void onMerge(MergeEvent mergeEvent, MergeContext mergeContext) throws HibernateException {
    Object entity = mergeEvent.getEntity();
    logChange("Merge", entity, String.format("MergerContext: %s", mergeContext));
  }

  private void logChange(String operation, Object entity, String details) {
    String entityName = entity.getClass().getSimpleName();
    String entityId = "Unknown ID";

    // Use reflection to get the ID
    try {
      var idField = entity.getClass().getDeclaredField("id"); // Assumes the field is named 'id'
      idField.setAccessible(true);
      entityId = idField.get(entity).toString();
    } catch (NoSuchFieldException | IllegalAccessException e) {
      logger.printBukkit(
          "["
              + ANSIColor.colorize("red[HIBERNATE-DB]")
              + "] Unable to retrieve ID for entity: "
              + entityName,
          true);
    }

    String format =
        String.format(
            "%s ~ %s having Id -> %s)%s",
            operation, entityName, entityId, details != null ? " Details: " + details : "");
    if (configManager == null) logger.printBukkit(format);
    else if (configManager.getBooleanValue("Debug")) logger.printBukkit(format);
  }

  @Override
  public boolean requiresPostCommitHandling(EntityPersister entityPersister) {
    return false;
  }
}
