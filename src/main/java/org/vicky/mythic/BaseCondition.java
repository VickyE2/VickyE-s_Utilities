/* Licensed under Apache-2.0 2024. */
package org.vicky.mythic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;

public interface BaseCondition extends IEntityCondition {
  @Override
  boolean check(AbstractEntity abstractEntity);
}
